package Session;

public class Session {
    // This class represents a session in the SQL engine.
    // It can be used to manage the current state of the session, such as the current database,
    // user permissions, and other session-specific information.

    private String currentDatabase;
    private static Session instance; 
 
    
    public Session() {
        // Initialize the session with no current database
        this.currentDatabase = null;
    }

    // Singleton instance
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setCurrentDatabase(String dbName) {
        this.currentDatabase = dbName;
    }

    public String getCurrentDatabase() {
        return this.currentDatabase;
    }

    public boolean isDatabaseSelected() {
        return this.currentDatabase != null;
    }
}
