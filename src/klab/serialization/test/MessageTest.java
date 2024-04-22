/**
 * Author:      Alex DeVries
 * Assignment:  Program 1
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization.test;

import klab.serialization.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * All tests for the Message class
 */
public class MessageTest {
    /**
     * Testing for null constructors
     */
    @Nested
    class NullTestConstructors {
        /**
         * testing for null msgID
         */
        @Test
        void testMsgIDMessageConstructor(){
            assertThrows(BadAttributeValueException.class, () -> new Response(null, 16, RoutingService.DEPTHFIRST, new InetSocketAddress("localhost", 5050)));
        }

        /**
         * testing for null routingService
         */
        @Test
        void testRoutingServiceMessageConstructor(){
            assertThrows(BadAttributeValueException.class, () -> new Response(new byte[] {1, 2, 3, 4,5,6,7,8,9,10,11,12,13,14,15}, 16, null, new InetSocketAddress("localhost", 5050)));

        }

        /**
         * testing for null iNetSocketAddress
         */
        @Test
        void testINetSocketAddressResponseConstructor(){
            assertThrows(BadAttributeValueException.class, () -> new Response(new byte[] {1, 2, 3, 4,5,6,7,8,9,10,11,12,13,14,15}, 16,RoutingService.DEPTHFIRST, null));

        }

        /**
         * testing for null search string
         */
        @Test
        void testSearchStringSearchConstructor(){
            assertThrows(BadAttributeValueException.class, () -> new Search(new byte[] {1, 2, 3, 4,5,6,7,8,9,10,11,12,13,14,15}, 16,RoutingService.DEPTHFIRST, null));
        }
    }
    @Nested
    class TestNullFunctions{
        byte[] data = new byte[] {1, 2, 3, 4,5,6,7,8,9,10,11,12,13,14,15};
        /**
         * Testing encode
         */
        @Test
        void testMessageEncode(){
            assertThrows(IOException.class, ()-> {
                final Search testSearch = new Search(data, 16,RoutingService.DEPTHFIRST, "testing");
                testSearch.encode(null);
            });
        }
        /**
         * Testing decode
         */
        @Test
        void testMessageDecode(){
            assertThrows(IOException.class, ()-> {
                final Search testSearch = new Search(data, 16,RoutingService.DEPTHFIRST, "testing");
                Message.decode(null);
            });
        }

        /**
         * Testing SetID
         */
        @Test
        void testMessageSetID(){
            assertThrows(BadAttributeValueException.class, ()-> {
                final Search testSearch = new Search(data, 16,RoutingService.DEPTHFIRST, "testing");
                testSearch.setID(null);
            });
        }

        /**
         * Testing setRoutingService
         */
        @Test
        void testMessageSetRoutingService(){
            assertThrows(BadAttributeValueException.class, ()-> {
                final Search testSearch = new Search(data, 16,RoutingService.DEPTHFIRST, "testing");
                testSearch.setRoutingService(null);
            });
        }

        /**
         * Testing setSearchString
         */
        @Test
        void testSearchSetSearchString(){
            assertThrows(BadAttributeValueException.class, ()-> {
                final Search testSearch = new Search(data, 16,RoutingService.DEPTHFIRST, "testing");
                testSearch.setSearchString(null);
            });
        }

        /**
         * Testing setResponseHost
         */
        @Test
        void testResponseSetResponseHost(){
            assertThrows(BadAttributeValueException.class, ()-> {
                final Response testResponse = new Response(data, 16,RoutingService.DEPTHFIRST, new InetSocketAddress("localhost", 5050));
                testResponse.setResponseHost(null);
            });
        }
    }
    @Nested
    class SearchTesting{
        @Test
        void decodeSimpleTest() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b' };
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, r.getID()),
                    () -> assertEquals(3, r.getTTL()), () -> assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService()),
                    () -> assertEquals("bob", r.getSearchString()));
        }
        @Test
        void setterValidFunctions() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b' };
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            r.setID(new byte[] {1, -0, 0, 1, 43, -12, -65, -20, 99, 47, 10, 100, 3, -128, 127});
            r.setTTL(0);
            r.setRoutingService(RoutingService.DEPTHFIRST);
            r.setSearchString("Ch_an-ged.");
            assertAll(() -> assertArrayEquals(new byte[] { 1, -0, 0, 1, 43, -12, -65, -20, 99, 47, 10, 100, 3, -128, 127}, r.getID()),
                    () -> assertEquals(0, r.getTTL()), () -> assertEquals(RoutingService.DEPTHFIRST, r.getRoutingService()),
                    () -> assertEquals("Ch_an-ged.", r.getSearchString()));
        }
        @Test
        void InvalidType() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b' };
            assertThrows(BadAttributeValueException.class, () -> {
                Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void setterInvalidmsgIDLen() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b' };
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));

            assertAll(() -> assertThrows(BadAttributeValueException.class, ()->{
                        r.setID(new byte[] {1, -0, 0, 1, 43, -12, -65, -20, 99, 47, 10, 100, 3});
                    }),
                    () -> assertEquals(3, r.getTTL()),
                    () -> assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService()),
                    () -> assertEquals("bob", r.getSearchString()));
        }
        @Test
        void setterInvalidTTLNegative() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b' };
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, r.getID()),
                    () -> assertThrows(BadAttributeValueException.class, ()->{
                        r.setTTL(-100);
                    }),
                    () -> assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService()),
                    () -> assertEquals("bob", r.getSearchString()));
        }
        @Test
        void setterInvalidTTLTooLarge() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b' };
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, r.getID()),
                    () -> assertThrows(BadAttributeValueException.class, ()->{
                        r.setTTL(500);
                    }),
                    () -> assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService()),
                    () -> assertEquals("bob", r.getSearchString()));
        }
        @Test
        void setterInvalidLongPayloadLength() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 5, 'b', 'o', 'b' };
            assertThrows(IOException.class, () -> {
                Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void setterInvalidCharactersDecode() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 5, 'b', 'o', 'b', '&', '*' };
            assertThrows(BadAttributeValueException.class, () -> {
                Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void setterInvalidPayloadLengthNegative() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, -2, 'b', 'o', 'b', '&', '*' };
            assertThrows(IOException.class, () -> {
                Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void testingDecodeEncodeDecode() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 3, 'b', 'o', 'b'};
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out));
            Search x = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
            assertAll(() -> assertArrayEquals(x.getID(), r.getID()),
                    () -> assertEquals(x.getTTL(), r.getTTL()),
                    () -> assertEquals(x.getRoutingService(), r.getRoutingService()),
                    () -> assertEquals(x.getSearchString(), r.getSearchString()));
            assertEquals(x,r);
            assertEquals(x,x);
            assertEquals(r,x);
        }
        @Test
        void testingEncodeDecodeEncode() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0};
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out));
            Search x = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            x.encode(new MessageOutput(out2));
            assertArrayEquals(out.toByteArray(),out2.toByteArray());

//            Search finalR = r;
//            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, finalR.getID()),
//                    () -> assertEquals(3, finalR.getTTL()),
//                    () -> assertEquals(RoutingService.BREADTHFIRST, finalR.getRoutingService()),
//                    () -> assertEquals("", finalR.getSearchString()));
        }
        @Test
        void testingToString() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 3, 0, 0, 3, 'b', 'o', 'b'};
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            String value = r.toString();
            assertEquals("Search: ID=0102030405060708090A0B0C0D0E0F TTL=3 Routing=BREADTHFIRST Search=bob",value);
        }
    }


    @Nested
    class ResponseTesting{
        @Test
        void decodeSimpleTest() throws NullPointerException, IOException, BadAttributeValueException {
            ArrayList<Result> listing = new ArrayList<>();
            listing.add(new Result(new byte[]{1,2,3,4}, 56, "TESTING"));
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 23, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10};
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, r.getID()),
                    () -> assertEquals(3, r.getTTL()),
                    () -> assertEquals(RoutingService.BREADTHFIRST, r.getRoutingService()),
                    () ->assertEquals(20,r.getResponseHost().getPort()),
                    () -> assertArrayEquals(new byte[]{127,0,0,1}, r.getResponseHost().getAddress().getAddress()),
                    () -> assertArrayEquals(new byte[] {1,2,3,4}, r.getResultList().getFirst().getFileID()),
                    () -> assertEquals(56, r.getResultList().getFirst().getFileSize()),
                    () -> assertEquals("TESTING", r.getResultList().getFirst().getFileName()));
        }
        @Test
        void setterValidFunctions() throws NullPointerException, IOException, BadAttributeValueException {
            ArrayList<Result> listing = new ArrayList<>();
            listing.add(new Result(new byte[]{1,2,3,4}, 56, "TESTING"));
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 23, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10};
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            r.setID(new byte[] {1, -0, 0, 1, 43, -12, -65, -20, 99, 47, 10, 100, 3, -128, 127});
            r.setTTL(0);
            r.setRoutingService(RoutingService.DEPTHFIRST);
            r.setResponseHost(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127,12,23,90}), 60));
            assertAll(() -> assertArrayEquals(new byte[] { 1, -0, 0, 1, 43, -12, -65, -20, 99, 47, 10, 100, 3, -128, 127 }, r.getID()),
                    () -> assertEquals(0, r.getTTL()),
                    () -> assertEquals(RoutingService.DEPTHFIRST, r.getRoutingService()),
                    () ->assertEquals(60,r.getResponseHost().getPort()),
                    () -> assertArrayEquals(new byte[]{127,12,23,90}, r.getResponseHost().getAddress().getAddress()),
                    () -> assertArrayEquals(new byte[] {1,2,3,4}, r.getResultList().getFirst().getFileID()),
                    () -> assertEquals(56, r.getResultList().getFirst().getFileSize()),
                    () -> assertEquals("TESTING", r.getResultList().getFirst().getFileName()));
        }
        @Test
        void InvalidType() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 19, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 23, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10 };
            assertThrows(BadAttributeValueException.class, () -> {
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void setterInvalidLongPayloadLength() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 30, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10 };
            assertThrows(BadAttributeValueException.class, () -> {
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void testingDecodeEncodeDecodeSingleResult() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 23, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10 };
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out));
            Response x = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
            assertAll(() -> assertArrayEquals(x.getID(), r.getID()),
                    () -> assertEquals(x.getTTL(), r.getTTL()),
                    () -> assertEquals(x.getRoutingService(), r.getRoutingService()),
                    () ->assertEquals(x.getResponseHost().getPort(),r.getResponseHost().getPort()),
                    () -> assertArrayEquals(x.getResponseHost().getAddress().getAddress(), r.getResponseHost().getAddress().getAddress()),
                    () -> assertArrayEquals(x.getResultList().getFirst().getFileID(), r.getResultList().getFirst().getFileID()),
                    () -> assertEquals(x.getResultList().getFirst().getFileSize(), r.getResultList().getFirst().getFileSize()),
                    () -> assertEquals(x.getResultList().getFirst().getFileName(), r.getResultList().getFirst().getFileName()));
            assertIterableEquals(x.getResultList(),r.getResultList());
            assertEquals(x,r);
            assertEquals(x,x);
            assertEquals(r,x);
        }
        @Test
        void testingEncodeDecodeEncodeSingleResult() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 23, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10 };
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out1));
            Response x = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out1.toByteArray())));
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            x.encode(new MessageOutput(out2));
            assertArrayEquals(out1.toByteArray(), out2.toByteArray());
//            Response finalR = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
//            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, finalR.getID()),
//                    () -> assertEquals(3, finalR.getTTL()),
//                    () -> assertEquals(RoutingService.BREADTHFIRST, finalR.getRoutingService()),
//                    () ->assertEquals(20,finalR.getResponseHost().getPort()),
//                    () -> assertArrayEquals(new byte[]{127,0,0,1}, finalR.getResponseHost().getAddress().getAddress()),
//                    () -> assertArrayEquals(new byte[] {1,2,3,4}, finalR.getResultList().getFirst().getFileID()),
//                    () -> assertEquals(56, finalR.getResultList().getFirst().getFileSize()),
//                    () -> assertEquals("TESTING", finalR.getResultList().getFirst().getFileName()));
        }
        @Test
        void testingToStringOneResult() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 23, 1, 0, 20, 127,0,0,1, 1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10 };
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            String value = r.toString();
            assertEquals("Response: ID=000000000000000000000000000000 TTL=3 Routing=BREADTHFIRST Host=127.0.0.1:20 [Result: FileID=01020304 FileSize=56 bytes FileName=TESTING]",value);
        }
        @Test
        void testingDecodeEncodeDecodeMultipleResult() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, 127,0,0,1,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out));
            Response x = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
            assertAll(() -> assertArrayEquals(x.getID(), r.getID()),
                    () -> assertEquals(x.getTTL(), r.getTTL()),
                    () -> assertEquals(x.getRoutingService(), r.getRoutingService()),
                    () ->assertEquals(x.getResponseHost().getPort(),r.getResponseHost().getPort()),
                    () -> assertArrayEquals(x.getResponseHost().getAddress().getAddress(), r.getResponseHost().getAddress().getAddress()),
                    () -> assertArrayEquals(x.getResultList().getFirst().getFileID(), r.getResultList().getFirst().getFileID()),
                    () -> assertEquals(x.getResultList().getFirst().getFileSize(), r.getResultList().getFirst().getFileSize()),
                    () -> assertEquals(x.getResultList().getFirst().getFileName(), r.getResultList().getFirst().getFileName()));
            assertIterableEquals(x.getResultList(),r.getResultList());
            assertEquals(x,r);
            assertEquals(x,x);
            assertEquals(r,x);
        }
        @Test
        void testingEncodeDecodeEncodeMultipleResult() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, 127,0,0,1,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out1));
            Response x = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out1.toByteArray())));
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            x.encode(new MessageOutput(out2));
            assertArrayEquals(out1.toByteArray(), out2.toByteArray());
        }
        @Test
        void testingToStringMultipleResult() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, 127,0,0,1,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            String value = r.toString();
            assertEquals("Response: ID=000000000000000000000000000000 TTL=3 Routing=BREADTHFIRST Host=127.0.0.1:20 " +
                    "[Result: FileID=01020304 FileSize=56 bytes FileName=TESTING, " +
                    "Result: FileID=FFFFFFFF FileSize=23 bytes FileName=funny, " +
                    "Result: FileID=64170A24 FileSize=3119 bytes FileName=ouch]",value);
        }
        @Test
        void testingInvalidINetSocketAddressMinMulticast() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, (byte) 0xE0,0,0,0,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            assertThrows(BadAttributeValueException.class, () -> {
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void testingInvalidINetSocketAddressMaxMulticast() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, (byte) 0xEF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            assertThrows(BadAttributeValueException.class, () -> {
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void testingInvalidINetSocketAddressLessBytes() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, (byte) 0xEF,(byte) 0xFF,(byte) 0xFF,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            assertThrows(BadAttributeValueException.class, () -> {
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }
        @Test
        void testingInvalidINetSocketAddressMoreBytes() throws BadAttributeValueException, IOException {
            byte[] enc = new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 50, 3, 0, 20, (byte) 0xEF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,(byte) 0xFF,
                    1, 2, 3, 4, 0, 0, 0, 56, 84, 69, 83, 84, 73, 78, 71, 10,
                    -1, -1, -1, -1, 0, 0, 0, 23, 'f','u','n','n','y', 10,
                    100, 23, 10, 36, 0, 0, 12, 47, 'o', 'u', 'c', 'h', 10};
            assertThrows(BadAttributeValueException.class, () -> {
                Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            });
        }

        @Test
        void testPayloadLength() throws BadAttributeValueException, IOException {
            Response r = new Response(
                    new byte[] {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1},
                    50,
                    RoutingService.BREADTHFIRST,
                    new InetSocketAddress("2.2.2.2", 13)
            );
            Result t = new Result(
                    new byte[] {1, 2, 3, 4},
                    56,
                    "o"
            );
            r.addResult(t);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            r.encode(new MessageOutput(outputStream));
            byte[] expected = new byte[] {2, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 17, 1, 0, 13, 2, 2, 2, 2, 1, 2, 3, 4, 0, 0, 0, 56, 111, 10};

            byte[] actual = outputStream.toByteArray();

            assertArrayEquals(expected, actual);
        }

        @Test
        void testingEncodeDecodeEncode() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] {2, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 17, 1, 0, 13, 2, 2, 2, 2, 1, 2, 3, 4, 0, 0, 0, 56, 111, 10};
            byte[] enc2 = new byte[] {2, -1, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 50, 0, 0, 17, 1, 0, 13, 2, 2, 2, 2, 1, 2, 3, 4, 0, 0, 0, 56, 111, 10};

            Response r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out1));
            Response x = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            x.encode(new MessageOutput(out2));
//            r = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out2.toByteArray())));
//            ByteArrayOutputStream out3 = new ByteArrayOutputStream();
//            r.encode(new MessageOutput(out3));
            assertArrayEquals(out1.toByteArray(), out2.toByteArray());
//            Response finalR = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
//            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, finalR.getID()),
//                    () -> assertEquals(3, finalR.getTTL()),
//                    () -> assertEquals(RoutingService.BREADTHFIRST, finalR.getRoutingService()),
//                    () ->assertEquals(20,finalR.getResponseHost().getPort()),
//                    () -> assertArrayEquals(new byte[]{127,0,0,1}, finalR.getResponseHost().getAddress().getAddress()),
//                    () -> assertArrayEquals(new byte[] {1,2,3,4}, finalR.getResultList().getFirst().getFileID()),
//                    () -> assertEquals(56, finalR.getResultList().getFirst().getFileSize()),
//                    () -> assertEquals("TESTING", finalR.getResultList().getFirst().getFileName()));
        }
        @Test
        void testingEncodeDecodeEncode2() throws NullPointerException, IOException, BadAttributeValueException{
            byte[] enc = new byte[] {1, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, -1, 0, 0, 2, 110, 101};
            Search r = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(enc)));
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            r.encode(new MessageOutput(out1));
            Search x = (Search) Message.decode(new MessageInput(new ByteArrayInputStream(out1.toByteArray())));
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            x.encode(new MessageOutput(out2));
            assertArrayEquals(out1.toByteArray(), out2.toByteArray());
//            Response finalR = (Response) Message.decode(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
//            assertAll(() -> assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, finalR.getID()),
//                    () -> assertEquals(3, finalR.getTTL()),
//                    () -> assertEquals(RoutingService.BREADTHFIRST, finalR.getRoutingService()),
//                    () ->assertEquals(20,finalR.getResponseHost().getPort()),
//                    () -> assertArrayEquals(new byte[]{127,0,0,1}, finalR.getResponseHost().getAddress().getAddress()),
//                    () -> assertArrayEquals(new byte[] {1,2,3,4}, finalR.getResultList().getFirst().getFileID()),
//                    () -> assertEquals(56, finalR.getResultList().getFirst().getFileSize()),
//                    () -> assertEquals("TESTING", finalR.getResultList().getFirst().getFileName()));
        }
    }
}
