//DEFINE   JOB (ACCT),'CREATE DATABASE',
//            NOTIFY=&SYSUID.,
//            CLASS=&$JBCLASS.,
//            MSGLEVEL=(1,1),
//            MSGCLASS=&$MSGCLASS.
//*
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
//*    GVBQDRAL  -  Remove the entire schema
//*    GVBQCDBT  -  Create the database and tablespaces for the
//*                 the GENEVAERS metadata
//*    GVBQCTBL  -  Create the tables for the GENEVAERS metadata
//*    GVBQCIDX  -  Create the indexes for the GENEVAERS metadata
//*    GVBQCFKY  -  Create the Foreign Keys for the GENEVAERS metadata
//*    GVBQINCO  -  Insert rows into CODE for the GENEVAERS metadata
//*    GVBQINTB  -  Insert rows into CODE for the GENEVAERS metadata
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
//   SET HLQ=&$PDSHLQ.
//   SET MLQ=&$PDSMLQ.
//   SET DB2SYS=&$DBSUB.
//   SET DB2PLAN=&$DB2PLAN.
//   SET DB2PLIB=&$DB2PLIB.
//   SET DB2RLIB=&$DB2RLIB.
//*
//PROCLIB  JCLLIB ORDER=(&DB2PLIB.,
//            &HLQ..&MLQ..JCL)
//*
//JOBLIB   INCLUDE MEMBER=@INCLLIB
//*
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
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
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
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCDBT)
/*
//*
//*   .   Create the tables for the GENEVAERS metadata
//*  
//D2       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
 DSN SYSTEM(&DB2SYS)
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCTBL)
/*
//*
//*   .   Create the indexes for the GENEVAERS metadata
//*
//D3       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
 DSN SYSTEM(&DB2SYS)
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCIDX)
/*
//*
//*   .   Create the Foreign Keys for the GENEVAERS metadata
//*
//D4       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
 DSN SYSTEM(&DB2SYS)
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQCFKY)
/*
//*
//*   .   Insert rows into CODE for the GENEVAERS metadata
//*
//D5       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
 DSN SYSTEM(&DB2SYS)
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINCO)
/*
//*
//*   .   Insert rows into CODE for the GENEVAERS metadata
//*
//D6       EXEC PGM=IKJEFT1A
//SYSTSPRT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSTSIN  DD *,SYMBOLS=EXECSYS
 DSN SYSTEM(&DB2SYS)
     RUN PROGRAM(DSNTEP2) PLAN(&DB2PLAN) -
            LIB('&DB2RLIB.')
//*
//SYSIN    DD DISP=SHR,DSN=&HLQ..&MLQ..DDL(GVBQINTB)
/*