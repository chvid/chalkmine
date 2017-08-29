package com.apelab.chalkmine;

import com.apelab.chalkmine.cp.JndiConfigurationProvider;
import com.apelab.chalkmine.cp.SystemPropertiesConfigurationProvider;

import java.io.InputStream;
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
    private static ChalkMineScriptLoader scriptLoader = new ChalkMineScriptLoader();

    private synchronized static QueryManager getQueryManager() {
        if (queryManager == null) queryManager = new QueryManager();
        return queryManager;
    }

    private synchronized static ChalkMineScriptLoader getScriptLoader() {
        if (scriptLoader == null) scriptLoader = new ChalkMineScriptLoader();
        return scriptLoader;
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

    public static <T> T queryScalar(Mapper<T> mapper, String statement, Object... parameters) {
        try {
            return getQueryManager().queryScalar(getConnection(), mapper, statement, parameters);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public static <T> List<T> queryList(Mapper<T> mapper, String statement, Object... parameters) {
        try {
            return getQueryManager().queryList(getConnection(), mapper, statement, parameters);
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

    public static void loadScript(InputStream inputStream) {
        try {
            getScriptLoader().loadScript(getConnection(), inputStream);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}