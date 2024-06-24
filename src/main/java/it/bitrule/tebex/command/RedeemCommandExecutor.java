package it.bitrule.tebex.command;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import it.bitrule.tebex.TebexPlugin;
import it.bitrule.tebex.model.TebexIdTransaction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public final class RedeemCommandExecutor implements CommandExecutor {

    private final @NonNull TebexPlugin plugin;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /redeem <tebex-id>");

            return true;
        }

        Repository<TebexIdTransaction> repository = Miwiklark.getRepository(TebexIdTransaction.class);
        CompletableFuture.supplyAsync(() -> repository.findOne(args[0]))
                .thenAccept(transactionOptional -> {
                    TebexIdTransaction transaction = transactionOptional.orElse(null);
                    if (transaction == null) {
                        commandSender.sendMessage(ChatColor.RED + "Invalid Tebex ID");

                        return;
                    }

                    commandSender.sendMessage(ChatColor.GREEN + "You have redeemed the package " + transaction.getPackageName());

                    String[] packages = transaction.getPackageName().split(", ");
                    for (String packageName : packages) {
                        List<String> commands = this.plugin.getConfig().getStringList("packages." + packageName.replaceAll("'", ""));
                        if (commands == null || commands.isEmpty()) continue;

                        for (String commandName : commands) {
                            Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    commandName.replace("{player}", commandSender.getName())
                            );
                        }
                    }

                    repository.delete(transaction.getIdentifier());
                });

        return false;
    }
}