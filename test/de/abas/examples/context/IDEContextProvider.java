package de.abas.examples.context;

import de.abas.erp.db.DbContext;
import de.abas.examples.common.ConnectionProvider;

public class IDEContextProvider implements ContextProvider {

	@Override
	public DbContext getContext() {
		return new ConnectionProvider().createDbContext("AJO-Test");
	}

}
