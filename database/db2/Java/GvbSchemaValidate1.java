// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE STORED PROCEDURES

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.sql.*;
 
public class GvbSchemaValidate1 {
    public static void main(String[] args)
    {
        HashMap<String, String> spmap = new HashMap<>(30);
        Integer i = 0;
        Integer finalI = 0;
        Integer ii = 0;

        BufferedReader reader;
        BufferedWriter writer;

        String url = ""; //"jdbc:db2://SP13.pok.stglabs.ibm.com:5033/DM12";
        String user = "";
        String digestType = "SHA3-512";
        String password = "";
        String schema_mask = " ";
        String schema;
        String nname;
        String vversion;
        String ttext;
        Connection con;
        Statement stmt;
        ResultSet rs;

        System.out.println ("**** Running GvbSchemaValidate1: checking stored procedures");

        Integer nArgs =args.length;
        Integer n;
        Boolean makeHash = false;

        for (n = 0; n < nArgs; n++) {
            if (args[n].substring(0,1).equals("-")) {
                switch( args[n].substring(1,2)) {
                    case "A":
                        makeHash = true;
                        break;
                    default:
                        break;
                }
            }
        }
        System.out.println("Option to generate hashmap code lines from DB2 catalog: " + makeHash);

        try {
            reader = new BufferedReader(new FileReader(System.getenv("HOMEPATH")+"\\password.txt"));
			String line = reader.readLine();
			while (line != null) {
                if ( 0 == i) {
                    user = line.substring(0);
                }
                else
                {
                    if ( 1 == i ) {
                        password = line.substring(0);
                    }
                    else
                    {
                        if ( 2 == i ) {
                            url = line.substring(0);
                        }
                        else
                        {
                            if ( 3 == i ) {
                                schema_mask = line.substring(0);
                            }
                        }
                    }
                }
                // read next line
                i = i + 1;
			    line = reader.readLine();
			}
            finalI = i;
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        System.out.println("User: " + user + " Url: " + url + " Schema mask: " + schema_mask);

        // Populate map of stored procedures and known hash values
        spmap.put("CLEARENVIRON","mPu5PXZbNzdO7I4LrtI5Qj2Lw933w4Tel4s1lQr+Vtb8EaA68KNBSgztVcoEEvDr6+vkyzUtcHLM/7QULwTXeQ==");
        spmap.put("DELETEENVIRON","F11HCOtgZTliw6Wl1ITJqqDCqsA9ELioVuDCLfzl4RyOTbRl7vXh17oUaj8H+gqepxUcrZ9rB4RoAbi/I823uQ==");
        spmap.put("ENVDEPENDENCY","hCTnsEq6+dKLdBWsuZkfj5dfXcFymJbC6lIIYoggKusMskXtpgyG9BB895ZNrELMj0A7cGGS1p+nGbiWAsccOg==");
        spmap.put("ENVINITVALUES","o7tSRfwAu0fBOP1wlE0rH+leL05DB2seI+QaXOcTcSbntYpWYgCAZ5hd3houDwxa4OHDXvfAkSDkmoNSFoVXWg==");
        spmap.put("GENLFDEPS","DSIa88pNgFSzWuzDC2dxD7nyFqpMUqlAvGfNif7Lw0Yu6u1hKUF2Uvx1BBKFA7tgUWkn5KHZlqFlyHcOallt6w==");
        spmap.put("GENLPDEPS","7kIFUpsC9OrLwPG5Wr2KE08jgEo9hvUikepzRuQ9Zwg8UFg2c2blunZJHSRG3OwzHefEKSzkYEwZ6xWpIZS2hA==");
        spmap.put("GENLRDEPS","V/qkBhiERr0/6XDDUPZBTJz69fH9Ra9f+XqQ9YHurz/dKTQ+wPd1+HSkbeYlCTqOYni4fEiu5gIqyjqngtMXFA==");
        spmap.put("GENPFDEPS","Mkvnx8gPB3Pln2PjWF3cVoNcZvfdJhldKLujmtcU4VRUQSmOFi2K1S/Bm0xem9Ux6j3UiexDcfJDX0ezREUf2Q==");
        spmap.put("GENVIEWDEPS","V+Vku1+8s4k3eT9a6zByl+lsco0LhOad4nbd3UYFBWTO1LiehqfOlUlPvD1qefrjwQys/UokdpjkP+P/Kl6aGg==");
        spmap.put("GETCOMPDEPS","9ozrJVUefHfywb/A1oRb1Wk6L40StIvqtGWCkpS7Tl+c99ln/1nBQyuRSDw1FksOBmj68iKDsLhViKVtDwzBig==");
        spmap.put("GETDEPSEXIT","K/t6UAhNFs27J7wXTN2QAs3twBbEU21TJRzrf+12tDZ1fpQ/27ZMx2SBLYFS300aZ6hOJCFVV52rDG0oAKI6rw==");
        spmap.put("GETDEPSFIELD","xZVPKk55qYgYirQx3iAl686kUhKBZuEZDL0tf24mZXc7gLyl3h/Bd5DUck9kLF3Vv+/NSIbYjgzBdW6alZvHMg==");
        spmap.put("GETDEPSLF","bAkwzWRY+zCXsTlgyratp8iqkL5QHYX4moqi1bmgzH95ZKRNT7dKHRoCAZCsRg54xCeotIaGA3V85r51FkGAdg==");
        spmap.put("GETDEPSLP","L0LBpa638C9TG7QCUyBIobooNFpjnm7x/Yd6Cx+kydIxZsTnAnb5i4kZr1pP8RyDvFLGkzXd0AxrDF91Npjnpg==");
        spmap.put("GETDEPSLR","e3KN9ECgCVMwXYV3vhpODGb48LRW55a1lSpyMwRETco9HokCe9dA/yUXo+2oHbgOUdamUeAlBWMLqWvyGYQjag==");
        spmap.put("GETDEPSPF","EtKfDNa9fgAakL+PeAZP/0ejN1jLEPul/3JZtnL06vcYrv1enOAS/zdmYUoJUtYAHdL1wrRoiszjZE3FKXlBYw==");
        spmap.put("GETDEPSVIEW","BYz48pGWy5K5C//KQUVdD+mcMZfp40kcR8BbV8xFYmvMPgQJzh+DkzlKYilm+e0WoRYWgAk9Mss5z4gG8m+0dg==");
        spmap.put("GETVERSION","DJ4O2MPx27ETrFrXhMVjOC+x2YJL30ri+yz84OPNb3LDJwo3tvr35GqlNJaCVI9KMJDqX6ko4V2lhf4kdznwDQ==");
        spmap.put("GETVIEWPROPS","Dwvz1AQMvmoJsg26eHXGJDp+H6VsB+cOn5zEVUXCZuGJilTOfZKeaZ7HNddGfO9kSrHuuQfuZ5gL2gvdqwmfnQ==");
        spmap.put("INSLPSRCFLD","YtuTvBTTcRr4MyhD/H4AVydXhtvSJaWCmR3gBsBlC3HuPqznPny+7Gbyyty9mThkuvTVbVNMvUMyi82JUeFK6Q==");
        spmap.put("INSLRFIELD","6cQiFdF9DiETF0XLYEOyLjiB6XH20FOrx0xoXEJORYahDBzxYrRus0AcwV7kM8JhfxO/JJyohQirQb9bGGjTNQ==");
        spmap.put("INSSECGRPRGHT","kAccwoYWAeElnhUNG/Xl+RSEabrnHfEfy64bL4oMeKZvx3kqiLVQDJD+UI0bYgOVqG1DyU0xxQeqEBOsjWJgow==");
        spmap.put("INSVIEWCOL","+WabW/hfdcvupIcB6J2ee+89qfjKZVyGEcBLEKpDW412ambAdVHKcmzTmTqdq0NVyHpfIOpxezOWHQMfIUkwMg==");
        spmap.put("INSVIEWCOLSRC","nzjOfwS0Ma53IOPV8Nx+bBmIatZk17HY+pOSV3iCqLxNl9NYGrV/nY19Ar5B9Y8VaC3hirI4GvRM5ANWSKzFjA==");
        spmap.put("UPDLRFIELD","LIT2v0V8gX82XxK5mb967fKB52I75C1Iyl6xCbg+n3FNQHBTdI4+gMsFh43CJPGA6jqVnAGHalBxLlkKKgtPqw==");
        spmap.put("UPDVIEWCOL","LQrgSsvV2kW/alGTazBZhLJlL3/By3e/ZDyDANy6BVJ5W5etr6CGvRCFaLhYNEoy10yrguYDhuO1wlZKR7ux9Q==");
        spmap.put("UPDVIEWCOLSRC","zZXmNUz0i1PfCNKIQJOv4Qo4Xp9vQBYGG5eDdV2Tr5zzoZlHIzEmWCAjdqYUVPdXSerUkH9z4sDJ/arkpKeDKA==");
        spmap.put("UPDVWLOGIC","DrQz7pOzu+krRHV2lw2QXtxuwKoMw8OiXOfGD7pSRkIXDwlYRfkwkdj5FQfIvt3XHWE81U7ynK1cwhLJg90NoA==");

        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("**** Loaded the JDBC driver");

        // Create the connection using the IBM Data Server Driver for JDBC and SQLJ
        try {
            con = DriverManager.getConnection (url, user, password);
            con.setAutoCommit(false);
            System.out.println("**** Created a JDBC connection to the data source");

            // Create the Statement
            stmt = con.createStatement();
            System.out.println("**** Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery("SELECT SCHEMA, NAME, VERSION, TEXT FROM SYSIBM.SYSROUTINES WHERE SCHEMA LIKE '"+schema_mask+"' ORDER BY SCHEMA, NAME");
            System.out.println("**** Created JDBC ResultSet object");

            // Print all of the data
            writer = new BufferedWriter(new FileWriter(System.getenv("HOMEPATH")+"\\StoredProcedures.txt"));
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                schema = rs.getString(1);
                nname = rs.getString(2);
                vversion = rs.getString(3);
                ttext = rs.getString(4);
                
                byte[] hashedBytes = md.digest(ttext.getBytes());
                String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                if (!makeHash) {
                    System.out.println(schema + " " + nname + " " + vversion ); // + "\n " + ttext);
                    System.out.println(digestType + ": " + encodedHash);
                }
                else
                {
                    System.out.println("        spmap.put(\"" + nname + "\"," + "\"" + encodedHash+"\"); "); //populate hash map
                }

                writer.write(schema+":"+nname+"============================================\n");
                writer.write(ttext);
                writer.write("\n");

                if (!makeHash) {
                    String hashvalue = spmap.get(nname);
                    if ( hashvalue.equals(encodedHash)) {
                        System.out.println("HASH value matches for stored procedure: " + nname);
                    }
                    else
                    {
                        System.out.println("HASH value mismatch for stored procedure: " + nname);
                        System.out.println("Stored hash value: " + hashvalue);
                        System.out.println("====================================================================================");
                        ii = 1;
                    }
                }
            }
            writer.close();
            System.out.println("**** Fetched all rows from JDBC ResultSet");

            // Close the ResultSet
            rs.close();
            System.out.println("**** Closed JDBC ResultSet");
      
            // Close the Statement
            stmt.close();
            System.out.println("**** Closed JDBC Statement");

            // Connection must be on a unit-of-work boundary to allow close
            con.commit();
            System.out.println ( "**** Transaction committed" );
      
            // Close the connection
            con.close();
            System.out.println("**** Disconnected from data source");

            System.out.println("**** JDBC completed - no DB2 errors");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception if MD5 algorithm is not available
            e.printStackTrace();
        }

        if (!makeHash) {
            if ( ii == 0 )
            {
                System.out.println("\nAll stored procedure definitions match.\n");
            }
            else
            {
                System.out.println("\nOne or more stored procedures do not match expected definitions !!!\n");
            }
        } else {
            System.out.println("\nLines of code generated for hashmap\n");
        }
    }
}