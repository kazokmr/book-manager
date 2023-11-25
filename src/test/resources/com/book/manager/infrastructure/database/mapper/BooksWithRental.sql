INSERT INTO book (id, title, author, release_date)
VALUES (100, 'Kotlin入門', 'ことりん太郎', to_date('1950-10-01', 'YYYY-MM-DD')),
       (200, 'Java入門', 'じゃば太郎', to_date('2005-08-29', 'YYYY-MM-DD')),
       (300, 'Spring入門', 'すぷりんぐ太郎', to_date('2001-03-21', 'YYYY-MM-DD')),
       (400, 'Kotlin実践', 'ことりん太郎', to_date('2020-01-25', 'YYYY-MM-DD'));
INSERT INTO rental(book_id, account_id, rental_datetime, return_deadline)
VALUES (200, 2, to_timestamp('2021-01-24 21:01:41.000', 'YYYY-MM-DD HH24:MI:SS.MS'),
        to_timestamp('2021-02-07 21:01:41.000', 'YYYY-MM-DD HH24:MI:SS.MS')),
       (300, 10, to_timestamp('2021-01-24 21:01:41.000', 'YYYY-MM-DD HH24:MI:SS.MS'),
        to_timestamp('2021-02-07 21:01:41.000', 'YYYY-MM-DD HH24:MI:SS.MS'));

