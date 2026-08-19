// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE INDEXES

package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import java.sql.*;

public class GvbSchemaValidateC {

    private Integer rc;
    private static final Logger logger = Logger.getLogger(GvbSchemaValidateC.class.getName());

    public GvbSchemaValidateC(GvbSchemaConfig sc)
    {
        Boolean match = true;

        BufferedWriter hwriter = sc.getHwriter(); // For writing digest values of hashmaps
        BufferedWriter[] dwriter = new BufferedWriter[4]; // For writing Schema definitions
        BufferedWriter fwriter = sc.getFwriter(); // General output writers
        dwriter = sc.getDwriter();
        String digestType = sc.getDigestType();
        Connection con = sc.getCon();
        String schema_mask = sc.getSchemaMask();
        Boolean makeHash = sc.getMakeHash();
        Boolean makeDef = sc.getMakeDef();
        HashMap<String, String> ixmap = sc.getIxmap();

        String schema;
        String tname;
        String iname;
        String uniqueR;
        String lastTab = "";
        String lastIname = "";

        String colname = "";
        Integer colno = 0;
        Integer colseq = 0;

        Statement stmt;
        Statement stmt2;
        ResultSet rs;
        ResultSet rs2;

        ConsoleHandler handler = new ConsoleHandler(); //Create a handler (where the logs go)
        handler.setFormatter(new GVBFormatter()); //Attach your custom formatter
        logger.setUseParentHandlers(false); //Disable default parent handlers to avoid duplicate logs
        logger.addHandler(handler); //Add the handler to your logger and set the level
        logger.setLevel(Level.ALL); //Apply to all levels

        logger.info("GvbSchemaValidateC: processing indexes for schema: " + schema_mask);

        String SQLstmt = "SELECT CREATOR, TBNAME, NAME, UNIQUERULE FROM SYSIBM.SYSINDEXES WHERE CREATOR LIKE '" + schema_mask + "' ORDER BY TBNAME, NAME;";

        try {
            StringBuilder sb = new StringBuilder("");
            // Print generated digest value to separate file if requested -A
            if ( makeHash ) {
                hwriter.write("        // HashMap<String, String> ixmap = new HashMap<>(100);\n");
                hwriter.write("        // Populate digest map of indexes using " + digestType +"\n");
            }

            // Create SQL Statement
            stmt = con.createStatement();
            logger.fine("Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery(SQLstmt);
            logger.fine("Created JDBC ResultSet object");

            fwriter.write("\nIndex Validation Report by table for schema: " + schema_mask + "\n\n");

            boolean hasData = false;
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                schema = rs.getString(1);
                tname = rs.getString(2);
                iname = rs.getString(3);
                uniqueR = rs.getString(4);

                String SQLstmt2= "SELECT COLNAME, COLNO, COLSEQ FROM SYSIBM.SYSKEYS WHERE IXCREATOR = '" + schema + "' AND IXNAME = '" + iname + "' ORDER BY IXNAME, COLSEQ;";

                try {
                    stmt2 = con.createStatement();
                    rs2 = stmt2.executeQuery(SQLstmt2);

                    while (rs2.next()) {
                        hasData = true;
                        colname = rs2.getString(1);
                        colno = rs2.getInt(2);
                        colseq = rs2.getInt(3);

                        if (lastIname.equals(iname)) {
                        } else { // iname break
                            if ( makeDef ) {
                                dwriter[2].write("\n========================================================================================\n");
                                dwriter[2].write(schema + " TABLE: " + tname + " INDEX: " + iname + "\n");
                            }
                        }

                        if ( makeDef ) {
                            dwriter[2].write(schema + " " + tname + " " + iname + " " + uniqueR + " " + colname + " " + colno + " " + colseq + "\n");
                        }
                        // append without schema because schema is user defined
                        sb.append(tname + " " + iname + " " + uniqueR + " " + colname + " " + colno + " " + colseq);

                        lastIname = iname;

                    } // inner loop ------

                    rs2.close();
                    stmt2.close();

                } catch (SQLException e) {
                    logger.severe("SQLSTATE: " + e.getSQLState() + " executing: " + SQLstmt2 + e.getMessage());
                    rc = 4;
                    return;
                } catch (IOException e) {
                    logger.severe("IO exception encountered in GvbSchemaValidateC inner loop");
                    rc = 8;
                    return;
                }

                if (sb.length() > 0 ) {
                    byte[] hashedBytes = md.digest((sb.toString()).getBytes());
                    String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                    if ( makeHash) {
                        hwriter.write(tname + iname + "," + encodedHash + "\n"); //populate hash map: one per TableIndex
                    }
                    else {
                        // report on schema correctness
                        fwriter.write("Table: " + tname + " index: " + iname + " Digest: " + digestType + ": " + encodedHash + "\n");
                        String hashvalue = ixmap.get(tname + iname);

                        if (hashvalue == null) {
                            logger.warning("HASH value mismatch for table: " + tname + " index: " + iname + " - no stored hash value");
                            fwriter.write("HASH value mismatch for table: " + tname + " index: " + iname + " - no stored hash value\n");
                            fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                            match = false;
                        }
                        else {
                            if ( hashvalue.equals(encodedHash) ) {
                                fwriter.write("HASH value matches for table: " + tname + " index: " + iname + "\n");
                            }
                            else
                            {
                                logger.warning("HASH value mismatch for table: " + tname + " index: " + iname);
                                fwriter.write("HASH value mismatch for table: " + tname + " index: " + iname + "\n");
                                fwriter.write("Computed hash value: " + encodedHash + "\n");
                                fwriter.write("Stored hash value  : " + hashvalue + "\n");
                                fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                match = false;
                            }
                        }
                    }
                } else {
                    logger.warning("HASH value mismatch for table: " + tname + " index: " + iname + " - no keys retrieved for index");
                    match = false;
                }

                sb.delete(0, sb.length());
                sb.append("");

                lastTab = tname;
            } // outer loop ------

            if (hasData) {
                logger.fine("Fetched all rows from JDBC ResultSet");
            } else {
                logger.severe("HASH value cannot be formulated - no indexes found for schema: " + schema_mask);
                fwriter.write("HASH value cannot be formulated - no indexes found for schema: " + schema_mask);
                fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                match = false;
            }

            // Close the ResultSet
            rs.close();
            logger.fine("Closed JDBC ResultSet");
      
            // Close the Statement
            stmt.close();
            logger.fine("Closed JDBC Statement");

        } catch (SQLException e) {
            logger.severe("SQLSTATE: " + e.getSQLState() + " executing: " + SQLstmt + e.getMessage());
            rc = 4;
            return;
        } catch (IOException e) {
            logger.severe("IO exception encountered in GvbSchemaValidateC");
            rc = 8;
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Digest algorithm: " + digestType + " not available");
            rc = 12;
            return;
        }
        
        if ( makeHash ) {
            if (match) {
                logger.info("Index digest hashmap created");
                rc = 2;
            } else {
                rc = 3;
            }
            return;
        }
        else {
            if ( match )
            {
                logger.info("All index definitions match");
                rc = 0;
                return;
            }
            else
            {
                logger.warning("One or more indexes do not match expected definitions ***");
                rc = 1;
                return;
            }
        }
    }

    public Integer getRc() {return rc;}

}