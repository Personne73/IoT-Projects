package Project2.src;


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
        int remainingLength = data[1];
        int index = 2;


        return sendConAck(0);
    }

    public byte[] sendConAck(int returnCode) {
        byte[] connAck = new byte[4];

        connAck[0] = (byte) 0x10; // CONNACK
        connAck[1] = (byte) 3; // 3 remaining bytes
        connAck[2] = (byte) 0; // no flags present
        connAck[3] = (byte) returnCode;

        return connAck;
    }
}
