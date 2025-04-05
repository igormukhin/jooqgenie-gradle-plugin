CREATE TABLE authors
(
    author_id SERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL
);

CREATE TABLE books
(
    book_id        SERIAL PRIMARY KEY,
    title          VARCHAR(200) NOT NULL,
    author_id      INT REFERENCES authors (author_id),
    published_date DATE,
    isbn           VARCHAR(13) UNIQUE
);

CREATE TABLE borrowers
(
    borrower_id SERIAL PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE loans
(
    loan_id     SERIAL PRIMARY KEY,
    book_id     INT REFERENCES books (book_id),
    borrower_id INT REFERENCES borrowers (borrower_id),
    loan_date   DATE NOT NULL,
    return_date DATE
);