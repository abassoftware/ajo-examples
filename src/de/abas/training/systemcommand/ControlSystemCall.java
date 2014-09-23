package de.abas.training.systemcommand;

import de.abas.erp.api.system.SystemCommand;
import de.abas.erp.db.DbContext;
import de.abas.training.common.AbstractAjoAccess;

/**
 * This class shows how to execute system commands on the abas server using AJO.
 * 
 * Caution: This currently only works in server mode!
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class ControlSystemCall extends AbstractAjoAccess {

	@Override
	public void run() {
		DbContext dbContext = getDbContext();
		
		// defines the command to execute
		 String cmd = "touch win/tmp/ControlSystemCall.txt";
		
		// runs command and gets exit code
		SystemCommand systemCommand = new SystemCommand(cmd, false);
		// if runVisible is set true a window is opened and the program waits for Return in order to exit
		// if run Visible is set false the command is executed in background (a window is still opened if anything is output).
		systemCommand.runVisible(false);
		int exitCode = systemCommand.getExitCode();
		
		// displays message according to exit code
		if (exitCode == 0) {
			dbContext.out().println("Command " + cmd + " executed successfully.");
		}
		else {
			dbContext.out().println("Command " + cmd + " could not be executed sucessfully.");
		}

	}

}
