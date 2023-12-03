//Contains project-wide helper methods

import java.util.ArrayList;
import java.util.BitSet;
import java.io.*;
import java.nio.file.*;

public class Helper {

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

    //Returns a list of bits that bitfield B has that bitfield A doesn't
    public static int[] detectNewBits(BitSet a, BitSet b){
        ArrayList<Integer> tempList = new ArrayList<>();
        for(int i=0; i<b.length(); i++){
            if(a.get(i)==false && b.get(i)==true){
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
    public static byte[] readPieceFromFile(BitSet bitfield, int pieceIndex, int selfPeerID){
        byte[] pieceData = new byte[ConfigHandler.commonVars.pieceSize];
        int startByteIndex = 0;

        // Find spot in file to read
        // Increment start byte index by all previous pieces
        for(int i=0; i<pieceIndex; i++){
            if(bitfield.get(i)==true){
                startByteIndex += ConfigHandler.commonVars.pieceSize;
            }
        }
        //TODO: CHANGE PATH FOR FINAL VERSION
        try {
            File peerFile = new File("../project_config_file_small/" + selfPeerID + "/thefile");
            byte[] fileContent = fileToByteArray(peerFile);
            // Iteratively get bytes until get entire piece or reach end of file
            for(int i=startByteIndex; i < (ConfigHandler.commonVars.pieceSize + startByteIndex) && i<fileContent.length; i++){
                pieceData[i-startByteIndex] = fileContent[i]; // Get byte from file and insert into pieceData (starting from index 0)
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return pieceData;
    }

    //Writes the given piececontent to the given pieceIndex in the file
    public static void writePieceToFile(BitSet bitfield, int pieceIndex, int selfPeerID, byte[] pieceContent){
        //Check if last piece, if so use size of last piece instead
        int startByteIndex = 0;
        for(int i=0; i<pieceIndex; i++){
            if(bitfield.get(i)==true){
                startByteIndex += ConfigHandler.commonVars.pieceSize;
            }
        }
        try {
            Files.createDirectories(Paths.get("../project_config_file_small/" + selfPeerID));
            File peerFile = new File("../project_config_file_small/" + selfPeerID + "/thefile");
            peerFile.createNewFile();
            int contentLength = pieceContent.length;
            if(pieceIndex == ConfigHandler.commonVars.numPieces-1){
                contentLength = ConfigHandler.commonVars.sizeOfLastPiece;
            }
            byte[] fileContent = fileToByteArray(peerFile);
            byte[] newFileContent = new byte[fileContent.length+contentLength];
            for(int i=0; i<startByteIndex; i++){
                newFileContent[i] = fileContent[i];
            }
            for(int i=0; i<contentLength; i++){
                newFileContent[i+startByteIndex] = pieceContent[i];
            }
            for(int i=startByteIndex+contentLength; i<newFileContent.length; i++){
                newFileContent[i] = fileContent[i-contentLength];
            }
            Files.write(Paths.get("../project_config_file_small/" + selfPeerID + "/thefile"), newFileContent);

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
