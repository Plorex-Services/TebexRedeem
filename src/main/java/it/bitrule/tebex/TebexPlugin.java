package it.bitrule.tebex;

import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.tebex.command.RedeemCommandExecutor;
import it.bitrule.tebex.listener.PlayerJoinListener;
import it.bitrule.tebex.repository.LabymodRepository;
import it.bitrule.tebex.repository.TebexRepository;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public final class TebexPlugin extends JavaPlugin {

    @SneakyThrows
    public void onEnable() {
        this.saveDefaultConfig();

        File file = new File(this.getDataFolder(), "tebex.csv");
        if (!file.exists()) {
            throw new RuntimeException("tebex.csv not found");
        }

        ConfigurationSection mongoSection = this.getConfig().getConfigurationSection("mongodb");
        if (mongoSection == null) {
            throw new RuntimeException("MongoDB section not found");
        }

        String dbUsername = mongoSection.getString("username");
        if (dbUsername == null || dbUsername.isEmpty()) {
            throw new RuntimeException("MongoDB username not found");
        }

        String password = mongoSection.getString("password");
        if (password != null && !password.isEmpty()) {
            dbUsername = dbUsername + ":" + password; // TODO: This is to implement the password into the db username
        }

        String address = mongoSection.getString("address");
        String[] addressSplit = address.split(":");

        Miwiklark.authMongo(String.format("mongodb://%s@%s:%s/", dbUsername, addressSplit[0], addressSplit.length > 1 ? addressSplit[1] : "27017"));

        String dbName = mongoSection.getString("db-name");
        if (dbName == null || dbName.isEmpty()) {
            throw new RuntimeException("MongoDB database name not found");
        }

        this.getCommand("redeem").setExecutor(new RedeemCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        if (!this.getConfig().getBoolean("import")) return;

        TebexRepository tebexRepository = new TebexRepository();
        tebexRepository.init(this.getConfig().getString("tebex-secret"));

        LabymodRepository labymodRepository = new LabymodRepository();
        labymodRepository.init();

        tebexRepository.adapt(labymodRepository, dbName);

        this.getConfig().set("import", false);
        this.saveConfig();
    }
}