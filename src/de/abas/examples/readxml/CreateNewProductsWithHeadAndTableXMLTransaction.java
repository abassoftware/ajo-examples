package de.abas.examples.readxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.Transaction;
import de.abas.erp.db.schema.part.Product;
import de.abas.erp.db.schema.part.ProductEditor;
import de.abas.erp.db.selection.Conditions;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.erp.db.util.QueryUtil;
import de.abas.examples.common.AbstractAjoAccess;

/**
 * This class reads from a XML file, extracts the products stored in it and
 * creates new products accordingly. If a product with the same search word
 * already exists the transaction is rolled back completely.
 *
 * win/tmp/productionListToRead.xml: 
 * <?xml version="1.0" encoding="UTF-8"?>
 * <abasData>
 * 	<recordSet source="ajo" type="import">
 * 		<record swd="MYPC10" * descrOperLang="PC black red">
 * 			<header>
 * 				<field name="swd">MYPC10</field>
 * 				<field name="descrOperLang">PC black red</field>
 * 				<field name="salesprice">199.99</field>
 * 			</header>
 * 			<row>
 * 				<field name="productListElem">MYMOB0</field>
 * 				<field name="elemQty">1</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYHDD1</field>
 * 				<field name="elemQty">2</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYCPU1</field>
 * 				<field name="elemQty">1</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYRAM0</field>
 * 				<field name="elemQty">2</field>
 * 			</row>
 * 		</record>
 * 		<record swd="MYPC11" descrOperLang="PC yellow red">
 * 			<header>
 * 				<field name="swd">MYPC11</field>
 * 				<field name="descrOperLang">PC yellow red</field>
 * 				<field name="salesprice">199.99</field>
 * 			</header>
 * 			<row>
 * 				<field name="productListElem">MYMOB1</field>
 * 				<field name="elemQty">1</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYHDD1</field>
 * 				<field name="elemQty">2</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYCPU0</field>
 * 				<field name="elemQty">1</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYRAM0</field>
 * 				<field name="elemQty">2</field>
 * 			</row>
 * 		</record>
 * 		<record swd="MYPC12" * descrOperLang="PC green red">
 * 			<header>
 * 				<field name="swd">MYPC12</field>
 * 				<field name="descrOperLang">PC green red</field>
 * 				<field name="salesprice">211.99</field>
 * 			</header>
 * 			<row>
 * 				<field name="productListElem">MYMOB0</field>
 * 				<field name="elemQty">1</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYHDD0</field>
 * 				<field name="elemQty">2</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYCPU2</field>
 * 				<field name="elemQty">1</field>
 * 			</row>
 * 			<row>
 * 				<field name="productListElem">MYRAM2</field>
 * 				<field name="elemQty">2</field>
 * 			</row>
 * 		</record>
 * 	</recordSet>
 * </abasData>
 *
 * @author abas Software AG
 * @version 1.0
 *
 */
public class CreateNewProductsWithHeadAndTableXMLTransaction extends
		AbstractAjoAccess {

	private final String XML_FILE = "win/tmp/productListToRead.xml";
	private final String LOG_FILE = "win/tmp/productListToRead.log";

	private BufferedWriter bufferedWriter = null;
	private DbContext ctx = null;
	private boolean rollBack = false;
	private Transaction transaction = null;
	private ProductEditor productEditor = null;

	@Override
	public void run(String[] args) {
		ctx = getDbContext();
		getsOrCreatesLogFile(LOG_FILE);
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(LOG_FILE));
			document = saxBuilder.build(XML_FILE);
			Element rootElement = document.getRootElement();
			if (isValidXML(rootElement)) {
				ouputRootElement(rootElement);
				outputAttributes(rootElement.getChild("recordSet"));
				beginTransaction();
				createsProductsIfNotExisting(rootElement.getChild("recordSet")
						.getChildren());
				rollBackIfNecessary();
				commit();
			}
			else {
				log("invalid abas xml format");
			}
			log("End of program");
		}
		catch (JDOMException e) {
			ctx.out().println(e.getMessage());
		}
		catch (IOException e) {
			ctx.out().println(e.getMessage());
		}
		finally {
			closeProductEditor();
			closeBufferedWriter();
		}
	}

	/**
	 * Instantiates and begins the transaction.
	 */
	private void beginTransaction() {
		transaction = ctx.getTransaction();
		transaction.begin();
	}

	/**
	 * Checks whether product already exists and sets value of rollBack
	 * accordingly.
	 *
	 * @param dbContext The database context.
	 * @param bufferedWriter The BufferedWriter instance.
	 * @param rollBack Whether or not a roll back is necessary.
	 * @param record The current record element.
	 * @return Returns value of rollBack indicating whether a roll back is
	 * necessary.
	 * @throws IOException Exception thrown if an error occurred.
	 */
	private boolean
			checksWhetherProductExists(boolean rollBack, Element record)
					throws IOException {
		List<Attribute> recordAttributes = record.getAttributes();
		for (Attribute attribute : recordAttributes) {
			// if current 'record' element attribute is swd
			if (attribute.getName().equals("swd")) {
				// if there is already a product with the same search word
				if (isRecordExisting(ctx, attribute.getValue())) {
					// the transaction has to be rolled back
					rollBack = true;
					log("Product with swd " + attribute.getValue()
							+ " already exists");
				}
			}
		}
		return rollBack;
	}

	/**
	 * Closes BufferedWriter instance.
	 */
	private void closeBufferedWriter() {
		try {
			bufferedWriter.close();
		}
		catch (IOException e) {
			ctx.out().println("Error while trying to close bufferedWriter.");
		}
	}

	/**
	 * Makes sure the ProductEditor instance is not active anymore.
	 */
	private void closeProductEditor() {
		if (productEditor != null) {
			if (productEditor.active()) {
				productEditor.abort();
			}
		}
	}

	/**
	 * Commits the transaction and logs the process.
	 *
	 * @throws IOException Exception thrown if an error occurs.
	 */
	private void commit() throws IOException {
		if (!rollBack) {
			transaction.commit();
			log("commit");
		}
	}

	/**
	 * Creates the products.
	 *
	 * @param dbContext The database context.
	 * @param record The current record element from the XML file.
	 * @param productEditor The ProductEditor instance.
	 */
	private void createProduct(Element record, ProductEditor productEditor) {
		// gets all child elements of each 'record' element
		List<Element> recordChildren = record.getChildren();
		// iterates all child elements of each 'record' element
		for (Element recordChild : recordChildren) {
			if (recordChild.getName().equals("header")) {
				writeProductHeaderFields(recordChild, productEditor, ctx);
			}
			else if (recordChild.getName().equals("row")) {
				writeProductRowFields(recordChild, productEditor, ctx);
			}
		}
	}

	/**
	 * Checks whether each record's product is not already existing. Then
	 * creates product or sets rollback to true.
	 *
	 * @param dbContext The database context.
	 * @param bufferedWriter The BufferedWriter instance.
	 * @param rollBack Whether or not to roll back.
	 * @param records The records from the XML file.
	 * @return Returns value of rollBack, to indicate whether or not a roll back
	 * is necessary.
	 * @throws IOException Exception thrown if an error occurs.
	 */
	private void createsProductsIfNotExisting(List<Element> records)
			throws IOException {
		for (Element record : records) {
			productEditor = ctx.newObject(ProductEditor.class);
			rollBack = checksWhetherProductExists(rollBack, record);
			if (rollBack == true) {
				// if rollBack is true the opened ProductEditor instance is
				// aborted and the foreach loop is cancelled
				productEditor.abort();
				break;
			}
			else {
				createProduct(record, productEditor);
			}
			// saves the new product
			productEditor.commit();
			// for testing the ProductEditor instance can be aborted
			// productEditor.abort();
			log(productEditor.objectId().getSwd() + " - "
					+ productEditor.objectId().getIdno());
		}
	}

	/**
	 * Gets or if not existent creates log file.
	 *
	 * @param logFile The log file.
	 */
	private void getsOrCreatesLogFile(String logFile) {
		File file = new File(logFile);
		if (!file.exists()) {
			try {
				boolean createNewFile = file.createNewFile();
				if (createNewFile) {
					ctx.out().println("File " + logFile + " was created");
				}
			}
			catch (IOException e) {
				ctx.out().println(e.getMessage());
			}
		}
	}

	/**
	 * Checks whether a product with the same search word exists.
	 *
	 * @param dbContext The database context.
	 * @param swd The search word.
	 * @return Returns true if the a product with the search word exits, else
	 * returns false.
	 */
	private boolean isRecordExisting(DbContext dbContext, String swd) {
		SelectionBuilder<Product> selectionBuilder =
				SelectionBuilder.create(Product.class);
		selectionBuilder.add(Conditions.eq(Product.META.swd, swd));
		Product first = QueryUtil.getFirst(dbContext, selectionBuilder.build());
		if (first == null) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Checks whether the XML file is valid.
	 *
	 * @param rootElement The root element
	 * @return True, if XML is valid, else false.
	 */
	private boolean isValidXML(Element rootElement) {
		return rootElement.getName().equals("abasData");
	}

	/**
	 * Logs message.
	 *
	 * @throws IOException Exception thrown if an error occurs.
	 */
	private void log(String message) throws IOException {
		bufferedWriter.write(message);
		bufferedWriter.newLine();
	}

	/**
	 * Outputs the root element.
	 *
	 * @param rootElement The root element.
	 */
	private void ouputRootElement(Element rootElement) {
		ctx.out().println("root-Element: " + rootElement.getName());
	}

	/**
	 * Outputs the attributes of the recordSet element.
	 *
	 * @param recordSet The recordSet element.
	 */
	private void outputAttributes(Element recordSet) {
		for (Attribute attribute : recordSet.getAttributes()) {
			ctx.out().println(
					attribute.getName() + " - " + attribute.getValue());
		}
	}

	/**
	 * Rolls the transaction back if necessary and logs the process.
	 *
	 * @throws IOException Exception thrown if an error occurs.
	 */
	private void rollBackIfNecessary() throws IOException {
		if (rollBack) {
			transaction.rollback();
			log("rollback");
		}
	}

	/**
	 * Reads the information about every product's head from the XML file and
	 * creates a product accordingly.
	 *
	 * @param recordChild The child element of the 'record' element containing
	 * information about the record's head.
	 * @param productEditor The ProductEditor instance.
	 * @param dbContext The database context.
	 */
	private void writeProductHeaderFields(Element recordChild,
			ProductEditor productEditor, DbContext dbContext) {
		// gets the head fields as child elements of the 'header' element
		List<Element> fields = recordChild.getChildren();
		for (Element field : fields) {
			String fieldName = field.getAttributeValue("name");
			String fieldValue = field.getValue();
			dbContext.out().println(fieldName + " - " + fieldValue);
			productEditor.setString(fieldName, fieldValue);
		}
	}

	/**
	 * Reads the information about every product's row from the XML file and
	 * creates table rows accordingly.
	 *
	 * @param recordChild The child element of the 'record' element containing
	 * information about the record's table rows.
	 * @param productEditor The ProductEditor instance.
	 * @param dbContext The database context.
	 */
	private void writeProductRowFields(Element recordChild,
			ProductEditor productEditor, DbContext dbContext) {
		// gets the row fields as child elements of the 'row' element
		List<Element> fields = recordChild.getChildren();
		ProductEditor.Row appendRow = productEditor.table().appendRow();
		for (Element field : fields) {
			String fieldName = field.getAttributeValue("name");
			String fieldValue = field.getValue();
			dbContext.out().println(fieldName + " - " + fieldValue);
			appendRow.setString(fieldName, fieldValue);
		}
	}

}
