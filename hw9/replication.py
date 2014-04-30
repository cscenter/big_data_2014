import httplib
import urllib2
import argparse
import cgi
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer

port = 0
term = 0
log = ''
followers = []
port_leader = 0
is_leader = False
leader_host = 'localhost'
follower_host = 'localhost'
followers_indexes = {} #save info about last sent record to follower

class Handler_leader(BaseHTTPRequestHandler):

    def is_finished(self):
        finished = True
        for fol in followers_indexes:
            if followers_indexes[fol] != -1:
                finished = False
                break
        return finished

    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        global port
        global term
        global log
        global followers
        records = log.split(',')
        if (self.path == "/send_log"):
            for follower in followers:
                i = followers_indexes[follower]
                if i > 0:
                    urllib2.urlopen("http://%s:%s/send_log/%s" %
                            (follower_host, follower, records[i] + '+' + records[i - 1] + '+' + str(term)))
                else:
                    urllib2.urlopen("http://%s:%s/full_log/%s" % (follower_host, follower, log + '+' + str(term)))

        if (self.path.startswith("/ok")):
            ok_follower = self.path[len("/ok") + 1:]
            followers_indexes[ok_follower] = -1
            if self.is_finished():
                self.wfile.write("Replication finished!\n")
                self.wfile.write("Every follower shows its log!\n")
                for follower in followers:
                    urllib2.urlopen("http://%s:%s/show_log" % (follower_host, follower))

        if (self.path.startswith("/prev")):
            follower = self.path[len("/prev") + 1:]
            i = followers_indexes[follower]
            i -= 1
            followers_indexes[follower] = i
            if i > 0:
                urllib2.urlopen("http://%s:%s/send_log/%s" %
                    (follower_host, follower, records[i] + '+' + records[i - 1] + '+' + str(term)))
            else:
                urllib2.urlopen("http://%s:%s/full_log/%s" % (follower_host, follower, log + '+' + str(term)))

        if (self.path.startswith("/suff")):
            info = self.path[len("/suff") + 1:]
            info = info.split('+')
            follower = info[0]
            rec = info[1]
            log_records = log.split(',')
            ind = log_records.index(rec)
            log_suffix = ''
            for rec_s in range(ind + 1, len(log_records)):
                log_suffix += ','
                log_suffix += log_records[rec_s]

            urllib2.urlopen("http://%s:%s/suff/%s" % (follower_host, follower, log_suffix + '+' + str(term)))


class Handler_follower(BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        global port
        global term
        global log
        global port_leader

        if (self.path.startswith("/send_log")):
            info = self.path.split('+')
            leader_rec = info[0][len("/send_log") + 1:]
            leader_prev = info[1]
            leader_term = info[2]
            if leader_term == term:
                log += ','
                log += leader_rec
                print leader_host
                print port_leader
                print port
                urllib2.urlopen("http://%s:%s/ok/%s" % (leader_host, port_leader, str(port)))
            else:
                follower_records = log.split(',')
                if leader_prev in follower_records:
                    urllib2.urlopen("http://%s:%s/suff/%s" % (leader_host, port_leader, str(port) + '+' + leader_prev))
                else:
                    urllib2.urlopen("http://%s:%s/prev/%s" % (leader_host, port_leader, str(port)))

        if (self.path.startswith("/full_log")):
            info = self.path.split('+')
            log = info[0][len("/full_log") + 1:]
            term = info[1]
            urllib2.urlopen("http://%s:%s/ok/%s" % (leader_host, port_leader, str(port)))

        if (self.path.startswith("/show_log")):
            self.wfile.write("My log: %s\n" % log)
            print "My log: %s\n" % log

        if (self.path.startswith("/suff")):
            info = self.path[len("/suff") + 1:]
            info = info.split('+')
            suff = info[0]
            term = int(info[1])
            log += suff
            urllib2.urlopen("http://%s:%s/ok/%s" % (leader_host, port_leader, str(port)))


def start_server():
    global is_leader
    global port
    try:
        if is_leader:
            server = HTTPServer(('', port), Handler_leader)
            print 'Started Leader on port ', port
        else:
            server = HTTPServer(('', port), Handler_follower)
            print 'Started Follower on port ', port
        server.serve_forever()
    except KeyboardInterrupt:
        print '^C received, shutting down the web server'
        server.socket.close()


def read_args():
    global port
    global term
    global log
    global followers
    global is_leader
    global port_leader

    parser = argparse.ArgumentParser()
    parser.add_argument('-p', nargs=1, type=int)
    parser.add_argument('-lp', nargs=1, type=int)
    parser.add_argument('-f', nargs=1, type=str)
    parser.add_argument('-l', nargs=1, type=str)
    parser.add_argument('-t', nargs=1, type=int)
    args = parser.parse_args()
    port = args.p[0]
    if args.f is not None:
        followers = args.f[0].split(',')
        is_leader = True
    else:
        port_leader = args.lp[0]

    term = args.t[0]
    log = args.l[0]

def init():
    n = len(log.split(','))
    if is_leader:
        for follower in followers:
            followers_indexes[follower] = n - 1

if __name__ == '__main__':
    read_args()
    init()
    start_server()