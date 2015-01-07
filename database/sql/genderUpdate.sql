UPDATE player SET gender = genderUpdate.sex
FROM (
     SELECT p.id, f.sex 
     FROM player p JOIN firstname f ON (p.firstname = f.name)
     WHERE p.gender = 'u'
) AS genderUpdate
WHERE player.id = genderUpdate.id;
