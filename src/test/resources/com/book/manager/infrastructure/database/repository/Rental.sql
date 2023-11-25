INSERT INTO rental(book_id, account_id, rental_datetime, return_deadline)
VALUES (1, 528, to_timestamp('2021-06-01 12:00:00.000', 'YYYY-MM-DD HH24:MI:SS.MS'),
        to_timestamp('2021-06-14 12:00:00.000', 'YYYY-MM-DD HH24:MI:SS.MS')),
       (999, 74, to_timestamp('2021-06-29 12:00:00.000', 'YYYY-MM-DD HH24:MI:SS.MS'),
        to_timestamp('2021-07-12 12:00:00.000', 'YYYY-MM-DD HH24:MI:SS.MS'));
