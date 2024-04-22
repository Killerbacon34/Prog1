/**
 * Author:      Alex DeVries
 * Assignment:  Program 1
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents a search
 */
public class Search extends Message{
    /**
     * search type message value
     */
    static final int SEARCH_TYPE = 1;
    /**
     * search string of the instance
     */
    private String searchString;

    /**
     * check if the searchstring passed validation
     * @param searchString the searchstring
     * @throws BadAttributeValueException if searchstring fails validation
     */
    public void checkSearchString(String searchString)
                    throws BadAttributeValueException {
        if(searchString == null){
            throw new BadAttributeValueException("Null value used",
                    "searchString",
                    new NullPointerException());
        }
        if(!searchString.matches("[a-zA-Z0-9_.-]*")){
            throw new BadAttributeValueException("Illegal search string",
                    "searchString");
        }
        if(searchString.length() > MAX_PAYLOAD_LENGTH){
            throw new BadAttributeValueException("Invalid value entered",
                    "searchString");
        }
    }

    /**
     * Constructs search from given values
     *
     * @param msgID          message ID
     * @param ttl            message TTL
     * @param routingService message routing service
     * @param searchString   search string
     * @throws BadAttributeValueException if any parameter fails validation
     */
    public Search(byte[] msgID, int ttl, RoutingService routingService,
                  String searchString) throws BadAttributeValueException {
        super(msgID, ttl, routingService);
        setSearchString(searchString);
    }

    /**
     * Search: ID=ID TTL=ttl Routing=routing Search=search
     * ID is represented as a 30-character hex string (2 chars per byte)
     *
     * @return string representation
     */
    public String toString(){
        String value = super.toString();
        StringBuilder formatted = new StringBuilder();
        formatted.append(" Search=");
        formatted.append(this.getSearchString());
        value += formatted.toString();
        return value;
    }

    /**
     * Get search string
     * @return search string
     */
    public String getSearchString(){
        return searchString;
    }

    /**
     * Set search string
     * @param searchString new search string
     * @return this Search with new search string
     * @throws BadAttributeValueException if searchString is null or fails
     * validation
     */
    public Search setSearchString(String searchString)
                                    throws BadAttributeValueException{
        checkSearchString(searchString);
        this.searchString = searchString;
        return this;
    }

    /**
     * gets the payload size of the packet
     * @return the payload size
     */
    public int getPayloadSize(){
        return searchString.length();
    }

    /**
     * encodes the search in the intended method
     * @param out output sink
     * @throws IOException if out is null or a parsing error has occurred
     */
    @Override
    public void encode(MessageOutput out) throws IOException {
        super.encode(out);
        if(out == null){
            throw new IOException("Message was null");
        }
        try{
            out.writeBytes(searchString.getBytes(StandardCharsets.US_ASCII));
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
    public int getMessageType(){
        return SEARCH_TYPE;
    }


    /**
     * equals override for Search
     * @param o the object to compare
     * @return true or false based on whether the two objects are the same
     */
//    @Override
//    public boolean equals(Object o) {
//        boolean b = super.equals(o);
//        if (this == o) return true;
//        if (!(o instanceof Search search)) return false;
//        return Objects.equals(getSearchString(), search.getSearchString())
//                && b;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Search search)) return false;
        if (!super.equals(o)) return false;
        return getSearchString().equals(search.getSearchString());
    }

    /**
     * overridden hashcode for Search
     * @return unique hashcode for Search
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSearchString());
    }
}
