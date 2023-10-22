import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {
    ServerSocket listener = null;;

    public Server(int sPort) throws Exception {
        System.out.println("The server is running on port " + sPort + ".");
        listener = new ServerSocket(sPort);
        int clientNum = 1;
        try {
            while (true) {
                new Handler(listener.accept(), clientNum).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private Socket connection;
        private ObjectInputStream in; // stream read from the socket
        private ObjectOutputStream out; // stream write to the socket
        private int no; // The index number of the client

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try {
                // initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {
                    
                    while (true) {
                        // receive the message sent from the client
                        Message message = new Message((byte[])in.readObject());
                        // show the message to the user
                        System.out.println("Received message from client " + no + "\n" + message + "\n");
                        // respond with a have message (with payload)
                        byte[] fake_reply = {0, 0, 0, 3, 4, 43, 122, 15};
                        Message reply = new Message(fake_reply);
                        sendMessage(reply);
                    }
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            } finally {
                // Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        // send a message to the output stream
        public void sendMessage(Message msg) {
            try {
                out.writeObject(msg.getBytes());
                out.flush();
                System.out.println("Sent message to client " + no + "\n" + msg + "\n");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }

}
