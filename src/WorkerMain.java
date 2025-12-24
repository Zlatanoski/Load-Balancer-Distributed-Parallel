public class WorkerMain {

    public static void main(String[] args) {

    int port = 5001;

        String portEnv = System.getenv("WORKER_PORT");
        if (portEnv != null) {
            port = Integer.parseInt(portEnv);
        }
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

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
