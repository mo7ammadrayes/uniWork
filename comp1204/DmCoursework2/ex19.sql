SELECT
    d.dateRep AS "Date",
    SUM(cd.cases) OVER (ORDER BY d.year, d.month, d.day) AS "Cumulative UK Cases",
    SUM(cd.deaths) OVER (ORDER BY d.year, d.month, d.day) AS "Cumulative UK Deaths"
FROM
    Case_Death_Records cd
JOIN
    Geographic_Information g ON cd.geoId = g.geoId
JOIN
    Date_Information d ON cd.dateRep = d.dateRep
WHERE
    g.countriesAndTerritories = 'United_Kingdom' 
ORDER BY
    d.year, d.month, d.day;

