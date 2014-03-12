import os
import sys
import codecs

import mincemeat


__author__ = 'evans'


def mapfn(k, v):
    f = codecs.open(os.path.join(k, v), "r", "utf-8")
    meta, sep, values_str = f.read().partition(' ')
    matrix_type, sep, row = meta.partition('-')
    values = values_str.split(' ')
    if matrix_type == "l":
        for column, value in enumerate(values):
            for k in range(1, 3):
                yield (str((int(row), k)), (matrix_type, column + 1, int(value)))
    else:
        for column, value in enumerate(values):
            for k in range(1, 3):
                yield (str((k, (column + 1))), (matrix_type, int(row), int(value)))


def reducefn(k, vs):
    left = {index: value for (matrix_type, index, value) in vs if matrix_type == 'l'}
    right = {index: value for (matrix_type, index, value) in vs if matrix_type != 'l'}
    keys = set(left) & set(right)
    result = sum([left[i] * right[i] for i in keys])
    return result



s = mincemeat.Server()

#s.map_input = mincemeat.DictMapInput(dict(enumerate(data)))
s.map_input = mincemeat.FileNameMapInput(sys.argv[1])
s.mapfn = mapfn
s.reducefn = reducefn
results = s.run_server(password="changeme")

mincemeat.dump_results(results)
