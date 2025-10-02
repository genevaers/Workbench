# How to define a DB2 Database for GenevaERS

This is designed only for Db2 running z/OS. This README
will guide you through the following steps:

1) cloning database/db2 directory contents to USS
2) prepare your site db2 defaults to use with GenevaERS
3) copying necessary JCL, DDL and SQL from USS to MVS datasets so that your site defaults are populated
4) Building the DB2 Schema to contain GenevaERS objects

## Cloning database/db2 directory contents to IBM USS

Logon to USS and using the bash shell enter one of the following commands, depending whether you are using ssh or https:

git clone git@github.com:genevaers/Workbench.git

git clone https://github.com/genevaers/Workbench.git

Then "cd" to the directory database/ and then to db2/

## Prepare your site DB2 defaults

Either using TSO option 3.17 or the "vi" editor open file GetMetaData.sh under directory database/db2/

Locate the relevant section of the file and replace the following with your site defaults. You may eventually want more than one DB2 schemas so repeat the process with different target datasets in each case.
<pre>
export GERS_DBUSER=your-RACFID-for-DB2-administration
export GERS_DBNAME=your-db2-8-character-database-name
export GERS_DBSG=your-db2-8-character-storage-group
export GERS_DBSCH=your-db2-8-character-schema
export GERS_DBSUB=your-db2-4-character-subsystem
export GERS_TO_PDS=your-hlq.your-mlq

</pre>
## Copy JCL, DDL and JCL to MVS PDS[E] dataset

First create the following 3 datasets as shown:

<pre>
//*   .   ensure variables are exportable
//*
//         EXPORT SYMLIST=*
//*
//         SET HLQ=GENEVA
//         SET MLQ=MIDDLE
//* 
//*   .   Delete any prior existing datasets
//*
//DELETE     EXEC   PGM=IDCAMS
//SYSPRINT   DD     SYSOUT=*
//SYSIN      DD *,SYMBOLS=EXECSYS
 DELETE &HLQ..&MLQ..JCL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
 DELETE &HLQ..&MLQ..DDL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
 DELETE &HLQ..MLQ..SQL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
//*
//*   .   Allocate datasets
//* 
//ALLOC    EXEC PGM=IEFBR14,
//            COND=(0,LT)
//SYSPRINT DD SYSOUT=* 
//DBRMJCL  DD DSN=&HLQ..&MLQ..JCL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
//DBRMDDL  DD DSN=&HLQ..&MLQ..DDL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
//DBRMSQL  DD DSN=&HLQ..&MLQ..SQL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
</pre>
To copy the information to MVS type
<pre>
./GetMetaData.sh
</pre>
## Building DB2 Schema to contain GenevaERS objects
First we'll start with building a new GenevaERS environment. A later section deals with how to build a GenevaERS environment and populate it with data from an existing environment, for example replicating an environment you already have.

For a new environment run these job in the following sequence. Do not do this when replicating an existing environment.

<pre>
DRPALL      - drop existing database schema if it exists
BLDDB01     - create database, C_*, E_* and X_* tables
BLDDB03     - create Logic Table/LOB column and indexes
BLDDB02     - create C_*, E_* and X_* indexes
BLDDB04     - create foreign keys
BLDDB05     - load CODETABL and other tables
BLDDB06     - create DB2 views
REPAIR      - remove tablespaces check pending status
INSTSP      - install stored procedures
</pre>

### Note on stored procedures - job INSTSP

Stored Procedures are used by the GENEVA Workbench to access
related metadata in the DB2 database. These native Stored
Procedures so must use DB2 Z/OS Version 11 or above.

Native stored procedures are created directly in DB2.

## Replicating an existing GenevaERS environment
This process differs necessarily from the one above.
<pre>
EXDSNMOD - change LOB file location <===
EXMPNC2  - change schema
DRPALL   - drop existing database schema if it exists
BLDDB01  - create database, C_*, E_* and X_* tables
BLDDB02  - create C_*, E_* and X_* indexes
BLDDB03  - create Logic Table/LOB column and indexes
LOAD01   - load database without E_LOGIC table
LOAD02   - load E_LOGIC table
BLDDB04  - create foreign keys
BLDDB06  - create DB2 views
REPAIR   - remove tablespaces check pending status
INSTSP   - install stored procedures
</pre>