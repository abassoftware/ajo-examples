package de.abas.examples.partnerday15;

import de.abas.erp.db.EditorAction;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.ExpertSelection;
import de.abas.erp.db.selection.RowSelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.examples.common.AbstractAjoAccess;

public class RowSeletionInEditor extends AbstractAjoAccess {
	
	public static void main(String[] args) throws Exception {
		new RowSeletionInEditor().run();
	}

	@Override
	public void run() throws CommandException {
		ProductEditor editor = aProductEditorFromSomewhere();
		ProductEditor.Table table = editor.table();
		
		RowSelectionBuilder<ProductEditor, ProductEditor.Row> rowSelectionBuilder = RowSelectionBuilder.create(ProductEditor.class, ProductEditor.Row.class);
		rowSelectionBuilder.add(Conditions.eq(ProductEditor.Row.META.elemQty, 2));

		Iterable<ProductEditor.Row> editableRows = table.getEditableRows(rowSelectionBuilder.build());
		for (ProductEditor.Row row : editableRows) {
			getDbContext().out().print("Row " + row.getRowNo());
			getDbContext().out().println(", Quantity " + row.getElemQty().toPlainString());
		}

		editor.abort();
	}

	private ProductEditor aProductEditorFromSomewhere() throws CommandException {
		Product product = QueryUtil.getFirst(getDbContext(), ExpertSelection.create(Product.class, "idno=30002"));
		ProductEditor editor = product.createEditor();
		editor.open(EditorAction.VIEW);
		return editor;
	}

}
