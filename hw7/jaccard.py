#!/usr/bin/env python

import mincemeat
import sys
import shingling
from os import listdir

shingling.n = 2

def mapfn(file_name, text):
    words = shingling.produce(text)
    for word in words:
        yield word, file_name

def reducefn(k, vs):
    list = []
    for v in vs:
        list.append(v)
    return list

def mapfn2(word, list):
    for file1 in list:
        for file2 in list:
            if (file1 <= file2):
                t = tuple([file1, file2])
                yield file1, file2
                yield file2, file1

def reducefn2(file, vs):
    list = {}
    for v in vs:
        if v in list.keys():
            list[v] += 1
        else:
            list[v] = 1
    return list

s = mincemeat.Server()
s.map_input = mincemeat.FileMapInput('data')
s.mapfn = mapfn
s.reducefn = reducefn
results = s.run_server(password="changeme")
print "Results after MapReduce 1:"
print results

s.map_input = mincemeat.DictMapInput(results)
s.mapfn = mapfn2
s.reducefn = reducefn2
results = s.run_server(password="changeme")
print "Results after MapReduce 2:"
print results

sizes = {}
for file in results.keys():
    sizes[file] = results[file][file]

for file in results.keys():
    other_files = results[file]
    for file2 in listdir('data'):
        file2 = 'data/' + file2
        if file <= file2:
            print "Files (%s, %s)" % (file, file2)
            if file2 in other_files.keys():
                print "%d %d %d" % (other_files[file2], sizes[file], sizes[file2])
                # intersection, size, size
                print "Jaccard index: %f " % (other_files[file2] / float(sizes[file] + sizes[file2] - other_files[file2]))
            else:
                print "0 %d %d" % (sizes[file], sizes[file2])
                print "Jaccard index: 0 "

