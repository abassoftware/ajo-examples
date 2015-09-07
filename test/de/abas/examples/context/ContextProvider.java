package de.abas.examples.context;

import de.abas.erp.db.DbContext;

public interface ContextProvider {
	
	public DbContext getContext();

}
