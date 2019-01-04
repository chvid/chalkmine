package dk.brightworks.chalkmine;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static dk.brightworks.chalkmine.ChalkMine.*;
import static org.junit.Assert.assertEquals;

/**
 * Notice that this test requires that there is a MySQL server running at localhost with a database test and is
 * accessible for user root with no password.
 *
 * (See configuration file in src/test/resources/system.properties)
 *
 * The test will create a table with some data in it and run some time consuming queries against it in simultaneous
 * 200 threads.
 *
 * This causes the pool to run out of connections and have users acquiring a connection wait until one becomes
 * available.
 */
@Ignore
public class MysqlConnectionPoolTest {
    @Before
    public void setup() throws IOException {
        System.getProperties().load(MysqlConnectionPoolTest.class.getResourceAsStream("/system.properties"));

        openConnection();
        try {
            update("create table test ( a integer, b integer )");

            for (int i = 0; i < 10000; i++) {
                update("insert into test (a, b) values(?, ?)", (int)(1000 * Math.random()), (int)(1000 * Math.random()));
            }

        } finally {
            closeConnection();
        }
    }

    private int count(int b) {
        openConnection();
        try {
            return queryScalar(Integer.class, "select sum(a) from test where b = ?", b);
        } finally {
            closeConnection();
        }
    }

    private long counter;

    @Test
    public void countStuff() throws InterruptedException {
        counter = 0;

        Thread[] threads = new Thread[200];

        for (int i = 0; i < 200; i++) {
            final int j = i;
            threads[i] = new Thread() {
                public void run() {
                    counter += count(j);
                }
            };
        }

        for (Thread m : threads) m.start();
        for (Thread m : threads) m.join();

        assertEquals(true, counter > 0);
    }

    @After
    public void teardown() {
        openConnection();
        try {
            update("drop table test");
        } finally {
            closeConnection();
        }
    }
}
