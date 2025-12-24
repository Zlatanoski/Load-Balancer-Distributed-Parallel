import java.util.Arrays;
import java.util.List;

public class ProxyMain {

    public static void main(String[] args) {
        try{
            int proxyPort = 8080;
            List<String> workerAddreses = Arrays.asList(
                    "http://worker1:5001",
                    "http://worker2:5001",
                    "http://worker3:5001"
            );

            ProxyServer proxyServer = new ProxyServer(proxyPort,workerAddreses);
            proxyServer.start();
         }catch(Exception e){
            Logger.error("Failed to start Proxy Server: " + e.getMessage());
        }
    }


}
