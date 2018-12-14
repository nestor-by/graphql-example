CREATE SCHEMA IF NOT EXISTS test;
SET SCHEMA test;

CREATE TABLE language (
  id              NUMBER(7)     NOT NULL PRIMARY KEY,
  cd              CHAR(2)       NOT NULL,
  description     VARCHAR2(50)
);

CREATE TABLE author (
  id              NUMBER(7)     NOT NULL PRIMARY KEY,
  first_name      VARCHAR2(50),
  last_name       VARCHAR2(50)  NOT NULL,
  date_of_birth   DATE,
  year_of_birth   NUMBER(7),
  distinguished   NUMBER(1)
);

CREATE TABLE book (
  id              NUMBER(7)     NOT NULL PRIMARY KEY,
  author_id       NUMBER(7)     NOT NULL,
  title           VARCHAR2(400) NOT NULL,
  published_in    NUMBER(7)     NOT NULL,
  language_id     NUMBER(7)     NOT NULL,

  CONSTRAINT fk_book_author     FOREIGN KEY (author_id)   REFERENCES author(id),
  CONSTRAINT fk_book_language   FOREIGN KEY (language_id) REFERENCES language(id)
);

CREATE TABLE book_store (
  name            VARCHAR2(400) NOT NULL UNIQUE
);

CREATE TABLE book_to_book_store (
  name            VARCHAR2(400) NOT NULL,
  book_id         INTEGER       NOT NULL,
  stock           INTEGER,

  PRIMARY KEY(name, book_id),
  CONSTRAINT fk_b2bs_book_store FOREIGN KEY (name)        REFERENCES book_store (name) ON DELETE CASCADE,
  CONSTRAINT fk_b2bs_book       FOREIGN KEY (book_id)     REFERENCES book (id)         ON DELETE CASCADE
);

insert into language values(1, 'ru', 'russian');
insert into language values(2, 'en', 'english');
insert into language values(3, 'by', 'belarus');
insert into language values(4, 'fr', 'france');

insert into author values(1, 'Jane', 'Austen', '1775-12-16', 1775, 1);
insert into author values(2, 'Alexandre', 'Dumas', '1802-07-24', 1802, 1);
insert into author values(3, 'Joanne', 'Rowling', '1965-07-31', 1965, 0);
insert into author values(4, 'Stephen', 'King', '1947-09-21', 1947, 0);

INSERT INTO book VALUES (1, 1, 'Sense and Sensibility', 1811, 2);
INSERT INTO book VALUES (2, 1, 'Pride and Prejudice', 1813, 2);
INSERT INTO book VALUES (3, 2, 'Le Capitaine Paul', 1838, 4);
INSERT INTO book VALUES (4, 2, 'Le Capitaine Pamphile', 1839, 4);
INSERT INTO book VALUES (5, 1, 'Northanger Abbey', 1818, 2);

INSERT INTO book_store VALUES ('Amazon');
INSERT INTO book_store VALUES ('Harvard Book Store');

INSERT INTO book_to_book_store VALUES ('Amazon', 1, 10);
INSERT INTO book_to_book_store VALUES ('Amazon', 2, 5);
INSERT INTO book_to_book_store VALUES ('Amazon', 3, 1);
INSERT INTO book_to_book_store VALUES ('Harvard Book Store', 2, 3);
INSERT INTO book_to_book_store VALUES ('Harvard Book Store', 4, 15);
