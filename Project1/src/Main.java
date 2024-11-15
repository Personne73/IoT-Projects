package Project1.src;

public class Main {
    
    public static void main(String[] args) {
        try {
            System.out.println("--> Hello World");
            byte b1 = -1;  
            int unsignedValue = b1 & 0xFF;
            String binary = String.format("%8s", Integer.toBinaryString(unsignedValue)).replace(' ', '0');
            System.out.println("Valeur signée : " + b1 + " -> Non signée : " + unsignedValue);
            System.out.println("Représentation binaire non signée : " + binary);
            
            
            int id16bit = 0xABCD; // Exemple d'ID 16 bits (hexadécimal)

        // Extraire les deux octets
        byte highByte = (byte) ((id16bit >> 8) & 0xFF); // Décalage des 8 bits supérieurs
        byte lowByte = (byte) (id16bit & 0xFF);         // Masquage des 8 bits inférieurs

        // Affichage des résultats
        System.out.println("ID 16 bits : " + Integer.toHexString(id16bit));
        System.out.println("High Byte : " + Integer.toHexString(highByte & 0xFF));
        System.out.println("Low Byte  : " + Integer.toHexString(lowByte & 0xFF));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
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
 */
