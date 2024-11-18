package Project1.src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class CoAPServer {

    private static final int COAP_PORT = 5683;
    private DatagramSocket socket;
    private Map<String, String> resources; // Stockage des ressources

    public CoAPServer() {
        try {
            socket = new DatagramSocket(COAP_PORT);
            resources = new HashMap<>();
            System.out.println("CoAP Server is running on port " + COAP_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Traite la requête reçue
                String response = handleRequest(packet.getData(), packet.getLength());
                if (response != null) {
                    byte[] responseBytes = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        packet.getAddress(),
                        packet.getPort()
                    );
                    socket.send(responsePacket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String handleRequest(byte[] data, int length) {
        try {
            // Analyse le message reçu
            int code = data[1] & 0xFF; // Code (GET, POST, etc.)
            int messageId = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF); // Message ID
            String rawOptionsAndPayload = new String(data, 4, length - 4).trim();

            System.out.println("Received CoAP message:");
            System.out.println("Code: " + code + ", Message ID: " + messageId + ", Raw Options and Payload: " + rawOptionsAndPayload);

            // Décode les options (Uri-Path)
            String path = parseUriPath(data, length);

            // Gère les différentes commandes
            switch (code) {
                case 1: // GET
                    return handleGET(path);
                case 2: // POST
                    return handlePOST(rawOptionsAndPayload);
                case 3: // PUT
                    return handlePUT(rawOptionsAndPayload);
                case 4: // DELETE
                    return handleDELETE(path);
                default:
                    return "4.05 Method Not Allowed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "5.00 Internal Server Error";
        }
    }

    private String parseUriPath(byte[] data, int length) {
        // Cherche l'option Uri-Path (delta = 11)
        int index = 4; // Options commencent après l'en-tête
        while (index < length) {
            int delta = (data[index] & 0xF0) >> 4;
            int optionLength = data[index] & 0x0F;
            index++;

            if (delta == 11) { // Uri-Path
                return new String(data, index, optionLength).trim();
            }

            index += optionLength; // Passe à l'option suivante
        }
        return ""; // Si aucun Uri-Path n'est trouvé
    }

    private byte[] createCoAPResponse(int code, int messageId, String payload) {
        // Header (4 bytes)
        byte[] header = new byte[4];
        header[0] = (byte) 0x60; // Version 1, Type ACK (2 bits) -> 0110 0000
        header[1] = (byte) code; // Code (e.g., 2.05 for Content)
        header[2] = (byte) ((messageId >> 8) & 0xFF); // Message ID (high byte)
        header[3] = (byte) (messageId & 0xFF);        // Message ID (low byte)
    
        // Payload
        byte[] payloadBytes = payload.getBytes();
        byte[] response = new byte[header.length + 1 + payloadBytes.length];
        System.arraycopy(header, 0, response, 0, header.length);
        response[header.length] = (byte) 0xFF; // Payload marker
        System.arraycopy(payloadBytes, 0, response, header.length + 1, payloadBytes.length);
    
        return response;
    }
    
    private byte[] handleGET(int messageId, String path) {
        if ("sensor".equals(path)) {
            String payload = "{\"temperature\":25}";
            return createCoAPResponse(69, messageId, payload); // 69 = 2.05 in CoAP
        }
        return createCoAPResponse(132, messageId, "Not Found"); // 132 = 4.04 in CoAP
    }
    

    private String handleGET(String path) {
        if (path.equalsIgnoreCase("sensor")) {
            return "2.05 Content\nTemperature: 25°C";
        }
        return "4.04 Not Found";
    }

    private String handlePOST(String payload) {
        String[] parts = payload.split("=");
        if (parts.length == 2) {
            resources.put(parts[0], parts[1]);
            return "2.01 Created";
        }
        return "4.00 Bad Request";
    }

    private String handlePUT(String payload) {
        String[] parts = payload.split("=");
        if (parts.length == 2 && resources.containsKey(parts[0])) {
            resources.put(parts[0], parts[1]);
            return "2.04 Changed";
        }
        return "4.04 Not Found";
    }

    private String handleDELETE(String path) {
        if (resources.remove(path) != null) {
            return "2.02 Deleted";
        }
        return "4.04 Not Found";
    }

    public static void main(String[] args) {
        CoAPServer server = new CoAPServer();
        server.start();
    }
}
