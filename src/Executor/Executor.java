package Executor;

import Session.Session;
import CatalogManager.CatalogManager;
import Util.Logger;

public class Executor {
    // Executes SQL commands by interacting with CatalogManager and using Session for current DB context
    
    private final CatalogManager catalogManager;
    private final Session session;
    private static Executor instance;

    public Executor(CatalogManager catalogManager, Session session) {
        this.catalogManager = catalogManager;
        this.session = session;
    }
 
    // Public method to provide access to the single instance
    public static synchronized Executor getInstance() {
        if (instance == null) {
            instance = new Executor(CatalogManager.getInstance(), Session.getInstance());
        }
        return instance;
    }

    // Database operations

    public void executeCreateDatabase(String dbName) throws Exception {
        if (catalogManager.databaseExists(dbName)) {
            throw new Exception("Database already exists: " + dbName);
        } else {
            catalogManager.createDatabase(dbName);
            System.out.println("Database created: " + dbName);
            Logger.info("Created DB: " + dbName);
        }
    }

    

    public void executeDropDatabase(String dbName) throws Exception {
        if (!catalogManager.databaseExists(dbName)) {
            throw new Exception("Database does not exist: " + dbName);
        }
        catalogManager.dropDatabase(dbName);
        // If the dropped DB was current, clear session DB
        if (dbName.equals(session.getCurrentDatabase())) {
            session.setCurrentDatabase(null);
        }
    System.out.println("Database dropped: " + dbName);
    Logger.info("Dropped DB: " + dbName);
    }

    public void executeUseDatabase(String dbName) throws Exception {
        if (!catalogManager.databaseExists(dbName)) {
            throw new Exception("Database does not exist: " + dbName);
        }
        session.setCurrentDatabase(dbName);
    System.out.println("Using database: " + dbName);
    Logger.info("USE " + dbName);
    }

    public void executeShowCurrentDatabase() {
        String currentDb = session.getCurrentDatabase();
        if (currentDb != null) {
            System.out.println("Current database: " + currentDb);
        } else {
            System.out.println("No database selected.");
        }
    }

    // Table operations are executed via Parser.AstExecutor and Util.TableEngine; DB-level ops remain here.

    public void executeShowDatabases() {
        for (String db : catalogManager.listDatabases()) {
            System.out.println(db);
        }
    }

    public void executeShowTables(String dbNameOrNull) {
        String db = dbNameOrNull != null ? dbNameOrNull : session.getCurrentDatabase();
        if (db == null || db.isBlank()) {
            System.out.println("No database selected.");
            return;
        }
        for (String t : catalogManager.listTables(db)) {
            System.out.println(t);
        }
    }
}
