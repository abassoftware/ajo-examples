package de.abas.training.infosystemcontrol;

import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.db.infosystem.custom.ow1.InfosystemVARNAMELIST;
import de.abas.erp.db.schema.company.Vartab;
import de.abas.erp.db.schema.company.Vartab.Row;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.event.ButtonEvent;

/**
 * The InfosystemVarnamelistEventHandler handles all registered events for the infosystem VARNAMELIST.
 * 
 * The infosystem VARNAMELIST displays all German variable names and their according English names of the variable table entered as filter criterion
 * in the head of the infosystem.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
@EventHandler(head = InfosystemVARNAMELIST.class, row = InfosystemVARNAMELIST.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class InfosystemVarnamelistEventHandler {

	/**
	 * If the start button of the infosystem VARNAMELIST is pressed, after checking a variable table was selected as basis for the selection, the
	 * German and English variable names of all variables of this variable table are loaded as table rows.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance
	 * @param ctx The database context.
	 * @param head The InfosystemVARNAMELIST instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, InfosystemVARNAMELIST head) throws EventException {
		// gets the variable table that was entered as the filter criterion
		Vartab vartab = head.getYvartab();
		// only tries to load all variables if a variable table was entered as filter criterion
		if (vartab != null) {
			// resets the table
			head.table().clear();
			// gets all table rows of the selected variable table
			Iterable<Row> rows = vartab.table().getRows();
			// creates a infosystem table row for each table row of the variable table containing the German and English variable name
			for (Row row : rows) {
				de.abas.erp.db.infosystem.custom.ow1.InfosystemVARNAMELIST.Row appendRow = head.table().appendRow();
				appendRow.setTyengname(row.getVarNameEnglish());
				appendRow.setTygername(row.getVarName());
			}
		}
	}

}
