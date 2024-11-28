package Project2.src;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket clientSocket;
    private boolean isConnected = true;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        System.out.println("Client handler started for: " + clientSocket.getInetAddress());
    }

    @Override
    public void run() {
        try {
            while(isConnected) {
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
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
