package klab.app.test;

import static klab.serialization.Message.decode;
import static klab.serialization.RoutingService.BREADTHFIRST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import klab.serialization.BadAttributeValueException;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.Response;
import klab.serialization.Result;
import klab.serialization.Search;

/**
 * Test handling non-match response:
 * 
 * Send search Receive non-matching and then matching response
 **/
public class NodeNonMatch {
    public static void main(String[] args) throws IOException, BadAttributeValueException {
        // Check arguments
        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter(s): <port>");
        }

        // Create server socket, accept connection, and set up I/O
        try (ServerSocket ss = new ServerSocket(Integer.parseInt(args[0])); Socket s = ss.accept()) {
            MessageInput in = new MessageInput(s.getInputStream());
            MessageOutput out = new MessageOutput(s.getOutputStream());

            // Note: Some casting below not necessary for OOP; used to force cast exception
            // Get search
            Search srch = (Search) decode(in);

            // Send responses (reverse order)
            byte[] badID = Arrays.copyOf(srch.getID(), srch.getID().length);
            badID[14] = 124;
            new Response(badID, 4, BREADTHFIRST, new InetSocketAddress("2.2.2.2", 222))
                    .addResult(new Result(new byte[] { 4, 4, 4, 4 }, 50, "bad")).encode(out);
            new Response(srch.getID(), 40, BREADTHFIRST, new InetSocketAddress("1.1.1.1", 111))
                    .addResult(new Result(new byte[] { 5, 5, 5, 5 }, 500, "good")).encode(out);
        }
    }
}
