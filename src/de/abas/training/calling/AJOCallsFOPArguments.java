package de.abas.training.calling;

import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.db.DbContext;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abas.training.common.AbstractAjoAccess;

public class AJOCallsFOPArguments extends AbstractAjoAccess {

	@Override
	public void run() {
		DbContext dbContext = getDbContext();
		dbContext.out().println("JFOP running ...");

		// gets the U buffer
		// BufferFactory.newInstance(false) => FO commands German
		// BufferFactory.newInstance(true) => FO commands English
		UserTextBuffer userTextBuffer = BufferFactory.newInstance(false).getUserTextBuffer();
		
		// instead of initializing the variables one by one
		
		// String varname = null;
		// varname = "xiNumber1";
		// if (!userTextBuffer.isVarDefined(varname)) {
		// userTextBuffer.defineVar("int", varname);
		// }
		// varname = "xiNumber2";
		// if (!userTextBuffer.isVarDefined(varname)) {
		// userTextBuffer.defineVar("int", varname);
		// }
		// varname = "xiResult";
		// if (!userTextBuffer.isVarDefined(varname)) {
		// userTextBuffer.defineVar("int", varname);
		// }
		
		// uses a method to initialize the variables
		initializeUBufferVariables(userTextBuffer, "int", "xiNumber1");
		initializeUBufferVariables(userTextBuffer, "int", "xiNumber2");
		initializeUBufferVariables(userTextBuffer, "int", "xiResult");
		
		// assigns variables
		userTextBuffer.assign("xiNumber1", 7);
		userTextBuffer.assign("xiNumber2", 7);
		userTextBuffer.assign("xiResult", 0);
		
		// calls FOP FOP.CALLED.BY.AJO.CLASS:
		//
		// ..!interpreter english noabbrev
		// .. FOP.CALLED.BY.AJO.CLASS
		// FOP is processed ...
		// .formula U|xiResult = U|xiNumber1 + U|xiNumber2
		// .continue
		FOe.input("FOP.CALLED.BY.AJO.CLASS");
		
		// gets content of xiResult from U buffer and outputs it
		int result = userTextBuffer.getIntegerValue("xiResult");
		dbContext.out().println("AJO class still running ...");
		dbContext.out().println("xiResult: " + result);
	}

	/**
	 * Initializes variables in U buffer.
	 * 
	 * @param userTextBuffer The U buffer instance.
	 * @param type The type of variable to initialize.
	 * @param varname The name of the variable.
	 */
	private void initializeUBufferVariables(UserTextBuffer userTextBuffer, String type, String varname) {
		if (!userTextBuffer.isVarDefined(varname)) {
			userTextBuffer.defineVar(type, varname);
		}
	}

}
