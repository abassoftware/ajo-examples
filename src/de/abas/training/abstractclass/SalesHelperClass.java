package de.abas.training.abstractclass;

import java.util.Random;

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
 * @author jasc
 *
 */
public class SalesHelperClass {

	/**
	 * This static block is executed whenever a new instance of SalesHelperClass is created.
	 * 
	 * It checks whether the field costCenter is available in the table row of the following screens: SalesOrderEditor.Row, QuotationEditor.Row,
	 * PackingSlipEditor.Row, InvoiceEditor.Row, BlanketOrderEditor.Row If the field costCenter is not available a RunTimeException is thrown.
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
	 * This method is used in the static block to checks whether the field costCenter is available. In order to be able to use this method in the
	 * static block this method has to be static, too.
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
	 * This method defines the cost object in the table row of invoice and packing slip.
	 * 
	 * @param ctx The database context.
	 * @param row The table row as instance of SelectableRow.
	 * @param index The index to output the row number.
	 */
	public void handleRow(DbContext ctx, SelectableRow row, int index) {
		Random random = new Random();

		// only outputs info message for sales orders
		if (row instanceof SalesOrderEditor.Row) {
			ctx.out().println("Row" + index + " ->editing salesOrderRow");
			SalesOrderEditor.Row salesOrdereditorRow = (SalesOrderEditor.Row) row;
			SelectablePart selectablePart = salesOrdereditorRow.getProduct();
			if (selectablePart instanceof Product) {
				ctx.out().println("cost center Product");
				SelectableAccount costCenter = salesOrdereditorRow.getCostCenter();
				if (costCenter != null) {
					ctx.out().println("use existing cost center");
				}
				else {
					ctx.out().println("create and use new cost center");

				}
			}
			else if (selectablePart instanceof SupplementaryItem) {
				ctx.out().println("cost center SupplementaryItem");
				SelectableAccount costCenter = salesOrdereditorRow.getCostCenter();
				if (costCenter != null) {
					ctx.out().println("use exiting cost center");
				}
				else {
					ctx.out().println("create and use new cost center");
				}
			}
			else {
				ctx.out().println("other cost center");
			}
		}
		// only outputs info message for quotations
		else if (row instanceof QuotationEditor.Row) {
			QuotationEditor.Row quotationEditorRow = (QuotationEditor.Row) row;
			ctx.out().println("Row" + index + " ->editing quotationRow");
			SelectablePart selectablePart = quotationEditorRow.getProduct();
			if (selectablePart instanceof Product) {
				ctx.out().println("cost center Product");
				SelectableAccount costCenter = quotationEditorRow.getCostCenter();
				if (costCenter != null) {
					ctx.out().println("use existing cost center");
				}
				else {
					ctx.out().println("create and use new cost center");
				}
			}
			else if (selectablePart instanceof SupplementaryItem) {
				ctx.out().println("cost center SupplementaryItem");
				SelectableAccount costCenter = quotationEditorRow.getCostCenter();
				if (costCenter != null) {
					ctx.out().println("use existing cost center");
				}
				else {
					ctx.out().println("create and use new cost center");
				}
			}
			else {
				ctx.out().println("other cost center");
			}
		}
		// row is instance of InvoiceEditor.Row
		else if (row instanceof InvoiceEditor.Row) {
			InvoiceEditor.Row invoiceEditorRow = (InvoiceEditor.Row) row;
			// gets object from column 'Product'
			SelectablePart selectablePart = invoiceEditorRow.getProduct();
			// checks whether selected part is instance of Product
			if (selectablePart instanceof Product) {
				// casts instance of SelectablePart to Product
				Product product = (Product) selectablePart;
				// gets procure mode
				EnumProcurementType procureMode = product.getProcureMode();
				// gets cost center of current table row
				SelectableAccount costCenter = invoiceEditorRow.getCostCenter();
				if (costCenter == null) {
					// if procure mode is inhouse production
					if (procureMode == EnumProcurementType.InhouseProduction) {
						// creates cost object using a stream three of random integer numbers
						int nextInt = random.nextInt(3);
						if (nextInt == 0) {
							// if random integer in range between 0 and 2 returns 0 an existing cost object between 100001 and 100004 is used
							String tmpIdno = "10000" + (random.nextInt(4) + 1);
							ctx.out().println("-----> existing cost object \" " + tmpIdno + "\" used");
							CostObject costObject = getSelectedCostObject(ctx, tmpIdno);
							invoiceEditorRow.setCostCenter(costObject);
						}
						else if (nextInt == 1) {
							// if random integer returns 1 a new cost object is created if the defined one is not available
							String idno = "100004";
							// gets cost object
							CostObject costObject = getSelectedCostObject(ctx, idno);
							if (costObject == null) {
								// creates cost object if previous selection returned null
								CostObject newCostObject = createNewCostObject(ctx, "PROD4", "100004", "production cost object");
								invoiceEditorRow.setCostCenter(newCostObject);
							}
						}
						else if (nextInt == 2) {
							// if random integer returns 2 no cost object is used
							ctx.out().println("-----> no cost object used");
						}
					}
					// if procure mode is external procurement
					else if (procureMode == EnumProcurementType.ExternalProcurement) {
						ctx.out().println("new cost center |ExternalProcurement| created and used");
					}
					// if procure mode is neither inhouse production nor external procurement
					else {
						ctx.out().println("create and use new cost center");
					}
				}
			}
			// checks whether selected part is instance of SupplementaryItem
			else if (selectablePart instanceof SupplementaryItem) {
				ctx.out().println("cost center SupplementaryItem");
				SelectableAccount costCenter = invoiceEditorRow.getCostCenter();
				if (costCenter != null) {
					ctx.out().println("use existing cost center");
				}
				else {
					ctx.out().println("create and use new cost center");
				}
			}
			else {
				ctx.out().println("other cost center");
			}
		}
		// row is instance of PackingSlipEditor.Row
		else if (row instanceof PackingSlipEditor.Row) {
			PackingSlipEditor.Row packingSlipEditorRow = (PackingSlipEditor.Row) row;
			ctx.out().println("Row" + index + " ->editing packingSlipeRow");
			// gets object from column 'Product'
			SelectablePart selectablePart = packingSlipEditorRow.getProduct();
			// checks whether selected part is instance of Product
			if (selectablePart instanceof Product) {
				// casts instance of SelectablePart to Product
				Product product = (Product) selectablePart;
				// gets cost center of current table row
				SelectableAccount costCenter = packingSlipEditorRow.getCostCenter();
				if (costCenter == null) {
					// gets procure mode
					EnumProcurementType procureMode = product.getProcureMode();
					// if procure mode is inhouse production
					if (procureMode == EnumProcurementType.InhouseProduction) {
						ctx.out().println("new cost center |InhouseProduction| created and used");
						// creates cost object using a stream three of random integer numbers
						int nextInt = random.nextInt(3);
						if (nextInt == 0) {
							// if random integer in range between 0 and 2 returns 0 an existing cost object between 100001 and 100004 is used
							ctx.out().println("-----> existing cost object \"100001\" used");
							CostObject costObject = getSelectedCostObject(ctx, "100001");
							packingSlipEditorRow.setCostCenter(costObject);
						}
						else if (nextInt == 1) {
							// if random integer returns 1 a new cost object is created if the defined one is not available
							String idno = "100003";
							// gets cost object
							CostObject costObject = getSelectedCostObject(ctx, idno);
							if (costObject == null) {
								// creates cost object if previous selection returned null
								CostObject newCostObject = createNewCostObject(ctx, "PROD3", "100003", "Production cost object");
								packingSlipEditorRow.setCostCenter(newCostObject);
							}
							else {
								ctx.out().println("-----> cost object \"100003\" exists");
								packingSlipEditorRow.setCostCenter(costObject);
							}
						}
						else if (nextInt == 2) {
							// if random integer returns 2 no cost object is used
							ctx.out().println("-----> no cost object used");
						}

					}
					// if procure mode is external procurement
					else if (procureMode == EnumProcurementType.ExternalProcurement) {
						ctx.out().println("new cost center |ExternalProcurement| created and used");
					}
					else {
						// if procure mode is neither inhouse production nor external procurement
						ctx.out().println("new cost center created and used");
					}
				}
			}
			// checks whether selected part is instance of SupplementaryItem
			else if (selectablePart instanceof SupplementaryItem) {
				ctx.out().println("cost center SupplementaryItem");
				SelectableAccount costCenter = packingSlipEditorRow.getCostCenter();
				if (costCenter != null) {
					ctx.out().println("use existing cost center");
				}
				else {
					ctx.out().println("create and use new cost center");
				}
			}
			else {
				ctx.out().println("other cost center");
			}
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
		CostObjectEditor costObjectEditor = ctx.newObject(CostObjectEditor.class);
		costObjectEditor.setSwd(swd);
		costObjectEditor.setIdno(idno);
		costObjectEditor.setDescrOperLang(descrOperLang);
		costObjectEditor.commit();
		CostObject newCostObject = costObjectEditor.objectId();

		return newCostObject;
	}

	/**
	 * Gets the cost object with the specified idno.
	 * 
	 * @param dbContext The database context.
	 * @param idno The idno of the cost object to select.
	 * @return The instance of CostObject with the specified idno or null if no such CostObject instance exists.
	 */
	private CostObject getSelectedCostObject(DbContext dbContext, String idno) {
		SelectionBuilder<CostObject> selectionBuilder = SelectionBuilder.create(CostObject.class);
		selectionBuilder.add(Conditions.eq(CostObject.META.idno, idno));
		CostObject costObject = QueryUtil.getFirst(dbContext, selectionBuilder.build());
		return costObject;
	}
}
