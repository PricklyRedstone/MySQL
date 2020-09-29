package br.pricklyredstone.mysql.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.pricklyredstone.mysql.bridge.database.EcoMysqlHandler;
import br.pricklyredstone.mysql.bridge.database.MysqlSetup;
import br.pricklyredstone.mysql.bridge.events.PlayerDisconnect;
import br.pricklyredstone.mysql.bridge.events.PlayerJoin;
import br.pricklyredstone.mysql.bridge.events.handlers.EcoDataHandler;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Eco extends JavaPlugin {
	
	public static Logger log;
	public static Economy vault = null;
	public static String pluginName = "MySQL";
	public Map<Player, Integer> syncCompleteTasks = new HashMap<Player, Integer>();
	
	private static ConfigHandler configHandler;
	private static MysqlSetup mysqlSetup;
	private static EcoMysqlHandler ecoMysqlHandler;
	private static EcoDataHandler edH;
	private static BackgroundTask bt;
	
	@Override
    public void onEnable() {
		log = getLogger();    	
        if (setupEconomy() == false) {
            log.severe("Aviso! - Cofre instalado? Se sim sistema Economy instalado?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        configHandler = new ConfigHandler(this);
        mysqlSetup = new MysqlSetup(this);
        ecoMysqlHandler = new EcoMysqlHandler(this);
        edH = new EcoDataHandler(this);
        bt = new BackgroundTask(this);        
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerJoin(this), this);
    	pm.registerEvents(new PlayerDisconnect(this), this);
    	log.info(pluginName + " carregado com sucesso!");
	}
	
	@Override
    public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		if (mysqlSetup.getConnection() != null) {
			edH.onShutDownDataSave();
			mysqlSetup.closeConnection();
		}
		log.info(pluginName + " está desativado!");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vault = rsp.getProvider();
        log.info("Usando o sistema econômico:" + rsp.getProvider().getName());
        return vault != null;
    }
	
	public ConfigHandler getConfigHandler() {
		return configHandler;
	}
	public MysqlSetup getMysqlSetup() {
		return mysqlSetup;
	}
	public EcoMysqlHandler getEcoMysqlHandler() {
		return ecoMysqlHandler;
	}
	public EcoDataHandler getEcoDataHandler() {
		return edH;
	}
	public BackgroundTask getBackgroundTask() {
		return bt;
	}

}
