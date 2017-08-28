package com.apelab.chalkmine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static com.apelab.chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

public class HsqlExceptionTest {
    @Before
    public void setup() {
        configureViaDriverManager("default", "jdbc:hsqldb:mem:aname", "sa", "");
        openConnection();
        update("create table test ( x integer )");
    }

    @After
    public void teardown() {
        update("drop table test");
        closeConnection();
    }

    @Test
    public void sqlExceptionTest() {
        try {
            update("some broken sql");
            assertEquals("Should not get here", "");
        } catch (SQLRuntimeException e) {
            assertEquals(true, e.getCause() instanceof SQLException);
        }
    }

    @Test
    public void nonScalarExceptionTest() {
        try {
            queryScalar(Integer.class, "select x from test");
            assertEquals("Should not get here", "");
        } catch (NonScalarException e) {
            assertEquals(true, true);
        }
    }
}
