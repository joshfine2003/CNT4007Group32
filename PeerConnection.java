import java.io.*;
import java.net.*;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PeerConnection {
    private Socket socket;
    private Integer selfPeerID;
    private Integer linkedPeerID;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BlockingQueue<Message> messageSendQueue = new LinkedBlockingQueue<>(); // Fill queue with messages that
                                                                                   // should
    // be sent.

    // Maybe another queue for meesages that need to be read? then make public and
    // so that peer can read from them and act

    public PeerConnection(Socket socket, Integer selfPeerID, Integer linkedPeerID) throws IOException {
        this.socket = socket;
        this.selfPeerID = selfPeerID;
        this.linkedPeerID = linkedPeerID;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public PeerConnection(Socket socket, ObjectOutputStream out, ObjectInputStream in, Integer selfPeerID,
            Integer linkedPeerID) throws IOException {
        this.socket = socket;
        this.selfPeerID = selfPeerID;
        this.linkedPeerID = linkedPeerID;
        this.out = out;
        this.in = in;
    }

    // Synchronized so only one thread should be able to execute at once
    public void sendMessage(Message message) {
        messageSendQueue.add(message);
    }

    // Synchronized so only one thread should be able to execute at once
    public synchronized Message receiveMessage() throws IOException, ClassNotFoundException {
        return new Message((byte[]) in.readObject());
    }

    public void sendBitField() {
        // this is called after handshake established
        // next step is to send bitfield (if peer ID's bitfield is not empty)
        BitSet selfBitField = Peer.bitfieldMap.get(this.selfPeerID);
        if (!selfBitField.isEmpty()) {
            byte[] bitfieldMessage = Helper.bitsetToByteArray(selfBitField);
            sendMessage(new Message(bitfieldMessage.length, (byte) 5, bitfieldMessage)); // Send Bitfield
        }
    }

    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startThreads() {
        // Thread for receiving messages
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        Message receivedMessage = receiveMessage();

                        // Handle the received message as needed
                        byte[] replyMessage = MessageHandler.handle(receivedMessage.getBytes(), selfPeerID, linkedPeerID);
                        
                        //System.out.println("Received message: " + receivedMessage);

                        if(receivedMessage.type == 7){
                            for(Integer i : Peer.peerConnections.keySet()){
                                byte[] havePayload = {receivedMessage.payload[0],receivedMessage.payload[1],receivedMessage.payload[2],receivedMessage.payload[3]};
                                Peer.peerConnections.get(i).sendMessage(new Message(4,(byte)4,havePayload));

                                // Check neighbor bitfields and send not interested
                                if(Helper.detectNewBits(Peer.bitfieldMap.get(selfPeerID), Peer.bitfieldMap.get(i)).length == 0){
                                    Peer.peerConnections.get(i).sendMessage(new Message((byte)3));
                                }
                            }
                        }
                        
                        if (replyMessage != null) {
                            sendMessage(new Message(replyMessage));
                        }
                    } catch (Exception e) {
                        //Something Went Wrong :(
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                // Handle exceptions or exit the thread
                e.printStackTrace();
            }
        });

        // Thread for sending messages
        Thread sendThread = new Thread(() -> {
            try {
                while (true) {
                    // Dequeue and send messages
                    try {
                    Message messageToSend = messageSendQueue.take();
                    out.writeObject(messageToSend.getBytes());
                    out.flush();
                    } catch (Exception e) {
                         //Something Went Wrong :(
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                // Handle exceptions or exit the thread
                e.printStackTrace();
            }
        });
        sendBitField();

        // Start the threads
        receiveThread.start();
        sendThread.start();
    }
}
