01  CUSTOMER-RECORD.
       05  STREET-ADDRESS          PIC X(20).
       05  CITY                    PIC X(17).
       05  STATE                   PIC XX.
       05  ZIP-CODE                PIC 9(5).
       05  FILLER                  PIC X(10).
       05  CUSTOMER-NAME OCCURS 6 TIMES.
           10  LAST-NAME           PIC X(15).
           10  FIRST-NAME          PIC X(8).
