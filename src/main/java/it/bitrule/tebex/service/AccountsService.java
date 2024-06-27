package it.bitrule.tebex.service;

import com.google.gson.JsonObject;
import lombok.NonNull;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AccountsService {

    @GET("api/search/get-previous-accounts/{username}")
    @NonNull Call<JsonObject> lookup(@Path("username") String username);
}