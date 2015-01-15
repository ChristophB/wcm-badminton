UPDATE nationality AS n
SET countrycode = c.countrycode
FROM countrycode AS c
WHERE c.name = n.nationality

