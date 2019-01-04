package dk.brightworks.chalkmine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryManager {
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
        List<T> result = new ArrayList<T>();

        PreparedStatement ps = connection.prepareStatement(query);

        try {
            addParametersToStatement(ps, parameters);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(mapper.map(rs));
            }

            return result;
        } finally {
            ps.close();
        }
    }

    public <T> List<T> queryList(Connection connection, Class<T> klass, String query, Object... parameters) throws SQLException {
        List<T> result = new ArrayList<T>();

        PreparedStatement ps = connection.prepareStatement(query);

        try {
            addParametersToStatement(ps, parameters);

            ResultSet rs = ps.executeQuery();

            Mapper<T> mapper = new DefaultMapper<T>(klass, rs.getMetaData());

            while (rs.next()) {
                result.add(mapper.map(rs));
            }

            return result;
        } finally {
            ps.close();
        }
    }

    public int update(Connection connection, String statement, Object... parameters) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(statement);

        try {
            addParametersToStatement(ps, parameters);

            return ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    private void addParametersToStatement(PreparedStatement ps, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++)
            if ((parameters[i] != null) && (parameters[i].getClass().equals(java.util.Date.class)))
                ps.setTimestamp(i + 1, new Timestamp(((java.util.Date) parameters[i]).getTime()));
            else if ((parameters[i] != null) && (parameters[i].getClass().isEnum()))
                ps.setString(i + 1, "" + parameters[i]);
            else if ((parameters[i] != null) && (parameters[i].getClass().equals(Boolean.class) || parameters[i].getClass().equals(boolean.class))) {
                if ((Boolean) parameters[i])
                    ps.setString(i + 1, "t");
                else
                    ps.setString(i + 1, "f");
            } else ps.setObject(i + 1, parameters[i]);
    }
}
