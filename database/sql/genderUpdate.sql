create table firstname(id int primary key not null, name varchar(50) not null, sex gender not null);

update player set gender = genderUpdate.sex
from (select p.id, f.sex from player p, firstname f
where p.firstname = f.name and p.gender = 'u') as genderUpdate
where player.id = genderUpdate.id