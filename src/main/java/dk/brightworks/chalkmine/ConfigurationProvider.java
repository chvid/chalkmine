package dk.brightworks.chalkmine;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConfigurationProvider {
    boolean hasConfiguration(String name);

    Connection getConnection(String name) throws SQLException;
}
