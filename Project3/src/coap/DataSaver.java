package Project3.src.coap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataSaver {

    private static final String FILE_PATH = "Project3/files/temperature_data.txt";

    public static void saveData(String room, double temperature) {
        try {
            // Vérifier et créer le dossier parent si nécessaire
            File file = new File(FILE_PATH);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("[INFO] Directory created: " + parentDir.getAbsolutePath());
                }
            }

            // Créer le fichier s'il n'existe pas
            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("[INFO] File created: " + file.getAbsolutePath());
                }
            }

            // Écrire les données dans le fichier
            try (FileWriter writer = new FileWriter(file, true)) { // false pour écraser à chaque fois
                String data = String.format("Room: %s, Temperature: %.2f°C%n", room, temperature);
                writer.write(data);
                System.out.println("[INFO] Data saved to file: " + FILE_PATH);
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save data: " + e.getMessage());
        }
    }
}
