package chalkmine;

import java.sql.SQLException;

/**
 * Date: Nov 16, 2008
 *
 * @author Christian Hvid
 */

public class SQLRuntimeException extends RuntimeException {
    public SQLRuntimeException(SQLException e) {
        super(e);
    }
}
