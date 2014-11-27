package de.abas.examples.eventhandler;

import de.abas.eks.jfop.remote.FO;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

/**
 * The CustomerEventHandler handles events occurring in a customer object (database 0:1).
 * 
 * This class shows the usage of EventExcption.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
@EventHandler(head = CustomerEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class CustomerEventHandler {

	/**
	 * When the customer screen is entered, a input window with custom input message is shown. After inserting some text the input is displayed in a
	 * TextBox and it is determined whether the input was OK or Error. This class is supposed to show the usage of the EventException.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The CustomerEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, CustomerEditor head) throws EventException {
		// creates input window with custom input message
		String[] customInputMessage = new String[1];
		customInputMessage[0] = "Input (Error/OK)";
		String lesen = FO.lesen(customInputMessage);

		// displays TextBox with input
		TextBox textBox = new TextBox(ctx, "Input was: ", lesen);
		textBox.show();

		if (!lesen.equals("Error")) {
			// shows TextBox with abas error message
			// throw new EventException(0);
			
			// does not show TextBox
			// throw new EventException("", 0);
			
			// shows TextBox with custom error message
			throw new EventException("All is well!", 0);
		}
		else {
			// shows TextBox with abas error message
			// throw new EventException(1);
			
			// does not show TextBox
			// throw new EventException("", 1);
			
			// shows TextBox with custom error message
			throw new EventException("Problems", 1);
		}
	}

}
