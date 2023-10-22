import java.util.Arrays;
public class Message {

    byte[] message;
    byte type;
    int length;
    byte[] payload;

    //Constructor for byte array
    public Message(byte[] _message){
        setMessage(_message);
    }

    //Empty type constructor
    public Message(byte type){
        message = new byte[5];
        message[4] = type;
        update();
    }

    //Changes message var and updates all variables appropriately
    public void setMessage(byte[] _message){
        byte[] lengthBits = {_message[0], _message[1], _message[2], _message[3]};
        int messageLength = 5 + byteArrayToInt(lengthBits);
        message = new byte[messageLength];
        for(int i=0; i<messageLength; i++){
            message[i] = _message[i];
        }
        update();
    }

    //Calls all update functions in the correct order
    private void update(){
        updateType();
        updateLength();
        updatePayload();
    }

    //Updates type var based on message
    private void updateType(){
        type = message[4];
    }

    //Updates length var based on message
    private void updateLength(){
        byte[] len = {message[0], message[1], message[2], message[3]};
        length = byteArrayToInt(len);
    }

    //Updates payload var based on message
    private void updatePayload(){
        payload = new byte[length];
        for(int i=0; i<length; i++){
            payload[i] = message[i+5];
        }
    }
    
    //Returns the byte array representing the message
    public byte[] getBytes(){
        return message;
    }

    //Returns 0-7 representing message type
    public byte getType(){
        return type;
    }
    
    //Returns int message length
    public int getLength(){
        return length;
    }

    //Returns byte[] message payload
    public byte[] getPayload(){
        return payload;
    }

    //Converts a 4 length byte array into an int
    public static int byteArrayToInt(byte[] bytes){
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }

    //Convert an int into a 4 length byte array
    public static byte[] intToByteArray(int num){
        byte[] result = new byte[4];
        result[0] = (byte)(num >> 24);
        result[1] = (byte)(num >> 16);
        result[2] = (byte)(num >> 8);
        result[3] = (byte)num;
        return result;
    }

    //toString method for debug
    @Override
    public String toString() {
        String[] types = {"choke", "unchoke", "interested", "not interested", "have", "bitfield", "request", "piece"};
        String result = "{\nLength: " + length + "\nType: " + types[type] + "\nPayload: " + Arrays.toString(payload) + "\n}";
        return result;
    }
}