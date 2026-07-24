package org.genevaers.db2check;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//import java.lang.System.Logger;
import java.util.HashMap;
import java.io.File;
import java.util.logging.*;

import java.sql.*;

// custom handle
class GVBFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        // Customize the log format: [LEVEL] Date - Methgod - Message
//        return String.format("(%1$-7s) %2$tF %2$tT - %3$s - %4$s %n",
        return String.format("(%1$-7s) %2$tF %2$tT - %3$s %n",
                record.getLevel().getName(),
                new Date(record.getMillis()),
                record.getMessage());
    }
}
//                 record.getSourceMethodName(),

public class GvbSchemaValidateMain {
    @SuppressWarnings("resource")

    private static final Logger logger = Logger.getLogger(GvbSchemaValidateMain.class.getName());

    public static void main(String[] args)
    {
        HashMap<String, String> spmap = new HashMap<>(30); // stored procedures digest
        HashMap<String, String> tbmap = new HashMap<>(100); // table/column digest
        HashMap<String, String> ixmap = new HashMap<>(100); // index digest
        HashMap<String, String> fkmap = new HashMap<>(100); // foreign key digest

        Integer ii = 0;
        Integer finalI = 0;
        BufferedReader reader;
        String url = "";
        String user = "";
        String digestType = "SHA3-512";
        String password = "";
        String schema_mask = "";
        Boolean makeHash = false;
        Boolean makeDef = false;
        Boolean makeF = true;
        BufferedWriter fwriter = null; // general output
        BufferedWriter hwriter = null; // digest output
        BufferedWriter[] dwriter = new BufferedWriter[4]; // Definition files: stored procedures, table/columns, indexes and foreign keys
        dwriter[0] = null;
        dwriter[1] = null;
        dwriter[2] = null;
        dwriter[3] = null;
        Connection con;

        String userhome = System.getProperty("user.home");

        ConsoleHandler handler = new ConsoleHandler(); //Create a handler (where the logs go)
        handler.setFormatter(new GVBFormatter()); //Attach your custom formatter
        logger.setUseParentHandlers(false); //Disable default parent handlers to avoid duplicate logs
        logger.addHandler(handler); //Add the handler to your logger and set the level
        logger.setLevel(Level.ALL); //Apply to all levels
    
        logger.info("Running GvbSchemaValidateMain: checking DB2 Schema.");

        Integer nArgs =args.length;
        Integer n;

        // parse
        // command line argument[s]
        for (n = 0; n < nArgs; n++) {
            if (args[n].substring(0,1).equals("-")) {
                switch( args[n].substring(1,2))
                 {
                    // generate hash map -- does NOT validate the schema
                    case "A":
                        makeHash = true;
                        break;
                    // generate DDL statement -- available in all cases
                    case "D":
                        makeDef = true;
                        break;
                    case "h":
                        logger.info("-D (-D (write schema definitions)\n-A (create schema digest map)");
                        return;
                    default:
                        break;
                }
            }
        }

        if (makeHash) {
            logger.info("Option set to generate Schema digest from DB2 catalog");
        }
        if (makeDef) {
            logger.info("Option set to generate Schema definitions from DB2 catalog");
        }

        // read configurtion information from home directory
        try {
            ii = 0;
            reader = new BufferedReader(new FileReader(userhome + "/db2check.config"));
			String line = reader.readLine();
			while (line != null) {
                if ( 0 == ii) {
                    user = line.substring(0);
                }
                else
                {
                    if ( 1 == ii ) {
                        password = line.substring(0);
                    }
                    else
                    {
                        if ( 2 == ii ) {
                            url = line.substring(0);
                        }
                        else
                        {
                            if ( 3 == ii ) {
                                schema_mask = line.substring(0);
                            }
                        }
                    }
                }
                // read next line
                ii = ii + 1;
			    line = reader.readLine();
			}
            finalI = ii;
			reader.close();
		} catch (IOException e) {
            logger.severe("IO exception encountered in GvbSchemaValidateMain reading configuration file: " + e.getMessage());
            return;
		}
        // configuration information has been read
        logger.fine("User: " + user + " Url: " + url + " Schema mask: " + schema_mask + ". Config lines read: " + finalI);


        // read Digest information from home directory
        if (makeHash) {
            // check if our directory exists: if it doesn't create it, if it does fine
            File newDir = new File(userhome+"/GenevaERS");
            if (newDir.mkdir()) {
                logger.info("Directory: " + userhome+"/GenevaERS" + " created");
            } else {
                // this is ok too
            }
        } else {
            try {
                Integer State = -1;

                // check if our directory exists: it MUST
                File newDir = new File(userhome+"/GenevaERS");
                if (newDir.mkdir()) {
                    logger.info("Directory: " + userhome + "/GenevaERS" + " did not previously exist");
                    logger.severe("Digest file does not exist. Terminating application");
                    return;
                } else {
                    // this is the happy path
                }

                ii = 0;
                reader = new BufferedReader(new FileReader(userhome + "/GenevaERS/SchemaDigest.txt"));
		    	String line = reader.readLine();
			    while (line != null) {
                    if (line.contains("// Populate digest map of stored procedures")) {
                        State = 1;
                    }
                    else {
                        if (line.contains("// Populate digest map of tables")) {
                            State = 2;
                        }
                        else {
                            if (line.contains("// Populate digest map of indexes")) {
                                State = 3;
                            }
                            else {
                                if (line.contains("// Populate digest map of foreign")) {
                                    State = 4;
                                }
                                else {
                                    String[] values = line.split(","); // Split by comma
                                    switch (State) {
                                        case -1:
                                            State = 0;
                                            break;
                                        case 1:
                                            spmap.put(values[0], values[1]);
                                            break;
                                        case 2:
                                            tbmap.put(values[0], values[1]);
                                            break;
                                        case 3:
                                            ixmap.put(values[0], values[1]);
                                            break;
                                        case 4:
                                            fkmap.put(values[0], values[1]);
                                            break;
                                        default:
                                            logger.severe("Invalid State:" + State + " Line: " + ii + " record: " + line);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    // read next line
                    ii = ii + 1;
			        line = reader.readLine();
			    }
                finalI = ii;
			    reader.close();
		    } catch (IOException e) {
                logger.severe("IO exception encountered in GvbSchemaValidateMain reading schema digest file: " + e.getMessage());
                return;
		    }
        }

        // load db2 jdbc
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            logger.severe("Error encountered loading DB2 SQLJ driver: " + e.getMessage());
            return;
        }
        logger.fine("Loaded the JDBC driver");

        // Create the connection using the IBM Data Server Driver for JDBC and SQLJ and open output file[s]
        try {
            con = DriverManager.getConnection (url, user, password);
            con.setAutoCommit(false);
            logger.fine("Created a JDBC connection to the data source\n");

            if ( makeF ) {
                fwriter = new BufferedWriter(new FileWriter(userhome + "/GenevaERS/Schema_report.txt"));
                fwriter.write("GenevaERS DB2 Schema Report for: " + schema_mask + "\n");
            }

            if (makeHash) {
                hwriter = new BufferedWriter(new FileWriter(userhome + "/GenevaERS/SchemaDigest.txt"));
            }

            if (makeDef) {
                dwriter[0] = new BufferedWriter(new FileWriter(userhome + "/GenevaERS/StoredProcedures.txt")); // stored procedures
                dwriter[1] = new BufferedWriter(new FileWriter(userhome + "/GenevaERS/Tabledata.txt")); // tables and columns
                dwriter[2] = new BufferedWriter(new FileWriter(userhome + "/GenevaERS/Indexdata.txt")); // indexes
                dwriter[3] = new BufferedWriter(new FileWriter(userhome + "/GenevaERS/Foreignkeydata.txt")); // foreign assets
            }

            // construct configuration object
            GvbSchemaConfig sc= new GvbSchemaConfig(digestType, con, schema_mask, makeHash, makeDef, fwriter, hwriter, dwriter, spmap, tbmap, ixmap, fkmap);

            // call stored procedure validation
            GvbSchemaValidateA mA = new GvbSchemaValidateA(sc);

            // call table column validation
            GvbSchemaValidateB mB = new GvbSchemaValidateB(sc);

            // call table column validation
            GvbSchemaValidateC mC = new GvbSchemaValidateC(sc);

            // call table column validation
            GvbSchemaValidateD mD = new GvbSchemaValidateD(sc);

            Integer maxRc = Math.max( mA.getRc(), Math.max( mB.getRc(), Math.max ( mC.getRc(), mD.getRc() )));

            switch ( maxRc ) {
                case 0:
                    logger.info("All parts of schema validated successfully\n");
                    fwriter.write("\nAll parts of schema validated successfully\n");
                    break;
                case 1:
                    logger.warning("One or more parts of schema failed validation\n");
                    fwriter.write("\nOne or more parts of schema failed validation\n");
                    break;
                case 2:
                    logger.info("Entire schema digest map created\n");
                    fwriter.write("\nEntire schema digest map created\n");
                    break;
                case 3:
                    logger.info("Some or all DB2 catalog data not found\n");
                    fwriter.write("\nSome or all DB2 catalog data not found\n");
                    break;
                case 4:
                    logger.severe("DB2 SQL error\n");
                    fwriter.write("\nDB2 SQL error\n");
                    break;
                case 8:
                    logger.severe("IO error\n");
                    fwriter.write("\nIO error\n");
                    break;
                case 12:
                    logger.severe("IO and DB2 SQL error\n");
                    fwriter.write("\nIO and DB2 SQL error\n");
                    break;
                default:
                    logger.severe("Incorrect max return code: " + maxRc + "\n");
                    fwriter.write("\nIncorrect max return code: " + maxRc + "\n");
                    break;
            }

            if ( makeF ) {
                fwriter.close();
            }

            if (makeHash) {
                hwriter.close();
            }

            if (makeDef) {
                dwriter[0].close();
                dwriter[1].close();
                dwriter[2].close();
                dwriter[3].close();
            }

            // Connection must be on a unit-of-work boundary to allow close
            con.commit();
            logger.fine("SQL statements completed on transaction boundary");
      
            // Close the connection
            con.close();
            logger.fine(userhome + "Disconnected from data source");
            logger.fine("JDBC completed - no DB2 errors");

        } catch (SQLException e) {
                logger.severe("SQLSTATE: " + e.getSQLState() + " creating database connection for: " + url + e.getMessage());
                return;

        } catch (IOException e) {
                logger.severe("IO exception encountered in GvbSchemaValidateMain" + e.getMessage());
                return;
        }
        return;
    }
}
