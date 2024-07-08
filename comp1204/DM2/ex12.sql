-- Insert data into Date Information Table
INSERT OR IGNORE INTO Date_Information (dateRep, day, month, year)
SELECT dateRep, day, month, year
FROM dataset;

-- Insert data into Geographic Information Table
INSERT OR IGNORE INTO Geographic_Information (geoId,countriesAndTerritories,countryterritoryCode, continentExp, popData2020)
SELECT geoId, countriesAndTerritories, countryterritoryCode, continentExp, popData2020
FROM dataset;

-- Insert data into Case and Death Records Table
INSERT OR IGNORE INTO Case_Death_Records (dateRep, geoId, cases, deaths)
SELECT dateRep, geoId, cases, deaths
FROM dataset;

