package de.abas.examples.guicommand;

import de.abas.erp.api.AppContext;
import de.abas.erp.api.commands.CommandFactory;
import de.abas.erp.api.commands.DatabaseCommand;
import de.abas.erp.api.commands.FieldManipulator;
import de.abas.erp.common.type.enums.EnumFileActions;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;
import de.abas.examples.common.AbstractAjoAccess;

/**
 * This class shows how to open GUI screens using AJO.
 * 
 * Attention: This only works in server mode.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class AccessGuiCommandAjo extends AbstractAjoAccess{
	
	@Override
	public void run(String[] args) {
		DbContext dbContext = getDbContext();
	    Selection<CustomerEditor> selection = ExpertSelection.create(CustomerEditor.class, "");
	    
	    CommandFactory commandFactory = AppContext.createFor(dbContext).getCommandFactory();
	      
	    FieldManipulator<CustomerEditor> fieldManipulator = commandFactory.getScrParamBuilder(CustomerEditor.class);
	    fieldManipulator.setField(CustomerEditor.META.responsOperator, "me");

	    DatabaseCommand databaseCommand = commandFactory.buildDbCommand(selection, EnumFileActions.Edit, "");
	    databaseCommand.run();
	}
}
