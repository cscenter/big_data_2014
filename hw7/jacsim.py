import argparse
import collections
import functools
import itertools
import os
import sys


class Shingler(object):
    def shingle(words):
        pass

def __dense_shingle(length, words):
    n = len(words)
    assert(length < n)
    result = []
    for idx in xrange(0, n - length + 1):
        subrange = tuple(words[idx:idx+length])
        result.append((idx, subrange))
    return result

def __shingler(length, valid_shingle):
    def impl(words):
        n = len(words)
        result = __dense_shingle(length, words)
        return set(shingle for idx, shingle in result if valid_shingle(idx, n, shingle))

def dense_shingler(length):
    return __shingler(length, lambda i,n,s: True)

def sparse_shingler(length):
    def valid_shingle(idx, document_length, shingle):
        shingle_length = len(shingle)
        if idx == 0:
            return True
    def impl(words):
        result = __dense_shingler(length, words)
        return set(shingle for idx, shingle in result if valid_shingle(idx, len(words), shingle))
    return impl

class DenseShingler(Shingler):
    def __init__(self, length):
        self.__length = length

    def shingle(words):
        pass

class SparseShingler(Shingler):
    def __init__(self, length):
        self.__length = length

    def shingle(words):
        pass


def shingler(sparse, length):
    if sparse:
        return DenseShingler(length)
    else: # dense
        return SparseShingler(length)

def similarity(left_words, right_words):
    return len(left_words & right_words) / float(len(left_words | right_words))

def read_words(document):
    with open(document, 'r') as f:
        line_words = (line.strip().split() for line in f)
        return list(itertools.chain.from_iterable(line_words))

def write_similarity(sim, left, right):
    pat = "sim_{}_{}"
    files = [pat.format(l, r) for l, r in [(left, right), (right, left)]]
    for filename in files:
        with open(f, 'w') as f:
            f.write(str(sim))

def process_corpus(corpus_dir, shingler):
    files = [f for f in os.listdir(corpus_dir) if os.isfile(corpus_dir)]
    for idx, left in enumerate(files):
        left_elements = shingler.shingle(read_words(left))
        for right in files[idx+1:]:
            right_elements = shingler.shingle(read_words(right))
            sim = similarity(left_elements, right_elements)
            write_similarity(sim, left, right)

def main(args):
    parser = argparse.ArgumentParser(description='Computes jaccard index')
    parser.add_argument('-n', type=int)
    parser.add_argument('-s', dest=sparse, action='store_const', const=True, default=False, help='sparse n-grams shingling (default: dense)')
    parser.add_argument('directory', help='directory with corpus')
    arguments = parse.parse_args(args)



if __name__ == '__main__':
    main(sys.argv)
