/**
 *
 * Elizaveta Shashkova, CSC, 2014
 *
 */

import java.util.Iterator;

public class Main {


    public static void main(String[] args) {
        RLEMy<String> l = new RLEMy<>();
        l.append("a");
        l.append("b");
        l.append("b");
        l.append("b");
        l.insert(2, "a");
        l.insert(1, "a");
        l.insert(1, "a");
        l.insert(3, "a");

        int k = l.getFull_length();
        l.show();
        for (int i = 0; i < k; ++i) {
            System.out.print(l.get(i) + " ");
        }
        System.out.println();
        Iterator<String> it1 = l.iterator();
        while(it1.hasNext()) {
            System.out.print(it1.next() + " ");
        }
    }
}
