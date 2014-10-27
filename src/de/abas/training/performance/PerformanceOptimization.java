package de.abas.training.performance;

import de.abas.ceks.jedp.EDPSession;
import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FO;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.FieldSet;
import de.abas.erp.db.FieldValueProvider;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.Selection;
import de.abas.erp.db.util.ContextHelper;
import de.abas.erp.db.util.LegacyUtil;

/**
 * This class shows how to optimize the performance of database requests with
 * AJO.
 *
 * @author abas Software AG
 * @version 1.0
 *
 */
public class PerformanceOptimization implements ContextRunnable {

    private DbContext ctx = null;

    @Override
    public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
	this.ctx = ctx.getDbContext();
	runOnServer();
	runClient(false);
	runClient(true);
	return 0;
    }

    /**
     * Print info about performance.
     *
     * @param counter Number of objects.
     * @param delta Time delta.
     */
    private void printStats(int counter, long delta) {
	ctx.out().println("Number of objects: " + counter);
	ctx.out().println("Duration: " + delta + "ms");
	double deltaInSecond = (double) delta / 1000;
	ctx.out().println("Performance: " + (int) (counter / deltaInSecond) + " objects/s!!!");
    }

    /**
     * Reads query and gets object specific information.
     *
     * @param query The query.
     * @return The number of objects.
     */
    private int readQuery(Query<Product> query) {
	int counter = 0;
	for (Product product : query) {
	    product.getIdno();
	    product.getSwd();
	    counter++;
	}
	return counter;
    }

    /**
     * Runs selection using FieldSet and LazyLoad.
     *
     * @param optimize Whether or not to optimize the performance.
     */
    private void runClient(boolean optimize) {
	DbContext clientContext = ContextHelper.createClientContext(null, 6550, "", FO.Gvar("einmalpw"), "AJO-Local-ClientContext");
	try {
	    if (optimize) {
		EDPSession session = LegacyUtil.getSession(clientContext);
		session.setDataSetSize(1000);
	    }

	    long start = System.currentTimeMillis();
	    Selection<Product> selection = ExpertSelection.create(Product.class, "swd=AJOPERF");
	    Query<Product> query = clientContext.createQuery(selection);

	    if (optimize) {
		// uses FieldSet to define the needed fields instead of loading
		// all fields
		FieldSet<FieldValueProvider> fieldSet = FieldSet.of("id", "idno", "swd", "product^idno", "price", "head", "head^idno", "head^swd");
		query.setFields(fieldSet);
		// disable lazy load to read everything at once
		query.setLazyLoad(false);
	    }

	    int counter = readQuery(query);

	    ctx.out().println("========== runClient() Optimize: " + (optimize ? "on" : "off"));
	    printStats(counter, System.currentTimeMillis() - start);
	}
	finally {
	    clientContext.close();
	}
    }

    /**
     * Runs selection in server mode without optimization.
     */
    private void runOnServer() {
	long start = System.currentTimeMillis();

	Selection<Product> selection = ExpertSelection.create(Product.class, "swd=AJOPERF");
	Query<Product> query = ctx.createQuery(selection);

	int counter = readQuery(query);

	ctx.out().println("========== runOnServer()");
	printStats(counter, System.currentTimeMillis() - start);
    }

}