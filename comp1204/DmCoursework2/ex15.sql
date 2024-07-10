SELECT Date_Information.dateRep AS Date_Reported,
       Case_Death_Records.cases AS Number_of_Cases
FROM Case_Death_Records
INNER JOIN Date_Information ON Case_Death_Records.dateRep = Date_Information.dateRep
INNER JOIN Geographic_Information ON Case_Death_Records.geoId = Geographic_Information.geoId
WHERE Geographic_Information.countryterritoryCode = 'GBR'
ORDER BY Date_Information.year, Date_Information.month, Date_Information.day ASC;

