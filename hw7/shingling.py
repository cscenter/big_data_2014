#!/usr/bin/env python

import sys
import re

n = 5

def produce(text):
    ans = []
    splitter = re.compile('\\W*')
    words = [s.lower() for s in splitter.split(text) if s != '']
    for i in range(0, len(words) - n + 1):
        s = ''
        for k in range(i, i + n):
            val = abs(hash(words[k])) % (10 ** 8)
            s += str(val)
            s += ' '
        ans.append(s)
    return ans

def produce_short(text):
    ans = []
    splitter = re.compile('\\W*')
    words = [s.lower() for s in splitter.split(text) if s != '']
    for i in range(0, len(words) - n + 1):
        # it's possible to implement it better, but i'm too lazy:)
        s = ''
        val = int(abs(hash(words[i])) % (10 ** 7))
        min = i
        min_val = val
        max = i
        max_val = val
        for k in range(i, i + n):
            val = int(abs(hash(words[k])) % (10 ** 7))
            if (val > max_val):
                max_val = val
                max = k
            if (val < min_val):
                min_val = val
                min = k
            s += str(val)
            s += ' '
        if (min == i) or (min == i + n - 1) or (max == i) or (max == i + n - 1):
            ans.append(s)
    return ans
