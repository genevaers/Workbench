000010****************************************************************
000020**                                                             *
000030**               COPYBOOK NAME - EAQKC                         *
000040**                                                             *
000050** DESCRIPTION:  BIRA FIRE PROFIT/LOSS FILE                    *
000060**                                                             *
000070**                                                             *
000080** LENGTH = 449                                                *
000090****************************************************************
000100**                                                             *
000110** VER       CONTROL       ANALYST       EFFECTIVE DATE        *
000120** ---       -------       -------       --------------        *
000130** AA        R15910        BORDT          2011/05/31           *
000140** REASON:  INITIAL ISSUANCE                                   *
000150**-------------------------------------------------------------*
000160****************************************************************
000170 01  FDW-FIRE-ALLOC-REC.
000180     05 RECORD-FORMAT                   PIC  X(05).
000190     05 FILE-DATE
000200        10 YEAR                         PIC  X(04).
000210        10 MONTH                        PIC  X(02).
000220        10 DAYS                         PIC  X(02).
000230     05 WRITING-MIS-STATE               PIC  X(02).
000240     05 MIS-STATE                       PIC  X(02).
000250     05 AGENT                           PIC  X(04).
000260     05 CO-CD                           PIC  X(04).
000270     05 H-CODE                          PIC  X(06).
000280*    05 DUMMY-ALLOC-IND                 PIC  X(01).
000290     05 PREM-ALLOC-FIELDS.
000300        10 GRP2-ULAE-EXPNS-PD-AMT       PIC  X(19).
000310        10 GRP2-ULAE-EXPNS-UNPD-AMT     PIC  X(19).
000320        10 GRP3-COMS-EXPNS-AMT          PIC  X(19).
000330        10 GRP3-OTH-ACQ-AMT             PIC  X(19).
000340        10 GRP4-EXPNS-AMT               PIC  X(19).
000350        10 GRP5-EXPNS-AMT               PIC  X(19).
000360        10 SUPP-RSRV-INDM-AMT           PIC  X(19).
000370        10 SUPP-RSRV-ALAE-AMT           PIC  X(19).
000380        10 SUPP-RSRV-ULAE-AMT           PIC  X(19).
000390        10 IBNR-RSRV-INDM-AMT           PIC  X(19).
000400        10 IBNR-RSRV-ALAE-AMT           PIC  X(19).
000410        10 IBNR-RSRV-ULAE-AMT           PIC  X(19).
000420        10 OS-RSRV-ALAE-AMT             PIC  X(19).
000430        10 OS-RSRV-ULAE-AMT             PIC  X(19).
000440     05 LOSS-ALLOC-FIELDS.
000450        10 SUPP-CAT-INDM-AMT            PIC  X(19).
000460        10 SUPP-CAT-ALAE-AMT            PIC  X(19).
000470        10 SUPP-CAT-ULAE-AMT            PIC  X(19).
000480        10 IBNR-CAT-INDM-AMT            PIC  X(19).
000490        10 IBNR-CAT-ALAE-AMT            PIC  X(19).
000500        10 IBNR-CAT-ULAE-AMT            PIC  X(19).
000510        10 OS-CAT-R-ALAE-AMT            PIC  X(19).
000520        10 OS-CAT-R-ULAE-AMT            PIC  X(19).
