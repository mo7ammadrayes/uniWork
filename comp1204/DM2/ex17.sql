SELECT
    g.countriesAndTerritories AS CountryName,
    ROUND(SUM(cd.cases) * 100.0 / g.popData2020, 2) AS Cases_Percentage,
    ROUND(SUM(cd.deaths) * 100.0 / g.popData2020, 2) AS Deaths_Percentage
FROM
    Case_Death_Records cd
JOIN
    Geographic_Information g ON cd.geoId = g.geoId
GROUP BY
    g.countriesAndTerritories;
