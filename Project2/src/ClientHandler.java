package Project2.src;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket clientSocket;
    private boolean isConnected = true;
    private boolean isDisconnected = false;
    private MQTTBroker broker;

    public ClientHandler(Socket socket, MQTTBroker b) {
        this.clientSocket = socket;
        this.broker = b;
        System.out.println("Client handler started for: " + clientSocket.getInetAddress());
    }

    @Override
    public void run() {
        try {
            while(isConnected) {
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();

                byte[] buffer = new byte[2048];
                int bytesRead = input.read(buffer); // read data

                // if (bytesRead == -1) { // Clean disconnection when the client return END
                //     // System.out.println("Client disconnected gracefully: " + clientSocket.getInetAddress());
                //     // broker.processDisconnect(new byte[]{0x00, 0x00, 0x00}, clientSocket); 
                //     if (!isDisconnected) {
                //         isDisconnected = true;
                //         System.out.println("Client disconnected gracefully: " + clientSocket.getInetAddress());
                //         broker.processDisconnect(new byte[]{0x00, 0x00, 0x00}, clientSocket); // Fake message to have a disconnection
                //     }
                //     break;
                // }

                byte[] response = broker.processMessage(buffer, clientSocket);

                output.write(response);
                output.flush();
                //output.close();
            }
        } catch (Exception e) { // Unexpexted disconnection
            // e.printStackTrace();
            // System.out.println("Client disconnected unexpectedly: " + clientSocket.getInetAddress());
            // //shutdown();
            // broker.processDisconnect(null, clientSocket); // deco brusque
            if (!isDisconnected) {
                isDisconnected = true;
                System.out.println("Client disconnected unexpectedly: " + clientSocket.getInetAddress());
                broker.processDisconnect(null, clientSocket); // deco brusque
            }
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        isConnected = false;
        try {
            clientSocket.close();
            System.out.println("Connection closed for client: " + clientSocket.getInetAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
