package de.abas.examples.eventhandler;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.event.ScreenEvent;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.common.type.AbasDateTime;
import de.abas.erp.common.type.AbasDuration;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.sales.PackingSlipEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.examples.utilities.AbasDateUtilities;

/**
 * The PackingSlipEventHandler handles events occurring in a packing slip object (database 3:23).
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
@EventHandler(head = PackingSlipEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class PackingSlipEventHandler {

	/**
	 * Uses the warranty period of a product to calculate the warranty date for each product in the table rows of the packing slip.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The PackingSlipEditor instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(ScreenEvent event, ScreenControl screenControl, DbContext ctx, PackingSlipEditor head) throws EventException {
		// Gets all rows of the PackingSlipEditor object
		Iterable<PackingSlipEditor.Row> editableRows = head.table().getEditableRows();
		// Gets the PackingSlipEditor's creation date
		AbasDate dateFrom = head.getDateFrom();

		TextBox box = new TextBox(ctx, "ScreenEnter", "running...");
		box.show();

		// Iterates the table rows
		for (PackingSlipEditor.Row row : editableRows) {

			// gets the object of the database part from each table row
			SelectablePart selectablePart = row.getProduct();
			// casts the object of the database part to Product if it is a product
			if (selectablePart instanceof Product) {
				Product product = (Product) selectablePart;

				// gets the warranty period of this product from the product master file
				AbasDuration warrantyPer = product.getWarrantyPer();
				// checks whether the warranty period is maintained
				if (warrantyPer != null) {
					// calculates the warranty date
					AbasDate warrantyDateDate = calculateWarrantyDateUtilities(ctx, dateFrom, warrantyPer);
					// stores the warranty date in the table row
					row.setYtwadate(warrantyDateDate);
				}
			}
		}
	}

	/**
	 * Uses the AbasDateUtility class to add the warranty period to the packaging slip's creation date. Then calls the getCalculatedWarrantyDate
	 * method to calculate the first day of the next month based on the previously calculated exact warranty date.
	 * 
	 * @param ctx The database context.
	 * @param dateFrom The packaging slip's creation date.
	 * @param warrantyPer The warranty period to add to calculate the warranty date.
	 * @return The real warranty date.
	 */
	private AbasDate calculateWarrantyDateUtilities(DbContext ctx, AbasDate dateFrom, AbasDuration warrantyPer) {
		// creates instance of helper class AbasDateUtilities
		AbasDateUtilities abasDateUtilities = new AbasDateUtilities();
		// converts dateFrom from AbasDate to AbasDateTime
		AbasDateTime abasDateTimeFrom = new AbasDateTime(dateFrom.toDate());
		// adds the warranty period to calculate the exact warranty end date
		AbasDate calculatedAbasDate = abasDateUtilities.addDuration(ctx, abasDateTimeFrom, warrantyPer);
		// calculates and returns the real warranty end date, which is the first day of the next month
		AbasDate calculatedWarrantyAbasDate = getCalculatedWarrantyDate(calculatedAbasDate);
		return calculatedWarrantyAbasDate;
	}

	/**
	 * Calculates the real warranty date by getting the first day of the next month based on the exact warranty date.
	 * 
	 * @param calculatedAbasDate The exact warranty date.
	 * @return The real warranty date.
	 */
	private AbasDate getCalculatedWarrantyDate(AbasDate calculatedAbasDate) {
		// creates a Calendar instance for German locale
		Calendar calendar = GregorianCalendar.getInstance(Locale.GERMANY);
		// initializes Calendar instance using the exact warranty date
		calendar.setTime(calculatedAbasDate.toDate());
		// increases month by 1
		calendar.add(Calendar.MONTH, 1);
		// sets day to the first of the month
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		// converts the Calendar instance's current time to AbasDate and returns it
		return new AbasDate(calendar.getTime());
	}

}
