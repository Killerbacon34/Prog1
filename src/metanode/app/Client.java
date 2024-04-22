/**
 * Author:      Alex DeVries
 * Assignment:  Program 5
 * Class:       CSI 4321 Data Communications
 */
package metanode.app;

import metanode.serialization.ErrorType;
import metanode.serialization.Message;
import metanode.serialization.MessageType;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.*;

public class Client {
    /**
     * argument length
     */
    private static final int ARG_LENGTH = 2;
    /**
     * max number of bytes in a message
     */
    private static final int MAX_MESSAGE_SIZE = 1534;
    /**
     * the timeout period before retransmitting
     */
    private static final int TIMEOUT = 3000;
    /**
     * max tries to resubmit the current message
     */
    private static final int MAXTRIES = 3;
    /**
     * the max possible session ID + 1 for random generation
     */
    private static final int MAX_SESSION_ID = 256;

    /**
     * map of SessionIDs to their messages
     */
    private static Map<Integer,Message> messageMap = new HashMap<>();

    /**
     * The main function responsible for handling the metanode client
     * @param args the command line arguments for the main function
     * @throws SocketException if a socket creation error occurs
     */
    public static void main(String[] args) throws SocketException {
        Scanner scanner = new Scanner(System.in);
        String currentValue;
        boolean errorPresent = false;
        boolean commandLineError = false;
        int port;
        String serverAddress;
        if (args.length != ARG_LENGTH) {
            System.err.println("Argument length invalid");
            return;
        }

        if(args[1] == null || args[0] == null){
            System.err.println("One of the arguments is null");
            return;
        }

        serverAddress = args[0];
        port = Integer.parseInt(args[1]);
        if(port < 0 || port > 65535){
            System.err.println("given port is not a valid port");
            return;
        }

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
            while (!errorPresent) {
                commandLineError = false;
                System.out.print("> ");
                currentValue = scanner.nextLine();
                if (currentValue.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting program...");
                    socket.close();
                    scanner.close();
                    break;
                }
                String[] values = currentValue.split(" ");
                if(MessageType.getByCmd(values[0]) == null){
                    System.err.println("Incorrect command line arguments");
                    commandLineError = true;
                }
                if((Objects.equals(values[0], "RN") ||  Objects.equals(values[0], "RM")) && values.length != 1){
                    System.err.println(values[0] + " command expects no arguments:");
                    commandLineError = true;
                }
                if(!commandLineError) {
                    int currTries = 0;
                    int randomNumber;
                    do{
                        Random random = new Random();
                        randomNumber = random.nextInt(1, MAX_SESSION_ID);
                    } while(messageMap.containsKey(randomNumber));

                    Message currMessage = new Message(MessageType.getByCmd(values[0]),
                            ErrorType.None, randomNumber);

                    if (!Objects.equals(values[0], "RN")
                            && !Objects.equals(values[0], "RM")) {

                        if (values.length >= 2) {

                            for (int x = 1; x < values.length; x++) {
                                String[] addressValues = values[x].split(":");
                                if(addressValues.length == 2){
//                                    InetAddress checkAddr = InetAddress.getByName(addressValues[0]);
                                    currMessage.addAddress(new InetSocketAddress(addressValues[0],
                                            Integer.parseInt(addressValues[1])));
//                                    if(checkAddr.getHostAddress().equals(addressValues[0])){
//                                        currMessage.addAddress(new InetSocketAddress(addressValues[0],
//                                                Integer.parseInt(addressValues[1])));
//                                    }
//                                    else{
//                                        System.err.println("IP address entered is not valid");
//                                    }
                                }
                                //might need to break out here somehow
                                else{
                                    System.err.println("Invalid address and port pair");
                                }
                            }

                            byte[] messageBytes = currMessage.encode();
                            if (!messageMap.containsKey(randomNumber)) {
                                messageMap.put(randomNumber, currMessage);
                            }
                            DatagramPacket sendPacket = new DatagramPacket(messageBytes,
                                    messageBytes.length, InetAddress.getByName(serverAddress), port);
                            socket.send(sendPacket);
                        }

                        if (values.length < 2) {
                            System.err.println(values[0] + " command expects at least one argument: " + values[0]);
                        }
                    }

                    else if (Objects.equals(values[0], "RN")
                            || Objects.equals(values[0], "RM")) {

                        boolean receivedResponse = false;
                        byte[] messageBytes = currMessage.encode();
                        if (!messageMap.containsKey(randomNumber)) {
                            messageMap.put(randomNumber, currMessage);
                        }
                        DatagramPacket sendPacket = new DatagramPacket(messageBytes,
                                messageBytes.length, InetAddress.getByName(serverAddress), port);

                        DatagramPacket receivePacket =
                                new DatagramPacket(new byte[MAX_MESSAGE_SIZE], MAX_MESSAGE_SIZE);

                        do {
                            socket.send(sendPacket);
                            try {
                                socket.receive(receivePacket);
                                if (!receivePacket.getAddress().equals(InetAddress.getByName(serverAddress))) {
                                    throw new IOException("Received packet from an unknown source");
                                }
                                byte[] current = receivePacket.getData();
                                if(messageBytes[2] == current[2]){
                                    receivedResponse = true;
                                }
                            } catch (InterruptedIOException e) {
                                currTries++;
                                System.out.println("Timed out, " + (MAXTRIES - currTries) + " more tries...");
                            }
                        } while ((!receivedResponse) && (currTries < MAXTRIES));

                        if (receivedResponse) {
                            try{
                                byte[] actualPacket = Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength());
                                Message responseMessage = new Message(actualPacket);
                                if (responseMessage.getType() != MessageType.AnswerRequest) {
                                    System.err.println("Unexpected Message Type");
                                }
                                if (!messageMap.containsKey(responseMessage.getSessionID())) {
                                    System.err.println("Unexpected Session ID");
                                } else if (messageMap.containsKey(responseMessage.getSessionID())
                                        || responseMessage.getSessionID() == 0) {
                                    System.out.println(responseMessage);
                                    messageMap.remove(responseMessage.getSessionID());
                                }
                            } catch (IllegalArgumentException e){
                                System.err.println("Invalid message: " + e);
                            } catch (IOException e){
                                throw new IOException();
                            }

                        }
                    }
                }
            }
//        } catch (IllegalArgumentException e){
//            System.err.println("Invalid message: " + e);
        } catch (IOException e){
            System.err.println("Communication problem: " + e );
            scanner.close();
        }
    }
}
