package de.abas.training.performance;

import de.abas.ceks.jedp.EDPSession;
import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FO;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.api.session.OperatorInformation;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.FieldSet;
import de.abas.erp.db.FieldValueProvider;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.sales.Item;
import de.abas.erp.db.schema.sales.SalesOrder;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.ContextHelper;
import de.abas.erp.db.util.LegacyUtil;

/**
 * This class shows how to optimize the performance of database requests with AJO.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class PerformanceOptimizationSalesOrder implements ContextRunnable {

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		DbContext dbContext = ctx.getDbContext();
		runClientWithOptimization(dbContext);
		runOnServer(dbContext);
		return 0;
	}

	/**
	 * Runs selection in server mode without optimization.
	 * 
	 * @param dbContext The database context.
	 */
	private void runOnServer(final DbContext dbContext) {
		int anzahlAuftraege = 0;
		int anzahlZeilen = 0;
		long start = System.currentTimeMillis();

		// selects all sales orders
		SelectionBuilder<SalesOrder> selectionBuilder = SelectionBuilder.create(SalesOrder.class);
		Query<SalesOrder> query = dbContext.createQuery(selectionBuilder.build());

		// iterates all sales orders
		for (SalesOrder salesOrder : query) {
			int j = salesOrder.table().getRowCount();

			// iterates all sales order rows
			for (int i = 1; i <= j; i++) {
				salesOrder.table().getRow(i);
				anzahlZeilen++;
			}

			anzahlAuftraege++;
		}

		long sec = (System.currentTimeMillis() - start);
		dbContext.out().println("Gesamtzeit in Millisec. " + sec);
		dbContext.out().println("Gesammtanzahl der Aufträge " + anzahlAuftraege);
		dbContext.out().println("Gesammtanzahl der Zeilen  " + anzahlZeilen);

	}

	/**
	 * Runs selection using FieldSet and LazyLoad.
	 * 
	 * @param dbContext The database context.
	 */
	private void runClientWithOptimization(DbContext dbContext) {
		DbContext clientContext = ContextHelper.createClientContext(null, 6550, "", FO.Gvar("einmalpw"), "AJO-Local-ClientContext");

		EDPSession session = LegacyUtil.getSession(clientContext);
		session.setDataSetSize(1000);

		long currentTimeMillis = System.currentTimeMillis();
		Selection<Item> selection = ExpertSelection.create(Item.class, "kopf^typ=Auftrag;@ordnung=kopf;@ablageart=lebendig;@zeilen=nein;@language=de");
		Query<Item> queryItem = clientContext.createQuery(selection);

		// uses FieldSet to define the needed fields instead of loading all fields
		FieldSet<FieldValueProvider> fieldSet = FieldSet.of("id", "idno", "swd", "product^idno", "price", "head", "head^idno", "head^swd");
		queryItem.setFields(fieldSet);

		// uses LazyLoad to postpone database access
		queryItem.setLazyLoad(true);

		int counter = 0;
		for (Item item : queryItem) {
			counter++;
		}

		long currentTimeMillis2 = System.currentTimeMillis();
		OperatorInformation operatorInformation = new OperatorInformation(dbContext);
		String uniqueNo = operatorInformation.getUniqueNo();
		clientContext.out().println("Start: " + currentTimeMillis + " - End: " + currentTimeMillis2);
		clientContext.out().println("Dauer: " + ((double) (currentTimeMillis2 / 1000 - currentTimeMillis / 1000)) + " ms!!!");
		clientContext.out().println("Anzahl der Datensätze : " + counter);
		clientContext.out().println("Aufrufernr. : " + uniqueNo);

	}

}
