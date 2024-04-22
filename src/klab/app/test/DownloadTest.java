package klab.app.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import klab.serialization.Message;
import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;
import klab.serialization.Response;
import klab.serialization.RoutingService;
import klab.serialization.Search;

/**
 * Test klab Download server
 * 
 * @version 1.0
 */
class DownloadTest {
  /**
   * Default character encoding
   */
  private static final Charset CHARENC = StandardCharsets.US_ASCII;
  protected static final Map<String, String> files = new HashMap<>();
  protected static String server;
  protected static int port;

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: <server> <port>");
      System.exit(1);
    }
    // Get NODE server and port
    server = args[0];
    port = Integer.parseInt(args[1]);

    // Connect to NODE and search for urp
    try (Socket sock = new Socket(server, port);
        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream()) {
      MessageOutput mout = new MessageOutput(out);
      new Search(genID(), 2, RoutingService.BREADTHFIRST, "urp").encode(mout);
      Response response = (Response) Message.decode(new MessageInput(in));
      // Get DOWNLOAD server and port
      server = response.getResponseHost().getAddress().getHostAddress();
      port = response.getResponseHost().getPort();

      response.getResultList().forEach(r -> files.put(r.getFileName(), bytes2String(r.getFileID())));
      
      testBasic();
      testSlow();
      testMulti();
    }
  }

  protected static Socket createConnection() throws UnknownHostException, IOException {
    Socket sock = new Socket();
    sock.setReceiveBufferSize(1000);
    sock.connect(new InetSocketAddress(server, port));
    return sock;
  }

  private static final byte[] yurpsha = new byte[] { 0x57, 0x1c, 0x4b, (byte) 0x8b, (byte) 0xf8, (byte) 0xef,
      (byte) 0xab, (byte) 0xed, (byte) 0x9c, (byte) 0xf2, (byte) 0xc2, 0x43, 0x2f, (byte) 0xf2, (byte) 0x96,
      (byte) 0xaf, 0x38, 0x0b, 0x43, (byte) 0xc1, (byte) 0x82, 0x08, 0x55, (byte) 0xa2, (byte) 0xd3, 0x02, 0x05, 0x2a,
      (byte) 0xa3, (byte) 0xc2, (byte) 0x9c, (byte) 0xce };
  private static final byte[] urpsha = new byte[] { (byte) 0xd5, 0x69, 0x76, (byte) 0xfa, (byte) 0xd1, 0x34, 0x23,
      (byte) 0xff, (byte) 0xb2, 0x67, 0x39, (byte) 0xf5, 0x0b, (byte) 0xe2, (byte) 0xdc, (byte) 0xbb, (byte) 0xc5,
      (byte) 0xf6, (byte) 0x90, (byte) 0x9d, (byte) 0xbc, 0x16, (byte) 0xc7, (byte) 0xcc, 0x2e, (byte) 0xdb, 0x69, 0x24,
      (byte) 0xee, (byte) 0xa0, (byte) 0x97, 0x53 };

  static boolean assertNull(Object o) {
    if (o != null) {
      System.err.println("Bad status");
      return false;
    }
    return true;
  }
  
  static boolean assertArrayEquals(byte[] b1, byte[] b2) {
    if (b1 == null || b2 == null) {
      System.err.println("Contents null");
      return false;
    }
    if (!Arrays.equals(b1, b2)) {
      System.err.println("Contents do not match");
      return false;
    }
    return true;
  }
  
  static void pass(boolean pass) {
    if (pass) {
      System.out.println("Pass");
    } else {
      System.err.println("Fail");
    }
  }
  
  /**
   * Test basic download of single file
   */
  static void testBasic() throws Exception {
    System.out.print("Basic download: ");
    try (Socket sock = new Socket(server, port);
        OutputStream out = sock.getOutputStream();
        InputStream in = sock.getInputStream()) {
      out.write((files.get("yurp") + "\n").getBytes(CHARENC));
      boolean rv = assertNull(getStatus(in));
      File f = File.createTempFile("TestBasic", ".dl");
      download(in, f, 0);
      rv = assertArrayEquals(yurpsha, sha256(f));
      // System.out.println(new BigInteger(1, sha256(f)).toString(16));
      f.delete();
      pass(rv);
    }
  }

  static void testSlow() throws Exception {
    System.out.print("Slow request: ");
    try (Socket sock = createConnection();
        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream()) {
      slowSend(out, "yurp");
      boolean rv = assertNull(getStatus(in));
      File f = File.createTempFile("TestSlow", ".dl");
      download(in, f, 0);
      rv = assertArrayEquals(yurpsha, sha256(f));
      // System.out.println(new BigInteger(1, sha256(f)).toString(16));
      f.delete();
      pass(rv);
    }
  }

  protected static final int NOTHREADS = 3;

  static void testMulti() throws Exception {
    System.out.print("Multiple, simultaneous download: ");
    List<Unit> units = new ArrayList<>();
    for (int i = 0; i < NOTHREADS; i++) {
      Unit u = new Unit();
      u.s = createConnection();
      u.f = File.createTempFile("MixTest", ".dl");
      u.t = makeMixThread(u.s, "urp", u.f);
      units.add(u);
    }
    for (Unit u : units) {
      u.t.start();
    }
    for (Unit u : units) {
      try {
        u.t.join();
        u.s.close();
        pass(assertArrayEquals(urpsha, sha256(u.f)));
        // System.out.println(new BigInteger(1, sha256(f)).toString(16));
        u.f.delete();
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  static class Unit {
    public Thread t;
    public Socket s;
    public File f;
  }

  private static Thread makeMixThread(final Socket sock, final String filename, final File downloadFile) {
    return new Thread(() -> {
      try (InputStream in = sock.getInputStream(); OutputStream out = sock.getOutputStream()) {
        out.write((files.get(filename) + "\n").getBytes(CHARENC));
        assertNull(getStatus(in));
        download(in, downloadFile, 1);
      } catch (Exception e) {
        System.err.println("Something bad happened: " + e.getMessage());
      }
    });
  }

  private static void slowSend(OutputStream out, String file) throws IOException, InterruptedException {
    byte[] send = (files.get(file) + "\n").getBytes(CHARENC);
    for (int i = 0; i < send.length; i++) {
      out.write(send[i]);
      Thread.sleep(5);
    }
  }

  private static Random rNo = new Random();

  private static byte[] genID() {
    byte[] id = new byte[15];
    rNo.nextBytes(id);
    return id;
  }

  static String bytes2String(byte[] bytes) {
    String rv = "";
    for (byte b : bytes) {
      rv += String.format("%02X", b);
    }
    return rv;
  }

  protected static byte[] sha256(File f) throws NoSuchAlgorithmException, IOException {
    try (DigestInputStream dis = new DigestInputStream(new FileInputStream(f), MessageDigest.getInstance("SHA-256"));
        OutputStream out = OutputStream.nullOutputStream()) {
      dis.transferTo(out);
      return dis.getMessageDigest().digest();
    }
  }

  /**
   * Get next token by fetching characters up to and including any delimiter or
   * EoS
   */
  protected static String getToken(InputStream in, char[] delims) throws IOException {
    StringBuilder token = new StringBuilder();
    int rv;
    while ((rv = in.read()) != -1) {
      token.append((char) rv);
      for (char c : delims) {
        if ((char) rv == c) {
          return token.toString();
        }
      }
    }
    return token.toString();
  }

  /**
   * Get the status
   * 
   * @param in byte input source
   * @return null if proper ok and error message if proper error
   * @throws Exception if problem
   */
  protected static String getStatus(InputStream in) throws IOException {
    String status = getToken(in, new char[] { ' ', '\n' });
    if ("OK\n".equals(status)) {
      getToken(in, new char[] { '\n' }); // Kill extra \n
      return null;
    } else if ("ERROR ".equals(status)) {
      return getToken(in, new char[] {}); // Return error message
    } else {
      throw new RuntimeException("Bad status from download server");
    }
  }

  /**
   * Download file
   * 
   * @param in           byte input source
   * @param downloadFile download file
   * @param sleep        milliseconds to sleep between reads (<=0 if no sleep)
   * @throws Exception if problem
   */
  protected static void download(InputStream in, File downloadFile, int sleep) throws Exception {
    try (OutputStream out = new FileOutputStream(downloadFile)) {
      byte[] buf = new byte[1024];
      int rv;
      while ((rv = in.read(buf)) != -1) {
        // System.out.println(Thread.currentThread() + " read " + rv + " bytes");
        out.write(buf, 0, rv);
        if (sleep > 0) {
          Thread.sleep(sleep);
        }
      }
    }
  }
}
