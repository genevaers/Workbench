// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE FOREIGN KEYS

package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import java.sql.*;

public class GvbSchemaValidateD {

    private Integer rc;
    private static final Logger logger = Logger.getLogger(GvbSchemaValidateC.class.getName());

    public GvbSchemaValidateD(GvbSchemaConfig sc)
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
        HashMap<String, String> fkmap = sc.getFkmap();

        String schema;
        String tname;
        String rname;
        String cname;
        Integer colno = 0;
        Integer colseq = 0;
        String lastTab = "";

        String reftname = "";
        String ixname = "";
    
        Statement stmt;
        ResultSet rs;
        Statement stmt2;
        ResultSet rs2;

        logger.info("GvbSchemaValidateD: processing foreign keys for schema: " + schema_mask);

        String SQLstmt = "SELECT CREATOR, TBNAME, RELNAME, COLNAME, COLNO, COLSEQ FROM SYSIBM.SYSFOREIGNKEYS WHERE CREATOR LIKE '" + schema_mask + "' ORDER BY TBNAME, RELNAME, COLSEQ;";

        try {
            StringBuilder sb = new StringBuilder("");
            // Print generated digest value to separate file if requested -A
            if ( makeHash ) {
                hwriter.write("        // HashMap<String, String> fkmap = new HashMap<>(100);\n");
                hwriter.write("        // Populate digest map of foreign keys using " + digestType +"\n");
            }

            // Create the Statement
            stmt = con.createStatement();
            logger.fine("Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery(SQLstmt);
            logger.fine("Created JDBC ResultSet object");

            fwriter.write("\nForeign key Validation Report by table for schema: " + schema_mask + "\n\n");

            boolean hasData = false;
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                hasData = true;
                schema = rs.getString(1);
                tname = rs.getString(2);
                rname = rs.getString(3);
                cname = rs.getString(4);
                colno = rs.getInt(5);
                colseq = rs.getInt(6);

                // There should be only one of these rows: by definition
                String SQLstmt2 = "SELECT REFTBNAME, IXNAME, CREATOR, RELNAME FROM SYSIBM.SYSRELS WHERE CREATOR LIKE '" + schema_mask + "' AND TBNAME = '" + tname + "' AND RELNAME = '" + rname + "' FETCH FIRST 1 ROW ONLY;";
                try {
                    stmt2 = con.createStatement();
                    rs2 = stmt2.executeQuery(SQLstmt2);
                    while (rs2.next()) {
                        reftname = rs2.getString(1);
                        ixname = rs2.getString(2);
                        if (ixname.equals("")) {
                            ixname = "Primary Key";
                        }
                    }
                    rs2.close();
                    stmt2.close();
                } catch (SQLException e) {
                    logger.severe("SQLSTATE: " + e.getSQLState() + " executing: " + SQLstmt2 + e.getMessage());
                    rc = 4;
                    return;
                }

                if ( lastTab.equals(tname)) {
                    if ( makeDef ) {
                        dwriter[3].write(schema + " " + tname + " " + rname + " " + cname + " " + colno + " " + colseq + " " + reftname + " " + ixname + "\n");
                    }
                    sb.append(" " + tname + " " + rname + " " + cname + " " + colno + " " + colseq + " " + reftname + " " + ixname);
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
                            fwriter.write("Foreign keys of table: " + tname + " Digest: " + digestType + ": " + encodedHash + "\n");
                            String hashvalue = fkmap.get(tname);

                            if (hashvalue == null) {
                                logger.warning("HASH value mismatch for foreign key of table: " + tname + " - no stored hash value");
                                fwriter.write("HASH value mismatch for foreign key of table: " + tname + " - no stored hash value\n");
                                fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                match = false;
                            }
                            else {
                                if ( hashvalue.equals(encodedHash) ) {
                                    fwriter.write("HASH value matches for foreign keys of table: " + tname + "\n");
                                }
                                else
                                {
                                    logger.warning("HASH value mismatch for foreign keys of table: " + tname);
                                    fwriter.write("HASH value mismatch for foreign keys of table: " + tname + "\n");
                                    fwriter.write("Computed hash value: " + encodedHash + "\n");
                                    fwriter.write("Stored hash value  : " + hashvalue + "\n");
                                    fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                    match = false;
                                }
                            }
                        }
                    }

                    if (makeDef) {
                        dwriter[3].write("\n" + schema + " TABLE: " + tname+" ============================================\n");
                        dwriter[3].write(schema + " " + tname + " " + rname + " " + cname + " " + colno + " " + colseq + " " + reftname + " " + ixname + "\n");
                    }
                    
                    sb.delete(0, sb.length());
                    sb.append(" " + tname + " " + rname + " " + cname + " " + colno + " " + colseq + " " + reftname + " " + ixname);
                }
                lastTab = tname;
            }

            if (hasData) {
                logger.fine("Fetched all rows from JDBC ResultSet");
            } else {
                logger.severe("HASH value cannot be formulated - no foreign keys found for schema: " + schema_mask);
                fwriter.write("HASH value cannot be formulated - no foreign keys found for schema: " + schema_mask);
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
            logger.severe("IO exception encountered in GvbSchemaValidateD");
            rc = 8;
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Digest algorithm: " + digestType + " not available");
            rc = 12;
            return;
        }
        
        if ( makeHash ) {
            if (match) {
                logger.info("Foreign key digest hashmap created");
                rc = 2;
            } else {
                rc = 3;
            }
            return;
        }
        else {
            if ( match ) {
                logger.info("All foreign key definitions match.");  
                rc = 0;
                return;
            }
            else {
                logger.warning("One or more foreign keys do not match expected definitions ***");
                rc = 1;
                return;
            }
        } 
    }

    public Integer getRc() {return rc;}

}