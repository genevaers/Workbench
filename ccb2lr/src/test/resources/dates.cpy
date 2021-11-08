* TRAIN6 EXAMPLE COBOL COPYBOOK
01 MASTER_REC.                                                  
  05  ACCOUNT_NO                  PIC X(9).                    
  05  REC_TYPE                    PIC X.                       
  05  AMOUNT                      PIC S9(4)V99 COMP-3.         
  05  BIN-NO                      PIC S9(8) COMP.              
  05  BIN-NO-X REDEFINES BIN-NO   PIC XXXX.                    
  05  DECIMAL-NO                  PIC S999.                    
  05  MASTER-DATE.                                             
      10  DATE-YY            PIC 9(2).                     
      10  DATE-MM            PIC 9(2).                        
      10  DATE-DD            PIC 9(2).                     
  05  MASTER-DOB REDEFINES MASTER-DATE.                     
      10  YYMMDD             PIC XXXXXX.                    
  05  ACT_TYPE                    PIC X.                 
  05  OTHER-DATE.                                           
      10  ODATE-YY           PIC 9(2).                    
      10  ODATE-MM           PIC 9(2).                      
      10  ODATE-DD           PIC 9(2).                  
  05  OTHER-DOB REDEFINES OTHER-DATE.                       
      10  OYYMMDDTT          PIC 9(8).                   
  05  OTHER_TYPE                  PIC X.                     