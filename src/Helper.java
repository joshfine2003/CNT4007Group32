//Contains project-wide helper methods
public class Helper {

    //Used to parse scanner input
    public static String parseStringFieldFromLine(String line) {
        String[] fields = line.split(" ");
        return fields[1];
    }

    //Converts a 4 length byte array into an int
    public static int byteArrayToInt(byte[] bytes){
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }

    //Convert an int into a 4 length byte array
    public static byte[] intToByteArray(int num){
        byte[] result = new byte[4];
        result[0] = (byte)(num >> 24);
        result[1] = (byte)(num >> 16);
        result[2] = (byte)(num >> 8);
        result[3] = (byte)num;
        return result;
    }
}
