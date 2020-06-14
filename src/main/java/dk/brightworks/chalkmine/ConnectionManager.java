package dk.brightworks.chalkmine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

/**
 * Date: Nov 9, 2007
 *
 * @author Christian Hvid
 */

public class ConnectionManager {
    private ThreadLocal<Stack<Connection>> connection = new ThreadLocal<Stack<Connection>>();
    private ConfigurationProvider configurationProvider;

    public ConnectionManager(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    private static String findCallingClass(int level) {
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement st[] = e.getStackTrace();
            return st[level].getClassName();
        }
    }

    private String findConfiguration(String name) {
        String elements[] = name.split("\\.");

        for (int i = elements.length; i >= 1; i--) {
            String names = "";

            for (int j = 0; j < i; j++) names += elements[j] + ".";

            names = names.substring(0, names.length() - 1);

            if (configurationProvider.hasConfiguration(names)) return names;
        }

        if (configurationProvider.hasConfiguration("default")) return "default";

        throw new RuntimeException("Cannot find configuration for " + name + ". Define at least a default configuration.");
    }

    public void openConnection() throws SQLException {
        openConnection(findCallingClass(3));
    }

    public void openConnection(String name) throws SQLException {
        String configuration = findConfiguration(name);

        if (connection.get() == null) connection.set(new Stack<Connection>());

        connection.get().push(configurationProvider.getConnection(configuration));
    }

    public Connection getConnection() {
        if (connection.get() == null) return null;
        return connection.get().peek();
    }

    public void closeConnection() throws SQLException {
        if (connection.get() == null || connection.get().peek() == null) {
            throw new RuntimeException("Connection already closed");
        }

        Connection c = this.connection.get().pop();
        PreparedStatementCache.closePreparedStatements(c);
        c.close();

        if (this.connection.get().isEmpty()) this.connection.remove();
    }
}
