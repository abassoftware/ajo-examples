package de.abas.examples.rowselectionbuilder;

import de.abas.erp.db.RowQuery;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.Product.Row;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.RowSelectionBuilder;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.examples.common.AbstractAjoAccess;

/**
 * This class shows advanced selections in the head and row of a data set.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class SelectProductsHeadAndRow extends AbstractAjoAccess {

	@Override
	public int run(String[] args) {
		RowSelectionBuilder<Product, Row> rowSelectionBuilder = 
			RowSelectionBuilder.create(Product.class, Row.class);
		
		
		// row selection criteria
		// Caution: The method getSelectedProduct can also return null
		rowSelectionBuilder.add(Conditions.eq(Row.META.productListElem, 
			getSelectedProduct("NN10021")));
		
		// head selection criteria
		rowSelectionBuilder.addForHead(Conditions.between(Product.META.idno, 
			"30010", "30015"));

		RowQuery<Product, Row> rowQueryProduct = getDbContext().createQuery(rowSelectionBuilder.build());
		
		// displays query result
		if(rowQueryProduct != null){
			getDbContext().out().println("Query-Object: " + rowQueryProduct);
			for (Row row : rowQueryProduct) {
				getDbContext().out().println(row.header().getIdno() + " - " + row.header().getSwd());
				getDbContext().out().println("--" + row.getProductListElem().getIdno() 
					+ " -- " + row.getProductListElem().getSwd());
			}		
		}else {
			getDbContext().out().println("Query-Object: null");
		}
		
		return 0;
		
	}

	/**
	 * Selects first product with the specified search word.
	 * 
	 * @param swd The search word.
	 * @return Returns the product as instance of SelectableObject.
	 */
	private Product getSelectedProduct(String swd) {
		SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.eq(Product.META.swd, swd));
		Product product = QueryUtil.getFirst(getDbContext(), selectionBuilder.build());
		return product;
	}

}
