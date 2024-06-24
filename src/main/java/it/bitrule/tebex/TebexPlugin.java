package it.bitrule.tebex;

import com.google.gson.*;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.tebex.command.RedeemCommandExecutor;
import it.bitrule.tebex.listener.PlayerJoinListener;
import it.bitrule.tebex.model.TebexIdTransaction;
import it.bitrule.tebex.model.TebexTransaction;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public final class TebexPlugin extends JavaPlugin {

    public void onEnable() {
        this.saveDefaultConfig();

        File file = new File(this.getDataFolder(), "tebex.csv");
        if (!file.exists()) {
            throw new RuntimeException("tebex.csv not found");
        }

        String mongodbUri = this.getConfig().getString("mongodb-uri");
        if (mongodbUri == null || mongodbUri.isEmpty()) {
            throw new RuntimeException("MongoDB URI not found");
        }

        ConfigurationSection mongoSection = this.getConfig().getConfigurationSection("mongodb");
        if (mongoSection == null) {
            throw new RuntimeException("MongoDB section not found");
        }

        String dbUsername = mongoSection.getString("username");
        String password = mongoSection.getString("password");

        if (dbUsername == null || dbUsername.isEmpty()) {
            throw new RuntimeException("MongoDB username not found");
        }

        if (password == null || password.isEmpty()) {
            throw new RuntimeException("MongoDB password not found");
        }

        String address = mongoSection.getString("address");
        String[] addressSplit = address.split(":");

        Miwiklark.authMongo(String.format("mongodb://%s:%s@%s:%s/", dbUsername, password, addressSplit[0], addressSplit.length > 1 ? addressSplit[1] : "27017"));

        String dbName = mongoSection.getString("db-name");
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("MongoDB database name not found");
        }

        Repository<TebexTransaction> premiumRepository = Miwiklark.addRepository(
                TebexTransaction.class,
                dbName,
                "premium"
        );
        Repository<TebexIdTransaction> repository = Miwiklark.addRepository(
                TebexIdTransaction.class,
                dbName,
                "id"
        );

        this.getCommand("redeem").setExecutor(new RedeemCommandExecutor(this));

        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        if (!this.getConfig().getBoolean("import")) return;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss");

            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) continue;

                String[] parts = line.split(",");
                if (parts.length == 0) {
                    throw new RuntimeException("Invalid line in tebex.csv: " + line);
                }

                if (parts.length < 16) {
                    System.out.println("Skipping line in tebex.csv: " + line);

                    continue;
                }

                String transactionId = parts[1];
                String date = parts[2];
                String status = parts[3];
                if (!status.equals("Complete")) continue;

                String username = parts[5];
                String uuid = parts[6];

                String[] packages = parts[15].split(",");
                if (packages.length == 0) {
                    throw new RuntimeException("No packages in tebex.csv for transaction " + transactionId);
                }

                if (uuid.startsWith("0000000000000000000000")) {
                    repository.save(new TebexIdTransaction(
                            transactionId,
                            String.join(", ", packages)
                    ));

                    continue;
                }

                JsonObject jsonObject = this.parseJson("https://laby.net/api/search/get-previous-accounts/" + username);
                if (jsonObject == null) {
                    throw new RuntimeException("Failed to fetch previous accounts for " + username);
                }

                JsonPrimitive hiddenObject = jsonObject.getAsJsonPrimitive("has_hidden");
                if (hiddenObject == null || hiddenObject.getAsBoolean()) continue;

                JsonArray jsonArray = jsonObject.getAsJsonArray("users");
                if (jsonArray == null) {
                    throw new RuntimeException("Failed to fetch users for " + username);
                }

                if (jsonArray.size() == 0) continue;

                System.out.println(jsonArray.size() + " previous accounts for " + username);

                long purchasedTime = simpleDateFormat.parse(date.replaceAll("T", "").replaceAll("\\+", "")).getTime();

//                System.out.println("Purchased time: " + purchasedTime);

                JsonObject betterJsonObject = null;
                Long betterChangedAt = null;

                for (JsonElement jsonElement : jsonArray) {
                    JsonObject userObject = jsonElement.getAsJsonObject();
                    if (userObject == null) {
                        throw new RuntimeException("Failed to fetch user object");
                    }

                    JsonArray historyArray = userObject.getAsJsonArray("history");
                    if (historyArray == null || historyArray.size() == 0) {
                        throw new RuntimeException("Failed to fetch history object");
                    }

                    for (JsonElement historyElement : historyArray) {
                        JsonObject historyObject = historyElement.getAsJsonObject();
                        if (historyObject == null) {
                            throw new RuntimeException("Failed to fetch history object");
                        }

                        String name = historyObject.get("name").getAsString();
                        if (name == null) {
                            throw new RuntimeException("Failed to fetch name object");
                        }

                        if (!name.equals(username)) continue;

                        JsonElement changedAtObject = historyObject.get("changed_at");
                        if (changedAtObject != null && !changedAtObject.isJsonNull()) {
                            String changedAt = changedAtObject.getAsString();
                            if (changedAt == null) continue;

                            long changedAtTime = simpleDateFormat.parse(changedAt.replaceAll("T", "").replaceAll("\\+", "")).getTime();
                            if (changedAtTime > purchasedTime) continue;
                            if (betterChangedAt != null && betterChangedAt > changedAtTime) continue;

                            betterChangedAt = changedAtTime;
                        }

                        betterJsonObject = userObject;
                    }
                }

                if (betterJsonObject == null) continue;

                System.out.println("Better account is " + betterJsonObject.get("uuid").getAsString());

                premiumRepository.save(new TebexTransaction(
                        transactionId,
                        betterJsonObject.get("uuid").getAsString(),
                        String.join(", ", packages)
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read tebex.csv", e);
        }

        this.getConfig().set("import", false);
        this.saveConfig();
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