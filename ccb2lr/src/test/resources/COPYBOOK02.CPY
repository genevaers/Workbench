000000***************************************************************#VB
000000*   GENEVA VERSION 3.3.001024P                                   *
000000******************************************************************
       01  RLHL-HEADER-RECORD.                                          
00103      05  RLHL-FILE-ID                     PIC  X(08).             
00104      05  RLHL-LOGICAL-RECORD              PIC  X(04).             
00106      05  RLHL-RECORD-COUNT                PIC S9(08) COMP.        
00105      05  RLHL-RECORD-LENGTH               PIC S9(04) COMP.        
00107      05  RLHL-KEY-OFFSET                  PIC S9(04) COMP.        
00108      05  RLHL-KEY-LENGTH                  PIC S9(04) COMP.        
           05  RLHL-DSAM-FLAG                   PIC  X(01).             
           05  RLHL-EFFECTIVE-DATE-FLAG         PIC  X(01).             
           05  RLHL-RECORDS-ABOVE               PIC S9(08) COMP.        
           05  RLHL-RECORDS-BELOW               PIC S9(08) COMP.        
           05  RLHL-START-DATE-OFFSET           PIC S9(04) COMP.        
           05  RLHL-END-DATE-OFFSET             PIC S9(04) COMP.        
00109      05  FILLER                           PIC  X(64).             
00110                                                                   