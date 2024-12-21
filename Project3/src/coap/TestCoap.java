package Project3.src.coap;

public class TestCoap {
    
    public static void main(String[] args) {
        try {
            // create the CoAP client
            CoAPClient client = new CoAPClient();

            // GET
            System.out.println("\n-----GET REQUEST-----");
            byte[] getRequest = client.createGETRequest("kitchen");
            client.sendMessage(getRequest);

            // GET
            System.out.println("\n-----GET REQUEST-----");
            getRequest = client.createGETRequest("livingroom");
            client.sendMessage(getRequest);

            // GET
            System.out.println("\n-----GET REQUEST-----");
            getRequest = client.createGETRequest("bedroom");
            client.sendMessage(getRequest);

            // GET
            System.out.println("\n-----GET REQUEST-----");
            getRequest = client.createGETRequest("bathroom");
            client.sendMessage(getRequest);

            // close the CoAP client
            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
