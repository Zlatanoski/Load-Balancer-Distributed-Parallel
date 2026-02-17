    import java.util.Arrays;
    import java.util.List;
    import java.util.ArrayList;

    public class ProxyMain {

        public static void main(String[] args) {
            try{
                int proxyPort = 8080;

                String workerEnv = System.getenv("WORKER_ADDRESSES");
                List<String> workerAddreses;
                if (workerEnv != null && !workerEnv.trim().isEmpty()) {
                    workerAddreses = Arrays.asList(workerEnv.trim().split(","));
                } else {
                    workerAddreses = Arrays.asList(
                            "worker1:8081",
                            "worker2:8082",
                            "worker3:8083",
                            "worker4:8084",
                            "worker5:8085",
                            "worker6:8086",
                            "worker7:8087",
                            "worker8:8088",
                            "worker9:8089",
                            "worker10:8090"
                    );
                }

                Logger.info("Proxy starting with workers: " + workerAddreses);
                ProxyServer proxyServer = new ProxyServer(proxyPort,workerAddreses);
                proxyServer.start();
             }catch(Exception e){
                Logger.error("Failed to start Proxy Server: " + e.getMessage());
            }
        }


    }
