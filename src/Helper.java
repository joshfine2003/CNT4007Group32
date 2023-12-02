//Contains project-wide helper methods

import java.util.ArrayList;
import java.util.BitSet;

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
}
