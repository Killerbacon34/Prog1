/**
 * Author:      Alex DeVries
 * Assignment:  Program 3
 * Class:       CSI 4321 Data Communications
 */
package klab.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to establish receiving incoming download connections
 * The instance of it starts by being called within the class Node
 * and remains running until the Node class terminates, ending the connections
 * the serverSocket within download service if they exist and the node's
 * ability to receive requests for download connections
 */
public class DownloadService implements Runnable{
    /**
     * the max number of threads that can exist at the same time
     */
    private static final int THREAD_POOL_SIZE = 4;
    /**
     * the serverSocket instance to receive download connections
     */
    private final ServerSocket serverSocket;
    /**
     * the thread instance that is used as a back reference for the main
     * node to use to close the connections successfully
     */
    private Thread thread;
    /**
     * the download port to create a serverSocket with to listen to
     * incoming connections
     */
    private final int downloadPort;
    /**
     * boolean to determine when to stop listening to download connections
     */
    private boolean done = false;
    /**
     * the directory path to look for file to download
     */
    private final String directoryPath;
    /**
     * The executorService to manage the threads used for the process
     * of downloading the actual file contents
     */
    private final ExecutorService executorService;

    /**
     * The constructor for the download service
     * @param downloadPort the download port to make a serverSocket instance
     *                     with
     * @param directoryPath the directory path to search for the files to
     *                      download
     * @throws IOException if there was an error creating the serverSocket
     */
    public DownloadService(int downloadPort, String directoryPath)
                                                throws IOException {
        this.thread = new Thread(this);
        this.serverSocket = new ServerSocket(downloadPort);
        this.downloadPort = downloadPort;
        this.directoryPath = directoryPath;
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Run method within the DownloadService instance to create new threads
     * for download connections
     */
    public void run() {
        Logger logger = Logger.getLogger("Node.Log");
        while(!done){
            try{
                Socket currConnection = serverSocket.accept();
                logger.log(Level.INFO, "Socket download connection to : "
                                            + currConnection.getInetAddress());
                this.executorService.submit
                        (new DownloadRunner(currConnection,directoryPath));

            } catch (IOException e) {
                logger.log(Level.INFO,
                        "ServerSocket closed: " + e.getLocalizedMessage(), e);
                break;
            }
        }
    }

    /**
     * start function for this thread
     */
    public void start(){
        this.thread.start();
    }
    /**
     * the close function to properly close all connections within
     * DownloadService
     * @throws IOException if an error occurs while closing connections
     */
    public void close() throws IOException {
        done = true;
        serverSocket.close();
        executorService.shutdownNow();
    }
}
