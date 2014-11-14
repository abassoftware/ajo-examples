package de.abas.training.performance;

import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.ContextRunnable;
import de.abas.eks.jfop.remote.FOPSessionContext;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.schema.part.ProductEditor.Row;

/**
 * Creates test data for <code>PerformanceOptimizationSalesOrder</code>.
 *
 * @author abas Software AG
 * @version 1.0
 *
 */
public class CreateTestData implements ContextRunnable {

	private DbContext ctx = null;

	/**
	 * Creates one product with AJOPERF{i} as search word.
	 *
	 * @param i Search word suffix.
	 */
	private void createOneProduct(int i) {
		final ProductEditor newObject = ctx.newObject(ProductEditor.class);
		newObject.setSwd("AJOPERF" + i);
		final Row newRow = newObject.table().appendRow();
		newRow.setString("productListElem", "10001");
		newRow.setElemQty(1);
		newObject.commit();
	}

	/**
	 * Calls createOneProduct method for {count} times.
	 *
	 * @param count Number of products to create.
	 */
	private void createSalesOrders(int count) {
		for (int i = 0; i < count; i++) {
			createOneProduct(i);
		}
	}

	@Override
	public int runFop(FOPSessionContext ctx, String[] args) throws FOPException {
		this.ctx = ctx.getDbContext();
		createSalesOrders(5000);
		return 0;
	}

}