// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE STORED PROCEDURES

package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.*;

import com.ibm.db2.jcc.a.f;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// import java.lang.StackWalker.StackFrame;

import java.sql.*;

public class GvbSchemaValidateA {

    private Integer rc;
    private static final Logger logger = Logger.getLogger(GvbSchemaValidateA.class.getName());

    public GvbSchemaValidateA(GvbSchemaConfig sc)
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
        HashMap<String, String> spmap = sc.getSpmap();

        String schema;
        String nname;
        String vversion;
        String ttext;
        Statement stmt;
        ResultSet rs;

        ConsoleHandler handler = new ConsoleHandler(); //Create a handler (where the logs go)
        handler.setFormatter(new GVBFormatter()); //Attach your custom formatter
        logger.setUseParentHandlers(false); //Disable default parent handlers to avoid duplicate logs
        logger.addHandler(handler); //Add the handler to your logger and set the level
        logger.setLevel(Level.ALL); //Apply to all levels

        logger.info("GvbSchemaValidateA: processing stored procedures for schema: " + schema_mask);

        String SQLstmt = "SELECT SCHEMA, NAME, VERSION, TEXT FROM SYSIBM.SYSROUTINES WHERE SCHEMA LIKE '"+schema_mask+"' ORDER BY SCHEMA, NAME";

        try {
            // Print generated digest value to separate file if requested -A
            if ( makeHash ) {
                hwriter.write("        // HashMap<String, String> spmap = new HashMap<>(30);\n");
                hwriter.write("        // Populate digest map of stored procedures using " + digestType +"\n");
            } 

            // Create the SQL statement
            stmt = con.createStatement();
            logger.fine("Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery(SQLstmt);
            logger.fine("Created JDBC ResultSet object");

            fwriter.write("\nStored Procedures Validation Report for schema: " + schema_mask + "\n\n");

            boolean hasData = false;
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                hasData = true;
                schema = rs.getString(1);
                nname = rs.getString(2);
                vversion = rs.getString(3);
                ttext = rs.getString(4);
                
                byte[] hashedBytes = md.digest(ttext.getBytes());
                String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                if (makeHash) {
                    hwriter.write(nname+ "," + encodedHash);  //populate digest hash map
                    hwriter.write("\n");
                }
                else
                {
                    // report on schema correctness
                    fwriter.write("Stored Procedure: " + nname + " " + vversion + " Digest: " + digestType + ": " + encodedHash + "\n");
                }

                // Print all of the definition data to separate file dwriter if requested -D
                if (makeDef) {
                    dwriter[0].write(schema+":"+nname+"============================================\n");
                    dwriter[0].write(ttext);
                    dwriter[0].write("\n");
                }

                if (makeHash) {
                    // Do nothing
                }
                else {
                    // Report on correctness of schema definitions
                    String hashvalue = spmap.get(nname);
                    if ( hashvalue == null) {
                        logger.warning("HASH value mismatch for stored procedure: " + nname + " - no stored hash value");
                        fwriter.write("HASH value mismatch for stored procedure: " + nname + " - no stored hash value\n"); 
                        fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                        match = false;
                    }
                    else
                    {
                        if ( hashvalue.equals(encodedHash)) {
                            fwriter.write("HASH value matches\n");
                        }
                        else
                        {
                            logger.warning("HASH value mismatch for stored procedure: " + nname);
                            fwriter.write("HASH value mismatch for stored procedure: " + nname + "\n");
                            fwriter.write("Computed hash value: " + encodedHash + "\n");
                            fwriter.write("Stored hash value  : " + hashvalue + "\n");
                            fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                            match = false;
                        }
                    }
                }
            }

            if (hasData) {
                logger.fine("Fetched all rows from JDBC ResultSet");
            } else {
                logger.severe("HASH value cannot be formulated - no stored procedures found for schema: " + schema_mask);
                fwriter.write("HASH value cannot be formulated - no stored procedures found for schema: " + schema_mask);
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
            logger.severe("IO exception encountered in GvbSchemaValidateA");
            rc = 8;
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Digest algorithm: " + digestType + " not available");
            rc = 12;
            return;
        }

        if (makeHash) {
            if (match) {
                logger.info("Stored procedure digest hashmap created");
                rc = 2;
            } else {
                rc = 3;
            }
            return;
        } else {
            if ( match )
            {
                logger.info("All stored procedure definitions match");
                rc = 0;
                return;
            }
            else
            {
                logger.warning("One or more stored procedures do not match expected definitions ***");
                rc = 1;
                return;
            }
        }
    }

    public Integer getRc() {return rc;}

}