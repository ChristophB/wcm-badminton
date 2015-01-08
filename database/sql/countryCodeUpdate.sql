create table CountryCode(id SERIAL primary key, countryCode varchar(3) NOT NULL, name varchar(50) not null);

ALTER TABLE nationality ADD COLUMN countryCode varchar(3);

UPDATE nationality as n
SET countrycode = c.countrycode
FROM countrycode as c
WHERE c.name = n.nationality

update nationality set countrycode = 'GBR' where nationality = 'England';
update nationality set countrycode = 'CHN' where nationality = 'Macau';
update nationality set countrycode = 'KOR' where nationality = 'Korea';
update nationality set countrycode = 'TWN' where nationality = 'Chinese Taipei';
update nationality set countrycode = 'GBR' where nationality = 'Scotland';
update nationality set countrycode = 'MKD' where nationality = 'FYR Macedonia';
update nationality set countrycode = 'USA' where nationality = 'U.S.A.';
update nationality set countrycode = 'GBR' where nationality = 'Wales';
update nationality set countrycode = 'PYF' where nationality = 'French Polynesia (Tahiti)';
update nationality set countrycode = 'SYR' where nationality = 'Syrian Arab Republic';
update nationality set countrycode = 'BRN' where nationality = 'Brunei Darussalem';
update nationality set countrycode = 'COG' where nationality = 'Congo, Dem. Rep.';
update nationality set countrycode = 'PRK' where nationality = 'Korea, Dem. People''s Rep.';

