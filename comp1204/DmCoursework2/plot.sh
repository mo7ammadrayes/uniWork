#!/bin/bash

# Define temporary files
TEMP_DATA=$(mktemp)
TEMP_SCRIPT=$(mktemp)

# Execute the SQL query to fetch the required data
sqlite3 coronavirus.db <<EOF
.headers on
.mode csv
.output $TEMP_DATA
WITH CumulativeDeaths AS (
    SELECT 
        countriesAndTerritories,
        strftime('%Y-%m-%d', dateRep) AS formattedDate,
        SUM(deaths) OVER (PARTITION BY countriesAndTerritories ORDER BY date(dateRep) ASC) AS cumulative_deaths
    FROM dataset
),
TopCountries AS (
    SELECT 
        countriesAndTerritories,
        MAX(cumulative_deaths) AS max_deaths
    FROM CumulativeDeaths
    GROUP BY countriesAndTerritories
    ORDER BY max_deaths DESC
    LIMIT 10
)
SELECT 
    cd.formattedDate AS dateRep,
    cd.countriesAndTerritories,
    cd.cumulative_deaths
FROM CumulativeDeaths cd
JOIN TopCountries tc ON cd.countriesAndTerritories = tc.countriesAndTerritories
ORDER BY cd.countriesAndTerritories, cd.formattedDate;
EOF

# Check if data was retrieved successfully
if [ ! -s $TEMP_DATA ]; then
    echo "Error: No data retrieved from database, or file is empty."
    exit 1
fi

# Display data for debugging
cat $TEMP_DATA

# Prepare the GnuPlot script
echo "set datafile separator ','
set terminal png
set output 'graph.png'
set xdata time
set timefmt '%Y-%m-%d'
set format x '%Y-%m-%d'
set xtics rotate by -45
set title 'Cumulative Deaths by Country Over Time'
set xlabel 'Date'
set ylabel 'Cumulative Deaths'
plot '$TEMP_DATA' using 1:3:xtic(2) with linespoints title 'Cumulative Deaths'" > $TEMP_SCRIPT

# Execute the GnuPlot script to generate the graph
gnuplot $TEMP_SCRIPT

# Cleanup: Remove temporary files
rm $TEMP_DATA $TEMP_SCRIPT

