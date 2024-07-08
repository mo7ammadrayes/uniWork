-- Table: Date Information
CREATE TABLE IF NOT EXISTS Date_Information (
    dateRep TEXT PRIMARY KEY NOT NULL,
    day INTEGER NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL
);

-- Table: Geographic Information
CREATE TABLE IF NOT EXISTS Geographic_Information (
    geoId TEXT PRIMARY KEY NOT NULL,
    countryterritoryCode TEXT NOT NULL,
    countriesAndTerritories TEXT NOT NULL,
    continentExp TEXT NOT NULL,
    popData2020 INTEGER
);

-- Table: Case and Death Records
CREATE TABLE IF NOT EXISTS Case_Death_Records (
    dateRep TEXT NOT NULL,
    geoId TEXT NOT NULL,
    cases INTEGER,
    deaths INTEGER,
    PRIMARY KEY (dateRep, geoId),
    FOREIGN KEY (dateRep) REFERENCES Date_Information(dateRep),
    FOREIGN KEY (geoId) REFERENCES Geographic_Information(geoId)
);

