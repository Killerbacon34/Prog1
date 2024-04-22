package metanode.serialization.test;

import klab.serialization.BadAttributeValueException;
import klab.serialization.Response;
import klab.serialization.RoutingService;
import klab.serialization.Search;
import metanode.serialization.ErrorType;
import metanode.serialization.Message;
import metanode.serialization.MessageType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MetANodeMessageTest {
    @Test
    void test() throws IllegalArgumentException, IOException {
        Message msg = new Message(new byte[] { 0x41, 0, 59, 0 });
        assertEquals(MessageType.RequestMetaNodes, msg.getType());
        assertEquals(ErrorType.None, msg.getError());
        assertEquals(59, msg.getSessionID());
        assertEquals(0, msg.getAddrList().size());
    }
    @Nested
    class NullTestConstructors {
        /**
         * testing for null MessageType
         */
        @Test
        void testMessageConstructorNullMessageType(){
            assertThrows(IllegalArgumentException.class, () -> new Message(null, ErrorType.None, 254));
        }
        /**
         * testing for null ErrorType
         */
        @Test
        void testMessageConstructorNullErrorType(){
            assertThrows(IllegalArgumentException.class, () -> new Message(MessageType.MetaNodeAdditions, null, 254));
        }
        /**
         * testing for negative sessionID
         */
        @Test
        void testMessageConstructorNegativeSessionID(){
            assertThrows(IllegalArgumentException.class, () -> new Message(MessageType.MetaNodeAdditions, ErrorType.None, -2));
        }
        /**
         * testing for too large sessionID
         */
        @Test
        void testMessageConstructorSessionIDTooBig(){
            assertThrows(IllegalArgumentException.class, () -> new Message(MessageType.MetaNodeAdditions, ErrorType.None, 256));
        }
        /**
         * testing for messageType and errorType
         */
        @Test
        void testMessageConstructorErrorValueWrongMessageType1(){
            assertThrows(IllegalArgumentException.class, () ->
                    new Message(MessageType.MetaNodeAdditions, ErrorType.System, 47));
        }

        @Test
        void testMessageByteArrayConstructorErrorValueWrongMessageType1(){
            byte[] expected = new byte[]{68,10,48,0};
            assertThrows(IllegalArgumentException.class, () ->
                    new Message(expected));
        }
        @Test
        void testMessageByteArrayConstructorErrorValueWrongMessageType2(){
            byte[] expected = new byte[]{68,20,48,0};
            assertThrows(IllegalArgumentException.class, () ->
                    new Message(expected));
        }
    }
    @Nested
    class TestFunctions{
        @Test
        void testEnums(){
            assertNull(MessageType.getByCode(7));
            assertNull(MessageType.getByCmd("CC"));
            assertNull(ErrorType.getByCode(2));
            assertEquals(MessageType.RequestNodes,MessageType.getByCode(0));
            assertEquals(MessageType.RequestNodes,MessageType.getByCmd("RN"));
            assertEquals(MessageType.RequestMetaNodes,MessageType.getByCode(1));
            assertEquals(MessageType.RequestMetaNodes,MessageType.getByCmd("RM"));
            assertEquals(MessageType.AnswerRequest,MessageType.getByCode(2));
            assertEquals(MessageType.AnswerRequest,MessageType.getByCmd("AR"));
            assertEquals(MessageType.NodeAdditions,MessageType.getByCode(3));
            assertEquals(MessageType.NodeAdditions,MessageType.getByCmd("NA"));
            assertEquals(MessageType.MetaNodeAdditions,MessageType.getByCode(4));
            assertEquals(MessageType.MetaNodeAdditions,MessageType.getByCmd("MA"));
            assertEquals(MessageType.NodeDeletions,MessageType.getByCode(5));
            assertEquals(MessageType.NodeDeletions,MessageType.getByCmd("ND"));
            assertEquals(MessageType.MetaNodeDeletions,MessageType.getByCode(6));
            assertEquals(MessageType.MetaNodeDeletions,MessageType.getByCmd("MD"));
            assertEquals(ErrorType.None,ErrorType.getByCode(0));
            assertEquals(ErrorType.System,ErrorType.getByCode(10));
            assertEquals(ErrorType.IncorrectPacket,ErrorType.getByCode(20));
        }

        @Test
        void testMessageEncode(){
            byte[] expected = new byte[]{67,0,48,0};
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            final Message testMessage = new Message(MessageType.NodeAdditions, ErrorType.None,48);
            byte[] result = testMessage.encode();
            assertAll(() -> assertArrayEquals(expected,result),
                    () -> assertEquals(MessageType.NodeAdditions, testMessage.getType()),
                    () -> assertEquals(ErrorType.None, testMessage.getError()),
                    () -> assertEquals(48, testMessage.getSessionID()),
                    () -> assertEquals(list, testMessage.getAddrList()));
        }
        @Test
        void testMessageDecodeWorking() throws IOException {
            byte[] expected = new byte[]{66,10,48,0};
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            Message testMessage = new Message(expected);
            byte[] array = testMessage.encode();
            assertAll(() -> assertArrayEquals(expected,array),
                    () -> assertEquals(MessageType.AnswerRequest, testMessage.getType()),
                    () -> assertEquals(ErrorType.System, testMessage.getError()),
                    () -> assertEquals(48, testMessage.getSessionID()),
                    () -> assertEquals(list, testMessage.getAddrList()));
        }
        @Test
        void testMessageDecodeWorking2() throws IOException {
            byte[] expected = new byte[]{68,0,-1,1,127,0,0,1,1,0};
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("127.0.0.1", 256));
            Message testMessage = new Message(expected);
            byte[] array = testMessage.encode();
            Message testMessage2 = new Message(array);
            assertAll(() -> assertArrayEquals(expected,array),
                    () -> assertEquals(MessageType.MetaNodeAdditions, testMessage.getType()),
                    () -> assertEquals(ErrorType.None, testMessage.getError()),
                    () -> assertEquals(255, testMessage.getSessionID()),
                    () -> assertEquals(list, testMessage.getAddrList()));
            assertIterableEquals(testMessage.getAddrList(),testMessage2.getAddrList());
            assertEquals(testMessage,testMessage2);
            assertEquals(testMessage,testMessage);
            assertEquals(testMessage2,testMessage);
        }
        @Test
        void testMessageDecodeFail1() {
            assertThrows(IOException.class,() -> {
                byte[] expected = new byte[]{68,0,48};
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageDecodeFail2() throws IOException {
            assertThrows(IOException.class,() -> {
                byte[] expected = new byte[]{68,0,-1,1,1,1,1,1,1,1,1,1,1,1,1};
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageDecodeFail3() throws IOException {
            assertThrows(IOException.class,() -> {
                byte[] expected = null;
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageDecodeFail4() {
            assertThrows(IllegalArgumentException.class,() -> {
                byte[] expected = new byte[]{-12,0,48,0};
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageDecodeFail5() {
            assertThrows(IOException.class,() -> {
                byte[] expected = new byte[]{68,0,48,1,-1,-1,-1,-1,-1,-1,1,1,1,1,1,1};
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageDecodeFail6() {
            assertThrows(IllegalArgumentException.class,() -> {
                byte[] expected = new byte[]{64,0,0,1};
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageDecodeFail7() {
            assertThrows(IOException.class,() -> {
                byte[] expected = new byte[]{66,0,0,1};
                Message copy = new Message(expected);
            });
        }
        @Test
        void testMessageSetSessionIDFail() {
            assertThrows(IllegalArgumentException.class,() -> {
                byte[] expected = new byte[]{68,0,48,0};
                Message copy = new Message(expected);
                copy.setSessionID(256);
            });
        }
        @Test
        void testMessageSetSessionIDFail2() {
            assertThrows(IllegalArgumentException.class,() -> {
                byte[] expected = new byte[]{68,0,48,0};
                Message copy = new Message(expected);
                copy.setSessionID(-100);
            });
        }
        @Test
        void testMessageSetSessionIDPass() throws IOException {
            byte[] expected = new byte[]{68,0,48,0};
            Message copy = new Message(expected);
            copy.setSessionID(25);
            assertEquals(25,copy.getSessionID());
        }
        @Test
        void testMessageSetSessionIDPass2() throws IOException {
            byte[] expected = new byte[]{68,0,48,0};
            Message copy = new Message(expected);
            copy.setSessionID(255);
            assertEquals(255,copy.getSessionID());
        }
        @Test
        void testMessageSetSessionIDPass3() throws IOException {
            byte[] expected = new byte[]{68,0,-1,0};
            Message copy = new Message(expected);
            copy.setSessionID(0);
            assertEquals(0,copy.getSessionID());
        }
        @Test
        void testMessageToString() throws IOException {
            byte[] expected = new byte[]{68,0,5,2,1,1,1,1,0,50,2,2,2,2,0,70};
            Message copy = new Message(expected);
            assertEquals(copy.toString(),"Type=MetaNodeAdditions Error=None Session ID=5 Addrs=1.1.1.1:50 2.2.2.2:70 ");
        }
        @Test
        void testMessageToString3() throws IOException {
            byte[] expected = new byte[]{68,0,5,2,1,1,1,1,0,50,2,2,2,2,0,70};
            Message copy = new Message(expected);
            copy.addAddress(new InetSocketAddress(Inet4Address.getLoopbackAddress(), 20));
            assertEquals(copy.toString(),"Type=MetaNodeAdditions Error=None Session ID=5 Addrs=1.1.1.1:50 2.2.2.2:70 127.0.0.1:20 ");
        }
        @Test
        void testMessageToString2() throws IOException {
            byte[] expected = new byte[]{64,0,5,0};
            Message copy = new Message(expected);
            assertEquals(copy.toString(),"Type=RequestNodes Error=None Session ID=5 Addrs=");
        }
        @Test
        void testMessageAddAddressPass1() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",255));
            byte[] expected = new byte[]{68,0,5,2,1,1,1,1,0,50,2,2,2,2,0,-1};
            Message testMessage = new Message(expected);
            testMessage.addAddress(new InetSocketAddress("3.3.3.3", 90));
            byte[] newlyExpected = new byte[]{68,0,5,3,1,1,1,1,0,50,2,2,2,2,0,-1,3,3,3,3,0,90};
            list.add(new InetSocketAddress("3.3.3.3", 90));
            byte[] array = testMessage.encode();
            Message testMessage2 = new Message(array);
            assertAll(() -> assertArrayEquals(newlyExpected,array),
                    () -> assertEquals(MessageType.MetaNodeAdditions, testMessage.getType()),
                    () -> assertEquals(ErrorType.None, testMessage.getError()),
                    () -> assertEquals(5, testMessage.getSessionID()),
                    () -> assertEquals(list, testMessage.getAddrList()));
            assertIterableEquals(testMessage.getAddrList(),testMessage2.getAddrList());
            assertEquals(testMessage,testMessage2);
            assertEquals(testMessage,testMessage);
            assertEquals(testMessage2,testMessage);
        }
        @Test
        void testMessageAddAddressFail1() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{64,0,5,1,1,1,1,1,0,50};
            assertThrows(IllegalArgumentException.class, () -> new Message(expected));
        }
        @Test
        void testMessageAddAddressFail2() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{64,0,5,0};
            Message testMessage = new Message(expected);
            assertThrows(IllegalArgumentException.class, () -> testMessage.addAddress(new InetSocketAddress("3.3.3.3", 90)));
        }
        @Test
        void testMessageAddAddressFail3() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{65,0,5,1,1,1,1,1,0,50};
            assertThrows(IllegalArgumentException.class, () -> new Message(expected));
        }
        @Test
        void testMessageAddAddressFail4() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{65,0,5,0};
            Message testMessage = new Message(expected);
            assertThrows(IllegalArgumentException.class, () -> testMessage.addAddress(new InetSocketAddress("3.3.3.3", 90)));
        }
        @Test
        void testMessageAddAddressPass2() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{68,0,5,1,1,1,1,1,0,50};
            Message t = new Message(expected);
            t.addAddress(new InetSocketAddress("1.1.1.1",50));
            assertEquals(t.toString(),"Type=MetaNodeAdditions Error=None Session ID=5 Addrs=1.1.1.1:50 ");
        }
        @Test
        void testMessageAddAddressPass3() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{68,0,5,1,1,1,1,1,0,50};
            Message t = new Message(expected);
            t.addAddress(new InetSocketAddress("1.1.1.1",60));
            assertEquals(t.toString(),"Type=MetaNodeAdditions Error=None Session ID=5 Addrs=1.1.1.1:50 1.1.1.1:60 ");
        }
        @Test
        void testMessageAddAddressFail5() throws IOException {
            byte[] expected = new byte[]{68,0,5,0};
            Message t = new Message(expected);
            t.addAddress(new InetSocketAddress("1.1.1.1",60));
            for(int x = 5; x < 260; x++){
                t.addAddress(new InetSocketAddress("1.1.1.1",x));
            }
            assertThrows(IllegalArgumentException.class, () -> t.addAddress(new InetSocketAddress("1.1.1.1",700)));
        }
        @Test
        void testMessageAddAddressFail6() throws IOException {
            byte[] expected = new byte[]{68,0,5,1,1,1,1,1,0,-1};
            Message t = new Message(expected);
            for(int x = 0; x < 254; x++){
                t.addAddress(new InetSocketAddress("1.1.1.1",x));
            }
            t.setSessionID(255);
            assertThrows(IllegalArgumentException.class, () -> t.addAddress(new InetSocketAddress("1.1.1.1",700)));
        }
        @Test
        void testMessageAddAddressFail7() throws IOException {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            list.add(new InetSocketAddress("1.1.1.1",50));
            list.add(new InetSocketAddress("2.2.2.2",70));
            byte[] expected = new byte[]{64,0,5,0};
            Message testMessage = new Message(expected);
            assertThrows(IllegalArgumentException.class, () -> testMessage.addAddress(null));
        }
    }
}
