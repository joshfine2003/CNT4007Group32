import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PeerInfoHandler {
    public static class PeerInfoVars {
        String hostname;
        int port;
        boolean hasFile;

        public PeerInfoVars(String hostname, int port, boolean hasFile) {
            this.hostname = hostname;
            this.port = port;
            this.hasFile = hasFile;
        }
    }

    private static Map<Integer, PeerInfoVars> peerInfoMap = new LinkedHashMap<>();

    // Getter
    public static Map<Integer, PeerInfoVars> getPeerInfoMap() {
        return peerInfoMap;
    }

    // Updates variables based on PeerInfo.cfg
    public static void updatePeerInfo() {
        // File IO code adapted from w3schools
        // https://www.w3schools.com/java/java_files_read.asp
        try {
            String filename = Peer.rootPath + "PeerInfo.cfg";
            File peerInfo = new File(filename);
            Scanner myReader = new Scanner(peerInfo);

            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                // System.out.println(line);

                // Split line into fields with +s deliminater
                String[] fields = line.split(" ");

                if (fields.length == 4) {
                    try {
                        // Create a data structure with peer info
                        // No error handling if fields don't match format

                        int tempPeerId = Integer.parseInt(fields[0]);
                        String tempHostName = fields[1];
                        int tempListeningPort = Integer.parseInt(fields[2]);
                        boolean tempHasFile = !"0".equals(fields[3]);
                        
                        // Create a new data object with hostname, port number, hasFile
                        PeerInfoVars tempPeerInfoVars = new PeerInfoVars(tempHostName, tempListeningPort, tempHasFile);

                        peerInfoMap.put(tempPeerId, tempPeerInfoVars);

                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Missing a field!");
                        myReader.close();
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
