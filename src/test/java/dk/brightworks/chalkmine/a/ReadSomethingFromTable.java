package dk.brightworks.chalkmine.a;

import static dk.brightworks.chalkmine.ChalkMine.closeConnection;
import static dk.brightworks.chalkmine.ChalkMine.openConnection;
import static dk.brightworks.chalkmine.ChalkMine.queryScalar;

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
