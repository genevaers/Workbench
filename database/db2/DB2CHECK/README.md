# Validation of GenevaERS DB2 Schema definition

This application is provided to allow you to validate the DB2 schema has been correctly defined. It is written in Java and uses JDBC driver for DB2. The program uses digest values for each of the following sets of schema definitions to ensure correctlness. This validates the correctness of the schema structure.

## DB2 schema items checked
<pre>
1) stored procedures
2) table definitions
3) index definitions
4) foreign keys
</pre>

## Configuration file

This contains the userid, password, url of DB2 target and a matching value for the name of schema. It must be located in your home directory.
<pre>
USERID
RACFPWD
jdbc:db2://SP13.pok.stglabs.ibm.com:5036/DM13
SAFR0002
</pre>

## Compiling the program

From directory DB2CHECK/ use the command:
<pre>
mvn install
</pre>

## Executing the program

From directory DB2CHECK/target/ use the command:
<pre>
java -jar db2check-1.0.1-jar-with-dependencies.jar
</pre>

## Command line options

From directory DB2CHECK/target/ use the command:
This generates files containing your schema definitions
<pre>
java -jar db2check-1.0.1-jar-with-dependencies.jar -D
</pre>