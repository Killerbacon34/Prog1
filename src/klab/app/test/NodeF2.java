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
 * F2 node in Network test
 * 
 * @version 1.0
 */
public class NodeF2 {
  public static void main(String[] args) throws IOException, BadAttributeValueException {
    // Check arguments
    if (args.length != 1) {
      throw new IllegalArgumentException("Parameter(s): <port>");
    }

    // Create server socket, accept connection, and set up I/O
    try (ServerSocket ss = new ServerSocket(Integer.parseInt(args[0])); Socket s = ss.accept()) {
      MessageInput in = new MessageInput(s.getInputStream());
      MessageOutput out = new MessageOutput(s.getOutputStream());

      // Receive search for "b"
      Search msg = (Search) decode(in);
      System.out.println("Received search for " + msg.getSearchString());

      // Send response for "b"
      new Response(msg.getID(), 3, BREADTHFIRST, new InetSocketAddress("22.2.22.2", 2222))
          .addResult(new Result(new byte[] { 3, 3, 3, 3 }, 25, "f1rub")).encode(out);
      
      // Receive messages
      while (true) {
        System.out.println("Received " + decode(in));
      }
    }
  }
}
