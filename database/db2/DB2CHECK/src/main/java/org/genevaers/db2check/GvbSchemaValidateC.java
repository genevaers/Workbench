// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE INDEXES

package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
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

        Statement stmt;
        ResultSet rs;

        logger.info("GvbSchemaValidateC: checking indexes for schema: " + schema_mask);
        //System.out.println ("**** GvbSchemaValidateC: checking indexes for schema: " + schema_mask);

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
            //System.out.println("**** Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery(SQLstmt);
            logger.fine("Created JDBC ResultSet object");
            //System.out.println("**** Created JDBC ResultSet object");

            fwriter.write("\nIndex Validation Report by table for schema: " + schema_mask + "\n\n");

            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                schema = rs.getString(1);
                tname = rs.getString(2);
                iname = rs.getString(3);
                uniqueR = rs.getString(4);

                if ( lastTab.equals(tname)) {
                    if ( makeDef ) {
                        dwriter[2].write(schema + " " + tname + " " + iname + " " + uniqueR + "\n");
                    }
                    sb.append(schema + " " + tname + " " + iname + " " + uniqueR);
                }
                else{
                    if (sb.length() > 0 ) {
                        byte[] hashedBytes = md.digest((sb.toString()).getBytes());
                        String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                        if ( makeHash) {
                            hwriter.write(tname+ "," + encodedHash); //populate hash map
                            hwriter.write("\n");
                        }
                        else {
                            // report on schema correctness
                            fwriter.write("Indexes for Table: " + tname + " Digest: " + digestType + ": " + encodedHash + "\n");
                            //System.out.println("Table: " + tname + " Digest: " + digestType + ": " + encodedHash);
                            String hashvalue = ixmap.get(tname);

                            if (hashvalue == null) {
                                logger.warning("HASH value mismatch for indexes of table: " + tname + " - no stored hash value");
                                fwriter.write("HASH value mismatch for indexes of table: " + tname + " - no stored hash value\n");
                                //System.out.println("HASH value mismatch for table: " + tname);
                                //System.out.println("No stored hash value");
                                fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                match = false;
                            }
                            else {
                                if ( hashvalue.equals(encodedHash) ) {
                                    fwriter.write("HASH value matches for indexes of table: " + tname + "\n");
                                    //System.out.println("HASH value matches for table: " + tname);
                                }
                                else
                                {
                                    logger.warning("HASH value mismatch for indexes of table: " + tname);
                                    fwriter.write("HASH value mismatch for indexes of table: " + tname + "\n");
                                    //System.out.println("HASH value mismatch for table: " + tname);
                                    fwriter.write("Computed hash value: " + encodedHash + "\n");
                                    //System.out.println("Computed hash value: " + encodedHash);
                                    fwriter.write("Stored hash value  : " + hashvalue + "\n");
                                    //System.out.println("Stored hash value: " + hashvalue);
                                    fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                    //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                                    match = false;
                                }
                            }
                        }
                    }

                    if ( makeDef ) {
                        dwriter[2].write("\n" + schema + " TABLE: " + tname+" ============================================\n");
                        dwriter[2].write(schema + " " + tname + " " + iname + " " + uniqueR + "\n");
                    }
                    
                    sb.delete(0, sb.length());
                    sb.append(schema + " " + tname + " " + iname + " " + uniqueR);
                }
                lastTab = tname;
            }
            logger.fine("Fetched all rows from JDBC ResultSet");
            //System.out.println("**** Fetched all rows from JDBC ResultSet");

            // Close the ResultSet
            rs.close();
            logger.fine("Closed JDBC ResultSet");
            //System.out.println("**** Closed JDBC ResultSet");
      
            // Close the Statement
            stmt.close();
            logger.fine("Closed JDBC Statement");
            //System.out.println("**** Closed JDBC Statement");

        } catch (SQLException e) {
            logger.severe("SQLSTATE: " + e.getSQLState() + " executing: " + SQLstmt + e.getMessage());
            //System.out.println("SQLSTATE: " + e.getSQLState() + " executing: " + SQLstmt);
            //e.printStackTrace();
            rc = 4;
            return;
        } catch (IOException e) {
            logger.severe("IO exception encountered in GvbSchemaValidateC");
            //System.out.println("IO exception encountered in GvbSchemaValidateC");
            //e.printStackTrace();
            rc = 8;
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Digest algorithm: " + digestType + " not available");
            //System.out.println("Digest algorithm: " + digestType + " not available");
            //e.printStackTrace();
            rc = 12;
            return;
        }
        
        if ( makeHash ) {
            logger.info("Index digest hashmap created");
            //System.out.println("\nIndex digest hashmap created\n");
            rc = 2;
            return;
        }
        else {
            if ( match )
            {
                logger.info("All index definitions match");
                //System.out.println("\nAll index definitions match.\n");
                rc = 0;
                return;
            }
            else
            {
                logger.warning("One or more indexes do not match expected definitions ***");
                //System.out.println("\nOne or more indexes do not match expected definitions !!!\n");
                rc = 1;
                return;
            }
        }
    }

    public Integer getRc() {return rc;}

}