package Project2.src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;
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

    private Map<Socket, String> sessions = new HashMap<>(); // key : clientSocket, value : id
    private Map<String, List<Socket>> topicSubscriptions = new HashMap<>(); // key : topic name, value : list of clientSockets
    private Map<String, byte[]> retainMessages = new HashMap<>(); // key : topic name, value : msg
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
            case 3: // publish
                message = processPublish(data, clientSocket);
                break;
            case 14: // disconnect
                // int remainingLength = data[1];
                // if (remainingLength != 0) {
                //     System.out.println("Invalid DISCONNECT message: Remaining Length is not 0");
                //     break;
                // }
                processDisconnect(data, clientSocket);
                break;
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
        for (Map.Entry<Socket, String> entry : sessions.entrySet()) {
            if (entry.getValue().equals(clientID)) {
                System.out.println("###### Duplicate Client ID detected: " + clientID);
                try {
                    Socket existingSocket = entry.getKey();
                    processDisconnect(new byte[]{0x00, 0x00, 0x00}, existingSocket);
                    System.out.println("Closed existing session for Client ID: " + clientID);
                } catch (Exception e) {
                    System.err.println("Error closing previous session: " + e.getMessage());
                }
                break;
            }
        }
        sessions.put(clienSocket, clientID);
        System.out.println("Client successfully connected: " + clientID);
        System.out.println("Active sessions: " + sessions.toString());

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
        System.out.println("Active sessions: " + sessions.toString());
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

            // Check for retained message
            if (retainMessages.containsKey(topicName)) {
                byte[] retainedPayload = retainMessages.get(topicName);
                try {
                    System.out.println("Sending retained message for topic: " + topicName);
                    OutputStream output = clientSocket.getOutputStream();
                    byte[] publishMessage = constructPublishMessage(topicName, retainedPayload, 0);
                    output.write(publishMessage);
                    output.flush();
                } catch (Exception e) {
                    System.out.println("Failed to send retained message to subscriber: " + e.getMessage());
                }
            }

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

    public byte[] processPublish(byte[] data, Socket clientSocket) {
        System.out.println("\nProcessing PUBLISH message");

        // HEADER

        // Extract QoS, DUP et RETAIN
        int header = data[0];
        int qos = (header >> 1) & 0x03; // QoS niveau
        boolean retain = (header & 0x01) == 1;
        boolean dup = ((header >> 3) & 0x01) == 1;

        // remaining length
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

        // topic length + name
        int topicLength = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        String topicName = new String(data, index, topicLength);
        index += topicLength;

        System.out.println("Topic: " + topicName + ", QoS: " + qos + ", RETAIN: " + retain + ", DUP: " + dup);

        // packet/message identifier for QoS 1 (not available on QoS 0)
        int packetId = -1;
        if (qos > 0) {
            packetId = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
            index += 2;
            System.out.println("Packet ID: " + packetId);
        }

        // payload
        int payloadLength = remainingLength - (index - 2); // remaining after topic and packet id
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, index, payload, 0, payloadLength);

        String message = new String(payload);
        System.out.println("Payload: " + message);

        // Send the message to all sockets on the subscription list of the topic
        for (Map.Entry<String, List<Socket>> entry : topicSubscriptions.entrySet()) {
            String subscribedTopic = entry.getKey();

            if (topicMatches(subscribedTopic, topicName)) { // Check topic match (wildcard support)
                List<Socket> subscribers = entry.getValue();
                for (Socket subscriber : subscribers) {
                    try {
                        OutputStream output = subscriber.getOutputStream();
                        byte[] msg = constructPublishMessage(topicName, payload, 0);
                        output.write(msg);
                        output.flush();
                        System.out.println("Message sent to subscriber: " + subscriber + " for topic: " + subscribedTopic);
                    } catch (Exception e) {
                        System.out.println("Failed to send message to subscriber: " + subscriber);
                    }
                }
            }
        }

        // flag RETAIN
        if (retain) {
            retainMessages.put(topicName, payload);
            System.out.println("Message retained for topic: " + topicName);
        }

        // Retourner PUBACK pour QoS 1
        if (qos == 1) {
            System.out.println("Sending PUBACK for Packet ID: " + packetId);
            return sendPubAck(packetId);
        }

        return null;
    }

    private boolean topicMatches(String subscribedTopic, String publishedTopic) {
        String[] subLevels = subscribedTopic.split("/");
        String[] pubLevels = publishedTopic.split("/");
    
        for (int i = 0; i < subLevels.length; i++) {
            if (i >= pubLevels.length) return false; // Plus de niveaux dans l'abonné que dans le message publié
    
            if (subLevels[i].equals("#")) {
                return true; // Le # capture tous les sous-sujets restants
            }
    
            if (!subLevels[i].equals("+") && !subLevels[i].equals(pubLevels[i])) {
                return false; // Pas de correspondance
            }
        }
    
        return pubLevels.length == subLevels.length; // Correspondance exacte
    }
    

    private byte[] constructPublishMessage(String topicName, byte[] payload, int qos) {
        System.out.println("Process CONSTRUCT PLUBLISH MESSAGE");
        byte[] topicBytes = topicName.getBytes();
        int payloadLength = payload.length;

        int remainingLength = 2 + topicBytes.length + payloadLength + (qos > 0 ? 2 : 0);
        byte[] remainingLengthBytes = encodeRemainingLength(remainingLength);
        System.out.println("Remaining Length: " + remainingLength);
    
        // Fixed header + Remaining Length
        int fixedHeaderLength = 1 + remainingLengthBytes.length;
        byte[] publishMessage = new byte[fixedHeaderLength + remainingLength];
        //byte[] publishMessage = new byte[1 + remainingLength];

        publishMessage[0] = (byte) (0x30 | (qos << 1)); // Fixed header: PUBLISH + QoS
    
        //publishMessage[1] = (byte) remainingLength;

        // Insert Remaining Length
        System.arraycopy(remainingLengthBytes, 0, publishMessage, 1, remainingLengthBytes.length);

        // Insert Topic Length and Topic Name
        int index = fixedHeaderLength;
        publishMessage[index++] = (byte) ((topicBytes.length >> 8) & 0xFF);
        publishMessage[index++] = (byte) (topicBytes.length & 0xFF);
        System.arraycopy(topicBytes, 0, publishMessage, index, topicBytes.length);
        index += topicBytes.length;

        // topic length + name
        // publishMessage[2] = (byte) ((topicBytes.length >> 8) & 0xFF);
        // publishMessage[3] = (byte) (topicBytes.length & 0xFF);
        // System.arraycopy(topicBytes, 0, publishMessage, 4, topicBytes.length);
    
        // // Packet Identifier pour QoS 1
        // int index = 4 + topicBytes.length;
        // if (qos > 0) {
        //     publishMessage[index++] = (byte) 0x00; // Packet ID (à améliorer pour avoir un ID unique)
        //     publishMessage[index++] = (byte) 0x01; // Exemple d'ID : 1
        // }

        // insert payload
        System.arraycopy(payload, 0, publishMessage, index, payloadLength);

        System.out.println("Constructed PUBLISH message: " + Arrays.toString(publishMessage));
        return publishMessage;
    }

    private byte[] encodeRemainingLength(int length) {
        List<Byte> encodedBytes = new ArrayList<>();
    
        do {
            int encodedByte = length % 128; // 7-bit group
            length /= 128;
    
            if (length > 0) {
                encodedByte |= 128; // Set MSB to 1 if more bytes follow
            }
    
            encodedBytes.add((byte) encodedByte);
        } while (length > 0);
    
        // Convert List<Byte> to byte[]
        byte[] result = new byte[encodedBytes.size()];
        for (int i = 0; i < encodedBytes.size(); i++) {
            result[i] = encodedBytes.get(i);
        }
    
        return result;
    }
    

    private byte[] sendPubAck(int packetId) {
        byte[] pubAck = new byte[4];
    
        pubAck[0] = (byte) 0x40; // PUBACK
        pubAck[1] = (byte) 2;    // Remaining Length
        pubAck[2] = (byte) ((packetId >> 8) & 0xFF);
        pubAck[3] = (byte) (packetId & 0xFF);
    
        return pubAck;
    }

    /*
    Gérer les déconnexions des clients de manière ordonnée ou inattendue, en effectuant les actions suivantes :
         Désinscrire le client de tous les sujets auxquels il est abonné.
         Supprimer la session du client du broker.
        - Gérer les cas de déconnexion inattendue (par exemple, une perte de connexion sans message DISCONNECT).
    */
    public void processDisconnect(byte[] data, Socket clientSocket) {
        if (!sessions.containsKey(clientSocket)) {
            System.out.println("Client already disconnected: " + clientSocket.getInetAddress());
            return;
        }

        System.out.println("\nProcessing DISCONNECT message");

        if (data == null) { // Unexpected disconnection
            System.out.println("Disconnect triggered unexpectedly for client: " + clientSocket.getInetAddress());
        } else {
            int disconnectReason = data[2];
            /*if (disconnectReason != 0) {
                System.out.println("(\"###### DISCONNECT reason is not normal: " + disconnectReason);
                return;
            }*/
            System.out.println("Client disconnected gracefully with reason code: " + disconnectReason);
        }

        // unsubscribe the client form all its topics
        // topicSubscriptions.forEach((topic, clients) -> {
        //     clients.remove(clientSocket);
        //     if (clients.isEmpty()) { // if the topic don't have subscribers
        //         topicSubscriptions.remove(topic);
        //     }
        // });

        List<String> topicsToRemove = new ArrayList<>();
        for (Map.Entry<String, List<Socket>> entry : topicSubscriptions.entrySet()) {
            List<Socket> clients = entry.getValue();
            clients.remove(clientSocket);
            if (clients.isEmpty()) {
                topicsToRemove.add(entry.getKey());
            }
        }

        // Delete all the empty topics
        for (String topic : topicsToRemove) {
            topicSubscriptions.remove(topic);
        }

        // remove the client from the active users
        String clientID = sessions.remove(clientSocket);
        System.out.println("Client " + clientID + " successfully disconnected.");

        try {
            clientSocket.close();
            System.out.println("Connection closed for client (disconnect): " + clientSocket.getInetAddress());
            System.out.println("Active sessions: " + sessions.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
