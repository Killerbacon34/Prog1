/**
 * Author:      Alex DeVries
 * Assignment:  Program 2
 * Class:       CSI 4321 Data Communications
 */
package klab.app;

import klab.serialization.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SenderRunner class which implements runnable to encode
 * Response and Search objects to the socket
 */
public class SenderRunner implements Runnable{
    /**
     * Message instance to be encoded
     */
    private final Message message;
    /**
     * The messageOutput Instance to be used with the socket
     */
    private final MessageOutput socketOutput;


    /**
     * Constuctor for the SenderRunner class
     * @param message the message instance to be encoded
     * @param socketOutput the MessageOutput instance to be encoded to
     */
    SenderRunner(Message message, MessageOutput socketOutput){
        this.message = message;
        this.socketOutput = socketOutput;
    }

    /**
     * synchronized function for sending searches
     * @param s the search to send
     * @throws IOException if an I/O error occurs
     */
    synchronized void sendSearch(Search s) throws IOException {
        s.encode(socketOutput);
    }

    /**
     * synchronized function for sending responses
     * @param r the response to send
     * @throws IOException if an I/O error occurs
     */
    synchronized void sendResponse(Response r) throws IOException {
        r.encode(socketOutput);
    }

    /**
     * Run method within the SenderRunner instance to encode either
     * a search or a response instance to the socket outputStream
     */
     public void run() {
        try {
//            message.setTTL(message.getTTL()-1);
            if (message instanceof Search search && message.getTTL() >= 1) {
                sendSearch(search);
                Logger.getLogger("Node.Log")
                                .log(Level.INFO, "Sending: " + search);
                Node.addToSearchMap(search);
            }
            else if (message instanceof Response response
                                && message.getTTL() >= 1){
                sendResponse(response);
                Logger.getLogger("Node.Log")
                                .log(Level.INFO, "Sending: " + response);
            }
        } catch (IOException e) {
            Logger logger = Logger.getLogger("Node.Log");
            logger.log(Level.WARNING,
                    "Unable to communicate: " + e.getMessage(), e);
        }
//        catch (BadAttributeValueException e) {
//            Logger logger = Logger.getLogger("Node.Log");
//            logger.log(Level.WARNING, "Error with setting the TTL" +
//                        e.getMessage(), e);
//        }
    }

}
