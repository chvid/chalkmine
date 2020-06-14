package dk.brightworks.chalkmine;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class QueryManager {
    private static boolean TIME_QUERIES = System.getProperty("chalkmine.timeQueries", "false").equals("true");

    private static Logger logger = Logger.getLogger(QueryManager.class.getName());

    public <T> T queryScalar(Connection connection, Mapper<T> mapper, String query, Object... parameters) throws SQLException {
        List<T> l = queryList(connection, mapper, query, parameters);

        if (l.size() != 1)
            throw new NonScalarException("Query " + query + " returned " + l.size() + " rows. Expected exactly 1 row.");

        return l.get(0);
    }

    public <T> T queryScalar(Connection connection, Class<T> klass, String query, Object... parameters) throws SQLException {
        List<T> l = queryList(connection, klass, query, parameters);

        if (l.size() != 1)
            throw new NonScalarException("Query " + query + " returned " + l.size() + " rows. Expected exactly 1 row.");

        return l.get(0);
    }

    public <T> List<T> queryList(Connection connection, Mapper<T> mapper, String query, Object... parameters) throws SQLException {
        long startTime = TIME_QUERIES ? System.currentTimeMillis() : 0;

        List<T> result = new ArrayList<T>();

        PreparedStatement ps = PreparedStatementCache.createPreparedStatement(connection, query);

        try {
            addParametersToStatement(connection, ps, parameters);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(mapper.map(rs));
            }

            return result;
        } finally {
            if (TIME_QUERIES) {
                logger.info("queryList(" + query + ") completed in " + (System.currentTimeMillis() - startTime) + " msec.");
            }
        }
    }

    public <T> List<T> queryList(Connection connection, Class<T> klass, String query, Object... parameters) throws SQLException {
        long startTime = TIME_QUERIES ? System.currentTimeMillis() : 0;

        List<T> result = new ArrayList<T>();

        PreparedStatement ps = PreparedStatementCache.createPreparedStatement(connection, query);

        try {
            addParametersToStatement(connection, ps, parameters);

            ResultSet rs = ps.executeQuery();

            Mapper<T> mapper = new DefaultMapper<T>(klass, rs.getMetaData());

            while (rs.next()) {
                result.add(mapper.map(rs));
            }

            return result;
        } finally {
            if (TIME_QUERIES) {
                logger.info("queryList(" + query + ") completed in " + (System.currentTimeMillis() - startTime) + " msec.");
            }
        }
    }

    public <K, V> Map<K, V> queryMap(Connection connection, Class<K> keyKlass, Class<V> valueKlass, String query, Object... parameters) throws SQLException {
        long startTime = TIME_QUERIES ? System.currentTimeMillis() : 0;

        Map<K, V> result = new HashMap<>();

        PreparedStatement ps = PreparedStatementCache.createPreparedStatement(connection, query);
        try {
            addParametersToStatement(connection, ps, parameters);

            ResultSet rs = ps.executeQuery();

            Mapper<K> keyMapper = new DefaultMapper<K>(keyKlass, rs.getMetaData(), 0);
            Mapper<V> valueMapper = new DefaultMapper<V>(valueKlass, rs.getMetaData(), 1);

            while (rs.next()) {
                K k = keyMapper.map(rs);
                V v = valueMapper.map(rs);
                result.put(k, v);
            }

            return result;
        } finally {
            if (TIME_QUERIES) {
                logger.info("queryMap(" + query + ") completed in " + (System.currentTimeMillis() - startTime) + " msec.");
            }
        }
    }

    public int update(Connection connection, String statement, Object... parameters) throws SQLException {
        long startTime = TIME_QUERIES ? System.currentTimeMillis() : 0;

        PreparedStatement ps = PreparedStatementCache.createPreparedStatement(connection, statement);

        try {
            addParametersToStatement(connection, ps, parameters);

            return ps.executeUpdate();
        } finally {
            if (TIME_QUERIES) {
                logger.info("update(" + statement + ") completed in " + (System.currentTimeMillis() - startTime) + " msec.");
            }
        }
    }

    private String findSqlType(Class klass) {
        return "VARCHAR";
    }

    private void addParametersToStatement(Connection connection, PreparedStatement ps, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] != null && parameters[i].getClass().equals(java.util.Date.class)) {
                ps.setTimestamp(i + 1, new Timestamp(((java.util.Date) parameters[i]).getTime()));
            } else if (parameters[i] != null && parameters[i].getClass().isEnum()) {
                ps.setString(i + 1, "" + parameters[i]);
            } else if (parameters[i] != null && (parameters[i].getClass().equals(Boolean.class) || parameters[i].getClass().equals(boolean.class))) {
                if ((Boolean) parameters[i]) {
                    ps.setString(i + 1, "t");
                } else {
                    ps.setString(i + 1, "f");
                }
            } else if (parameters[i] != null && parameters[i].getClass().isArray()) {
                ps.setArray(i + 1, connection.createArrayOf(findSqlType(parameters[i].getClass()), (Object[]) parameters[i]));
            } else {
                ps.setObject(i + 1, parameters[i]);
            }
        }
    }

    ThreadLocal<List<PreparedStatement>> batchStatements = new ThreadLocal<>();

    public void updateBatch(Connection connection, String statement, Object... parameters) throws SQLException {
        long startTime = TIME_QUERIES ? System.currentTimeMillis() : 0;

        if (batchStatements.get() == null) {
            batchStatements.set(new ArrayList<>());
        }

        PreparedStatement ps = PreparedStatementCache.createPreparedStatement(connection, statement);

        try {
            addParametersToStatement(connection, ps, parameters);
            ps.addBatch();
            if (!batchStatements.get().contains(ps)) {
                batchStatements.get().add(ps);
            }
        } finally {
            if (TIME_QUERIES) {
                logger.info("updateBatch(" + statement + ") completed in " + (System.currentTimeMillis() - startTime) + " msec.");
            }
        }
    }

    public void doBatch() throws SQLException {
        long startTime = TIME_QUERIES ? System.currentTimeMillis() : 0;
        try {
            for (PreparedStatement ps : batchStatements.get()) {
                ps.executeBatch();
            }
        } finally {
            batchStatements.remove();
            if (TIME_QUERIES) {
                logger.info("doBatch() completed in " + (System.currentTimeMillis() - startTime) + " msec.");
            }
        }
    }
}
