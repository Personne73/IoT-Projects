package Project0.src;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        
        try {
            RestServer restServer = new RestServer();
            restServer.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Press any key to stop the server");
            reader.readLine();

            restServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
