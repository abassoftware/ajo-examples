package de.abas.examples.partnerday15;

import de.abas.erp.api.gui.InputBox;
import de.abas.erp.db.DbContext;
import de.abas.examples.common.AbstractAjoAccess;

public class InputBoxExample extends AbstractAjoAccess {
	
	@Override
	public void run() {
		DbContext ctx = getDbContext();
		
		InputBox inputBox = new InputBox(ctx, "Normal Text goes here:");
		String input = inputBox.read();
		ctx.out().println("You entered: " + input);
		
		InputBox viewProtectedInputBox = new InputBox(ctx, "View Protected Text goes here:");
		String viewProtectedInput = viewProtectedInputBox.read(true);
		ctx.out().println("It was view protected, but I can still read it. You entered: " + viewProtectedInput);
	}
	
}
