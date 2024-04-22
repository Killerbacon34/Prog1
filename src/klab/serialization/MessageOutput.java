/**
 * Author:      Alex DeVries
 * Assignment:  Program 0
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Serialization output source for only general methods for output
 */
public class MessageOutput {
    private BufferedOutputStream out;
    /**
     * Constructs a new output source from an OutputStream
     *
     * @param out byte output sink
     * @throws NullPointerException if out is null
     */
    public MessageOutput(OutputStream out) throws NullPointerException {
        Objects.requireNonNull(out, "OutputStream was null");
        this.out = new BufferedOutputStream(out,1024);
    }


    /**
     * writes in an unsigned integer given by converting it
     * to bytes to be written to the OutputStream
     * @param number the unsigned integer to be written
     * @param length the number of bytes to write to
     * @throws IOException if any writing errors occur
     */
    public void writeUnsignedInt(long number, int length) throws IOException {
        byte[] value = new byte[length];
        for (int i = length-1; i >= 0; i--) {
            value[i] = (byte)(number & 0xFF);
            number >>= 8;
        }
        out.write(value);
    }
    /**
     * writes a number of bytes to output stream
     * @param values value to be written
     * @throws IOException if an I/O error occurs
     */
    public void writeBytes(byte[] values) throws IOException {
        try{
            out.write(values);
        }
        catch(IOException e){
            throw new IOException();
        }
    }

    /**
     * flush function for the output stream
     * @throws IOException if an I/O exception occurs
     */
    public void flush() throws IOException {
        out.flush();
    }
}