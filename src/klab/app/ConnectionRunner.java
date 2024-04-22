/**
 * Author:      Alex DeVries
 * Assignment:  Program 3
 * Class:       CSI 4321 Data Communications
 */
package klab.app;

import klab.serialization.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * connection class fo managing connections to the node
 */
public class ConnectionRunner implements Runnable {
    /**
     * the server socket to listen with
     */
    private final ServerSocket serverSocket;
    /**
     * the directory path of the files
     */
    private final String directoryPath;
    /**
     * the node port for the node
     */
    private final int localNodePort;
    /**
     * the thread running the connectionRunner class
     */
    private Thread thread;


    /**
     * the download port of the node
     */
    private final int localDownloadPort;
    /**
     * the list of node connections made
     */
    private List<Socket> nodeConnections = new ArrayList<>();
    /**
     * to determine if the connectionRunner should stop
     */
    private boolean done = false;

    /**
     * the constructor for the connectionRunner instance
     * @param localNodePort the node connection port
     * @param directoryPath the file directory path
     * @param localDownloadPort the node download port
     * @throws IOException if an I/O error occurs
     */
    public ConnectionRunner(int localNodePort,
                            String directoryPath,
                            int localDownloadPort) throws IOException {
        this.thread = new Thread(this);
        this.localNodePort = localNodePort;
        this.serverSocket = new ServerSocket(localNodePort);
        this.directoryPath = directoryPath;
        this.localDownloadPort = localDownloadPort;
    }

    /**
     * the run function for the class which get executed when a thread
     * of it is made
     */
    public void run() {
        Logger logger = Logger.getLogger("Node.Log");
        while(!done){
            try {
                Socket currSocket = serverSocket.accept();
                synchronized (Node.connectionList){
                    nodeConnections = new ArrayList<>(Node.connectionList);
                }
                logger.log(Level.INFO, "Adding " +
                        "new connection to connection list: "
                        + currSocket.getInetAddress() + ":"
                        + currSocket.getPort());
                Node.connectionList.add(currSocket);
                logger.log(Level.INFO, "Socket connection to : " +
                        currSocket.getInetAddress() + ":" +
                        currSocket.getPort());
                Node.getCachedService().submit(new ReceiveRunner(currSocket,
                        directoryPath, localDownloadPort));
            }catch (IOException e) {
                logger.log(Level.INFO,
                        "Closing socket: " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * the start function for the class
     */
    public void start(){
        this.thread.start();
    }

    /**
     * the close function for the class and its connections
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        done = true;
        serverSocket.close();
//        service.shutdownNow();
    }
}