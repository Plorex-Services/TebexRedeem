package it.bitrule.tebex;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public final class TebexPlugin extends JavaPlugin {

    public void onEnable() {
        File file = new File(this.getDataFolder(), "tebex.csv");
        if (!file.exists()) {
            throw new RuntimeException("tebex.csv not found");
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 0) {
                    throw new RuntimeException("Invalid line in tebex.csv: " + line);
                }

                if (parts.length < 16) {
                    System.out.println("Skipping line in tebex.csv: " + line);

                    continue;
                }

                String id = parts[0];
                String transactionId = parts[1];
                String date = parts[2];
                String status = parts[3];
                String gateway = parts[4];
                String username = parts[5];
                String uuid = parts[6];

                if (uuid.startsWith("0000000000000000000000")) continue;

                String[] packages = parts[15].split(",");
                if (packages.length == 0) {
                    throw new RuntimeException("No packages in tebex.csv for transaction " + transactionId);
                }

                JsonObject jsonObject = this.parseJson("https://laby.net/api/search/get-previous-accounts/" + username);
                if (jsonObject == null) {
                    throw new RuntimeException("Failed to fetch previous accounts for " + username);
                }

                JsonArray jsonArray = jsonObject.getAsJsonArray("users");
                if (jsonArray.size() == 0) {
                    throw new RuntimeException("No previous accounts for " + username);
                }

                System.out.println(jsonArray.size() + " previous accounts for " + username);
//                for (JsonElement jsonElement : jsonArray) {
//
//                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read tebex.csv", e);
        }
    }

    private @Nullable JsonObject parseJson(@NonNull String urlString) {
        try {
            URL url = new URL(urlString);

            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("Accept", "application/json");

            int responseCode = http.getResponseCode();
            InputStream inputStream = 200 <= responseCode && responseCode <= 299 ? http.getInputStream() : http.getErrorStream();
            if (inputStream == null) return null;

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String currentLine;
            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }

            in.close();

            return new JsonParser().parse(response.toString()).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return null;
    }
}