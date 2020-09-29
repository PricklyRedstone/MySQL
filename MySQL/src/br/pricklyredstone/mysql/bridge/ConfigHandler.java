package br.pricklyredstone.mysql.bridge;

import java.io.File;

public class ConfigHandler {
	
	private Eco eco;
	
	public ConfigHandler(Eco eco) {
		this.eco = eco;
		loadConfig();
	}
	
	public void loadConfig() {
		File pluginFolder = new File("plugins" + System.getProperty("file.separator") + Eco.pluginName);
		if (pluginFolder.exists() == false) {
    		pluginFolder.mkdir();
    	}
		File configFile = new File("plugins" + System.getProperty("file.separator") + Eco.pluginName + System.getProperty("file.separator") + "config.yml");
		if (configFile.exists() == false) {
			Eco.log.info("Nenhum arquivo de config encontrado! Criando um novo...");
			eco.saveDefaultConfig();
		}
    	try {
    		Eco.log.info("Carregando o arquivo de config...");
    		eco.getConfig().load(configFile);
    	} catch (Exception e) {
    		Eco.log.severe("Não foi possível carregar o arquivo de config! Você precisa regenerar o config! Erro: " + e.getMessage());
			e.printStackTrace();
    	}
	}
	
	public String getString(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Não foi possível localizar " + key + " no config.yml dentro do " + Eco.pluginName + " Pasta! (Tente gerar um novo excluindo a corrente)");
			return "errorCouldNotLocateInConfigYml:" + key;
		} else {
			return eco.getConfig().getString(key);
		}
	}
	
	public Integer getInteger(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Não foi possível localizar " + key + " no config.yml dentro do " + Eco.pluginName + " Pasta! (Tente gerar um novo excluindo a corrente)");
			return null;
		} else {
			return eco.getConfig().getInt(key);
		}
	}
	
	public Boolean getBoolean(String key) {
		if (!eco.getConfig().contains(key)) {
			eco.getLogger().severe("Não foi possível localizar " + key + " no config.yml dentro do " + Eco.pluginName + " Pasta! (Tente gerar um novo excluindo a corrente)");
			return null;
		} else {
			return eco.getConfig().getBoolean(key);
		}
	}

}
