/**
 * Author:      Alex DeVries
 * Assignment:  Program 4
 * Class:       CSI 4321 Data Communications
 */
package metanode.serialization;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * message class for the MetANode implementation
 */
public class Message {
    /**
     * all possible message types for the MessageType
     */
    private static final String[] POSSIBLE_MESSAGE_TYPES
                    = new String[]{"RN", "RM", "AR", "NA", "MA", "ND", "MD"};
    /**
     * min message code value
     */
    private static final int MIN_MESSAGE_CODE = 0;
    /**
     * max message code value
     */
    private static final int MAX_MESSAGE_CODE = 6;
    /**
     * min message id value
     */
    private static final int MIN_MESSAGE_ID_SIZE= 0;
    /**
     * max message id value
     */
    private static final int MAX_MESSAGE_ID_SIZE= 255;
    /**
     * min message header size
     */
    private static final int MIN_MESSAGE_SIZE= 4;
    /**
     * length of each socket address in octets
     */
    private static final int SOCKET_ADDRESS_SIZE = 6;
    /**
     * length of socket address in octets
     */
    private static final int ADDRESS_SIZE= 4;
    /**
     * length of port value in octets
     */
    private static final int PORT_SIZE= 2;
    /**
     * max number of address values
     */
    private static final int MAX_ADDRESS_LIST_SIZE = 255;
    /**
     * the version number
     */
    private static final int VERSION_NUMBER = 4;
    /**
     * max message size
     */
    private static final int MAX_MESSAGE_SIZE = 1534;

    /**
     * the MessageType for the message instance
     */
    private final MessageType type;
    /**
     * the ErrorType for the message instance
     */
    private final ErrorType error;
    /**
     * the sessionID for the message instance
     */
    private int sessionID;
    /**
     * the list of socket addresses within the message if it needs it
     */
    private final List<InetSocketAddress> addrList;

    /**
     * used to check if the enumerations are valid within the message
     * @param messageType the messageType of the message instance
     * @param errorType the errorType of the error instance
     * @throws IllegalArgumentException if the messageType or errorType is not
     *                                  valid
     */
    public static void checkEnumerations(MessageType messageType,
                                         ErrorType errorType)
                                        throws IllegalArgumentException{
        if(messageType == null || errorType == null){
            throw new IllegalArgumentException("Null type");
        }
        if(messageType.getCode() < MIN_MESSAGE_CODE ||
                messageType.getCode() > MAX_MESSAGE_CODE ||
                !Arrays.stream(POSSIBLE_MESSAGE_TYPES)
                        .toList().contains(messageType.getCmd())){
            throw new IllegalArgumentException("Bad message type");
        }
        else if(errorType.getCode() != ErrorType.None.getCode() &&
                (messageType.getCode() !=
                        MessageType.AnswerRequest.getCode())){
            throw new IllegalArgumentException("Bad error code");
        }
    }

    /**
     * Checks the sessionID to see if it's a valid value
     * @param sessionID the sessionID to check
     * @throws IllegalArgumentException if the sessionID is not valid
     */
    public static void checkSessionID(int sessionID)
                                        throws IllegalArgumentException{
        if(sessionID < MIN_MESSAGE_ID_SIZE || sessionID > MAX_MESSAGE_ID_SIZE){
            throw new IllegalArgumentException("Illegal SessionID");
        }
    }

    /**
     * converst byte array to an integer
     * @param values the array to convert
     * @param length the number of values within the array
     * @return an unsigned integer based on the byte array
     */
    public static long convertByteArrayToInt(byte[] values, int length){
        long result = 0;
        for (int i = 0; i < length; i++) {
            int currByte = values[i];
            result = (result << 8) | (currByte & 0xFF);
        }
        return result;
    }

    /**
     * gets the version and type from the given byte value
     * @param value the byte to convert to the version and type
     * @return the messagetype of the instance
     */
    public static MessageType getVersionAndType(byte value) throws IllegalArgumentException{
        try{
            int version = ((value >> 4) & (byte) 0x0F);
            if(version != VERSION_NUMBER){
                throw new IllegalArgumentException();
            }
            int typeInt = (value & 0x0F);
            return MessageType.getByCode(typeInt);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid version or type");
        }
    }

    /**
     *
     * convert the version and type to a byte to write
     * @param type the messageType of the instance
     * @return the byte to write to an output stream with the version and type
     */
    public static byte convertVersionAndTypeIntoByte(MessageType type){
        int version = 4;
        byte result = 0;
        result |= (byte) (version << 4);
        result |= (byte) type.getCode();
        return result;
    }

    /**
     * convert number to byte array
     * @param number the unsigned int to convert
     * @param length the length of the byte array to result
     * @return the byte array with the number converted
     */
    public static byte[] convertToUnsignedInt(long number, int length){
        byte[] value = new byte[length];
        for (int i = length-1; i >= 0; i--) {
            value[i] = (byte)(number & 0xFF);
            number >>= 8;
        }
        return value;
    }


    /**
     * constructor for the Message using a byte array as a parameter
     * @param buf the byte array to convert to a message
     * @throws IOException if the byte array is too long/too short
     * @throws IllegalArgumentException if a parameter is invalid
     */
    public Message(byte[] buf) throws IOException, IllegalArgumentException {
        try{
            addrList = new ArrayList<>();
            if(buf == null || buf.length < MIN_MESSAGE_SIZE
                            || buf.length > MAX_MESSAGE_SIZE){
                throw new IOException("Invalid byte array length");
            }
            ByteArrayInputStream in = new ByteArrayInputStream(buf);
            byte currByte = (byte) in.read();
            type = getVersionAndType(currByte);
            currByte = (byte) in.read();
            error = ErrorType.getByCode(currByte);
            sessionID = in.read();
            checkEnumerations(type,error);
            checkSessionID(sessionID);
            int count = in.read();
            int totalAddrCount = MIN_MESSAGE_SIZE + SOCKET_ADDRESS_SIZE*count;
            if((type == MessageType.RequestMetaNodes ||
                    type == MessageType.RequestNodes)
                    && totalAddrCount - MIN_MESSAGE_SIZE != 0){
                throw new IllegalArgumentException("Invalid argument");
            }
            if(totalAddrCount != buf.length){
                throw new IOException("Invalid byte array length");
            }
            totalAddrCount -= MIN_MESSAGE_SIZE;
            for(int x = 0; x < count; x++){
                byte[] currIPAddress = in.readNBytes(ADDRESS_SIZE);
                byte[] portBytes = in.readNBytes(PORT_SIZE);
                int port = (int)
                        convertByteArrayToInt(portBytes, PORT_SIZE);
                InetSocketAddress s = new
                        InetSocketAddress(InetAddress.
                        getByAddress(currIPAddress), port);
                addAddress(s);
                totalAddrCount -= SOCKET_ADDRESS_SIZE;
            }
            if(count != addrList.size()){
                throw new IllegalArgumentException("invalid count");
            }
            if(totalAddrCount != 0){
                throw new IOException("Byte array too long");
            }
        } catch(IOException e){
            throw new IOException("Invalid byte array length");
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid argument");
        }

    }

    /**
     * constructor for message class
     * @param type the messageType of the instance
     * @param error the errorType of the instance
     * @param sessionID the sessionID of the instance
     * @throws IllegalArgumentException if an invalid parameter is given
     */
    public Message(MessageType type, ErrorType error, int sessionID)
                                    throws IllegalArgumentException{
        setSessionID(sessionID);
        checkEnumerations(type, error);
        this.type = type;
        this.error = error;
        this.addrList = new ArrayList<>();
    }

    /**
     * encodes the message instance into a byte array
     * @return the byte array with the message contents
     */
    public byte[] encode(){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte versionAndType = convertVersionAndTypeIntoByte(type);
        out.write(versionAndType);
        out.write(error.getCode());
        out.write(sessionID);
        out.write(addrList.size());
        checkEnumerations(type,error);
        checkSessionID(sessionID);
        for(InetSocketAddress i : addrList){
            out.writeBytes(i.getAddress().getAddress());
            out.writeBytes(convertToUnsignedInt(i.getPort(),PORT_SIZE));
        }
        return out.toByteArray();
    }

    /**
     * toString method for the message class
     * @return a string representation of the message instance in the desired
     *         format
     */
    public String toString(){
        StringBuilder formatted = new StringBuilder();
        formatted.append("Type=");
        formatted.append(this.type.toString());
        formatted.append(" Error=");
        formatted.append(this.error.toString());
        formatted.append(" Session ID=");
        formatted.append(this.sessionID);
        formatted.append(" Addrs=");
        for(int i = 0; i < this.addrList.size(); i++){
            int s = this.addrList.get(i).toString().indexOf('/');
            formatted.append(this.addrList.get(i).toString().substring(s+1));
            formatted.append(" ");
        }
        return formatted.toString();
    }

    /**
     * gets the type of the message
     * @return the messageType of the instance
     */
    public MessageType getType(){
        return type;
    }

    /**
     * the errortype of the message
     * @return the errorType of the instance
     */
    public ErrorType getError(){
        return error;
    }

    /**
     * sets the sessionID of the message instance
     * @param sessionID the new sessionID for the message
     * @return the message instance with the new sessionID
     * @throws IllegalArgumentException if the sessionID is invalid
     */
    public Message setSessionID(int sessionID) throws IllegalArgumentException{
        checkSessionID(sessionID);
        this.sessionID = sessionID;
        return this;
    }

    /**
     * gets the sessionID of the message
     * @return the sessionID of the instance
     */
    public int getSessionID(){
        return sessionID;
    }

    /**
     * gets the socket address list of the message
     * @return the socket address list
     */
    public List<InetSocketAddress> getAddrList(){
        return addrList;
    }

    /**
     * adds a new socket address to the address list within the message
     * @param newAddress the new socket address to add to the list
     * @return the message instance with the new address in its list
     * @throws IllegalArgumentException if the socket address is invalid
     */
    public Message addAddress(InetSocketAddress newAddress)
                        throws IllegalArgumentException{
        try{
            if(newAddress == null){
                throw new IllegalArgumentException("null Socket address");
            }
            if(this.getType().getCode()
                    == MessageType.RequestNodes.getCode()
                    || this.getType().getCode()
                    == MessageType.RequestMetaNodes.getCode()){
                throw new IllegalArgumentException("Invalid message type");
            }

            if(newAddress.getAddress().getAddress().length != 4){
                throw new IllegalArgumentException("Address not IPv4");
            }

            if(!addrList.contains(newAddress)){
                if(addrList.size() >= MAX_ADDRESS_LIST_SIZE){
                    throw new IllegalArgumentException("Address list full");
                }
                else{
                    addrList.add(newAddress);
                }
            }

        } catch (IllegalArgumentException e){
            throw e;
        }
        return this;
    }

    /**
     * overridden hashcode for Message
     * @return unique hashcode for Message
     */
    @Override
    public int hashCode(){
        return Objects.hash(this.type,this.error,this.sessionID,this.addrList);
    }

    /**
     * equals override for Message
     * @param o the object to compare
     * @return true or false based on whether the two objects are the same
     */
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return (getType() == message.getType())
                && (getError() == message.getError())
                && (getSessionID() == message.getSessionID())
                && (Objects.equals(addrList, message.addrList));
    }
}
