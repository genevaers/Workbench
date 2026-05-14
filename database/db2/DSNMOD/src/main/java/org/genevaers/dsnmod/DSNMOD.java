/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.io.UnsupportedEncodingException;

import com.ibm.jzos.RcException;
import com.ibm.jzos.RecordReader;
import com.ibm.jzos.RecordWriter;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileConstants;
import com.ibm.jzos.ZFileException;
import com.ibm.jzos.CatalogSearch;
import com.ibm.jzos.CatalogSearchField;
import com.ibm.jzos.Format1DSCB;
import com.ibm.jzos.RcException;
import com.ibm.jzos.ZUtil;

import java.util.Scanner;

public class DSNMOD {

    static Integer max_length = 54; // 44 plus a pds member

    public static void main(String[] args) {

        Integer dbg = 0;
        String codepage = "Cp1047";
        String[] dsn1 = new String[6]; //////////////////////////////////////////////////////////////////
        String[] dsn2 = new String[6]; //////////////////////////////////////////////////////////////////
        Integer[] offset = new Integer[6]; //////////////////////////////////////////////////////////////////
        Integer rc = 0;
        Integer parmrec = 0;
        RecordReader parmreader = null;
        String ddparm = "DDPARM";
        String maskOld = "";
        String maskNew = "";
        String maskData = "";
        String maskPnch = "";
        String unldHlqMlq = "";
        String loadHlqMlq = "";
        String schemaNameOld = "";
        String schemaNameNew = "";
        Boolean lData = true;
        Boolean lPunch = true;
        Integer iRec = 0;
        Integer i, n;

        // command line argument[s]
        Integer nArgs =args.length;
        GVBA2I b = new GVBA2I();

        for (n = 0; n < nArgs; n++) {
            if (args[n].substring(0,1).equals("-")) {
                switch( args[n].substring(1,2))
                 {
                    // Process PNCH files only
                    case "P":
                        lData = false;
                        System.out.println("Processing .PNCH files only is selected");
                        break;
                    // Process DATA files only
                    case "D":
                        lPunch = false;
                        System.out.println("Processing .DATA files only is selected");
                        break;
                    // Debug information
                    case "d":
                        dbg = b.doAtois(args[n], 2);
                        System.out.println("Debug set, level: " + dbg);
                        break;
                    case "h":
                        System.out.println("-D (process DATA files only)\n-P (process PNCH files only)");
                        return;
                    default:
                        break;
                }
            }
        }

        // read parameter cards 
        try {
            parmreader = RecordReader.newReaderForDD(ddparm);
            int lrecl = parmreader.getLrecl(); 
            byte[] recordBuf = new byte[lrecl];
            int bytesRead;

            System.out.println("Parameter processing -----------------------------------------------------------------------------");
            // Read records one by one until the end of the file
            while ((bytesRead = parmreader.read(recordBuf)) >= 0) {
                String card = new String(recordBuf, 0, 80, codepage);
                //System.out.println("Card: " + card + bytesRead);
                Scanner scanner = new Scanner(card);
                if (parmrec == 0) {
                  schemaNameOld = scanner.next();
                  System.out.println("Old schema name: " + schemaNameOld);
                } else {
                  if (parmrec == 1) {
                    schemaNameNew = scanner.next();
                    System.out.println("New schema name: " + schemaNameNew);
                  } else {
                    if (parmrec == 2) {
                      unldHlqMlq = scanner.next();
                      maskOld = unldHlqMlq + ".**";
                    } else {
                      if (parmrec == 3) {
                        loadHlqMlq = scanner.next();
                        maskNew = loadHlqMlq + ".**";
                        maskData = loadHlqMlq + ".**.DATA";
                        maskPnch = loadHlqMlq + ".**.PNCH";
                        System.out.println("DATA Mask: " + maskData);
                        System.out.println("PNCH Mask: " + maskPnch);
                      } else {
                        if (parmrec >= 4 && parmrec <= 9) { //////////////////////////////////////////////////////////////////
                          String dataLlq = scanner.next();
                          Integer dataOff = scanner.nextInt();
                          dsn1[iRec] = unldHlqMlq + "." + dataLlq;
                          dsn2[iRec] = loadHlqMlq + "." + dataLlq;
                          offset[iRec] = dataOff;
                          System.out.println("Old file name: " + dsn1[iRec] + " Offset: " + offset[iRec]);
                          System.out.println("New file name: " + dsn2[iRec]);
                          iRec++;
                        }
                      }
                    }
                  }
                }
                scanner.close();
                parmrec++;
            }

        } catch (ZFileException e) {
            System.out.println("IO error reading from " + ddparm);
            return;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Code page exception using " + codepage);
            return;
        } finally {
            // Ensure the reader is closed in a finally block to release resources
            if (parmreader != null) {
                try {
                    parmreader.close();
                } catch (ZFileException e) {
                    System.out.println("IO error closing " + ddparm);
                }
            }
        }

        if ( lData ) {
          Integer rcHigh = 0;
          System.out.println("\nProcess .DATA files for .LOB dependencies --------------------------------------------------------");
          for ( i = 0; i < 6; i++) { //////////////////////////////////////////////////////////////////
              rc = processDataFile( dsn1[i], dsn2[i], offset[i], codepage, dbg);
              if ( rcHigh < rc ) {
                rcHigh = rc;
              }
          }
          System.out.println("Highest return code from processDataFile: " + rcHigh);
        }

        if ( lPunch ) {
          rc = processPnchFiles( maskPnch, schemaNameOld, schemaNameNew, codepage, dbg );
          System.out.println("Return code from processPnchFiles: " + rc);
        }

        return;
    }

    public static Integer processPnchFiles(String maskPnch, String schemaNameOld, String schemaNameNew, String codepage, Integer dbg) {
        Integer rc = 0;
      
        System.out.println("\nProcess .PNCH files to update Schema names -------------------------------------------------------");
        if (0 < dbg) {
          System.out.println("PNCH Mask: " + maskPnch);
        }

        CatalogSearch cs = new CatalogSearch(maskPnch, 64000);
        // Define search criteria (dataset name, volume, etc.)
        try {
          cs.addFieldName("ENTNAME"); // Entry Name (Dataset Name)
          cs.addFieldName("VOLSER");  // Volume Serial
          cs.search();
          while (cs.hasNext()) {
            CatalogSearch.Entry entry = (CatalogSearch.Entry)cs.next();
            if (entry.isDatasetEntry()) {
              CatalogSearchField field = entry.getField("ENTNAME");
              String dsName = field.getFString().trim();
              field = entry.getField("VOLSER");
              String volser = field.getFString().trim();
              System.out.printf("Dataset: %-44s Volser: %-6s\n", dsName, volser);

              rc = processSinglePnchFile(dsName, schemaNameOld, schemaNameNew, codepage, dbg);
            }
          }
        } catch (Exception e) {
          System.out.println("RC: " + cs.getRc() + " " + cs.getReason());
          e.printStackTrace();
        }
        return rc;
    }

    public static Integer processDataFile(String dsn1, String dsn2, Integer offset, String codepage, Integer dbg) {
        RecordReader reader = null;
        RecordWriter writer = null;
        Integer i, hexlen;
        Integer n = 0;
        Integer m = 0;
        Integer iLastIndex = 0;
        Integer jLastIndex = 0;

        byte hexbyte;
        String dsn2Data = ""; // name of .DATA file associated with .PNCH file
        String fmtDsn2Data = ""; // USS version
        
        if (0 < dbg) {
          System.out.println("Dsn1: " + dsn1 + " Dsn2: " + dsn2 + " Offset: " + offset + " Codepage: " + codepage);
        }

        iLastIndex = dsn2.lastIndexOf(".LOB"); // determine name of .DATA file associated with .PNCH file
        if (iLastIndex >= 1) {
            jLastIndex = dsn2.lastIndexOf(".VIEWSRCF.LOB"); // this one has a slightly different name just to be consistent
            if (jLastIndex >= 1) {
                dsn2Data = dsn2.substring(0, jLastIndex) + ".VIEWSRC.DATA";
                fmtDsn2Data = "//'" + dsn2Data + "'";
            } else {                                        // these have consistent names for .DATA file
                dsn2Data = dsn2.substring(0, iLastIndex) + ".DATA";
                fmtDsn2Data = "//'" + dsn2Data + "'";
            }
        } else {
            System.out.println("Error detected in LOB file specification: " + dsn2  + ". Check LOB file definitions in parameter file.");
            return 8;
        }

        String dsn2DataOut  = dsn2Data + "2";
        String fmtDsn2DataOut = "//'" + dsn2DataOut + "'"; // USS version
        String dummyDD = ZFile.allocDummyDDName();
        String cmd = "alloc fi("+dummyDD+") da(" + dsn2DataOut + ") reuse new catalog msg(2) recfm(v,b) space(25,25) RELEASE cyl lrecl(27994) blksize(27998)";


        if (0 < dbg) {
            System.out.println("Dsn2Data: " + dsn2Data + " Formatted Dsn2Data: " + fmtDsn2Data);
            System.out.println("DATA2 cmd: " + cmd);
        }

        // validation of search/replacement of dataset name(s)
        if ( offset < 1 ) {
            System.out.println("Offset of start of dataset name must be greater than or equal to one (1)");
            return 8;
        }

        try {
            ZFile.bpxwdyn(cmd);  // might throw RcException
        } catch (RcException rce) {
            rce.printStackTrace();
            return 12;
         }

        if ( dsn1.length() < 1 ) {
            System.out.println("Value of scanned for dataset name is insufficient: " + dsn1 );
            return 8;
        }
        if ( dsn2.length() < 1 ) {
            System.out.println("Value to replace dataset name is insufficient: " + dsn2 );
            return 8;
        }

        try {
            // Get an instance of RecordReader for the specified DD name
            reader = RecordReader.newReader(fmtDsn2Data, ZFileConstants.FLAG_DISP_SHR);
            
            // Determine the maximum record length (LRECL) for buffer sizing
            int lrecl = reader.getLrecl();
            if (( offset + max_length ) > lrecl ) {
                System.out.println("The maximum LRECL of " + dsn2Data + " of " + lrecl + " is insufficient for the specified offset " + offset);
                return 8;
            }
            byte[] recordBuf = new byte[lrecl];
            int bytesRead;
            byte[] dsn1bytes = dsn1.getBytes(codepage);
            byte[] dsn2bytes = dsn2.getBytes(codepage);

            // open output file
            try {
                writer = RecordWriter.newWriterForDD(dummyDD);

                // Read records one by one until the end of the file
                while ((bytesRead = reader.read(recordBuf)) >= 0) {
                    m ++;
                    if ( bytesRead >= (offset + max_length)) {
                        String dsn = new String(recordBuf, offset, max_length, codepage);
                        if (memcmp(dsn1bytes, 0, recordBuf, offset, dsn1.length())) {
                            n ++;
                            hexbyte = recordBuf[offset -1]; 
                            hexlen = Byte.toUnsignedInt(hexbyte);
                            // save bytes we will need in a minute                    
                            byte[] savebytes = new byte[10];
                            //System.out.println("Updating: " + dsn );
                            for ( i = 0; i < 10; i++ ) {
                                savebytes[i] = recordBuf[dsn1.length() + offset + i];
                            }
                            // substitute other bytes in file name
                            for ( i = 0; i < dsn2.length(); i++) {
                                recordBuf[offset + i] = dsn2bytes[i];
                            }
                            // restore save bytes
                            for ( i = 0; i < 10; i++ ) {
                                recordBuf[dsn2.length() + offset + i] = savebytes[i];
                            }
                            // blank over remainder
                            for ( i = 0; i < 32; i++ ) {
                                recordBuf[10 + dsn2.length() + offset + i] = 0x40;
                            }
                            // new value for hex length
                            hexlen = dsn2.length() + 10;
                            hexbyte = hexlen.byteValue();
                            recordBuf[offset -1] = hexbyte;
                        }
                    }
                    else {
                        System.out.println("Record is too small to process and cannot contain search dataset name: ");
                    }
                    // if match with dataset modify dataset name
                    writer.write(recordBuf,0,bytesRead); // write record back anyway
                }
            } catch (ZFileException e) {
                System.out.println("IO error for output DSN: " + dsn2DataOut);
                return 12;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (ZFileException zfe) {
                        System.out.println("IO error closing output DSN:" + dsn2DataOut);
                        return 12;
                    }
                    try {
                        ZFile.bpxwdyn("free fi(" + dummyDD + ") msg(2)");
                    } catch (RcException rce) {
                        System.out.println("Err deallocating output dataset: " + dsn2DataOut);
                        rce.printStackTrace();  // but continue
                    }
                }
            }
        } catch (ZFileException e) {
            System.out.println("IO error reading from " + dsn2Data);
            return 12;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Code page exception using " + codepage);
            return 12;
        } finally {
            // Ensure the reader is closed in final block to release resources
            if (reader != null) {
                try {
                    reader.close();
                } catch (ZFileException e) {
                    System.out.println("IO error closing input " + dsn2Data);
                    return 12;
                }
            }
        }
        
        System.out.println("Number of records processed from input file: " + dsn2Data + " is: " + m);
        System.out.println("Number of .LOB related embedded dataset names modified is: " + n );
        System.out.println("All records including modifications written to output file: " + dsn2DataOut);

        try {
            System.out.println("Attempting to delete: " + dsn2Data);
            ZFile.remove(fmtDsn2Data);
            System.out.println("Successfully deleted: " + dsn2Data);
            try {
                ZFile.rename(fmtDsn2DataOut, fmtDsn2Data);
                System.out.println("Successfully renamed: " + dsn2DataOut + " to: " + dsn2Data);
            } catch (Exception e) {
                System.err.println("Failed to rename file: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ZFileException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public static Integer processSinglePnchFile(String dsName, String schemaNameOld, String schemaNameNew, String codepage, Integer dbg) {
        Integer iCount = 0;
        Integer jCount = 0;
        Integer offset = 1; // offset of first double quote preceding schema in record 4
        Integer lengthReplaced = 11; // always replace padded length in quotes followed by period

        RecordReader reader = null;
        String fmtName = "//'" + dsName + "'";
        String dsNameOut  = dsName + "2";
        String fmtNameOut = "//'" + dsNameOut + "'";
        String dummyDD = ZFile.allocDummyDDName();

        String cmd = "alloc fi("+dummyDD+") da(" + dsNameOut + ") reuse new catalog msg(2) recfm(f,b) space(1,3) RELEASE cyl lrecl(80)";
        if (0 < dbg) {
            System.out.println("DSN: " + dsName + " Old Schema: " + schemaNameOld + " New Schema: " + schemaNameNew);
            System.out.println("PNCH cmd: " + cmd);
        }

        Integer schemaLength = Math.max(schemaNameOld.length(), schemaNameNew.length());
        if (schemaLength > 8 ) {
            System.out.println("DB2 Schema name must not exceed length of 8: supplied length is: " + schemaLength);
            return 8;
        }

        // Schema: take into account "'s and . meaning actual length will be 11 
        String NameOldPad = String.format("%-11s", "\"" + schemaNameOld + "\".");
        String NameNewPad = String.format("%-11s", "\"" + schemaNameNew + "\".");

        try {
            byte[] OldSchemaBytes = NameOldPad.getBytes(codepage);
            byte[] NewSchemaBytes = NameNewPad.getBytes(codepage);

            ZFile.bpxwdyn(cmd);  // might throw RcException
            RecordWriter writer = null;

            reader = RecordReader.newReader(fmtName, ZFileConstants.FLAG_DISP_SHR);
            Integer recLength = reader.getLrecl();

            try {
                writer = RecordWriter.newWriterForDD(dummyDD);

                if (recLength != 80) {
                    System.out.println("Input file must be fixed record length 80, actual record length is: " + recLength);
                    return 8;
                }
            
                byte[] recordBuf = new byte[recLength];
                while ((reader.read(recordBuf)) >= 0) {
                    iCount = iCount + 1;
                    if (memcmp(OldSchemaBytes, 0, recordBuf, offset, lengthReplaced)) {
                        jCount = jCount + 1;
                        if (4 != iCount) {
                            System.out.println("Warning: Old Schema name located on line: " + iCount + " of " + dsName);
                        }
                        System.arraycopy(NewSchemaBytes, 0, recordBuf, offset, lengthReplaced);
                    }
                    writer.write(recordBuf, 0, recLength); // write record back anyway
                }
            } catch (ZFileException e) {
                System.out.println("IO error for output DSN: " + dsNameOut);
                return 12;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (ZFileException zfe) {
                        System.out.println("IO error closing output DSN:" + dsNameOut);
                        return 12;
                    }
                    try {
                        ZFile.bpxwdyn("free fi(" + dummyDD + ") msg(2)");
                    } catch (RcException rce) {
                        rce.printStackTrace();  // but continue
                    }
                }
            }
        } catch (ZFileException e) {
            System.out.println("IO error for input DSN: " + dsName);
            return 12;
        } catch (RcException rce) {
            rce.printStackTrace();
            return 12;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Code page exception using " + codepage);
            return 12;
        } finally {
            if (reader != null) {
                try {
                  reader.close();
                } catch (ZFileException e) {
                    System.out.println("IO error closing input DSN: " + dsName);
                    return 12;
                }
            } 
        }

        System.out.println("Number of records copied from DSN: " + dsName + " is: " + iCount + " modified is: " + jCount);

        try {
            System.out.println("Attempting to delete: " + dsName);
            ZFile.remove(fmtName);
            System.out.println("Successfully deleted: " + dsName);
            try {
                ZFile.rename(fmtNameOut, fmtName);
                System.out.println("Successfully renamed: " + dsNameOut + " to: " + dsName);
            } catch (Exception e) {
                System.err.println("Failed to rename file: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ZFileException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;

    }

    public static boolean memcmp(byte[] b1, int b1Index, byte[] b2, int b2Index, int length) {
    
        for (int i = 0; i < length; i++) {
            // Compare bytes as unsigned values for C-style behavior
            int uByte1 = b1[b1Index + i] & 0xFF;
            int uByte2 = b2[b2Index + i] & 0xFF;

            if (uByte1 != uByte2) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWithinRange(int num, int min, int max) {
        return (num >= min && num <= max);
    }
}
