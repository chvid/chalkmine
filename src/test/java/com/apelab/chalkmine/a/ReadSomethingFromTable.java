package chalkmine.a;

import static chalkmine.ChalkMine.closeConnection;
import static chalkmine.ChalkMine.openConnection;
import static chalkmine.ChalkMine.queryScalar;

public class ReadSomethingFromTable {
    public static String read() {
        openConnection();
        try {
            return queryScalar(String.class, "select t from a");
        } finally {
            closeConnection();
        }
    }
}
