public class MessageHandler {
    public static void handle(Message message, Peer self, int peerID){
        byte[] payload = message.getPayload();
        switch(message.getType()){
            case 0: //Choke
                Logger.logChokedBy(self.peerID, peerID);
                break;
            case 1: //Unchoke
                Logger.logUnchokedBy(self.peerID, peerID);
                break;
            case 2: //Interested
                Logger.logReceivedInterested(self.peerID, peerID);
                break;
            case 3: //Not Interested
                Logger.logReceivedNotInterested(self.peerID, peerID);
                break;
            case 4: //Have
                byte[] indexH = {payload[0], payload[1], payload[2], payload[3]};
                int pieceIndexH = Helper.byteArrayToInt(indexH);
                Logger.logReceivedHave(self.peerID, peerID, pieceIndexH);
                break;
            case 5: //Bitfield

                break;
            case 6: //Request

                break;
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
                break;
            default:
                System.out.println("Error: Invalid type.");
        }
    }
}
