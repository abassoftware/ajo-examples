package de.abas.examples.util;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import de.abas.erp.db.DbContext;
import de.abas.examples.context.ContextProvider;
import de.abas.examples.context.IDEContextProvider;

public class ClientContextTest {

	@Rule
	public TestName testName = new TestName();
	public DbContext ctx;

	public void setDefaultLogger(final String filename) {
		try {
			ctx.setLogger(new FileWriter(filename));
		} catch (final IOException e) {
			e.printStackTrace(ctx.out());
		}
	}

	@Before
	public void setup() {
		ContextProvider contextProvider = new IDEContextProvider();
		ctx = contextProvider.getContext();
		setDefaultLogger(getClass().getName() + "." + testName.getMethodName() + ".edp.log");
	}
	
	@After
	public void tearDown() {
		ctx.close();
	}

}
