package it.bitrule.tebex.service;

import it.bitrule.tebex.object.tebex.PaymentsInfo;
import lombok.NonNull;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PaymentsService {

    @GET("payments")
    @NonNull Call<PaymentsInfo> retrieve(@Query("paged") int page);
}