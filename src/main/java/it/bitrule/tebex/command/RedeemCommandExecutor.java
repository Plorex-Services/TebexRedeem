package it.bitrule.tebex.command;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.tebex.TebexPlugin;
import it.bitrule.tebex.object.model.TebexIdTransaction;
import it.bitrule.tebex.object.tebex.Package;
import it.bitrule.tebex.object.tebex.Payment;
import it.bitrule.tebex.repository.TebexRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class RedeemCommandExecutor implements CommandExecutor {

    private final @NonNull TebexPlugin plugin;
    private final @NonNull TebexRepository tebexRepository;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /redeem <tebex-id>");

            return true;
        }

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "You must be a player to execute this command");

            return true;
        }

        Repository<TebexIdTransaction> repository = Miwiklark.getRepository(TebexIdTransaction.class);
        CompletableFuture.runAsync(() -> {
            if (repository.findOne(args[0]).isPresent()) {
                commandSender.sendMessage(ChatColor.RED + "You have already redeemed this package");

                return;
            }

            Payment payment = this.tebexRepository.lookup(args[0]);
            if (payment == null) {
                commandSender.sendMessage(ChatColor.RED + "Failed to fetch payment");

                return;
            }

            commandSender.sendMessage(ChatColor.GREEN + "You have redeemed the package " + payment.getPackages().stream().map(Package::getName).collect(Collectors.joining(", ")));

            for (Package tebexPackage : payment.getPackages()) {
                List<String> commands = this.plugin.getConfig().getStringList("packages." + tebexPackage.getName());
                if (commands == null || commands.isEmpty()) continue;

                for (String commandName : commands) {
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            commandName.replace("{player}", commandSender.getName())
                    );
                }
            }

            repository.save(new TebexIdTransaction(args[0], ((Player) commandSender).getUniqueId().toString()));
        }).exceptionally(throwable -> {
            commandSender.sendMessage(ChatColor.RED + "Failed to check if you have already redeemed this package: " + throwable.getMessage());

            return null;
        });

        return true;
    }
}