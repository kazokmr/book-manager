#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 -U 'book_manager' -d 'book_manager' <<-EOSQL
  INSERT INTO book VALUES(100, 'Kotlin入門', 'コトリン太郎', '1950-10-01'), (200, 'Java入門', 'ジャヴァ太郎', '2005-08-29');
  INSERT INTO account values (1,'admin@example.com','$2a$10$wPy2Tkuv2LK/lq0a5u10..dMhQutt/nVKGGtJzgYJT7ueV2w7efeG','管理者','ADMIN'),
                           (2,'user@example.com','$2a$10$/QR/7iYQVapU2RgMPYJ3luiEZ8vzrTZJgb3SImXLtrUKQ3axhhrDS','ユーザー','USER');
EOSQL
