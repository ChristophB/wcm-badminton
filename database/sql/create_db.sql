DROP TABLE player_language;
DROP TABLE images;
DROP TABLE webresources;
DROP TABLE players;
DROP TABLE languages;
DROP TABLE styles;
DROP TABLE cities;
DROP TABLE clubs;
DROP TABLE coaches;
DROP TABLE nationalities;
DROP TYPE gender;
DROP TYPE hand;
DROP TYPE image_url;

CREATE TYPE gender AS ENUM ('m', 'f', 'u');
CREATE TYPE hand AS ENUM ('right', 'left', 'unknown');
CREATE TYPE image_url AS ENUM ('profile', 'other');

CREATE TABLE clubs (
       id     serial PRIMARY KEY
       , name text NOT NULL UNIQUE
);
CREATE TABLE coaches (
       id serial PRIMARY KEY
       , name text NOT NULL UNIQUE
);    
CREATE TABLE nationalities (
       id serial PRIMARY KEY
       , nationality text NOT NULL UNIQUE
);
CREATE TABLE styles (
       id serial PRIMARY KEY
       , style text NOT NULL UNIQUE
);
CREATE TABLE cities (
       id serial PRIMARY KEY
       , name text NOT NULL
       , state text
       , UNIQUE (name, state)
);
CREATE TABLE players (
       id                      text PRIMARY KEY
       , firstname 	       text
       , name 		       text
       , birthdate 	       date CHECK(birthdate BETWEEN '1900-01-01' AND CURRENT_DATE)
       , gender 	       gender NOT NULL 
       , birthplace_city_id    integer REFERENCES cities(id)
                                       ON DELETE NO ACTION
                                       ON UPDATE CASCADE
       , club_id 	       integer REFERENCES clubs(id) 
                                       ON DELETE NO ACTION 
  				       ON UPDATE CASCADE
       , coach_id	       integer REFERENCES coaches(id)
       	 		      	       ON DELETE NO ACTION
				       ON UPDATE CASCADE
       , cur_residence_city_id integer REFERENCES cities(id)
                                       ON DELETE NO ACTION
                                       ON UPDATE CASCADE
       , debut_year            text
       --, debut_year            integer CHECK(debut_year BETWEEN EXTRACT(year FROM birthdate) 
       	 		     			        --AND EXTRACT(year FROM CURRENT_DATE))
       , hand                  hand NOT NULL
       , height                double precision CHECK(height BETWEEN 50 AND 250)
       , nationality_id        integer REFERENCES nationalities(id)
       	 		     	       ON DELETE NO ACTION
				       ON UPDATE CASCADE
       , nickname 	       text
       , start_competitive     text 
       --, start_competitive     integer CHECK(debut_year BETWEEN EXTRACT(year FROM birthdate)
       	 		        			--AND EXTRACT(year FROM CURRENT_DATE))
       , style_id 	       integer REFERENCES styles(id)
                                       ON DELETE NO ACTION
                                       ON UPDATE CASCADE
       , teammember_since      text
       --, teammember_since      integer CHECK(teammember_since BETWEEN EXTRACT(year FROM birthdate)
       	 		     	     			      --AND EXTRACT(year FROM CURRENT_DATE))
);
CREATE TABLE images (
       player_id text REFERENCES players(id)
                      ON DELETE CASCADE
                      ON UPDATE CASCADE
       , url     text NOT NULL
       , PRIMARY KEY(player_id, url)
);
CREATE TABLE webresources (
       player_id  text PRIMARY KEY REFERENCES players(id)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE
       , facebook text
       , twitter  text
       , website  text
       , CHECK(facebook IS NOT NULL
               OR twitter IS NOT NULL
               OR website IS NOT NULL)
);
CREATE TABLE languages (
       id         serial PRIMARY KEY
       , language text NOT NULL UNIQUE
);
CREATE TABLE player_language (
       player_id     text REFERENCES players(id) 
       		          ON DELETE CASCADE
       		      	  ON UPDATE CASCADE
       , language_id integer REFERENCES languages(id)
       	 	     	     ON DELETE NO ACTION
			     ON UPDATE CASCADE
       , PRIMARY KEY(player_id, language_id) 			    
);
