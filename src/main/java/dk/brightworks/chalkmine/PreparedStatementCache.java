package dk.brightworks.chalkmine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PreparedStatementCache {
    private static Map<Connection, Map<String, PreparedStatement>> cache = new HashMap<>();
    private static Object lock = new Object();

    public static PreparedStatement createPreparedStatement(Connection connection, String query) throws SQLException {
        synchronized (lock) {
            if (!cache.containsKey(connection)) {
                cache.put(connection, new HashMap<>());
            }
            if (!cache.get(connection).containsKey(query)) {
                cache.get(connection).put(query, connection.prepareStatement(query));
            }
            return cache.get(connection).get(query);
        }
    }

    public static void closePreparedStatements(Connection connection) throws SQLException {
        synchronized (lock) {
            if (cache.containsKey(connection)) {
                for (PreparedStatement ps : cache.get(connection).values()) {
                    ps.close();
                }
                cache.remove(connection);
            }
        }
    }


}
