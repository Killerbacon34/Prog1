/**
 * Author:      Alex DeVries
 * Assignment:  Program 0
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

/**
 * Deserialization input source for only general methods for parsing
 */
public class MessageInput {
    private BufferedInputStream in;

    /**
     * Constructs a new input source from an InputStream
     *
     * @param in byte input source
     * @throws NullPointerException is in is null
     */
    public MessageInput(InputStream in) throws NullPointerException {
        Objects.requireNonNull(in, "InputStream was null");
        this.in = new BufferedInputStream(in,1024);
    }


    /**
     * Reads in a certain number of bytes and converts it
     * to an unsigned integer
     * @param length number of bytes to read
     * @return the unsigned integer valye of bytes read
     * @throws IOException if any read errors occur
     */
    public long readUnsignedInt(int length) throws IOException {
        long result = 0;
        for (int i = 0; i < length; i++) {
            int currByte = in.read();
            if (currByte == -1) {
                throw new IOException();
            }
            result = (result << 8) | (currByte & 0xFF);
        }
        return result;
    }
    /**
     * reads a certain number of bytes from input stream
     * @param numBytes number of bytes
     * @return the byte array of values read
     * @throws IOException if an I/O error occurs
     */
    public byte[] readNBytes(int numBytes) throws IOException {
        byte[] bytes = new byte[numBytes];
        int currBytesRead = 0;
        int count = -1;
        while(currBytesRead < numBytes){
            count = in.read(bytes,currBytesRead,numBytes-currBytesRead);
            if(count == -1){
                throw new IOException();
            }
            currBytesRead += count;
        }
        return bytes;
    }
    /**
     * read in a single byte
     * @return integer value of byte read from stream
     * @throws IOException if I/O error occurs
     */
    public int readOnce() throws IOException {
        int value = -1;
        value = in.read();
        if(value == -1){
            throw new IOException();
        }
        return value;
    }

    /**
     * a function to read all bytes within an inputstream
     * @return a byte array with the contents
     * @throws IOException if an I/O error occurs
     */
    public byte[] readBytes() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = in.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
