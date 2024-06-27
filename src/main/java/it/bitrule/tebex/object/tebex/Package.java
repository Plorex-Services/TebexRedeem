package it.bitrule.tebex.object.tebex;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class Package {

    private final int quantity;
    private final long id;
    private final @NonNull String name;
}