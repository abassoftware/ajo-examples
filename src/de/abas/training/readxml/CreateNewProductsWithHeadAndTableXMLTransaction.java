package de.abas.training.readxml;

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
import de.abas.training.common.AbstractAjoAccess;

/**
 * This class reads from a XML file, extracts the products stored in it and creates new products accordingly.
 * If a product with the same search word already exists the transaction is rolled back completely.
 * 
 * win/tmp/productionListToRead.xml:
 * <?xml version="1.0" encoding="UTF-8"?>
 * <abasData>
 * 	<recordSet source="ajo" type="import">
 *  	<record swd="MYPC10" descrOperLang="PC black red">
 * 			<header>
 * 				<field name="swd">MYPC10</field>
 *        		<field name="descrOperLang">PC black red</field>
 *        		<field name="salesprice">199.99</field>
 *      	</header>
 *      	<row>
 *        		<field name="productListElem">MYMOB0</field>
 *        		<field name="elemQty">1</field>
 *      	</row>
 *     		<row>
 *        		<field name="productListElem">MYHDD1</field>
 *        		<field name="elemQty">2</field>
 *      	</row>
 *     		<row>
 *        		<field name="productListElem">MYCPU1</field>
 *        		<field name="elemQty">1</field>
 *      	</row>
 *      	<row>
 *        		<field name="productListElem">MYRAM0</field>
 *        		<field name="elemQty">2</field>
 *      	</row>
 *     	</record>
 *      <record swd="MYPC11" descrOperLang="PC yellow red">
 *      	<header>
 *        		<field name="swd">MYPC11</field>
 *        		<field name="descrOperLang">PC yellow red</field>
 *        		<field name="salesprice">199.99</field>
 *      	</header>
 *      	<row>
 *        		<field name="productListElem">MYMOB1</field>
 *        		<field name="elemQty">1</field>
 *      	</row>
 *     		<row>
 *        		<field name="productListElem">MYHDD1</field>
 *        		<field name="elemQty">2</field>
 *      	</row>
 *     		<row>
 *        		<field name="productListElem">MYCPU0</field>
 *        		<field name="elemQty">1</field>
 *      	</row>
 *      	<row>
 *        		<field name="productListElem">MYRAM0</field>
 *        		<field name="elemQty">2</field>
 *      	</row>
 *     	</record>
 *    	<record swd="MYPC12" descrOperLang="PC green red">
 *      	<header>
 *        		<field name="swd">MYPC12</field>
 *        		<field name="descrOperLang">PC green red</field>
 *        		<field name="salesprice">211.99</field>
 *      	</header>
 *      	<row>
 *        		<field name="productListElem">MYMOB0</field>
 *        		<field name="elemQty">1</field>
 *      	</row>
 *     		<row>
 *        		<field name="productListElem">MYHDD0</field>
 *        		<field name="elemQty">2</field>
 *      	</row>
 *     		<row>
 *        		<field name="productListElem">MYCPU2</field>
 *        		<field name="elemQty">1</field>
 *      	</row>
 *      	<row>
 *        		<field name="productListElem">MYRAM2</field>
 *        		<field name="elemQty">2</field>
 *      	</row>
 *     	</record>
 *  </recordSet>
 * </abasData>
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class CreateNewProductsWithHeadAndTableXMLTransaction extends AbstractAjoAccess {

	@Override
	public void run() {
		// gets database context
		DbContext dbContext = getDbContext();

		// defines path and name of xml file and log file
		// for server mode only: $MANDANTDIR/win/tmp
		String xmlFile = "win/tmp/productListToRead.xml";
		String logFile = "win/tmp/productListToRead.log";

		// checks whether the log file exists
		File file = new File(logFile);
		if (!file.exists()) {
			// if log file does not exist a new one is created
			try {
				boolean createNewFile = file.createNewFile();
				if (createNewFile) {
					dbContext.out().println("Datei: " + logFile + " wurde angelegt");
				}
			}
			catch (IOException e) {
				dbContext.out().println(e.getMessage());
			}
		}

		// creates Simple API for XML (SAX) instance
		SAXBuilder saxBuilder = new SAXBuilder();
		// declares document
		Document document = null;
		try {
			// creates a BufferedWriter instance for the log file
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile));
			// creates Document object for the XML file using the SAXBuilder instance
			document = saxBuilder.build(xmlFile);
			// gets the root element
			Element rootElement = document.getRootElement();
			// validates XML format
			if (rootElement.getName().equals("abasData")) {

				// initializes roll back flag
				boolean rollBack = false;

				// displays root element
				dbContext.out().println("root-Element: " + rootElement.getName());
				// gets the child element 'recordSet' of the root element
				Element recordSet = rootElement.getChild("recordSet");
				// displays all attributes of the 'recordSet' element
				List<Attribute> recordSetAttributes = recordSet.getAttributes();
				for (Attribute attribute : recordSetAttributes) {
					dbContext.out().println(attribute.getName() + " - " + attribute.getValue());
				}

				// gets all child elements of the 'recordSets' element
				List<Element> records = recordSet.getChildren();

				// begins the transaction
				Transaction transaction = dbContext.getTransaction();
				transaction.begin();

				// iterates all 'record' elements
				for (Element record : records) {

					// creates a new ProductEditor instance
					ProductEditor productEditor = dbContext.newObject(ProductEditor.class);

					// gets all attributes of the current 'record' element
					List<Attribute> recordAttributes = record.getAttributes();
					// iterates all attributes of the 'record' element
					for (Attribute attribute : recordAttributes) {
						dbContext.out().println(attribute.getName() + " - " + attribute.getValue());
						// if current 'record' element attribute is swd
						if (attribute.getName().equals("swd")) {
							dbContext.out().println("Prüfen ob swd: " + attribute.getValue() + " vorhanden ist");
							// if there is already a product with the same search word
							if (isRecordExisting(dbContext, attribute.getValue())) {
								// the transaction has to be rolled back
								rollBack = true;
								bufferedWriter.write("Datensatz swd: " + attribute.getValue() + " ist bereits vorhanden ");
								bufferedWriter.newLine();
							}
						}
					}
					// checks whether rollback is necessary
					if (rollBack == true) {
						// if rollBack is true the opened ProductEditor instance is aborted and the foreach loop is cancelled
						productEditor.abort();
						break;
					}
					else {
						// gets all child elements of each 'record' element
						List<Element> recordChildren = record.getChildren();
						// iterates all child elements of each 'record' element
						for (Element recordChild : recordChildren) {
							if (recordChild.getName().equals("header")) {
								dbContext.out().println("header schreiben");
								writeProductHeaderFields(recordChild, productEditor, dbContext);
							}
							else if (recordChild.getName().equals("row")) {
								dbContext.out().println("row schreiben");
								writeProductRowFields(recordChild, productEditor, dbContext);
							}
						}
					}

					// for testing the ProductEditor instance can be aborted
					// productEditor.abort();

					// saves the new product
					productEditor.commit();
					Product objectId = productEditor.objectId();
					String swd = objectId.getSwd();
					String idno = objectId.getIdno();
					// logs the process
					dbContext.out().println("commit and log ----------------->");
					bufferedWriter.write(swd + " - " + idno);
					bufferedWriter.newLine();
				}

				// according to the value of rollBack the transaction is either committed or rolled back
				if (rollBack) {
					transaction.rollback();
					dbContext.out().println("rollback");
					bufferedWriter.write("rollback");
					bufferedWriter.newLine();
				}
				else {
					transaction.commit();
					dbContext.out().println("commit");
					bufferedWriter.write("commit");
					bufferedWriter.newLine();
				}
			}
			else {
				dbContext.out().println("kein abas xml Format");
				bufferedWriter.write("kein abas xml Format");
				bufferedWriter.newLine();
			}
			bufferedWriter.write("end of program");
			bufferedWriter.newLine();
			bufferedWriter.close();
		}
		catch (JDOMException e) {
			dbContext.out().println(e.getMessage());
		}
		catch (IOException e) {
			dbContext.out().println(e.getMessage());
		}
	}

	/**
	 * Checks whether a product with the same search word exists.
	 * 
	 * @param dbContext The database context.
	 * @param swd The search word.
	 * @return Returns true if the a product with the search word exits, else returns false.
	 */
	private boolean isRecordExisting(DbContext dbContext, String swd) {
		SelectionBuilder<Product> selectionBuilder = SelectionBuilder.create(Product.class);
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
	 * Reads the information about every product's row from the XML file and creates table rows accordingly.
	 * 
	 * @param recordChild The child element of the 'record' element containing information about the record's table rows.
	 * @param productEditor The ProductEditor instance.
	 * @param dbContext The database context.
	 */
	private void writeProductRowFields(Element recordChild, ProductEditor productEditor, DbContext dbContext) {
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

	/**
	 * Reads the information about every product's head from the XML file and creates a product accordingly.
	 * 
	 * @param recordChild The child element of the 'record' element containing information about the record's head.
	 * @param productEditor The ProductEditor instance.
	 * @param dbContext The database context.
	 */
	private void writeProductHeaderFields(Element recordChild, ProductEditor productEditor, DbContext dbContext) {
		// gets the head fields as child elements of the 'header' element
		List<Element> fields = recordChild.getChildren();
		for (Element field : fields) {
			String fieldName = field.getAttributeValue("name");
			String fieldValue = field.getValue();
			dbContext.out().println(fieldName + " - " + fieldValue);
			productEditor.setString(fieldName, fieldValue);
		}
	}

}
