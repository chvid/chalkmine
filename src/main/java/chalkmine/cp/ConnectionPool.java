package chalkmine.cp;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection pool using ConnectionPoolDataSource.
 *
 * The pool will read a configuration similar to this:
 *
 * <pre>
 *     chalkmine.configurationProvider=system
 *     default.dbDataSource = com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
 *     default.dbPoolSize = 10
 *     default.dbPoolTimeout = 60
 *     default.dbDatabaseName = somedatabasename
 *     default.dbServerName = localhost
 *     default.dbUser = someuser
 *     default.dbPassword = password
 * </pre>
 *
 * Where dbDataSource name is the full classname of the ConnectionPoolDataSource.
 * Parameters dbPoolSize and dbPoolTimeout set pool size and timeout respective.
 *
 * The additional dbXxxxx parameters (dbDatabaseName, dbServerName, dbUser, dbPassword) are arbitrary properties of the
 * instantiated class and set using reflection.
 *
 * @author Christian Hvid
 */

public class ConnectionPool implements ConnectionProvider, ConnectionEventListener {
    private static Logger logger = Logger.getLogger(ConnectionPool.class.getName());

    private ConnectionPoolDataSource dataSource;
    private int timeout;
    private Stack<PooledConnection> recycledConnections;
    private Semaphore semaphore;

    private static ConnectionPoolDataSource createConnectionPoolFromSystemProperties(String name) throws SQLException {
        try {
            Class dataSourceClass = Class.forName(System.getProperty(name + ".dbDataSource"));
            ConnectionPoolDataSource dataSource = (ConnectionPoolDataSource) dataSourceClass.newInstance();

            for (Enumeration e = System.getProperties().keys(); e.hasMoreElements(); ) {
                String systemPropertyName = (String) e.nextElement();

                if (systemPropertyName.startsWith(name + ".db")) {
                    String methodName = "set" + systemPropertyName.substring((name + ".db").length());
                    String propertyValue = System.getProperty(systemPropertyName);

                    try {
                        Method m = dataSourceClass.getMethod(methodName, String.class);
                        m.invoke(dataSource, propertyValue);
                    } catch (NoSuchMethodException ignored) {
                        // ignored
                    }

                    try {
                        Method m = dataSourceClass.getMethod(methodName, Integer.class);
                        m.invoke(dataSource, Integer.parseInt(propertyValue));
                    } catch (NoSuchMethodException ignored) {
                        // ignored
                    }
                }
            }

            return dataSource;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConnectionPool createBySystemProperties(String name) {
        try {
            ConnectionPoolDataSource dataSource = createConnectionPoolFromSystemProperties(name);

            return new ConnectionPool(
                    dataSource,
                    Integer.parseInt(System.getProperty(name + ".dbPoolSize", "10")),
                    Integer.parseInt(System.getProperty(name + ".dbPoolTimeout", "60"))
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ConnectionPool(ConnectionPoolDataSource dataSource, int maxConnections, int timeout) {
        this.dataSource = dataSource;
        this.timeout = timeout;
        recycledConnections = new Stack<PooledConnection>();
        semaphore = new Semaphore(maxConnections, true);
    }

    public Connection getConnection() throws SQLException {
        try {
            if (semaphore.tryAcquire(timeout, TimeUnit.SECONDS)) {
                synchronized (this) {
                    PooledConnection pooledConnection;
                    if (!recycledConnections.empty()) {
                        pooledConnection = recycledConnections.pop();
                    } else {
                        pooledConnection = dataSource.getPooledConnection();
                    }
                    pooledConnection.addConnectionEventListener(this);
                    return pooledConnection.getConnection();
                }
            } else {
                throw new RuntimeException("Timeout acquiring connection, timeout is " + timeout + " sec.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectionClosed(ConnectionEvent event) {
        semaphore.release();
        PooledConnection pooledConnection = (PooledConnection) event.getSource();
        pooledConnection.removeConnectionEventListener(this);
        recycledConnections.push(pooledConnection);
    }

    public void connectionErrorOccurred(ConnectionEvent event) {
        semaphore.release();
        PooledConnection pooledConnection = (PooledConnection) event.getSource();
        pooledConnection.removeConnectionEventListener(this);
        try {
            pooledConnection.close();
        } catch (SQLException e) {
            logger.log(Level.INFO, "Error while closing database connection: " + e, e);
        }
    }
}
