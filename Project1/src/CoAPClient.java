package Project1.src;

import java.net.DatagramPacket;
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
    private int prevDelta;

    public CoAPClient(){
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName("californium.eclipseprojects.io");
            //serverAddress = InetAddress.getByName("localhost");
            messageId = new Random().nextInt(65536);
            prevDelta = 0;
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
        int currentMessageId = messageId;
        header[2] = (byte) ((currentMessageId >> 8) & 0xFF);
        header[3] = (byte) (currentMessageId & 0xFF);
        messageId = getNextMessageId();

        return header;
    }

    public byte[] createCoAPOptions(int delta, String path) {
        int delta2 = delta - prevDelta;
        prevDelta = delta;
        byte[] optionValue = path.getBytes();
        int len = path.length();
        byte[] options = new byte[1 + len];

        options[0] = (byte) ((delta2 << 4) | len); // the delta and the length

        for(int i = 0; i < len; i ++) options[i+1] = optionValue[i]; // add the option value after the header

        return options;
    }

    public byte[] createCoAPPayload(String pl) {
        int len = pl.length();
        byte[] plBytes = pl.getBytes();

        byte[] payload = new byte[1 + len];
        payload[0] = -1; // because 255 is not available because byte are signed

        for(int i = 0; i < len; i ++) payload[i+1] = plBytes[i];

        return payload;
    }

    public byte[] createCoAPMessage(byte[] header, byte[] options, byte[] payload) {
        byte[] message = new byte[header.length + options.length + payload.length];

        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(options, 0, message, header.length, options.length);
        System.arraycopy(payload, 0, message, header.length + options.length, payload.length);

        return message;
    }

    public byte[] createGETRequest(String path) {
        byte[] header = createCoAPHeader(1);
        byte[] options = createCoAPOptions(11, path);

        return createCoAPMessage(header, options, new byte[0]);
    }

    public void sendMessage(byte[] message) {
        try {
            // sending of the message
            DatagramPacket packet = new DatagramPacket(message, message.length, serverAddress, COAP_PORT);
            socket.send(packet);

            // receiving of the message
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            //socket.receive(responsePacket);
            socket.setSoTimeout(2000); // Timeout de 2 secondes

            try {
                socket.receive(responsePacket);
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("No response from the server. It may not be running.");
                socket.close();
                return;
            }

            if (responsePacket.getLength() == 0) {
                System.out.println("Empty response received from the server.");
                socket.close();
                return;
            }

            // response
            String response = new String(responsePacket.getData(), 0, responsePacket.getLength()).trim();
            System.out.println("Response from the server - See detail in Wireshark:\n" + response);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // public void sendMessage(byte[] message) {
    //     try {
    //         // sending of the message
    //         DatagramPacket packet = new DatagramPacket(message, message.length, serverAddress, COAP_PORT);
    //         socket.send(packet);
    
    //         // receiving of the message
    //         byte[] buffer = new byte[1024];
    //         DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
    //         socket.setSoTimeout(2000); // Timeout de 2 secondes
    
    //         try {
    //             socket.receive(responsePacket);
    //         } catch (java.net.SocketTimeoutException e) {
    //             System.out.println("No response from the server. It may not be running.");
    //             socket.close();
    //             return;
    //         }
    
    //         if (responsePacket.getLength() == 0) {
    //             System.out.println("Empty response received from the server.");
    //             socket.close();
    //             return;
    //         }
    
    //         // response
    //         byte[] responseData = responsePacket.getData();
    //         int length = responsePacket.getLength();
    
    //         parseCoAPMessage(responseData, length);
    //         socket.close();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    
    private void parseCoAPMessage(byte[] data, int length) {
        // 1. En-tête
        int version = (data[0] >> 6) & 0x03;
        int type = (data[0] >> 4) & 0x03;
        int tokenLength = data[0] & 0x0F;
        int code = data[1] & 0xFF;
        int messageId = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
    
        System.out.println("=== CoAP Response ===");
        System.out.println("Version: " + version);
        System.out.println("Type: " + (type == 2 ? "Acknowledgement (ACK)" : "Non-Confirmable (NON)"));
        System.out.println("Code: " + code);
        System.out.println("Message ID: " + messageId);
    
        // 2. Token (si présent)
        int index = 4;
        if (tokenLength > 0) {
            byte[] token = new byte[tokenLength];
            System.arraycopy(data, index, token, 0, tokenLength);
            System.out.println("Token: " + bytesToHex(token));
            index += tokenLength;
        }
    
        // 3. Options et Payload
        boolean payloadMarkerFound = false;
        while (index < length) {
            if ((data[index] & 0xFF) == 0xFF) { // Marqueur de début du payload
                payloadMarkerFound = true;
                index++;
                break;
            }
    
            int delta = (data[index] >> 4) & 0x0F;
            int optionLength = data[index] & 0x0F;
            index++;
    
            // Option Delta et Longueur
            if (delta == 13) {
                delta += data[index++] & 0xFF;
            } else if (delta == 14) {
                delta += ((data[index++] & 0xFF) << 8) | (data[index++] & 0xFF);
            }
    
            if (optionLength == 13) {
                optionLength += data[index++] & 0xFF;
            } else if (optionLength == 14) {
                optionLength += ((data[index++] & 0xFF) << 8) | (data[index++] & 0xFF);
            }
    
            // Option Value
            String optionValue = new String(data, index, optionLength);
            index += optionLength;
    
            System.out.println("Option Delta: " + delta + ", Value: " + optionValue);
        }
    
        // 4. Payload (si présent)
        if (payloadMarkerFound && index < length) {
            String payload = new String(data, index, length - index);
            System.out.println("Payload: " + payload);
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }    

}