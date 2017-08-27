package chalkmine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: Nov 9, 2007
 *
 * @author Christian Hvid
 */

public class QueryManager {
    private Constructor findMatchingConstructor(Class klass, ResultSetMetaData metadata) throws SQLException {
        for (Constructor c : klass.getDeclaredConstructors()) {
            Class[] types = c.getParameterTypes();

            boolean matches = true;

            if (types.length == metadata.getColumnCount()) {
                for (int i = 0; i < metadata.getColumnCount(); i++) {
                    switch (metadata.getColumnType(i + 1)) {
                        case Types.BIGINT:
                        case Types.INTEGER:
                        case Types.TINYINT:
                            matches &= types[i].equals(int.class) || types[i].equals(long.class) ||
                                    types[i].equals(Integer.class) || types[i].equals(Long.class) ||
                                    types[i].equals(float.class) || types[i].equals(double.class) ||
                                    types[i].equals(Float.class) || types[i].equals(Double.class);

                            break;

                        case Types.VARCHAR:
                        case Types.CHAR:
                            matches &= types[i].equals(String.class) || types[i].isEnum() ||
                                    types[i].equals(boolean.class) || types[i].equals(Boolean.class);

                            break;

                        case Types.DATE:
                        case Types.TIMESTAMP:
                            matches &= types[i].equals(java.util.Date.class) || types[i].equals(java.sql.Date.class) ||
                                    types[i].equals(java.sql.Timestamp.class);

                            break;

                        case Types.DECIMAL:
                        case Types.DOUBLE:
                        case Types.FLOAT:
                            matches &= types[i].equals(float.class) || types[i].equals(double.class) ||
                                    types[i].equals(Float.class) || types[i].equals(Double.class);

                            break;

                        default:
                            matches &= true;
                            break;
                    }

                }
            } else {
                matches = false;
            }

            if (matches) return c;
        }

        String message = klass.getName() + "(";

        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            message += metadata.getColumnTypeName(i);
            if (i != metadata.getColumnCount()) message += ", ";
        }

        message += ")";

        throw new RuntimeException("Cannot find constructor matching " + message);
    }

    private boolean isSimpleType(Class klass) {
        return klass.equals(Long.class) || klass.equals(long.class) || klass.equals(Integer.class) ||
                klass.equals(Double.class) || klass.equals(String.class) || klass.equals(int.class) ||
                klass.equals(double.class) || klass.equals(Date.class) || klass.equals(java.util.Date.class) ||
                klass.equals(Boolean.class) || klass.equals(boolean.class) || klass.isEnum();
    }

    private <T> T readSimpleType(ResultSet rs, Class<T> klass, int index) throws SQLException {
        if (klass.equals(Long.class)) {
            return ((T) new Long(rs.getLong(index)));
        } else if (klass.equals(long.class)) {
            return ((T) new Long(rs.getLong(index)));
        } else if (klass.equals(Integer.class)) {
            return ((T) new Integer(rs.getInt(index)));
        } else if (klass.equals(Double.class)) {
            return ((T) new Double(rs.getDouble(index)));
        } else if (klass.equals(String.class)) {
            return ((T) rs.getString(index));
        } else if ((klass.equals(Boolean.class)) || (klass.equals(boolean.class))) {
            return ((T) (new Boolean("t".equalsIgnoreCase(rs.getString(index)))));
        } else if (klass.equals(int.class)) {
            return ((T) new Integer(rs.getInt(index)));
        } else if (klass.equals(double.class)) {
            return ((T) new Double(rs.getDouble(index)));
        } else if (klass.equals(java.util.Date.class)) {
            Timestamp timestamp = rs.getTimestamp(index);
            if (timestamp != null) return ((T) new java.util.Date(timestamp.getTime()));
            // else return (T) rs.getDate(index);
        } else if (klass.equals(java.sql.Date.class)) {
            return (T) rs.getDate(index);
        } else if (klass.isEnum()) {
            try {
                return (T) klass.getMethod("valueOf", String.class).invoke(null, rs.getString(index));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public <T> T queryScalar(Connection connection, Class<T> klass, String query, Object... parameters) throws SQLException {
        List<T> l = queryList(connection, klass, query, parameters);

        if (l.size() != 1)
            throw new NonScalarException("Query " + query + " returned " + l.size() + " rows. Expected exactly 1 row.");

        return l.get(0);
    }

    public <T> List<T> queryList(Connection connection, Class<T> klass, String query, Object... parameters) throws SQLException {
        List<T> result = new ArrayList<T>();

        PreparedStatement ps = connection.prepareStatement(query);

        try {
            addParametersToStatement(ps, parameters);

            ResultSet rs = ps.executeQuery();

            if (isSimpleType(klass)) {
                while (rs.next()) {
                    result.add(readSimpleType(rs, klass, 1));
                }
            } else {
                ResultSetMetaData rsmd = rs.getMetaData();

                Constructor<T> c = findMatchingConstructor(klass, rsmd);

                c.setAccessible(true);

                Class<?>[] parameterTypes = c.getParameterTypes();
                Object constructorParameters[] = new Object[parameterTypes.length];

                try {
                    while (rs.next()) {
                        for (int i = 0; i < parameterTypes.length; i++)
                            constructorParameters[i] = readSimpleType(rs, parameterTypes[i], i + 1);

                        result.add(c.newInstance(constructorParameters));
                    }
                } finally {
                    c.setAccessible(false);
                }
            }

            return result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
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
