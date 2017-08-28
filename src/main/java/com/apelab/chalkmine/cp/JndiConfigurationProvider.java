package com.apelab.chalkmine.cp;

import com.apelab.chalkmine.ConfigurationProvider;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Nov 19, 2008
 *
 * @author Christian Hvid
 */

public class JndiConfigurationProvider implements ConfigurationProvider {
    private Map<String, DataSource> dataSources = new HashMap<String, DataSource>();

    private DataSource lookupDataSource(String name) throws NamingException {
        return (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/" + name.replace('.', '/'));
    }

    public boolean hasConfiguration(String name) {
        try {
            lookupDataSource(name);
            return true;
        } catch (NamingException e) {
            return false;
        }
    }

    public Connection getConnection(String name) throws SQLException {
        synchronized (this) {
            try {
                if (!dataSources.containsKey(name)) dataSources.put(name, lookupDataSource(name));
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }

        return dataSources.get(name).getConnection();
    }
}
