
import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.nio.file.*; // Used for creating directories

class peerProcess {
    public static void main(String[] args) {
        // Load Common.cfg
        ConfigHandler.updateCommon();

        // Load PeerInfo.cfg
        PeerInfoHandler.updatePeerInfo();

        // If provided argument is present in peer info map, initialize a peer
        int peerID = Integer.valueOf(args[0]);

        // Create a log directory if it doesn't exist
        new File("peer_" + peerID).mkdirs();

        if (PeerInfoHandler.getPeerInfoMap().containsKey(peerID)) {
            Logger.deleteLog(peerID); // Delete the log

            // Check if the peer should have the file
            // If not, delete the file :))
            if (!PeerInfoHandler.getPeerInfoMap().get(peerID).hasFile) {
                String fileToDelete = Peer.rootPath + Integer.toString(peerID) + "/" + ConfigHandler.commonVars.fileName;
                Helper.deleteFile(fileToDelete);
            }

            Peer peer = new Peer(peerID); // Start the peer
        } else {
            System.out.println("Peer ID not present in configuration!");
        }
    }
}