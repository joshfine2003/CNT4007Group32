import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.Collections;

public class Peer {
    // ignoring encapsulation for now
    int peerID;
    String hostName;
    int listeningPort;
    boolean hasFile;
    
    public static int optimisticNeighbor = 0;

    Logger logger;

    // public static List<PeerConnection> peerConnections;
    public static Map<Integer, PeerConnection> peerConnections = new ConcurrentHashMap<>(); // attempt to make thread //
                                                                                            // safe

    public static Map<Integer, BitSet> bitfieldMap = new ConcurrentHashMap<>(); // Keeps track of known bitmaps of other
                                                                                // peers
    public static Map<Integer, Boolean> isInterestedMap = new ConcurrentHashMap<>(); // Keeps track of what peers
                                                                                     // are interested in this peer
    public static Map<Integer, Boolean> chokedMap = new ConcurrentHashMap<>(); // Keeps track of what peers are choked

    public static Map<Integer, Integer> downloadMap = new ConcurrentHashMap<>();

    /**
     * A handler thread class. Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private Socket connection;
        private int peerID;

        public Handler(Socket connection, int peerID) {
            this.connection = connection;
            this.peerID = peerID;
        }

        private Integer getNextExpectedPeer() {
            // Print out next expected incoming handshake
            Boolean reachedSelf = false; // Should have already handshaked with all previous peers
            for (Integer keyPeerID : PeerInfoHandler.getPeerInfoMap().keySet()) {
                if (reachedSelf) {
                    // Server expects the next handshake to come from the first peer ID where a
                    // connection doesn't exist
                    if (!peerConnections.containsKey(keyPeerID)) {
                        return keyPeerID;
                    }
                }

                if (keyPeerID == this.peerID) {
                    reachedSelf = true;
                }
            }

            return -1;
        }

        public void run() {
            // Create PeerConnection and start send/receive threads
            try {
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                // Check handshake header and if peerID is the expected one
                Handshake receivedHandshake = new Handshake((byte[]) in.readObject());
                int expectedPeer = getNextExpectedPeer();
                if (receivedHandshake.verify(expectedPeer)) {
                    System.out.println("Verified Handshake From Client " + (expectedPeer));
                    downloadMap.put(expectedPeer, 0);
                } else {
                    System.out.println("Handshake failed! Expected a handshake from peerID " + expectedPeer);
                }

                PeerConnection peerConnection = new PeerConnection(connection, out, in, this.peerID, expectedPeer);

                peerConnections.put(expectedPeer, peerConnection);
                chokedMap.put(expectedPeer, true); // Mark all neighbors initially as choked
                peerConnection.startThreads();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void listen(ServerSocket serverSocket) {
        try {
            while (true) {
                new Handler(serverSocket.accept(), this.peerID).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHandshakesToPreviousPeers() {
        try {
            // Iterate through peer info and send handshakes to previous peers
            for (Integer keyPeerID : PeerInfoHandler.getPeerInfoMap().keySet()) {
                if (keyPeerID == this.peerID) {
                    break;
                } else {
                    // Create socket for peer connection
                    PeerInfoHandler.PeerInfoVars linkedPeerInfo = PeerInfoHandler.getPeerInfoMap().get(keyPeerID);
                    Socket socket = new Socket(linkedPeerInfo.hostname, linkedPeerInfo.port);
                    PeerConnection tempPeerConnection = new PeerConnection(socket, this.peerID, keyPeerID);
                    peerConnections.put(keyPeerID, tempPeerConnection);
                    chokedMap.put(keyPeerID, true); // Mark all neighbors initially as choked

                    // Send handshake
                    Handshake sendHandshake = new Handshake(this.peerID);
                    Message handshakeMessage = new Message(sendHandshake.getBytes(), true);
                    peerConnections.get(keyPeerID).sendMessage(handshakeMessage);
                    tempPeerConnection.startThreads();

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop() {
        long lastMessageTime = System.currentTimeMillis();
        long messageInterval = ConfigHandler.commonVars.unchokingInterval * 1000; // Multiply by 1000 for ms--> s

        while (true) {
            long currentTime = System.currentTimeMillis();

            // UNCHOKING ALL FOR NOW
            // Later need to get download rate
            // If unchoking interval has passed, iterate through interested peers
            if (currentTime - lastMessageTime >= messageInterval) {
                    int currKey = Collections.max(downloadMap.entrySet(), Map.Entry.comparingByValue()).getKey();
                    downloadMap.put(currKey, 0);
                    
                }

                lastMessageTime = currentTime; // Update the last message time
            }
        }
    }

    // Create a class constructor for the Main class
    public Peer(int peerID) {
        // Initialize class variables with arguments and peer info map
        this.peerID = peerID;

        PeerInfoHandler.PeerInfoVars peerInfo = PeerInfoHandler.getPeerInfoMap().get(this.peerID);
        this.hostName = peerInfo.hostname;
        this.listeningPort = peerInfo.port;
        this.hasFile = peerInfo.hasFile;

        int bitSetSize = ConfigHandler.commonVars.bitfieldSize;
        BitSet tempBitSet = new BitSet(bitSetSize);
        if (hasFile) {
            tempBitSet.set(0, ConfigHandler.commonVars.numPieces);
        } else {
            tempBitSet.clear(0, ConfigHandler.commonVars.numPieces);
        }
        bitfieldMap.put(this.peerID, tempBitSet);

        try {
            // Start new server socket that listens on the correct port
            ServerSocket serverSocket = new ServerSocket(this.listeningPort);
            new Thread(() -> listen(serverSocket)).start(); // Thread constantly listens for new incoming new messages

            sendHandshakesToPreviousPeers();
            loop();

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Overriding toString() method of String class
    @Override
    public String toString() {
        // Create a new array containing the specified range of elements
        return "\tPeer ID: " + this.peerID +
                "\n\tHost Name: " + this.hostName +
                "\n\tListening Port: " + this.listeningPort +
                "\n\tHas File: " + this.hasFile +
                "\n\tNumber Of Preferred Neighbors: " + ConfigHandler.commonVars.numberOfPreferredNeighbors +
                "\n\tUnchoking Interval: " + ConfigHandler.commonVars.unchokingInterval +
                "\n\tOptimistic Unchoking Interval: " + ConfigHandler.commonVars.optimisticUnchokingInterval +
                "\n\tFile Name: " + ConfigHandler.commonVars.fileName +
                "\n\tFile Size: " + ConfigHandler.commonVars.fileSize +
                "\n\tPiece Size: " + ConfigHandler.commonVars.pieceSize;// +
        // "\n\tBit Field (partial): " + Arrays.toString(partialBitField);
    }
}
