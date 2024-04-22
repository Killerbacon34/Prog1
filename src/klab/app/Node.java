/**
 * Author:      Alex DeVries
 * Assignment:  Program 3
 * Class:       CSI 4321 Data Communications
 */
package klab.app;

import klab.serialization.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Node class for processing user input and thread actions
 */
public class Node {
    /**
     * Node default constructor
     */
    Node(){

    }
    /**
     * Random ID Length
     */
    private static final int RANDOM_ID_LENGTH = 15;
    /**
     * the number of command line arguments
     */
    private static final int ARG_LENGTH = 3;
    /**
     * Arbitrary TTL value
     */
    private static final int ARBITRARY_TTL = 50;

    /**
     * number of arguments required for connecting to a peer node
     */
    private static final int CONNECTIONS_ARG_LENGTH = 3;

    /**
     * number of values within a fileID
     */
    private static final int FILE_ID_LENGTH = 8;
    /**
     * number of arguments required for a download connection
     */
    private static final int DOWNLOAD_ARG_LENGTH = 5;
    /**
     * number of bytes extra to reed if an error message is sent by the
     * download service
     */
    private static final int DOWNLOAD_ERROR_MES_LEN = 2;

    /**
     * Logger instance used to log to a file
     */
    private static final Logger LOGGER = Logger.getLogger("Node.Log");

    /**
     * Map for searching with the searchID as the key and the search
     * string as the value
     */
    private static final Map<String,String> searchMap = new HashMap<>();

    /**
     * a map of file names to their random file IDs
     */
    private static final Map<String, Long> fileMapping = new HashMap<>();
    /**
     * ExecutorService for managing senderRunner instances
     */
    private static final ExecutorService service =
                         Executors.newSingleThreadExecutor();

    /**
     * list of socket connections made
     */
    public static final List<Socket> connectionList = new ArrayList<>();

    /**
     * lust of searches that have been made
     */
    public static List<Search> searches = new ArrayList<>();
    /**
     * the executorservice for the connections
     */
    private static final ExecutorService cachedService =
            Executors.newCachedThreadPool();

    /**
     * Static block of code used to create settings for the logger.
     */
    static{
        FileHandler fh;
        ConsoleHandler c;
        try {
            fh = new FileHandler("node.log");
            c = new ConsoleHandler();
            fh.setLevel(Level.ALL);
            c.setLevel(Level.WARNING);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            LOGGER.addHandler(fh);
            LOGGER.addHandler(c);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function to add Searches to a map
     * @param search the search to add to the map
     */
    public static synchronized void addToSearchMap(Search search){
        byte[] temp = search.getID();
        String s = Arrays.toString(temp);
        searchMap.put(s,search.getSearchString());
    }

    /**
     * Gets the filename from the map with filenames mapped to their IDs
     * @param FileID the ID of the file to find
     * @return the name of the file with the given ID
     */
    public static String getFileNameFromMap(long FileID){
        String result = null;
        for (Map.Entry<String, Long> entry : fileMapping.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            if(value == FileID){
                result = key;
            }
        }
        return result;
    }

    /**
     * Get the SearchMap instance from node
     * @return the SearchMap instance
     */
    public static Map<String, String> getSearchMap(){
        return searchMap;
    }

    /**
     * Get the map instance responsible for mapping file names to their IDs
     * @return the map instance with the file names mapped to their IDS
     */
    public static Map<String, Long> getFileMapping() {
        return fileMapping;
    }

    /**
     * Get the ExecutorService instance from the node
     * @return the ExecutorService instance from node
     */
    public static ExecutorService getSingleService(){
        return service;
    }

    /**
     * get cachedthread pool for node connections
     * @return the cachedthreadpool executor service
     */
    public static ExecutorService getCachedService(){
        return cachedService;
    }


    /**
     * goes through list of searches to see if message is inside it
     * @param r the message to look for
     * @return true if the list has the message desired
     */
    public synchronized static boolean checkSearchList(Message r){
        for(Search s : Node.searches){
            if(Arrays.equals(s.getID(), r.getID())){
                return true;
            }
        }
        return false;
    }

    /**
     * the main function within the Node class
     * @param args the command line arguments to be passed in
     * @throws IOException if any parsing errors occur
     * @throws BadAttributeValueException if any invalid message parameters
     *                                    are set
     */
    public static void main(String[] args)
                    throws IOException, BadAttributeValueException {
        Scanner scanner = new Scanner(System.in);
        String currentValue;
        boolean errorPresent = false;
        int localNodePort;
        String directoryPath;
        int localDownloadPort;
        if (args.length != ARG_LENGTH) {
            System.err.println("Argument length invalid");
            errorPresent = true;
            return;
        }

        if(args[1] == null || args[0] == null || args[2] == null){
            System.err.println("One of the arguments is null");
            return;
        }

        localNodePort = Integer.parseInt(args[0]);
        directoryPath = args[1];
        localDownloadPort = Integer.parseInt(args[2]);

        if(localNodePort < 0 || localNodePort > 65535){
            System.err.println("Local node port is not a valid port");
            return;
        }
        if(localDownloadPort < 0 || localDownloadPort > 65535){
            System.err.println("Local download port is not a valid port");
            return;
        }

        Path temp = Paths.get(directoryPath);
        if(Files.notExists(temp)){
            System.err.println("Invalid directory path");
            errorPresent = true;
            return;
        }

        DownloadService downloadService = new
                DownloadService(localDownloadPort, directoryPath);
        ConnectionRunner connectionRunner = new
                ConnectionRunner(localNodePort, directoryPath,
                                    localDownloadPort);

        downloadService.start();
        connectionRunner.start();

        while (!errorPresent) {
            try {
                System.out.print("> ");
                currentValue = scanner.nextLine();
                if (currentValue.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting program...");
                    downloadService.close();
                    connectionRunner.close();
                    for(Socket s : connectionList){
                        s.close();
                    }
                    service.close();
                    cachedService.close();
                    scanner.close();
                    break;
                }
                String[] values = currentValue.split(" ");
                if(values[0].equals("connect") &&
                                    values.length == CONNECTIONS_ARG_LENGTH) {
                    Socket newConnection =
                            new Socket(values[1], Integer.parseInt(values[2]));
                    LOGGER.log(Level.INFO,
                            "Entering in Node through socket: " +
                        Inet4Address.getLocalHost().getHostAddress() +
                        ":" + newConnection.getLocalPort());
                    synchronized (connectionList){
                        if(!connectionList.contains(newConnection)){
                            connectionList.add(newConnection);
                            cachedService.submit(new
                                    ReceiveRunner(newConnection,
                                    directoryPath, localDownloadPort));
                        }
                        else{
                            LOGGER.log(Level.INFO,
                                    "Connection already exists");
                        }
                    }
                }
                else if(values[0].equals("connect") &&
                        values.length < CONNECTIONS_ARG_LENGTH){
                    System.err.println("Bad connect command: " +
                            "Expect connect <node id> <node port>");
                }
                else if(values[0].equals("download") &&
                        values.length == DOWNLOAD_ARG_LENGTH){
                    LOGGER.log(Level.INFO,
                            "Attempting download server " +
                                    "connection through socket: " +
                            values[1] + ":" + values[2]);

                    Socket download = new Socket(values[1],
                            Integer.parseInt(values[2]));

                    LOGGER.log(Level.INFO, "Established " +
                            "download server connection through socket: " +
                            download.getInetAddress() +
                            ":" + download.getPort());

                    MessageOutput downloadOutStream = new
                            MessageOutput(download.getOutputStream());


                    LOGGER.log(Level.INFO,
                            "Attempting to download file with fileID: " +
                            values[3]);

                    if(values[3].length() != FILE_ID_LENGTH){
                        System.err.println("Bad File ID: " + values[3]);
                    }
                    else{
                        downloadOutStream.writeBytes((values[3]+"\n").
                                getBytes(StandardCharsets.US_ASCII));
                        downloadOutStream.flush();
                        MessageInput downloadInStream =
                                new MessageInput(download.getInputStream());
                        byte[] okCheck = downloadInStream.
                                readNBytes(4);

                        if(Arrays.equals(okCheck,
                                new byte[]{'O', 'K', '\n', '\n'})){
                            String filePath =
                                    directoryPath + "\\" + values[4];
                            File df = new File(filePath);
                            synchronized(df){
                                FileOutputStream fOut;
                                if(df.exists()){
                                    LOGGER.log(Level.WARNING,
                                            "File already exists, " +
                                                    "overwriting contents");
                                }
                                fOut = new FileOutputStream(df);
                                download.getInputStream().transferTo(fOut);
                                fOut.close();
                            }
                        }
                        else{
                            byte[] errorPadding =
                                    downloadInStream.
                                            readNBytes(DOWNLOAD_ERROR_MES_LEN);
                            errorPadding = downloadInStream.readBytes();
                            System.err.println(
                                    new String(errorPadding,
                                            StandardCharsets.US_ASCII));
                        }
                        download.close();
                    }
                }
                else if(values.length == 1){
                    LOGGER.log(Level.INFO,
                            "Entered search value of " + currentValue);
                    byte[] b = new byte[RANDOM_ID_LENGTH];
                    new Random().nextBytes(b);
                    Search search = new Search(b, ARBITRARY_TTL,
                        RoutingService.BREADTHFIRST, currentValue);
                    searches.add(search);
                    synchronized (connectionList){
                        for(Socket s : connectionList){
                            LOGGER.log(Level.INFO, "Sending search value of "
                                    + currentValue + " to Node connection with "
                                    + s.getInetAddress() + ":" + s.getPort());
                            service.submit(new SenderRunner(
                                    search,
                                    new MessageOutput(s.getOutputStream())));
                        }
                    }
                }

            }
            catch (RuntimeException | SocketException e) {
                LOGGER.log(Level.SEVERE, "Unable to communicate: "
                                            + e.getLocalizedMessage(), e);
                for(Socket s : connectionList){
                    s.close();
                }
                downloadService.close();
                connectionRunner.close();
                service.close();
                cachedService.close();
                scanner.close();
                break;
            }
            catch (BadAttributeValueException x) {
                LOGGER.log(Level.SEVERE,
                        "Unable to send search request: " + x.getMessage());
                System.err.println("Unable to send search request: "
                                    + x.getMessage());
            } catch (IOException e){
                LOGGER.log(Level.SEVERE,
                        "An error occurred while creating a socket: "
                                + e.getMessage());
                System.err.println("An error occurred while " +
                        "creating a socket: " + e.getMessage());
            }
        }
    }
}
