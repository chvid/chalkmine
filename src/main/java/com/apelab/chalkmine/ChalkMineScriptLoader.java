package com.apelab.chalkmine;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Created on 28/08/2017.
 *
 * @author Christian Hvid
 */
public class ChalkMineScriptLoader {
    private String filterComments(String text) {
        return text.replaceAll("/\\*[^*]*\\*/", "").replaceAll("--[^\\n]*\\n", "\n").trim();
    }

    public void loadScript(Connection connection, InputStream inputStream) throws SQLException {
        Scanner scanner = new Scanner(inputStream);
        scanner.useDelimiter(";");
        Statement statement = connection.createStatement();
        try {
            while (scanner.hasNext()) {
                statement.execute(filterComments(scanner.next()));
            }
        } finally {
            statement.close();
        }
    }
}
