package chalkmine;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryManagerTest {
    @Mock
    private Connection connection;

    @Mock
    private ResultSet resultSet;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    public void test() throws SQLException {
        QueryManager queryManager = new QueryManager();

        when(connection.prepareStatement("select count(*) from test")).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenAnswer(new Answer<Boolean>() {
            int count = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                count ++;
                return count <= 1;
            }
        });

        when(resultSet.getInt(1)).thenReturn(42);

        int result = queryManager.queryScalar(connection, Integer.class, "select count(*) from test");

        Assert.assertEquals(42, result);
    }
}
