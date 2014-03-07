from collections import defaultdict
import sqlite3
from sys import argv
import math

__author__ = 'lia'


class Searcher:
    def __init__(self, db):
        self.db = sqlite3.connect(db)
        self.query = {}
        self.doc_norms = {}
        self.doc_length = {}

    def idf(self, word):
        value = self.db.execute("SELECT idf FROM word_list WHERE word = '%s'" % word).fetchone()
        return 0 if value is None else value[0]

    def get_q(self):
        n = len(argv)
        for i in xrange(2, n):
            self.query[argv[i]] = self.idf(argv[i])

    @staticmethod
    def get_norm(vector):
        res = 0
        for k in vector.keys():
            res += vector[k] * vector[k]
        return math.pow(res, 0.5)


    def get_tf(self, w, t):
        w__fetchone = self.db.execute("SELECT rowid FROM word_list WHERE word = '%s'" % w).fetchone()
        if None == w__fetchone:
            return {}
        word_id = w__fetchone[0]
        for row in self.db.execute('SELECT * FROM word_location where word_id = %d' % word_id):
            url_id = row[0]
            t[url_id] += 1
        for doc in t.keys():
            if doc in self.doc_norms:
                t[doc] = float(t[doc]) / self.doc_length[doc]
            else:
                local_tf = defaultdict(float)
                length = self.db.execute("SELECT COUNT(word_id) FROM word_location where url_id = %d" % doc).fetchone()[
                    0]
                self.doc_length[doc] = length
                t[doc] = float(t[doc]) / self.doc_length[doc]
                for row in self.db.execute("SELECT word_id FROM word_location where url_id = %d" % doc):
                    word_id = row[0]
                    local_tf[word_id] += 1
                temp_sum = 0
                for k in local_tf.keys():
                    local_tf[k] = float(local_tf[k]) / length
                    temp_sum += local_tf[k] * local_tf[k]
                self.doc_norms[doc] = math.pow(temp_sum, 0.5)
        return t

    def search(self):
        self.get_q()
        tf = {}
        for word in self.query.keys():
            tmp = defaultdict(float)
            tf[word] = (self.get_tf(word, tmp))
        rank = {}
        for doc in self.doc_norms:
            temp_sum = 0
            for word in self.query.keys():
                if doc in tf[word]:
                    temp_sum += tf[word][doc] * self.query[word]
            temp_sum = float(temp_sum) / (self.get_norm(self.query) * self.doc_norms[doc])
            rank[doc] = temp_sum
        result = sorted(rank.iteritems(), key=lambda (k, v): v, reverse=True)[:6]
        ids = zip(*result)[0]
        for my_id in ids:
            print my_id


if '__main__' == __name__:
    s = Searcher(argv[1])
    s.search()
