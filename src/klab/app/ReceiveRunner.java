/**
 * Author:      Alex DeVries
 * Assignment:  Program 2
 * Class:       CSI 4321 Data Communications
 */
package klab.app;

import klab.serialization.*;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReceiveRunner class which implements runnable to decode
 * Response and Search objects to the socket
 */
public class ReceiveRunner implements Runnable {
    /**
     * the file id length
     */
    private static final int FILE_ID_RANDOM_LEN = 4;
    /**
     * the largest unsigned integer value
     */
    private static final long LARGEST_UNSIGNED_INT = 4294967296L;
    /**
     * the smallest unsigned integer value
     */
    private static final long SMALLEST_UNSIGNED_INT = 0L;
    /**
     * The socket to which the messages are decoded and encoded to
     */
    private final Socket socket;
    /**
     * The directory path given to search for the files
     */
    private final String directoryPath;

    /**
     * the download port for the node
     */
    private final int localDownloadPort;

    /**
     * a map of file names to their random file IDs
     */
    private final Map<String, Long> fileMapping;





    /**
     * the constructor for the receiveRunner object
     * @param socket the socket to communicate with
     * @param directoryPath the directory path to the files to use
     * @param localDownloadPort the download port of the Node
     */
    ReceiveRunner(Socket socket, String directoryPath, int localDownloadPort) {
        this.socket = socket;
        this.directoryPath = directoryPath;
        this.localDownloadPort = localDownloadPort;
        this.fileMapping = Node.getFileMapping();
        updateMapping();
    }

    /**
     * A function to update the mapping of random file ids to their names
     * in case the current directory changes its contents
     */
    public void updateMapping() {
        try {
            File directory = new File(directoryPath);
            File[] files = directory.listFiles();
            for (File file : files) {
                if (!fileMapping.containsKey(file.getName())) {
                    long randomID = generateRandomFileID(fileMapping);
                    fileMapping.put(file.getName(), randomID);
                }
            }
        } catch (NullPointerException | SecurityException e) {
            Logger.getLogger("Node.Log").log(Level.WARNING,
                    "Error with the file system: "
                            + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Print the response to the screen based on a given response instance
     *
     * @param response the response to be printed to the screen
     */
    public void printResponse(Response response) {
        if (Node.getSearchMap()
                .containsKey(Arrays.toString(response.getID()))) {
            synchronized (System.out) {
                System.out.println("Search response for " + Node.getSearchMap()
                        .get(Arrays.toString(response.getID())) + ":");
                System.out.println("Download host: " +
                        response.getResponseHost().getAddress() + ":"
                        + response.getResponseHost().getPort());
                StringBuilder formatted = new StringBuilder();
                for (Result r : response.getResultList()) {
                    formatted.append("      ");
                    formatted.append(r.getFileName());
                    formatted.append(": ID ");
                    for (byte b : r.getFileID()) {
                        formatted.append(String.format("%02X", b));
                    }
                    formatted.append(" (");
                    formatted.append(r.getFileSize());
                    formatted.append(" bytes)\n");
                }
                System.out.println(formatted);
            }
        }
    }

    /**
     * convert an unsigned integer to a byte array
     *
     * @param number the value to convert to a byte array
     * @return the file ID as a byte array
     */
    public byte[] convertToByteArray(long number) {
        byte[] value = new byte[FILE_ID_RANDOM_LEN];
        for (int i = FILE_ID_RANDOM_LEN - 1; i >= 0; i--) {
            value[i] = (byte) (number & 0xFF);
            number >>= 8;
        }
        return value;
    }

    /**
     * Generate a random file id and avoid collisions if encountered
     *
     * @param map the map holding the file names and their random file ids
     * @return the randomly generated file id
     */
    public long generateRandomFileID(Map<String, Long> map) {
        long otherID = (long) (new Random().nextDouble()
                * LARGEST_UNSIGNED_INT + 1);
        for (long value : map.values()) {
            if (value == otherID) {
                if (otherID == LARGEST_UNSIGNED_INT) {
                    otherID = SMALLEST_UNSIGNED_INT;
                } else {
                    otherID++;
                }
            }
        }
        return otherID;
    }

    /**
     * synchronized function for handling socket input
     * @param mi the message input instance with the socket inputstream
     * @return message from being decoded from the input stream
     * @throws IOException if an I/O error occurs
     * @throws BadAttributeValueException if an error with the message format
     *                                    occurs.
     */
    static Message getMessage(MessageInput mi) throws
                        IOException, BadAttributeValueException {
        MessageInput socketInput = mi;
        Message message = Message.decode(socketInput);
        return message;
    }


    /**
     * Run method within the ReceiveRunner instance to decode either
     * a search or a response instance to the socket inputStream
     */
    public void run() {
        Logger logger = Logger.getLogger("Node.Log");
        try {
            MessageInput socketInput =
                    new MessageInput(socket.getInputStream());
            while (!socket.isClosed()) {
                try {
                    updateMapping();
                    Message message = getMessage(socketInput);
                    message.setTTL(message.getTTL()-1);
                    //If message is a response instance
                    if (message instanceof Response response) {
                        if(Node.checkSearchList(response)){
                            logger.log(Level.INFO, "Received: " + response);
                            printResponse(response);
                        }
                        else{
                            List<Socket> sockList;
                            synchronized (Node.connectionList){
                                sockList = new ArrayList<>
                                        (Node.connectionList);
                            }
                            for(Socket s : sockList){
                                if(s != socket){
                                    Node.getSingleService().submit(
                                            new SenderRunner(
                                            response,
                                            new MessageOutput
                                                    (s.getOutputStream())));
                                }
                            }
                        }
                    }
                    //If the message is a search instance
                    else if (message instanceof Search search) {
                        Response response = new Response(search.getID(),
                                search.getTTL(), RoutingService.BREADTHFIRST,
                                new InetSocketAddress(Inet4Address.
                                        getLocalHost().getHostAddress(),
                                        localDownloadPort));

                        List<Socket> sockList;
                        synchronized (Node.connectionList){
                            sockList = new ArrayList<>(Node.connectionList);
                        }
                        for(Socket s : sockList){
                            if(s != socket){
                                Node.getSingleService().submit(new
                                        SenderRunner(
                                        search,
                                        new MessageOutput(s.
                                                getOutputStream())));
                            }
                        }

                        File directory = new File(directoryPath);
                        File[] files = directory.listFiles((dir, name) ->
                                name.contains(search.getSearchString()));
                        if (!search.getSearchString().isEmpty()
                                                &&
                                Objects.requireNonNull(files).length > 0) {
                            logger.log(Level.INFO, "Received: " + search);
                            for (File file : files) {
                                if (fileMapping.containsKey(file.getName())) {
                                    response.addResult(
                                            new Result(convertToByteArray(
                                                    fileMapping.get(
                                                            file.getName())),
                                                    file.length(),
                                                    file.getName()));
                                }
                            }
                            Node.getSingleService().submit(new SenderRunner(
                                    response,
                                    new MessageOutput(socket.getOutputStream())));
                        }
                        else if(Objects.equals(search.getSearchString(), "")){
                            Node.getSingleService().submit(new SenderRunner(
                                    response,
                                    new MessageOutput(socket.getOutputStream())));
                        }
                    }
                } catch (BadAttributeValueException e) {
                    logger.log(Level.WARNING,
                            "Invalid message: " + e.getLocalizedMessage(), e);
                } catch (IOException e) {
                    logger.log(Level.INFO,
                            "Closing node: " + e.getLocalizedMessage(), e);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}