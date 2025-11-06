# JCL and parameters to run RCA

This is intended executing GeneversERS RCA on ZOS. Run Control Application can take input either from the DB2 schema defined for GenevaERS or from XML files exported via Workbench. For example JCL see GenevaERS Workbench repository and file GVBDEME.JCL in directory database/db2.

## Job steps involved
<pre>
1) delete output files from last run
2) generate the output run control file
</pre>

## Assign symbolic variables in JCL
<pre>
//   EXPORT SYMLIST=*                                                  
//   SET PDSHLQ=GEBT                                                   
//   SET PDSMLQ=RTC23321                                               
//   SET DB2SYS=DM13                                                   
//   SET DEMOHLQ=GEBT                                                  
//   SET DEMOMLQ=GVBDEMO                                               
//   SET LOADLIB=GEBT.NEILE.GVBLOAD                                    
//   SET JZOSLIB=AJV.V11R0M0.SIEALNKE                                  
//   SET DB2SCH=SAFRNEIL               
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
export JAVA_HOME=&$JAVAHOME.
export IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"

export APP_HOME=&$RCAJAR.
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

![Alt text](Image/Import_to_empty_environment.jpg)

### Importing to a DB2 schema which contains existing Workbench views

You cannot import an XMLformat view folder into an environment containing existing Workbench objects. Therefore a new *environment* must be created for the import to work. Under Administration select New Environment. In order to avoid a clash of Control Records it is recommended you un-tick the box that says **Generate a Control Record** before proceeding. See the screenshot below.

![Alt text](Image/NEW_environment.jpg)

### Avoiding a clash in the control record

If you encounter the following problem, see screen shot, it means there is a conflicting control record already in the empty environment you are trying to use for the import. Therefore you must first create an empty environment using the option to not create a control record.

![Alt text](Image/Control_Record_clash.jpg)

### Note on stored procedures - job INSTSP

Stored Procedures are used by the GENEVA Workbench to access
related metadata in the DB2 database. These native Stored
Procedures so must use DB2 Z/OS Version 11 or above.

Native stored procedures are created directly in DB2.

