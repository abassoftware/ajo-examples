package de.abas.examples.eventhandler;

import java.util.Date;

import de.abas.eks.jfop.annotation.Stateful;
import de.abas.erp.api.gui.ButtonSet;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.enums.EnumDialogBox;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

@EventHandler(head = CustomerEditor.class)
@RunFopWith(EventHandlerRunner.class)
@Stateful
public class StatefulCustomerEventHandler {
	
	private String emailAddr;

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(CustomerEditor head) throws EventException {
		emailAddr = head.getEmailAddr();
	}
	
	@FieldEventHandler(field = "telexAddr", type = FieldEventType.EXIT)
	public void emailAddrValidation(DbContext ctx, CustomerEditor head) throws EventException {
		String emailAddress = head.getEmailAddr();
		if (!this.emailAddr.equals(emailAddress)) {
			addNote(ctx, head, emailAddress);
		}
	}

	private void addNote(DbContext ctx, CustomerEditor head, String emailAddress) {
		if (noteWanted(ctx, emailAddress)) {
			head.setComments(String.format("%s - email address updated from %s to %s", new Date(), this.emailAddr,
					emailAddress));
		}
	}

	private boolean noteWanted(DbContext ctx, String emailAddress) {
		EnumDialogBox boxResult = new TextBox(ctx, "Note", "Do you want to add a note?")
			.setButtons(ButtonSet.YES_NO)
			.show();
		if (boxResult.equals(EnumDialogBox.Yes)) {
			return true;
		}
		return false;
	}

}
