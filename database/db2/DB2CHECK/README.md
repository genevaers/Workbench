# Validation of GenevaERS DB2 Schema definition

This application is provided to allow you to validate the DB2 schema has been correctly defined. It is written in Java and uses JDBC driver for DB2. The program uses digest values for each of the following sets of schema definitions to ensure correctlness. This validates the correctness of the schema structure.

## Types of DB2 schema items checked
<pre>
1) stored procedures
2) table definitions
3) index definitions
4) foreign keys
</pre>

## Configuration file

This contains the userid, password, url of DB2 target and a matching value for the name of your schema. It must be located in your home directory. A sample is provided as Workbench/DB2CHECK/db2check.config:
<pre>
USERID
RACFPWD
jdbc:db2://SP13.pok.stglabs.ibm.com:5036/DM13
SCHEMANM
</pre>

## Schema Digest file

In order to verify the correctness of the GenevaERS schema stored in the DB2 catalog a digest file is referenced containing hash values pertaining to each item belonging to the schema. In order to run the verification first perform the following:
<pre>
cp Workbench/database/db2/DB2CHECK/SchemaDigest.txt ~/GenevaERS/SchemaDigest.txt
</pre>

## Schema Report

Verification of the GenevaERS DB2 schema will produce a report ~/GenevaERS/schema_report.txt. For each of the above four schema item types the correctness of each item is reported. If there is a definition error in the DB2 schema the report will pinpoint it exactly.

## Compiling the program

From directory DB2CHECK/ use the command:
<pre>
mvn install
</pre>

## Executing the program

From directory DB2CHECK/ run the following script. This verifies the definitions stored within the DB2 catalog are correct.
<pre>
./runDB2check.sh
</pre>

## Command line option -D

From directory DB2CHECK/ run the following script. This generates four output files containing your schema definitions as well as verifying the definitions stored within the DB2 catalog are correct. The files are output to directory GenevaERS in your home diretory.
<pre>
./runDB2check.sh -D
</pre>

## Command line option -f

From directory DB2CHECK/ run the following script. This option provides FINE (detailed) logging level in addition to logging levels above this.
<pre>
./runDB2check.sh -D -f
</pre>

## Command line option -A

This option is not required unless you have been advised to make a change to the GenevaERS schema. It will generate a new SchemaDigest file. Specifically, if there is a definiton error in your DB2 Schema then generating a new SchemaDigest file will propagate this error and consequently you will not be able to verify the schema correctness. Use only as directed, for example after accepting a proven fix to the GenevaERS schema definition.
<pre>
./runDB2check.sh -A
</pre>