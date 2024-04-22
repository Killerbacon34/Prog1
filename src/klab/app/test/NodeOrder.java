package klab.app.test;

import static klab.serialization.Message.decode;
import static klab.serialization.RoutingService.BREADTHFIRST;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import klab.serialization.BadAttributeValueException;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.Response;
import klab.serialization.Result;
import klab.serialization.Search;

/**
 * Test handling response order:
 * 
 * Receive search and send response Send correct searches Receive response in
 * reverse order of searches
 * 
 * @version 1.0
 **/
public class NodeOrder {
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

      // Send search
      new Search(new byte[] { 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7 }, 4, BREADTHFIRST, "og").encode(out);
      System.out.println((Response) decode(in));

      // Get searches
      Search s1 = (Search) decode(in);
      Search s2 = (Search) decode(in);

      // Send responses (reverse order)
      new Response(s2.getID(), 4, BREADTHFIRST, new InetSocketAddress("2.2.2.2", 222))
          .addResult(new Result(new byte[] { 4, 4, 4, 4 }, 50, "aaa")).encode(out);
      new Response(s1.getID(), 40, BREADTHFIRST, new InetSocketAddress("1.1.1.1", 111))
          .addResult(new Result(new byte[] { 5, 5, 5, 5 }, 500, "bbbb"))
          .addResult(new Result(new byte[] { 6, 6, 6, 6 }, 5000, "bbbb7")).encode(out);
    }
  }
}
