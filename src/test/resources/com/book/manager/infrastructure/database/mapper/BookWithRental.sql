INSERT INTO book (id, title, author, release_date)
VALUES (100, 'Kotlin入門', 'ことりん太郎', to_date('19501001', 'YYYYMMDD'));
INSERT INTO rental(book_id, account_id, rental_datetime, return_deadline)
VALUES (100, 3, to_timestamp('2021-07-01 05:39:48.000', 'YYYY-MM-DD HH24:MI:SS.MS'),
        to_timestamp('2021-07-15 05:39:48:000', 'YYYY-MM-DD HH24:MI:SS.MS'));

