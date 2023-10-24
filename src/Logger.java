import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  

public class Logger {
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
    private String filePath;
    private int peerID;

    public Logger(int peerID) {
        this.peerID = peerID;
        
        this.filePath = "../logs/log_peer_" + String.valueOf(peerID) + ".log";
    }

    // File I/O adapted from w3schools
    // https://www.w3schools.com/java/java_files_create.asp
    private void writeLog(String message) {
        try {
            FileWriter logger = new FileWriter(this.filePath);
            LocalDateTime now = LocalDateTime.now();  

            logger.write(dtf.format(now) + ": Peer " + peerID + " " + message);

            logger.close();
            System.out.println("Successfully logged to " + filePath);
            
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void logTcpConnectionTo(int neighborID) {
        String message = "makes a connection to Peer " + neighborID + ".";
        writeLog(message);
    }

    public void logTcpConnectionFrom(int neighborID) {
        String message = "is connected from Peer " + neighborID + ".";
        writeLog(message);
    }

    public void logChangePreferredNeighbors(int[] preferredNeighbors) {

        StringBuilder stringBuilder = new StringBuilder();

        // Get string representation of preferred neighbors
        for (int i = 0; i < preferredNeighbors.length; i++) {
            stringBuilder.append(preferredNeighbors[i]);

            // Add a comma separator if not the last element
            if (i < preferredNeighbors.length - 1) {
                stringBuilder.append(", ");
            }
        }
        String neighborsStr = stringBuilder.toString();
        
        String message = "has the preferred neighbors " + neighborsStr;
        writeLog(message);
    }

    public void logChangeOptimisticallyUnchocked(int neighborID) {
        String message = "has the optimistically unchoked neighbor [" + neighborID + "].";
        writeLog(message);
    }

    public void logUnchokedBy(int neighborID) {
        String message = "is unchoked by [" + neighborID + "].";
        writeLog(message);
    }

    public void logChokedBy(int neighborID) {
        String message = "is choked by [" + neighborID + "].";
        writeLog(message);
    }

    public void logReceivedHave(int neighborID, int pieceIndex) {
        String message = "received the 'have' message from" + neighborID + "for the piece " + pieceIndex + ".";
        writeLog(message);
    }

    public void logReceivedInterested(int neighborID) {
        String message = "received the 'interested' message from " + neighborID + ".";
        writeLog(message);
    }

    public void logReceivedNotInterested(int neighborID) {
        String message = "received the ‘not interested’ message from " + neighborID + ".";
        writeLog(message);
    }

    public void logDownloadedPiece(int neighborID, int pieceIndex, int numPieces) {
        String message = "has downloaded the piece " +  pieceIndex + "from " + neighborID + ". Now the number of pieces it has is " + numPieces + ".";
        writeLog(message);
    }

    public void logDownloadCompleted() {
        String message = "has downloaded the complete file.";
        writeLog(message);
    }
}
