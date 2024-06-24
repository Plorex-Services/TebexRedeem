package it.bitrule.tebex.model;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class TebexIdTransaction implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;
    private final @NonNull String packageName;
}