import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyServer {

    private int currentWorkerIndex;
    private List<String> workerServers; // list of worker server addresses
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Random random;
    private AtomicInteger[] requestsCounts;


    public ProxyServer(int port ,List<String> workerServers) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.workerServers = workerServers;
        this.currentWorkerIndex = 0;
        this.threadPool = Executors.newFixedThreadPool(9);
        this.random = random;

        this.requestsCounts = new AtomicInteger[workerServers.size()];
        for(int i=0; i < workerServers.size(); i++){
            requestsCounts[i] = new AtomicInteger(0); // set all positions of the atomic integer array to 0 // leter for each we increase by 1
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
    public String randomAssign(){
        int randomIndex = random.nextInt(workerServers.size());
        String worker = workerServers.get(randomIndex);
        return worker;
    }
    public String leastConnAssign(){
        int leastIndex = 0;
        int minCount = Integer.MAX_VALUE;

        for(int i=0; i < requestsCounts.length; i++){
            int count = requestsCounts[i].get();
            if(count < minCount){
                minCount = count;
                leastIndex = i; // get the index that has current minimal number of requests
            }
        }
        requestsCounts[leastIndex].incrementAndGet(); // increment the count for that worker
        String worker = workerServers.get(leastIndex);
        return worker;
    }

    public synchronized String roundRobinAssign() {

        String worker = workerServers.get(currentWorkerIndex); // we grab the current worker server
        currentWorkerIndex = (currentWorkerIndex + 1) % workerServers.size(); // auto reset to 0 when end of list

        return worker;
    }


    // main proxy coordinator , contains the threadpool, receive requests and submit a proxyservertask to server
}
