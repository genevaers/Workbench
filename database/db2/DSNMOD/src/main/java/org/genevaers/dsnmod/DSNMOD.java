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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.ibm.jzos.RcException;
import com.ibm.jzos.RecordReader; // BSAM access
import com.ibm.jzos.RecordWriter;
import com.ibm.jzos.ZFile;
import com.ibm.jzos.ZFileConstants;
import com.ibm.jzos.ZFileException;
import com.ibm.jzos.fields.PackedDecimalAsIntField; // Packed number processing

import java.util.Scanner;

public class DSNMOD {

    static Integer max_length = 54; // 44 plus a pds member

    public static void main(String[] args) {

        Integer dbg = 0;
        String codepage = "Cp1047";
        String[] ddname = new String[5];
        String[] ddout  = new String[5];
        String[] dsn1 = new String[5];
        String[] dsn2 = new String[5];
        Integer[] offset = new Integer[5];
        Integer rc = 0;
        Integer parmrec = 0;
        RecordReader parmreader = null;
        String ddparm = "DDPARM";
        Integer i;

        ddname[0] = "INPUT01";
        ddname[1] = "INPUT02";
        ddname[2] = "INPUT03";
        ddname[3] = "INPUT04";
        ddname[4] = "INPUT05";
        ddout[0] = "OUTPUT1";
        ddout[1] = "OUTPUT2";
        ddout[2] = "OUTPUT3";
        ddout[3] = "OUTPUT4";
        ddout[4] = "OUTPUT5";

        //GvbDsnmodConfig dc = new GvbDsnmodConfig(dsn1, dsn2, offset, codepage, ddname, ddout, dbg);

        // read parameter cards 
        try {
            parmreader = RecordReader.newReaderForDD(ddparm);
            int lrecl = parmreader.getLrecl(); 
            byte[] recordBuf = new byte[lrecl];
            int bytesRead;
            // Read records one by one until the end of the file
            while ((bytesRead = parmreader.read(recordBuf)) >= 0) {
                String card = new String(recordBuf, 0, 80, codepage);
                //System.out.println("Card: " + card + bytesRead);
                Scanner scanner = new Scanner(card);
                if (parmrec < 5) {
                    String oldfilenm = scanner.next();
                    Integer oldfileoff = scanner.nextInt();
                    System.out.println("Old file name: " + oldfilenm + " offset: " + oldfileoff);
                    dsn1[parmrec] = oldfilenm;
                    offset[parmrec] = oldfileoff;
                } else {
                    if ( parmrec < 10) {
                        String newfilenm = scanner.next();
                        System.out.println("New file name: " + newfilenm);
                        dsn2[parmrec - 5] = newfilenm;
                    } else {
                        if (parmrec == 10) {
                            String codepg = scanner.next();
                            System.out.println("Code page: " + codepg);
                            codepage = codepg;
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

        for ( i = 0; i < 5; i++) {
            rc = processfile( dsn1[i], dsn2[i], offset[i], codepage, ddname[i], ddout[i], dbg);
        }
        System.out.println("Rc: " + rc);

        return;
    }

    public static Integer processfile(String dsn1, String dsn2, Integer offset, String codepage, String ddname, String ddout, Integer dbg) {
    
        RecordReader reader = null;
        RecordWriter writer = null;
        Integer i, hexlen;
        byte hexbyte;
        Integer n = 0;
        Integer m = 0;

        // validation
        if ( offset < 1 ) {
            System.out.println("Offset of start of dataset name must be greater than one");
            return 8;
        }
        try {
            byte[] ddnamebytes = ddname.getBytes(codepage);
            byte[] ddoutbytes = ddout.getBytes(codepage);
            if ( ddname == null || ddout == null || !isWithinRange(ddnamebytes.length, 1, 8) || !isWithinRange(ddoutbytes.length, 1, 8)) {
                System.out.println("DDNAME for input or output is null. DDNAME: " + ddname + " DDOUT: " + ddout + ":" + ddnamebytes.length + ":" + ddoutbytes.length);
                return 8;
            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Unsupported encoding");
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

        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Now append the ".LOB part"

        try {
            // Get an instance of RecordReader for the specified DD name
            reader = RecordReader.newReaderForDD(ddname);
            writer = RecordWriter.newWriterForDD(ddout);
            
            // Determine the maximum record length (LRECL) for buffer sizing
            int lrecl = reader.getLrecl();
            if (( offset + max_length ) > lrecl ) {
                System.out.println("The maximum LRECL of " + ddname + " of " + lrecl + " is insufficient for the specified offset " + offset);
                return 12;
            }
            byte[] recordBuf = new byte[lrecl];
            int bytesRead;
            byte[] dsn1bytes = dsn1.getBytes(codepage);
            byte[] dsn2bytes = dsn2.getBytes(codepage);

            // Read records one by one until the end of the file
            while ((bytesRead = reader.read(recordBuf)) >= 0) {
                m ++;
                if ( bytesRead >= (offset + max_length)) {
                    String dsn = new String(recordBuf, offset, max_length, codepage);
                    if (memcmp(dsn1bytes, 0, recordBuf, offset, dsn1.length())) {
                        n ++;
                        //j = 100 - dsn2.length() - 10;
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
                    System.out.println("Record is too small to process and cannot contain search dataset name");
                }
                // if match with dataset modify dataset name
                writer.write(recordBuf,0,bytesRead); // write record back anyway
            }
        } catch (ZFileException e) {
            System.out.println("IO error reading from " + ddname + " and writing to " + ddout);
            return 12;
        } catch (UnsupportedEncodingException e) {
            System.out.println("Code page exception using " + codepage);
            return 12;
        } finally {
            // Ensure the reader is closed in a finally block to release resources
            if (writer != null) {
                try {
                    writer.close();
                } catch (ZFileException e) {
                    System.out.println("IO error closing output " + ddout);
                    return 12;
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (ZFileException e) {
                    System.out.println("IO error closing input " + ddname);
                    return 12;
                }
            }
        }
        System.out.println("Number of records processed for ddname " + ddname + " is " + m);
        System.out.println("Number of dataset names modified is " + n );
        System.out.println("Output written to " + ddout + "\n");
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
