       01  MAILING-RECORD.
              05  COMPANY-NAME            PIC X(30).
              05  ADDRESS                 PIC X(15).
              05  CITY                    PIC X(15).
              05  STATE                   PIC XX.
              05  ZIP                     PIC 9(5).
              05  CONTACTS.
                     10  PRESIDENT.
                            15  P-LAST-NAME       PIC X(15).
                            15  P-FIRST-NAME      PIC X(8).
                     10  VP-MARKETING.
                            15  V-LAST-NAME       PIC X(15).
                            15  V-FIRST-NAME      PIC X(8).
                     10  ALTERNATE-CONTACT.
                            15  A-TITLE           PIC X(10).
                            15  A-LAST-NAME       PIC X(15).
                            15  A-FIRST-NAME      PIC X(8).
