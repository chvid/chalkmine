package chalkmine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

public class HsqlMultipleConfigurationsTest {
    @Before
    public void setup() {
        configureViaDriverManager("chalkmine.a", "jdbc:hsqldb:mem:a", "sa", "");
        configureViaDriverManager("chalkmine.b", "jdbc:hsqldb:mem:b", "sa", "");
        openConnection("chalkmine.a");
        try {
            update("create table a ( t varchar(200) )");
            update("insert into a(t) values(?)", "a");
        } finally {
            closeConnection();
        }
        openConnection("chalkmine.b");
        try {
            update("create table a ( t varchar(200) )");
            update("insert into a(t) values(?)", "b");
        } finally {
            closeConnection();
        }
    }

    @After
    public void teardown() {
        openConnection("chalkmine.a");
        try {
            update("drop table a");
        } finally {
            closeConnection();
        }
        openConnection("chalkmine.b");
        try {
            update("drop table a");
        } finally {
            closeConnection();
        }
    }

    @Test
    public void testA() {
        assertEquals("a", chalkmine.a.ReadSomethingFromTable.read());
    }

    @Test
    public void testB() {
        assertEquals("b", chalkmine.b.ReadSomethingFromTable.read());
    }
}
