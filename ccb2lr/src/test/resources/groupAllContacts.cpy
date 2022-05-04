       01  MAILING-RECORD.
              05  COMPANY-NAME            PIC X(30).
              05  ALL-CONTACTS.
                     10  CONTACTS OCCURS 3 TIMES.
                            15  FULLNAME OCCURS 3 TIMES.
                            20  P-LAST-NAME       PIC X(15).
                            20  P-FIRST-NAME      PIC X(8).
              05  ADDRESS                 PIC X(15).
              05  CITY                    PIC X(15).
              05  STATE                   PIC XX.
              05  ZIP                     PIC 9(5).
              