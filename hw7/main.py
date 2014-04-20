__author__ = 'lia'
from sys import argv
from os import listdir
from os.path import join
from collections import defaultdict
from random import randint


def build_feature_vector(text, n, sparse, known):
    vector = []
    features = defaultdict(int)
    is_first = True
    f = open(text)
    for word in f.read().split():
        h = hash(word)
        if len(vector) < n:
            vector.append(h)
            continue
        feature = tuple(vector)
        f_min = min(feature)
        f_max = max(feature)
        if not sparse or is_first or f_min == feature[0] \
                or feature[0] == f_max or f_min == feature[-1] \
                or f_max == feature[-1]:
            is_first = False
            if not feature in known:
                known[feature] = len(known)
            features[known[feature]] += 1
        vector = vector[1:]
        vector.append(h)

    feature = tuple(vector)
    if not feature in known:
        known[feature] = len(known)
    features[known[feature]] += 1

    f.close()
    return features


def generate_hash(k):
    a = randint(1, k)
    b = randint(0, k)

    def h(x):
        if x == -1:
            return k + 2
        return (a * x + b) % k

    return h


def get_min_hash(texts):
    k = len(texts[0])
    n = min(k / 2, 400)
    min_hash = []
    for i in xrange(len(texts)):
        min_hash.append([-1] * n)

    hashes = [generate_hash(k) for i in xrange(n)]
    for i, text in enumerate(texts):
        for j, v in text.iteritems():
            if v != 0: # defaultdict works properly but i'm paranoid
                for k, h in enumerate(hashes):
                    val = h(j)
                    if val < h(min_hash[i][k]):
                        min_hash[i][k] = j
    return min_hash


def jaccard(a, b):
    k = len(a)
    both = 0
    for i, v in enumerate(a):
        if v == b[i]:
            both += 1
    return float(both) / k


def main(corpus, sparse, n):
    if not corpus:
        print "Corpus is not specified!"
        return
    known = {}
    texts = []
    filenames = []
    for text in listdir(corpus):
        filenames.append(text)
        texts.append(build_feature_vector(join(corpus, text), n, sparse, known))
    min_hash = get_min_hash(texts)

    k = len(min_hash)
    for i in xrange(k):
        for j in xrange(i):
            print 'j(%s, %s) = %f' % \
                  (filenames[i], filenames[j], jaccard(min_hash[i], min_hash[j]))


def parse_args():
    sparse = False
    corpus = None
    n = 6 # default n
    is_n = False
    for arg in argv[1:]:
        if is_n:
            n = int(arg)
            is_n = False
        elif arg == '-s':
            sparse = True
        elif arg == '-n':
            is_n = True
        else:
            corpus = arg
    return corpus, sparse, n


if __name__ == '__main__':
    main(*parse_args())
