package com.apelab.chalkmine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.apelab.chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

public class HsqlPrimitiveMappingTest {
    @Before
    public void setup() {
        configureViaDriverManager("default", "jdbc:hsqldb:mem:aname", "sa", "");
        openConnection();
    }

    @After
    public void teardown() {
        closeConnection();
    }

    @Test
    public void testIntegerMapping() {
        assertEquals(42, (int) queryScalar(Integer.class, "select 42 from (values(0))"));
    }

    @Test
    public void testStringMapping() {
        assertEquals("hello", queryScalar(String.class, "select 'hello' from (values(0))"));
    }

    @Test
    public void testDoubleMapping() {
        assertEquals(8.7, queryScalar(Double.class, "select 8.7 from (values(0))"), 0.01);
    }

    @Test
    public void testBooleanMapping() {
        assertEquals(true, queryScalar(Boolean.class, "select 't' from (values(0))"));
        assertEquals(false, queryScalar(Boolean.class, "select 'f' from (values(0))"));
    }

    @Test
    public void testDateMapping() {
        assertEquals(new Date().getTime(), queryScalar(Date.class, "select ? from (values(0))", new Date()).getTime(), 1001);
    }
}