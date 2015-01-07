create table CountryCode(id SERIAL primary key, countryCode varchar(3) NOT NULL, name varchar(50) not null);

ALTER TABLE nationality ADD COLUMN countryCode varchar(3);

UPDATE nationality as n
SET countrycode = c.countrycode
FROM countrycode as c
WHERE c.name = n.nationality

