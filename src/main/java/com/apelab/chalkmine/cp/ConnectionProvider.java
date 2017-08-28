package com.apelab.chalkmine.cp;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Something that provides a connection which can be released by calling connection.close().
 *
 * @author Christian Hvid
 */

public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
