# How to load a DB2 Database with Stored procedures

This is designed only for Db2 running z/OS. This README
will guide you to define a Stored Procedure.

SAFR Stored Procedures are used by the SAFR Workbench 
to access metadata in the SAFR DB2 database. These are native
Stored Procedures so must use DB2 Z/OS Version 9 or above.
Native stored procedures are created directly in DB2.

## this activity is performed on a z/OS system.

   The first step is to have the repo placed in a USS directory.

   This example shows a git clone action positioning the repo
   in a USS directory

   git clone git@github.ibm.com:SAFR/wb420.git 

## allocate a z/OS PDS

   Here is a suggested batch JCL to allocate such a data set
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
//               SET HLQ1=GENEVA.GVBSTOR
//* 
//*   .   Delete any prior existing dataset
//*
//DELETE     EXEC   PGM=IDCAMS
//SYSPRINT   DD     SYSOUT=*
//SYSIN      DD *,SYMBOLS=EXECSYS
 DELETE &HLQ1..GVBSTOR
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
//*
//*   .   Allocate dataset
//* 
//ALLOC    EXEC PGM=IEFBR14,
//            COND=(0,LT)
//SYSPRINT DD SYSOUT=* 
//DBRM     DD DSN=&HLQ1..GVBSTOR,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
</pre>
## move parts into a z/OS PDS

   Here is a suggested shell script to copy files from a USS folder 
   to the newly allocated PDS.

   The first parameter to the shell script ($1) is the high level
   qualifier of the GVBDDL PDS

   The second parameter to the shell script ($2) is USS directory 
   where the the GVBDDL source resides

   e.g. /u/user1/wb420
<pre>
#!/bin/bash  
# 
#   .   copyfiles into GVBSTOR  
#
for entry in `ls $2/StorProc`; do  
    fullf=$entry 
    fname=${fullf%%.*}
    echo $fname 
    cp -F nl $2/StorProc/$entry "//'$1.GVBSTOR($fname)'" 
    if (( $? )); then 
                echo "------------------------------------"
                echo "$fname has not been moved to GVBSTOR"
                echo "------------------------------------" 
                exit 1 
    fi 
done 
exit
</pre>
## Update the DEFPROCS member 

   This will add in the Db2 subsystem libraries, 
   Language Environment libraries
   and Db2 utility program names

   The DEFPROCS member will have a variable block at its
   begining to allow these changes to be done easily.
  
## Run the DEFPROCS member to define the Db2 objects
   
