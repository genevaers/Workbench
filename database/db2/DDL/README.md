# How to load a DB2 Database with sample data

This is designed only for Db2 running z/OS. This README
will guide you to define and load a schema with data.

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
//*   job.
//*
//* Question 1.  What is the High Level Qualifier for the
//*              PDS to hold DB2 data definition source.?
//               SET HLQ1=GENEVA.GVBDDL
//*
//*   .   Delete any prior existing dataset
//*
//DELETE     EXEC   PGM=IDCAMS
//SYSPRINT   DD     SYSOUT=*
//SYSIN      DD *,SYMBOLS=EXECSYS
 DELETE &HLQ1..GVBDDL
 IF LASTCC > 0 THEN -
   SET MAXCC = 0
//*
//*   .   Allocate dataset
//*
//ALLOC    EXEC PGM=IEFBR14,
//            COND=(0,LT)
//SYSPRINT DD SYSOUT=*
//DBRM     DD DSN=&HLQ1..GVBDDL,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,DSNTYPE=LIBRARY,
//            SPACE=(TRK,(10,10),RLSE),
//            DSORG=PO,RECFM=FB,LRECL=80
</pre>
## move parts into a z/OS PDS

   Here is a suggested shell script to copy files from a USS folder 
   to the newly allocated PDS.

   The first parameter to the shell script ($1) is the
   high level qualifier of the GVBDDL PDS

   The second parameter to the shell script ($2) is USS directory
   where the the GVBDDL source resides

   e.g. /u/user1/wb420 
<pre>
#!/bin/bash  
#
#   .   copyfiles into GVBDDL
#
for entry in `ls $2/Database_new_format`; do 
    fullf=$entry
    fname=${fullf%%.*}
    echo $fname
    cp -F nl $2/Database_new_format/$entry "//'$1.GVBDDL($fname)'"
    if (( $? )); then
                echo "-----------------------------------"
                echo "$fname has not been moved to GVBDDL"
                echo "-----------------------------------"
                exit 1
    fi
done 
exit
</pre>
## Update the DEFINE member 

   This will add in the Db2 subsystem libraries,
   Language Environment libraries
   and Db2 utility program names

   The DEFINE member will have a variable block at its
   begining to allow these changes to be done easily.
  
## Run the DEFINE member to define the Db2 objects
