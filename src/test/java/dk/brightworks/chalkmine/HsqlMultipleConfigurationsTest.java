package dk.brightworks.chalkmine;

import dk.brightworks.chalkmine.a.ReadSomethingFromTable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static dk.brightworks.chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

public class HsqlMultipleConfigurationsTest {
    @Before
    public void setup() {
        configureViaDriverManager("dk.brightworks.chalkmine.a", "jdbc:hsqldb:mem:a", "sa", "");
        configureViaDriverManager("dk.brightworks.chalkmine.b", "jdbc:hsqldb:mem:b", "sa", "");
        openConnection("dk.brightworks.chalkmine.a");
        try {
            update("create table a ( t varchar(200) )");
            update("insert into a(t) values(?)", "a");
        } finally {
            closeConnection();
        }
        openConnection("dk.brightworks.chalkmine.b");
        try {
            update("create table a ( t varchar(200) )");
            update("insert into a(t) values(?)", "b");
        } finally {
            closeConnection();
        }
    }

    @After
    public void teardown() {
        openConnection("dk.brightworks.chalkmine.a");
        try {
            update("drop table a");
        } finally {
            closeConnection();
        }
        openConnection("dk.brightworks.chalkmine.b");
        try {
            update("drop table a");
        } finally {
            closeConnection();
        }
    }

    @Test
    public void testA() {
        Assert.assertEquals("a", ReadSomethingFromTable.read());
    }

    @Test
    public void testB() {
        Assert.assertEquals("b", dk.brightworks.chalkmine.b.ReadSomethingFromTable.read());
    }
}
