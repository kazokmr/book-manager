INSERT INTO book(id, title, author, release_date)
VALUES ('1', 'Kotlin入門', 'コトリン', to_date('2020-06-29', 'YYYY-MM-DD')),
       ('2', 'Spring入門', 'スプリング', to_date('2020-01-01', 'YYYY-MM-DD')),
       ('3', 'Java入門', 'ジャヴァ', to_date('2000-01-01', 'YYYY-MM-DD')),
       ('4', 'Scala入門', 'スカラ', to_date('2001-01-01', 'YYYY-MM-DD'));
INSERT INTO rental(book_id, account_id, rental_datetime, return_deadline)
VALUES (2, 528, to_timestamp('2021-06-28 16:28:00.000', 'YYYY-MM-DD HH24:MI:SS.MS'),
        to_timestamp('2021-07-12 00:00:00.000', 'YYYY-MM-DD HH24:MI:SS.MS'));


