import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigHandler {
    public static class commonVars {
        public static int numberOfPreferredNeighbors;
        public static int unchokingInterval;
        public static int optimisticUnchokingInterval;
        public static String fileName;
        public static int fileSize;
        public static int pieceSize;
        public static int numPieces;
        public static int bitfieldSize;
        public static int sizeOfLastPiece;
    }

    //Updates variables based on Common.cfg
    public static void updateCommon() {
        // File IO code adapted from w3schools
        // https://www.w3schools.com/java/java_files_read.asp
        try {
            //TODO: CHANGE PATH HERE!!!
            File common = new File("../project_config_file_small/Common.cfg");
            Scanner myReader = new Scanner(common);

            commonVars.numberOfPreferredNeighbors = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.unchokingInterval = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.optimisticUnchokingInterval = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.fileName = Helper.parseStringFieldFromLine(myReader.nextLine());
            commonVars.fileSize = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.pieceSize = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));

            commonVars.numPieces = (int)Math.ceil((double)commonVars.fileSize/commonVars.pieceSize);
            commonVars.bitfieldSize = commonVars.numPieces + (8-commonVars.numPieces%8); //numPieces rounded up to the nearest multiple of 8 (in bits)
            commonVars.sizeOfLastPiece = commonVars.fileSize % commonVars.pieceSize;

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
