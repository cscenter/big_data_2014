package main

import (
	"database/sql"
	"fmt"
	_ "github.com/mattn/go-sqlite3"
	"log"
	"os"
	"runtime/debug"
)

func check_err(err error) {
	if err != nil {
		debug.PrintStack()
		log.Fatal(err)
	}
}

func file_exists(path string) (bool, error) {
	_, err := os.Stat(path)
	if err == nil { return true, nil }
	if os.IsNotExist(err) { return false, nil }
	return false, err
}

func process_query(db *sql.DB, query []string) {
	create_query_table := func(tx *sql.Tx, query_array []string) {
		_, err := tx.Exec(`create table query (word varchar(128) unique);`)
		check_err(err)
		statement, err := tx.Prepare("insert into query(word) values (?)")
		check_err(err)
		defer statement.Close()
		for _, val := range query_array {
			_, err := statement.Exec(val)
			check_err(err)
		}
	}
	create_query_terms_table := func(tx *sql.Tx) {
		_, err := tx.Exec(`create table query_terms
                as select word_list.rowid as word_id, idf
                from word_list inner join query
                on word_list.word=query.word;`)
		check_err(err)
	}
	rank_documents := func(tx *sql.Tx) {
		rows, err := tx.Query(`select url_id, sum(tf * idf) as rank
                from query_terms inner join document_terms
                on query_terms.word_id = document_terms.word_id
                group by document_terms.url_id order by rank DESC`);
		check_err(err)
		defer rows.Close()
		for rows.Next() {
			var url_id int
			var rank float32
			rows.Scan(&url_id, &rank)
			fmt.Printf("Document %d with rank %f\n", url_id, rank)
		}
	}
	transaction, err := db.Begin()
	check_err(err)
	defer transaction.Rollback()
	create_query_table(transaction, query)
	create_query_terms_table(transaction)
	rank_documents(transaction)
}

func precompute_documents_if_needed(db *sql.DB) {
	is_documents_precomputed := func(transaction *sql.Tx) bool {
		rows, err := transaction.Query("select name from sqlite_master where type='table' and name='document_terms';")
		check_err(err)
		defer rows.Close()
		return rows.Next()
	}
	precompute_documents := func(transaction *sql.Tx) {
		_, err := transaction.Exec(`create table document_lengths
                  as select url_id, count(*) as length
                  from word_location group by url_id;`)
		check_err(err)
		_, err = transaction.Exec(`create table document_terms
                  as select word_location.url_id as url_id, word_id, count(*) * 1.0 / (length * length) as tf
                  from word_location inner join document_lengths
                  on word_location.url_id = document_lengths.url_id
                  group by word_location.url_id, word_id;`)
		check_err(err)
	}
	transaction, err := db.Begin()
	check_err(err)
	precomputed := is_documents_precomputed(transaction)
	if (!precomputed) {
		precompute_documents(transaction)
	}
	err = transaction.Commit()
	check_err(err)
}


func main() {
	filename := "corpus.db"
	found, err := file_exists(filename)
	check_err(err)
	if !found {
		fmt.Printf("Database file %s not found\n", filename)
		os.Exit(1)
	}
	db, err := sql.Open("sqlite3", filename)
	check_err(err)
	defer db.Close()

	precompute_documents_if_needed(db)
	process_query(db, os.Args[1:])
}
