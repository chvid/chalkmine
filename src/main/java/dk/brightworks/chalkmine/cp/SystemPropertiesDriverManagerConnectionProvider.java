package dk.brightworks.chalkmine.cp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SystemPropertiesDriverManagerConnectionProvider implements ConnectionProvider {
    private String url;
    private String password;
    private String user;

    public static ConnectionProvider create(String name) {
        return new SystemPropertiesDriverManagerConnectionProvider(
                System.getProperty(name + ".dbUrl"),
                System.getProperty(name + ".dbUser", null),
                System.getProperty(name + ".dbPassword", null
                )
        );
    }

    public SystemPropertiesDriverManagerConnectionProvider(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
