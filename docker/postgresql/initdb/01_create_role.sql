CREATE ROLE book_manager WITH LOGIN PASSWORD 'book_manager';
CREATE DATABASE book_manager OWNER book_manager template =template0 encoding ='utf-8' lc_collate ='C' lc_ctype ='C';
GRANT ALL PRIVILEGES ON DATABASE book_manager TO book_manager;