import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyServer {

    private int currentWorkerIndex;
    private List<String> workerServers; // list of worker server addresses
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Random random;
    private AtomicInteger[] requestsCounts;
    private ScheduledExecutorService healthChecker;
    public AtomicBoolean[] workerHealthStatus; // true if healthy, false if not

    public ProxyServer(int port ,List<String> workerServers) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.workerServers = workerServers;
        this.currentWorkerIndex = 0;
        this.threadPool = Executors.newFixedThreadPool(30);
        this.random = new Random();
        this.workerHealthStatus = new AtomicBoolean[workerServers.size()];

        for(int i=0; i < workerHealthStatus.length; i++){
            workerHealthStatus[i] = new AtomicBoolean(false);
        }

        this.requestsCounts = new AtomicInteger[workerServers.size()];
        for(int i=0; i < workerServers.size(); i++){
            requestsCounts[i] = new AtomicInteger(0); // set all positions of the atomic integer array to 0 // leter for each we increase by 1
        }


        this.healthChecker = Executors.newScheduledThreadPool(1); // single thread for health checking meaning that it will check workers sequentially, if we scale to 3 then parallel 3 threads 3 workers.
        healthChecker.scheduleAtFixedRate(
                ()-> checkAllWorkers(),0,1, TimeUnit.SECONDS
        ); // runnable, when to start, period, time unit

    }

    private void checkAllWorkers() {
        Logger.debug("Checking health !");
        for(int i =0; i < workerServers.size(); i++){
            String workerAddress = workerServers.get(i);
            boolean isHealthy = checkWorkerHealth(workerAddress);
            boolean wasHealthy = workerHealthStatus[i].getAndSet(isHealthy);
            if(!isHealthy && wasHealthy){
                Logger.warn("Worker " + workerAddress + " is not working.");
            }
        }
    }

    private boolean checkWorkerHealth(String workerAddress) {
        String[] parts = workerAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try {
            Socket socket = new Socket(host, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.print("GET /health HTTP/1.1\nHost: " + host + "\n\n");
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            socket.close();

            return response != null && response.contains("200");

        } catch (IOException e) {
            Logger.debug("Worker " + workerAddress + " health check failed: " + e.getMessage());
            return false;
        }
    }

    public void start(){
        Logger.info("Proxy Server started on port " + serverSocket.getLocalPort());
        while(true){
            //new one created for each client connection
            try {
                Socket clientSocket = serverSocket.accept();
                ProxyServerTask proxyTask = new ProxyServerTask( clientSocket, this);
                threadPool.submit(proxyTask);

            } catch (IOException e) {
                Logger.error("Error accepting client connection: " + e.getMessage());
            }
            //


        }

    }
    public synchronized String randomAssign(){
        List<Integer> workerIndexes = new java.util.ArrayList<>();
        for(int i=0; i < workerHealthStatus.length; i++){
            if(workerHealthStatus[i].get()){
                workerIndexes.add(i);
            }
        }
        if(workerIndexes.isEmpty()){
            return null;
        }
        int randomIndex = random.nextInt(workerIndexes.size());
        int workerIndex = workerIndexes.get(randomIndex);
        String worker = workerServers.get(workerIndex);
        return worker;
    }

    public synchronized String leastConnAssign(){
        int leastIndex = -1;
        int minCount = Integer.MAX_VALUE;

        for(int i=0; i < requestsCounts.length; i++){
            if(workerHealthStatus[i].get()){
                int count = requestsCounts[i].get();
                if(count < minCount){
                    minCount = count;
                    leastIndex = i;
                }
            }
        }
        if(leastIndex == -1){
            return null;
        }
        requestsCounts[leastIndex].incrementAndGet();
        String worker = workerServers.get(leastIndex);
        return worker;
    }

    public synchronized String roundRobinAssign() {
        for(int i = 0; i < workerServers.size(); i++){
            int index = (currentWorkerIndex + i) % workerServers.size();
            if(workerHealthStatus[index].get()){
                currentWorkerIndex = (index + 1) % workerServers.size();
                String worker = workerServers.get(index);
                return worker;
            }
        }
        return null;
    }

    //after successfully processed request for the LeastConn algorithm decrease the count by 1 for the requests pending to that worker
    public synchronized void decrementWorkerCount(String workerAddress){
        int index = workerServers.indexOf(workerAddress);
        if(index != -1){
            requestsCounts[index].decrementAndGet();
        }

    }


    // main proxy coordinator , contains the threadpool, receive requests and submit a proxyservertask to server
}
