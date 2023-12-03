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
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.nio.file.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Peer {
    // ignoring encapsulation for now
    int peerID;
    String hostName;
    int listeningPort;
    public static boolean hasFile;

    public static int optimisticNeighbor = 0;

    public static ConcurrentLinkedQueue<List<byte[]>> IOQueue = new ConcurrentLinkedQueue<>();

    Logger logger;

    public static String rootPath = "../project_config_file_smaller/";

    // public static List<PeerConnection> peerConnections;
    public static Map<Integer, PeerConnection> peerConnections = new ConcurrentHashMap<>(); // attempt to make thread //
                                                                                            // safe
    public static Map<Integer, BitSet> bitfieldMap = new ConcurrentHashMap<>(); // Keeps track of known bitmaps of other
                                                                                // peers
    public static Map<Integer, Boolean> isInterestedMap = new ConcurrentHashMap<>(); // Keeps track of what peers
                                                                                     // are interested in this peer
    public static Map<Integer, Boolean> chokedMap = new ConcurrentHashMap<>(); // Keeps track of what peers are choked
    public static Map<Integer, Integer> downloadMap = new ConcurrentHashMap<>();
    public static Map<Integer, Long> lastRequestMap = new ConcurrentHashMap<>(); // Keeps track of last time a file was
                                                                                 // requested
    public static Map<Integer, Boolean> completedDownloadMap = new ConcurrentHashMap<>(); // Keeps track of peers with
                                                                                          // completed downloads

    public static long requestTimeout = 4000; // Request timeout in milliseconds

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
                    Logger.logTcpConnectionFrom(this.peerID, expectedPeer);
                } else {
                    System.out.println("Handshake failed! Expected a handshake from peerID " + expectedPeer);
                }

                PeerConnection peerConnection = new PeerConnection(connection, out, in, this.peerID, expectedPeer);

                downloadMap.put(expectedPeer, 0);
                peerConnections.put(expectedPeer, peerConnection);
                chokedMap.put(expectedPeer, true); // Mark all neighbors initially as choked
                isInterestedMap.put(expectedPeer, false); // Mark all neighbors initially as uninterested
                completedDownloadMap.put(expectedPeer, false); // Mark all neighbors initially as not completed download

                // Mark neighbor initially as having no bitfield
                BitSet tempBitSet = new BitSet(ConfigHandler.commonVars.bitfieldSize);
                tempBitSet.clear(0, ConfigHandler.commonVars.numPieces);
                bitfieldMap.put(expectedPeer, tempBitSet);

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
                    isInterestedMap.put(keyPeerID, true); // Mark all neighbors initially as uninterested
                    completedDownloadMap.put(keyPeerID, false); // Mark all neighbors initially as not completed
                                                                // download

                    // Mark neighbor initially as having no bitfield
                    BitSet tempBitSet = new BitSet(ConfigHandler.commonVars.bitfieldSize);
                    tempBitSet.clear(0, ConfigHandler.commonVars.numPieces);
                    bitfieldMap.put(keyPeerID, tempBitSet);

                    // Send handshake
                    Handshake sendHandshake = new Handshake(this.peerID);
                    Message handshakeMessage = new Message(sendHandshake.getBytes(), true);
                    Logger.logTcpConnectionTo(this.peerID, keyPeerID);
                    peerConnections.get(keyPeerID).sendMessage(handshakeMessage);
                    downloadMap.put(keyPeerID, 0);
                    tempPeerConnection.startThreads();

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop() {
        long lastMessageTime = System.currentTimeMillis();
        long lastMessageTime0 = System.currentTimeMillis();

        long messageInterval = ConfigHandler.commonVars.unchokingInterval * 1000; // Multiply by 1000 for ms--> s
        long messageInterval0 = ConfigHandler.commonVars.unchokingInterval * 1000;

        while (true) {
            long currentTime0 = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();

            if(IOQueue.size() >= 1){
                int type = Helper.byteArrayToInt(IOQueue.peek().get(0));
                if(type == 0){
                    readPieceFromFile(Helper.byteArrayToInt(IOQueue.peek().get(1)), Helper.byteArrayToInt(IOQueue.peek().get(2)));
                    IOQueue.remove();
                }
                else if(type == 1){
                    writePieceToFile(Helper.byteArrayToInt(IOQueue.peek().get(1)), Helper.byteArrayToInt(IOQueue.peek().get(2)), IOQueue.peek().get(3));
                    IOQueue.remove();
                }
            }
            // If all peers have completed downloads, shut it all down
            boolean allDone = true;
            for (int i : completedDownloadMap.keySet()) {
                if (!completedDownloadMap.get(i)) { // If any peers are not completed downloading, keep going
                    allDone = false;
                }
            }
            if (allDone) {
                // and peer happens
                for (int i : peerConnections.keySet()) {
                    // peerConnections.get(i).close();
                }
            }

            // Optimistic Unchoking
            if (currentTime0 - lastMessageTime0 >= messageInterval0) {
                List<Integer> chokedInterested = new ArrayList<>();
                for (int i : chokedMap.keySet()) {
                    // Find all interested peers that are choked
                    if (chokedMap.get(i) && isInterestedMap.get(i)) {
                        chokedInterested.add(i);
                    }
                }
                // Pick a random peer in choked and interested to unchoke
                if (chokedInterested.size() >= 1) {
                    int randNum = ThreadLocalRandom.current().nextInt(0, chokedInterested.size());
                    optimisticNeighbor = chokedInterested.get(randNum);
                    chokedMap.put(optimisticNeighbor, false);
                    peerConnections.get(optimisticNeighbor).sendMessage(new Message((byte) 1));
                    Logger.logChangeOptimisticallyUnchoked(peerID, optimisticNeighbor);
                    lastMessageTime0 = currentTime0;
                }
            }

            // If unchoking interval has passed, iterate through interested peers
            if (currentTime - lastMessageTime >= messageInterval) {
                // If it has the whole file, randomly determine preferred neighbors
                if (hasFile && isInterestedMap.size() >= 1) {
                    List<Integer> interestedNeighbors = new ArrayList<>();
                    Set<Integer> prefNeighbors = new HashSet<>();
                    for (Integer i : isInterestedMap.keySet()) {
                        if (isInterestedMap.get(i)) {
                            interestedNeighbors.add(i);
                        }
                    }
                    while (prefNeighbors.size() < ConfigHandler.commonVars.numberOfPreferredNeighbors) {
                        if (interestedNeighbors.size() > 0) {
                            int randNum = ThreadLocalRandom.current().nextInt(0, interestedNeighbors.size());
                            prefNeighbors.add(interestedNeighbors.get(randNum));
                        }
                    }
                    Integer[] log = prefNeighbors.toArray(new Integer[0]);
                    Logger.logChangePreferredNeighbors(peerID, log);
                    for (int i : prefNeighbors) {
                        if (chokedMap.get(i)) {
                            chokedMap.put(i, false);
                            peerConnections.get(i).sendMessage(new Message((byte) 1));
                        }
                    }
                    for (int j : chokedMap.keySet()) {
                        if (!chokedMap.get(j)) {
                            boolean safe = false;
                            for (int k : prefNeighbors) {
                                if (j == k || j == optimisticNeighbor) {
                                    safe = true;
                                }
                            }
                            if (!safe) {
                                chokedMap.put(j, true);
                                peerConnections.get(j).sendMessage(new Message((byte) 0));
                            }
                        }
                    }
                    // Else, use download rate to determine pref neighbors
                } else if (isInterestedMap.size() >= 1) {
                    List<Integer> prefNeighbors = new ArrayList<>();
                    List<Integer> randomKeys = new ArrayList<>(downloadMap.keySet());
                    Collections.shuffle(randomKeys);
                    Map<Integer, Integer> downloadMapRand = new ConcurrentHashMap<>();
                    for (int i = 0; i < randomKeys.size(); i++) {
                        downloadMapRand.put(randomKeys.get(i), downloadMap.get(randomKeys.get(i)));
                    }
                    // Find the preferred top neighbors
                    for (int i = 0; i < ConfigHandler.commonVars.numberOfPreferredNeighbors; i++) {
                        int currKey = Collections.max(downloadMapRand.entrySet(), Map.Entry.comparingByValue())
                                .getKey();
                        downloadMapRand.put(currKey, 0);
                        prefNeighbors.add(currKey);
                    }
                    Integer[] log = prefNeighbors.toArray(new Integer[0]);
                    Logger.logChangePreferredNeighbors(peerID, log);
                    // Flush the download map
                    for (int i : downloadMap.keySet()) {
                        downloadMap.put(i, 0);
                    }
                    // If preferred neighbors are choked, send an unchoke message
                    for (int i = 0; i < prefNeighbors.size(); i++) {
                        if (chokedMap.get(prefNeighbors.get(i))) {
                            chokedMap.put(prefNeighbors.get(i), false);
                            peerConnections.get(prefNeighbors.get(i)).sendMessage(new Message((byte) 1));
                        }
                    }
                    for (int j : chokedMap.keySet()) {
                        if (!chokedMap.get(j)) {
                            boolean safe = false;
                            for (int k = 0; k < prefNeighbors.size(); k++) {
                                if (j == prefNeighbors.get(k) || j == optimisticNeighbor) {
                                    safe = true;
                                }
                            }
                            if (!safe) {
                                chokedMap.put(j, true);
                                peerConnections.get(j).sendMessage(new Message((byte) 0));
                            }
                        }
                    }
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
        hasFile = peerInfo.hasFile;

        int bitSetSize = ConfigHandler.commonVars.bitfieldSize;
        BitSet tempBitSet = new BitSet(bitSetSize);
        if (hasFile) {
            tempBitSet.set(0, ConfigHandler.commonVars.numPieces);
        } else {
            tempBitSet.clear(0, ConfigHandler.commonVars.numPieces);
        }
        bitfieldMap.put(this.peerID, tempBitSet);

        completedDownloadMap.put(this.peerID, hasFile); // Mark peer as completed or not depending on if it has a file

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

    public static byte[] readPieceFromFile(int pieceIndex, int selfPeerID){
        /*while(fileBeingUsed){
            try {
                Thread.sleep(10);
            } catch (Exception e){
                System.out.println(e);
            }
        }
        fileBeingUsed = true;*/
        BitSet bitfield = Peer.bitfieldMap.get(selfPeerID);
        Logger.logStartedReading(selfPeerID, pieceIndex);

        // Initialize piece data of size config piece size
        byte[] pieceData = new byte[ConfigHandler.commonVars.pieceSize];
        int startByteIndex = 0;

        // Find spot in file to read
        // Increment start byte index by all previous pieces
        for(int i=0; i<pieceIndex; i++){
            if(bitfield.get(i)==true){
                startByteIndex += ConfigHandler.commonVars.pieceSize;
            }
        }
        try {
            File peerFile = new File(Peer.rootPath + selfPeerID + "/" + ConfigHandler.commonVars.fileName);
            // Convert file to a byte array
            byte[] fileContent = Helper.fileToByteArray(peerFile);
            // Iteratively get bytes until get entire piece or reach end of file
            for(int i=startByteIndex; i < (ConfigHandler.commonVars.pieceSize + startByteIndex) && i<fileContent.length; i++){
                pieceData[i-startByteIndex] = fileContent[i]; // Get byte from file and insert into pieceData (starting from index 0)
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        //fileBeingUsed = false;
        Logger.logStoppedReading(selfPeerID, pieceIndex);
        boolean nullData = true; // DELETE LATER
        for(int i=0; i<pieceData.length; i++){
            if(pieceData[i] != 0){
                nullData = false;
            }
        }
        if(nullData){
            System.out.println("Read all null data in piece " + pieceIndex);
        }
        return pieceData;
    }

    public static void writePieceToFile(int pieceIndex, int selfPeerID, byte[] pieceContent){
        //Check if last piece, if so use size of last piece instead
        boolean nullData = true;
        for(int i=0; i<pieceContent.length; i++){
            if(pieceContent[i] != 0){
                nullData = false;
            }
        }
        if(nullData){
            System.out.println("Piece content is null for writing piece " + pieceIndex);
        }
        /*while(fileBeingUsed){
            try {
                Thread.sleep(10);
            } catch (Exception e){
                System.out.println(e);
            }
        }
        fileBeingUsed = true;*/
        BitSet bitfield = Peer.bitfieldMap.get(selfPeerID);
        Peer.bitfieldMap.get(selfPeerID).set(pieceIndex, true);

        Logger.logStartedWriting(selfPeerID, pieceIndex);
        int startByteIndex = 0; // Start point initialized to 0
        for(int i=0; i<pieceIndex; i++){ // Find start point (for each prior piece, move start cursor up to piece size)
            if(bitfield.get(i)==true){
                startByteIndex += ConfigHandler.commonVars.pieceSize;
            }
        }
        try {
            // Create directory if needed
            Files.createDirectories(Paths.get(Peer.rootPath + selfPeerID));
            // Open file (create if needed)
            File peerFile = new File(Peer.rootPath + selfPeerID + "/" + ConfigHandler.commonVars.fileName);
            peerFile.createNewFile();

            // Content length should be the length of piece content
            // Unless it's the last piece (should be size of last piece)
            int contentLength = pieceContent.length;
            if(pieceIndex == ConfigHandler.commonVars.numPieces-1){
                contentLength = ConfigHandler.commonVars.sizeOfLastPiece;
            }

            // Convert file to byte array
            byte[] fileContent = Helper.fileToByteArray(peerFile);
            // New file content is loaded into a new byte array, with length of file + new piece
            byte[] newFileContent = new byte[fileContent.length+contentLength];

            // For the bytes before the start of the new piece, fill newFileContent with existing file content
            for(int i=0; i<startByteIndex; i++){
                newFileContent[i] = fileContent[i];
            }
            // Fill in next section of file then start loading the new content in
            for(int i=0; i<contentLength; i++){
                newFileContent[i+startByteIndex] = pieceContent[i];
            }
            // Fill the remainder with file content
            for(int i=startByteIndex+contentLength; i<newFileContent.length; i++){
                newFileContent[i] = fileContent[i-contentLength];
            }
            // Write byte array to file
            Files.write(Paths.get(Peer.rootPath + selfPeerID + "/" + ConfigHandler.commonVars.fileName), newFileContent);

        } catch (Exception e) {
            System.out.println(e);
        }
        Logger.logStoppedWriting(selfPeerID, pieceIndex);
        //fileBeingUsed = false;
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
