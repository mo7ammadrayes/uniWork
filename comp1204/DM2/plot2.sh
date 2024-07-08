#!/bin/bash

# Create a temporary file for SQLite output and a temporary script file for GnuPlot
TEMP_DATA=$(mktemp)
TEMP_SCRIPT=$(mktemp)

# Extract cumulative death data for the top 10 countries
sqlite3 coronavirus.db <<EOF
.headers on
.mode csv
.output $TEMP_DATA
SELECT dateRep, countriesAndTerritories, SUM(deaths) OVER (PARTITION BY countriesAndTerritories ORDER BY date(dateRep)) AS cumulative_deaths
FROM dataset
GROUP BY countriesAndTerritories, dateRep
ORDER BY cumulative_deaths DESC
LIMIT 10;
EOF

# Check if the temporary data file is not empty
if [ ! -s $TEMP_DATA ]; then
    echo "No data retrieved from database, or file is empty."
    exit 1
fi

# Display data for debugging
cat $TEMP_DATA

# Prepare the GnuPlot script
echo "set datafile separator ','
set terminal png
set output 'graph.png'
set xdata time
set timefmt '%m/%d/%Y'  # Adjusted to match the date format in the data
set format x '%m/%d/%Y'
set xtics rotate by -45
set title 'Cumulative Deaths by Country Over Time'
set xlabel 'Date'
set ylabel 'Cumulative Deaths'
plot '$TEMP_DATA' using 1:3:xtic(2) with linespoints title 'Cumulative Deaths'" > $TEMP_SCRIPT

# Run GnuPlot
gnuplot $TEMP_SCRIPT

# Cleanup
rm $TEMP_DATA $TEMP_SCRIPT

