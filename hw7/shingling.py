#!/usr/bin/env python

import sys
import re

n = 2

def produce(text):
    ans = []
    splitter = re.compile('\\W*')
    words = [s.lower() for s in splitter.split(text) if s != '']

    for i in range(0, len(words) - n + 1):
        s = ''
        for k in range(i, i + n):
            s += str(hash(words[k]))
            s += ' '
        ans.append(s)
    #for word in words:
    #    ans.append(word)
    return ans

