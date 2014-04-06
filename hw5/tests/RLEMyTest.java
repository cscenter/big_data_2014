/**
 *
 * Elizaveta Shashkova, CSC, 2014
 *
 */

import java.util.Iterator;
import static org.junit.Assert.assertEquals;


public class RLEMyTest {
    private RLEMy<String> db;

    @org.junit.Before
    public void setUp() throws Exception {
        db = new RLEMy<String>();
    }

    @org.junit.Test
    public void testAppend() throws Exception {
        for (int i = 0; i < 100; ++i) {
            if (i % 4 == 0) {
                db.append("a");
            } else {
                db.append("b");
            }
        }
        for (int i = 0; i < 100; ++i) {
            if (i % 4 == 0) {
                assertEquals("a", db.get(i));
            } else {
                assertEquals("b", db.get(i));
            }
        }

    }

    @org.junit.Test
    public void testInsert() throws Exception {
        for (int i = 0; i < 30; ++i) {
            db.append("c");
        }

        for (int i = 0; i < 10; ++i) {
            db.insert(i, "a");
        }
        for (int i = 10; i < 20; ++i) {
            db.insert(i, "b");
        }
        for (int i = 20; i < 30; ++i) {
            db.insert(i, "a");
        }
        for (int i = 0; i < 30; ++i) {
            if (i < 10) {
                assertEquals("a", db.get(i));
            } else if (10 <= i && i < 20 ){
                assertEquals("b", db.get(i));
            }  else if (20 <= i){
                assertEquals("a", db.get(i));
            }
        }
    }

    @org.junit.Test
    public void testIterator() throws Exception {
        for (int i = 0; i < 30; ++i) {
            db.append("c");
        }
        for (int i = 0; i < 10; ++i) {
            db.insert(i, "c");
        }
        for (int i = 10; i < 20; ++i) {
            db.insert(i, "b");
        }
        for (int i = 20; i < 30; ++i) {
            db.insert(i, "a");
        }
        Iterator<String> it = db.iterator();
        int i = 0;
        while (it.hasNext()) {
            assertEquals(it.next(), db.get(i));
            ++i;
        }
    }
}
