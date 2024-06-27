package it.bitrule.tebex.object.tebex;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor @Data
public final class Payment {

    private final @NonNull String id;
    private final @NonNull String status;
    private final @NonNull String date;

    private final @NonNull TebexPlayer player;
    private final @NonNull List<Package> packages;
}