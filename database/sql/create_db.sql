DROP TABLE player_language;
DROP TABLE players;
DROP TABLE languages;
DROP TABLE clubs;
DROP TABLE coaches;
DROP TABLE nationalities;
DROP TYPE gender;
DROP TYPE hand;

CREATE TYPE gender AS ENUM ('m', 'f', 'u');
CREATE TYPE hand AS ENUM ('right', 'left', 'unknown');

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
CREATE TABLE players (
       id                    text PRIMARY KEY
       , firstname 	     text
       , name 		     text
       , birthdate 	     date CHECK(birthdate BETWEEN '1900-01-01' AND CURRENT_DATE)
       , gender 	     gender NOT NULL 
       , birthplace_city     text
       , birthplace_state    text
       , club_id 	     integer REFERENCES clubs(id) 
                                     ON DELETE NO ACTION 
  				     ON UPDATE CASCADE
       , coach_id	     integer REFERENCES coaches(id)
       	 		      	     ON DELETE NO ACTION
				     ON UPDATE CASCADE
       , cur_residence_city  text
       , cur_residence_state text
       , debut_year          integer CHECK(debut_year BETWEEN EXTRACT(year FROM birthdate) 
       	 		     			      AND EXTRACT(year FROM CURRENT_DATE))
       , facebook            text
       , hand                hand NOT NULL
       , height              double precision CHECK(height BETWEEN 50 AND 250)
       , nationality_id      integer REFERENCES nationalities(id)
       	 		     	     ON DELETE NO ACTION
				     ON UPDATE CASCADE
       , nickname 	     text
       , start_competitive   integer CHECK(debut_year BETWEEN EXTRACT(year FROM birthdate)
       	 		     			      AND EXTRACT(year FROM CURRENT_DATE))
       , style 		     text
       , teammember_since    integer CHECK(teammember_since BETWEEN EXTRACT(year FROM birthdate)
       	 		     	     			    AND EXTRACT(year FROM CURRENT_DATE))
       , twitter 	     text
       , website 	     text
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
