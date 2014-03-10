#!/usr/bin/env python
import mincemeat
import sys
import matrix


def mapfn(k, v):
    elements = matrix.do_map(v)
    for key, value in elements.items():
        yield str(key), value

def reducefn(k, vs):
    val_mul = 1
    x = 0
    y = 0
    list = []
    res = []
    for v in vs:
        res += v
    vs = res

    for v1 in vs:
        for v2 in vs:
            if v1[0] != v2[0]:
                val_mul = v1[1] * v2[1]
                if v1[0] == 1:
                    x = v1[2]
                    y = v2[2]
                else:
                    x = v2[2]
                    y = v1[2]

                t = tuple([val_mul, x, y])
                if t not in list:
                    list.append(t)
    return list

def mapfn2(k, vs):
    for v in vs:
        key = tuple([v[1], v[2]])
        val = v[0]
        yield str(key), val

def reducefn2(k, vs):
    result = sum(vs)
    return result

def mapfn3(k, v):
    #terrible code:(
    key = int(k.split(',')[0][1:])
    cut = k.split(',')[1]
    key_new = int(cut[:len(cut)-1])
    t = tuple([key_new, v])
    yield str(key), t

def reducefn3(k, vs):
    dict = {val[0]: val[1] for val in vs}
    line_ans = []
    l = len(dict)
    for i in range(1, l + 1):
        line_ans += str(dict[i])
    return line_ans

s = mincemeat.Server()

s.map_input = mincemeat.FileMapInput(sys.argv[1])
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

s.map_input = mincemeat.DictMapInput(results)
s.mapfn = mapfn3
s.reducefn = reducefn3
results = s.run_server(password="changeme")

print "Answer:"
for line in results:
    print "line %s: %s" % (line, results[line])