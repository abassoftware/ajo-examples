package de.abas.examples.subeditor;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.EditorAction;
import de.abas.erp.db.EditorObject;
import de.abas.erp.db.exception.CommandException;
import de.abas.erp.db.schema.bankdata.Bank;
import de.abas.erp.db.schema.bankdata.BankDetailsEditor;
import de.abas.erp.db.schema.customer.Customer;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.examples.common.AbstractAjoAccess;

/**
 * This class shows how to work with a sub editor using AJO.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class ControlCustomerInsertNewBankDetails extends AbstractAjoAccess {

	@Override
	public int run(String[] args) {
		DbContext dbContext = getDbContext();
		
		try {
			// defines selection criteria
			SelectionBuilder<Customer> selectionBuilder = SelectionBuilder.create(Customer.class);
			selectionBuilder.add(Conditions.eq(Customer.META.idno, "70026"));
			
			// executes query
			Customer customer = QueryUtil.getFirst(dbContext, selectionBuilder.build());
			
			// outputs customer
			dbContext.out().println(customer.getIdno() + " - " + customer.getSwd() + " - " + customer.getDescr());
			
			// creates and opens editor
			CustomerEditor customerEditor = customer.createEditor();
			customerEditor.open(EditorAction.UPDATE);
			
			// opens sub editor by using button 'bankNew'
			EditorObject editorObject = customerEditor.invokeBankNew();
			if (editorObject instanceof BankDetailsEditor) {
				BankDetailsEditor bankDetailsEditor = (BankDetailsEditor) editorObject;
				bankDetailsEditor.setSwd("JPMORGAN");
				// bank object must be existing e.g. indo 1 => JP Morgan Chase & Co	
				bankDetailsEditor.setBankDetBankName(getBank(dbContext, "1"));
				// saves sub editor object
				bankDetailsEditor.commit();
			}
			
			dbContext.out().println("Completed!");
			return 0;
		}
		catch (CommandException e) {
			dbContext.out().println("An error occurred: " + e.getMessage());
			return 1;
		}

	}

	/**
	 * Selects bank with specified idno from database.
	 * 
	 * @param dbContext The database context.
	 * @param idno The idno of the bank to select.
	 * @return Returns the bank object with the specified idno.
	 */
	private Bank getBank(DbContext dbContext, String idno) {
		SelectionBuilder<Bank> selectionBuilder = SelectionBuilder.create(Bank.class);
		selectionBuilder.add(Conditions.eq(Bank.META.idno, idno));
		return QueryUtil.getFirst(dbContext, selectionBuilder.build());
	}

}
