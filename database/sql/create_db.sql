CREATE TYPE gender AS ENUM ('m', 'f', 'u');
CREATE TABLE players (
       id character varying(36) PRIMARY KEY
       , firstname character varying(100)
       , name character varying(100)
       , birthdate date
       , gender gender
       );
