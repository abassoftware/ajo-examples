package de.abas.examples.abstractclass;

import java.util.Random;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.common.type.enums.EnumProcurementType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.SelectableRow;
import de.abas.erp.db.schema.account.CostObject;
import de.abas.erp.db.schema.account.CostObjectEditor;
import de.abas.erp.db.schema.account.SelectableAccount;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.SelectablePart;
import de.abas.erp.db.schema.part.SupplementaryItem;
import de.abas.erp.db.schema.sales.BlanketOrderEditor;
import de.abas.erp.db.schema.sales.InvoiceEditor;
import de.abas.erp.db.schema.sales.PackingSlipEditor;
import de.abas.erp.db.schema.sales.QuotationEditor;
import de.abas.erp.db.schema.sales.SalesOrderEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;

/**
 * This class is a helper class to randomly assign cost objects to table rows of packing slips and
 * invoices.
 * 
 * @author abas software AG
 * @version 1.1
 *
 */
public class SalesHelperClass {

	/**
	 * This method is used in the static block to checks whether the field costCenter is available.
	 * In order to be able to use this method in the static block this method has to be static, too.
	 * 
	 * @param expected The expected value.
	 * @param current The current value.
	 */
	private static void assertEquals(String expected, String current) {
		if (!expected.equals(current)) {
			throw new RuntimeException("Assertion failed. Expected: <" + expected + "> but was: <" + current + ">");
		}
	}

	/**
	 * This static block is executed whenever a new instance of SalesHelperClass is created.
	 * 
	 * It checks whether the field costCenter is available in the table row of the following
	 * screens: SalesOrderEditor.Row, QuotationEditor.Row, PackingSlipEditor.Row, InvoiceEditor.Row,
	 * BlanketOrderEditor.Row If the field costCenter is not available a RunTimeException is thrown.
	 */
	static {
		assertEquals(SalesOrderEditor.Row.META.costCenter.getName(), QuotationEditor.Row.META.costCenter.getName());
		assertEquals(QuotationEditor.Row.META.costCenter.getName(), PackingSlipEditor.Row.META.costCenter.getName());
		assertEquals(PackingSlipEditor.Row.META.costCenter.getName(), InvoiceEditor.Row.META.costCenter.getName());
		assertEquals(InvoiceEditor.Row.META.costCenter.getName(), BlanketOrderEditor.Row.META.costCenter.getName());
		// for testing purposes remove comment below to trigger Exception
		// assertEquals("costCenter", "costCenterX");
	}

	/**
	 * This method defines the cost object in the table row of invoice and packing slip.
	 * 
	 * @param ctx The database context.
	 * @param row The table row as instance of SelectableRow.
	 * @param index The index to output the row number.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	public void handleRow(DbContext ctx, SelectableRow row, int index) throws EventException {
		outputInfoMessageForQuotationOrSalesOrder(ctx, row, index);
		defineCostObjectForPackingSlipOrInvoice(ctx, row, index);
	}

	/**
	 * Creates a new cost object 100003 if it does not already exist.
	 * 
	 * @param ctx The database context.
	 * @param row The current table row.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void createNewCostObject(DbContext ctx, SelectableRow row) throws EventException {
		final String idno = "100003";
		// gets cost object
		final CostObject costObject = getSelectedCostObject(ctx, idno);
		if (costObject == null) {
			// creates cost object if previous selection returned null
			final CostObject newCostObject = createNewCostObject(ctx, "PROD3", "100003", "Production cost object");
			setCostCenter(row, newCostObject);
		}
		else {
			ctx.out().println("-----> cost object \"100003\" exists");
			setCostCenter(row, costObject);
		}
	}

	/**
	 * Creates a new instance of CostObject.
	 * 
	 * @param ctx The database context.
	 * @param swd The new object's search word.
	 * @param idno The new object's idno.
	 * @param descrOperLang The new object's description.
	 * @return The newly created object.
	 */
	private CostObject createNewCostObject(DbContext ctx, String swd, String idno, String descrOperLang) {
		final CostObjectEditor costObjectEditor = ctx.newObject(CostObjectEditor.class);
		costObjectEditor.setSwd(swd);
		costObjectEditor.setIdno(idno);
		costObjectEditor.setDescrOperLang(descrOperLang);
		costObjectEditor.commit();
		final CostObject newCostObject = costObjectEditor.objectId();

		return newCostObject;
	}

	/**
	 * Defines and assigns a cost object to the current table row if its a packing slip table row.
	 * 
	 * @param ctx The database context.
	 * @param row The current table row.
	 * @param index The index of the current row.
	 * @param random The Random object to create a random integer.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void defineCostObjectForPackingSlipOrInvoice(DbContext ctx, SelectableRow row, int index) throws EventException {
		if ((row instanceof PackingSlipEditor.Row) || (row instanceof InvoiceEditor.Row)) {
			final SelectablePart selectablePart = getPart(ctx, row);
			defineCostObjectForProduct(ctx, selectablePart, row);
			outputCostObjectForSupplementaryItem(ctx, selectablePart, row);
			if (!((selectablePart instanceof Product) || (selectablePart instanceof SupplementaryItem))) {
				ctx.out().println("other cost center");
			}
		}
	}

	/**
	 * Defines and assigns a cost object for products if table rows are either of instance
	 * PackingSlipEditor.Row or InvoiceEditor.Row.
	 * 
	 * @param ctx The database context.
	 * @param selectablePart The instance of SelectablePart.
	 * @param row The current table row.
	 * @return The product as SelectablePart.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void defineCostObjectForProduct(DbContext ctx, SelectablePart selectablePart, SelectableRow row) throws EventException {
		if (selectablePart instanceof Product) {
			final Product product = (Product) selectablePart;
			final SelectableAccount costCenter = getCostCenter(row);
			if (costCenter == null) {
				// gets procure mode
				final EnumProcurementType procureMode = product.getProcureMode();
				if (procureMode == EnumProcurementType.InhouseProduction) {
					randomlyDetermineCostObject(ctx, row);

				}
				else if (procureMode == EnumProcurementType.ExternalProcurement) {
					ctx.out().println("new cost center |ExternalProcurement| created and used");
				}
				else {
					ctx.out().println("new cost center created and used");
				}
			}
		}
	}

	/**
	 * Gets the value of the field costCenter according to whether the current table row is instance
	 * of PackingSlipEditor.Row or InvoiceEditor.Row.
	 * 
	 * @param row The current table row.
	 * @return The value of the field costCenter.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private SelectableAccount getCostCenter(SelectableRow row) throws EventException {
		SelectableAccount costCenter = null;
		if (row instanceof PackingSlipEditor.Row) {
			costCenter = ((PackingSlipEditor.Row) row).getCostCenter();
		}
		else if (row instanceof InvoiceEditor.Row) {
			costCenter = ((InvoiceEditor.Row) row).getCostCenter();
		}
		else {
			throw new EventException("Only PackingSlipEditor.Row and InvoiceEditor.Row objects accepted here.");
		}
		return costCenter;
	}

	/**
	 * Gets value of field product according to whether the current table row is an instance of
	 * QuotationEditor.Row, SalesOrderEditor.Row, PackingSlipEditor.Row or InvoiceEditor.Row.
	 * 
	 * @param ctx The database context.
	 * @param row The current table row.
	 * @return The value of the field product as instance of SelectablePart.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private SelectablePart getPart(DbContext ctx, SelectableRow row) throws EventException {
		if ((row instanceof PackingSlipEditor.Row) || (row instanceof InvoiceEditor.Row)) {
			return getPart(ctx, row, -1);
		}
		throw new EventException("Only PackingSlipEditor.Row and InvoiceEditor.Row objects accepted here.");
	}

	/**
	 * Gets value of field product according to whether the current table row is an instance of
	 * QuotationEditor.Row or InvoiceEditor.Row.
	 * 
	 * @param ctx The database context.
	 * @param row The current table row.
	 * @param index The index of the current row.
	 * @return The value of the field product as instance of SelectablePart.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private SelectablePart getPart(DbContext ctx, SelectableRow row, int index) throws EventException {
		SelectablePart selectablePart;
		if (row instanceof QuotationEditor.Row) {
			ctx.out().println("Row" + index + " ->editing quotationRow");
			selectablePart = ((QuotationEditor.Row) row).getProduct();
		}
		else if (row instanceof InvoiceEditor.Row) {
			ctx.out().println("Row" + index + " ->editing quotationRow");
			selectablePart = ((InvoiceEditor.Row) row).getProduct();
		}
		else if (row instanceof PackingSlipEditor.Row) {
			selectablePart = ((PackingSlipEditor.Row) row).getProduct();
		}
		else if (row instanceof InvoiceEditor.Row) {
			selectablePart = ((InvoiceEditor.Row) row).getProduct();
		}
		else {
			throw new EventException("Only PackingSlipEditor.Row and InvoiceEditor.Row objects accepted here.");
		}
		return selectablePart;
	}

	/**
	 * Gets the cost object with the specified idno.
	 * 
	 * @param dbContext The database context.
	 * @param idno The idno of the cost object to select.
	 * @return The instance of CostObject with the specified idno or null if no such CostObject
	 * instance exists.
	 */
	private CostObject getSelectedCostObject(DbContext dbContext, String idno) {
		final SelectionBuilder<CostObject> selectionBuilder = SelectionBuilder.create(CostObject.class);
		selectionBuilder.add(Conditions.eq(CostObject.META.idno, idno));
		final CostObject costObject = QueryUtil.getFirst(dbContext, selectionBuilder.build());
		return costObject;
	}

	/**
	 * Outputs an info message containing information about the cost center in use.
	 * 
	 * @param ctx The database context.
	 * @param selectablePart The instance of SelectablePart.
	 * @param row The current table row.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void outputCostObjectForSupplementaryItem(DbContext ctx, SelectablePart selectablePart, SelectableRow row) throws EventException {
		if (selectablePart instanceof SupplementaryItem) {
			ctx.out().println("cost center SupplementaryItem");
			final SelectableAccount costCenter = getCostCenter(row);
			outputInfoMessageForCostCenter(ctx, costCenter);
		}
	}

	/**
	 * Outputs an info message according to whether the field costCenter is empty or not.
	 * 
	 * @param ctx The database context.
	 * @param costCenter The value of the field costCenter which is an instance of
	 * SelectableAccount.
	 */
	private void outputInfoMessageForCostCenter(DbContext ctx, SelectableAccount costCenter) {
		if (costCenter != null) {
			ctx.out().println("use existing cost center");
		}
		else {
			ctx.out().println("create and use new cost center");
		}
	}

	/**
	 * Outputs an info message about whether or not the cost center field is filled if the current
	 * table row is a quotation table row.
	 * 
	 * @param ctx The database context
	 * @param row The current table row.
	 * @param index The index of the current row.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void outputInfoMessageForQuotationOrSalesOrder(DbContext ctx, SelectableRow row, int index) throws EventException {
		if ((row instanceof QuotationEditor.Row) || (row instanceof SalesOrderEditor.Row)) {
			final SelectablePart selectablePart = getPart(ctx, row, index);
			if (selectablePart instanceof Product) {
				ctx.out().println("cost center Product");
				final SelectableAccount costCenter = getCostCenter(row);
				outputInfoMessageForCostCenter(ctx, costCenter);
			}
			else if (selectablePart instanceof SupplementaryItem) {
				ctx.out().println("cost center SupplementaryItem");
				final SelectableAccount costCenter = getCostCenter(row);
				outputInfoMessageForCostCenter(ctx, costCenter);
			}
			else {
				ctx.out().println("other cost center");
			}
		}
	}

	/**
	 * Uses a random integer between 0 and 2 to determine whether an existing cost object is used, a
	 * new one is created, or none is used.
	 * 
	 * @param ctx The database context.
	 * @param row The current table row.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void randomlyDetermineCostObject(DbContext ctx, SelectableRow row) throws EventException {
		ctx.out().println("new cost center |InhouseProduction| created and used");
		// creates cost object using a stream of three random integer numbers
		final int nextInt = new Random().nextInt(3);
		if (nextInt == 0) {
			useExistingCostObject(ctx, row);
		}
		else if (nextInt == 1) {
			createNewCostObject(ctx, row);
		}
		else if (nextInt == 2) {
			ctx.out().println("-----> no cost object used");
		}
	}

	/**
	 * Gets the value of the field costCenter according to whether the current table row is instance
	 * of PackingSlipEditor.Row or InvoiceEditor.Row.
	 * 
	 * @param row The current table row.
	 * @param costObject The CostObject instance to set the field costCenter.
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void setCostCenter(SelectableRow row, CostObject costObject) throws EventException {
		if (row instanceof PackingSlipEditor.Row) {
			((PackingSlipEditor.Row) row).setCostCenter(costObject);
		}
		else if (row instanceof InvoiceEditor.Row) {
			((InvoiceEditor.Row) row).setCostCenter(costObject);
		}
		else {
			throw new EventException("Only PackingSlipEditor.Row and InvoiceEditor.Row objects accepted here.");
		}
	}

	/**
	 * Uses the existing cost object 100001
	 * 
	 * @param ctx The database context.
	 * @param row The current table row
	 * @throws EventException Thrown if row is not instance of PackingSlipEditor.Row or
	 * InvoiceEditor.Row.
	 */
	private void useExistingCostObject(DbContext ctx, SelectableRow row) throws EventException {
		ctx.out().println("-----> existing cost object \"100001\" used");
		final CostObject costObject = getSelectedCostObject(ctx, "100001");
		setCostCenter(row, costObject);
	}
}
