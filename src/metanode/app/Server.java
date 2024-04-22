/**
 * Author:      Alex DeVries
 * Assignment:  Program 6
 * Class:       CSI 4321 Data Communications
 */
package metanode.app;

import metanode.serialization.ErrorType;
import metanode.serialization.Message;
import metanode.serialization.MessageType;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.*;

/**
 * class for creating the metaNode server
 */
public class Server {
    /**
     * max number of bytes in a message
     */
    private static final int ECHOMAX = 1534;
    /**
     * Logger instance used to log to a file
     */
    private static final Logger LOGGER = Logger.getLogger("MetaNode.log");

    /**
     * Static block of code used to create settings for the logger.
     */
    static {
        FileHandler fh;
        ConsoleHandler c;
        try {
            fh = new FileHandler("meta.log");
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
     * the main function to run the metaNode Server
     * @param args the command line parameters to pass to the main function
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }
        int serverPort = Integer.parseInt(args[0]);
        try (DatagramSocket serverSocket = new DatagramSocket(serverPort)) {
            List<InetSocketAddress> nodeAddrList = new ArrayList<>();
            List<InetSocketAddress> metaNodeAddrList = new ArrayList<>();
            DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);
            while (true) {
                try {
                    serverSocket.receive(packet);
                    byte[] actualPacket = Arrays.copyOfRange(packet.getData(),0,packet.getLength());
                    Message currMessage = new Message(actualPacket);
                    if (currMessage.getType() == MessageType.NodeAdditions) {
                        LOGGER.log(Level.INFO, "Received: " + currMessage);
                        for (InetSocketAddress s : currMessage.getAddrList()) {
                            if (!nodeAddrList.contains(s)) {
                                nodeAddrList.add(s);
                            }
                        }
                    }
                    if (currMessage.getType() == MessageType.MetaNodeAdditions) {
                        LOGGER.log(Level.INFO, "Received: " + currMessage);
                        for (InetSocketAddress s : currMessage.getAddrList()) {
                            if (!metaNodeAddrList.contains(s)) {
                                metaNodeAddrList.add(s);
                            }
                        }
                    }
                    if (currMessage.getType() == MessageType.NodeDeletions) {
                        LOGGER.log(Level.INFO, "Received: " + currMessage);
                        for (InetSocketAddress s : currMessage.getAddrList()) {
                            nodeAddrList.remove(s);
                        }
                    }
                    if (currMessage.getType() == MessageType.MetaNodeDeletions) {
                        LOGGER.log(Level.INFO, "Received: " + currMessage);
                        for (InetSocketAddress s : currMessage.getAddrList()) {
                            metaNodeAddrList.remove(s);
                        }
                    }

                    if (currMessage.getType() == MessageType.RequestNodes) {
                        Message sendMessage = new Message(MessageType.AnswerRequest,
                                ErrorType.None, currMessage.getSessionID());
                        for (InetSocketAddress s : nodeAddrList) {
                            sendMessage.addAddress(s);
                        }
                        byte[] messageBytes = sendMessage.encode();
                        DatagramPacket sendPacket = new DatagramPacket(messageBytes,
                                messageBytes.length,
                                packet.getAddress(),
                                packet.getPort());
                        System.out.println(Arrays.toString(messageBytes));
                        serverSocket.send(sendPacket);
                    }

                    if (currMessage.getType() == MessageType.RequestMetaNodes) {
                        Message sendMessage = new Message(MessageType.AnswerRequest,
                                ErrorType.None, currMessage.getSessionID());
                        for (InetSocketAddress s : metaNodeAddrList) {
                            sendMessage.addAddress(s);
                        }

                        byte[] messageBytes = sendMessage.encode();
                        DatagramPacket sendPacket = new DatagramPacket(messageBytes,
                                messageBytes.length,
                                packet.getAddress(),
                                packet.getPort());
                        serverSocket.send(sendPacket);
                    }
                    if (currMessage.getType() == MessageType.AnswerRequest) {
                        LOGGER.log(Level.SEVERE, "Unexpected message type: " + currMessage);
                        Message errorMessage = new Message(MessageType.AnswerRequest, ErrorType.IncorrectPacket, currMessage.getSessionID());
                        byte[] errorBytes = errorMessage.encode();
                        DatagramPacket errorPacket = new DatagramPacket(errorBytes,
                                errorBytes.length,
                                packet.getAddress(),
                                packet.getPort());
                        serverSocket.send(errorPacket);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Communication problem: " + e);
                    Message errorMessage = new Message(MessageType.AnswerRequest, ErrorType.System, 0);
                    byte[] errorBytes = errorMessage.encode();
                    DatagramPacket errorPacket = new DatagramPacket(errorBytes,
                            errorBytes.length,
                            packet.getAddress(),
                            packet.getPort());
                    serverSocket.send(errorPacket);

                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.SEVERE, "Invalid message: " + e);
                    Message errorMessage = new Message(MessageType.AnswerRequest, ErrorType.IncorrectPacket, 0);
                    byte[] errorBytes = errorMessage.encode();
                    DatagramPacket errorPacket = new DatagramPacket(errorBytes,
                            errorBytes.length,
                            packet.getAddress(),
                            packet.getPort());
                    serverSocket.send(errorPacket);

                }
            }
        } catch (IllegalArgumentException | SocketException e) {
            LOGGER.log(Level.SEVERE, "Startup Problem: " + e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Invalid message:" + e);
        }

    }
}