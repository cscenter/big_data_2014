#!/usr/bin/env python
import argparse
import collections
import functools
import itertools
import os
import sys
import operator

# util

def curry(f):
    return lambda first: lambda second: f(first, second)

def unzip(l):
    return ([f for f, s in l], [s for f, s in l])

# shingling

def __dense_shingle(length, words):
    """
    Hashable a => Int -> [a] -> [[(a, Int)]]
    """
    n = len(words)
    assert(length < n)
    words_hashes = [hash(word) for word in words]
    words_with_hashes = zip(words, words_hashes)
    for idx in xrange(0, n - length + 1):
        subrange = tuple(words_with_hashes[idx:idx+length])
        yield (idx, subrange)


def __common_shingler(length, valid_shingle):
    """
    Eq a, Hashable a => Int -> ([(a, Int)] -> Bool) -> ([a] -> Set [a])
    """
    def impl(words):
        n = len(words)
        result = __dense_shingle(length, words)
        return set(tuple(unzip(shingle_with_hash)[0])
                   for idx, shingle_with_hash in result
                   if valid_shingle(idx, n, shingle_with_hash))
    return impl

def dense_shingler(length):
    """Returns shingler that returns every nword-gram from list of words.

    >>> from jacsim import dense_shingler

    >>> sorted(dense_shingler(3)(range(7)))
    [(0, 1, 2), (1, 2, 3), (2, 3, 4), (3, 4, 5), (4, 5, 6)]

    >>> sorted(dense_shingler(3)([1, 123, 256, 45, 64, 18, 23, 2, 20, 128]))
    [(1, 123, 256), (2, 20, 128), (18, 23, 2), (23, 2, 20), (45, 64, 18), (64, 18, 23), (123, 256, 45), (256, 45, 64)]

    """

    return __common_shingler(length, lambda i,n,s: True)

def sparse_shingler(length):
    """Returns shingler that returns nword-gram such that:

    * it is first or last n-gram in list
    or
    * first or last element of n-gram is element with min or max hash value

    >>> from jacsim import sparse_shingler

    >>> sorted(sparse_shingler(4)([1, 123, 256, 45, 64, 18, 23, 2, 20, 128]))
    [(1, 123, 256, 45), (23, 2, 20, 128), (64, 18, 23, 2), (256, 45, 64, 18)]

    >>> sorted(sparse_shingler(3)(range(7)))
    [(0, 1, 2), (1, 2, 3), (2, 3, 4), (3, 4, 5), (4, 5, 6)]

    """
    x_element = lambda x, iterable: x(enumerate(iterable), key=operator.itemgetter(1))[0]
    min_element = lambda iterable: x_element(min, iterable)
    max_element = lambda iterable: x_element(max, iterable)

    def valid_shingle(idx, document_length, shingle):
        if idx == 0 or idx + len(shingle) == document_length:
            return True
        hashes = unzip(shingle)[1]
        first_or_last = lambda x: x == 0 or x + 1 == len(shingle)
        return any(first_or_last(func(hashes))
            for func in [max_element, min_element])
    return __common_shingler(length, valid_shingle)

def shingler(sparse, length):
    if sparse:
        return sparse_shingler(length)
    else:
        return dense_shingler(length)

# jaccard index

def similarity(left_words, right_words):
    """ Returns jaccard index of two sets of elements

    >>> from jacsim import similarity

    >>> similarity(set([1, 4]), set([2, 3]))
    0.0
    >>> similarity(set([1, 2]), set([2]))
    0.5
    >>> similarity(set(['abc', 'cde', 'eui', 'uid', ]), set(['aoe', 'cde', 'eui', 'uid', 'idh']))
    0.5
    >>> similarity(set([1, 3, 4]), set([1, 2, 3, 4]))
    0.75

    """
    return len(left_words.intersection(right_words)) / float(len(left_words.union(right_words)))

## IO

def read_words(document):
    """
    FileName -> IO [String]
    """
    with open(document, 'r') as f:
        line_words = (line.strip().split() for line in f)
        return list(itertools.chain.from_iterable(line_words))

def test_read_words():
    check_data = [ ('abc cde efg aoe', ['abc', 'cde', 'efg', 'aoe']),
                   ('lorem ipsum dolor sit amet', ['lorem', 'ipsum', 'dolor', 'sit', 'amet']),
                   ('lorem\nipsum\ndolor\nsit\namet', ['lorem', 'ipsum', 'dolor', 'sit', 'amet']) ]
    import tempfile
    def check(data, answer):
        with tempfile.NamedTemporaryFile(delete=False) as f:
            f.write(data)
            filename = f.name
        try:
            words = read_words(filename)
            assert words == answer
        finally:
            os.remove(filename)
    for data, answer in check_data:
        check(data, answer)

def write_similarity(sim, output_dir, left, right):
    left, right = map(os.path.basename, [left, right])
    pat = "sim_{}_{}"
    files = [os.path.join(output_dir, pat.format(l, r))
             for l, r in [(left, right), (right, left)]]
    for filename in files:
        with open(filename, 'w') as f:
            f.write(str(sim))

def process_corpus(corpus_dir, shingler):
    """
    FileName -> ([String] -> Set [String]) -> IO ()
    """
    output_dir = os.path.join(corpus_dir, 'jacsim_output')
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    files = [filename for filename in map(curry(os.path.join)(corpus_dir), os.listdir(corpus_dir))
             if os.path.isfile(filename)]
    for idx, left in enumerate(files):
        left_words = read_words(left)
        left_elements = shingler(left_words)
        for right in files[idx+1:]:
            right_elements = shingler(read_words(right))
            sim = similarity(left_elements, right_elements)
            write_similarity(sim, output_dir, left, right)

def main():
    parser = argparse.ArgumentParser(description='Computes jaccard index')
    parser.add_argument('-n', type=int)
    parser.add_argument('-s', dest='sparse', action='store_const', const=True, default=False, help='sparse n-grams shingling (default: dense)')
    parser.add_argument('directory', type=str, help='directory with corpus')
    arguments = parser.parse_args()
    print arguments
    process_corpus(arguments.directory, shingler(arguments.sparse, arguments.n))

if __name__ == '__main__':
    main()
