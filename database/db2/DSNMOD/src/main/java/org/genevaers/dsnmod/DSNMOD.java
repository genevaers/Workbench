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
import com.ibm.jzos.ZFileException;
import com.ibm.jzos.RcException;

import com.ibm.jzos.RecordReader;
import com.ibm.jzos.RecordWriter;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileConstants;
import com.ibm.jzos.CatalogSearch;
import com.ibm.jzos.CatalogSearchField;
import com.ibm.jzos.Format1DSCB;
import com.ibm.jzos.ZUtil;
import java.util.logging.Logger;
import java.util.Scanner;

public class DSNMOD {

    private static final Logger logger = Logger.getLogger(DSNMOD.class.getName());

    static Integer max_length = 54; // 44 plus a pds member

    public static void main(String[] args) {

        Integer dbg = 0;
        String codepage = "Cp1047"; // default
        String[] dsn1 = new String[6];
        String[] dsn2 = new String[6];
        Integer[] offset = new Integer[6];
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
                        logger.info("Processing .PNCH files only is selected");
                        break;
                    // Process DATA files only
                    case "D":
                        lPunch = false;
                        logger.info("Processing .DATA files only is selected");
                        break;
                    // Codepage override
                    case "C":
                        codepage = args[n].substring(2);
                        logger.info("Codepage requested:" + codepage);
                    // Debug information
                    case "d":
                        dbg = b.doAtois(args[n], 2);
                        logger.info("Debug is set at level: " + dbg);
                        break;
                    case "h":
                        logger.info("-D (process DATA files only)\n-P (process PNCH files only)\n-C (codepage)\n-d (debug level)");
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

            logger.info("Parameter processing -----------------------------------------------------------------------------");
            // Read records one by one until the end of the file
            while ((bytesRead = parmreader.read(recordBuf)) >= 0) {
                String card = new String(recordBuf, 0, 80, codepage);
                Scanner scanner = new Scanner(card);
                if (parmrec == 0) {
                  schemaNameOld = scanner.next();
                  logger.info("Old schema name: " + schemaNameOld);
                } else {
                  if (parmrec == 1) {
                    schemaNameNew = scanner.next();
                    logger.info("New schema name: " + schemaNameNew);
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
                        logger.info("DATA Mask: " + maskData);
                        logger.info("PNCH Mask: " + maskPnch);
                      } else {
                        if (parmrec >= 4 && parmrec <= 9) {
                          String dataLlq = scanner.next();
                          Integer dataOff = scanner.nextInt();
                          dsn1[iRec] = unldHlqMlq + "." + dataLlq;
                          dsn2[iRec] = loadHlqMlq + "." + dataLlq;
                          offset[iRec] = dataOff;
                          logger.info("Old file name: " + dsn1[iRec] + " Offset: " + offset[iRec]);
                          logger.info("New file name: " + dsn2[iRec]);
                          iRec++;
                        } else {
                            logger.info("Warning: more parameter lines read than expected: " + card);
                        }
                      }
                    }
                  }
                }
                scanner.close();
                parmrec++;
            }

        } catch (ZFileException zfe) {
            logger.severe("ZFileException occurred reading from: " + ddparm);
            logger.severe("Native errno description: " + zfe.getErrnoMsg());
            return;
        } catch (RcException rce) {
            logger.severe("Native ZOS error reading dataset: " + ddparm);
            logger.severe("Message: " + rce.getMessage());
            logger.severe("Return Code: " + rce.getRc());
            return;
        } catch (UnsupportedEncodingException e) {
            logger.severe("Code page exception using: " + codepage);
            return;
        } catch (Exception e) {
            logger.severe("Unexpected error reading dataset: " + ddparm);
            return;
        } finally {
            // Ensure the reader is closed in a finally block to release resources
            if (parmreader != null) {
                try {
                    parmreader.close();
                } catch (ZFileException zfe) { // continue
                    logger.warning("ZFileException occurred closing: " + ddparm);
                    logger.warning("Native errno description: " + zfe.getErrnoMsg());
                }
            }
        }

        if ( iRec != 6 ) {
            logger.severe("Error: less parameter lines read than expected.");
            return;
        }

        if ( lData ) {
          Integer rcHigh = 0;
          logger.info("Process .DATA files for .LOB dependencies --------------------------------------------------------");
          for ( i = 0; i < dsn1.length; i++) {
              rc = processDataFile( dsn1[i], dsn2[i], offset[i], codepage, dbg);
              if ( rcHigh < rc ) {
                rcHigh = rc;
              }
          }
          logger.info("Highest return code from processDataFile: " + rcHigh);
        }

        if ( lPunch ) {
          rc = processPnchFiles( maskPnch, schemaNameOld, schemaNameNew, codepage, dbg );
          logger.info("Return code from processPnchFiles: " + rc);
        }

        return;
    }

    public static Integer processPnchFiles(String maskPnch, String schemaNameOld, String schemaNameNew, String codepage, Integer dbg) {
        Integer rc = 0;
      
        logger.info("Process .PNCH files to update Schema names -------------------------------------------------------");
        if (0 < dbg) {
          logger.info("PNCH Mask: " + maskPnch);
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
              logger.info(String.format("Dataset: %-44s Volser: %-6s\n", dsName, volser));
              rc = processSinglePnchFile(dsName, schemaNameOld, schemaNameNew, codepage, dbg);
            }
          }
        } catch (RcException rce) {
            logger.severe("Native ZOS error reading MVS catalog with mask: " + maskPnch);
            logger.severe("Message: " + rce.getMessage());
            logger.severe("Return Code: " + rce.getRc());
            return 12;
        } catch (Exception e) {
            logger.severe("Catalog search RC: " + cs.getRc() + " " + cs.getReason());
            return 12;
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
          logger.info("Dsn1: " + dsn1 + " Dsn2: " + dsn2 + " Offset: " + offset + " Codepage: " + codepage);
        }

        iLastIndex = dsn2.lastIndexOf(".LOB"); // determine name of .DATA file associated with .PNCH file
        if (iLastIndex >= 1) {
            jLastIndex = dsn2.lastIndexOf(".VIEWSRCF.LOB"); // this one has a slightly different name just to be consistent
            if (jLastIndex >= 1) {
                dsn2Data = dsn2.substring(0, jLastIndex) + ".VIEWSRC.DATA";
                fmtDsn2Data = "//'" + dsn2Data + "'";
            } else {
                jLastIndex = dsn2.lastIndexOf(".VIEWSRCO.LOB"); // this one has a slightly different name just to be consistent
                if (jLastIndex >= 1) {
                    dsn2Data = dsn2.substring(0, jLastIndex) + ".VIEWSRC.DATA";
                    fmtDsn2Data = "//'" + dsn2Data + "'";
                } else {                                        // these have consistent names for .DATA file
                    dsn2Data = dsn2.substring(0, iLastIndex) + ".DATA";
                    fmtDsn2Data = "//'" + dsn2Data + "'";
                }
            }
        } else {
            logger.severe("Error detected in LOB file specification: " + dsn2  + ". Check LOB file definitions in parameter file.");
            return 8;
        }

        String dsn2DataOut  = dsn2Data + "2";
        String fmtDsn2DataOut = "//'" + dsn2DataOut + "'"; // USS version
        String dummyDD = ZFile.allocDummyDDName();
        String cmd = "alloc fi("+dummyDD+") da(" + dsn2DataOut + ") like(" + dsn2Data + ") reuse new catalog msg(wtp)";

        if (0 < dbg) {
            logger.info("Dsn2Data: " + dsn2Data + " Formatted Dsn2Data: " + fmtDsn2Data);
            logger.info("Dsn2 cmd: " + cmd);
        }

        // validation of search/replacement of dataset name(s)
        if ( offset < 1 ) {
            logger.severe("Offset of start of dataset name to match must be greater than or equal to one (1)");
            return 8;
        }

        try {
            ZFile.bpxwdyn(cmd);  // might throw RcException
        } catch (RcException rce) {
            logger.severe("Native ZOS error allocating output dataset: " + dsn2DataOut);
            logger.severe("Message: " + rce.getMessage());
            logger.severe("Return Code: " + rce.getRc());
            return 12;
         }

        if ( dsn1.length() < 1 ) {
            logger.severe("Value of scanned for dataset name is insufficient: " + dsn1 );
            return 8;
        }
        if ( dsn2.length() < 1 ) {
            logger.severe("Value to replace dataset name is insufficient: " + dsn2 );
            return 8;
        }

        try {
            // Get an instance of RecordReader for the specified DD name
            reader = RecordReader.newReader(fmtDsn2Data, ZFileConstants.FLAG_DISP_SHR);
            
            // Determine the maximum record length (LRECL) for buffer sizing
            int lrecl = reader.getLrecl();
            if (( offset + max_length ) > lrecl ) {
                logger.severe("The maximum LRECL of " + dsn2Data + " of " + lrecl + " is insufficient for the specified offset " + offset);
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
                        logger.severe("Record too small to process and cannot contain search dataset name within dataset: " + dsn2Data);
                        return 12;
                    }
                    // if match with dataset modify the dataset name where it occurs at offset in records
                    writer.write(recordBuf,0,bytesRead); // write record back anyway
                }
            } catch (ZFileException zfe) {
                logger.severe("ZFileException occurred for output dataset: " + dsn2DataOut);
                logger.severe("Native errno description: " + zfe.getErrnoMsg());
                return 12;
            } catch (RcException rce) {
                logger.severe("Native ZOS error for output dataset: " + dsn2DataOut);
                logger.severe("Message: " + rce.getMessage());
                logger.severe("Return Code: " + rce.getRc());
                return 12;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (ZFileException zfe) {
                        logger.severe("ZFileException occurred closing output dataset: " + dsn2DataOut);
                        logger.severe("Native errno description: " + zfe.getErrnoMsg());
                        return 12;
                    }
                    try {
                        ZFile.bpxwdyn("free fi(" + dummyDD + ") msg(2)");
                    } catch (RcException rce) { // continue
                        logger.warning("Native ZOS error deallocating output dataset: " + dsn2DataOut);
                        logger.warning("Message: " + rce.getMessage());
                        logger.warning("Return Code: " + rce.getRc());
                    }
                }
            }
        } catch (ZFileException zfe) {
            logger.severe("ZFileException for input dataset: " + dsn2Data);
            logger.severe("Native errno description: " + zfe.getErrnoMsg());
            return 12;
        } catch (RcException rce) {
            logger.severe("Native ZOS error for input dataset: " + dsn2Data);
            logger.severe("Return Code: " + rce.getRc());
            logger.severe("Message: " + rce.getMessage());
            return 12;    
        } catch (UnsupportedEncodingException e) {
            logger.severe("Code page exception using: " + codepage);
            return 12;
        } finally {
            // Ensure the reader is closed in final block to release resources
            if (reader != null) {
                try {
                    reader.close();
                } catch (ZFileException zfe) {
                    logger.severe("ZFileException occurred closing input dataset: " + dsn2Data);
                    logger.severe("Native errno description: " + zfe.getErrnoMsg());
                    return 12;
                }
            }
        }
        
        logger.info("Number of records processed from input dataset: " + dsn2Data + " is: " + m);
        logger.info("Number of .LOB related embedded dataset names modified is: " + n );
        logger.info("All records including modifications written to output dataset: " + dsn2DataOut);

        try {
            logger.info("Attempting to delete: " + dsn2Data);
            ZFile.remove(fmtDsn2Data);
            logger.info("Successfully deleted: " + dsn2Data);
            try {
                ZFile.rename(fmtDsn2DataOut, fmtDsn2Data);
                logger.info("Successfully renamed: " + dsn2DataOut + " to: " + dsn2Data);
            } catch (ZFileException zfe) {
                logger.severe("ZFileException failed to rename dataset: " + dsn2DataOut);
                logger.severe("Native errno description: " + zfe.getErrnoMsg());
                return 12;
            }
        } catch (ZFileException zfe) {
            logger.severe("ZFileException failed to delete dataset: " + dsn2Data);
            logger.severe("Native errno description: " + zfe.getErrnoMsg());
            return 12;
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

        // validate .PNCH file DSORG
        try {
            ZFile dsnFileAttr = new ZFile(fmtName, "rb,type=record,noseek");
            if (!dsnFileAttr.getRecfm().equals("FB") || dsnFileAttr.getLrecl() != 80) {
                logger.severe("PNCH file must be LRECL=80 and RECFM=FB for dataset: " + dsName);
                logger.severe("\tRecord Format: " + dsnFileAttr.getRecfm());
                logger.severe("\tRecord Length: " + dsnFileAttr.getLrecl());
                return 12;
            }
            dsnFileAttr.close();
        } catch (ZFileException zfe) {
            logger.severe("ZFileException getting attributes for dataset: " + dsName);
            logger.severe("Native errno description: " + zfe.getErrnoMsg());
            return 12;
        } catch (RcException rce) {
            logger.severe("Native ZOS error getting attributes for dataset: " + dsName);
            logger.severe("Message: " + rce.getMessage());
            logger.severe("Return Code: " + rce.getRc());
            return 12;
        }
        catch (Exception e) {
            logger.severe("Unexpected error getting attributes for dataset: " + dsName);
            return 12;
        }
        
        String cmd = "alloc fi("+dummyDD+") da(" + dsNameOut + ") like(" + dsName + ") reuse new catalog msg(wtp)";
        if (0 < dbg) {
            logger.info("DSN: " + dsName + " Old Schema: " + schemaNameOld + " New Schema: " + schemaNameNew);
            logger.info("PNCH cmd: " + cmd);
        }

        Integer schemaLength = Math.max(schemaNameOld.length(), schemaNameNew.length());
        if (schemaLength > 8 ) {
            logger.severe("DB2 Schema name must not exceed length of 8: supplied length is: " + schemaLength);
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

                byte[] recordBuf = new byte[recLength];
                while ((reader.read(recordBuf)) >= 0) {
                    iCount = iCount + 1;
                    if (memcmp(OldSchemaBytes, 0, recordBuf, offset, lengthReplaced)) {
                        jCount = jCount + 1;
                        if (4 != iCount) {
                            logger.info("Warning: Old Schema name is located on line: " + iCount + " of input dataset: " + dsName);
                        }
                        System.arraycopy(NewSchemaBytes, 0, recordBuf, offset, lengthReplaced);
                    }
                    writer.write(recordBuf, 0, recLength); // write record back anyway
                }
            } catch (ZFileException zfe) { //.//
                logger.severe("ZFileException for output dataset: " + dsNameOut);
                logger.severe("Native errno description: " + zfe.getErrnoMsg());
                return 12;
            } catch (RcException rce) {
                logger.severe("Native ZOS error for output dataset: " + dsNameOut);
                logger.severe("Message: " + rce.getMessage());
                logger.severe("Return Code: " + rce.getRc());
                return 12;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (ZFileException zfe) {
                        logger.severe("ZFileException closing output dataset:" + dsNameOut);
                        logger.severe("Native errno description: " + zfe.getErrnoMsg());
                        return 12;
                    }
                    try {
                        ZFile.bpxwdyn("free fi(" + dummyDD + ") msg(2)");
                    } catch (RcException rce) {
                        logger.warning("Error deallocating output dataset:" + dsNameOut);
                    }
                }
            }
        } catch (ZFileException zfe) {
            logger.severe("ZFileException opening or reading input dataset: " + dsName);
            logger.severe("Native errno description: " + zfe.getErrnoMsg());
            return 12;
        } catch (RcException rce) {
            logger.severe("Native ZOS error for input dataset: " + dsName);
            logger.severe("Return Code: " + rce.getRc());
            logger.severe("Message: " + rce.getMessage());
            return 12;    
        } catch (UnsupportedEncodingException e) {
            logger.severe("Code page exception using: " + codepage);
            return 12;
        } finally {
            if (reader != null) {
                try {
                  reader.close();
                } catch (ZFileException zfe) { // continue
                    logger.severe("ZFileException closing input dataset: " + dsName);
                    logger.severe("Native errno description: " + zfe.getErrnoMsg());
                    return 12;
                }
            } 
        }

        logger.info("Number of records copied from dataset: " + dsName + " is: " + iCount + " modified record count is: " + jCount);

        try {
            logger.info("Attempting to delete: " + dsName);
            ZFile.remove(fmtName);
            logger.info("Successfully deleted: " + dsName);
            try {
                ZFile.rename(fmtNameOut, fmtName);
                logger.info("Successfully renamed: " + dsNameOut + " to: " + dsName);
            } catch (ZFileException zfe) {
                logger.severe("ZFileException renaming dataset: " + dsNameOut);
                logger.severe("Native errno description: " + zfe.getErrnoMsg());
                return 12;
            }
        } catch (ZFileException zfe) {
            logger.severe("ZFileException deleting dataset: " + dsName);
            logger.severe("Native errno description: " + zfe.getErrnoMsg());
            return 12;
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
