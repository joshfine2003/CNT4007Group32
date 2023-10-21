import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
    Socket requestSocket; // socket connect to the server
    ObjectOutputStream out; // stream write to the socket
    ObjectInputStream in; // stream read from the socket
    String message; // message send to the server
    String MESSAGE; // capitalized message read from the server

    // template field? string for now
    // specific func for handler, bitfield
    // then close connections

    // send a message to the output stream
    void sendMessage(String msg) {
        try {
            // stream write the message
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public Client(int port, String hostname, String message) {
        try {
            // create a socket to connect to the server
            System.out.println("Client trying to connect to " + port);
            requestSocket = new Socket("localhost", port);
            System.out.println("Connected to localhost in port " + port);

            // initialize inputStream and outputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            // Send the sentence to the server
            sendMessage(message);
            // Receive the upperCase sentence from the server
            MESSAGE = (String) in.readObject();
            // show the message to the user
            System.out.println("Receive message: " + MESSAGE);
        } catch (ConnectException e) {
            e.printStackTrace();
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            // Close connections
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
