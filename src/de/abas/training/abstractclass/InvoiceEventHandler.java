package de.abas.training.abstractclass;

import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.enums.EnumEditorAction;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.SelectableRow;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.db.schema.sales.InvoiceEditor;
import de.abas.erp.db.schema.sales.InvoiceEditor.Row;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;

@EventHandler(head = InvoiceEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class InvoiceEventHandler {

	@ScreenEventHandler(type = ScreenEventType.VALIDATION)
	public void screenValidation(ScreenEvent event, ScreenControl screenControl, DbContext ctx,InvoiceEditor head) throws EventException {
		// gets all editable rows
		Iterable<Row> editableRows = head.table().getEditableRows();
		// does not do anything if in screen mode view or delete
		if(event.getCommand() == EnumEditorAction.View || event.getCommand() == EnumEditorAction.Delete) {
			return;
		}
		int index = 1;
		SalesHelperClass salesHelperClass = new SalesHelperClass();
		for (Row row : editableRows) {
			salesHelperClass.handleRow(ctx, (SelectableRow)row, index++);
		}
	}

}
