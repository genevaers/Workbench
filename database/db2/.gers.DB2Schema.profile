
##variables chosen by user for setting up a basic DB2 Schema for GenevaERS

export GERS_DB2_DBUSER=SAFRBLD;
export GERS_DB2_DBNAME=SADBNEIL;
export GERS_DB2_STGGRP=SASGNEIL;
export GERS_DB2_DBSCH=SAFRNEIL;
export GERS_DB2_SUBSYSTEM=DM13;
export GERS_DB2_PLAN=DSNTEP13;
export GERS_DB2_PROCLIB=DSN.V13R1M0.PROCLIB;
#                                                       
# choose if you want DB2 RUNLIB explicitly in JCL or not
#                                                       
export GERS_DB2_RUN_LIB=DSN131.RUNLIB.LOAD;             
# export GERS_DB2_RUN_LIB="";                           
#                                                       
export GERS_DB2_LOAD_LIB=DSN.V13R1M0.SDSNLOAD;
export GERS_DB2_EXIT_LIB=DSN.V13R1M0.SDSNEXIT;
#
export GERS_JOB_CLASS=A;
export GERS_JOB_MSG_CLASS=H;
export GERS_LE_RUN_LIB=CEE.SCEERUN;
export GERS_LE_RUN_LIB2=CEE.SCEERUN2;
export GERS_SCBCDLL=CBC.SCLBDLL;
export GERS_TO_PDS_HLQ=GEBT;
export GERS_TO_PDS_MLQ=RTC23321;

## Optional variables when replicating GenevaERS metadata from another DB2 schema

# export GERS_FROM_PDS_HLQ=GEBT;
# export GERS_FROM_PDS_MLQ=RTC23321.FROM;
# export GERS_FROM_DB2_DBUSER=SAFRBLD;
# export GERS_FROM_DB2_DBNAME=SADBNCB2;
# export GERS_FROM_DB2_DBSG=SASGNCB2;
# export GERS_FROM_DB2_DBSCH=SAFRNCB2;
# export GERS_FROM_DB2_DBSUB=DM13;

## Optional variables for running GVBDEMO smoke test against DB2

# export GERS_ENV_HLQ='GEBT.NEILE';
# export GERS_DEMO_HLQ=GEBT;
# export GERS_DEMO_MLQ=GVBDEMO;
# export GERS_JVM_PROC_LIB='AJV.V11R0M0.PROCLIB';
# export GERS_JZOS_LOAD_LIB='AJV.V11R0M0.SIEALNKE';
# export GERS_DB2_HOST='sp13.pok.stglabs.ibm.com';
# export GERS_DB2_PORT='5036';
# export GERS_DB2_SAFR_ENV='1';
# export GERS_JAVA_HOME="/Java/J17.0_64"
# export GERS_RCA_JAR_DIR="/u/nbeesle/git/public/RCA_jar"
