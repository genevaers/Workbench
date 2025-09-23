//DEFINE   JOB (ACCT),'CREATE DATABASE',
//            NOTIFY=&SYSUID.,
//            CLASS=A,
//            MSGLEVEL=(1,1),
//            MSGCLASS=H
//********************************************************************
//*
//* (C) COPYRIGHT IBM CORPORATION 2023.
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
//******************************************************************
//*         Define GENEVAERS Database in Db2
//******************************************************************
//*
//*   These instructions can be used for creaon of a Db2 database
//*
//*   Member Description
//*
//*    GVBQCDBT        Create Database and tablespace
//*    GVBQCTBC        Create C_* Tables
//*    GVBQCTBE        Create E_* Tables
//*    GVBQCTBX        Create X_* Tables
//*    GVBQCLBT        Create Logic Table with LOB column
//*    GVBQCIXC        Create C_* Indexes
//*    GVBQCIXE        Create E_* Indexes
//*    GVBQCIXX        Create X_* Indexes
//*    GVBQCFKY        Create Foreign Keys
//*    GVBQINCO        Insert CODETABLE rows
//*    GVBQINTB        Insert all other rows
//*    GVBQINNX        Insert C_NEXTKEYID rows
//*    GVBQDRAL        Drop the entire schema
//*
//* 
//*   The following variables will need to be set to match the Db2
//*   subsystem the packages are to reside.
//*
//*   .   ensure variables are exportable
//*
//         EXPORT SYMLIST=*
//*
//*******************************************************************
//*   Standard Variables in use
//*
//   SET HLQ='GENEVA'
//   SET MLQ='GVBSTOR'
//   SET DB2LOAD='DSN.V13R1M0.SDSNLOAD'
//   SET DB2EXIT='DSN.V13R1M0.SDSNEXIT'
//   SET DB2RUN='DSN131.RUNLIB.LOAD'
//   SET DB2SYS='DM13'
//   SET DB2SCMA='GENES001'
//   SET DB2DB='GENED001'
//*
//********************************************************************
//*
//JOBLIB   DD DISP=SHR,DSN=&DB2EXIT
//         DD DISP=SHR,DSN=&DB2LOAD
//         DD DISP=SHR,DSN=&DB2RUN
//         DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=CEE.SCEERUN2
//         DD DISP=SHR,DSN=CBC.SCLBDLL
//*************************************************************
//*   BUILD NEW FORMAT DATABASE
//*************************************************************
//*
//*   .   Remove the entire schema
//*
//D0       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQDRAL)
/*
//*
//*   .   Create the database and tablespaces for the
//*       the GENEVAERS metadata
//*
//D1       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCDBT)
/*
//*
//*   .   Create C_* Tables for the GENEVAERS metadata
//*  
//D2       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBC)
/*
//*
//*   .   Create E_* Tables for the GENEVAERS metadata
//*  
//D3       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBE)
/*
//*
//*   .   Create X_* Tables for the GENEVAERS metadata
//*  
//D4       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBX)
/*
//*
//*   .   Create Logic Table with LOB column for the GENEVAERS metadata
//*  
//D5       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCLBT)
/*
//*
//*   .   Create C_* Indexes for the GENEVAERS metadata
//*
//D6       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIXC)
/*
//*
//*   .   Create E_* Indexes for the GENEVAERS metadata
//*
//D7       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIXE)
/*
//*
//*   .   Create X_* Indexes for the GENEVAERS metadata
//*
//D8       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIXX)
/*
//*
//*   .   Create the Foreign Keys for the GENEVAERS metadata
//*
//D9       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCFKY)
/*
//*
//*   .   Insert rows into CODETABE for the GENEVAERS metadata
//*
//D10       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINCO)
/*
//*
//*   .   Insert all other rows for the GENEVAERS metadata
//*
//D11       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINTB)
/*
//*
//*   .   Insert C_NEXTKEYID rows for the GENEVAERS metadata
//*
//D12       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
  DSN SYSTEM(&DB2SYS)
  RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13)
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINNX)
/*
