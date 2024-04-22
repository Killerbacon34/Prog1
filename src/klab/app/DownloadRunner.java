/**
 * Author:      Alex DeVries
 * Assignment:  Program 3
 * Class:       CSI 4321 Data Communications
 */
package klab.app;

import klab.app.Node;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The downloadRunner class which implements the ability
 * to receive file IDs after an established connection
 * and send out the file contents if it exists
 */
public class DownloadRunner implements Runnable{
    /**
     * The length of the message sent by downloading
     */
    private static final int MESSAGE_lENGTH = 9;
    /**
     * the length of the message without the delimiter
     */
    private static final int FILE_ID_LENGTH = 8;
    /**
     * the file ID length compressed into hex values
     */
    private static final int COMPRESSED_FILE_ID_SIZE = 4;
    /**
     * the value used to help calculate the compressed file ID
     */
    private static final int FILE_ID_COMPRESS_DIVISOR = 2;
    /**
     * the base of hex digits
     */
    private static final int BASE_HEX_VALUE = 16;

    /**
     * the socket for the current download connection
     */
    private final Socket currConnection;
    /**
     * the directory path to look through when trying to download
     */
    private final String directoryPath;

    /**
     * constructor for the DownloadRunner class
     * @param socket the socket to download with
     * @param directoryPath the directory path to find the files to use
     */
    public DownloadRunner(Socket socket, String directoryPath) {
        this.currConnection = socket;
        this.directoryPath = directoryPath;
    }

    /**
     * Run method within the DownloadRunner instance to perform
     * the process of downloading the desired file to the established
     * connection it was given
     */
    public void run() {
        Logger logger = Logger.getLogger("Node.Log");
        try{
            MessageInput downloadInStream =
                    new MessageInput(currConnection.getInputStream());
            MessageOutput downloadOutStream =
                    new MessageOutput(currConnection.getOutputStream());

            byte[] fileID = downloadInStream.readNBytes(MESSAGE_lENGTH);
            fileID = Arrays.copyOf(fileID,FILE_ID_LENGTH);
            String tempString = new String(fileID);

            byte[] compactedFileID = new byte[COMPRESSED_FILE_ID_SIZE];
            for (int i = 0; i < fileID.length; i += 2) {
                String hexByte = tempString.
                        substring(i, i + FILE_ID_COMPRESS_DIVISOR);
                compactedFileID[i / FILE_ID_COMPRESS_DIVISOR] =
                        (byte) Integer.parseInt(hexByte, BASE_HEX_VALUE);
            }

            logger.log(Level.INFO,
                    "Received download request with fileID: "
                            + new String(fileID));
            long result = 0;
            for (int i = 0; i < compactedFileID.length; i++) {
                result = (result << 8) | (compactedFileID[i] & 0xFF);
            }

            if(Node.getFileMapping().containsValue(result)){
                logger.log(Level.INFO, "FileID: " + new String(fileID) +
                        " does exist.  Attempting to send file contents.");
                Path temp = Paths.
                        get(directoryPath+"\\"+
                                Node.getFileNameFromMap(result));
                FileInputStream fi = new FileInputStream(temp.toFile());
                currConnection.getOutputStream().
                        write("OK\n\n".getBytes(StandardCharsets.US_ASCII));
                fi.transferTo(currConnection.getOutputStream());
            }

            else if(!Node.getFileMapping().containsValue(result)){
                logger.log(Level.INFO, "FileID: " + new String(fileID) +
                        " does not exist.  Attempting to send error message");
                downloadOutStream.
                        writeBytes(("ERROR ID (" + new String(fileID) +
                        ") not found").getBytes(StandardCharsets.US_ASCII));
                downloadOutStream.flush();
            }

            this.currConnection.close();
        }catch (IOException e) {
            logger.log(Level.INFO,
                    "Socket closed: " + e.getLocalizedMessage(), e);
        }
    }
}
