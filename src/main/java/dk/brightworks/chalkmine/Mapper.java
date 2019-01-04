package dk.brightworks.chalkmine;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface Mapper<T> {
    T map(ResultSet rs) throws SQLException;
}
