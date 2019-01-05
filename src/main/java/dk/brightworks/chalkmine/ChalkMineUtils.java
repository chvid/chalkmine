package dk.brightworks.chalkmine;

import java.sql.Date;

public class ChalkMineUtils {
    public static boolean isSimpleType(Class klass) {
        return klass.equals(Long.class) || klass.equals(long.class) || klass.equals(Integer.class) ||
                klass.equals(Double.class) || klass.equals(String.class) || klass.equals(int.class) ||
                klass.equals(double.class) || klass.equals(Date.class) || klass.equals(java.util.Date.class) ||
                klass.equals(Boolean.class) || klass.equals(boolean.class) || klass.isEnum();
    }
}
