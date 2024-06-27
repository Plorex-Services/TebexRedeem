package it.bitrule.tebex.listener;

import com.mojang.authlib.GameProfile;
import com.mongodb.client.model.Filters;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.tebex.TebexPlugin;
import it.bitrule.tebex.object.model.TebexTransaction;
import it.bitrule.tebex.object.tebex.Package;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

@RequiredArgsConstructor
public final class PlayerJoinListener implements Listener {

    private final @NonNull TebexPlugin plugin;

    @EventHandler
    public void onPlayerJoinEvent(@NonNull PlayerJoinEvent ev) {
        Player source = ev.getPlayer();
        if (!source.isOnline()) {
            throw new RuntimeException("Player is not online");
        }

        if (!(source instanceof CraftPlayer)) return;

        GameProfile gameProfile = ((CraftPlayer) source).getHandle().getProfile();
        if (gameProfile == null || gameProfile.getId() == null) return;

        Repository<TebexTransaction> repository = Miwiklark.getRepository(TebexTransaction.class);
        Bukkit.getScheduler().runTaskAsynchronously(
                this.plugin,
                () -> repository.findMany(Filters.eq("source", source.getUniqueId().toString()))
                        .forEach(transaction -> {
                            for (Package tebexPackage : transaction.getPackages()) {
                                List<String> commands = this.plugin.getConfig().getStringList("packages." + tebexPackage.getName());
                                if (commands == null || commands.isEmpty()) continue;

                                for (String command : commands) {
                                    Bukkit.dispatchCommand(
                                            Bukkit.getConsoleSender(),
                                            command.replace("{player}", source.getName())
                                    );
                                }
                            }

                            repository.delete(transaction.getIdentifier());
                        })
        );
    }
}