# How to load a DB2 Database with Stored procedures

This is designed only for Db2 running z/OS. This README
will guide you to define a Stored Procedure.

Stored Procedures are used by the GENEVA Workbench to access
related metadata in the DB2 database. These native Stored
Procedures so must use DB2 Z/OS Version 11 or above.

Native stored procedures are created directly in DB2.

## this activity is performed on a z/OS system.

   The first step is to clone the Workbench repository to USS.

   git clone git@github.com:genevaers/Workbench.git 

## allocate the z/OS PDS datasets

   The following JCL allocates datasets used to copy JCL, DDL
   and SQL statements:
<pre> 
//* 
//*   .   ensure variables are exportable
//*
//         EXPORT SYMLIST=*
//*
//*   Please answer the following question before submitting
//*   this job
//*
//* Question 1.  What is the High Level Qualifier for the 
//*              PDS to hold DB2 data definition source.?
//               SET HLQ1=GENEVA
//* 
//*   .   Delete any prior existing datasets
//*
//DELETE     EXEC   PGM=IDCAMS
//SYSPRINT   DD     SYSOUT=*
//SYSIN      DD *,SYMBOLS=EXECSYS
 DELETE &HLQ1..GVBSTOR.JCL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
 DELETE &HLQ1..GVBSTOR.DDL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
 DELETE &HLQ1..GVBSTOR.SQL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
 DELETE &HLQ1..GVBSTOR.PROC.JCL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
//*
//*   .   Allocate datasets
//* 
//ALLOC    EXEC PGM=IEFBR14,
//            COND=(0,LT)
//SYSPRINT DD SYSOUT=* 
//DBRMJCL  DD DSN=&HLQ1..GVBSTOR.JCL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
//DBRMDDL  DD DSN=&HLQ1..GVBSTOR.DDL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
//DBRMSQL  DD DSN=&HLQ1..GVBSTOR.SQL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
/DBRMDPP  DD DSN=&HLQ1..GVBSTOR.PROC.JCL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
</pre>
## copy the JCL, DDL and SQL to the z/OS PDS datasets

   Here are suggested shell script commands to copy files from the USS folders 
   to the newly allocated PDS datasets.

<pre>
cp -vS d=.JCL ~/git/public/Workbench/database/db2/*.JCL "//'GENEVA.GVBSTOR.JCL'"
cp -vS d=.DDL ~/git/public/Workbench/database/db2/*.DDL "//'GENEVA.GVBSTOR.DDL'"
cp -vS d=.SQL ~/git/public/Workbench/database/db2/StorProc/*.SQL "//'GENEVA.GVBSTOR.SQL'"
cp -vS d=.JCL ~/git/public/Workbench/database/db2/StorProc/*.JCL "//'GENEVA.GVBSTOR.PROC.JCL'"
</pre>
## Update the DEFPROCS member 

   Add the Db2 subsystem libraries, Language Environment libraries
   and Db2 utility program names in the Standard Variables section
   of the JCL
  
## Run the DEFPROCS member to define the Db2 objects
   
   Run DEFPROCS to remove from the DB2 database any existing stored procedures and to define
   them again using the definitions provided for the current GENEVA version.
