package dk.brightworks.chalkmine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static dk.brightworks.chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

public class HsqlIntegrationTest {
    public static class User {
        private String name;
        private String country;
        private int yearOfBirth;

        public User(String name, String country, int yearOfBirth) {
            this.name = name;
            this.country = country;
            this.yearOfBirth = yearOfBirth;
        }

        public String getName() {
            return name;
        }

        public String getCountry() {
            return country;
        }

        public int getYearOfBirth() {
            return yearOfBirth;
        }

        public String toString() {
            return name + " from " + country + " born in " + yearOfBirth;
        }
    }

    @Before
    public void setup() {
        configureViaDriverManager("default", "jdbc:hsqldb:mem:aname", "sa", "");

        openConnection();

        try {
            update("create table users ( name varchar(200), country varchar(200), year_of_birth integer )");
            update("insert into users(name, country, year_of_birth) values(?, ?, ?)", "Christian", "Denmark", 1973);
            update("insert into users(name, country, year_of_birth) values(?, ?, ?)", "Michelle", "Australia", 1982);

        } finally {
            closeConnection();
        }
    }

    @After
    public void teardown() {
        openConnection();
        try {
            update("drop table users");
        } finally {
            closeConnection();
        }
    }

    @Test
    public void testQueryScalar() {
        openConnection();
        try {
            User user = queryScalar(User.class, "select name, country, year_of_birth from users where name = ?", "Christian");
            assertEquals("Christian", user.getName());
            assertEquals("Denmark", user.getCountry());
            assertEquals(1973, user.getYearOfBirth());
        } finally {
            closeConnection();
        }
    }

    @Test
    public void testQueryList() {
        openConnection();
        try {
            List<User> users = queryList(User.class, "select name, country, year_of_birth from users order by name");
            assertEquals("Christian", users.get(0).getName());
            assertEquals("Michelle", users.get(1).getName());
        } finally {
            closeConnection();
        }
    }

    @Test
    public void testQueryMap() {
        openConnection();
        try {
            Map<String, Integer> counts = queryMap(String.class, Integer.class, "select country, count(*) from users group by country");
            assertEquals(1, (int) counts.get("Australia"));
        } finally {
            closeConnection();
        }
    }

    @Test
    public void testBatch() {
        openConnection();
        try {
            int count = queryScalar(Integer.class, "select count(*) from users");
            for (int i = 0; i < 100; i++) {
                updateBatch("insert into users(name) values(?)", "test-" + i);
            }
            doBatch();
            assertEquals(count + 100, (int) queryScalar(Integer.class, "select count(*) from users"));
            for (int i = 0; i < 100; i++) {
                updateBatch("delete from users where name = ?", "test-" + i);
            }
            doBatch();
            assertEquals(count, (int) queryScalar(Integer.class, "select count(*) from users"));
        } finally {
            closeConnection();
        }
    }
}
