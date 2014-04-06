from RLEList import RLEListRefImpl

__author__ = 'liana'

import unittest
from random import randint


class RLEListTestCase(unittest.TestCase):
    def test_get(self):
        lst = RLEListRefImpl()
        ref_lst = []
        for i in xrange(10):
            for j in xrange(randint(0, 10)):
                lst.append(i)
                ref_lst.append(i)

        for i, v in enumerate(ref_lst):
            self.assertEqual(lst.get(i), v)

    def test_iterator(self):
        lst = RLEListRefImpl()
        ref_lst = []
        for i in xrange(10):
            for j in xrange(randint(0, 10)):
                lst.append(i)
                ref_lst.append(i)

        for i, v in enumerate(lst):
            self.assertEqual(ref_lst[i], v)

    def test_insert(self):
        lst = RLEListRefImpl()
        for i in xrange(10):
            for j in xrange(randint(0, 10)):
                lst.append(i)
        for i in xrange(100):
            index = randint(-1, len(lst) + 2)
            value = randint(0, 10)
            if index == -1:
                self.assertRaises(IndexError, lst.insert, index, value)
            elif index == len(lst) + 1:
                self.assertRaises(IndexError, lst.insert, index, value)
            else:
                lst.insert(index, value)
                self.assertEqual(lst.get(index), value)


if __name__ == '__main__':
    unittest.main()
