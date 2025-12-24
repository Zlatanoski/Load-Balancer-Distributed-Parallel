import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerServerTask implements Runnable{

    private int port;
    private ExecutorService threadPool;
    public WorkerServerTask(int port){

        this.threadPool = Executors.newFixedThreadPool(3);
        this.port = port;
    }

    @Override
    public void run(){
            ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            Logger.info("Server started on port " + port);

            while(true){
                    Socket connection = serverSocket.accept();

                    threadPool.submit(()-> processRequest(connection));


            }


        } catch (IOException e) {
            Logger.error("Worker on port " + port + " failed: " + e.getMessage());
        }finally{ // this case gets reached if port is already in use or some other error occurs
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Logger.error("Error while closing server socket on port " + port + ": " + e.getMessage());
                }
            }
        }





    }

    private void processRequest(Socket connection){

        try{
            BufferedReader incomingRequest = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String requestLine = incomingRequest.readLine();
            Logger.debug("Worker on port " + port + " received: " + requestLine);

            String response;
            if(requestLine == null || requestLine.isEmpty()){
                response = notFound();
            }
            else if(requestLine.contains("/health")){
                response = getHealth();
            }else if(requestLine.contains("/work")){
                response = getWork();
            }else{
                response =notFound();
            }

            PrintWriter output = new PrintWriter(connection.getOutputStream(),true);
            output.println(response);

        }catch(IOException e){
            Logger.error("Error processing request on worker port " + port + ": " + e.getMessage());
        }finally{
            if(connection !=null){
                try {
                    connection.close();
                } catch (IOException e) {
                    Logger.error("Error while closing connection " + e.getMessage() );
                }
            }

        }



    }

    private String getHealth(){

        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 3\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                "OK\n";
    }

    private String getWork(){
        Random random = new Random();
        int n =  random.nextInt(10);
        int sum = 0;
        for( int i = 0; i < n; i++ ){
            sum += i;
        }

        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 3\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                "OK\n";
    }
    private String notFound() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: 9\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                "Not Found\n";
    }




}
