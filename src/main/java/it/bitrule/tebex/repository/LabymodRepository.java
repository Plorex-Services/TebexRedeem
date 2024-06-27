package it.bitrule.tebex.repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.bitrule.tebex.service.AccountsService;
import lombok.NonNull;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;

public final class LabymodRepository {

    private @Nullable AccountsService accountsService;

    public void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://laby.net")
                .build();

        this.accountsService = retrofit.create(AccountsService.class);
    }

    public @Nullable UUID validate(@NonNull String username, @NonNull String purchaseDate) throws IOException, ParseException {
        if (this.accountsService == null) {
            throw new IllegalStateException("LabymodRepository has not been initialized yet");
        }

        Response<JsonObject> response = this.accountsService.lookup(username).execute();
        if (!response.isSuccessful()) return null;

        JsonObject jsonObject = response.body();
        if (jsonObject == null) return null;

        JsonPrimitive hiddenObject = jsonObject.getAsJsonPrimitive("has_hidden");
        if (hiddenObject == null || hiddenObject.getAsBoolean()) return null;

        JsonArray jsonArray = jsonObject.getAsJsonArray("users");
        if (jsonArray == null) {
            throw new RuntimeException("Failed to fetch users for " + username);
        }

        if (jsonArray.size() == 0) return null;

        long purchasedTime = TebexRepository.SIMPLE_DATE_FORMAT.parse(purchaseDate.replaceAll("T", "").replaceAll("\\+", "")).getTime();

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

                    long changedAtTime = TebexRepository.SIMPLE_DATE_FORMAT.parse(changedAt.replaceAll("T", "").replaceAll("\\+", "")).getTime();
                    if (changedAtTime > purchasedTime) continue;
                    if (betterChangedAt != null && betterChangedAt > changedAtTime) continue;

                    betterChangedAt = changedAtTime;
                }

                betterJsonObject = userObject;
            }
        }

        if (betterJsonObject == null) return null;

        JsonPrimitive uuidObject = betterJsonObject.getAsJsonPrimitive("uuid");
        if (uuidObject == null) return null;

        return UUID.fromString(uuidObject.getAsString());
    }
}