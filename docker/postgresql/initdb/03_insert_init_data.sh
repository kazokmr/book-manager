#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 -U 'book_manager' -d 'book_manager' <<-EOSQL
  INSERT INTO book VALUES(100, 'Kotlin入門', 'コトリン太郎', '1950-10-01'), (200, 'Java入門', 'ジャヴァ太郎', '2005-08-29');
EOSQL