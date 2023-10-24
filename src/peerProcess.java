
import java.io.File; // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

class peerProcess {
    public static void main(String[] args) {
        //Load Common.cfg
        ConfigHandler.updateCommon();

        // // Load PeerInfo.cfg
        // PeerInfo.updatePeerInfo();

        // File IO code adapted from w3schools
        // https://www.w3schools.com/java/java_files_read.asp
        try {
            File peerInfo = new File("../project_config_file_small/PeerInfo.cfg");
            Scanner myReader = new Scanner(peerInfo);

            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                // System.out.println(line);

                String[] fields = line.split(" ");

                if (fields.length == 4) {
                    try {
                        // If peerID matches the argument
                        if (fields[0].equals(args[0])) {
                            // Instantiate a peer with the fields
                            // No error handling if fields don't match format
                            boolean tempHasFile;
                            if (fields[3].equals("0")) {
                                tempHasFile = false;
                            } else {
                                tempHasFile = true;
                            }
                            Peer peer = new Peer(Integer.parseInt(fields[0]), fields[1], Integer.parseInt(fields[2]),
                                    tempHasFile); // True if 1 else false ternary operator
                            System.out.println(peer);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Missing peer ID argument!");
                        myReader.close(); // To avoid vscode warning
                        throw e;
                    }
                } else {
                    System.out.println("Less than four fields in peer info file!");
                }
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}