package ru.hw2;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Evgeny Vanslov
 * vans239@gmail.com
 */
public class SqlLiteMain {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        String path = "hw2/corpus.db";
        List<String> query = Arrays.asList("database", "query", "optimization");
        if(args.length > 0){
            path = args[0];
            query = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

        }
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            DbService dbService = new DbService(connection);
            int URL_ID = 10000;
            dbService.addUrl(query);
//            dbService.print(URL_ID);
//            dbService.printWords();
            List<Integer> ans = dbService.findBest(URL_ID);
            System.out.println("\n\nQuery doc:");
            dbService.prettyPrint(ans.get(0));
            System.out.println("First relevant doc:");
            dbService.prettyPrint(ans.get(1));
            System.out.println("Second relevant doc:");
            dbService.prettyPrint(ans.get(2));
            dbService.remove(URL_ID);
        }
    }

    private static class DbService {
        private Connection connection;

        public DbService(Connection connection) {
            this.connection = connection;
        }

        public int addUrl(List<String> words) throws SQLException {
            int url_id = 10000;
            PreparedStatement statement = connection.prepareStatement("INSERT INTO word_location (url_id, word_id, location) SELECT ?, rowid, ? FROM word_list WHERE word=?");
            int location = 1;
            for (final String word : words) {
                statement.setInt(1, url_id);
                statement.setString(3, word);
                statement.setInt(2, location);
                ++location;
                statement.executeUpdate();
            }
            return url_id;
        }

        public void remove(int urlId) throws SQLException {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM word_location where url_id=?");
            statement.setInt(1, urlId);
            statement.executeUpdate();
        }

        public void print(int urlId) throws SQLException {
            PreparedStatement statement = connection.prepareStatement("SELECT url_id, word, location, word_id, idf FROM word_location wlo INNER JOIN word_list wli ON wli.rowid = wlo.word_id WHERE url_id = ?");
            statement.setInt(1, urlId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                System.out.println("url_id = " + rs.getInt("url_id"));
                System.out.println("word = " + rs.getString("word"));
                System.out.println("wordId = " + rs.getString("word_id"));
                System.out.println("idf = " + rs.getString("idf"));
                System.out.println("location = " + rs.getInt("location"));
            }
        }

        public void prettyPrint(int urlId) throws SQLException {
            PreparedStatement statement = connection.prepareStatement("SELECT  word FROM word_location wlo INNER JOIN word_list wli ON wli.rowid = wlo.word_id WHERE url_id = ?");
            statement.setInt(1, urlId);
            ResultSet rs = statement.executeQuery();

            final StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("word")).append(" ");
            }
            System.out.println(sb.toString());
        }

        public List<Integer> findBest(int a) throws SQLException {
            String sql =
                    "SELECT quer.url_id, (sim / sqrt(norm)) as norm_sim FROM " +
                        "(SELECT url_id, SUM(drepeat * qrepeat * idf * idf) as sim FROM " +
                                "(" +
                                "SELECT doc.url_id, doc.word_id, doc.repeat as drepeat, query.repeat as qrepeat FROM " +
                                    "(SELECT url_id, word_id, count(*) as repeat FROM word_location where url_id = ? GROUP BY url_id, word_id) as query " +
                                "INNER JOIN " +
                                    "(SELECT url_id, word_id, count(*) as repeat FROM word_location GROUP BY url_id, word_id) as doc " +
                                "ON doc.word_id = query.word_id " +
                                ") " +
                            "INNER JOIN word_list wli ON word_id = wli.rowid " +
                            "GROUP BY url_id " +
                        ") as ans " +
                    "INNER JOIN " +
                        "(SELECT url_id, SUM(idf) as norm FROM word_location INNER JOIN word_list wli ON wli.rowid = word_id GROUP BY url_id) as quer " +
                    "ON quer.url_id = ans.url_id ORDER BY norm_sim DESC";
            PreparedStatement statement = connection.prepareStatement(
                    sql);
            statement.setInt(1, a);

            ResultSet rs = statement.executeQuery();
            List<Integer> urls = new ArrayList<>();
            while (rs.next()) {
                System.out.println("Url_id " + rs.getInt(1));
                System.out.println("Sim " + rs.getDouble(2));
                urls.add(rs.getInt(1));
//                System.out.println("word_id " + rs.getInt(2));
//                System.out.println("drepeat " + rs.getInt(3));
//                System.out.println("qrepeat " + rs.getInt(4));
//                System.out.println("idf " + rs.getDouble(5));
            }
            return urls;
        }

        public int getWordCount(int urlId) throws SQLException {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT count(*) as count FROM word_location wlo WHERE url_id = ?");
            statement.setInt(1, urlId);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
            throw new IllegalArgumentException();

        }

        public void printWords() throws SQLException {
            ResultSet rs = connection.createStatement().executeQuery("SELECT word, idf FROM word_list");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " " + rs.getDouble(2));
            }

        }
    }
}

//todo check repeat words!
/*
CREATE TABLE word_list(
 rowid INT PRIMARY KEY,
 word VARCHAR(128) UNIQUE,
idf REAL);

CREATE TABLE word_location(
 url_id INT,
 word_id INT FOREIGN KEY REFERENCES word_list,
 location INT);
 */
