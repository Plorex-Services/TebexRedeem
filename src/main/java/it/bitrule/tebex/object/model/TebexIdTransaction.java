package it.bitrule.tebex.object.model;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor @Data
public final class TebexIdTransaction implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;
    /**
     * The id of who redeemed the transaction
     */
    private final @NonNull String source;
}