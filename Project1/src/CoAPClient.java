package Project1.src;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * In the first programming project you will create a client program that can talk to an IoT server (for example a sensor) via CoAP. 
 * The program should be able to discover available information on the IoT server, retrieve data from the IoT server, 
 * and push data to the IoT server. Hence, you will need to create a program that can correctly connect to a CoAP server, 
 * to then send, recive, and interpret CoAP messages.
 * 
 * Your CoAP client need to minimally support the following commands, and then display the responses 
 * from the CoAP server just like a real CoAP client.
    POST
    The program should be able to send POST messages to send data to sensors/actuators with a short path names.

    PUT
    The program should be able to send PUT messages to send data to sensors/actuators with a short path names.

    GET
    The program should be able to send GET messages to retrieve data from sensors/actuators with a short path names. 
    (You don't need to implement block transfer)

    DELETE
    The program should be able to send DELETE messages to remove data from sensors/actuators with a short path names.
 */
public class CoAPClient extends Thread {
    
    private static final int COAP_PORT = 5683;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int messageId; // maximum 11111111 11111111 = 65 536

    public CoAPClient(){
        try {
            socket = new DatagramSocket(COAP_PORT);
            serverAddress = InetAddress.getByName(getName());
            messageId = new Random().nextInt(65536);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getNextMessageId() {
        messageId = (messageId + 1) % 65536;
        return messageId;
    }

    public byte[] createCoAPHeader(int code) {
        // & OxFF pour rendre non signé 1 byte
        // header + token = 1octect = 8bits = 1byte
        byte[] header = new byte[4]; // byte array
        // it's the first byte = 8 bits
        header[0] = 80; // Version 01, Type NON, Token Length = 0 --> 0101 0000
        header[1] = (byte) code; // Code sur 8bits = 1byte
        // Message ID = 16bits = 2byte
        int currentMessageId = getNextMessageId();
        header[2] = (byte) ((currentMessageId >> 8) & 0xFF);
        header[3] = (byte) (currentMessageId & 0xFF);

        return header;
    }
}
