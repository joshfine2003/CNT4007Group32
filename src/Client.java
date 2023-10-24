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
        }
        catch (ConnectException e) {
            e.printStackTrace();
            System.err.println("Connection refused. You need to initiate a server first.");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
