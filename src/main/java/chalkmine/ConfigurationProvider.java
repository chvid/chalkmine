package chalkmine;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Date: Sep 20, 2008
 *
 * @author Christian Hvid
 */

public interface ConfigurationProvider {
    boolean hasConfiguration(String name);

    Connection getConnection(String name) throws SQLException;
}
