package Project3.src.mqtt;

import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {

    private boolean runServer = true;
    private ServerSocket ss;
    private MQTTBroker broker;

    public TCPServer(MQTTBroker b) {
        try {
            this.ss = new ServerSocket(1883);
            this.broker = b;
            System.out.println("Server is running and waiting for clients connection...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(runServer) {
                Socket clientSocket = ss.accept();
                System.out.println("Client connected:" + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, broker);
                clientHandler.start(); // start a new thread
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        // Implement the shutdown method
        runServer = false;
        try {
            ss.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
