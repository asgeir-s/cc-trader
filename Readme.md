CCTrader
========================

Pre-Requirements
------------------------
* Postgres database
* sbt

To Run
------------------------
1. Configure "CCTrader/src/main/resources/RENAME_application.conf" to point to your database.
2. Rename the file to: "application.conf"
3. Go to the project root folder
4. Run with: "sbt run"


OkCoin:
1. get previous candlestick through REST
2. get previous ticker / trades data through REST
3. stream live candlestick data
4. stream live ticker / trades data


Bitfinex
1. get previous candlestick through REST
2. get previous ticker / trades data through REST
3. stream live candlestick data
4. stream live ticker / trades data


    Must have a Postgres DB with granuleritys