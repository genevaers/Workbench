      ******************************************************************
      01  RECORD-FORMAT.
           02  PREMIUM-HIST-DATA.
               10  RECORD-FORMAT                 PIC X(6).
               10  FILE-DATE.
                   15  YEAR                      PIC 9(4).
                   15  MONTH                     PIC 99.
                   15  DAYS                      PIC 99.

      ******* END OF JY0  ********************************************

         02  EXPANDED-AREA.

      ***************************************************************

           05  SECTION-01.
               10  AMT                          PIC S9(7)V99.

     
            **FILLED                                **
      ***************************************************************
           05  ANOTHER-AMT                       PIC S9(5).
           05  FILLER                            PIC X(5).
      ***************************************************************

         02  FILLER-CODES.
               10  STUFF                         PIC X(22).
               10  FILLER                        PIC X(3).

         02  NUMBERED-FILL.
             05  VALUES.
               10  VALUE-01                      PIC S9(9)V99.
               10  VALUE-02                      PIC S9(9)V99.
               10 FILLER-03                      PIC X(3).
         02 FILLER                               PIC X(25)     
