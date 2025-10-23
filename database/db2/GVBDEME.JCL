//GVBDEME  JOB (ACCT),'GENEVAERS DEMO PASS1',
//          NOTIFY=&SYSUID.,
//          REGION=0M,
//          CLASS=&$JBCLASS.,
//          MSGLEVEL=(1,1),
//          MSGCLASS=&$MSGCLASS.
//*
//********************************************************************
//*
//* (C) COPYRIGHT IBM CORPORATION 2025.
//*    Copyright Contributors to the GenevaERS Project.
//*SPDX-License-Identifier: Apache-2.0
//*
//********************************************************************
//*
//*  Licensed under the Apache License, Version 2.0 (the "License");
//*  you may not use this file except in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*     http://www.apache.org/licenses/LICENSE-2.0
//*
//*  Unless required by applicable law or agreed to in writing, software
//*  distributed under the License is distributed on an "AS IS" BASIS,
//*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
//*  or implied.
//*  See the License for the specific language governing permissions
//*  and limitations under the License.
//*
//********************************************************************
//* Run Performance Engine Extract Phase
//********************************************************************
//*
//   EXPORT SYMLIST=*
//*
//   SET HLQ=&$PDSHLQ.
//   SET MLQ=&$PDSMLQ.
//   SET DB2SYS=&$DBSUB.
//   SET DB2PLAN=&$DB2PLAN.
//   SET DB2PLIB=&$DB2PLIB.
//   SET DB2RLIB=&$DB2RLIB.
//   SET HLQ=GEBT
//   SET MLQ=GVBDEMO
//   SET LOADLIB=GEBT.GVBDEMO.GVBLOAD
//   SET JCLLIB=GEBT.RTC23321.STAGE2.JCL
//*
//   JCLLIB ORDER=AJV.V11R0M0.PROCLIB
//*
//*********************************************************************
//*
//*     GVBDEME  - EXECUTE CONTROL FILE CREATION, REFERENCE AND EXTRACT
//*                PHASES.
//*
//* BEFORE SUBMITTING THIS JOB, PLEASE:
//*
//*     1)  UPDATE THE JOB STATEMENT ABOVE TO CONFORM TO YOUR
//*         INSTALLATION'S STANDARDS.
//*
//*     2)  SET THE VALUE OF "HLQ" ABOVE TO YOUR TSO PREFIX.
//*         THIS IS NORMALLY THE SAME AS YOUR TSO ID,
//*         UNLESS YOU HAVE CHANGED IT WITH THE TSO PROFILE PREFIX
//*         COMMAND.
//*
//*         THIS VALUE WILL DETERMINE THE HIGH-LEVEL QUALIFIER
//*         OF THE NAMES OF THE DEMO DATA SETS.
//*
//*     3)  THE "MLQ" DEFAULT VALUE IS GVBDEMO AND DOES NOT NEED TO BE
//*         CHANGED.
//*
//*********************************************************************
//* DELETE THE FILE(S) CREATED IN NEXT STEP
//*********************************************************************
//*
//PSTEP200 EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *,SYMBOLS=EXECSYS

 DELETE  &HLQ..&MLQ..PASS1C1.VDP PURGE
 DELETE  &HLQ..&MLQ..PASS1C1.JLT PURGE
 DELETE  &HLQ..&MLQ..PASS1C1.XLT PURGE

 IF MAXCC LE 8 THEN         /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

//*
//*********************************************************************
//* CREATE THE RUN CONTROL FILES
//*********************************************************************
//* LICENSED MATERIALS - PROPERTY OF IBM
//* 5655-DGJ
//* COPYRIGHT IBM CORP. 1997, 2021
//* STATUS = HJVBB00
//*
//* BATCH JOB TO RUN THE JAVA VM
//*
//* TAILOR THE PROC AND JOB FOR YOUR INSTALLATION:
//* 1.) MODIFY THE JOB CARD PER YOUR INSTALLATION'S REQUIREMENTS
//* 2.) MODIFY THE PROCLIB CARD TO POINT TO THIS PDS
//* 3.) EDIT JAVA_HOME TO POINT THE LOCATION OF THE SDK
//* 4.) EDIT APP_HOME TO POINT THE LOCATION OF YOUR APP (IF ANY)
//* 5.) MODIFY THE CLASSPATH AS REQUIRED TO POINT TO YOUR JAVA CODE
//* 6.) MODIFY JAVACLS AND ARGS TO LAUNCH DESIRED JAVA CLASS
//*
//*******************************************************************
//*
//JAVA EXEC PROC=JVMPRC16,
// JAVACLS='org.genevaers.rcapps.Runner'
//STEPLIB  DD DISP=SHR,DSN=AJV.V11R0M0.SIEALNKE
//STDENV DD *
. /etc/profile
export A2E=-ofrom=ISO8859-1,to=IBM-1047
export JAVA_HOME=/Java/J17.0_64
export IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"

export APP_HOME=/u/nbeesle/git/public/RCA_jar
export APP_NAME=rcapps-latest.jar
export CLASSPATH=$APP_HOME:"$JAVA_HOME"/lib

LIBPATH=/lib:/usr/lib:"$JAVA_HOME"/bin
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib
LIBPATH="$LIBPATH":"$JAVA_HOME"/lib/j9vm
export LIBPATH="$LIBPATH":

// DD DISP=SHR,DSN=NBEESLE.JINFO
// DD *
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
/*
//*        INPUTS
//*
//RCAPARM  DD *
# Input
INPUT_TYPE=DB2
ENVIRONMENT_ID=1
DB_SCHEMA=SAFRNEIL
DB_PORT=5036
DB_SERVER=sp13.pok.stglabs.ibm.com
DB_DATABASE=DM13
LOG_LEVEL=FINEST
COMPARE=N
GENERATE_RC_FILES=Y
REPORT_FORMAT=TXT
VDP_REPORT=Y
JLT_REPORT=Y
XLT_REPORT=Y
NUMBER_MODE=STANDARD
/*
//DBVIEWS DD *
10700
10689
10702
10714
10715
/*
//*        OUTPUT FILES
//*
//VDPNEW   DD DSN=&HLQ..&MLQ..PASS1C1.VDP,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192,BLKSIZE=0)
//*                                                                     
//JLTNEW   DD DSN=&HLQ..&MLQ..PASS1C1.JLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//*
//XLTNEW   DD DSN=&HLQ..&MLQ..PASS1C1.XLT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(CYL,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4004,BLKSIZE=32036)
//RCARPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//RCALOG  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//*
//VDPRPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//XLTRPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//JLTRPT  DD SYSOUT=*,DCB=(RECFM=VB,LRECL=255)
//*
//*********************************************************************
//* IF THE PRIOR STEP SETS AN INVALID RETURN CODE, TERMINATE THE JOB
//*********************************************************************
//*
//PSTEP206 EXEC PGM=GVBUT99,                                           
//            COND=(0,GE),                                             
//            PARM='1099'                                              
//*           COND=(4,GE,JVMPRC16),                                    
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB                                    
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//*********************************************************************
//* DELETE THE FILE(S) CREATED IN NEXT STEP                            
//*********************************************************************
//*                                                                    
//PSTEP500 EXEC PGM=IDCAMS                                             
//*                                                                    
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
                                                                       
 DELETE  &HLQ..&MLQ..PASS1D1.FILE001.RED PURGE                         
 DELETE  &HLQ..&MLQ..PASS1D1.FILE002.RED PURGE                         
 DELETE  &HLQ..&MLQ..PASS1D1.FILE003.RED PURGE                         
 DELETE  &HLQ..&MLQ..PASS1D1.FILE004.RED PURGE                         
 DELETE  &HLQ..&MLQ..PASS1D1.FILE005.RED PURGE                         
 DELETE  &HLQ..&MLQ..PASS1D1.FILE006.RED PURGE                         
 DELETE  &HLQ..&MLQ..PASS1D1.REH PURGE                                 
 DELETE  &HLQ..&MLQ..PASS1D1.RTH PURGE                                 
 DELETE  &HLQ..&MLQ..PASS1D1.SYSMDUMP PURGE                            
                                                                       
 IF LASTCC > 0 THEN        /* IF OPERATION FAILED,     */    -         
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */             
                                                                       
//*                                                                    
//*********************************************************************
//*   PREPARE REFERENCE DATA                                           
//*********************************************************************
//*                                                                    
//PSTEP505 EXEC PGM=GVBMR95R,                                          
// REGION=0M                                                           
//*                                                                    
//*        <<< INPUT GENEVAERS FILES >>>                               
//*                                                                    
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB                                    
//REFRPARM DD *                                                        
*                                                                      
*   STANDARD OPTIONS                                                   
*-------------------                                                   
*                                                                      
*DISK_THREAD_LIMIT=10                   DEFAULT: 9999                  
*TAPE_THREAD_LIMIT=10                   DEFAULT: 9999                  
*                                                                      
*IO_BUFFER_LEVEL=8                      DEFAULT: 4                     
*OPTIMIZE_PACKED_OUTPUT=N               DEFAULT: Y                 
*PAGE_FIX_IO_BUFFERS=N                  DEFAULT: Y                 
*TREAT_MISSING_VIEW_OUTPUTS_AS_DUMMY=Y  DEFAULT: N                 
*ABEND_ON_CALCULATION_OVERFLOW=N        DEFAULT: Y                 
*                                                                  
*   DEBUGGING OPTIONS                                              
*--------------------                                              
*                                                                  
*TRACE=Y                                DEFAULT: N                 
*DUMP_LT_AND_GENERATED_CODE=Y           DEFAULT: N                 
*SOURCE_RECORD_LIMIT=100                DEFAULT: (NO LIMIT)        
*ABEND_ON_LOGIC_TABLE_ROW_NBR=57        DEFAULT: (NO ABEND)        
*ABEND_ON_MESSAGE_NBR=149               DEFAULT: (NO ABEND)        
*EXECUTE_IN_MAIN_TASK=1                 DEFAULT: N                 
*                                           1=1ST UNIT, A=ALL UNITS
//*                                                                
//REFRTPRM DD *                                                    
*TRACE KEYWORDS:                                                   
*VIEW=,FROMREC=,THRUREC=,FROMLTROW=,THRULTROW=,LTFUNC=,DDNAME=     
*VPOS=,VLEN=,VALUE=                                                
*                                                                  
*TRACE EXAMPLES:                                                   
*VIEW=4418,LTFUNC=WR                                               
*VPOS=17,VLEN=10,VALUE=24CXA01501,VIEW=3424                        
*VIEW=302                                                          
//*                                                                
//REFRENVV DD *                                                    
//*                                                                
//MR95VDP  DD DSN=&HLQ..&MLQ..PASS1C1.VDP,                         
//            DISP=SHR                                             
//*                                                                
//REFRLTBL DD DSN=&HLQ..&MLQ..PASS1C1.JLT,                         
//            DISP=SHR                                             
//*                                                                
//*        <<< INPUT REFERENCE FILES >>>                           
//*                                                                
//CUSTNAME DD DISP=SHR,DSN=&HLQ..&MLQ..CUSTNAME                    
//CUSTOMER DD DISP=SHR,DSN=&HLQ..&MLQ..CUSTOMER                    
//CUSTADDR DD DISP=SHR,DSN=&HLQ..&MLQ..CUSTADDR                    
//ORDER001 DD DISP=SHR,DSN=&HLQ..&MLQ..ORDER                       
//PRODDESC DD DISP=SHR,DSN=&HLQ..&MLQ..PRODDESC                    
//STORE001 DD DISP=SHR,DSN=&HLQ..&MLQ..STOREADR                    
//*                                                                
//*        <<< OUTPUT GENEVAERS REFERENCE WORK FILES >>>           
//*                                                                
//REFRREH  DD DSN=&HLQ..&MLQ..PASS1D1.REH,                         
//            DISP=(NEW,CATLG,DELETE),                             
//            UNIT=SYSDA,                                          
//            SPACE=(TRK,(10,1),RLSE),                             
//            DCB=(DSORG=PS,RECFM=FB,LRECL=100)                    
//*                                                                
//REFRRTH  DD DSN=&HLQ..&MLQ..PASS1D1.RTH,                         
//            DISP=(NEW,CATLG,DELETE),                             
//            UNIT=SYSDA,                                          
//            SPACE=(TRK,(10,1),RLSE),                             
//            DCB=(DSORG=PS,RECFM=FB,LRECL=100)                    
//*                                                                
//REFR001  DD DSN=&HLQ..&MLQ..PASS1D1.FILE001.RED,                     
//            DISP=(NEW,CATLG,DELETE),                                 
//            UNIT=(SYSDA,10),                                         
//            SPACE=(TRK,(1,1000),RLSE),                               
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)                       
//*                                                                    
//REFR002  DD DSN=&HLQ..&MLQ..PASS1D1.FILE002.RED,                     
//            DISP=(NEW,CATLG,DELETE),                                 
//            UNIT=(SYSDA,10),                                         
//            SPACE=(TRK,(1,1000),RLSE),                               
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)                       
//*                                                                    
//REFR003  DD DSN=&HLQ..&MLQ..PASS1D1.FILE003.RED,                     
//            DISP=(NEW,CATLG,DELETE),                                 
//            UNIT=(SYSDA,10),                                         
//            SPACE=(TRK,(1,1000),RLSE),                               
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)                       
//*                                                                    
//REFR004  DD DSN=&HLQ..&MLQ..PASS1D1.FILE004.RED,                     
//            DISP=(NEW,CATLG,DELETE),                                 
//            UNIT=(SYSDA,10),                                         
//            SPACE=(TRK,(1,1000),RLSE),                               
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)                       
//*                                                                    
//REFR005  DD DSN=&HLQ..&MLQ..PASS1D1.FILE005.RED,                     
//            DISP=(NEW,CATLG,DELETE),                                 
//            UNIT=(SYSDA,10),                                         
//            SPACE=(TRK,(1,1000),RLSE),                               
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)                       
//*                                                                    
//REFR006  DD DSN=&HLQ..&MLQ..PASS1D1.FILE006.RED,                     
//            DISP=(NEW,CATLG,DELETE),                                 
//            UNIT=(SYSDA,10),                                         
//            SPACE=(TRK,(1,1000),RLSE),                               
//            DCB=(DSORG=PS,RECFM=VB,LRECL=4144)                       
//*                                                                    
//REFRRPT  DD SYSOUT=*                                                 
//REFRLOG  DD SYSOUT=*                                                 
//REFRTRAC DD SYSOUT=*                                                 
//SNAPDATA DD SYSOUT=*                                                 
//SYSOUT   DD SYSOUT=*                                                 
//*                                                                    
//SYSMDUMP DD DSN=&HLQ..&MLQ..PASS1D1.SYSMDUMP,                        
//            DISP=(NEW,DELETE,CATLG),                                 
//            UNIT=SYSDA,                                              
//            SPACE=(TRK,(1000,1000),RLSE),                            
//            DCB=(DSORG=PS,RECFM=FBS,LRECL=4160)                      
//*                                                                    
//*********************************************************************
//* IF THE PRIOR STEP SETS AN INVALID RETURN CODE, TERMINATE THE JOB   
//*********************************************************************
//*                                                                    
//PSTEP506 EXEC PGM=GVBUT99,                                           
//            COND=(1,GE,PSTEP505),                                    
//            PARM='1099'                                              
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB                                    
//*                                                                    
//*********************************************************************
//* DELETE THE FILE(S) CREATED IN NEXT STEP                            
//*********************************************************************
//*                                                                    
//PSTEP700 EXEC PGM=IDCAMS                                             
//*                                                                    
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
 /* VIEW DATA SETS: */                                                 
                                                                       
 DELETE  &HLQ..&MLQ..PASS1E1.OUTPUT01 PURGE                            
 DELETE  &HLQ..&MLQ..PASS1E1.OUTPUT02 PURGE                            
 DELETE  &HLQ..&MLQ..PASS1E1.OUTPUT03 PURGE                            
                                                                       
                                                                       
 /* DATA SETS  GOING TO FORMAT PHASE */                                
                                                                       
 DELETE  &HLQ..&MLQ..PASS1E1.FILE001.EXT PURGE                         
 DELETE  &HLQ..&MLQ..PASS1E1.FILE001.SXT PURGE                         
 DELETE  &HLQ..&MLQ..PASS1E1.FILE002.EXT PURGE                         
 DELETE  &HLQ..&MLQ..PASS1E1.FILE002.SXT PURGE                         
 DELETE  &HLQ..&MLQ..PASS1E1.FILE003.EXT PURGE                         
 DELETE  &HLQ..&MLQ..PASS1E1.FILE003.SXT PURGE                         
 DELETE  &HLQ..&MLQ..PASS1E1.SYSMDUMP PURGE                            
                                                                       
 IF LASTCC > 0 THEN        /* IF OPERATION FAILED,     */    -         
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */             
                                                                       
//*                                                                    
//*********************************************************************
//* EXTRACT DATA FOR VIEWS                                             
//*********************************************************************
//*                                                                    
//PSTEP705 EXEC PGM=GVBMR95E,                                          
// REGION=0M                                                           
//*                                                                    
//*        <<< INPUT GENEVAERS FILES >>>                               
//*                                                                    
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB                                    
//EXTRPARM DD *                                                        
*                                                                      
*   STANDARD OPTIONS                                                   
*-------------------                                                   
*                                                                      
*RUN_DATE=20170105                      DEFAULT: (CURRENT DATE)        
*FISCAL_DATE_DEFAULT=20161231           DEFAULT: RUN_DATE              
*FISCAL_DATE_OVERRIDE=1:20160731        DEFAULT: FISCAL_DATE_DEFAULT   
*                                                                      
*DISK_THREAD_LIMIT=2                    DEFAULT: 9999                  
*TAPE_THREAD_LIMIT=10                   DEFAULT: 9999                  
*                                                                      
*IO_BUFFER_LEVEL=8                      DEFAULT: 4                     
*OPTIMIZE_PACKED_OUTPUT=N               DEFAULT: Y                     
*PAGE_FIX_IO_BUFFERS=N                  DEFAULT: Y                     
*TREAT_MISSING_VIEW_OUTPUTS_AS_DUMMY=Y  DEFAULT: N                     
*ABEND_ON_CALCULATION_OVERFLOW=N        DEFAULT: Y                 
*                                                                  
*   DEBUGGING OPTIONS                                              
*--------------------                                              
*                                                                  
*TRACE=Y                                DEFAULT: N                 
*DUMP_LT_AND_GENERATED_CODE=Y           DEFAULT: N                 
*SOURCE_RECORD_LIMIT=100                DEFAULT: (NO LIMIT)        
*ABEND_ON_LOGIC_TABLE_ROW_NBR=57        DEFAULT: (NO ABEND)        
*ABEND_ON_MESSAGE_NBR=149               DEFAULT: (NO ABEND)        
*EXECUTE_IN_PARENT_THREAD=A             DEFAULT: N                 
*                                           1=1ST UNIT, A=ALL UNITS
//*                                                                
//EXTRTPRM DD *                                                    
*TRACE KEYWORDS:                                                   
*VIEW=,FROMREC=,THRUREC=,FROMLTROW=,THRULTROW=,LTFUNC=,DDNAME=     
*VPOS=,VLEN=,VALUE=                                                
*                                                                  
*TRACE EXAMPLES:                                                   
*VIEW=4418,LTFUNC=WR                                               
*VPOS=17,VLEN=10,VALUE=24CXA01501,VIEW=3424                        
*VIEW=302                                                          
//*                                                                
//EXTRENVV DD *                                                    
$SUBSYS=DM12                                                       
$QUAL=SDATRT01                                                     
//*                                                                
//MR95VDP  DD DSN=&HLQ..&MLQ..PASS1C1.VDP,                         
//            DISP=SHR                                             
//*                                                                
//EXTRLTBL DD DSN=&HLQ..&MLQ..PASS1C1.XLT,                         
//            DISP=SHR                                             
//*                                                                
//EXTRREH  DD DSN=&HLQ..&MLQ..PASS1D1.REH,                         
//            DISP=SHR                                             
//*                                                                
//*        <<< INPUT GENEVAERS REFERENCE WORK FILES >>>            
//*                                                                
//REFR001  DD DISP=SHR,DSN=&HLQ..&MLQ..PASS1D1.FILE001.RED         
//REFR002  DD DISP=SHR,DSN=&HLQ..&MLQ..PASS1D1.FILE002.RED         
//REFR003  DD DISP=SHR,DSN=&HLQ..&MLQ..PASS1D1.FILE003.RED         
//REFR004  DD DISP=SHR,DSN=&HLQ..&MLQ..PASS1D1.FILE004.RED         
//REFR005  DD DISP=SHR,DSN=&HLQ..&MLQ..PASS1D1.FILE005.RED         
//REFR006  DD DISP=SHR,DSN=&HLQ..&MLQ..PASS1D1.FILE006.RED         
//*                                                                
//*        <<< INPUT SOURCE FILES >>>                              
//*                                                                
//ORDIT001 DD DISP=SHR,DSN=&HLQ..&MLQ..ORDITM01                    
//ORDIT002 DD DISP=SHR,DSN=&HLQ..&MLQ..ORDITM02                    
//ORDIT003 DD DISP=SHR,DSN=&HLQ..&MLQ..ORDITM03                    
//*                                                                
//*        <<< OUTPUT FILES GOING TO FORMAT PHASE >>>              
//*                                                                
//EXTR001  DD DSN=&HLQ..&MLQ..PASS1E1.FILE001.EXT,                 
//            DISP=(NEW,CATLG,DELETE),                             
//            UNIT=(SYSDA,10),                                     
//            SPACE=(TRK,(1,1000),RLSE),                           
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192)      
//*                                                   
//SORT001  DD DSN=&HLQ..&MLQ..PASS1E1.FILE001.SXT,    
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=SYSDA,                             
//            SPACE=(TRK,(1,1),RLSE),                 
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)        
//*                                                   
//EXTR002  DD DSN=&HLQ..&MLQ..PASS1E1.FILE002.EXT,    
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=(SYSDA,10),                        
//            SPACE=(TRK,(1,1000),RLSE),              
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192)      
//*                                                   
//SORT002  DD DSN=&HLQ..&MLQ..PASS1E1.FILE002.SXT,    
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=SYSDA,                             
//            SPACE=(TRK,(1,1),RLSE),                 
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)        
//*                                                   
//EXTR003  DD DSN=&HLQ..&MLQ..PASS1E1.FILE003.EXT,    
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=(SYSDA,10),                        
//            SPACE=(TRK,(1,1000),RLSE),              
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192)      
//*                                                   
//SORT003  DD DSN=&HLQ..&MLQ..PASS1E1.FILE003.SXT,    
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=SYSDA,                             
//            SPACE=(TRK,(1,1),RLSE),                 
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)        
//*                                                   
//*        <<< OUTPUT FILES FOR EXTRACT ONLY VIEWS >>>
//*                                                   
//OUTPUT01 DD DSN=&HLQ..&MLQ..PASS1E1.OUTPUT01,       
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=VT3592,                            
//            SPACE=(TRK,(5000,500),RLSE),            
//            DCB=(DSORG=PS,RECFM=VB,LRECL=1004)      
//*                                                   
//OUTPUT02 DD DSN=&HLQ..&MLQ..PASS1E1.OUTPUT02,       
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=VT3592,                            
//            SPACE=(TRK,(5000,500),RLSE),            
//            DCB=(DSORG=PS,RECFM=VB,LRECL=1004)      
//*                                                   
//OUTPUT03 DD DSN=&HLQ..&MLQ..PASS1E1.OUTPUT03,       
//            DISP=(NEW,CATLG,DELETE),                
//            UNIT=VT3592,                            
//            SPACE=(TRK,(5000,500),RLSE),            
//            DCB=(DSORG=PS,RECFM=VB,LRECL=1004)      
//*                                                   
//EXTRRPT  DD SYSOUT=*                                
//EXTRLOG  DD SYSOUT=*                                
//EXTRTRAC DD SYSOUT=*                                
//MERGRPT  DD SYSOUT=*                                
//SNAPDATA DD SYSOUT=*                                
//SYSOUT   DD SYSOUT=*                                               
//CEEDUMP  DD SYSOUT=*                                               
//*                                                                  
//IDIOFF   DD DUMMY                                                  
//*                                                                  
//SYSMDUMP DD DSN=&HLQ..&MLQ..PASS1E1.SYSMDUMP,                      
//            DISP=(NEW,DELETE,CATLG),                               
//            UNIT=SYSDA,                                            
//            SPACE=(TRK,(1000,1000),RLSE),                          
//            DCB=(DSORG=PS,RECFM=FBS,LRECL=4160)                    
//*                                                                  
//*******************************************************************
//* IF THE PRIOR STEP SETS AN INVALID RETURN CODE, TERMINATE THE JOB 
//*******************************************************************
//*                                                                  
//PSTEP706 EXEC PGM=GVBUT99,                                         
//            COND=(1,GE,PSTEP705),                                  
//            PARM='1099'                                            
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB                                  
//*                                                                  
//*******************************************************************
//* JSTEPNX1 - SUBMIT NEXT JOB: FORMAT JOB 1                         
//*******************************************************************
//*                                                                  
//JSTEPNX1 EXEC PGM=IEBGENER                                         
//*                                                                  
//SYSIN    DD DUMMY                                                  
//*                                                                  
//SYSUT1   DD DSN=&JCLLIB.(GVBDEMF1),                                
//            DISP=SHR                                               
//*                                                                  
//SYSUT2   DD SYSOUT=(*,INTRDR)                                      
//*                                                                  
//SYSPRINT DD SYSOUT=*                                               
//*                                                                  
//*******************************************************************
//* JSTEPNX2 - SUBMIT NEXT JOB: FORMAT JOB 2                         
//*******************************************************************
//*                                                                  
//JSTEPNX2 EXEC PGM=IEBGENER                                         
//*                                                                  
//SYSIN    DD DUMMY                                                  
//*                                                                  
//SYSUT1   DD DSN=&JCLLIB.(GVBDEMF2),                                
//            DISP=SHR                                               
//*                                                                  
//SYSUT2   DD SYSOUT=(*,INTRDR)                                      
//*                                                                  
//SYSPRINT DD SYSOUT=*                                               
//*                                                                  
//*******************************************************************
//* JSTEPNX3 - SUBMIT NEXT JOB: FORMAT JOB 3                         
//*******************************************************************
//*                                                                  
//JSTEPNX3 EXEC PGM=IEBGENER                                         
//*                                                                  
//SYSIN    DD DUMMY                                                  
//*                                  
//SYSUT1   DD DSN=&JCLLIB.(GVBDEMF3),
//            DISP=SHR               
//*                                  
//SYSUT2   DD SYSOUT=(*,INTRDR)      
//*                                  
//SYSPRINT DD SYSOUT=*               
//*                                  
//                                                                       