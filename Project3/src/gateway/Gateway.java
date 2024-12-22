package Project3.src.gateway;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Project3.src.coap.CoAPClient;

public class Gateway {
    
    private final CoAPClient coapClient;

    public Gateway() {
        this.coapClient = new CoAPClient();
    }

    public void start() {
        coapClient.start();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("[INFO] Fetching data from CoAP server...");
                byte[] kitchenData = coapClient.createGETRequest("kitchen");
                coapClient.sendMessage(kitchenData);

                byte[] livingRoomData = coapClient.createGETRequest("livingroom");
                coapClient.sendMessage(livingRoomData);

                byte[] bedroomData = coapClient.createGETRequest("bedroom");
                coapClient.sendMessage(bedroomData);

                byte[] bathroomData = coapClient.createGETRequest("bathroom");
                coapClient.sendMessage(bathroomData);

            } catch (Exception e) {
                System.err.println("[ERROR] Failed to fetch or publish data: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS); // Repeat every 10 seconds
    }

    public void shutdown() {
        coapClient.shutdown();
    }

    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        gateway.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gateway.shutdown();
        }));
    }
}
