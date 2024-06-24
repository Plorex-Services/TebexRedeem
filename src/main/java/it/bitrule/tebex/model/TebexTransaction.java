package it.bitrule.tebex.model;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class TebexTransaction implements IModel {

    /**
     * This is the id of the transaction
     */
    @SerializedName("_id")
    private final @NonNull String identifier;
    /**
     * This is the id of the player who have this transaction
     */
    private final @NonNull String source;
    /**
     * This is the package name of the transaction
     */
    private final @NonNull String packageName;
}