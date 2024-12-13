package Project2.src;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket clientSocket;
    private boolean isConnected = true;
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
                input.read(buffer); // read data

                byte[] response = broker.processMessage(buffer, clientSocket);

                output.write(response);
                output.flush();
                //output.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Client disconnected unexpectedly: " + clientSocket.getInetAddress());
            shutdown();
        }
    }

    public void shutdown() {
        isConnected = false;
        try {
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
