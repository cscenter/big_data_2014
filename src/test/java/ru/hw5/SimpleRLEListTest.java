package ru.hw5;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author evans
 *         06.04.14.
 */
public class SimpleRLEListTest {

    @Test
    public void test1() {
        SimpleRLEList<Character> rleList = new SimpleRLEList<Character>() {
            {
                append('C');
                append('C');
                append('W');
                append('W');
                append('C');
            }
        };
        assertRLEEquals("2C2W1C", rleList);
    }

    @Test
    public void test2() {
        SimpleRLEList<Character> rleList = new SimpleRLEList<Character>() {
            {
                append('C');
                append('C');
                append(null);
                append(null);
                append('C');
                append(null);
            }
        };
        assertRLEEquals("2C2null1C1null", rleList);
    }

    @Test
    public void test4() {
        SimpleRLEList<Character> rleList = new SimpleRLEList<Character>() {
            {
                insert(0, 'C');
                insert(1, 'C');
            }
        };
        assertRLEEquals("2C", rleList);
    }

    @Test
    public void test5() {
        SimpleRLEList<Character> rleList = new SimpleRLEList<Character>() {
            {
                append('C');
                append('C');
                append('W');
                append('W');
                append('C');
                Assert.assertEquals((Character) 'C', get(0));
                Assert.assertEquals((Character) 'C', get(1));
                Assert.assertEquals((Character) 'W', get(2));
                Assert.assertEquals((Character) 'W', get(3));
                Assert.assertEquals((Character) 'C', get(4));
            }
        };
        assertRLEEquals("2C2W1C", rleList);
    }


    @Test
    public void test6() {
        SimpleRLEList<Character> rleList = new SimpleRLEList<Character>() {
            {
                append('C');
                append('C');
                append('W');
                insert(2, 'C');
                insert(3, 'W');
            }
        };
        assertRLEEquals("3C2W", rleList);

        StringBuilder sb = new StringBuilder();
        for(Character c : rleList){
            sb.append(c);

        }

        Assert.assertEquals("CCCWW", sb.toString());

        Iterator<Character> iterator = rleList.iterator();
        while (iterator.hasNext()){
            iterator.next();
            iterator.remove();
        }

        assertRLEEquals("", rleList);
    }

    public void assertRLEEquals(String s, SimpleRLEList<Character> list) {
        Assert.assertEquals(s, toString(list));
    }

    public String toString(SimpleRLEList<Character> list) {
        final StringBuilder sb = new StringBuilder();
        for (Pair<Integer, Character> pair : list.getList()) {
            sb.append(pair.fst).append(pair.snd);
        }
        return sb.toString();
    }
}
