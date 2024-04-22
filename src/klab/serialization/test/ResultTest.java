/**
 * Author:      Alex DeVries
 * Assignment:  Program 1
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization.test;

import klab.serialization.BadAttributeValueException;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing for all result test cases
 */
public class ResultTest {
    /**
     * Testing for null constructors
     */
    @Nested
    class NullTestConstructors {
        /**
         * Testing for result constructor
         */
        @Test
        void testResultConstructor() {
            assertThrows(BadAttributeValueException.class, () -> new Result(null, 16L, "testfile"));
        }
        /**
         * Testing for MessageInput constructor
         */
        @Test
        void testMessageInputConstructor() {
            assertThrows(NullPointerException.class, () -> new MessageInput(null));
        }

        /**
         * Testing for result constructor
         */
        @Test
        void testResultMessageInputConstructor() {
            assertThrows(IOException.class, () -> new Result(null));
        }

        /**
         * Testing for MessageOutput constructor
         */
        @Test
        void testMessageOutputConstructor() {
            assertThrows(NullPointerException.class, () -> new MessageOutput(null));
        }
    }

    /**
     * Tesing null for functions within result
     */
    @Nested
    class NullTestFunctions {
        byte[] data = new byte[4];

        /**
         * Testing encode
         */
        @Test
        void testResultEncode(){
            assertThrows(IOException.class, ()-> {
                final Result testResult = new Result(data, 500L, "testing.txt");
                testResult.encode(null);
            });
        }

        /**
         * Testing fileID
         */
        @Test
        void testResultSetFileID(){
            assertThrows(BadAttributeValueException.class, ()-> {
                final Result testResult = new Result(data, 500L, "testing.txt");
                testResult.setFileID(null);
            });
        }

        /**
         * testing file name
         */
        @Test
        void testResultSetFileName(){
            assertThrows(BadAttributeValueException.class, ()-> {
                final Result testResult = new Result(data, 500L, "testing.txt");
                testResult.setFileName(null);
            });
        }
    }

    /**
     * Testing for file size
     */
    @Nested
    class SetFileSize {
        /**
         * testing max file size
         * @throws BadAttributeValueException if invalid value is entered
         * @throws IOException if a parsing error occurs
         */
        @Test
        void testMaxFileSize() throws BadAttributeValueException, IOException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            r.setFileSize(4294967295L);
            assertEquals(4294967295L, r.getFileSize());
        }

        /**
         * testing min file size
         * @throws BadAttributeValueException if invalid value is entered
         * @throws IOException if a parsing error occurs
         */
        @Test
        void testMinFileSize() throws BadAttributeValueException, IOException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            r.setFileSize(0);
            assertEquals(0, r.getFileSize());
        }
        /**
         * testing negative file size
         * @throws BadAttributeValueException if invalid value is entered
         * @throws IOException if a parsing error occurs
         */
        @Test
        void testNegativeFileSize() throws BadAttributeValueException, IOException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            assertThrows(BadAttributeValueException.class, () -> r.setFileSize(-1000000L));
        }
        /**
         * testing a valid file size
         * @throws BadAttributeValueException if invalid value is entered
         * @throws IOException if a parsing error occurs
         */
        @Test
        void testValidFileSize() throws BadAttributeValueException, IOException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            r.setFileSize(500);
            assertEquals(500, r.getFileSize());
        }
    }

    /**
     * Testing fileID values
     */
    @Nested
    class setFileID {
        /**
         * testing max fileID
         * @throws BadAttributeValueException if an invalid value is entered
         * @throws IOException if a parsing error occurs
         */
        @Test
        void testMaxFileID() throws BadAttributeValueException, IOException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            r.setFileID(new byte[] {127,127,127,127});
            assertArrayEquals(new byte[] {127,127,127,127}, r.getFileID());
        }
        /**
         * testing min fileID
         * @throws BadAttributeValueException if an invalid value is entered
         * @throws IOException if a parsing error occurs
         */
        @Test
        void testMinFileID() throws BadAttributeValueException, IOException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            r.setFileID(new byte[] {0,0,0,0});
            assertArrayEquals(new byte[] {0,0,0,0}, r.getFileID());
        }
    }

    /**
     * Testing for message input and output
     */
    @Nested
    class MessageResult {
        /**
         * test for messageInput
         * @throws NullPointerException if null value is used
         * @throws IOException if a parsing error occurs
         * @throws BadAttributeValueException if an invalid value is entered
         */
        @Test
        void testResultMessageInput() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] result = new byte[] {1, 2, 3, 4, 0, 0, 0, 30, 'f', 'o', 'o', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            assertArrayEquals(new byte[] {1,2,3,4}, r.getFileID());
            assertEquals(30, r.getFileSize());
            assertEquals("foo", r.getFileName());
        }
        /**
         * test for messageInput
         * @throws NullPointerException if null value is used
         * @throws IOException if a parsing error occurs
         * @throws BadAttributeValueException if an invalid value is entered
         */
        @Test
        void testResultMessageInput2() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] result = new byte[] {0, 0, 0, 0, 0, -1, 0, 0, 111, 110, 101, 10};
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            assertArrayEquals(new byte[] {0,0,0,0}, r.getFileID());
            assertEquals(16711680L, r.getFileSize());
            assertEquals("one", r.getFileName());
        }

        //THIS TEST IS INACCURATE FOR NOW, FIND WAY TO GET CONTENTS OF
        //OUTPUTSTREAM INSIDE MESSAGEOUTPUT TO PASS
        /**
         * test for messageInput
         * @throws NullPointerException if null value is used
         * @throws IOException if a parsing error occurs
         * @throws BadAttributeValueException if an invalid value is entered
         */
        @Test
        void testResultMessageOutput() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] result = new byte[] {1, 2, 3, 4, -1, -1, -1, -1, 'f', 'o', 'o', '\n'};

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            var x = new MessageOutput(out);
            var r = new Result(new MessageInput(new ByteArrayInputStream(result)));
            r.encode(x);
            var y = new Result(new MessageInput(new ByteArrayInputStream(out.toByteArray())));
            assertArrayEquals(y.getFileID(), r.getFileID());
            assertEquals(y.getFileSize(), r.getFileSize());
            assertEquals(y.getFileName(), r.getFileName());
//            String test = new String(result, StandardCharsets.US_ASCII);
//            String test2 = new String(result, StandardCharsets.US_ASCII);
//            assertEquals(test, test2);
        }
    }

    /**
     * testing for the tostring for result
     */
    @Nested
    class ToStringTest {
        /**
         * testing tostring
         * @throws NullPointerException is null value is entered
         * @throws IOException if parsing error occurs
         * @throws BadAttributeValueException is any invalid value is entered
         */
        @Test
        void testResultToString() throws NullPointerException, IOException, BadAttributeValueException {
            byte[] data = new byte[] {12, 34, 56, 78, 0, 0, 0, 0, 'w', 'o', 'r', 'd', '\n'};
            var r = new Result(new MessageInput(new ByteArrayInputStream(data)));
            assertEquals("Result: FileID=0C22384E FileSize=0 bytes FileName=word", r.toString());
        }
    }
}
