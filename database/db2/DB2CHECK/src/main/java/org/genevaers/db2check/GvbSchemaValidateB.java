// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE TABLES AND COLUMNS

package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import java.sql.*;

public class GvbSchemaValidateB {

    private Integer rc;
    private static final Logger logger = Logger.getLogger(GvbSchemaValidateB.class.getName());

    public GvbSchemaValidateB(GvbSchemaConfig sc)
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
        HashMap<String, String> tbmap = sc.getTbmap();

        String schema;
        String tname;
        String cname;
        String typename;
        int    length;
        String lastTab = "";

        Statement stmt;
        ResultSet rs;

        ConsoleHandler handler = new ConsoleHandler(); //Create a handler (where the logs go)
        handler.setFormatter(new GVBFormatter()); //Attach your custom formatter
        logger.setUseParentHandlers(false); //Disable default parent handlers to avoid duplicate logs
        logger.addHandler(handler); //Add the handler to your logger and set the level
        logger.setLevel(Level.ALL); //Apply to all levels

        logger.info("GvbSchemaValidateB: processing tables and columns for schema: " + schema_mask);

        String SQLstmt = "SELECT TBCREATOR, TBNAME, NAME, COLTYPE, LENGTH FROM SYSIBM.SYSCOLUMNS WHERE TBCREATOR LIKE '" + schema_mask + "' ORDER BY TBNAME, NAME";

        try {
            StringBuilder sb = new StringBuilder("");
            // Print generated digest value to separate file if requested -A
            if ( makeHash ) {
                hwriter.write("        // HashMap<String, String> tbmap = new HashMap<>(100);\n");
                hwriter.write("        // Populate digest map of tables using " + digestType +"\n");
            }

            // Create the SQL statement
            stmt = con.createStatement();
            logger.fine("Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery(SQLstmt);
            logger.fine("Created JDBC ResultSet object");

            fwriter.write("\nTable and Column Validation Report for schema: " + schema_mask + "\n\n");

            boolean hasData = false;
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                hasData = true;
                schema = rs.getString(1);
                tname = rs.getString(2);
                cname = rs.getString(3);
                typename = rs.getString(4);
                length = rs.getInt(5);

                if ( lastTab.equals(tname)) {
                    if ( makeDef ) {
                        dwriter[1].write(schema + " " + tname + " " + cname + " " + typename + " " + length + "\n");
                    }
                    //sb.append(schema + " " + tname + " " + cname + " " + typename + " " + length);
                    // don't include schema name in hash value as this will prevent choice of different schema name
                    sb.append(" " + tname + " " + cname + " " + typename + " " + length);
                }
                else{
                    if (sb.length() > 0 ) {
                        byte[] hashedBytes = md.digest((sb.toString()).getBytes());
                        String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                        if ( makeHash ) {
                            hwriter.write(tname + "," + encodedHash); //populate digest hash map
                            hwriter.write("\n");
                        }
                        else {
                            // report on schema correctness
                            fwriter.write("Table: " + tname + " Digest: " + digestType + ": " + encodedHash + "\n");
                            String hashvalue = tbmap.get(tname);
                            if (hashvalue == null)
                            {
                                logger.warning("HASH value mismatch for table: " + tname + " - no stored hash value");
                                fwriter.write("HASH value mismatch for table: " + tname + " - no stored hash value\n");
                                fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                match = false;
                            }
                            else
                            {
                                if ( hashvalue.equals(encodedHash))
                                {
                                    fwriter.write("HASH value matches for table: " + tname + "\n");
                                }
                                else
                                {
                                    logger.warning("HASH value mismatch for table: " + tname);
                                    fwriter.write("HASH value mismatch for table: " + tname + "\n");
                                    fwriter.write("Computed hash value: " + encodedHash + "\n");
                                    fwriter.write("Stored hash value  : " + hashvalue + "\n");
                                    fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                                    match = false;
                                }
                            }
                        }
                    }

                    // Print all of the definition data to separate file dwriter if requested -D
                    if ( makeDef ) {
                        dwriter[1].write("\n" + schema + " TABLE: " + tname+" ============================================\n");
                        dwriter[1].write(schema + " " + tname + " " + cname + " " + typename + " " + length + "\n");
                    }
                    
                    sb.delete(0, sb.length());
                    // sb.append(schema + " " + tname + " " + cname + " " + typename + " " + length);
                    // don't include schema name in hash value as this will prevent choice of different schema name
                    sb.append(" " + tname + " " + cname + " " + typename + " " + length);
                }
                lastTab = tname;
            }

            if (hasData) {
                logger.fine("Fetched all rows from JDBC ResultSet");
            } else {
                logger.severe("HASH value cannot be formulated - no tables found for schema: " + schema_mask);
                fwriter.write("HASH value cannot be formulated - no tables found for schema: " + schema_mask);
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
            logger.severe("IO exception encountered in GvbSchemaValidateB");
            rc = 8;
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Digest algorithm: " + digestType + " not available");
            rc = 12;
            return;
        }
        
        if ( makeHash ) {
            if (match) {
                logger.info("Table digest hashmap created");
                rc = 2;
            } else {
                rc = 3;
            }
            return;
        }
        else
        {
            if ( match )
            {
                logger.info("All table definitions match");
                rc = 0;
                return;
            }
            else
            {
                logger.warning("One or more tables do not match expected definitions ***");
                rc = 1;
                return;
            }
        }
    }

    public Integer getRc() {return rc;}

}