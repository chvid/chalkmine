package dk.brightworks.chalkmine;

import java.sql.SQLException;

public class SQLRuntimeException extends RuntimeException {
    public SQLRuntimeException(SQLException e) {
        super(e);
    }
}
