package com.apelab.chalkmine.cp;

import com.apelab.chalkmine.ConfigurationProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Nov 15, 2008
 *
 * @author Christian Hvid
 */

public class SystemPropertiesConfigurationProvider implements ConfigurationProvider {
    private Map<String, ConnectionProvider> providers = new HashMap<String, ConnectionProvider>();

    public boolean hasConfiguration(String name) {
        return (System.getProperty(name + ".dbDataSource", null) != null) || (System.getProperty(name + ".dbUrl", null) != null);
    }

    public Connection getConnection(String name) throws SQLException {
        synchronized (this) {
            if (!providers.containsKey(name)) {
                if (System.getProperty(name + ".dbDataSource", null) != null) {
                    providers.put(name, ConnectionPool.createBySystemProperties(name));
                } else {
                    providers.put(name, SystemPropertiesDriverManagerConnectionProvider.create(name));
                }
            }
        }
        return providers.get(name).getConnection();
    }
}
