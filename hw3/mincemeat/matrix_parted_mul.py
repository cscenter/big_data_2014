import os
import sys
import codecs

import mincemeat


__author__ = 'evans'


def mapfn(k, v):
    r = 2

    f = codecs.open(os.path.join(k, v), "r", "utf-8")
    meta, sep, values_str = f.read().partition(' ')
    matrix_type, sep, row = meta.partition('-')
    row = int(row) - 1
    values = map(lambda val: int(val), values_str.split(' '))

    n = len(values)

    for column, value in enumerate(values):
        for k in range(0, n // r):
            map_value = (matrix_type, row, column, value)
            if matrix_type == "l":
                yield (str((row // r, column // r, k)), map_value)
            else:
                yield (str((k,  row // r, column // r)), map_value)


def reducefn(k, vs):
    result = {}
    for (lmatrix_type, lrow, lcolumn, lvalue) in vs:
        if lmatrix_type == 'l':
            for (rmatrix_type, rrow, rcolumn, rvalue) in vs:
                if rmatrix_type == 'r':
                    if lcolumn == rrow:
                        key = (lrow, rcolumn)
                        if key in result:
                            result[key] = result[key] + lvalue * rvalue
                        else:
                            result[key] = lvalue * rvalue
    # print k
    # print vs
    # print result
    return result


def mapfn2(k, v):
    # print k
    # print v
    for key in v.keys():
        yield (str(key), v[key])

def reducefn2(k, vs):
    return sum(vs)


s = mincemeat.Server()
s.map_input = mincemeat.FileNameMapInput(sys.argv[1])
s.mapfn = mapfn
s.reducefn = reducefn
results = s.run_server(password="changeme")

s = mincemeat.Server()
s.map_input = mincemeat.DictMapInput(results)
s.mapfn = mapfn2
s.reducefn = reducefn2
results = s.run_server(password="changeme")

mincemeat.dump_results(results)
