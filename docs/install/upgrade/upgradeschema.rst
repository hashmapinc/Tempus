##########################
Schema Upgrade For Stories
##########################

These commands need to be run manually to alter/update the database for the mentioned stories.

***************************************
Tempus-364 Serverless compute on Tempus
***************************************

1. The key point here is now, there can be computations other than spark computation namely kubleless computations.
2. The schema of the computation tables which currently holds the configuration parameters like jar_name, jar_path etc
   will not be present in kubeless computation.
3. This means kubeless computation have different configuration, hence a need for schema alteration of computations.

For SQL
=======

1. Refer the file `../../../dao/src/main/resources/sql/upgrade/5.sql`

For NOSQL
=========

2. Refer the file `../../../dao/src/main/resources/cassandra/upgrade/5.cql`
