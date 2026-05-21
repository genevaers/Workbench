//DSNMOD   JOB (ACCT),'JAVA DSNMOD',
//          NOTIFY=&SYSUID.,
//          REGION=0M,
//          CLASS=&$JBCLASS.,
//          MSGLEVEL=(1,1),
//          MSGCLASS=&$MSGCLASS.
//*
//   EXPORT SYMLIST=*
//*
//   SET UNLDHLQ=&$UNLDHLQ.
//   SET UNLDMLQ=&$UNLDMLQ.
//   SET LOADHLQ=&$LOADHLQ.
//   SET LOADMLQ=&$LOADMLQ.
//   SET JZOSLIB=&$JZOSLIB.
//   SET &DB2SCH=&$DB2SCH.
//* 
//         JCLLIB ORDER=AJV.V21R0M0.PROCLIB
//*
//JOBLIB   DD DISP=SHR,DSN=AJV.V21R0M0.SIEALNKE
//*
//*********************************************************************
//* This job searches through DATA files that reference the PDS LOB
//* files from the unloaded GenevaERS Schema and updates the files
//* referenced to the new file names (local file names).
//* See parameters for specification of "old" and "new" file names.
//*********************************************************************
//*   DELETE DATA SETS
//*********************************************************************
//*
//DELETE    EXEC PGM=IDCAMS
//*
//SYSPRINT  DD SYSOUT=*,DCB=(LRECL=133,BLKSIZE=12901,RECFM=FBA)
//*
//SYSIN     DD *,SYMBOLS=EXECSYS
  DELETE  &LOADHLQ..&LOADMLQ..LOGIC.DATA2    PURGE
  DELETE  &LOADHLQ..&LOADMLQ..VIEWCOL.DATA2  PURGE
  DELETE  &LOADHLQ..&LOADMLQ..VIEWSRC.DATA2  PURGE
  DELETE  &LOADHLQ..&LOADMLQ..VIEWTS.DATA2   PURGE
  DELETE  &LOADHLQ..&LOADMLQ..VWCOLSRC.DATA2 PURGE
  IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
      SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

//*******************************************************************
//*
//*******************************************************************
//* Licensed Materials - Property of IBM
//* 5655-DGJ
//* Copyright IBM Corp. 1997, 2021
//* STATUS = HJVBB00
//*
//* Batch job to run the Java VM
//*
//* Tailor the proc and job for your installation:
//* 1.) Modify the Job card per your installation's requirements
//* 2.) Modify the PROCLIB card to point to this PDS
//* 3.) edit JAVA_HOME to point the location of the SDK
//* 4.) edit APP_HOME to point the location of your app (if any)
//* 5.) Modify the CLASSPATH as required to point to your Java code
//* 6.) Modify JAVACLS and ARGS to launch desired Java class
//*
//*******************************************************************
//JAVA EXEC PROC=JVMPRC21,
// JAVACLS='DSNMOD'
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB
//STDENV DD *,SYMBOLS=EXECSYS
. /etc/profile
export A2E=-ofrom=ISO8859-1,to=IBM-1047
export JAVA_HOME=&$JAVAHOME.
export IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"

export APP_HOME=&$RCAJAR.
export APP_NAME=dsnmod-latest.jar
export CLASSPATH=$APP_HOME:"$JAVA_HOME"/lib

LIBPATH=/lib:/usr/lib:"$JAVA_HOME"/bin
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib/j9vm
export LIBPATH="$LIBPATH":
# Customize your CLASSPATH here
# Add Application required jars to end of CLASSPATH
CLASSPATH="$CLASSPATH":"$APP_HOME"/"$APP_NAME"
echo $CLASSPATH
export CLASSPATH="$CLASSPATH":

# Set JZOS specific options
# Use this variable to specify encoding for DD STDOUT and STDERR
#export JZOS_OUTPUT_ENCODING=Cp1047
# Use this variable to prevent JZOS from handling MVS operator commands
#export JZOS_ENABLE_MVS_COMMANDS=false
# Use this variable to supply additional arguments to main
#export JZOS_MAIN_ARGS=""

# Configure JVM options
IJO="-Xms16m -Xmx128m"
# Uncomment the following to aid in debugging "Class Not Found" problems
#IJO="$IJO -verbose:class"
# Uncomment the following if you want to run with Ascii file encoding..
IJO="$IJO -Dfile.encoding=ISO8859-1"
export IBM_JAVA_OPTIONS="$IJO "

//*
//*        PARAMETERS
//* 
//* These comprise the original unload PDS dataset names on the system
//* where the GenevaERS DB2 Schema contents were unloaded. For example,

//*   C02888.GENERS.D251114U.etc
//* 
//* The integer following the dataset name is the offset within the
//* record of the dataset name. Do not change without consulting
//* SAFR Support.
//*
//* Next is the names of these datasets on the target system where
//* the contents of the datasets will be loaded. For example,
//* 
//*   GEBT.GENERS.D251231V.etc
//*  
//DDPARM   DD *,SYMBOLS=EXECSYS
GENERS
&DB2SCH
&UNLDHLQ..&UNLDMLQ
&LOADHLQ..&LOADMLQ
LOGIC.LOB 57
VIEWCOL.LOB 1839
VIEWSRCF.LOB 141
VIEWSRCO.LOB 447
VIEWTS.LOB 1317
VWCOLSRC.LOB 433
/*                                 
//*        INPUT FILES ARE DYNAMIC 
//*                                
//*        OUTPUT FILES ARE DYNAMIC
//*                                
//                                 