# How to define a DB2 Database for GenevaERS

This is intended for Db2 running z/OS at DB2 version 11 and above.
There are distinct and separate processes for either creating a new DB2 Schema for GenevaERS or for replicating an existing GenevaERS DB2 Schema. Ensure you follow the appropriate instructions.

## Summary of steps involved
<pre>
1) clone database/db2 directory contents to USS
2) prepare your site db2 defaults to use with GenevaERS
3) copy JCL, DDL SQL to MVS datasets with your site defaults
4A) Build the DB2 Schema to contain new GenevaERS objects, OR 
4B) Replicate an existing GenevaERS DB2 Schema and objects
</pre>
## Clone database/db2 directory contents to IBM USS

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
export GERS_TO_PDS_HLQ=your-pds-hlq
export GERS_TO_PDS_MLQ=your-pds-mlq
export GERS_JOB_CLASS=your-job-class
export GERS_JOB_MSG_CLASS=your-msg-class
export GERS_DB2_PROCLIB=your-DB2-proclib
export GERS_DB2_LOADLIB=your-DB2-loadlib
export GERS_DB2_EXITLIB=your-DB2-exit-lib
export GERS_DB2_RUNLIB=your-DB2-runlib
export GERS_DB2_PLAN=your-DB2-administration-plan-used
export GERS_SCEERUN=your-CEE.SCEERUN
export GERS_SCEERUN2=your-CEE.SCEERUN2
export GERS_SCBCDLL=your-CBC.SCLBDLL
</pre>
## Copy JCL, DDL and JCL to MVS PDS[E] dataset

Logon to TSO and copy the following JCL into an existing jobs library, using your own jobcard. Ensure you set the HLQ and MLQ symbolics as you require:

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
To copy the information to your newly allocated MVS datasets type the following in USS:
<pre>
./MakeDB2Schema.sh
</pre>
## Build DB2 Schema to contain GenevaERS objects
First we'll cover building a new GenevaERS environment. A later section deals with how to build a GenevaERS environment and populate it with data from an existing environment, i.e. replicating a GenevaERS environment you already have.

For a new environment run these job in the following sequence. To replicate and environment run the steps in the subsequent section also.

<pre>
DROPALL     - drop existing database schema if it exists
BLDDB01     - create database, C_*, E_* and X_* tables
BLDDB02     - create Logic Table/LOB column
BLDDB03     - create C_*, E_* and X_* indexes
BLDDB04     - create foreign keys
BLDDB05     - load CODETABL and the other table
BLDDB06     - create DB2 views
REPAIR      - remove tablespaces check pending status
INSTSP      - install stored procedures
</pre>

## Replicate an existing GenevaERS environment and objects

These additional steps populate the new environment. This will replicate metadata from an existing GenevaERS DB2 schema.
<pre>
UNLOAD   - unload GenevaERS data from existing DB2 schema             - new
EXDSNMOD - change LOB file location <===
EXMPNC2  - change schema
DROPVIEW - drop views                                                 - new
DROPFKEY - drop foregin keys                                          - new
LOAD01   - load database without E_LOGIC table
LOAD02   - load E_LOGIC table
BLDDB04  - create foreign keys
BLDDB06  - create DB2 views
REPAIR   - remove tablespaces check pending status
</pre>

### Note on stored procedures - job INSTSP

Stored Procedures are used by the GENEVA Workbench to access
related metadata in the DB2 database. These native Stored
Procedures so must use DB2 Z/OS Version 11 or above.

Native stored procedures are created directly in DB2.

