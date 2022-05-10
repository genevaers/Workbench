      ******************************************************************
      ******************************************************************
       01  RECORD-FORMAT.

           05  SECTION-05.
               10  AMOUNT                    PIC S9(7)V99  COMP-3.


               10  TYPE-MATCH-ACCY-CD        PIC X(1).
               10  LNGTD                     PIC S9(4)V9(7)
                                                     COMP-3.
               10  LATUD                     PIC S9(4)V9(7)
                                                     COMP-3.
               10  QMS-LOC-CD                PIC X(5).
               10  QMS-MTCH-CD               PIC X(5).


      ****************   OTHER KEY FIELDS       *******************

             10  OTHER-KEY-FIELDS.
                 15 KEY-ID1                  PIC S9(9)   USAGE COMP.
                 15 KEY-ID2                  PIC S9(9)   USAGE IS COMP.
