import java.util.BitSet;
import java.util.Random;

public class MessageHandler {
    public static byte[] handle(byte[] messageBytes, Integer selfPeerID, int neighborPeerID) {
        Message message = new Message(messageBytes);
        byte[] payload = message.getPayload();

        switch (message.getType()) {
            case 0: // Choke
                Logger.logChokedBy(selfPeerID, neighborPeerID);
                return handleChoke(neighborPeerID);

            case 1: // Unchoke
                Logger.logUnchokedBy(selfPeerID, neighborPeerID);
                return handleUnchoke(selfPeerID, neighborPeerID);

            case 2: // Interested
                Logger.logReceivedInterested(selfPeerID, neighborPeerID);
                return handleInterested(neighborPeerID);

            case 3: // Not Interested
                Logger.logReceivedNotInterested(selfPeerID, neighborPeerID);
                return handleNotInterested(neighborPeerID);

            case 4: // Have
                byte[] indexH = {payload[0], payload[1], payload[2], payload[3]};
                int pieceIndexH = Helper.byteArrayToInt(indexH);
                Logger.logReceivedHave(selfPeerID, neighborPeerID, pieceIndexH);
                return handleHave(selfPeerID, pieceIndexH);

            case 5: // Bitfield
                return handleBitfield(selfPeerID, neighborPeerID, payload);

            case 6: // Request
                byte[] pieceIndex = {payload[0], payload[1], payload[2], payload[3]};
                return handleRequest(selfPeerID, neighborPeerID, Helper.byteArrayToInt(pieceIndex));

            case 7: //Piece
                byte[] indexP = {payload[0], payload[1], payload[2], payload[3]};
                int pieceIndexP = Helper.byteArrayToInt(indexP);
                byte[] pieceContent = new byte[message.length-4];
                for(int i=0; i<pieceContent.length; i++){
                    pieceContent[i] = payload[i+4];
                }
                Helper.writePieceToFile(Peer.bitfieldMap.get(selfPeerID), pieceIndexP, selfPeerID, pieceContent);
                Peer.bitfieldMap.get(selfPeerID).set(pieceIndexP, true);
                int pieceCount = 0;
                for(int i=0; i<ConfigHandler.commonVars.numPieces; i++){
                    if(Peer.bitfieldMap.get(selfPeerID).get(i)){
                        pieceCount++;
                    }
                }
                Logger.logDownloadedPiece(selfPeerID, neighborPeerID, pieceIndexP, pieceCount);
                if(pieceCount == ConfigHandler.commonVars.numPieces){
                    Logger.logDownloadCompleted(selfPeerID);
                }
                return handlePiece(selfPeerID, neighborPeerID);

            default:
                System.out.println("Error: Invalid type.");
                return null;
        }
    }

    // Handles cases where a choke message was received
    private static byte[] handleChoke(int neighbor) {
        return null;
    }

    //Handles cases where an unchoke message was received (need to form a request message)
    private static byte[] handleUnchoke(int self, int neighbor){
        byte[] result = new byte[9];
        result[3] = 4; // Message length field
        result[4] = 6; // Message type (request)

        BitSet selfBitfield = Peer.bitfieldMap.get(self);
        BitSet neighborBitfield = Peer.bitfieldMap.get(neighbor);
        int[] newBits = Helper.detectNewBits(selfBitfield, neighborBitfield); // Get bit indices that the neighbor peer has which self doesn't have

        Random rand = new Random();
        // Randomly select from newBits and put index into message
        if(newBits.length > 0){
            int randomIndex = rand.nextInt(newBits.length);
            byte[] index = Helper.intToByteArray(newBits[randomIndex]);
            for(int i=5; i<9; i++){
                result[i] = index[i-5];
            }
            Peer.lastRequestMap.put(newBits[randomIndex], System.currentTimeMillis());
            return result;
        }else{
            return null; // No more new bits to get
        }
    }

    // Handles cases where an interested message was received
    private static byte[] handleInterested(int neighbor) {
        // Mark peer as interested
        Peer.isInterestedMap.put(neighbor, true);
        return null;
    }

    // Handles cases where a not interested message was received
    private static byte[] handleNotInterested(int neighbor) {
        // Mark peer as not interested
        Peer.isInterestedMap.put(neighbor, false);
        return null;
    }

    //Handles cases where a have message was received
    private static byte[] handleHave(int selfPeerID, int pieceIndex){
        byte[] result;
        if(Peer.bitfieldMap.get(selfPeerID).get(pieceIndex) == false){
            //Sends an interested message
            result = (new Message((byte)2)).getBytes();
        }else{
            //Sends a not interested message
            result = (new Message((byte)3)).getBytes();
        }
        return result;
    }

    // Handles cases where a bitfield message was received
    private static byte[] handleBitfield(int self, int neighbor, byte[] payload) {
        // Save neighbor's bitfield information to peer
        BitSet neighborBitfield = BitSet.valueOf(payload);
        Peer.bitfieldMap.put(neighbor, neighborBitfield);

        // Check if neighbor has a piece
        boolean hasNewPiece = false;
        for (int i = 0; i < ConfigHandler.commonVars.numPieces; i++) {
            if (Peer.bitfieldMap.get(self).get(i) == false && neighborBitfield.get(i) == true) {
                hasNewPiece = true;
                break;
            }
        }
        Message response;
        if (hasNewPiece) {
            response = new Message((byte) 2); // Interested
        } else {
            response = new Message((byte) 3); // Not interested
        }

        return response.getBytes();
    }

    // Handles cases where a request message was received
    private static byte[] handleRequest(int selfPeerID, int neighbor, int neededPiece) {
        Peer.downloadMap.put(neighbor, Peer.downloadMap.get(neighbor) + 1);
        byte[] pieceContent = Helper.readPieceFromFile(Peer.bitfieldMap.get(selfPeerID), neededPiece, selfPeerID);
        byte[] payload = new byte[4+pieceContent.length];
        byte[] pieceIndexBytes = Helper.intToByteArray(neededPiece);
        for(int i=0; i<4; i++){
            payload[i] = pieceIndexBytes[i];
        }
        for(int i=0; i<pieceContent.length; i++){
            payload[i+4] = pieceContent[i];
        }
        return (new Message(payload.length, (byte)7, payload)).getBytes();
    }

    // Handles cases where a piece message was received
    private static byte[] handlePiece(int selfPeerID, int neighborPeerID){
        byte[] result = new byte[9];
        result[3] = 4;
        result[4] = 6;
        int[] newBits = Helper.detectNewBits(Peer.bitfieldMap.get(selfPeerID), Peer.bitfieldMap.get(neighborPeerID));
        Random rand = new Random();
        if(newBits.length > 0){
            int randomIndex = rand.nextInt(newBits.length);
            byte[] index = Helper.intToByteArray(newBits[randomIndex]);
            for(int i=5; i<9; i++){
                result[i] = index[i-5];
            }
            Peer.lastRequestMap.put(newBits[randomIndex], System.currentTimeMillis());
            return result;
        }else{
            return null; // No more new bits to get
        }
    }
}
