package it.bitrule.tebex;

import it.bitrule.tebex.repository.LabymodRepository;
import it.bitrule.tebex.repository.TebexRepository;
import org.jetbrains.annotations.NotNull;

public final class text {

    public static void main(String @NotNull [] args) {
        TebexRepository tebexRepository = new TebexRepository();
        tebexRepository.init("15bdf30a41e2814659ff250acce0357610c42092");

        LabymodRepository labymodRepository = new LabymodRepository();
        labymodRepository.init();

        try {
            tebexRepository.adapt(labymodRepository, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}