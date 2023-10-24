import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Arrays;
import java.util.Scanner; // Import the Scanner class to read text files

public class Peer {
    // ignoring encapsulation for now
    int peerID;
    String hostName;
    int listeningPort;
    boolean hasFile;

    Boolean[] bitfield;

    Server server;
    Client client;

    private void startServer(int listeningPort) {
        try {
            server = new Server(listeningPort, peerID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Client startClient(int port) {
        try {
            client = new Client(port, hostName, peerID);
            return client;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a class constructor for the Main class
    public Peer(int peerID, String hostName, int listeningPort, boolean hasFile) {
        this.peerID = peerID;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;

        this.bitfield = new Boolean[ConfigHandler.commonVars.pieceSize];
        if (hasFile) {
            Arrays.fill(bitfield, Boolean.TRUE);
        } else {
            Arrays.fill(bitfield, Boolean.FALSE);
        }

        // Start both server and client
        new Thread(() -> startServer(listeningPort)).start();
        // Make connections with all peers that started before it
        // How do I check for this? Use peerIDs maybe (hardcoded?)
        // Handshake handshakeMsg = new Handshake(this.peerID);
        for (int i = this.peerID - 1; i > 1000; i--) {
            final int peerIdCopy = i + 6000;
            new Thread(() -> startClient(peerIdCopy)).start();
            // send handshake, bitfield to other peers
            // need public method within client to be able to send messages
            // pass bitfield into Client? then
        }

    }

    // Overriding toString() method of String class
    @Override
    public String toString() {
        // Create a new array containing the specified range of elements
        Boolean[] partialBitField = Arrays.copyOfRange(bitfield, 0, 5);

        return "\tPeer ID: " + this.peerID +
                "\n\tHost Name: " + this.hostName +
                "\n\tListening Port: " + this.listeningPort +
                "\n\tHas File: " + this.hasFile +
                "\n\tNumber Of Preferred Neighbors: " + ConfigHandler.commonVars.numberOfPreferredNeighbors +
                "\n\tUnchoking Interval: " + ConfigHandler.commonVars.unchokingInterval +
                "\n\tOptimistic Unchoking Interval: " + ConfigHandler.commonVars.optimisticUnchokingInterval +
                "\n\tFile Name: " + ConfigHandler.commonVars.fileName +
                "\n\tFile Size: " + ConfigHandler.commonVars.fileSize +
                "\n\tPiece Size: " + ConfigHandler.commonVars.pieceSize +
                "\n\tBit Field (partial): " + Arrays.toString(partialBitField);
    }
}
