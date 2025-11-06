# JCL and parameters to run RCA

These instructions are intended for executing GeneversERS RCA on ZOS. The Run Control Application can take input either from data contained in a DB2 schema defined for GenevaERS, or from XML files exported via Workbench. For example JCL, see GenevaERS Workbench repository and file GVBDEME.JCL in directory database/db2.

## Job steps involved
<pre>
1) delete output files from last run
2) generate the output run control file -- or compare old and new run control files
</pre>

## Assign symbolic variables in JCL
<pre>
//   EXPORT SYMLIST=*
//   SET PDSHLQ=GENEVA
//   SET PDSMLQ=GVBDEMO
//   SET DB2SYS=my-db2-subsystem
//   SET DEMOHLQ=GENEVA
//   SET DEMOMLQ=GVBDEMO
//   SET LOADLIB=GENEVA.GVBDEMO.GVBLOAD
//   SET JZOSLIB=AJV.V11R0M0.SIEALNKE
//   SET DB2SCH=my-db2-schema
</pre>

## Job step delete output files from last run
IDCAMS is used to remove existing files, if they exist. Notice the return code will be set to zero even if files do not currently exist.
<pre>
//*********************************************************************
//* DELETE THE FILE(S) CREATED IN NEXT STEP
//*********************************************************************
//*
//PSTEP200 EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *,SYMBOLS=EXECSYS

 DELETE  &DEMOHLQ..&DEMOMLQ..PASS1C1.VDP PURGE
 DELETE  &DEMOHLQ..&DEMOMLQ..PASS1C1.JLT PURGE
 DELETE  &DEMOHLQ..&DEMOMLQ..PASS1C1.XLT PURGE

 IF MAXCC LE 8 THEN         /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
</pre>

## Job step to run RCA on ZOS
The following sections describe the different sections of this job step

### Exec card
This specifies the cataloged procedure for running Java in batch and the name of the main Java class being executed
<pre>
//* LICENSED MATERIALS - PROPERTY OF IBM
//* 5655-DGJ
//* COPYRIGHT IBM CORP. 1997, 2021
//* STATUS = HJVBB00
//JAVA EXEC PROC=JVMPRC16,                                              
// JAVACLS='org.genevaers.rcapps.Runner'                                
//STEPLIB  DD DISP=SHR,DSN=&JZOSLIB                                     
</pre>

### STDENV parameters defining application and Java itself
The section of the JCL sets the STDENV parameters, importantly APP_HOME and APP_NAME along with appending the CLASSPATH.Also important is the A2E, JAVA_HOME and IBM_JAVA_OPTIONS for your site. The LIBPATH is adjusted for your Java installation.

Note: Since rcapps-latest.jar is an alias pointing to your latest main Java class for RCA you must define the alisas on the same machine where you run RCA. The alias cannot be copied from a machine with a different file system.
<pre>
//STDENV DD *,SYMBOLS=EXECSYS
. /etc/profile
export A2E=-ofrom=ISO8859-1,to=IBM-1047
export JAVA_HOME=/Java/J17.0_64
export IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"

export APP_HOME=/u/user01/git/public/RCA_jar
export APP_NAME=rcapps-latest.jar
export CLASSPATH=$APP_HOME:"$JAVA_HOME"/lib

LIBPATH=/lib:/usr/lib:"$JAVA_HOME"/bin
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib/j9vm
export LIBPATH="$LIBPATH":
</pre>

### STDENV parameters defining userID and password
In order not to include your userID and password in stream, these are provided to STDENV using a RACF protected file.
<pre>
// DD DISP=SHR,DSN=USER01.PRIVATE
</pre>

### Miscellaneous STDENV parameters
These complete the definition of CLASSPATH, JZOS specific features and JVM options, such as memory size which can be increased from the 128 megabyte maximum limit as required.
<pre>
// DD *,SYMBOLS=EXECSYS
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
</pre>

### RCAPARM parameters
These deal with parameters specific to the RCA application, such as whether the input is read from DB2 or XML files exported from the Workbench. The GenevaraERS environment number is specified here along with the DB2 schema, in the case of reading input from DB2. The server and port are specified for TCP/IP communication through JDBC with DB2. Further DB2 details include the name of the DB2 database.

Generalized parameters follow such as LOG_LEVEL and directions whether RCA is to generate run control files, or to compare two sets of run control files, and the type of reports required.
<pre>
//RCAPARM  DD *,SYMBOLS=EXECSYS
# Input
INPUT_TYPE=DB2
ENVIRONMENT_ID=environment_ID, i.e. 1 for GVBDEMO
DB_SCHEMA=&DB2SCH
DB_PORT=db2-port-number
DB_SERVER=db2-host-ip-address
DB_DATABASE=&DB2SYS
LOG_LEVEL=FINEST
COMPARE=N
GENERATE_RC_FILES=Y
REPORT_FORMAT=TXT
VDP_REPORT=Y
JLT_REPORT=Y
XLT_REPORT=Y
NUMBER_MODE=STANDARD
/*
</pre>

### DBVIEWS parameter
These specifiy the GenevaERS view numbers to be processed. These view numbers are for GVBDEMO
<pre>
//DBVIEWS DD *
10700
10689
10702
10714
10715
/*
</pre>

### Output dataset definition statements
Here the 3 output files are defined for the VDP, XLT and JLT. In the case of a compare there will also be VDPOLD, XLTOLD and JLTOLD data definition statements used for the comparison.
<pre>
//VDPNEW   DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1C1.VDP,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192,BLKSIZE=0)
//*                                                                     
//JLTNEW   DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1C1.JLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//*
//XLTNEW   DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1C1.XLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
</pre>

### Output report datasets
The RCA run report and log datasets are specified here, along with the 3 separate VDP, XLT and JLT output reports.
<pre>
//RCARPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//RCALOG  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//VDPRPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//XLTRPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//JLTRPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
</pre>