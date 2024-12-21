package Project3.src.coap;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class DataSender {
    private static final String GATEWAY_HOST = "localhost";
    private static final int GATEWAY_PORT = 1880;

    public static void sendDataToGateway(String data) {
        try (Socket socket = new Socket(GATEWAY_HOST, GATEWAY_PORT);
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true)) {
            writer.println(data);
            System.out.println("[INFO] Sent data to gateway: " + data);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send data: " + e.getMessage());
        }
    }
}
