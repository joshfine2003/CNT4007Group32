public class Handshake {
    byte[] bytes;
    String header = "P2PFILESHARINGPROJ";
    byte[] zeroBits = new byte[10];
    int peerID;

    //Constructor for byte array input
    public Handshake(byte[] _bytes){
        bytes = new byte[32];
        for(int i=0; i<32; i++){
            bytes[i] = _bytes[i];
        }
        updatePeerID();
    }

    //Constructor for a specific peerID
    public Handshake(int _peerID) {
        peerID = _peerID;
        bytes = new byte[32];
        byte[] headerBytes = header.getBytes();
        for(int i=0; i<18; i++){
            bytes[i] = headerBytes[i];
        }
        byte[] idBytes = Helper.intToByteArray(peerID);
        for(int i=0; i<4; i++){
            bytes[i+28] = idBytes[i];
        }
    }

    //Updates the peerID based on the bytes
    private void updatePeerID(){
        byte[] idBytes = new byte[4];
        for(int i=0; i<4; i++){
            idBytes[i] = bytes[i+28];
        }
        peerID = Helper.byteArrayToInt(idBytes);
    }

    //Returns the bytes
    public byte[] getBytes() {
        return bytes;
    }

    //Returns the peerID
    public int getPeerID(){
        return peerID;
    }

    //Verifies if header bytes are correct and peerID is as expected
    public boolean verify(int expected) {
        byte[] headerBytes = header.getBytes();
        for(int i=0; i<18; i++){
            if(headerBytes[i] != bytes[i]){
                return false;
            }
        }
        return peerID == expected;
    }
}
