cd /home/christoph/Projekte/wcm-badminton/crawler
unzip backup/data.zip
cd ../database/sql
psql wcm_badminton < create_db.sql
psql wcm_badminton -c"copy countrycode (countrycode, name) from '/home/christoph/Projekte/wcm-badminton/countrycodes.csv' delimiter ';' csv;"
psql wcm_badminton -c"copy firstname from '/home/christoph/Projekte/wcm-badminton/firstname.csv' delimiter ';' csv;"
psql wcm_badminton < countryCodeUpdate.sql
perl ../import/import.pl
psql wcm_badminton < genderUpdate.sql
rm ../../crawler/data -r
