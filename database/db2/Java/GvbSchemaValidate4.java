// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE FOREIGN KEYS

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import java.sql.*;
 
public class GvbSchemaValidate4 {
    public static void main(String[] args)
    {
        HashMap<String, String> spmap = new HashMap<>(100);
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
        String tname;
        String rname;
        String cname;
        String lastTab = "";

        Connection con;
        Statement stmt;
        ResultSet rs;

        System.out.println ("**** Running GvbSchemaValidate1: foreign keys");

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

        // Populate map of foreign keys and known hash values
        spmap.put("E_FILETBL","AXCrAl7ulTO+kv3O05mQJ2uA26qivAcnIm85rIoNgg5ruu5Yr++BhaOka2UqxiZacuMn1hrIO/PnicaRtpkA0Q=="); 
        spmap.put("E_JOINSOURCEKEYTBL","HkbbGHTFPjbEocle+LzIUa0Sb8EYN8i0zaJZJnhgF/w2OpW6oMcJM29HSdquOCVa3bWEW2udFTT6+819ET0xGg==");
        spmap.put("E_JOINTBL","yuNdjnlOc23kL0NywDwCMuNM58u5TguyvoVwH2YXEaWXkk1IWWqyXR5bIS2+ZlozhAOTd54z/T6JHOBGk2qCpA==");
        spmap.put("E_LOGICTBL","+qCYJ+BAIq83xh4NtBa766pRJTegAfZL5ryYO58XQhRZNVOnZ/fAmN/wwMkqhMXpl+h/KYuxuRtCqkeZwJPZxA==");
        spmap.put("E_LRINDEXTBL","ezjvD8gIcpFbnJOy+2W/K28dL8cxu0+hNZDHdK6OuWjSxKVadog73qBXER6ZvrTwS7SeLML+GSBYi5t6rcyXaw==");
        spmap.put("E_LRTBL","OnSUEsDnYrJskVvTdQ4FAuM4ho//i2wlIagkpMFfSmFJESvxa8wAzmgAXbJgJG3x7lD+Ks2hXMYouso/gnGmDA==");
        spmap.put("E_PARTITIONTBL","6Mg4LS0+FYKoIPbmFEiT2HVaNe5+kXOmwOy+45BuvHA5wTH3Be3YgZ0LhWyBGx5zD4wRyePiqhhCmlSLNcRT7A==");
        spmap.put("E_PROGRAMTBL","H0ei5tfNAAL+v87Ha/AIUBlaxQKMk1mHgjkgWYgo8GOvosikrgVnpoX46ugI9uQzVN+uFN7w+/vhegF2mVCWNg==");
        spmap.put("E_VIEWTBL","YJSNO1e/gNEd5epZFx4pvUYEdwPf1WbbJlKbtzKvWjSblHWYTyksR2IYscL2LhSuSXguBSrctICLqqd4VrQ6Dw==");
        spmap.put("XOV_LRFLDATTR","pLFoQgIEW+YgaZp7SSUAIBtb6AnNmagB3poj1XAIQWigz2HieSouixrYskNzk0y2LNNKTsxAhfUzjtsBNJKXyw=="); 
        spmap.put("XOV_VIEWLRFLDATTR","Tvc61zR1R90z79NwogtOlqQRLXtpSRQSKw+h4DnYx4H3atsRhyep8NMx8Vv1ljB2/v/AqdTyvxXSt5QTn0tB9A==");
        spmap.put("X_FILEPARTITIONTBL","A7WtTgoUlXaA1W65RYUxwoC9iqaXU9C0kewBdd2X9aBPU5Cv/KeEuON1gu+jpPD1VvyNL4NR52N++rAtZF3Wyw==");
        spmap.put("X_JOINTARGETLRTBL","0pY8SF7NvdKgSSgR8sEL9S3i2CZkLmAI+y6N3k3lC0YbWC8qHTLuFwLEjPkW1hpjrXqO3+SP10srMHPNJ5hy0w==");
        spmap.put("X_LRFILETBL","nK+UznIX14sEOtCUviHg2ETxEsrllvbPHkE/oqovZ4R48hhSstLyns5CHkpxiuhWqO9ps3UCjllW+DEnhef/Lw==");
        spmap.put("X_LRFLDTBL","pGyTAMzvzuCoNadh8VFCU8ghfiraCMvR1sn3I/zBrpIfYBtXbgBhk7U38/lyxTbKesSmKHEL92tovBkzeVFFdw==");
        spmap.put("X_LRINDEXFLDTBL","fPJmcTjAgn1fGnVs/6cKg5k4J67889ESxZvTsUfNUy34PJnhpSVDq18ljyrRHfl+pMM75Zm5+hxyBP+rLXJ45w==");
        spmap.put("X_SECGROUPSEXIT","JU8D6JmbtcDWUEroHejlUrH/U3ozjmzYQOKRhcMYicNTwJACf6Ya/BwPBNO1OEdmfAAMINZlpEftIXgQFHgnEQ==");
        spmap.put("X_SECGROUPSFILES","0ks7bj9udW84X+owoXHyATsvKHtiFD6fYvfOIMzWS6iE9wHEjmeo9OOp8ZzEsrH9wLuBAXiYiuYfJUoHEFCsow==");
        spmap.put("X_SECGROUPSJOIN","TRRCj4AMnz5Slwp2XoHBjEk4XbhX2BXVXAlI2XEeeOvK6tJQx850/Wlv51zVKjvsrPOgQaV1/2NSMYbrk5uNQw==");
        spmap.put("X_SECGROUPSLR","L3B8C0/Nd7RwWBRktmweCwG8pStah3yfUxeCbzyGm6skmUA835BkmZZfABdIt+XnFx/SNvee+IBxkfz1K56GRw==");
        spmap.put("X_SECGROUPSPARTS","tMHHxinfZKMl1nSw7ifJuSg3vTxwMNvAHQpT8mPZKUXelRHoHO0dy2BYldsx3IYVQXFZQcrQdqK0AWvuucuX4w==");
        spmap.put("X_SECGROUPSUSERS","G86MfoGth3LOq3sNiuCeWvwEdAzlmY12l9jZ4JdSExwCYuPUNPqdgkueQfZmzfA1QE2Om9e3EEWTbW3VYDVUuA==");
        spmap.put("X_SECGROUPSVIEW","gBFnnPa7XOKJX2UC5DTwu+p5deTSkSzAJPcgYFyG5iBjb3K38RIMEV0qDnNB3ed7mvEacIGeBrlUaDcY23xpFg==");
        spmap.put("X_SECGRPSENVTS","pFUHI5wH4uiH5O6hgbiVeITDNzZ3khksaXjHF/I2OmRwBpmiefxN3RTb/472pKVzUxUNkz4tCxRODPI/RgKjEA=="); 
        spmap.put("X_SECGRPSVFLDRS","XsC7aFf02bf3u9TchYNFB3hUT7RcdTpOsKVfBuPNxgX5j/4niSpo4yFyQcffbvLbmgS/Vf3M+ctxR6IBNDAjDQ==");
        spmap.put("X_VFVTBL","BSqnn80fxhv1InpQti0F0POlJnGkUc0SNDaMBbptZshLANBcgjzJim7S1fl6o9RZh1hIB/rPBrbBAWAs9p5caQ==");
        spmap.put("X_VIEWLOGICDEPEND","I14SLbtCGlf5+aq0fP3C64J5h7UAErPBMZjj9TOt/LP/CJdFUr5eKRCXYGNTp7C20UtX4KVQvdWx0qUP9A78SA==");
        spmap.put("X_VIEWLRFLD_LRFLD","e1N/TkBYbMvVORwlngLY+Z2jpuk1HjLrrfmHJc7H4zqMS97YBTptVkHPRd7Jw68d898QR/+AXjsiecukb3tzCw==");
        spmap.put("X_VIEWSORTKEYTBL","lZ0yn9TVO8zIuKE5kO39Yv3BqtFg4DlXsRZpCnJcQgoA5hujTs3xzRF8I1vDVxiUBunq7JrV7MupCau50caLew==");
        spmap.put("X_VIEWSRCLRFILETBL","ANWzHO7UOZJ/ZhMnsLYxUqkO0r2ERwBo6RDpm5w/lTtoOR7rM9e00wW94xn4PoXuu27Rzjzt7EwiYXZa3W3Awg==");

        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("**** Loaded the JDBC driver");

        // Create the connection using the IBM Data Server Driver for JDBC and SQLJ
        try {
            StringBuilder sb = new StringBuilder("");
            con = DriverManager.getConnection (url, user, password);
            con.setAutoCommit(false);
            System.out.println("**** Created a JDBC connection to the data source");

            // Create the Statement
            stmt = con.createStatement();
            System.out.println("**** Created JDBC Statement object");

            // Execute a query and generate a ResultSet instance
            rs = stmt.executeQuery("SELECT CREATOR, TBNAME, RELNAME, COLNAME FROM SYSIBM.SYSFOREIGNKEYS WHERE CREATOR LIKE '" + schema_mask + "' ORDER BY TBNAME, RELNAME, COLNAME;");
            System.out.println("**** Created JDBC ResultSet object");

            // Print all of the data
            writer = new BufferedWriter(new FileWriter(System.getenv("HOMEPATH")+"\\foreignkeydata.txt"));
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                schema = rs.getString(1);
                tname = rs.getString(2);
                rname = rs.getString(3);
                cname = rs.getString(4);

                if ( lastTab.equals(tname)) {
                    writer.write(schema + " " + tname + " " + rname + " " + cname);
                    sb.append(schema + " " + tname + " " + rname + " " + cname);
                }
                else{
                    if (sb.length() > 0 ) {
                        byte[] hashedBytes = md.digest((sb.toString()).getBytes());
                        String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                        if (!makeHash) {
                            System.out.println("Table: " + tname + " Digest: " + digestType + ": " + encodedHash);
                        }
                        else
                        {
                            System.out.println("        spmap.put(\"" + tname + "\"," + "\"" + encodedHash+"\"); "); //populate hash map
                        }
                        
                        if (!makeHash) {
                            String hashvalue = spmap.get(tname);
                            if ( hashvalue.equals(encodedHash))
                            {
                                System.out.println("HASH value matches for table: " + tname);
                            }
                            else
                            {
                                System.out.println("HASH value mismatch for table: " + tname);
                                System.out.println("Stored hash value: " + hashvalue);
                                System.out.println("====================================================================================");
                                ii = 1;
                            }
                        }
                    }

                    writer.write(schema + " TABLE: " + tname+" ============================================\n");
                    writer.write(schema + " " + tname + " " + rname + " " + cname);
                    
                    sb.delete(0, sb.length());
                    sb.append(schema + " " + tname + " " + rname + " " + cname);
                }
                writer.write("\n");
                lastTab = tname;
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

            System.out.println("**** JDBC completed - no errors");

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
                System.out.println("\nAll foreign key definitions match.\n");
            }
            else
            {
                System.out.println("\nOne or more foreign keys do not match expected definitions !!!\n");
            }
        }
        else
        {
            System.out.println("\nLines of code generated for hashmap\n");
        }
    }
}