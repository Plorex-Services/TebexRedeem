package it.bitrule.tebex.model;

import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class TebexIdTransaction implements IModel {

    private final @NonNull String identifier;
    private final @NonNull String packageName;
}