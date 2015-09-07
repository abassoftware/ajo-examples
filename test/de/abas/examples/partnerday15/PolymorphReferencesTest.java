package de.abas.examples.partnerday15;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Test;

import de.abas.erp.common.type.Id;
import de.abas.erp.db.EditorAction;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.schema.referencetypes.PurchasingAndSalesProcess;
import de.abas.erp.db.schema.referencetypes.PurchasingAndSalesProcessEditor;
import de.abas.erp.db.schema.sales.Invoice;
import de.abas.erp.db.schema.sales.InvoiceEditor;
import de.abas.erp.db.schema.sales.Opportunity;
import de.abas.erp.db.schema.sales.OpportunityEditor;
import de.abas.erp.db.schema.sales.PackingSlip;
import de.abas.erp.db.schema.sales.PackingSlipEditor;
import de.abas.erp.db.schema.sales.Quotation;
import de.abas.erp.db.schema.sales.QuotationEditor;
import de.abas.erp.db.schema.sales.SalesOrder;
import de.abas.erp.db.schema.sales.SalesOrderEditor;
import de.abas.examples.util.ClientContextTest;

public class PolymorphReferencesTest extends ClientContextTest {
	
	private final String TEST_UUID = UUID.randomUUID().toString();
	
	@Test
	public void testSalesChain() throws CommandException {
		PolymorphReferences polyRefDemo = new PolymorphReferences();
		
		// 1. Traverse Sales Process Chain
		// neue Chance
		PurchasingAndSalesProcessEditor salesProcessEditor = polyRefDemo.newEmptyOpportunity(ctx, TEST_UUID);
		assertThat(salesProcessEditor, instanceOf(OpportunityEditor.class));
		final Id opportunityId = salesProcessEditor.id();

		// Chance -> Angebot
		salesProcessEditor = polyRefDemo.nextStep(ctx, opportunityId, EditorAction.RELEASE);
		assertThat(salesProcessEditor, instanceOf(QuotationEditor.class));
		polyRefDemo.appendARowWithTenPieces(ctx, salesProcessEditor);
		final Id quotationId = salesProcessEditor.id();
		assertThat(quotationId, not(equalTo(opportunityId)));

		// Angebot -> Auftrag
		salesProcessEditor = polyRefDemo.nextStep(ctx, quotationId, EditorAction.RELEASE);
		polyRefDemo.twelvePiecesInFirstRow(salesProcessEditor);
		assertThat(salesProcessEditor, instanceOf(SalesOrderEditor.class));
		final Id salesOrderId = salesProcessEditor.id();
		assertThat(salesOrderId, not(equalTo(quotationId)));

		// Auftrag -> Rechnung
		salesProcessEditor = polyRefDemo.nextStep(ctx, salesOrderId, EditorAction.INVOICE);
		assertThat(salesProcessEditor, instanceOf(InvoiceEditor.class));
		polyRefDemo.invoiceSixPieces(salesProcessEditor, TEST_UUID);
		final Id invoiceId = salesProcessEditor.id();
		assertThat(invoiceId, not(equalTo(salesOrderId)));

		// Auftrag -> Lieferschein
		salesProcessEditor = polyRefDemo.nextStep(ctx, salesOrderId, EditorAction.DELIVERY);
		assertThat(salesProcessEditor, instanceOf(PackingSlipEditor.class));
		polyRefDemo.sixPiecesInFirstRow(salesProcessEditor);
		final Id packingSlipId = salesProcessEditor.id();
        assertThat(packingSlipId, not(equalTo(invoiceId)));
        
        // 2. Readonly Tests with the objects constructed above
        checkSalesProcess(ctx.load(Opportunity.class, opportunityId));
        checkSalesProcess(ctx.load(Quotation.class, quotationId));
        checkSalesProcess(ctx.load(SalesOrder.class, salesOrderId));
        checkSalesProcess(ctx.load(Invoice.class, invoiceId));
        checkSalesProcess(ctx.load(PackingSlip.class, packingSlipId));
	}

    private void checkSalesProcess(final PurchasingAndSalesProcess salesProcess) {
        assertNotNull(salesProcess);
        assertEquals(this.TEST_UUID, salesProcess.getAddr());
    }

}
