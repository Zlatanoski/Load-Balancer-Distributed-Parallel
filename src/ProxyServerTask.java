
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ProxyServerTask implements Runnable { // reception desk
    private Socket clientSocket;
    private ProxyServer proxyServer; // reference to the main ProxyServer // we can access directly roundrobinassign method


    public ProxyServerTask(Socket clientSocket,ProxyServer proxyServer) {
        //needed an algorithm to assign tasks to workers from the workers list
        this.clientSocket = clientSocket;
        this.proxyServer = proxyServer;

    }
    @Override
    public void run() {

        try{
            BufferedReader request = null;
            request = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String requestLine = null; // readLine in order to just read one line until \n
            requestLine = request.readLine();

            String workerAddress = proxyServer.roundRobinAssign(); // get worker using round robin algorithm

            sendRequestToWorker(workerAddress, requestLine, clientSocket);

        }catch(IOException e){
            Logger.error("Error processing the request:" + e.getMessage()  );
        }finally{
            try {
                if(clientSocket != null){
                    clientSocket.close();
                }
            } catch (IOException e) {
                Logger.error("Error while closing the socket " + e.getMessage());
            }

        }
            // accepting connection from client
            //buffered reader since we get only bits
            // getInputStream bytes -> InputStreamReader chars -> BufferedReader strings

            //method to forward request to the chosen worker server
    }

    private void sendRequestToWorker(String workerAddress, String requestLine, Socket clientSocket) { //accepts also client socket since needed again for sending back response
        workerAddress = workerAddress.replace("http://", ""); // Remove http://
        String[] parts = workerAddress.split(":");        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        PrintWriter workerOutput = null;
        Socket workerSocket = null; //creates a new TCP connection to that host with that port // Connect to that machine on that port
        try {
            Logger.debug("Attempting to connect to worker" + workerAddress );
            workerSocket = new Socket(host,port);
            Logger.debug("Connected to worker " + workerAddress );
            workerOutput = new PrintWriter( workerSocket.getOutputStream(),true);
            workerOutput.println(requestLine);
            workerOutput.println(); // Send request to worker1

            //Read the actual response from worker
            BufferedReader workerInput = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
            PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while ((line = workerInput.readLine()) != null) {
                clientOutput.println(line);
            }

        } catch (IOException e) {

            Logger.debug("Error processing the request " + e.getMessage() );

            try{
                PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(),true);
                clientOutput.println("Cannot send error to client " );
            }catch (Exception ex){
                Logger.error(ex.getMessage());
            }

        }finally{
            try {
                if(workerSocket != null){
                    workerSocket.close();
                }
            } catch (IOException e) {
                Logger.error("Error while closing the socket " + e.getMessage());
            }
        }


    }



}
