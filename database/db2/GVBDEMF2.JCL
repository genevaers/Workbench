//GVBDEMF2 JOB (ACCT),'GENEVAERS DEMO FMT 2',
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
//   SET PDSHLQ=&$PDSHLQ.
//   SET PDSMLQ=&$PDSMLQ.
//   SET DEMOHLQ=&$DEMOHLQ.
//   SET DEMOMLQ=&$DEMOMLQ.
//   SET LOADLIB=&$ENVHLQ..GVBLOAD
//*
//*********************************************************************
//*                                                                    
//*     GVBDEMF2 - EXECUTE FORMAT PHASE SUMMARIZATION JOB 2.           
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

 DELETE  &DEMOHLQ..&DEMOMLQ..PASS1F1.DCOBYSTO PURGE
 DELETE  &DEMOHLQ..&DEMOMLQ..PASS1F1.SYSMDUMP PURGE

 IF LASTCC > 0 THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

//*
//*********************************************************************
//* RUN FORMAT SUMMARIZATION JOB
//*********************************************************************
//*
//PSTEP205 EXEC PGM=GVBMR88,
// REGION=0M
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB
//*
//*        <<< INPUT GENEVAERS FILES >>>
//*
//MR88PARM DD *
*RUN_DATE=20251024                      DEFAULT: (CURRENT DATE)
*FISCAL_DATE_DEFAULT=20251031           DEFAULT: RUN_DATE
*FISCAL_DATE_OVERRIDE=1:20251231        DEFAULT: FISCAL_DATE_DEFAULT
*
*ABEND_ON_CALCULATION_OVERFLOW=N        DEFAULT: Y
*ABEND_ON_DIVISION_BY_ZERO=N            DEFAULT: Y
*PROCESS_HEADER_RECORDS=N               DEFAULT: Y
*SORT_EXTRACT_FILE=N                    DEFAULT: Y
//*
//MR88VDP  DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1C1.VDP,
//            DISP=SHR
//*
//SYSIN    DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1E1.FILE002.SXT,
//            DISP=SHR
//*
//SORTCNTL DD *
 OPTION DYNALLOC=(SYSDA,5),MAINSIZE=32M,RESALL=1M,VLSHRT
//*
//SORTIN   DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1E1.FILE002.EXT,
//            DISP=SHR
//*
//MR88HXE  DD DUMMY
//*
//MR88MSTR DD DUMMY,BLKSIZE=2408
//*
//REFRRTH  DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.RTH,
//            DISP=SHR
//*
//*        <<< INPUT GENEVAERS REFERENCE WORK FILES >>>
//*
//REFR001  DD DISP=SHR,
//            DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.FILE001.RED
//REFR002  DD DISP=SHR,
//            DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.FILE002.RED
//REFR003  DD DISP=SHR,
//            DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.FILE003.RED
//REFR004  DD DISP=SHR,
//            DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.FILE004.RED
//REFR005  DD DISP=SHR,
//            DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.FILE005.RED
//REFR006  DD DISP=SHR,
//            DSN=&DEMOHLQ..&DEMOMLQ..PASS1D1.FILE006.RED
//*
//*        <<< OUTPUT GENEVAERS FILES >>>
//*
//MR88RPT  DD SYSOUT=*
//MR88LOG  DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SORTDIAG DD SYSOUT=*
//*
//MR88HXM  DD DUMMY,BLKSIZE=27998
//*
//MR88HXD  DD DUMMY,BLKSIZE=2458
//*
//SYSMDUMP DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1F1.SYSMDUMP,
//            DISP=(NEW,DELETE,CATLG),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1000,1000),RLSE),
//            DCB=(DSORG=PS,RECFM=FBS,LRECL=4160)
//*
//*        <<< OUTPUT VIEW FILES >>>
//*
//DCOBYSTO DD DSN=&DEMOHLQ..&DEMOMLQ..PASS1F1.DCOBYSTO,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(10,10),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=1004)
//*
//******************************************************************
//* IF THE PRIOR STEP SETS AN INVALID RETURN CODE, TERMINATE THE JOB
//******************************************************************
//*
//PSTEP206 EXEC PGM=GVBUT99,
//            COND=(1,GE,PSTEP205),
//            PARM='1099'
//STEPLIB  DD DISP=SHR,DSN=&LOADLIB
//*
//