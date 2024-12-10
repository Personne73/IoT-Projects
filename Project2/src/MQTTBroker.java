package Project2.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In the second programming project you will create an MQTT broker program that can broker MQTT messages. 
 * The program should be able to handle subscribe and unsubscribe requests, as well as handle clients that 
 * publish information to the subscribed topics. Hence, you will need to create a program that can correctly 
 * recieve connections from MQTT clients and then interpret the basic MQTT messages.

    Remember that you must code everything from scratch. No existing MQTT library is allowed to used. Build it yourself.
    Your MQTT client need to minimally support the following commands (mqtt version 5.0)

    Connect
    The program should be able to receive and handle clients that want to connect

    Subscribe
    The program should be able to receive and handle client requests for subscribing to different topics.

    Unsubscribe
    The program should be able to receive and handle client request for unsubscribing to previously subscribed topics.

    Publish
    The program should be able to receive and handle clients that want to publish messages to certain topics, including retain functionality.

    MQTT Ping
    The program should be able to receive and handle clients that sends MQTT Ping requests

    Disconnect
    The program should be able to receive and handle clients that wants to gracefully disconnect. 
    As well as recover when someone ungracefully disconnects.
 */
public class MQTTBroker {

    // list of active client : clientID - clientSocket
    //private Map<String, Socket> sessions = new HashMap<>();
    private List<String> sessions =  new ArrayList<String>();
    public MQTTBroker() {

    }

    public byte[] processMessage(byte[] data) {
        byte[] message = null;
        int messageType = (data[0] >> 4) & 0x0F;
        
        switch (messageType) {
            case 1: // connect
                message = processConnect(data);
                break;
        
            default:
                System.out.println("Unknown message type: " + messageType);
                break;
        }

        return message;
    }

    public byte[] processConnect(byte[] data) {
        System.out.println("Processing CONNECT message");

        // header
        //int remainingLength = data[1];
        int remainingLength = 0;
        int multiplier = 1;
        int index = 1; // Après le premier byte (Header)
        byte encodedByte;

        do {
            encodedByte = data[index++];
            remainingLength += (encodedByte & 127) * multiplier;
            multiplier *= 128;
        } while ((encodedByte & 128) != 0);
        //int index = 2;

        System.out.println("Remaining Length: " + remainingLength);

        // protocol name
        int protocolNameLength = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        String protocolName = new String(data, index, protocolNameLength);
        index += protocolNameLength;

        System.out.println("Protocol id length : " + protocolNameLength + "\nProtocol id name : " + protocolName);

        if(!protocolName.equalsIgnoreCase("MQTT")) {
            System.out.println("###### Invalid protocol name:" + protocolName);
            return sendConAck(1); // 0x01 Connection rejected for unsupported protocol version
        }

        // protocol version
        int protocolVersion = data[index++];
        if(protocolVersion != 5) {
            System.out.println("###### Invalid protocol version:" + protocolVersion);
            return sendConAck(1);
        }

        // flag
        int connectFlags = data[index++];

        // keep alive
        int keepAlive = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        System.out.println("Keep Alive: " + keepAlive);

        // properties
        int propertiesLength = 0;
        multiplier = 1;

        do {
            encodedByte = data[index++];
            propertiesLength += (encodedByte & 127) * multiplier;
            multiplier *= 128;
        } while ((encodedByte & 128) != 0);

        index += propertiesLength;

        // payload : client ID length and client ID
        int clientIDLength = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        String clientID = new String(data, index, clientIDLength);
        index += clientIDLength;

        if(index != remainingLength) { // because 2 bytes for the header
            System.out.println("###### Mismatch in Remaining Length. Expected: " + remainingLength + ", Parsed: " + (index));
            return sendConAck(2);
        }

        // store the client id
        sessions.add(clientID);
        System.out.println("Client successfully connected: " + clientID);

        // send the response
        return sendConAck(0);
    }

    public byte[] sendConAck(int returnCode) {
        byte[] connAck = new byte[5];

        connAck[0] = (byte) 0x20; // CONNACK
        connAck[1] = (byte) 3; // 3 remaining bytes
        connAck[2] = (byte) 0; // no flags present
        connAck[3] = (byte) returnCode; // return code
        connAck[4] = (byte) 0; // no properties

        return connAck;
    }
}
