package com.apelab.chalkmine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionManagerTest {
    @Mock
    private ConfigurationProvider configurationProvider;

    @Mock
    private Connection connection;

    @Test
    public void openAndCloseConnection() throws SQLException {
        when(configurationProvider.getConnection("default")).thenReturn(connection);

        when(configurationProvider.hasConfiguration("default")).thenReturn(true);

        ConnectionManager connectionManager = new ConnectionManager(configurationProvider);

        connectionManager.openConnection();

        verify(configurationProvider, times(1)).getConnection("default");

        assertEquals(this.connection, connectionManager.getConnection());

        connectionManager.closeConnection();;

        verify(connection, times(1)).close();

        assertEquals(null, connectionManager.getConnection());
    }
}
