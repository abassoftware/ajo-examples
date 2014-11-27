package de.abas.examples.infosystemcontrol;

import java.math.BigDecimal;

import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.event.ButtonEvent;
import de.abas.erp.axi2.event.FieldEvent;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.common.type.AbasDate;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.infosystem.custom.ow1.InventoryInfoystem;
import de.abas.erp.db.infosystem.custom.ow1.InventoryInfoystem.Row;
import de.abas.erp.db.schema.custom.inventorycounter.InventoryCounter;
import de.abas.erp.db.schema.custom.inventorycounter.InventoryCounterEditor;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;
import de.abas.erp.db.settings.DisplayMode;
import de.abas.erp.db.type.AbasUnit;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

/**
 * The InventoryInfosystemEventHandler handles all registered events for the infosystem INVENTORY.
 * 
 * The infosystem INVENTORY can either create InventoryCounter objects, which store information about an article's stock quantity or display
 * InventoryCounter objects as table rows according to filter criteria such as team and date range.
 * 
 * The infosystem INVENTORY is a custom infosystem. It refers to InventoryCounter objects. The InventoryCounter database is a custom database.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
@EventHandler(head = InventoryInfoystem.class, row = InventoryInfoystem.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class InventoryInfoystemEventHandler {

	/**
	 * Stores data entered in the InventoryInfosystem such as team, article, quantity, unit and current date in the InventoryCounter database.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The InventoryInfosystem instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "commitqty", type = ButtonEventType.AFTER)
	public void commitqtyAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, InventoryInfoystem head) throws EventException {
		// gets data from InventoryInfosystem
		Product article = head.getArticle();
		int team = head.getTeam();
		BigDecimal quantity = head.getQty();
		AbasUnit unit = head.getUnit();

		// uses gathered data to create a new InventoryCounter object
		InventoryCounterEditor inventoryInstance = ctx.newObject(InventoryCounterEditor.class);
		inventoryInstance.setSwd("Inventory2014");
		inventoryInstance.setYteam(team);
		inventoryInstance.setYarticle(article);
		inventoryInstance.setYqty(quantity);
		inventoryInstance.setYwarehouseunit(unit);
		inventoryInstance.setYdate(new AbasDate());
		inventoryInstance.commit();

		// displays success message
		TextBox textBox = new TextBox(ctx, "Committed", "Data is secured!");
		textBox.show();
	}

	/**
	 * Shows the quantity of the article entered as stored in the InventoryCounter database.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The InventoryInfosystem instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "showqty", type = ButtonEventType.AFTER)
	public void showqtyAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, InventoryInfoystem head) throws EventException {
		// gets team and date from first tab
		int team = head.getTeam();
		AbasDate abasDate = new AbasDate();

		// initializes counter for table rows
		int counter = 1;

		// selects all data sets for the selected team and date entered in the InventoryCounter
		String selectionString = "idno=;swd=;descr==`!;yteam=" + team + "!" + team + ";ydate=" + abasDate + "!" + abasDate + ";@englvar=(Yes)";
		Selection<InventoryCounter> selection = ExpertSelection.create(InventoryCounter.class, selectionString);
		Query<InventoryCounter> createQuery = ctx.createQuery(selection);

		// sets display mode to display unit as text
		// this only works when getting the AbasUnit value with getString()
		ctx.getSettings().setDisplayMode(DisplayMode.DISPLAY);

		// resets table
		head.table().clear();

		// loads one row in table for each InventoryCounter object that was selected previously
		for (InventoryCounter inventoryCounter : createQuery) {

			Product article = inventoryCounter.getYarticle();
			String warehouseunit = inventoryCounter.getString("ywarehouseunit");
			int teamFromDatabase = inventoryCounter.getYteam();

			BigDecimal purchPriceArticle = inventoryCounter.getYarticle().getPurchPrice();
			BigDecimal quantity = inventoryCounter.getYqty();
			String purchDescrOperLang = inventoryCounter.getYarticle().getPurchDescrOperLang();

			Row appendRow = head.table().appendRow();

			appendRow.setItem(counter++);
			appendRow.setTarticle(article);
			appendRow.setDate(inventoryCounter.getYdate());
			appendRow.setTteam(teamFromDatabase);
			appendRow.setTqty(quantity);
			appendRow.setString("tunit", warehouseunit);
			appendRow.setTarticledescr(purchDescrOperLang);
			appendRow.setTpurchprice(purchPriceArticle);
			appendRow.setTwarehousevalue(purchPriceArticle.multiply(quantity));

		}
		// positions the cursor on the table tab
		screenControl.moveCursor(head, InventoryInfoystem.META.teamfrom);
	}

	/**
	 * Loads table of InventoryInfosystem by getting all objects from InventoryCounter database that fit the filter criteria.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The InventoryInfosystem instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
	public void startAfter(ButtonEvent event, ScreenControl screenControl, DbContext ctx, InventoryInfoystem head) throws EventException {
		int teamFrom = head.getTeamfrom();
		int teamTo = head.getTeamto();
		AbasDate dateFrom = head.getDatefrom();
		AbasDate dateTo = head.getDateto();

		if ((dateFrom != null) && (dateTo != null)) {
			// initializes counter for table rows
			int counter = 1;

			// selects all data sets for the selected team and date range entered in the filter criteria
			String selectionString = "idno=;swd=;descr==`!;yteam=" + teamFrom + "!" + teamTo + ";ydate=" + dateFrom + "!" + dateTo + ";@englvar=(Yes)";
			Selection<InventoryCounter> selection = ExpertSelection.create(InventoryCounter.class, selectionString);
			Query<InventoryCounter> createQuery = ctx.createQuery(selection);

			// sets display mode to display unit as text
			// this only works when getting the AbasUnit value with getString()
			ctx.getSettings().setDisplayMode(DisplayMode.DISPLAY);

			// resets table
			head.table().clear();

			// loads one row in table for each InventoryCounter object that was selected previously
			for (InventoryCounter inventoryCounter : createQuery) {

				Product article = inventoryCounter.getYarticle();
				AbasUnit warehouseUnit = inventoryCounter.getYwarehouseunit();
				int team = inventoryCounter.getYteam();

				BigDecimal purchPriceFromArticle = inventoryCounter.getYarticle().getPurchPrice();
				BigDecimal quantity = inventoryCounter.getYqty();
				String purchDescrOperLang = inventoryCounter.getYarticle().getPurchDescrOperLang();

				Row appendRow = head.table().appendRow();

				appendRow.setItem(counter++);
				appendRow.setTarticle(article);
				appendRow.setDate(inventoryCounter.getYdate());
				appendRow.setTteam(team);
				appendRow.setTqty(quantity);
				appendRow.setTunit(warehouseUnit.toString());
				appendRow.setTarticledescr(purchDescrOperLang);
				appendRow.setTpurchprice(purchPriceFromArticle);
				appendRow.setTwarehousevalue(purchPriceFromArticle.multiply(quantity));

				BigDecimal tmpWarehouseValue = purchPriceFromArticle.multiply(quantity);
				tmpWarehouseValue.add(tmpWarehouseValue);

			}
		}
	}

	/**
	 * Gets unit from article master file, when article is entered.
	 * 
	 * @param event The event that occurred.
	 * @param screenControl The ScreenControl instance.
	 * @param ctx The database context.
	 * @param head The InventoryInfosystem instance.
	 * @throws EventException The exception thrown if an error occurs.
	 */
	@FieldEventHandler(field = "article", type = FieldEventType.EXIT)
	public void articleExit(FieldEvent event, ScreenControl screenControl, DbContext ctx, InventoryInfoystem head) throws EventException {
		head.setUnit(head.getArticle().getSU());
	}

}
