SELECT
    g.countriesAndTerritories AS "Country Name",
    ROUND((SUM(cd.deaths) * 100.0 / SUM(cd.cases)), 2) AS "Percentage Deaths of Country Cases (%)"
FROM
    Case_Death_Records cd
JOIN
    Geographic_Information g ON cd.geoId = g.geoId
GROUP BY
    g.countriesAndTerritories
HAVING
    SUM(cd.cases) > 0  -- To prevent division by zero
ORDER BY
    "Percentage Deaths of Country Cases (%)" DESC
LIMIT 10;

