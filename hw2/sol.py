#!/usr/bin/python2
import sqlite3, sys, math

if len(sys.argv) != 3:
  print("Usage: %s <path to db file> <search request>" % (sys.argv[0]))
  sys.exit(1)

conn = sqlite3.connect(sys.argv[1])

c = conn.cursor()

words = map(lambda x: x.lower(), sys.argv[2].split())
 
words=c.execute("SELECT rowid, word, idf FROM word_list WHERE word IN (%s)" % (",".join("?" * len(words))), tuple(words)).fetchall()
print "Words found: ", words

ids=map(lambda x: x[0], words)

result = c.execute("\
  SELECT url_id, url, SUM(sub.weight) AS score, COUNT(*) AS words_count \
  FROM (\
    SELECT url_id, word_id, COUNT(*) * word_list.idf AS weight FROM word_location\
    LEFT JOIN word_list ON word_list.rowid = word_location.word_id\
    WHERE word_id IN (%s)\
    GROUP BY url_id, word_id\
    ORDER BY weight DESC\
  ) AS sub\
  LEFT JOIN url_list ON url_list.rowid = sub.url_id\
  GROUP BY url_id\
  ORDER BY words_count DESC, score DESC\
  LIMIT 20\
  " % (",".join("?" * len(ids))), ids).fetchall()

print("%4s %6s %5s %s" % ("ID", "Weight", "Words", "URL"))
for id, url, score, cnt in result:
  print("%4d %6.2f %5d %s" % (id, score, cnt, url))
