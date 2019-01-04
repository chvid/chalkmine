package dk.brightworks.chalkmine;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.sql.*;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMapperTest {
    @Mock
    private Connection connection;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData rsmd;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    public void test() throws SQLException {
        DefaultMapper<Integer> defaultMapper = new DefaultMapper<Integer>(Integer.class, rsmd);

        when(connection.prepareStatement("select count(*) from test")).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenAnswer(new Answer<Boolean>() {
            int count = 0;

            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                count++;
                return count <= 1;
            }
        });

        when(resultSet.getInt(1)).thenReturn(42);

        Assert.assertEquals(42, (long)defaultMapper.map(resultSet));
    }
}
