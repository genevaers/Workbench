IGNORE 01  GVBRH-HEADER-RECORD.
           05  RH-FILE-ID                       PIC S9(08) COMP.
           05  RH-LR-ID                         PIC S9(08) COMP.
           05  RH-RECORD-CNT                    PIC S9(08) COMP.
           05  RH-RECORD-LEN                    PIC S9(04) COMP.
           05  RH-KEY-OFFSET                    PIC S9(04) COMP.
           05  RH-KEY-LEN                       PIC S9(04) COMP.
           05  RH-EXTR-FILE-NBR                 PIC S9(04) COMP.
           05  FILLER REDEFINES RH-EXTR-FILE-NBR.
               10 FILLER                        PIC  X(01).
               10 RH-EFF-DATE-IND               PIC  X(01).
           05  RH-EFF-DATE-OPT-CD               PIC  X(01).
           05  FILLER                           PIC  X(01).
           05  RH-TEXT-DATA-FLAG                PIC  X(01).
           05  FILLER                           PIC  X(57).
