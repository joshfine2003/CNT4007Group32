import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PeerConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(); // Fill queue with messages that should
                                                                               // be sent.

    public PeerConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public PeerConnection(Socket socket, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    // Synchronized so only one thread should be able to execute at once
    public void sendMessage(Message message) {
        messageQueue.add(message);
    }

    // Synchronized so only one thread should be able to execute at once
    public synchronized Message receiveMessage() throws IOException, ClassNotFoundException {
        return new Message((byte[]) in.readObject());
    }

    public void close() {
        try {
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
                    // Check if there is data available to be read
                    if (in.available() > 0) {
                        Message receivedMessage = receiveMessage();
                        // Handle the received message as needed
                        System.out.println("Received message: " + receivedMessage);
                    } else {
                        // No data available, sleep for a short duration
                        Thread.sleep(100);
                    }
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                // Handle exceptions or exit the thread
                e.printStackTrace();
            }
        });

        // Thread for sending messages
        Thread sendThread = new Thread(() -> {
            try {
                while (true) {
                    // Dequeue and send messages
                    Message messageToSend = messageQueue.take();
                    sendMessage(messageToSend);
                }
            } catch (Exception e) {
                // Handle exceptions or exit the thread
                e.printStackTrace();
            }
        });

        // Start the threads
        receiveThread.start();
        sendThread.start();
    }
}
