CREATE TABLE discipline (
       id     int primary key not null
       , name varchar(20) not null
	   , shortName varchar(2) not null
);

ALTER TABLE player ADD COLUMN discipline_id integer REFERENCES discipline(id) ON DELETE NO ACTION ON UPDATE CASCADE;

INSERT INTO discipline VALUES 	(1, 'Men''s Singles', 'MS'), 
								(2, 'Women''s Singles', 'WS'), 
								(3, 'Men''s Doubles', 'MD'), 
								(4, 'Women''s Doubles', 'WD'), 
								(5, 'Mixed Doubles', 'MX');
