
import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

class peerProcess {
    public static void main(String[] args) {
        // Load Common.cfg
        ConfigHandler.updateCommon();

        // Load PeerInfo.cfg
        PeerInfoHandler.updatePeerInfo();

        // peer can take the peer info map/list in
        // If provided argument is present in peer info map, initialize a peer
        int peerID = Integer.valueOf(args[0]);
        if (PeerInfoHandler.getPeerInfoMap().containsKey(peerID)) {
            Peer peer = new Peer(peerID);
        } else {
            System.out.println("Peer ID not present in configuration!");
        }
    }
}