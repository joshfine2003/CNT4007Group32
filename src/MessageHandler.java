public class MessageHandler {
    public static byte[] handle(byte[] messageBytes, Peer self, int peerID){
        Message message = new Message(messageBytes);
        byte[] payload = message.getPayload();
        switch(message.getType()){
            case 0: //Choke
                Logger.logChokedBy(self.peerID, peerID);
                return handleChoke(peerID);

            case 1: //Unchoke
                Logger.logUnchokedBy(self.peerID, peerID);
                return handleUnchoke(peerID);

            case 2: //Interested
                Logger.logReceivedInterested(self.peerID, peerID);
                return handleInterested(peerID);

            case 3: //Not Interested
                Logger.logReceivedNotInterested(self.peerID, peerID);
                return handleNotInterested(peerID);

            case 4: //Have
                byte[] indexH = {payload[0], payload[1], payload[2], payload[3]};
                int pieceIndexH = Helper.byteArrayToInt(indexH);
                Logger.logReceivedHave(self.peerID, peerID, pieceIndexH);
                return handleHave(self, pieceIndexH);

            case 5: //Bitfield
                return handleBitfield(peerID);

            case 6: //Request
                return handleRequest(peerID);

            case 7: //Piece
                byte[] indexP = {payload[0], payload[1], payload[2], payload[3]};
                int pieceIndexP = Helper.byteArrayToInt(indexP);
                int pieceCount = 0;
                for(boolean hasPiece : self.bitfield){
                    if(hasPiece){
                        pieceCount++;
                    }
                }
                Logger.logDownloadedPiece(self.peerID, peerID, pieceIndexP, pieceCount);
                if(pieceCount == Math.ceil((double)ConfigHandler.commonVars.fileSize/ConfigHandler.commonVars.pieceSize)){
                    Logger.logDownloadCompleted(self.peerID);
                }
                return handlePiece(peerID);

            default:
                System.out.println("Error: Invalid type.");
                return null;
        }
    }

    //Handles cases where a choke message was received
    private static byte[] handleChoke(int neighbor){
        return null;
    }

    //Handles cases where an unchoke message was received
    private static byte[] handleUnchoke(int neighbor){
        byte[] result = new byte[9];
        result[3] = 4;
        result[4] = 6;
        //Fill payload with wanted piece index
        return result;
    }

    //Handles cases where an interested message was received
    private static byte[] handleInterested(int neighbor){
        //Mark peer as interested
        return null;
    }

    //Handles cases where a not interested message was received
    private static byte[] handleNotInterested(int neighbor){
        //Mark peer as not interested
        return null;
    }

    //Handles cases where a have message was received
    private static byte[] handleHave(Peer self, int pieceIndex){
        byte[] result;
        if(self.bitfield[pieceIndex] == false){
            //Sends an interested message
            result = (new Message((byte)2)).getBytes();
        }else{
            //Sends a not interested message
            result = (new Message((byte)3)).getBytes();
        }
        return result;
    }

    //Handles cases where a bitfield message was received
    private static byte[] handleBitfield(int neighbor){
        //Bitfields are handled in peer logic
        return null;
    }

    //Handles cases where a request message was received
    private static byte[] handleRequest(int neighbor){
        //This should reply with the actual piece, not an empty piece message
        return (new Message((byte)7)).getBytes();
    }

    //Handles cases where a piece message was received
    private static byte[] handlePiece(int neighbor){
        //This should reply with a new request for the next unowned piece, not an empty request message
        return (new Message((byte)6)).getBytes();
    }
}
