/**
 * Author:      Alex DeVries
 * Assignment:  Program 1
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;


/**
 * Represents message instance
 */
public abstract class Message {
    /**
     * default message id length
     */
    static final int MSG_ID_LENGTH = 15;
    /**
     * minimum time to live
     */
    static final int TTL_MIN = 0;
    /**
     * maximum time to live
     */
    static final int TTL_MAX = 255;
    /**
     * message type length
     */
    static final int MESSAGE_TYPE_LENGTH = 1;
    /**
     * Time to live length
     */
    static final int TTL_LENGTH = 1;
    /**
     * routing service length
     */
    static final int ROUTING_SERVICE_LENGTH = 1;
    /**
     * payload length value size
     */
    static final int PAYLOAD_VAR_LENGTH = 2;
    /**
     * response type value identifier
     */
    static final int RESPONSE_TYPE = 2;
    /**
     * search type value identifier
     */
    static final int SEARCH_TYPE = 1;
    /**
     * matches length
     */
    static final int MATCH_LENGTH = 1;
    /**
     * port value length
     */
    static final int PORT_LENGTH = 2;
    /**
     * ip address length, must only be IPv4
     */
    static final int IP_ADDRESS_LENGTH = 4;
    /**
     * Minimum payload length
     */
    static final int MIN_PAYLOAD_LENGTH = 0;
    /**
     * Maximum payload length
     */
    static final int MAX_PAYLOAD_LENGTH = 65535;
    /**
     * message ID
     */
    private byte[] msgID;
    /**
     * Time to Live
     */
    private int ttl;
    /**
     * routing service value
     */
    private RoutingService routingService;


    /**
     * abstract function for getting the payload size
     * @return the payload size
     */
    abstract int getPayloadSize();

    /**
     * check message ID given to see if its valid
     * @param msgID msgID
     * @throws BadAttributeValueException if msgID is invalid
     */
    public static void checkMessageID(byte[] msgID)
                        throws BadAttributeValueException {
        if(msgID == null){
            throw new BadAttributeValueException("Null value error", "msgID",
                    new NullPointerException());
        }
        if(msgID.length != MSG_ID_LENGTH){
            throw new BadAttributeValueException("msgID length incorrect",
                                                "msgID");
        }
    }
    /**
     * check ttl given to see if its valid
     * @param ttl Time to Live
     * @throws BadAttributeValueException if ttl is invalid
     */
    public static void checkTTL(int ttl) throws BadAttributeValueException {
        if(ttl < TTL_MIN || ttl > TTL_MAX){
            throw new BadAttributeValueException("Invalid value present",
                    "ttl", new IllegalArgumentException());
        }
    }

    /**
     * check routing service given to see if its valid
     * @param routingService routing service
     * @throws BadAttributeValueException if routing service is invalid
     */
    public static void checkRoutingService(RoutingService routingService)
                                            throws BadAttributeValueException {
        if(routingService == null){
            throw new BadAttributeValueException("Null value error",
                    "routingService", new NullPointerException());
        }
    }

    /**
     * Constructs base message with given values
     * @param msgID message ID
     * @param ttl message TTL
     * @param routingService message routing service
     * @throws BadAttributeValueException if any parameter fails validation
     */
    Message(byte[] msgID, int ttl, RoutingService routingService)
                                throws BadAttributeValueException {
        setID(msgID);
        setTTL(ttl);
        setRoutingService(routingService);
    }

    /**
     * Encode message to given output sink
     * @param out output sink
     * @throws IOException if I/O problem or out is null
     */
    public void encode(MessageOutput out) throws IOException {
        if(out == null){
            throw new IOException("Message was null");
        }
        try{
            out.writeUnsignedInt(this.getMessageType(),MESSAGE_TYPE_LENGTH);
            out.writeBytes(msgID);
            out.writeUnsignedInt(ttl,TTL_LENGTH);
            out.writeUnsignedInt(routingService.getCode(),
                                    ROUTING_SERVICE_LENGTH);
            out.writeUnsignedInt(getPayloadSize(), PAYLOAD_VAR_LENGTH);
        }
        catch (IOException e){
            throw new IOException("Error occurred");
        }
    }

    /**
     * Deserializes message from input source
     * @param in deserialization input source
     * @return a specific message resulting from deserialization
     * @throws IOException if in is null or I/O problem occurs
     * @throws BadAttributeValueException if any parsed value fails validation
     */
    public static Message decode(MessageInput in) throws IOException,
                                            BadAttributeValueException {
        if(in == null){
            throw new IOException("Input sink cannot be null");
        }
        try{
            int payloadLen = -1;
            int messageType = in.readOnce();
            if((messageType != SEARCH_TYPE) &&
                    messageType != RESPONSE_TYPE){
                throw new BadAttributeValueException("Invalid message type",
                        "messageType");
            }

            byte[] msgID = new byte[MSG_ID_LENGTH];
            msgID = in.readNBytes(MSG_ID_LENGTH);


            int ttl = (int) in.readUnsignedInt(TTL_LENGTH);

            RoutingService routingService = RoutingService.getRoutingService(
                            (int) in.readUnsignedInt(ROUTING_SERVICE_LENGTH));
            checkMessageID(msgID);
            checkTTL(ttl);
            checkRoutingService(routingService);

            payloadLen = (int) in.readUnsignedInt(PAYLOAD_VAR_LENGTH);
            if (payloadLen < MIN_PAYLOAD_LENGTH ||
                    payloadLen > MAX_PAYLOAD_LENGTH){

                throw new BadAttributeValueException(
                        "Invalid payload length", "payloadLen");
            }
            if(messageType == SEARCH_TYPE){
                byte[] searchStringbuffer = new byte[payloadLen];
                searchStringbuffer = in.readNBytes(payloadLen);
                String searchString = new String(
                                    searchStringbuffer,
                                    StandardCharsets.US_ASCII);
                return new Search(msgID, ttl, routingService, searchString);
            }
            else if(messageType == RESPONSE_TYPE) {
                int matches = (int) in.readUnsignedInt(MATCH_LENGTH);
                int port = (int) in.readUnsignedInt(PORT_LENGTH);
                byte[] ipAddress = in.readNBytes(IP_ADDRESS_LENGTH);
                InetSocketAddress responseHost;
                try{
                    responseHost = new InetSocketAddress(
                                    InetAddress.getByAddress(ipAddress),
                                    port);
                }
                catch(IllegalArgumentException|SecurityException e){
                    throw new BadAttributeValueException(
                            "Invalid response host",
                            "responseHost",
                            e);
                }

                Response response = new Response
                                    (msgID,ttl,routingService,responseHost);
                for(int x = 0; x < matches; x++){
                    Result r = new Result(in);
                    response.addResult(r);
                }

                if(payloadLen > response.getPayloadSize()){
                    throw new BadAttributeValueException(
                                "payload Size too long", "payload length");
                }
                return response;
            }

        }
        catch (IOException e) {
            throw new IOException(e);
        }
        catch (Exception e){
            throw e;
        }
        return null;
    }

    /**
     * Message: ID=ID TTL=ttl Routing=routing
     * represented as a 30-character hex string (2 chars per byte) For example
     *
     * Message: ID=010203040506070809101112131415 TTL=4 Routing=DEPTHFIRST
     * @return String representation
     */
    public String toString(){
        StringBuilder formatted = new StringBuilder();
        if(getMessageType() == SEARCH_TYPE){
            formatted.append("Search: ID=");
        }
        else if(getMessageType() == RESPONSE_TYPE){
            formatted.append("Response: ID=");
        }
        for(byte b : this.getID()){
            formatted.append(String.format("%02X", b));
        }
        formatted.append(" TTL=");
        formatted.append(this.getTTL());
        formatted.append(" Routing=");
        formatted.append(this.getRoutingService().toString());
        return formatted.toString();
    }

    /**
     * Return the message type code (from protocol)
     * @return message type code (from protocol)
     */
    abstract int getMessageType();

    /**
     * Get ID
     * @return ID of message
     */
    public byte[] getID(){
        return msgID;
    }

    /**
     * Set ID
     * @param ID new ID
     * @return this message with new ID
     * @throws BadAttributeValueException if ID null or invalid
     */
    public Message setID(byte[] ID) throws BadAttributeValueException{
        checkMessageID(ID);
        this.msgID = ID;
        return this;
    }

    /**
     * Get TTL
     * @return TTL of message
     */
    public int getTTL(){
        return ttl;
    }

    /**
     * Set TTL
     * @param ttl new TTL
     * @return this Message with new TTL
     * @throws BadAttributeValueException if ttl invalid
     */
    public Message setTTL(int ttl) throws BadAttributeValueException {
        checkTTL(ttl);
        this.ttl = ttl;
        return this;
    }

    /**
     * Get routing service
     * @return routing service
     */
    public RoutingService getRoutingService(){
        return routingService;
    }

    /**
     * Set routing service
     * @param routingService new routing service
     * @return this Message with new routing service
     * @throws BadAttributeValueException if routingService is null
     */
    public Message setRoutingService(RoutingService routingService)
                                throws BadAttributeValueException{
        checkRoutingService(routingService);
        this.routingService = routingService;
        return this;
    }

    /**
     * equals override for Message
     * @param o the object to compare
     * @return true or false based on whether the two objects are the same
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message)) return false;
        return Arrays.equals(getID(), message.getID()) &&
               (getTTL() == message.getTTL()) &&
                (getRoutingService() == message.getRoutingService());
    }

    /**
     * overridden hashcode for Message
     * @return unique hashcode for Message
     */
    @Override
    public int hashCode() {
        return Objects.hash(getTTL(), getRoutingService(), Arrays.hashCode(getID()));
    }

}
