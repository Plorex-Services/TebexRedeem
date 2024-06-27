package it.bitrule.tebex.object.tebex;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor @Data
public final class TebexPlayer {

    private final int id;
    private final @NonNull String name;
    @SerializedName("uuid")
    private final @NonNull String uniqueId;
}