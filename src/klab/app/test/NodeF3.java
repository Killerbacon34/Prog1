package klab.app.test;

import static klab.serialization.Message.decode;

import java.io.IOException;
import java.net.Socket;
import java.util.stream.IntStream;

import klab.serialization.BadAttributeValueException;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.RoutingService;
import klab.serialization.Search;

/**
 * F3 node in Network test
 * 
 * @version 1.0
 */
public class NodeF3 {
  public static void main(String[] args) throws IOException, BadAttributeValueException {
    // Check arguments
    if (args.length != 2) {
      throw new IllegalArgumentException("Parameter(s): <id> <port>");
    }

    try (Socket s = new Socket(args[0], Integer.parseInt(args[1]))) {
      MessageInput in = new MessageInput(s.getInputStream());
      MessageOutput out = new MessageOutput(s.getOutputStream());

      // Send search for "b"
      byte[] id = new byte[15];
      IntStream.range(0, 15).forEach(i -> id[i] = (byte) i);
      IntStream.range(0, 15).forEach(i -> id[i] = (byte) i);
      new Search(id, 6, RoutingService.BREADTHFIRST, "b").encode(out);

      // Receive messages
      while (true) {
        System.out.println("Received " + decode(in));
      }
    }
  }
}
