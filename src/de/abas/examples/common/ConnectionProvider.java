package de.abas.examples.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.util.ContextHelper;

public class ConnectionProvider {
	public String hostname;
	public String mandant;
	public String password;
	public int port;
	public boolean edpLog;

	public ConnectionProvider() {
	}

	public DbContext createDbContext(String name) {
		loadProperties();
		return ContextHelper.createClientContext(hostname, port, mandant, password,
				name);
	}

	private void loadProperties() {
		Properties pr = new Properties();
		File configFile = new File("ajo-access.properties");
		try {
			pr.load(new FileReader(configFile));
			hostname = pr.getProperty("hostname");
			mandant = pr.getProperty("mandant");
			port = Integer.parseInt(pr.getProperty("port", "6550"));
			password = pr.getProperty("password");
			edpLog = Boolean.parseBoolean(pr.getProperty("edpLog", "false"));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find configuration file "
					+ configFile.getAbsolutePath());
		}
		catch (IOException e) {
			throw new RuntimeException("Could not load configuration file "
					+ configFile.getAbsolutePath());
		}
	}
}