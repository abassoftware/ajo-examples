package de.abas.examples.keytable;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.Query;
import de.abas.erp.db.schema.company.KeyTable;
import de.abas.erp.db.schema.company.KeyTable.Row;
import de.abas.erp.db.selection.SelectionBuilder;
import de.abas.examples.common.AbstractAjoAccess;

/**
 * This class selects all keys and its table rows. It works in client and server mode.
 * 
 * @author abas Software AG
 * @version 1.0
 *
 */
public class SelectionKeyTable extends AbstractAjoAccess {

    /**
     * The main method to start the class in client mode.
     * 
     * @param args
     */
    public static void main(String[] args) {
        new SelectionKeyTable().runClientProgram(args);
    }
    
    @Override
    public void run(String[] args) {
        DbContext dbContext = getDbContext();
        
        // selects all keys
        SelectionBuilder<KeyTable> selectionBuilder = SelectionBuilder.create(KeyTable.class);
        Query<KeyTable> queryKeyTable = dbContext.createQuery(selectionBuilder.build());
        
        // iterates all keys
        for (KeyTable keyTable : queryKeyTable) {
            dbContext.out().println(keyTable.getIdno() + " - " + keyTable.getSwd() + " - " + keyTable.getType().getDisplayString());
            // gets each key's table rows
            Iterable<Row> rows = keyTable.table().getRows();
            // iterates each key's table rows
            for (Row row : rows) {
                dbContext.out().println("-- " + row.getPartialKey() + " - " + row.getPartialKeyMeaning());
            }
        }        
    }
}
