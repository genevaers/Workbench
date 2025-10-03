//INSTSP  JOB (GILLIAN),'STORED PROC',REGION=0M,                      
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
//* install stored procedures
//********************************************************************
//*  
//   EXPORT SYMLIST=*                                                  
//*                                                                    
//   SET HLQ='GEBT'                                                    
//   SET MLQ='RTC23321'                                                
//   SET DB2SYS=DM13                                                   
//   SET DB2NAME=SADBNEIL                                              
//   SET DB2SCH=SAFRNEIL                                               
//*                                                                    
//PROCLIB  JCLLIB ORDER=(GEBT.BUILD.SITE,                              
//            GEBT.BUILD.PROC,                                         
//            DSN.V13R1M0.PROCLIB,                                     
//            GEBT.RTC23321.JCL)                                       
//*                                                                    
//JOBLIB   INCLUDE MEMBER=@INCLLIB                                     
//*                                                                    
//*********************************************************************
//*   GRANT SCHEMA PRIVILEDGES                                         
//*********************************************************************
//*                                                                    
//GVBNGRSC EXEC PGM=IKJEFT1A                                           
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                             
         LIB('DSN131.RUNLIB.LOAD')                                     
//*                                                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
GRANT BINDADD TO &DB2SCH;                                              
GRANT CREATE IN COLLECTION &DB2SCH TO &DB2SCH;                         
//*                                                                    
//SYSTSPRT DD SYSOUT=*                                                 
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//*********************************************************************
//*   DROP STORED PROCEDURES                                           
//*********************************************************************
//*                                                                    
//GVBNDPP1 EXEC PGM=IKJEFT1A                                           
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
 DSN SYSTEM(&DB2SYS)                                                   
     RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                             
         LIB('DSN131.RUNLIB.LOAD')                                     
//*                                                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
SET CURRENT SQLID = '&DB2SCH';                                         
//         DD DSN=&HLQ..&MLQ..SQL(GVBNDPPR),                           
//            DISP=SHR                                                 
//*                                                                    
//SYSTSPRT DD SYSOUT=*                                                 
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//*********************************************************************
//*   CREATE STORED PROCEDURES                                         
//*********************************************************************
//*                                                                    
//* //CHKDRP   IF (RC > 4) THEN                                        
//*                                                                    
//* //ELDR1    ELSE                                                    
//CRPR1 EXEC PGM=IKJEFT1A                                              
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
   DSN SYSTEM(&DB2SYS)                                                 
       RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                           
           LIB('DSN131.RUNLIB.LOAD')       -                           
           PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')                       
//*                                                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
SET CURRENT SQLID = '&DB2SCH'@                                         
//         DD DSN=&HLQ..&MLQ..SQL(GVBPCLEE),                           
//            DISP=SHR                                                 
//*                                                                    
//SYSTSPRT DD SYSOUT=*                                                 
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//* //         ENDIF                                                   
//*                                                                    
//         IF (CRPR1.RC > 4) THEN                                      
//*                                                                    
//ELDR2    ELSE                                                        
//CRPR2 EXEC PGM=IKJEFT1A                                              
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
  DSN SYSTEM(&DB2SYS)                                                  
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                            
          LIB('DSN131.RUNLIB.LOAD')       -                            
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')                        
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPDELE),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR2.RC > 4) THEN              
//*                                            
//ELDR3    ELSE                                
//CRPR3 EXEC PGM=IKJEFT1A                      
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPENVD),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR3.RC > 4) THEN              
//*                                            
//ELDR4    ELSE                                
//CRPR4 EXEC PGM=IKJEFT1A                      
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPINIT),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR4.RC > 4) THEN              
//*                                            
//ELDR5    ELSE                                
//CRPR5 EXEC PGM=IKJEFT1A                      
//*                                              
//SYSTSIN  DD *,SYMBOLS=EXECSYS                  
  DSN SYSTEM(&DB2SYS)                            
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -      
          LIB('DSN131.RUNLIB.LOAD')       -      
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')  
//*                                              
//SYSIN    DD *,SYMBOLS=EXECSYS                  
SET CURRENT SQLID = '&DB2SCH'@                   
//         DD DSN=&HLQ..&MLQ..SQL(GVBPGLFD),     
//            DISP=SHR                           
//*                                              
//SYSTSPRT DD SYSOUT=*                           
//SYSPRINT DD SYSOUT=*                           
//*                                              
//         ENDIF                                 
//*                                              
//         IF (CRPR5.RC > 4) THEN                
//*                                              
//ELDR6    ELSE                                  
//CRPR6 EXEC PGM=IKJEFT1A                        
//*                                              
//SYSTSIN  DD *,SYMBOLS=EXECSYS                  
  DSN SYSTEM(&DB2SYS)                            
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -      
          LIB('DSN131.RUNLIB.LOAD')       -      
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')  
//*                                              
//SYSIN    DD *,SYMBOLS=EXECSYS                  
SET CURRENT SQLID = '&DB2SCH'@                   
//         DD DSN=&HLQ..&MLQ..SQL(GVBPGJND),     
//            DISP=SHR                           
//*                                              
//SYSTSPRT DD SYSOUT=*                           
//SYSPRINT DD SYSOUT=*                           
//*                                              
//         ENDIF                                 
//*                                              
//         IF (CRPR6.RC > 4) THEN                
//*                                              
//ELDR7    ELSE                                  
//CRPR7 EXEC PGM=IKJEFT1A                        
//*                                              
//SYSTSIN  DD *,SYMBOLS=EXECSYS                  
    DSN SYSTEM(&DB2SYS)                          
        RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
            LIB('DSN131.RUNLIB.LOAD')       -    
            PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                              
//SYSIN    DD *,SYMBOLS=EXECSYS                  
SET CURRENT SQLID = '&DB2SCH'@                   
//         DD DSN=&HLQ..&MLQ..SQL(GVBPGLRD),     
//            DISP=SHR                           
//*                                              
//SYSTSPRT DD SYSOUT=*                           
//SYSPRINT DD SYSOUT=*                           
//*                                              
//         ENDIF                               
//*                                            
//         IF (CRPR7.RC > 4) THEN              
//*                                            
//ELDR8    ELSE                                
//CRPR8 EXEC PGM=IKJEFT1A                      
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPGPFD),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR8.RC > 4) THEN              
//*                                            
//ELDR9    ELSE                                
//CRPR9 EXEC PGM=IKJEFT1A                      
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPVWDE),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR9.RC > 4) THEN              
//*                                            
//ELDR10    ELSE                               
//CRPR10 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTARC),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR10.RC > 4) THEN             
//*                                            
//ELDR11    ELSE                               
//CRPR11 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDEX),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR11.RC > 4) THEN             
//*                                            
//ELDR12    ELSE                               
//CRPR12 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDFL),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR12.RC > 4) THEN             
//*                                            
//ELDR13    ELSE                               
//CRPR13 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDLF),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR13.RC > 4) THEN             
//*                                            
//ELDR14    ELSE                               
//CRPR14 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDJN),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR14.RC > 4) THEN             
//*                                            
//ELDR15    ELSE                               
//CRPR15 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDLR),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR15.RC > 4) THEN             
//*                                            
//ELDR16    ELSE                               
//CRPR16 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDPF),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR16.RC > 4) THEN             
//*                                            
//ELDR17    ELSE                               
//CRPR17 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPTDVW),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR17.RC > 4) THEN             
//*                                            
//ELDR18    ELSE                               
//CRPR18 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPVWPR),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR18.RC > 4) THEN             
//*                                            
//ELDR19    ELSE                               
//CRPR19 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPGVER),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR19.RC > 4) THEN             
//*                                            
//ELDR20    ELSE                               
//CRPR20 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPINJF),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR20.RC > 4) THEN             
//*                                            
//ELDR21    ELSE                               
//CRPR21 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPINLR),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR21.RC > 4) THEN             
//*                                            
//ELDR22    ELSE                               
//CRPR22 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPINSE),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR22.RC > 4) THEN             
//*                                            
//ELDR23    ELSE                               
//CRPR23 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPINVC),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR23.RC > 4) THEN             
//*                                            
//ELDR24    ELSE                               
//CRPR24 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPINVS),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR24.RC > 4) THEN             
//*                                            
//ELDR25    ELSE                               
//CRPR25 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPUPFD),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                               
//*                                            
//         IF (CRPR25.RC > 4) THEN             
//*                                            
//ELDR26    ELSE                               
//CRPR26 EXEC PGM=IKJEFT1A                     
//*                                            
//SYSTSIN  DD *,SYMBOLS=EXECSYS                
  DSN SYSTEM(&DB2SYS)                          
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -    
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBPUPVW),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//*                                            
//         ENDIF                                                       
//*                                                                    
//         IF (CRPR26.RC > 4) THEN                                     
//*                                                                    
//ELDR27    ELSE                                                       
//CRPR27 EXEC PGM=IKJEFT1A                                             
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
  DSN SYSTEM(&DB2SYS)                                                  
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                            
          LIB('DSN131.RUNLIB.LOAD')       -                            
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')                        
//*                                                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
SET CURRENT SQLID = '&DB2SCH'@                                         
//         DD DSN=&HLQ..&MLQ..SQL(GVBPUPVC),                           
//            DISP=SHR                                                 
//*                                                                    
//SYSTSPRT DD SYSOUT=*                                                 
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//         ENDIF                                                       
//*                                                                    
//         IF (CRPR27.RC > 4) THEN                                     
//*                                                                    
//ELDR28    ELSE                                                       
//CRPR28 EXEC PGM=IKJEFT1A                                             
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
  DSN SYSTEM(&DB2SYS)                                                  
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                            
          LIB('DSN131.RUNLIB.LOAD')       -                            
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')                        
//*                                                                    
//SYSIN    DD *,SYMBOLS=EXECSYS                                        
SET CURRENT SQLID = '&DB2SCH'@                                         
//         DD DSN=&HLQ..&MLQ..SQL(GVBPUPLO),                           
//            DISP=SHR                                                 
//*                                                                    
//SYSTSPRT DD SYSOUT=*                                                 
//SYSPRINT DD SYSOUT=*                                                 
//*                                                                    
//         ENDIF                                                       
//*                                                                    
//         IF (CRPR28.RC > 4) THEN                                     
//*                                                                    
//*                                                                    
//*********************************************************************
//*   GRANT EXECUTE PRIVILEGE TO STORED PROCEDURES                     
//*********************************************************************
//*                                                                    
//ELCRE    ELSE                                                        
//GVBNGRPR EXEC PGM=IKJEFT1A                                           
//*                                                                    
//SYSTSIN  DD *,SYMBOLS=EXECSYS                                        
  DSN SYSTEM(&DB2SYS)                                                  
      RUN PROGRAM(DSNTEP2) PLAN(DSNTEP13) -                            
          LIB('DSN131.RUNLIB.LOAD')       -    
          PARMS('/SQLFORMAT(SQLPL),SQLTERM(@)')
//*                                            
//SYSIN    DD *,SYMBOLS=EXECSYS                
SET CURRENT SQLID = '&DB2SCH'@                 
//         DD DSN=&HLQ..&MLQ..SQL(GVBNGRPR),   
//            DISP=SHR                         
//*                                            
//SYSTSPRT DD SYSOUT=*                         
//SYSPRINT DD SYSOUT=*                         
//         ENDIF                               
//
