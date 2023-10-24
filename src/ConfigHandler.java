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
    }

    //Updates variables based on Common.cfg
    public static void updateCommon() {
        // File IO code adapted from w3schools
        // https://www.w3schools.com/java/java_files_read.asp
        try {
            File common = new File("../project_config_file_small/Common.cfg");
            Scanner myReader = new Scanner(common);

            commonVars.numberOfPreferredNeighbors = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.unchokingInterval = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.optimisticUnchokingInterval = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.fileName = Helper.parseStringFieldFromLine(myReader.nextLine());
            commonVars.fileSize = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));
            commonVars.pieceSize = Integer.parseInt(Helper.parseStringFieldFromLine(myReader.nextLine()));

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
