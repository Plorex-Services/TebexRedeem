package it.bitrule.tebex.object.tebex;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor @Data
public final class PaymentsInfo {

    private final int total;
    @SerializedName("per_page")
    private final int perPage;
    @SerializedName("current_page")
    private final int currentPage;
    @SerializedName("last_page")
    private final int lastPage;
    @SerializedName("data")
    private final List<Payment> payments;
}