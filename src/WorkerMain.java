public class WorkerMain {

    public static void main(String[] args) {

        String portStr = System.getenv("WORKER_PORT");
        int port = portStr != null ? Integer.parseInt(portStr) : 5001;


    WorkerServerTask worker = new WorkerServerTask(port);
    Thread workerThread = new Thread(worker);
    workerThread.start();

    try{
        workerThread.join();
    }catch(InterruptedException e){
        Logger.error("Worker thread interrupted: " + e.getMessage());
    }


    }



}
