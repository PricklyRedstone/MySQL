package br.pricklyredstone.mysql.bridge;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.bukkit.Bukkit;

import net.craftersland.eco.bridge.Eco;

public class MysqlSetup {
	
	private Connection conn = null;
	private Eco eco;
	
	public MysqlSetup(Eco eco) {
		this.eco = eco;
		connectToDatabase();
		setupDatabase();
		updateTables();
		databaseMaintenanceTask();
	}
	
	public void connectToDatabase() {
		Eco.log.info("Conectando-se ao banco de dados...");
		try {
            Class.forName("com.mysql.jdbc.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", eco.getConfigHandler().getString("database.mysql.user"));
            properties.setProperty("password", eco.getConfigHandler().getString("database.mysql.password"));
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            properties.setProperty("requireSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            
            conn = DriverManager.getConnection("jdbc:mysql://" + eco.getConfigHandler().getString("database.mysql.host") + ":" + eco.getConfigHandler().getString("database.mysql.port") + "/" + eco.getConfigHandler().getString("database.mysql.databaseName"), properties);
           
          } catch (ClassNotFoundException e) {
        	  Eco.log.severe("Não consegui localizar motoristas para mysql! Erro: " + e.getMessage());
            return;
          } catch (SQLException e) {
        	  Eco.log.severe("Não conseguiu se conectar ao banco de dados mysql! Erro: " + e.getMessage());
            return;
          }
		Eco.log.info("Conexão de banco de dados bem sucedida!");
	}
	
	public void setupDatabase() {
		if (conn == null) return;
		PreparedStatement query = null;
	      try {	        
	        String data = "CREATE TABLE IF NOT EXISTS `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` (id int(10) AUTO_INCREMENT, player_uuid varchar(50) NOT NULL UNIQUE, player_name varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL, money double(30,2) NOT NULL, sync_complete varchar(5) NOT NULL, last_seen varchar(30) NOT NULL, PRIMARY KEY(id));";
	        query = conn.prepareStatement(data);
	        query.execute();
	      } catch (SQLException e) {
	        e.printStackTrace();
	        Eco.log.severe("Erro criando tabelas! Erro: " + e.getMessage());
	      } finally {
	    	  try {
	    		  if (query != null) {
	    			  query.close();
	    		  }
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
	}
	
	public Connection getConnection() {
		checkConnection();
		return conn;
	}
	
	public void checkConnection() {
		try {
			if (conn == null) {
				Eco.log.warning("A conexão falhou. Reconectar...");
				reConnect();
			}
			if (!conn.isValid(3)) {
				Eco.log.warning("A conexão está ociosa ou terminada. Reconectar...");
				reConnect();
			}
			if (conn.isClosed() == true) {
				Eco.log.warning("A conexão está fechada. Reconectar...");");
				reConnect();
			}
		} catch (Exception e) {
			Eco.log.severe("Could not reconnect to Database! Error: " + e.getMessage());
		}
	}
	
	public boolean reConnect() {
		try {            
            long start = 0;
			long end = 0;
			
		    start = System.currentTimeMillis();
		    Eco.log.info("Tentando estabelecer uma conexão com o servidor MySQL!");
            Class.forName("com.mysql.jdbc.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", eco.getConfigHandler().getString("database.mysql.user"));
            properties.setProperty("password", eco.getConfigHandler().getString("database.mysql.password"));
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            properties.setProperty("requireSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            conn = DriverManager.getConnection("jdbc:mysql://" + eco.getConfigHandler().getString("database.mysql.host") + ":" + eco.getConfigHandler().getString("database.mysql.port") + "/" + eco.getConfigHandler().getString("database.mysql.databaseName"), properties);
		    end = System.currentTimeMillis();
		    Eco.log.info("Conexão com servidor MySQL estabelecido em " + ((end - start)) + " ms!");
            return true;
		} catch (Exception e) {
			Eco.log.severe("Conexão com servidor MySQL estabelecido em " + e.getMessage());
			return false;
		}
	}
	
	public void closeConnection() {
		try {
			Eco.log.info("Fechando a conexão do banco de dados...");
			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTables() {
		if (conn != null) {
			DatabaseMetaData md = null;
	    	ResultSet rs1 = null;
	    	PreparedStatement query1 = null;
	    	try {
	    		md = conn.getMetaData();
	    		rs1 = md.getColumns(null, null, eco.getConfigHandler().getString("database.mysql.dataTableName"), "sync_complete");
	            if (rs1.next()) {
			    	
			    } else {
			        String data = "ALTER TABLE `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` ADD sync_complete varchar(5) NOT NULL DEFAULT 'true';";
			        query1 = conn.prepareStatement(data);
			        query1.execute();
			    }
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	} finally {
	    		try {
	    			if (query1 != null) {
	    				query1.close();
	    			}
	    			if (rs1 != null) {
	    				rs1.close();
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
		}
	}
	
	private void databaseMaintenanceTask() {
		if (eco.getConfigHandler().getBoolean("database.removeOldAccounts.enabled") == true) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(eco, new Runnable() {

				@Override
				public void run() {
					if (conn != null) {
						long inactivityDays = Long.parseLong(eco.getConfigHandler().getString("database.removeOldAccounts.inactivity"));
						long inactivityMils = inactivityDays * 24 * 60 * 60 * 1000;
						long curentTime = System.currentTimeMillis();
						long inactiveTime = curentTime - inactivityMils;
						Eco.log.info("A tarefa de manutenção do banco de dados começou...");
						PreparedStatement preparedStatement = null;
						try {
							String sql = "DELETE FROM `" + eco.getConfigHandler().getString("database.mysql.dataTableName") + "` WHERE `last_seen` < ?";
							preparedStatement = conn.prepareStatement(sql);
							preparedStatement.setString(1, String.valueOf(inactiveTime));
							preparedStatement.execute();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								if (preparedStatement != null) {
									preparedStatement.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						Eco.log.info("Manutenção do banco de dados completa!");
					}
				}
				
			}, 100 * 20L);
		}
	}

}
