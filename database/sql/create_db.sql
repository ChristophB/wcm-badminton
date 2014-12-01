DROP TABLE player_language;
DROP TABLE image;
DROP TABLE webresource;
DROP TABLE player;
DROP TABLE language;
DROP TABLE style;
DROP TABLE city;
DROP TABLE club;
DROP TABLE coach;
DROP TABLE nationality;
DROP TYPE gender;
DROP TYPE hand;
DROP TYPE image_url;

CREATE TYPE gender AS ENUM ('m', 'f', 'u');
CREATE TYPE hand AS ENUM ('right', 'left', 'unknown');
CREATE TYPE image_url AS ENUM ('profile', 'other');

CREATE TABLE club (
       id     serial PRIMARY KEY
       , name text NOT NULL UNIQUE
);
CREATE TABLE coach (
       id serial PRIMARY KEY
       , name text NOT NULL UNIQUE
);    
CREATE TABLE nationality (
       id serial PRIMARY KEY
       , nationality text NOT NULL UNIQUE
);
CREATE TABLE style (
       id serial PRIMARY KEY
       , style text NOT NULL UNIQUE
);
CREATE TABLE city (
       id serial PRIMARY KEY
       , name text NOT NULL
       , state text
       , UNIQUE (name, state)
);
CREATE TABLE player (
       id                      text PRIMARY KEY
       , firstname 	       text
       , name 		       text
       , birthdate 	       date CHECK(birthdate BETWEEN '1900-01-01' AND CURRENT_DATE)
       , gender 	       gender NOT NULL 
       , birthplace_city_id    integer REFERENCES city(id)
                                       ON DELETE NO ACTION
                                       ON UPDATE CASCADE
       , club_id 	       integer REFERENCES club(id) 
                                       ON DELETE NO ACTION 
  				       ON UPDATE CASCADE
       , coach_id	       integer REFERENCES coach(id)
       	 		      	       ON DELETE NO ACTION
				       ON UPDATE CASCADE
       , cur_residence_city_id integer REFERENCES city(id)
                                       ON DELETE NO ACTION
                                       ON UPDATE CASCADE
       , debut_year            text
       --, debut_year            integer CHECK(debut_year BETWEEN EXTRACT(year FROM birthdate) 
       	 		     			        --AND EXTRACT(year FROM CURRENT_DATE))
       , hand                  hand NOT NULL
       , height                double precision CHECK(height BETWEEN 50 AND 250)
       , nationality_id        integer REFERENCES nationality(id)
       	 		     	       ON DELETE NO ACTION
				       ON UPDATE CASCADE
       , nickname 	       text
       , start_competitive     text 
       --, start_competitive     integer CHECK(debut_year BETWEEN EXTRACT(year FROM birthdate)
       	 		        			--AND EXTRACT(year FROM CURRENT_DATE))
       , style_id 	       integer REFERENCES style(id)
                                       ON DELETE NO ACTION
                                       ON UPDATE CASCADE
       , teammember_since      text
       --, teammember_since      integer CHECK(teammember_since BETWEEN EXTRACT(year FROM birthdate)
       	 		     	     			      --AND EXTRACT(year FROM CURRENT_DATE))
);
CREATE TABLE image (
       player_id text REFERENCES player(id)
                      ON DELETE CASCADE
                      ON UPDATE CASCADE
       , url     text NOT NULL
       , PRIMARY KEY(player_id, url)
);
CREATE TABLE webresource (
       player_id  text PRIMARY KEY REFERENCES player(id)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE
       , facebook text
       , twitter  text
       , website  text
       , CHECK(facebook IS NOT NULL
               OR twitter IS NOT NULL
               OR website IS NOT NULL)
);
CREATE TABLE language (
       id         serial PRIMARY KEY
       , language text NOT NULL UNIQUE
);
CREATE TABLE player_language (
       player_id     text REFERENCES player(id) 
       		          ON DELETE CASCADE
       		      	  ON UPDATE CASCADE
       , language_id integer REFERENCES language(id)
       	 	     	     ON DELETE NO ACTION
			     ON UPDATE CASCADE
       , PRIMARY KEY(player_id, language_id) 			    
);
