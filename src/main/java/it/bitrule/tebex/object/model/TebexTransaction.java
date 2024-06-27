package it.bitrule.tebex.object.model;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import it.bitrule.tebex.object.tebex.Package;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
     * The list of packages that the player have bought
     */
    private final @NonNull List<Package> packages;
}