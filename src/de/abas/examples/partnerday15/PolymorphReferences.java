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
import de.abas.examples.common.ConnectionProvider;

public class PolymorphReferences {

	private final String A_RANDOM_ADDRESS = UUID.randomUUID().toString();

	public static void main(String[] args) throws CommandException {
		ConnectionProvider contextProvider = new ConnectionProvider();
		DbContext ctx = contextProvider.createDbContext("PolymorphReferencesExample");
		new PolymorphReferences().createSalesChain(ctx);
	}

	public void createSalesChain(DbContext ctx) throws CommandException {
		// Traverse Sales Process Chain
		// neue Chance
		PurchasingAndSalesProcessEditor salesProcessEditor = newEmptyOpportunity(ctx, this.A_RANDOM_ADDRESS);
		printInfo(ctx, salesProcessEditor); // OpportunityEditor
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
		invoiceSixPieces(salesProcessEditor, this.A_RANDOM_ADDRESS);
		printInfo(ctx, salesProcessEditor); // InvoiceEditor

		// Auftrag -> Lieferschein
		salesProcessEditor = nextStep(ctx, salesOrderId, EditorAction.DELIVERY);
		sixPiecesInFirstRow(salesProcessEditor);
		printInfo(ctx, salesProcessEditor); // PackingSlipEditor
	}

	private void printInfo(DbContext ctx, PurchasingAndSalesProcessEditor editor) throws CommandException {
		ctx.out().println("---");
		ctx.out().println("Classname : " + editor.getClass().getSimpleName());
		ctx.out().println("ID        : " + editor.id());
		ctx.out().println("Searchword: " + editor.objectId().getSwd());
	}

	PurchasingAndSalesProcessEditor newEmptyOpportunity(DbContext ctx, String address) throws CommandException {
		OpportunityEditor salesProcessEditor = ctx.newObject(OpportunityEditor.class);
		salesProcessEditor.setCustomer(SelectionBuilder.create(Customer.class).add(eq(Customer.META.idno, "70001")).build());
		salesProcessEditor.setAddr(address);
		salesProcessEditor.commit();

		return salesProcessEditor;
	}

    PurchasingAndSalesProcessEditor nextStep(DbContext ctx, Id salesProcessId, EditorAction action) throws CommandException {
        EditorCommand releaseCommand = EditorCommandFactory.create(action, salesProcessId.toString());
        return (PurchasingAndSalesProcessEditor) ctx.openEditor(releaseCommand);
    }

    void appendARowWithTenPieces(DbContext ctx, PurchasingAndSalesProcessEditor salesProcessEditor) {
        PurchasingAndSalesProcessEditor.Table table = salesProcessEditor.table();
        PurchasingAndSalesProcessEditor.Row row = table.appendRow();

        Product product = QueryUtil.getFirst(ctx, SelectionBuilder.create(Product.class).add(eq(Product.META.idno, "10001")).build());
        row.setProduct(product);
        row.setUnitQty(10.0);
        salesProcessEditor.commit();
    }

    void twelvePiecesInFirstRow(PurchasingAndSalesProcessEditor salesProcessEditor) {
        PurchasingAndSalesProcessEditor.Row firstRow = salesProcessEditor.table().getRow(1);
        firstRow.setUnitQty(12.0);
        salesProcessEditor.commit();
    }

    void invoiceSixPieces(PurchasingAndSalesProcessEditor salesProcessEditor, String address) {
        salesProcessEditor.setDeadlineWeek(salesProcessEditor.getValDate());
        salesProcessEditor.setEntDate(salesProcessEditor.getValDate());
        salesProcessEditor.setAddr(address);
        
        sixPiecesInFirstRow(salesProcessEditor);
    }

    void sixPiecesInFirstRow(PurchasingAndSalesProcessEditor salesProcessEditor) {
        PurchasingAndSalesProcessEditor.Row firstRow = salesProcessEditor.table().getRow(1);
        firstRow.setUnitQty(6.0);
        salesProcessEditor.commit();
    }

}
