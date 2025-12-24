import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadBalancerMain {


    public static void main(String[] args) {

        List<String> workerAddresses = Arrays.asList("localhost:5001","localhost:5002","localhost:5003");



        Thread worker1 = new Thread(new WorkerServerTask(5001));
        Thread worker2 = new Thread(new WorkerServerTask(5002));
        Thread worker3 = new Thread(new WorkerServerTask(5003));

        worker1.start();
        worker2.start();
        worker3.start();
        ProxyServer proxyServer = null;
        try {
            proxyServer = new ProxyServer(8080,workerAddresses);
            proxyServer.start();
        } catch (IOException e) {
            Logger.error("Failed to start Proxy Server: " + e.getMessage());
        }

    }

}
