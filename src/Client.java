import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
    public class SocketTriple{
        public ObjectInputStream in;
        public ObjectOutputStream out;
        public Socket socket;
        public SocketTriple(ObjectInputStream in_, ObjectOutputStream out_, Socket socket_){
            in = in_;
            out = out_;
            socket = socket_;
        }
    }
    Map<Integer, SocketTriple> sockets = new HashMap<Integer, SocketTriple>();
    public void ConnectToServer(int serverPeerId, int port, int clientPeerId){
        try{
            System.out.println("Client " + clientPeerId + " trying to connect to port " + port);
            Socket socket = new Socket("localhost", port);
            System.out.println("Connected to server " + serverPeerId + " on port " + port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            SocketTriple trip = new SocketTriple(in, out, socket);
            sockets.put(serverPeerId, trip);
            sendHandshake(new Handshake(clientPeerId), serverPeerId);
            boolean loop = true;
            while(loop){
                Handshake handshake = new Handshake((byte[])in.readObject());
                if(handshake.verify(serverPeerId)){
                    loop=false;
                }
            }
            System.out.println("Verified Handshake From Sever " + serverPeerId);
            while (true) {
                // receive a message sent from the server
                Message message = new Message((byte[])in.readObject());
                // show the message to the user
                System.out.println("Received message from server " + serverPeerId + "\n" + message + "\n");
            }
        }
        catch (ConnectException e) {
            e.printStackTrace();
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch(ClassNotFoundException classNotFound){
            System.err.println("Class Type is Not Found");
        }catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void sendHandshake(Handshake hsk, int peerId){
        try {
            ObjectOutputStream out = sockets.get(peerId).out;
            out.writeObject(hsk.getBytes());
            out.flush();
            System.out.println("Sent Handshake to Server " + peerId);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void sendMessage(Message msg, int peerId) {
        try {
            ObjectOutputStream out = sockets.get(peerId).out;
            out.writeObject(msg.getBytes());
            out.flush();
            System.out.println("Sent Message to Server " + peerId);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
