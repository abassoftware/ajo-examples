package de.abas.examples.common;

import java.io.FileWriter;
import java.io.IOException;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.DbMessage;
import de.abas.erp.db.MessageListener;

/**
 * Utility method for getting the client and/or server context.
 *
 * @author abas Software AG
 *
 */
public abstract class AbstractAjoAccess implements ContextRunnable {

	/**
	 * Context mode enumeration.
	 *
	 * @author abas Software AG
	 *
	 */
	public enum ContextMode {
		UNDEFINED {
			@Override
			public String toString() {
				return "";
			}
		},
		SERVER_MODE {
			@Override
			public String toString() {
				return "server-mode";
			}
		},
		CLIENT_MODE {
			@Override
			public String toString() {
				return "client-mode";
			}
		}
	}

	private ConnectionProvider connectionProvider = new ConnectionProvider();
	private FileWriter fileWriterLogging;

	// Initialize DbContext
	private DbContext dbContext = null;
	private ContextMode mode = ContextMode.UNDEFINED;

	/**
	 * Adds a default message listener. Gets all text, status and error messages.
	 */
	public void addDefaultMessageListener() {
		getDbContext().addMessageListener(new MessageListener() {
			// Displays all text, status and error messages
			@Override
			public void receiveMessage(DbMessage message) {
				getDbContext().out().println("|" + message + "|");
			}

		});
	}

	/**
	 * Gets the database context. Create a client context if dbContext == null
	 *
	 * @return The database context.
	 */
	public DbContext getDbContext() {
		if (dbContext == null) {
			dbContext =
					connectionProvider.createDbContext(this.getClass()
							.getSimpleName());
			mode = ContextMode.CLIENT_MODE;
			if (connectionProvider.edpLog) {
				enableLogging();
			}
			addDefaultMessageListener();
		}
		return dbContext;
	}

	/**
	 * Returns the host name.
	 *
	 * @return The host name.
	 */
	public String getHostname() {
		return connectionProvider.hostname;
	}

	/**
	 * Returns the client.
	 *
	 * @return The client.
	 */
	public String getMandant() {
		return connectionProvider.mandant;
	}

	/**
	 * Returns the context mode.
	 *
	 * @return The context mode.
	 */
	public String getMode() {
		return mode.toString();
	}

	/**
	 * Returns the port.
	 *
	 * @return The port.
	 */
	public int getPort() {
		return connectionProvider.port;
	}

	/**
	 * Abstract run method to implement in all derived classes.
	 *
	 * @param args
	 */
	public abstract int run(String[] args);

	/**
	 * Runs program in client mode. Creates client context if dbContext==null
	 *
	 * @param args
	 */
	public final void runClientProgram(String[] args) {
		run(args);
		disableLogging();
		getDbContext().close();
	}

	// server access: get server context. Initialize mode
	@Override
	public int runFop(FOPSessionContext fopSessionContext, String[] args)
			throws FOPException {
		dbContext = fopSessionContext.getDbContext();
		mode = ContextMode.SERVER_MODE;
		addDefaultMessageListener();
		return run(args);
	}

	/**
	 * Disables EDP logging
	 */
	private void disableLogging() {
		if (null != fileWriterLogging) {
			try {
				fileWriterLogging.close();
			}
			catch (IOException e) {
				getDbContext().out().println(e.getMessage());
			}
			finally {
				fileWriterLogging = null;
			}
		}
		getDbContext().setLogger(null);
	}

	/**
	 * Enables EDP logging. Creates a log file (name is class name).
	 */
	private final void enableLogging() {
		enableLogging(getClass().getSimpleName() + ".log");
	}

	/**
	 * Enables EDP logging. Creates a log file (name as defined in parameter).
	 *
	 * @param fileName Name of log file.
	 */
	private void enableLogging(String fileName) {
		try {
			fileWriterLogging = new FileWriter(fileName);
			getDbContext().setLogger(fileWriterLogging);
		}
		catch (IOException e) {
			getDbContext().out().println(e.getMessage());
		}
	}
}