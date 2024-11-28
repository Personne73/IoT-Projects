package Project2.src;

public class Main {
    
    public static void main(String[] args) {
        try {
            TCPServer tcpServer = new TCPServer();
            tcpServer.start(); // start a thread for the server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

