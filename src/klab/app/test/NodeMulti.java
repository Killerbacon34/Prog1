package klab.app.test;

import static klab.serialization.Message.decode;
import static klab.serialization.RoutingService.BREADTHFIRST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import klab.serialization.BadAttributeValueException;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.Response;
import klab.serialization.Result;
import klab.serialization.Search;

/**
 * Test handling multi searches and response:
 * Get N searches
 * Send N responses intermixed with N searches
 * Get N responses
 *
 * @version 1.0
 **/
public class NodeMulti {
    private static final int NOSEARCHES = 8;

    public static void main(String[] args) throws IOException, BadAttributeValueException {
        // Check arguments
        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter(s): <port>");
        }

        // Create server socket, accept connection, and set up I/O
        try (ServerSocket ss = new ServerSocket(Integer.parseInt(args[0])); Socket s = ss.accept()) {
            MessageInput in = new MessageInput(s.getInputStream());
            MessageOutput out = new MessageOutput(s.getOutputStream());

            List<Response> responseList = new ArrayList<>();

            // Receive searches and store response in random order
            for (int i=0; i < NOSEARCHES; i++) {
                Search srch = (Search) decode(in);
                responseList.add(new Response(srch.getID(), 6, BREADTHFIRST, new InetSocketAddress(i+"."+i+"."+i+"."+i, i)).addResult(new Result(new byte[] {1,2,3,4}, i, "file"+i)));
            }
            Collections.shuffle(responseList);


            // Send stored responses intermixed with searches
            byte id = 10;
            for (Response r : responseList) {
//                System.out.println(r.toString());
                r.encode(out);
                new Search(new byte[] {id,id,id,id,id,id,id,id,id,id,id,id,id,id,id}, 4, BREADTHFIRST, "e").encode(out);
                id++;
            }
            // Receive responses
            for (int i=0; i < NOSEARCHES; i++) {
                System.out.println((Response)decode(in));
            }
        }
    }
}
