#!/bin/sh
# prepare_ddl.sh - Prepare JCL jobs for user preferences
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
  echo "Usage: $0 <name of file to prepare> <name of from directory>";
  echo "Example: $0 GVBQDRAL.DDL";
  exit 1;
fi

MEMBER="$1";
FROM_DIR="$2";

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

# echo "sed $mycmdstr1   ../$FROM_DIR/$MEMBER > ../"$FROM_DIR"/prep/tmp1;"

# perform substitutions which unfortunately still converts to ACII with -W filecodeset=IBM-1047 
sed $mycmdstr1 ../"$FROM_DIR"/"$MEMBER" > ../"$FROM_DIR"/prep/tmp1;
sed $mycmdstr2 ../"$FROM_DIR"/prep/tmp1 > ../"$FROM_DIR"/prep/tmp2;
sed $mycmdstr3 ../"$FROM_DIR"/prep/tmp2 > ../"$FROM_DIR"/prep/tmp3;
sed $mycmdstr4 ../"$FROM_DIR"/prep/tmp3 > ../"$FROM_DIR"/prep/tmp4;
sed $mycmdstr5 ../"$FROM_DIR"/prep/tmp4 > ../"$FROM_DIR"/prep/tmp5;
sed $mycmdstr6 ../"$FROM_DIR"/prep/tmp5 > ../"$FROM_DIR"/prep/tmp6;
sed $mycmdstr7 ../"$FROM_DIR"/prep/tmp6 > ../"$FROM_DIR"/prep/tmp7;
sed $mycmdstr8 ../"$FROM_DIR"/prep/tmp7 > ../"$FROM_DIR"/prep/tmp8;
sed $mycmdstr9 ../"$FROM_DIR"/prep/tmp8 > ../"$FROM_DIR"/prep/tmp9;
sed $mycmdstr10 ../"$FROM_DIR"/prep/tmp9 > ../"$FROM_DIR"/prep/tmp10;
sed $mycmdstr11 ../"$FROM_DIR"/prep/tmp10 > ../"$FROM_DIR"/prep/tmp11;
sed $mycmdstr12 ../"$FROM_DIR"/prep/tmp11 > ../"$FROM_DIR"/prep/tmp12;
sed $mycmdstr13 ../"$FROM_DIR"/prep/tmp12 > ../"$FROM_DIR"/prep/tmp13;
sed $mycmdstr14 ../"$FROM_DIR"/prep/tmp13 > ../"$FROM_DIR"/prep/tmp14;
sed $mycmdstr15 ../"$FROM_DIR"/prep/tmp14 > ../"$FROM_DIR"/prep/tmp15;
sed $mycmdstr16 ../"$FROM_DIR"/prep/tmp15 > ../"$FROM_DIR"/prep/tmp16;
sed $mycmdstr17 ../"$FROM_DIR"/prep/tmp16 > ../"$FROM_DIR"/prep/tmp17;
sed $mycmdstr18 ../"$FROM_DIR"/prep/tmp17 > ../"$FROM_DIR"/prep/tmp18;
sed $mycmdstr19 ../"$FROM_DIR"/prep/tmp18 > ../"$FROM_DIR"/prep/tmp19;
sed $mycmdstr20 ../"$FROM_DIR"/prep/tmp19 > ../"$FROM_DIR"/prep/tmp20;
sed $mycmdstr21 ../"$FROM_DIR"/prep/tmp20 > ../"$FROM_DIR"/prep/tmp21;
sed $mycmdstr22 ../"$FROM_DIR"/prep/tmp21 > ../"$FROM_DIR"/prep/tmp22;
sed $mycmdstr23 ../"$FROM_DIR"/prep/tmp22 > ../"$FROM_DIR"/prep/tmp23;
sed $mycmdstr24 ../"$FROM_DIR"/prep/tmp23 > ../"$FROM_DIR"/prep/tmp24;
sed $mycmdstr25 ../"$FROM_DIR"/prep/tmp24 > ../"$FROM_DIR"/prep/tmp25;
sed $mycmdstr26 ../"$FROM_DIR"/prep/tmp25 > ../"$FROM_DIR"/prep/tmp26;
sed $mycmdstr27 ../"$FROM_DIR"/prep/tmp26 > ../"$FROM_DIR"/prep/tmp27;
sed $mycmdstr28 ../"$FROM_DIR"/prep/tmp27 > ../"$FROM_DIR"/prep/tmp28;
sed $mycmdstr29 ../"$FROM_DIR"/prep/tmp28 > ../"$FROM_DIR"/prep/tmp29;
sed $mycmdstr30 ../"$FROM_DIR"/prep/tmp29 > ../"$FROM_DIR"/prep/tmp30;
sed $mycmdstr31 ../"$FROM_DIR"/prep/tmp30 > ../"$FROM_DIR"/prep/tmp31;
sed $mycmdstr32 ../"$FROM_DIR"/prep/tmp31 > ../"$FROM_DIR"/prep/tmp32;
sed $mycmdstr33 ../"$FROM_DIR"/prep/tmp32 > ../"$FROM_DIR"/prep/tmp33;
sed $mycmdstr34 ../"$FROM_DIR"/prep/tmp33 > ../"$FROM_DIR"/prep/tmp34;
# last tmp file is referenced below several times

# Remove DB2RLIB contextualy, if variable not set
if [[ $GERS_INCLUDE_DB2_RUNLIB == "N" ]]; then
  echo "Processing file: $MEMBER to remove references to DB2RLIB";
  ./editRLIB.sh ../"$FROM_DIR"/prep/tmp34 "$FROM_DIR";
    exitIfError;
  # echo "File $MEMBER copied from .tmp back to original name with DB2RLIB removed if it was present";
  cp -f ../"$FROM_DIR"/prep/tmp34.tmp ../"$FROM_DIR"/prep/tmp34;
fi

#convert output back to EBCDIC again
iconv -f ISO8859-1 -t IBM-1047 ../"$FROM_DIR"/prep/tmp34 > ../"$FROM_DIR"/prep/$MEMBER;
chtag -r ../"$FROM_DIR"/prep/$MEMBER;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"