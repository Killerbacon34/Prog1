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
 * Test combination of reverse response order and non-matching response
 * 
 * @version 1.0
 */
public class NodeOrderNonMatch {
  public static void main(String[] args) throws IOException, BadAttributeValueException {
    // Check arguments
    if (args.length != 1) {
      throw new IllegalArgumentException("Parameter(s): <node port>");
    }

    // Create server socket, accept connection, and set up I/O
    try (ServerSocket ss = new ServerSocket(Integer.parseInt(args[0])); Socket s = ss.accept()) {
      MessageInput in = new MessageInput(s.getInputStream());
      MessageOutput out = new MessageOutput(s.getOutputStream());

      // Note: Some casting below not necessary for OOP; used to force cast exception
      /*
       * Test receiving search and sending bad + reordered responses
       */
      Search s1 = (Search) decode(in);
      Search s2 = (Search) decode(in);

      // Send responses (reverse order)
      byte[] badID = Arrays.copyOf(s2.getID(), s2.getID().length);
      badID[14] = 124;
      new Response(badID, 4, BREADTHFIRST, new InetSocketAddress("2.2.2.2", 222))
          .addResult(new Result(new byte[] { 4, 4, 4, 4 }, 50, "bad")).encode(out);
      new Response(s2.getID(), 4, BREADTHFIRST, new InetSocketAddress("2.2.2.2", 222))
          .addResult(new Result(new byte[] { 4, 4, 4, 4 }, 50, "aaa")).encode(out);
      new Response(s1.getID(), 40, BREADTHFIRST, new InetSocketAddress("1.1.1.1", 111))
          .addResult(new Result(new byte[] { 5, 5, 5, 5 }, 500, "bbbb"))
          .addResult(new Result(new byte[] { 6, 6, 6, 6 }, 5000, "bbbb7")).encode(out);

      /*
       * Test send search; expect Download response
       */
      new Search(new byte[] { 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7 }, 4, BREADTHFIRST, "og").encode(out);
      Response r = (Response) decode(in);
      System.out.println(r);

      /*
       * Test sending empty search; expect node address
       */
      new Search(new byte[] { 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 8 }, 4, BREADTHFIRST, "").encode(out);
      System.out.println(decode(in));
    }
  }
}
