package it.bitrule.tebex.object.model;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import it.bitrule.tebex.object.tebex.Package;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor @Data
public final class TebexIdTransaction implements IModel {

    @SerializedName("_id")
    private final @NonNull String identifier;
    /**
     * The list of packages that the player have bought
     */
    private final @NonNull List<Package> packages;
}