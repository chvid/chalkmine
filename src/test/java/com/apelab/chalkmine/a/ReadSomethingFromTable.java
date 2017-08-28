package com.apelab.chalkmine.a;

import static com.apelab.chalkmine.ChalkMine.closeConnection;
import static com.apelab.chalkmine.ChalkMine.openConnection;
import static com.apelab.chalkmine.ChalkMine.queryScalar;

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
