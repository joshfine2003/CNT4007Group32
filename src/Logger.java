import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  

public class Logger {
    // Specify date time format for log files
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    // File I/O adapted from w3schools
    // https://www.w3schools.com/java/java_files_create.asp
    // Write message out to logfile
    private static void writeLog(int peer1, String message) {
        try {
            FileWriter logger = new FileWriter("../logs/log_peer_" + String.valueOf(peer1) + ".log", true); // Includes boolean to append instead of overwrite existing file
            LocalDateTime now = LocalDateTime.now();

            logger.write(dtf.format(now) + ": Peer " + peer1 + " " + message + "\n"); // glory to newlines
            logger.close();

            System.out.println("Successfully logged to ../logs/log_peer_" + String.valueOf(peer1) + ".log");
            
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Delete code adapted from w3schools
    // https://www.w3schools.com/java/java_files_delete.asp
    // Delete log file
    public static void deleteLog(int peer) {
        String fileToDelete = "../logs/log_peer_" + String.valueOf(peer) + ".log";
        Helper.deleteFile(fileToDelete);
    }

    // Log when peer makes a TCP connection to other peer
    public static void logTcpConnectionTo(int peerID, int neighborID) {
        String message = "makes a connection to Peer " + neighborID + ".";
        writeLog(peerID, message);
    }

    // Log when peer is connected from another peer
    public static void logTcpConnectionFrom(int peerID, int neighborID) {
        String message = "is connected from Peer " + neighborID + ".";
        writeLog(peerID, message);
    }

    // Log when peer changes its preferred neighbors
    public static void logChangePreferredNeighbors(int peerID, Integer[] preferredNeighbors) {

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
        writeLog(peerID, message);
    }

    // Log when peer changes its optimistically unchoked neighbor
    public static void logChangeOptimisticallyUnchoked(int peerID, int neighborID) {
        String message = "has the optimistically unchoked neighbor [" + neighborID + "].";
        writeLog(peerID, message);
    }

    // Log when peer is unchoked by a neighbor
    public static void logUnchokedBy(int peerID, int neighborID) {
        String message = "is unchoked by [" + neighborID + "].";
        writeLog(peerID, message);
    }

    // Log when peer is choked by a neighbor
    public static void logChokedBy(int peerID, int neighborID) {
        String message = "is choked by [" + neighborID + "].";
        writeLog(peerID, message);
    }

    // Log when peer receives a 'have' message
    public static void logReceivedHave(int peerID, int neighborID, int pieceIndex) {
        String message = "received the 'have' message from" + neighborID + "for the piece " + pieceIndex + ".";
        writeLog(peerID, message);
    }

    // Log when peer receives an 'interested' message
    public static void logReceivedInterested(int peerID, int neighborID) {
        String message = "received the 'interested' message from " + neighborID + ".";
        writeLog(peerID, message);
    }

    // Log when peer receives a 'not interested' message
    public static void logReceivedNotInterested(int peerID, int neighborID) {
        String message = "received the 'not interested' message from " + neighborID + ".";
        writeLog(peerID, message);
    }

    // Log when peer finishes downloading a piece
    public static void logDownloadedPiece(int peerID, int neighborID, int pieceIndex, int numPieces) {
        String message = "has downloaded the piece " +  pieceIndex + "from " + neighborID + ". Now the number of pieces it has is " + numPieces + ".";
        writeLog(peerID, message);
    }

    // Log when peer downloads complete file
    public static void logDownloadCompleted(int peerID) {
        String message = "has downloaded the complete file.";
        writeLog(peerID, message);
    }
}
