import java.util.Arrays;

public class Handshake {
    static final String header = "P2PFILESHARINGPROJ";
    byte[] zeroBits = new byte[10]; // make 0 ugh
    int peerID;

    public Handshake(int peerID) {
        this.peerID = peerID;
        Arrays.fill(zeroBits, (byte) 0);
    }
}
