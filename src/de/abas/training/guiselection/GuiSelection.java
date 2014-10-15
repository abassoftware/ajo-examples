package de.abas.training.guiselection;

import de.abas.erp.api.gui.GUISelectionBuilder;
import de.abas.erp.api.gui.GUISelectionBuilder.Parameters;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.selection.Selection;
import de.abas.training.common.AbstractAjoAccess;

public class GuiSelection extends AbstractAjoAccess {

	@Override
	public void run() {
		DbContext dbContext = getDbContext();

		Parameters parameters = new Parameters();
		parameters.setCriteria("such=NN;nummer=10010!10015;@autostart=(Yes)");
		// calls GUISelectionBuilder with previously defined parameters
		Selection<Product> selection =
				GUISelectionBuilder
						.select(dbContext, Product.class, parameters);

		// use range?
		// selected -> all objects
		// not selected -> only first object
		if (selection != null) {
			Query<Product> query = dbContext.createQuery(selection);

			for (Product product : query) {
				dbContext.out().println(
						product.getIdno() + " -" + product.getSwd() + " - "
								+ product.getDescr());
			}
		}
		else {
			dbContext.out().println("Nothing selected");
		}
	}
}