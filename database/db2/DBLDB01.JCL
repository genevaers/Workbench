//BLDDB01  JOB (ACCT),'BUILD DB STEP1',                                
//            NOTIFY=&SYSUID.,                                         
//            CLASS=A,                                                 
//            MSGLEVEL=(1,1),                                          
//            MSGCLASS=H                                               
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
//* create database, C_*, E_* and X_* tables
//********************************************************************
//*  
//   EXPORT SYMLIST=*                                                  
//*                                                                    
//   SET HLQ='GEBT'                                                    
//   SET MLQ='RTC23321'                                                
//   SET DB2SYS=DM13                                                   
//*                                                                    
//PROCLIB  JCLLIB ORDER=(GEBT.BUILD.SITE,                              
//            GEBT.BUILD.PROC,                                         
//            DSN.V13R1M0.PROCLIB,                                     
//            GEBT.RTC23321.JCL)                                       
//*                                                                    
//JOBLIB   INCLUDE MEMBER=@INCLLIB                                     
//*                                                                    
//*********************************************************************
//*   BUILD DATABASE                                                   
//*********************************************************************
//BUILDDB EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                             
            LIB('DSN131.RUNLIB.LOAD')                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCDBT)                      
//*                                                                    
//*********************************************************************
//*   BUILD C_ TABLES                                                  
//*********************************************************************
//BLDCTB  EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                             
            LIB('DSN131.RUNLIB.LOAD')                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBC)                      
//*                                                                    
//*********************************************************************
//* BUILD E_ TABLES                                                    
//*********************************************************************
//BLDETB  EXEC PGM=IKJEFT01,DYNAMNBR=20                                
//SYSTSPRT DD SYSOUT=*                                                 
//SYSTSIN DD *,SYMBOLS=EXECSYS                                         
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                             
            LIB('DSN131.RUNLIB.LOAD')                                  
//SYSPRINT DD SYSOUT=*                                                 
//SYSUDUMP DD SYSOUT=*                                                 
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBE)
//*                                              
//***********************************************
//*   BUILD X_ TABLES                            
//***********************************************
//BLDXTB  EXEC PGM=IKJEFT01,DYNAMNBR=20          
//SYSTSPRT DD SYSOUT=*                           
//SYSTSIN DD *,SYMBOLS=EXECSYS                   
 DSN SYSTEM(&DB2SYS)                             
     RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -       
            LIB('DSN131.RUNLIB.LOAD')            
//SYSPRINT DD SYSOUT=*                           
//SYSUDUMP DD SYSOUT=*                           
//SYSIN DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBX)
//*                                              
//                                               
