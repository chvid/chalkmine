package dk.brightworks.chalkmine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static dk.brightworks.chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

public class HsqlComplexMappingTest {
    public static class ComplexObject {
        private String a;
        private Date b;
        private int c;

        public ComplexObject(String a, Date b) {
            this.a = a;
            this.b = b;
        }

        public ComplexObject(String a, int c) {
            this.a = a;
            this.c = c;
        }
     }

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
    public void testAcConstructor() {
        assertEquals(42, queryScalar(ComplexObject.class, "select 'test', 42 from (values(0))").c);
        assertEquals("test", queryScalar(ComplexObject.class, "select 'test', 42 from (values(0))").a);
    }

    @Test
    public void testAbConstructor() {
        assertEquals(new Date().getTime(), queryScalar(ComplexObject.class, "select 'test', current_timestamp from (values(0))").b.getTime(), 1001);
        assertEquals("test", queryScalar(ComplexObject.class, "select 'test', 42 from (values(0))").a);
    }
}
