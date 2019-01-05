package dk.brightworks.chalkmine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;

import static dk.brightworks.chalkmine.ChalkMineUtils.isSimpleType;

@SuppressWarnings("unchecked")
public class DefaultMapper<T> implements Mapper<T> {
    private Class<T> klass;
    private Constructor<T> c;
    private int offset;

    public DefaultMapper(Class<T> klass, ResultSetMetaData rsmd) throws SQLException {
        this(klass, rsmd, 0);
    }

    public DefaultMapper(Class<T> klass, ResultSetMetaData rsmd, int offset) throws SQLException {
        this.klass = klass;
        this.offset = offset;

        if (isSimpleType(klass)) {
            c = null;
        } else {
            c = findMatchingConstructor(klass, rsmd);
            c.setAccessible(true);
        }
    }

    public T map(ResultSet rs) throws SQLException {
        if (c != null) {
            Class<?>[] parameterTypes = c.getParameterTypes();
            Object constructorParameters[] = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++)
                constructorParameters[i] = readSimpleType(rs, parameterTypes[i], i + 1 + offset);

            try {
                return c.newInstance(constructorParameters);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        } else {
            return readSimpleType(rs, klass, 1 + offset);
        }
    }

    private <T> T readSimpleType(ResultSet rs, Class<T> klass, int index) throws SQLException {
        if (klass.equals(Long.class)) {
            return ((T) Long.valueOf(rs.getLong(index)));
        } else if (klass.equals(long.class)) {
            return ((T) Long.valueOf(rs.getLong(index)));
        } else if (klass.equals(Integer.class)) {
            return ((T) Integer.valueOf(rs.getInt(index)));
        } else if (klass.equals(Double.class)) {
            return ((T) Double.valueOf(rs.getDouble(index)));
        } else if (klass.equals(String.class)) {
            return ((T) rs.getString(index));
        } else if ((klass.equals(Boolean.class)) || (klass.equals(boolean.class))) {
            return ((T) (Boolean.valueOf("t".equalsIgnoreCase(rs.getString(index)))));
        } else if (klass.equals(int.class)) {
            return ((T) Integer.valueOf(rs.getInt(index)));
        } else if (klass.equals(double.class)) {
            return ((T) Double.valueOf(rs.getDouble(index)));
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

    private Constructor findMatchingConstructor(Class klass, ResultSetMetaData metadata) throws SQLException {
        for (Constructor c : klass.getDeclaredConstructors()) {
            Class[] types = c.getParameterTypes();

            boolean matches = true;

            if (types.length == metadata.getColumnCount() - offset) {
                for (int i = 0; i < metadata.getColumnCount()-offset; i++) {
                    switch (metadata.getColumnType(i + 1 + offset)) {
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

        for (int i = 1 + offset; i <= metadata.getColumnCount(); i++) {
            message += metadata.getColumnTypeName(i);
            if (i != metadata.getColumnCount()) message += ", ";
        }

        message += ")";

        throw new RuntimeException("Cannot find constructor matching " + message);
    }
}
