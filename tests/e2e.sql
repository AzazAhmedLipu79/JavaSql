-- End-to-end test covering DDL, DML, DQL, JOIN
-- Setup
CREATE DATABASE e2e_demo;
USE e2e_demo;

-- Tables
CREATE TABLE people (id,name,city_id,age);
CREATE TABLE cities (id,city);

-- Inserts
INSERT INTO people (id,name,city_id,age) VALUES (1,'Alice',10,30);
INSERT INTO people (id,name,city_id,age) VALUES (2,'Bob',10,25);
INSERT INTO people (id,name,city_id,age) VALUES (3,'Charlie',20,35);
INSERT INTO people (id,name,city_id,age) VALUES (4,'Diana',20,28);
INSERT INTO cities (id,city) VALUES (10,'Paris');
INSERT INTO cities (id,city) VALUES (20,'London');

-- Queries
SELECT * FROM people WHERE age = 28;
UPDATE people SET age=26 WHERE name = 'Bob';
DELETE FROM people WHERE name = 'Bob';
SELECT name,age FROM people ORDER BY age,name;
SELECT * FROM people JOIN cities ON city_id = id;
SELECT DATABASE;
