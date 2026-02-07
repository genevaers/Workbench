#!/bin/sh
# prepare_ddl.sh - Prepare data management statements for user preferences
########################################################

main() {

# &$DBUSER.   DATABASE USER
# &$DBNAME.   DATABASE NAME
# &$DBSG.     DATABASE STORAGE GROUP
# &$DBSCH.    DATABASE SCHEMA
# &$DBSUB.    DATABASE SUBSYSTEM

# &$JBCLASS.  JOB CLASS   -- GERS_JOB_CLASS=A;
# &$MSGCLASS. MSG CLASS   -- GERS_JOB_MSG_CLASS=H;
# &$DB2PLIB.  DB2 PROCLIB -- GERS_DB2_PROCLIB=DSN.V13R1M0.PROCLIB;
# &$DB2RLIB.  DB2 RUNLIB  -- GERS_DB2_RUNLIB=DSN131.RUNLIB.LOAD;
# &$DB2PLAN.  DB2 PLAN    -- GERS_DB2_PLAN=DSNTEP13;
# &$PDSHLQ.   HLQ         --
# &$PDSMLQ.   MLQ         --

# Check if member name is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <name of file to prepare";
  echo "Example: $0 GVBQDRAL.DDL";
  exit 1;
fi

MEMBER="$1";

# never mind the symbolics
mycmdstr1='s/&$DBUSER.'/${GERS_DB2_DBUSER}/'g';
mycmdstr2='s/&$DBNAME.'/${GERS_DB2_DBNAME}/'g'
mycmdstr3='s/&$DBSG.'/${GERS_DB2_STGGRP}/'g'
mycmdstr4='s/&$DBSCH.'/${GERS_DB2_DBSCH}/'g'
mycmdstr5='s/&$DBSUB.'/${GERS_DB2_SUBSYSTEM}/'g'
mycmdstr6='s/&$DB2PLAN.'/${GERS_DB2_PLAN}/'g'
mycmdstr7='s/&$DB2PLIB.'/${GERS_DB2_PROCLIB}/'g'
mycmdstr8='s/&$DB2RLIB.'/${GERS_DB2_RUN_LIB}/'g'
mycmdstr9='s/&$DB2XLIB.'/${GERS_DB2_EXIT_LIB}/'g'
mycmdstr10='s/&$DB2LLIB.'/${GERS_DB2_LOAD_LIB}/'g'

mycmdstr11='s/&$JBCLASS.'/${GERS_JOB_CLASS}/'g'
mycmdstr12='s/&$MSGCLASS.'/${GERS_JOB_MSG_CLASS}/'g'
mycmdstr13='s/&$SCEELIB.'/${GERS_LE_RUN_LIB}/'g'
mycmdstr14='s/&$SCEE2LIB.'/${GERS_LE_RUN_LIB2}/'g'
mycmdstr15='s/&$SCBCDLL.'/${GERS_SCBCDLL}/'g'
mycmdstr16='s/&$PDSHLQ.'/${GERS_TO_PDS_HLQ}/'g'
mycmdstr17='s/&$PDSMLQ.'/${GERS_TO_PDS_MLQ}/'g'

#optional ones for replication
mycmdstr18='s/&$FROMPDSHLQ.'/${GERS_FROM_PDS_HLQ}/'g'
mycmdstr19='s/&$FROMPDSMLQ.'/${GERS_FROM_PDS_MLQ}/'g'
mycmdstr20='s/&$FROMDBUSER.'/${GERS_FROM_DB2_DBUSER}/'g';
mycmdstr21='s/&$FROMDBNAME.'/${GERS_FROM_DB2_DBNAME}/'g';
mycmdstr22='s/&$FROMDBSG.'/${GERS_FROM_DB2_DBSG}/'g';
mycmdstr23='s/&$FROMDBSCH.'/${GERS_FROM_DB2_DBSCH}/'g';
mycmdstr24='s/&$FROMDBSUB.'/${GERS_FROM_DB2_DBSUB}/'g';

#to run optional smoke test with DB2
mycmdstr25='s/&$ENVHLQ.'/${GERS_ENV_HLQ}/'g';
mycmdstr26='s/&$DEMOHLQ.'/${GERS_DEMO_HLQ}/'g';
mycmdstr27='s/&$DEMOMLQ.'/${GERS_DEMO_MLQ}/'g';
mycmdstr28='s/&$JVMPLIB.'/${GERS_JVM_PROC_LIB}/'g';
mycmdstr29='s/&$JZOSLIB.'/${GERS_JZOS_LOAD_LIB}/'g';
mycmdstr30='s/&$DB2HOST.'/${GERS_DB2_HOST}/'g';
mycmdstr31='s/&$DB2PORT.'/${GERS_DB2_PORT}/'g';
mycmdstr32='s/&$DB2SENV.'/${GERS_DB2_SAFR_ENV}/'g';
mycmdstr33='s@&$JAVAHOME.'@${GERS_JAVA_HOME}@'g';
mycmdstr34='s@&$RCAJAR.'@${GERS_RCA_JAR_DIR}@'g';


# perform substitutions which unfortunately still converts to ACII with -W filecodeset=IBM-1047 
sed $mycmdstr1   $MEMBER > prep/tmp1;
sed $mycmdstr2 prep/tmp1 > prep/tmp2;
sed $mycmdstr3 prep/tmp2 > prep/tmp3;
sed $mycmdstr4 prep/tmp3 > prep/tmp4;
sed $mycmdstr5 prep/tmp4 > prep/tmp5;
sed $mycmdstr6 prep/tmp5 > prep/tmp6;
sed $mycmdstr7 prep/tmp6 > prep/tmp7;
sed $mycmdstr8 prep/tmp7 > prep/tmp8;
sed $mycmdstr9 prep/tmp8 > prep/tmp9;
sed $mycmdstr10 prep/tmp9 > prep/tmp10;
sed $mycmdstr11 prep/tmp10 > prep/tmp11;
sed $mycmdstr12 prep/tmp11 > prep/tmp12;
sed $mycmdstr13 prep/tmp12 > prep/tmp13;
sed $mycmdstr14 prep/tmp13 > prep/tmp14;
sed $mycmdstr15 prep/tmp14 > prep/tmp15;
sed $mycmdstr16 prep/tmp15 > prep/tmp16;
sed $mycmdstr17 prep/tmp16 > prep/tmp17;
sed $mycmdstr18 prep/tmp17 > prep/tmp18;
sed $mycmdstr19 prep/tmp18 > prep/tmp19;
sed $mycmdstr20 prep/tmp19 > prep/tmp20;
sed $mycmdstr21 prep/tmp20 > prep/tmp21;
sed $mycmdstr22 prep/tmp21 > prep/tmp22;
sed $mycmdstr23 prep/tmp22 > prep/tmp23;
sed $mycmdstr24 prep/tmp23 > prep/tmp24;
sed $mycmdstr25 prep/tmp24 > prep/tmp25;
sed $mycmdstr26 prep/tmp25 > prep/tmp26;
sed $mycmdstr27 prep/tmp26 > prep/tmp27;
sed $mycmdstr28 prep/tmp27 > prep/tmp28;
sed $mycmdstr29 prep/tmp28 > prep/tmp29;
sed $mycmdstr30 prep/tmp29 > prep/tmp30;
sed $mycmdstr31 prep/tmp30 > prep/tmp31;
sed $mycmdstr32 prep/tmp31 > prep/tmp32;
sed $mycmdstr33 prep/tmp32 > prep/tmp33;
#sed $mycmdstr34 prep/tmp33 > prep/tmp34;
sed $mycmdstr34 prep/tmp34 > $MEMBER;
# last tmp file is referenced below several times

# Remove DB2RLIB contextualy, if variable not set
echo "GERS_INCLUDE_DB2_RUNLIB: $GERS_INCLUDE_DB2_RUNLIB";
if [[ $GERS_INCLUDE_DB2_RUNLIB == "N" ]]; then
  echo "Removing references to DB2RLIB";
  echo "Processing file: $MEMBER";
  ./editRLIB.sh $MEMBER;
    exitIfError;
  echo "File $MEMBER copied from /temp back to /prep with DB2RLIB removed if it was present";
  cp -f temp/tmp34 prep/$tmp34;
fi

#convert output back to EBCDIC again
iconv -f ISO8859-1 -t IBM-1047 prep/tmp34 > prep/$MEMBER;
chtag -r prep/$MEMBER;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"