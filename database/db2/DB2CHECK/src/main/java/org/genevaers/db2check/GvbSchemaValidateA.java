// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE STORED PROCEDURES

package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import com.ibm.db2.jcc.a.f;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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

        logger.info("GvbSchemaValidateA: checking stored procedures for schema: " + schema_mask);
        //System.out.println ("**** GvbSchemaValidateA: checking stored procedures for schema: " + schema_mask);

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
            //System.out.println("**** Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery(SQLstmt);
            logger.fine("Created JDBC ResultSet object");
            //System.out.println("**** Created JDBC ResultSet object");

            fwriter.write("\nStored Procedures Validation Report for schema: " + schema_mask + "\n\n");

            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
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

                    // report on schema correctness
//                    fwriter.write(schema + " " + nname + " " + vversion + "\n");
                    //System.out.println(schema + " " + nname + " " + vversion ); // + "\n " + ttext);
//                    fwriter.write(digestType + ": " + encodedHash + "\n");
                    //System.out.println(digestType + ": " + encodedHash);
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
                        //System.out.println("HASH value mismatch for stored procedure: " + nname);
                        //System.out.println("No stored hash value");
                        fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                        //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                        match = false;
                    }
                    else
                    {
                        if ( hashvalue.equals(encodedHash)) {
                            fwriter.write("HASH value matches\n");
                            //System.out.println("HASH value matches for stored procedure: " + nname);
                        }
                        else
                        {
                            logger.warning("HASH value mismatch for stored procedure: " + nname);
                            fwriter.write("HASH value mismatch for stored procedure: " + nname + "\n");
                            //System.out.println("HASH value mismatch for stored procedure: " + nname);
                            fwriter.write("Computed hash value: " + encodedHash + "\n");
                            fwriter.write("Stored hash value  : " + hashvalue + "\n");
                            //System.out.println("Stored hash value: " + hashvalue);
                            fwriter.write("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
                            //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                            match = false;
                        }
                    }
                }
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
            logger.severe("IO exception encountered in GvbSchemaValidateA");
            //System.out.println("IO exception encountered in GvbSchemaValidateA");
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

        if (makeHash) {
            logger.info("Stored procedure digest hashmap created");
            //System.out.println("\nStored procedure digest hashmap created\n");
            rc = 2;
            return;
        } else {
            if ( match )
            {
                logger.info("All stored procedure definitions match");
                //System.out.println("\nAll stored procedure definitions match.\n");
                rc = 0;
                return;
            }
            else
            {
                logger.warning("One or more stored procedures do not match expected definitions ***");
                //System.out.println("\nOne or more stored procedures do not match expected definitions !!!\n");
                rc = 1;
                return;
            }
        }
    }

    public Integer getRc() {return rc;}

}