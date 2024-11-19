package Project1.src;

public class Main {
    
    public static void main(String[] args) {
        try {
            // create the CoAP client
            CoAPClient client = new CoAPClient();

            // GET
            System.out.println("\n-----GET REQUEST-----");
            byte[] getRequest = client.createGETRequest("test");
            client.sendMessage(getRequest);

            // POST
            System.out.println("\n-----POST REQUEST-----");
            byte[] postRequest = client.createPOSTRequest("sink", "temperature=25");
            client.sendMessage(postRequest);

            // PUT
            System.out.println("\n-----PUT REQUEST-----");
            byte[] putRequest = client.createPUTRequest("sink", "temperature=-6");
            client.sendMessage(putRequest);

            // DELETE
            System.out.println("\n-----DELETE REQUEST-----");
            byte[] deleteRequest = client.createDELETERequest("sink");
            client.sendMessage(deleteRequest);

            // close the CoAP client
            client.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
