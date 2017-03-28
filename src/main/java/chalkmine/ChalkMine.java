package chalkmine;

import chalkmine.cp.JndiConfigurationProvider;
import chalkmine.cp.SystemPropertiesConfigurationProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Date: Nov 9, 2007
 *
 * @author Christian Hvid
 */

public class ChalkMine {
    private static QueryManager queryManager = null;
    private static ConnectionManager connectionManager = null;

    private synchronized static QueryManager getQueryManager() {
        if (queryManager == null) queryManager = new QueryManager();
        return queryManager;
    }

    private synchronized static ConnectionManager getConnectionManager() {
        if (connectionManager == null) {
            String connectionProviderName = System.getProperty("chalkmine.configurationProvider");

            if (connectionProviderName == null)
                connectionProviderName = JndiConfigurationProvider.class.getName();
            else if (connectionProviderName.equals("system"))
                connectionProviderName = SystemPropertiesConfigurationProvider.class.getName();
            else if (connectionProviderName.equals("jndi"))
                connectionProviderName = JndiConfigurationProvider.class.getName();

            try {
                connectionManager = new ConnectionManager(
                        (ConfigurationProvider) Class.forName(connectionProviderName).newInstance()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return connectionManager;
    }

    public static Connection getConnection() {
        return getConnectionManager().getConnection();
    }

    public static void configureViaDriverManager(String name, String url, String user, String password) {
        System.setProperty("chalkmine.configurationProvider", "system");
        System.setProperty(name + ".dbUrl", url);
        System.setProperty(name + ".dbUser", user);
        System.setProperty(name + ".dbPassword", password);
    }

    public static void openConnection() {
        try {
            getConnectionManager().openConnection();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public static void openConnection(String cfg) {
        try {
            getConnectionManager().openConnection(cfg);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public static void closeConnection() {
        try {
            getConnectionManager().closeConnection();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public static <T> T queryScalar(Class<T> klass, String statement, Object... parameters) {
        try {
            return getQueryManager().queryScalar(getConnection(), klass, statement, parameters);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public static <T> List<T> queryList(Class<T> klass, String statement, Object... parameters) {
        try {
            return getQueryManager().queryList(getConnection(), klass, statement, parameters);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public static int update(String statement, Object... parameters) {
        try {
            return getQueryManager().update(getConnection(), statement, parameters);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}