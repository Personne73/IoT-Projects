package Project3.src.mqtt;

public class TestGateway {
         
    public static void main(String[] args) {
        try {
            MQTTBroker broker = new MQTTBroker();
            TCPServer tcpServer = new TCPServer(broker);
            tcpServer.start(); // start a thread for the server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
