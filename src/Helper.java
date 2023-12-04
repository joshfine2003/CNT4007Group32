//Contains project-wide helper methods

import java.util.ArrayList;
import java.util.BitSet;
import java.io.*;
import java.nio.file.*;

public class Helper {

    public static boolean fileBeingUsed = false;

    // Used to parse scanner input
    public static String parseStringFieldFromLine(String line) {
        String[] fields = line.split(" ");
        return fields[1];
    }

    // Converts a 4 length byte array into an int
    public static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }

    // Convert an int into a 4 length byte array
    public static byte[] intToByteArray(int num) {
        byte[] result = new byte[4];
        result[0] = (byte) (num >> 24);
        result[1] = (byte) (num >> 16);
        result[2] = (byte) (num >> 8);
        result[3] = (byte) num;
        return result;
    }

    //Convert bitset to byte[] of proper length for bitfield
    public static byte[] bitsetToByteArray(BitSet b){
        byte[] claimedBytes = b.toByteArray();
        byte[] realBytes = new byte[ConfigHandler.commonVars.bitfieldSize/8];
        for(int i=0; i<claimedBytes.length; i++){
            realBytes[i] = claimedBytes[i];
        }
        return realBytes;
    }

    //Returns a list of bits that bitfield B has that bitfield A doesn't (accounts for outgoing requests that haven't timed out)
    public static int[] detectNewBits(BitSet a, BitSet b){
        ArrayList<Integer> tempList = new ArrayList<>();
        for(int i=0; i<b.length(); i++){
            if(a.get(i)==false && b.get(i)==true){
                if(!Peer.lastRequestMap.containsKey(i) || Peer.lastRequestMap.get(i)+Peer.requestTimeout <= System.currentTimeMillis())
                    tempList.add(i);
            }
        }
        int[] result = tempList.stream().mapToInt(i -> i).toArray();
        return result;
    }

    // Delete code adapted from w3schools
    // https://www.w3schools.com/java/java_files_delete.asp
    // Delete log file
    public static void deleteFile(String fileName) {
        try {
            File myObj = new File(fileName);
            if (myObj.exists()) {
                myObj.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Get byte array from file
    public static byte[] fileToByteArray(File f){
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] result = new byte[(int)f.length()];
            fis.read(result);
            fis.close();
            return result;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static boolean bitfieldIsCompleted(BitSet b){
        for(int i=0; i<ConfigHandler.commonVars.numPieces; i++){
            if(b.get(i) == false){
                return false;
            }
        }
        return true;
    }

    //Reads the given pieceIndex from the file and returns the piece content
    public static synchronized byte[] readPieceFromFile(int pieceIndex, int selfPeerID){
        while(fileBeingUsed){
            try {
                Thread.sleep(10);
            } catch (Exception e){
                System.out.println(e);
            }
        }
        fileBeingUsed = true;
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
            byte[] fileContent = fileToByteArray(peerFile);
            // Iteratively get bytes until get entire piece or reach end of file
            for(int i=startByteIndex; i < (ConfigHandler.commonVars.pieceSize + startByteIndex) && i<fileContent.length; i++){
                pieceData[i-startByteIndex] = fileContent[i]; // Get byte from file and insert into pieceData (starting from index 0)
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        fileBeingUsed = false;
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

    //Writes the given piece content to the given pieceIndex in the file
    public static synchronized boolean writePieceToFile(int pieceIndex, int selfPeerID, byte[] pieceContent){
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
        while(fileBeingUsed){
            try {
                Thread.sleep(10);
            } catch (Exception e){
                System.out.println(e);
            }
        }
        if (!Peer.bitfieldMap.get(selfPeerID).get(pieceIndex)){
            fileBeingUsed = true;

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
                byte[] fileContent = fileToByteArray(peerFile);
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
            fileBeingUsed = false;
            return true;
        } else {
            return false;
        }
    }
}
