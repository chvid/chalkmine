package dk.brightworks.chalkmine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChalkMineScriptLoaderTest {
    @Mock
    Connection connection;

    @Mock
    Statement statement;

    @Test
    public void basic() throws SQLException {
        InputStream is = getClass().getResourceAsStream("/ChalkMineScriptLoaderTest.sql");
        when(connection.createStatement()).thenReturn(statement);
        new ChalkMineScriptLoader().loadScript(connection, is);
        verify(statement).execute("create table banana (\n" +
                "    one varchar(200), \n" +
                "    two varchar(50)\n" +
                ")");
        verify(statement).execute("create table apple (\n" +
                "    one number,\n" +
                "    two boolean\n" +
                ")");
    }
}
