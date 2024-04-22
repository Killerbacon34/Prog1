/**
 * Author:      Alex DeVries
 * Assignment:  Program 1
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a response instance
 */
public class Response extends Message{

    /**
     * Default size for the header of response
     */
    static final int DEFAULT_SIZE = 7;
    /**
     * ipv4 address length
     */
    static final int ADDRESS_LENGTH = 4;

    /**
     * response host of instance
     */
    private InetSocketAddress responseHost;
    /**
     * list of results
     */
    private List<Result> list;

    /**
     * check responseHost given to see if its valid
     * @param responseHost response host
     * @throws BadAttributeValueException if responseHost is invalid
     */
    public void checkInetSocketAddress(InetSocketAddress responseHost)
                                    throws BadAttributeValueException {
        if(responseHost == null){
            throw new BadAttributeValueException("Null value used",
                                                "responseHost",
                                                new NullPointerException());
        }
        if(responseHost.getAddress().isMulticastAddress() ||
                (responseHost.getAddress().getAddress().length
                        != ADDRESS_LENGTH)){

            throw new BadAttributeValueException("Invalid IPv4 address",
                                                "responseHost",
                                               new IllegalArgumentException());
        }
    }

    /**
     * Constructs Response from given attributes
     *
     * @param msgID          message ID
     * @param ttl            message TTL
     * @param routingService message routing service
     * @param responseHost Address and port of responding host
     * @throws BadAttributeValueException if bad or null attribute value
     */
    public Response(byte[] msgID, int ttl, RoutingService routingService,
                    InetSocketAddress responseHost)
                    throws BadAttributeValueException {
        super(msgID, ttl, routingService);
        setResponseHost(responseHost);
        this.list = new ArrayList<>();

    }

    /**
     * Response: ID=ID TTL=ttl Routing=routing Host=address:port
     * [result1, result2gt; ... ID is
     * represented as a 30-character hex string (2 chars per byte) For example
     *
     * Response: ID=010203040506070809101112131415 TTL=4
     * Routing=DEPTHFIRST Host=1.2.3.4:5678 [Result:
     * FileID=0513A1CD FileSize=500 bytes
     * FileName=readme.txt, Result: FileID=12345678 FileSize=105 bytes
     * FileName=install.me]
     * @return String representation
     */
    public String toString(){
        String value = super.toString();
        StringBuilder formatted = new StringBuilder();
        formatted.append(" Host=");
        formatted.append(this.getResponseHost().toString().substring(1));
        formatted.append(" [");
        for(int i = 0; i < this.getResultList().size(); i++){
            formatted.append(this.getResultList().get(i).toString());
            if(i < this.getResultList().size()-1){
                formatted.append(", ");
            }
        }
        formatted.append("]");
        value += formatted.toString();
        return value;
    }

    /**
     * Get address and port of responding host
     * @return responding host address and port
     */
    public InetSocketAddress getResponseHost(){
        return responseHost;
    }

    /**
     * Set address and port of responding host
     * @param responseHost responding host address and port
     * @return this Response with new response host
     * @throws BadAttributeValueException if responseHost is null or if the
     * address is 1) multicast or 2) not IPv4 address
     */
    public Response setResponseHost(InetSocketAddress responseHost)
                                    throws BadAttributeValueException {
        checkInetSocketAddress(responseHost);
        this.responseHost = responseHost;
        return this;
    }

    /**
     * get list of results
     * @return result list
     */
    public List<Result> getResultList(){
        return list;
    }

    /**
     * Add result to list
     * @param result new result to add to result list
     * @return this Response with new result added
     * @throws BadAttributeValueException if result is null or would make
     * result list too long to encode
     */
    public Response addResult(Result result) throws BadAttributeValueException{
        if(result == null){
            throw new BadAttributeValueException("Result was null", "Result",
                    new NullPointerException());
        }
        if(list.size() >= 255 ||
                (getPayloadSize() + result.getSize() > MAX_PAYLOAD_LENGTH)){
            throw new BadAttributeValueException("Result list at max size",
                    "list");
        }
        list.add(result);
        return this;
    }

    /**
     * gets the payload size
     * @return the payload size
     */
    public int getPayloadSize(){
        int size = DEFAULT_SIZE;
        for(Result r : this.list){
            size += r.getSize();
        }
        return size;
    }

    /**
     * encodes the response in the intended method
     * @param out output sink
     * @throws IOException if out is null or a parsing error occurs
     */
    @Override
    public void encode(MessageOutput out) throws IOException {
        super.encode(out);
        if(out == null){
            throw new IOException("Message was null");
        }
        try{
            out.writeUnsignedInt(list.size(), MATCH_LENGTH);
            out.writeUnsignedInt(getResponseHost().getPort(), PORT_LENGTH);
            out.writeBytes(getResponseHost().getAddress().getAddress());
            for(Result r : list){
                r.encode(out);
            }
            out.flush();
        }
        catch (IOException e){
            throw new IOException("Error occurred");
        }
    }
    /**
     * Return the message type code (from protocol)
     * @return message type code (from protocol)
     */
    @Override
    public int getMessageType(){
        return RESPONSE_TYPE;
    }

    /**
     * equals override for Response
     * @param o the object to compare
     * @return true or false based on whether the two objects are the same
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response response)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getResponseHost(), response.getResponseHost())
                && Objects.equals(list, response.list);
    }

    /**
     * overridden hashcode for Response
     * @return unique hashcode for Response
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getResponseHost(), list);
    }
}
