SELECT
    g.countriesAndTerritories AS CountryName,  -- Using country name instead of country code
    d.dateRep AS Date,
    cd.cases AS Cases,
    cd.deaths AS Deaths
FROM
    Case_Death_Records cd
JOIN
    Geographic_Information g ON cd.geoId = g.geoId
JOIN
    Date_Information d ON cd.dateRep = d.dateRep
ORDER BY
    d.year ASC, d.month ASC, d.day ASC, g.countriesAndTerritories ASC;

