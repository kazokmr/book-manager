#!/bin/bash
set -e

# 文字列の`$` が コマンド引数や変数と認識されてしまうので、全ての`$`をエスケープ `\` した
password1="'\$2a\$10\$wPy2Tkuv2LK/lq0a5u10..dMhQutt/nVKGGtJzgYJT7ueV2w7efeG'"
password2="'\$2a\$10$/QR/7iYQVapU2RgMPYJ3luiEZ8vzrTZJgb3SImXLtrUKQ3axhhrDS'"

psql \
    -U 'book_manager' \
    -d 'book_manager' \
    -v ON_ERROR_STOP=1 \
    -v "password1=${password1}" \
    -v "password2=${password2}" \
<<-EOSQL
  INSERT INTO book VALUES(100, 'Kotlin入門', 'コトリン太郎', '1950-10-01'), (200, 'Java入門', 'ジャヴァ太郎', '2005-08-29');
  INSERT INTO account values (1,'admin@example.com',:password1,'管理者','ADMIN'),
                           (2,'user@example.com',:password2,'ユーザー','USER');
EOSQL
