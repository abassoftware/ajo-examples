package de.abas.examples.context;

import de.abas.erp.db.DbContext;

public abstract class AbstractContextProvider {
	
	public abstract DbContext getContext();

}
