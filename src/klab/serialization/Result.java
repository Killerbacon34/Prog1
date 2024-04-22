/**
 * Author:      Alex DeVries
 * Assignment:  Program 0
 * Class:       CSI 4321 Data Communications
 */
package klab.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single search result
 */
public class Result {
    final static int FILE_ID_SIZE = 4;
    final static int FILE_SIZE_LENGTH = 4;
    final static int MIN_FILE_SIZE = 0;
    final static long MAX_FILE_SIZE = 4294967295L;

    //File ID of the given result
    private byte[] fileID;
    //file size of the given result
    private long fileSize;
    //file name of the given result
    private String fileName;


    /**
     * check fileID given to see if its valid
     * @param fileID fileID
     * @throws BadAttributeValueException if fileID is invalid
     */
    public void CheckFileID(byte[] fileID) throws BadAttributeValueException {
        if(fileID == null){
            throw new BadAttributeValueException("Null value error", "fileID",
                    new NullPointerException());
        }
        if(fileID.length != FILE_SIZE_LENGTH){
            throw new BadAttributeValueException("fileID too big", "fileID");
        }
    }

    /**
     * check fileSize given to see if its valid
     * @param fileSize fileSize
     * @throws BadAttributeValueException if fileSize is invalid
     */
    public void CheckFileSize(long fileSize) throws BadAttributeValueException {
        if(fileSize < MIN_FILE_SIZE || fileSize > MAX_FILE_SIZE){
            throw new BadAttributeValueException("Invalid value present",
                    "fileSize", new IllegalArgumentException(""));
        }
    }

    /**
     * check fileName given to see if its valid
     * @param fileName fileName
     * @throws BadAttributeValueException if fileName is invalid
     */
    public void CheckFileName(String fileName) throws BadAttributeValueException{
        if(fileName == null || fileName.equals("\n")){
            throw new BadAttributeValueException("Null value error",
                    "fileName", new NullPointerException());
        }
        if(!fileName.matches("[a-zA-Z0-9_.-]+")){
            throw new BadAttributeValueException("Invalid value entered",
                    "fileName", new IllegalArgumentException());
        }
    }

    /**
     * Constructs a Result from given attributes
     *
     * @param fileID file ID
     * @param fileSize file size
     * @param fileName file name
     * @throws BadAttributeValueException if any parameter fails validation
     */
    public Result(byte[] fileID, long fileSize, String fileName)
            throws BadAttributeValueException {
        setFileID(fileID);
        setFileSize(fileSize);
        setFileName(fileName);
    }

    /**
     * Constructs a Result from given input source
     *
     * @param in input source to parse
     * @throws IOException if in is null or an I/O problem occurs
     * @throws BadAttributeValueException if any parsed value fails validation
     */
    public Result(MessageInput in) throws IOException,
            BadAttributeValueException{
        if(in == null){
            throw new IOException("Input sink cannot be null");
        }
        try{
            int x = -1;
            fileID = new byte[FILE_ID_SIZE];
            fileID = in.readNBytes(FILE_ID_SIZE);
            fileSize = in.readUnsignedInt(FILE_SIZE_LENGTH);
            fileName = "";
            while(((x = in.readOnce()) > -1) && ((char)x != '\n')) {
                fileName += (char) x;
            }
            if (fileName.isEmpty() || x == -1){
                throw new IOException();
            }
            CheckFileID(fileID);
            CheckFileSize(fileSize);
            CheckFileName(fileName);
        }
        catch (IOException e) {
            throw new IOException(e);
        }
        catch (Exception e){
            throw e;
        }
    }

    /**
     * Serialize to given output sink
     *
     * @param out output sink to serialize to
     * @throws IOException if out is null or an I/O problem occurs
     */
    public void encode(MessageOutput out) throws IOException {
        if(out == null){
            throw new IOException("Message was null");
        }
        try{
            out.writeBytes(fileID);
            out.writeUnsignedInt(fileSize,FILE_SIZE_LENGTH);
            out.writeBytes(fileName.getBytes(StandardCharsets.US_ASCII));
            out.writeBytes("\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
        }
        catch (IOException e){
            throw new IOException("Error occurred");
        }
    }

    /**
     * Returns a String representation
     *
     * @return FileID=ID FileSize=size bytes FileName=name FileID is
     * represented as an 8 character hex string (2 chars per byte)
     */
    public String toString(){
        StringBuilder formatted = new StringBuilder();
        formatted.append("Result: FileID=");
        for(byte b : this.fileID){
            formatted.append(String.format("%02X", b));
        }
        formatted.append(" FileSize=");
        formatted.append(this.fileSize);
        formatted.append(" bytes FileName=");
        formatted.append(this.fileName);
        return formatted.toString();
    }

    /**
     * get fileID
     *
     * @return file ID
     */
    public byte[] getFileID(){
        return fileID;
    }

    /**
     * set fileID
     *
     * @param fileID new fileID
     * @return this result with new fileID
     * @throws BadAttributeValueException if fileID fails validation
     */
    public final Result setFileID(byte[] fileID)
            throws BadAttributeValueException {
        CheckFileID(fileID);
        this.fileID = fileID;
        return this;
    }

    /**
     * get file size
     *
     * @return file size
     */
    public long getFileSize(){
        return fileSize;
    }

    /**
     * set file size
     *
     * @param fileSize new file size
     * @return this Result with new file size
     * @throws BadAttributeValueException if fileSize fails validation
     */
    public final Result setFileSize(long fileSize)
            throws BadAttributeValueException {
        CheckFileSize(fileSize);
        this.fileSize = fileSize;
        return this;
    }

    /**
     * get file name
     *
     * @return file name
     */
    public String getFileName(){
        return fileName;
    }

    /** set file name
     *
     * @param fileName new file name
     * @return the Result with new file name
     * @throws BadAttributeValueException if fileName fails validation
     */
    public final Result setFileName(String fileName)
            throws BadAttributeValueException {
        CheckFileName(fileName);
        this.fileName = fileName;
        return this;
    }

    /**
     * get the size of the result including its header
     * @return the size of the result
     */
    public int getSize(){
        return FILE_ID_SIZE + FILE_SIZE_LENGTH + this.fileName.length() + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result result)) return false;
        return getFileSize() == result.getFileSize() &&
                Arrays.equals(getFileID(), result.getFileID())
                && getFileName().equals(result.getFileName());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getFileSize(), getFileName());
        result = 31 * result + Arrays.hashCode(getFileID());
        return result;
    }

}
