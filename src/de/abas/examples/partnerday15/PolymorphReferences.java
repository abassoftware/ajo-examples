package de.abas.examples.partnerday15;

import static de.abas.erp.db.selection.Conditions.eq;

import java.util.UUID;

import de.abas.erp.common.type.Id;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.EditorAction;
import de.abas.erp.db.EditorCommand;
import de.abas.erp.db.EditorCommandFactory;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.schema.customer.Customer;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.referencetypes.PurchasingAndSalesProcessEditor;
import de.abas.erp.db.schema.sales.OpportunityEditor;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.training.advanced.testutility.Utility;

public class PolymorphReferences {

	private final String TEST_UUID = UUID.randomUUID().toString();

	public static void main(String[] args) throws CommandException {
		Utility contextProvider = new Utility();
		DbContext ctx = contextProvider.createClientContext();
		new PolymorphReferences().createSalesChain(ctx);
	}

	public void createSalesChain(DbContext ctx) throws CommandException {
		// 1. Vorgangskette durchspielen
		// neue Chance
		PurchasingAndSalesProcessEditor salesProcessEditor = newEmptyOpportunity(ctx);
		printInfo(ctx, salesProcessEditor); // OpportunityEditor
		salesProcessEditor.abort();
		final Id opportunityId = salesProcessEditor.id();

		// Chance -> Angebot
		salesProcessEditor = nextStep(ctx, opportunityId, EditorAction.RELEASE);
		appendARowWithTenPieces(ctx, salesProcessEditor);
		printInfo(ctx, salesProcessEditor); // QuotationEditor
		final Id quotationId = salesProcessEditor.id();

		// Angebot -> Auftrag
		salesProcessEditor = nextStep(ctx, quotationId, EditorAction.RELEASE);
		twelvePiecesInFirstRow(salesProcessEditor);
		printInfo(ctx, salesProcessEditor); // SalesOrderEditor
		final Id salesOrderId = salesProcessEditor.id();

		// Auftrag -> Rechnung
		salesProcessEditor = nextStep(ctx, salesOrderId, EditorAction.INVOICE);
		invoiceSixPieces(salesProcessEditor);
		printInfo(ctx, salesProcessEditor); // InvoiceEditor

		// Auftrag -> Lieferschein
		salesProcessEditor = nextStep(ctx, salesOrderId, EditorAction.DELIVERY);
		deliverSixPieces(salesProcessEditor);
		printInfo(ctx, salesProcessEditor); // PackingSlipEditor
	}

	private void printInfo(DbContext ctx, PurchasingAndSalesProcessEditor editor) throws CommandException {
		ctx.out().println("---");
		ctx.out().println("Classname : " + editor.getClass().getSimpleName());
		ctx.out().println("ID        : " + editor.id());
		ctx.out().println("Searchword: " + editor.objectId().getSwd());
	}

	private PurchasingAndSalesProcessEditor newEmptyOpportunity(DbContext ctx) throws CommandException {
		final PurchasingAndSalesProcessEditor salesProcessEditor = ctx.newObject(OpportunityEditor.class);
		((OpportunityEditor) salesProcessEditor).setCustomer(SelectionBuilder.create(Customer.class).add(eq(Customer.META.idno, "70001"))
				.build());
		salesProcessEditor.setAddr(this.TEST_UUID);
		salesProcessEditor.commitAndReopen();

		return salesProcessEditor;
	}

    private PurchasingAndSalesProcessEditor nextStep(DbContext ctx, Id salesProcessId, EditorAction action)
            throws CommandException {
        final EditorCommand releaseCommand = EditorCommandFactory.create(action, salesProcessId.toString());
        return (PurchasingAndSalesProcessEditor) ctx.openEditor(releaseCommand);
    }

    private void appendARowWithTenPieces(DbContext ctx, PurchasingAndSalesProcessEditor salesProcessEditor) {
        final PurchasingAndSalesProcessEditor.Table table = salesProcessEditor.table();
        final PurchasingAndSalesProcessEditor.Row row = table.appendRow();

        final Product product = QueryUtil.getFirst(ctx, SelectionBuilder.create(Product.class).add(eq(Product.META.idno, "10001")).build());
        row.setProduct(product);
        row.setUnitQty(10.0);
        salesProcessEditor.commit();
    }

    private void twelvePiecesInFirstRow(PurchasingAndSalesProcessEditor salesProcessEditor) {
        final PurchasingAndSalesProcessEditor.Row firstRow = salesProcessEditor.table().getRow(1);
        firstRow.setUnitQty(12.0);
        salesProcessEditor.commit();
    }

    private void invoiceSixPieces(PurchasingAndSalesProcessEditor salesProcessEditor) {
        salesProcessEditor.setDeadlineWeek(salesProcessEditor.getValDate());
        salesProcessEditor.setEntDate(salesProcessEditor.getValDate());
        salesProcessEditor.setAddr(this.TEST_UUID);
        final PurchasingAndSalesProcessEditor.Row firstRow = salesProcessEditor.table().getRow(1);
        firstRow.setUnitQty(6.0);
        salesProcessEditor.commit();
    }

    private void deliverSixPieces(final PurchasingAndSalesProcessEditor salesProcessEditor) {
        final PurchasingAndSalesProcessEditor.Row firstRow = salesProcessEditor.table().getRow(1);
        firstRow.setUnitQty(6.0);
        salesProcessEditor.commit();
    }

}
