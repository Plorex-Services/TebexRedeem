package it.bitrule.tebex;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.tebex.command.RedeemCommandExecutor;
import it.bitrule.tebex.listener.PlayerJoinListener;
import it.bitrule.tebex.repository.LabymodRepository;
import it.bitrule.tebex.repository.TebexRepository;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class TebexPlugin extends JavaPlugin {

    @SneakyThrows
    public void onEnable() {
        this.saveDefaultConfig();

        ConfigurationSection mongoSection = this.getConfig().getConfigurationSection("mongodb");
        if (mongoSection == null) {
            throw new RuntimeException("MongoDB section not found");
        }

        String dbUsername = mongoSection.getString("username");
        if (dbUsername == null || dbUsername.isEmpty()) {
            throw new RuntimeException("MongoDB username not found");
        }

        String dbName = mongoSection.getString("db-name");
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("MongoDB database name not found");
        }

        String address = mongoSection.getString("address");
        if (address == null || address.isEmpty()) {
            throw new RuntimeException("MongoDB address not found");
        }

        String[] addressSplit = address.split(":");
        String password = mongoSection.getString("password");
        if (password == null || password.isEmpty()) {
            System.out.println("Connecting to MongoDB without authentication");
            Miwiklark.withoutAuth(addressSplit[0], addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 27017);
        } else {
            System.out.println("Connecting to MongoDB with authentication");
            Miwiklark.authMongo(String.format("mongodb://%s:%s@%s:%s/admin", dbUsername, password, addressSplit[0], addressSplit.length > 1 ? addressSplit[1] : "27017"));
        }

        TebexRepository tebexRepository = new TebexRepository();
        tebexRepository.init(this.getConfig().getString("tebex-secret"));

        this.getCommand("redeem").setExecutor(new RedeemCommandExecutor(this, tebexRepository));
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        if (!this.getConfig().getBoolean("import")) return;

        LabymodRepository labymodRepository = new LabymodRepository();
        labymodRepository.init();

        tebexRepository.adapt(labymodRepository, dbName);

        this.getConfig().set("import", false);
        this.saveConfig();
    }
}