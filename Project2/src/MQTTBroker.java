package Project2.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.Socket;

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

    // list of active client : clientSocket - clientID
    private Map<Socket, String> sessions = new HashMap<>();
    private Map<String, List<Socket>> topicSubscriptions = new HashMap<>();
    //private List<String> sessions =  new ArrayList<String>();
    public MQTTBroker() {

    }

    public byte[] processMessage(byte[] data, Socket clientSocket) {
        byte[] message = null;
        int messageType = (data[0] >> 4) & 0x0F;
        
        switch (messageType) {
            case 1: // connect
                message = processConnect(data, clientSocket);
                break;
            case 12: // pingreq
                message = processPingResp(data);
                break;
            case 8: // subscribe
                message = processSubscribe(data, clientSocket);
                break;
            case 10: // unsubscribe
                message = processUnsubscribe(data, clientSocket);
                break;
            // case 14: // disconnect
            //     processDisconnect(data);
            default:
                System.out.println("Unknown message type: " + messageType);
                break;
        }

        return message;
    }

    public byte[] processConnect(byte[] data, Socket clienSocket) {
        System.out.println("\nProcessing CONNECT message");

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
        sessions.put(clienSocket, clientID);
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

    public byte[] processPingResp(byte[] data) {
        System.out.println("\nProcessing PINGREQ message");

        byte[] pingResp = new byte[2];
        pingResp[0] = (byte) 0xD0; // PINGRESQ
        pingResp[1] = (byte) 0x00; // remaining length

        System.out.println("Sending PINGRESP");
        return pingResp;
    }

    public byte[] processSubscribe(byte[] data, Socket clientSocket) {
        System.out.println("\nProcessing SUBSCRIBE message");

        // header
        int remainingLength = 0;
        int multiplier = 1;
        int index = 1; // Après le premier byte (Header)
        byte encodedByte;

        do {
            encodedByte = data[index++];
            remainingLength += (encodedByte & 127) * multiplier;
            multiplier *= 128;
        } while ((encodedByte & 128) != 0);

        System.out.println("Remaining Length: " + remainingLength);

        // packet identifier (2 bytes)
        int packetId = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;

        System.out.println("Packet identifier: " + packetId);

        // property length
        int propertiesLength = 0;
        multiplier = 1;

        do {
            encodedByte = data[index++];
            propertiesLength += (encodedByte & 127) * multiplier;
            multiplier *= 128;
        } while ((encodedByte & 128) != 0);

        index += propertiesLength;

        // payload : topic filter
        int nbTopics = 0;
        while (index < data.length) { //while(index != remainingLength) {
            // topic length
            int topicLength = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
            index += 2;

            // topic name
            String topicName = new String(data, index, topicLength);
            index += topicLength;

            // subscription options
            int options = data[index++];
            nbTopics++;
            System.out.println("Topic: " + topicName + ", QoS: " + options);

            // add the subscriptions
            // check if the topic exist, otherwise add the topic and create a list to it and add the clientSocket
            topicSubscriptions.computeIfAbsent(topicName, k -> new ArrayList<>()).add(clientSocket);

            if (index >= 2 + remainingLength) {
                break;
            }
        }
        System.out.println("Updated topic subscriptions after Subscribe: " + topicSubscriptions.toString() );
        
        // send the response
        return sendSubAck(packetId, nbTopics);
    }

    public byte[] sendSubAck(int packetId, int nbTopics) {
        int len = 2 + 1 + nbTopics; // packecId + propertiesLength + returnCodes length
        byte[] subAck = new byte[2 + len];

        subAck[0] = (byte) 0x90; // SUBACK
        subAck[1] = (byte) len; // remaining length

        // packet identifier
        subAck[2] = (byte) ((packetId >> 8) & 0xFF);
        subAck[3] = (byte) (packetId & 0xFF);
        
        subAck[4] = (byte) 0x00; // Properties Length (0 for simplicity)

        int index = 5;
        for(int i = 0; i < nbTopics; i++) subAck[index++] = 0x00; // QoS 0 for each topics

        return subAck;
    }

    public byte[] processUnsubscribe(byte[] data, Socket clientSocket) {
        System.out.println("\nProcessing UNSUBSCRIBE message");

        // header
        int remainingLength = 0;
        int multiplier = 1;
        int index = 1; // Après le premier byte (Header)
        byte encodedByte;

        do {
            encodedByte = data[index++];
            remainingLength += (encodedByte & 127) * multiplier;
            multiplier *= 128;
        } while ((encodedByte & 128) != 0);

        System.out.println("Remaining Length: " + remainingLength);

        // packet identifier (2 bytes)
        int packetId = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;

        System.out.println("Packet identifier: " + packetId);

        // property length
        int propertiesLength = 0;
        multiplier = 1;

        do {
            encodedByte = data[index++];
            propertiesLength += (encodedByte & 127) * multiplier;
            multiplier *= 128;
        } while ((encodedByte & 128) != 0);

        index += propertiesLength;

        // payload : topic filter
        int nbTopics = 0;
        while (index < data.length) {
            // topic length
            int topicLength = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
            index += 2;

            // topic name
            String topicName = new String(data, index, topicLength);
            index += topicLength;

            System.out.println("Topic: " + topicName);

            // remove the subscriptions
            topicSubscriptions.computeIfPresent(topicName, (key, subscribers) -> {
                subscribers.remove(clientSocket); // Remove the specific client
                // Remove the topic if no clients are left because a null value remove the key from the map
                return subscribers.isEmpty() ? null : subscribers; 
            });
            nbTopics++;

            if (index >= 2 + remainingLength) {
                break;
            }
        }
        System.out.println("Updated topic subscriptions after Unsubscribe: " + topicSubscriptions.toString() );

        // send the response
        return sendUnsubAck(packetId, nbTopics);
    }

    public byte[] sendUnsubAck(int packetId, int nbTopics) {
        int len = 2 + 1 + nbTopics; // packecId + propertiesLength + returnCodes length
        byte[] unsubAck = new byte[2 + len];

        unsubAck[0] = (byte) 0xB0; // SUBACK
        unsubAck[1] = (byte) len; // remaining length

        // packet identifier
        unsubAck[2] = (byte) ((packetId >> 8) & 0xFF);
        unsubAck[3] = (byte) (packetId & 0xFF);
        
        unsubAck[4] = (byte) 0x00; // Properties Length (0 for simplicity)

        int index = 5;
        for(int i = 0; i < nbTopics; i++) unsubAck[index++] = 0x00; // QoS 0 for each topics

        return unsubAck;
    }

    // public void processDisconnect(byte[] data) {
    //     System.out.println("\nProcessing DISCONNECT message");

    //     int remainingLength = data[1];
    //     if (remainingLength != 0) {
    //         System.out.println("Invalid DISCONNECT message: Remaining Length is not 0");
    //         return;
    //     }

    //     // Désinscription logique du client
    //     // Note : Vous pourriez recevoir l'identifiant du client à partir de la session en cours
    //     System.out.println("Client requested to disconnect");
    // }
}
