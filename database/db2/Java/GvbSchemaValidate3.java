// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE INDEXES

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
 
public class GvbSchemaValidate3 {
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
        String iname;
        String uniqueR;
        String lastTab = "";

        Connection con;
        Statement stmt;
        ResultSet rs;

        System.out.println ("**** Running GvbSchemaValidate3: checking index definitions");

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

        // Populate map of indexs and known hash values
        spmap.put("C_CONTROLREC","6iSFoQvxuIBwiMGCyR+6NYcFE0gtMh5hrUr4V8m0Px8EobWTjPgoquvtPMtNXQMFujY/B1zkRpCdQPwIanPJeQ=="); 
        spmap.put("C_ENVIRONTBL","mUZ8sybLmGLZDiQOezbXYawHxeru/1jxJvg6Mk6kj4gZpc4Hqm1HbJ4Lc5ZXXTzQ5RtmIW8+slcE/tdsyUAWNA==");
        spmap.put("C_NEXTKEYID","sGZlYdgl0Qjq9yCFqXqqsALmbE1xJ7F95pG/CKz4BU74EwdPt87YDnSz2Nv+/cW3mMuo7EdQJvCIBSeR9dtxIg==");
        spmap.put("C_SECGROUPS","dPxya/YJg55APXR49aRPDjIK6jDWq+Mrm7AJsV2q/eSBS/V7uxLkok/SAp/6lznz5ZdPGEnXl5qtwobnuvpSrg==");
        spmap.put("C_USERTBL","2Uw5rMD5m+63g+hQ3QYhl1GsTo6enVxusxsG0hJ0FkCDWFU4pkbZMkSHKmSy7x1f/3moqwHJQatwqvHAoKW/Qw==");
        spmap.put("C_VIEWFOLDERS","VLaG+6nAVqANcs14GkrEREtD19ezbw3aigixnTyUbirD7DQADC3Z37SXaNJvC9EsY2O3kdJ5I6TpROb6bPxTEg==");
        spmap.put("EXTRACTCALCLOGIC_LOB","sk89UfsRmp27wYdgNTMwf0OcVGn7bilagtNIrfSPq00T8qEmLGAPD0GxmHoxGGtiol36KVHLw2bZ6bVd7iESPA==");       
        spmap.put("EXTRACTFILTLOGIC_LOB","fh/9KO8Ii7bf6THebArIBfuSTgxYw1esN3pXu28PnljJf9tievljunu3XUM/yIgdIcpnHwQ4iHgCkFtTX43S+A==");       
        spmap.put("EXTRACTOUTPUTLOGIC_LOB","9mzvYYn6IqEB3U64oLczZI4MrTNDwqdtZ/7MpmoA/A1l4GEhhA0DwtFqhMlZvUA+zepmOnJttxzLcMEPHFJzcw==");     
        spmap.put("E_FILETBL","bYQDIrYF+80VYjgrFn1X5gZdDGTGNvwjC7Fr2neJwhq8qQzKJuZkLMH3Ur5Z8NXSUgHtbio0nGSwIZ1xZ4+m5g==");
        spmap.put("E_FLDTBL","lIG56t15HenjH33/igE3CzInWcWvBaH8GCMzTFMAAjDvh4nt3cCpb/B6UNU+IgD6WZvkQ8dJNq9B2bwtQF3u9g==");
        spmap.put("E_JOINSOURCEKEYTBL","sCHVfvLBiOfqThU1jDY02S5jrnB3i70eEtoxtUe6J/H4PvETmLzvVJ4K+Lo6+TpcvvCXueY82q6f7n3YakmEVQ==");
        spmap.put("E_JOINTBL","ym3fcYH22PVFM1ftC5ulg9kKfbVjEZF6b0YtwwrP7hHggcFwDcYULjUfReycjVSRhJYaHBI1lL1isUab/PLaOQ==");
        spmap.put("E_LOGICTBL","TGugtcYhTVUt1OGiFFyFUouy+wPyufm1r+qiZAy2qAGsFoLNUgm7kzwidO/hIgt3V7gULLeT5AdGtz+pDMeciA==");
        spmap.put("E_LOGICTBL_LOB","kFTC6YzQtcx1mbRf2YlViDN319RzaKejhVZY4j2L590vysUlv9wQ/adc/md1iA2EjvoqdUBTAzoFOGUuySVhKA==");
        spmap.put("E_LRINDEXTBL","hN3WwwHZAPExlUk/Wi9mhXiIyvChu4kWPE61ma5eprkQD8TsfrHY+4x8Tt0eOpM4URhxHGBKDFAwUrW45KNSwg==");
        spmap.put("E_LRTBL","9yKHehh0VTCdCSOPxNG2SwwSZgmeahwFJSlgsEJPYmxIMuPoTd+eeiOj9GKkiBq85QmDnhPCOJsBZauK7mpNNg==");
        spmap.put("E_MIGAUDITTBL","0YLiqXxrjgIIc8tXZImYx2x1U14qHw/eG4M1wVsrDGtOwsSjcsZmC3apraSxreYcRwx/ujeRGSjE/9JBlOuUcA==");
        spmap.put("E_PARTITIONTBL","885mBRR7VPaxXz9m3Ke5WtSASiq9My7aalScYMsZqaiqDP3Q6w6R4zZEaZz1cKYV1NEZXHguem0TCBzJN7oGFQ==");
        spmap.put("E_PROGRAMTBL","qmFRxaTP4SdauaynPReFUK5d7V45cOt8Lk+9Nj3S1zVXQj9inIOcsLOtfxb03wBwj1SPCvTQhAXH6T7O9Kadrw==");
        spmap.put("E_SERVERTBL","p43ea1VqLUdYlDDV8d/tBbW/a32YSkWCNeaDBXi8xwzln6clhziNNfgAa4Y2sgUE/k5GXOe6CDV9O9iqyyt/gA==");
        spmap.put("E_VDPBATCH","cWPfEUmDEm349Y66b2Qe+3U0p+hbj9ZJLDrIl8YdpmStsqvbAJ4OUgeLGPy1BGWfZuOwPLCuo3G2WTswIhpd0A==");
        spmap.put("E_VDPBATCHVIEWS","/HqOWSQVfPfLg2/inoPF+Mu6oLvWPRJHk5B6ksMUgmyCTPtaetf+tRr9vunz/+61NEl08sgdGELSkYv9+YKOkQ==");
        spmap.put("E_VIEWHEADERFOOTER","fIqr8GWDX8vuJYiNjF1mYF2QXOnJOFGyYG5g2HwUh8W0/tUPyrZdwzc6STjL1aIszvV7FESyt+NkDgMTiHf7Bw==");
        spmap.put("E_VIEWTBL","vCXyCkTtbc4BxNcTVcCigFLh/X9GOUgsJBwajOhsjUEtU/JFjkMIRyWktUbCDBZ0QD4l1klp9yqgCDqXNte4TQ==");
        spmap.put("FORMATCALCLOGIC_LOB","w7EaKoLy7sTslK+ermmTLyHQMZF8VGHvw5eeqO7Pp9WPMTGLWSNyhOMg45E9HMUsRXIsrS4hlAPKnzc4X4+huQ==");        
        spmap.put("FORMATFILTLOGIC_LOB","TjFpHRpALt0KcLEI/2f2CzdPnZcA1rnJ65flcvbJdVDiIJzvVhU4taomu4vLKrRzmwiWKDQWll7yIq5PuKZFuA==");        
        spmap.put("XOV_LRFLDATTR","ju86YRGaaP8IOIGXWlUBFcwq+v6I63ISRvO2lFSAESbZtvXBnHCFwiUtZCtSpGN6+59DmmHDeTQ3ufspix9hvA==");
        spmap.put("XOV_VIEWLRFLDATTR","Nvoy/oHClgOJVwQxGMZDdRt/e4Uwm0VSHt+EuoSR3lJGuNmcY1d6DdVlw5YaxCkpBru0IPYMAYm3mGFTEqlQPw==");
        spmap.put("X_FILEPARTITIONTBL","D8kAD/nZLK2taREcKAVPkaTqvpf/SE39oIITedBK7vStmoRjYJoGyX9JXZqCYMSuyv7XL8k4Jmag6AEZFMkzKw==");
        spmap.put("X_JOINTARGETLRTBL","BX59hu0TAKVlxSCTk6cwNj+nwfmcwvNFlZF9+lecTg2MsUxt4RbZZgQ5UzVmz/GcRVxTOO9vwaAdAdwURALHvw=="); 
        spmap.put("X_LRFILETBL","F/Fybh9xurjrR6SPhdObZV3rDatqkyzA4mm0SOzrBm08Y30PcECgGBR9jn44iNZfysDehpEyMeDvfcnJu8ts0Q==");
        spmap.put("X_LRFLDTBL","Ic1KOR5iFXMHeb7m6LxMCWXcmIaOl/NZBFx7tvL7AUVcPm8T8kFavV6hvVS4uMu9bCHM9IBjwqpXX1mXUhqbrw==");
        spmap.put("X_LRINDEXFLDTBL","9RYsLo2r0Y35btXdsRwKaEv4ncvJ/WjekM5ZoOKpvV2eh+KPbLSUNnxd4iic9kteCItwepqaShj+3cQkjy/qjw==");
        spmap.put("X_SECGROUPSEXIT","BXfHjXH1iYdURdJRhB9us4HSOQwmQ/BoOgoAn8Xca/QcKEnB/M65so8XwXGigz6sSh0UtkUCr7Fk9KbVjrLK8Q==");
        spmap.put("X_SECGROUPSFILES","q1lXxCFMa44PEwXUZmt68nUkI45uqOtHQq9GcMfOKLRj9a+tyhApNlCPS0ePjx5kL36afsqD7FLWewLy0RtYXw==");
        spmap.put("X_SECGROUPSJOIN","RwgOPPDk3j39WxT5N8COAu9yNQxtWSgYciFhNvq9QSs1LvadQlaFsD6URaU1y3+FWG5ufTu+1ZgaDlVTD/C1sQ==");
        spmap.put("X_SECGROUPSLR","Ba/6CoJsCOftevU5YB0XfZg2aLLbMbIJ7Mzj/0hUyftU4w/43Xd5RThgvzbGyof8/zhYnVokRO41vN7LnFtRBA==");
        spmap.put("X_SECGROUPSPARTS","ScHUr6Gn4UnbOmrl5hzSgapB7kLu2dDQAXxac21hcllRLEy82tLH4J6yX4zRMCSS8QRHSrnrv1Wpgf5eeqq0rQ==");
        spmap.put("X_SECGROUPSUSERS","5QNn55M6iOUhYyg1VPPbDTAH/HuE7W2skYSoaempzpSniXGg0Ytw/yrIMO3U4NIuqwnZvRo/khX/cLp3NO3+Hw==");
        spmap.put("X_SECGROUPSVIEW","9c5jOt1893PNVmPeJ760vmvGitV0zgK5+sXLXgK4eEn3HG+3R5po6xm2znVPmKy/3wpkCjvnzUXQ2pYPKMPwkA==");
        spmap.put("X_SECGRPSENVTS","1jq38dINu7Y2Sg/fmBQSX975nkTkTj0hniJzmtLfB0LQyWfn2PIVE6Tvk3aat8VaPtEzXTkANX3cD8jVnXCEmQ==");
        spmap.put("X_SECGRPSVFLDRS","B5NQ6VK5sbm03Hbk3NsXF8yCgz2xClxXXT2i88ATUpIBTji4Kj3ycgnzSoimSiMK6cm0eCAHeEVvfJSNcCarLQ==");
        spmap.put("X_VFVTBL","A1nJO9p4FNFiHTiFMGLhuA7npE8OmHXUj7w9z1BLMyltaNTBN4Y8IhmKgiiZwTfwjlgdQkJyKHirKBmuyQeI1Q==");
        spmap.put("X_VIEWLOGICDEPEND","5Wu9Syp4rXGN3mtd+tOsS7i4JDfy2P9ns52ImLtaDpcK7Rjcvf9/2c4x5P2wkH+pSAgWfmzdBQt3rokwTPooqw==");
        spmap.put("X_VIEWLRFLD_LRFLD","5Q9p6gHmuqU20iXCNOV4/BBVdcm+IzvnCxGNrbIx0m12jx6WTuUS5Mwie7yH1EGo+BwaRaHXg0EmnJvYyYUhtg==");
        spmap.put("X_VIEWSORTKEYTBL","0NSZ7JeCfoBkyzKIM0g+LcOXAmm8orUQagUMuWGxMKOnX05/zQb3MStY8MRPonnlftuwF51h4gvpiyFsl4qwJQ==");
        spmap.put("X_VIEWSRCLRFILETBL","/ItFcqrWtKtGbK7YIUfBDLkVQPvw76yLFvUPd0COf7s+2EglW0AIB4hPs7b/l9Sg0S4i3RmXZpqEaiZvoVomNQ==");

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
            rs = stmt.executeQuery("SELECT CREATOR, TBNAME, NAME, UNIQUERULE FROM SYSIBM.SYSINDEXES WHERE CREATOR LIKE '" + schema_mask + "' ORDER BY TBNAME, NAME;");
            System.out.println("**** Created JDBC ResultSet object");

            // Print all of the data
            writer = new BufferedWriter(new FileWriter(System.getenv("HOMEPATH")+"\\indexdata.txt"));
            MessageDigest md = MessageDigest.getInstance(digestType);
            while (rs.next()) {
                schema = rs.getString(1);
                tname = rs.getString(2);
                iname = rs.getString(3);
                uniqueR = rs.getString(4);

                if ( lastTab.equals(tname)) {
                    writer.write(schema + " " + tname + " " + iname + " " + uniqueR);
                    sb.append(schema + " " + tname + " " + iname + " " + uniqueR);
                }
                else{
                    if (sb.length() > 0 ) {
                        byte[] hashedBytes = md.digest((sb.toString()).getBytes());
                        String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                        if (!makeHash) {
                            System.out.println("Table: " + tname + " Digest: " + digestType + ": " + encodedHash);
                        }
                        else{
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
                    writer.write(schema + " " + tname + " " + iname + " " + uniqueR);
                    
                    sb.delete(0, sb.length());
                    sb.append(schema + " " + tname + " " + iname + " " + uniqueR);
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
                System.out.println("\nAll index definitions match.\n");
            }
            else
            {
                System.out.println("\nOne or more indexes do not match expected definitions !!!\n");
            }
        }
        else {
            System.out.println("\nLines of code generated for hashmap\n");
        }
    }
}