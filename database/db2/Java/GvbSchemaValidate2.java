// https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=samples-example-simple-jdbc-application

// VALIDATE TABLES AND COLUMNS

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
 
public class GvbSchemaValidate2 {
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
        String cname;
        String typename;
        int    length;
        String lastTab = "";

        Connection con;
        Statement stmt;
        ResultSet rs;

        System.out.println ("**** Running GvbSchemaValidate2: checking tables and columns");

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

        // Populate map of tables and columns and known hash values
        spmap.put("CODETABLE", "i7QGnOzRbYS3+mf6GDD8n6A4gLYN9keGRzJSuONrUJaQcBum3yyfVIUjr9grEbhbqF+Zk1wE9f2L777gsBCRCA==");
        spmap.put("CONTROLREC", "Tlz7dBxJ3YRl+5dLneEPxIEeG11SUepCh7A1rnKam9sHwoi6sOWw35XWnnNCQRbdXt7gk1qlsWbF2wMvA5AJMw==");
        spmap.put("C_CONTROLREC", "At/rMMb5xNQUhTf/1+H3nMdsqm3Npxa4tJcjh4aSP8vNuivfsz/PPPFsJaAr5jkXFYGsitQRvoHjjoPmZlpwTg==");
        spmap.put("C_ENVIRONTBL", "4yQTn5fbpX8p9IfM654hc+CLbO6yqbZjmgDXnPKpwyz5/qW+CnXkHUGpr0YoVA+jQTClfw98EzAkRTbYs8vvQw==");
        spmap.put("C_NEXTKEYID", "P2hYChIzuClRmgSrk0jNP7BRUMABe8/v8c/kefCG86w3Y3OzU0Z+MIEEtkTXjVA3at/4U11tOFb5hZfPx+H5UA==");
        spmap.put("C_SECGROUPS", "5hrmQQlJ2wF2uddywoBBoUqAFD6amjjLf92eH8N+m5ss6i5bvBlkupCFBl4ItoMDuSV8PnmpGQzE0IZn8H/WyA==");
        spmap.put("C_USERTBL", "F1e8mg+9Jvm5tjaTass2RD2Cujd6ZPwh1pHKQ7rnWP3ACqOhTK/bwaqRh+JZc4UaKUmGg5liZWiKSun6iT7klA==");
        spmap.put("C_VIEWFOLDERS", "Vvf4/gUM0VZBliQZEEoiaioQd/D/35IacN0juecq9V2v1XUUVG/EBKenq8D1dBAydAaK4ToMi6QcBSZLTPHh/g==");
        spmap.put("ENVIRON", "CKvRVfUzrkyUA0cgvaCu2KYqq1GLWhV4NbaDf0D8Pm7EE0aB6aZdb6UyuD2BHfin8jdoweVRi4diaVPBcGFJnA==");
        spmap.put("EXIT", "+9ACPDGLi4xx2/fIS/yrn27bsdR5mdWG+gGREx1daU2jdlOvWqSfqZtdq/EG4UUcIOfDlTKssZ60OUgBlyNIxw==");
        spmap.put("EXTRACTCALCLOGIC_LOB", "mVPlKBs8GOGpFwz5OhcJkuC8yHC5f7AkNYp8OWcs5/QSIAxmI2hHKcPrGdBj75j6lUmsbm5LugQA/O1utRwomA==");
        spmap.put("EXTRACTFILTLOGIC_LOB", "7yWhOwWHKAscFDgtWrbJoV2IE5wcBiccH5qINQfiAvCkvjl6WYyiTxNagqGwefP90+v7MO5ZDvt/fXvHPnc8OA==");
        spmap.put("EXTRACTOUTPUTLOGIC_LOB", "NBSFYvEEFFOpRSBm3Blhu/yKH2KwvW32Noh9Mz5gRp693xMO85PQatokbDAREgIf3HP41yYbbM8A5yi+HnVYiw==");
        spmap.put("E_FILETBL", "XOCxlrbf+t/yBPdR9mGsUYwUTIFjb5XXKLxuYjXsmZyHdyxlpLC/ZDw7zpfTurqPeWDBRMBJwpIq8rj0lOHKSw==");
        spmap.put("E_FLDTBL", "qc3YPdVocDlptbNxEcmbuA2bWWC1cx+ImlFzLG4Ezm0qFERMS0ur8Zsruw7ZETveF634oCSETRQOFhpUYbtrWQ==");
        spmap.put("E_JOINSOURCEKEYTBL", "Y0je66bYHbUfx6qALbVX61S+59aQjiq0JFF3RFAyABV3G2veUIvFTahzXWCYgVDSKtr3D2jWzqgXrJ3Gf1mqvw==");
        spmap.put("E_JOINTBL", "I7r9VAWIgK+OgiuqeP4myidjbuy8mlmj60G725RoNggIo8dZIH0mJtjStqaYBffyW/pZX6une49SmiwSiNJNbw==");
        spmap.put("E_LOGICTBL", "A3/W6uqY5qtrsBPUtW3u31G+v0p8NxA2LptZSQ9LCyoL/axEWbSeRDsv4btL8G1Bl74mMaE/NUGaiefLHoKj0A==");
        spmap.put("E_LOGICTBL_LOB", "qKXYZ95+CwfIiAejyzu0c8D3gA+fS6Ob+AxAfKHbOlBtBRJzLgRsq85tm4wDO+J6lUd28nYVDtUhD7fEjOhN8w==");
        spmap.put("E_LRINDEXTBL", "0XQ2z+r7uGIh0IC3nS9JL5mml6qhf+5zxLkofH+2Yre78TMcNWWmsaUnijNzRQkAgqrs5hpwc8+lCDmby/MzUw==");
        spmap.put("E_LRTBL", "yKDnnH5bz+L/+lft1+pfvszZKK407HK4RvGSc94mPkmtkGLqwIY/KP1SQqcxcOkBiU7TqkGLxNGCfL5oqBKpLQ==");
        spmap.put("E_MIGAUDITTBL", "gKhgrd3MS7mTonOtswR9V4nMFn/6pIL7S8P2u8oGgp17pBmv7I2MZHaErFeTMmHvv35QDYzUcAc/o/CbviT47Q==");
        spmap.put("E_PARTITIONTBL", "uqXjIF+wqrKxwMciPSduzWYFbXyNcoT37XNQKaIfFfQ9jdOHhdwpQPmE+9R8FmByDaksxEIRgz6Fb0Qiqh28bA==");
        spmap.put("E_PROGRAMTBL", "qH8JyMmwPZx6OOrudo6C5TLNwYa39kOd7UQaX7gBROeCy0Mh+wZI6FsTbEUCSpF+Gem3/gL4cvJByYD2/pdsJg==");
        spmap.put("E_SERVERTBL", "NhCemBRacq7JrcrlMY85xxFMLyYSSXSw3MrrIk1scEZ1sOw86NWbBy3XxSe49MqurE+Kc/7Uuy5y0t5Rx2kiIA==");
        spmap.put("E_VDPBATCH", "6+RhjVSnzxRuTkFs7PNjKeaQLxrWbZlgNcRRWq5fO+JES0YZc71x+S2bKxrw6bvKq3Z3Qau3OyFNjIYYl5jxXQ==");
        spmap.put("E_VDPBATCHVIEWS", "Zb6ZkOpQ1eoM0qtVtKdH5Tw3wTbwKGJBF6XVkYkUVoO6+FdYo5yLGcd4jjd1WGlRthVW+d6G2bnTeMz18VB2uA==");
        spmap.put("E_VIEWHEADERFOOTER", "y1ntpvDGR7jaEe7ZQ1Hu/l13cWGl0FYP5RzUfb3nviJ2mbQvY9P7tdwiBs0oKgnUfb2UE+qjMowAcusDJODwxg==");
        spmap.put("E_VIEWTBL", "2PeAYWsKoJfh3lSd51R2OW+4XoM9DL+LXj3UOzQPD57dU4b0lkMrRIf4D1K8hfqj6b8g+JFunj3fD1dWrn98WQ==");
        spmap.put("FORMATCALCLOGIC_LOB", "bTtsTjddnI5LWy8vdIYVAmAPO3jCDt6QuSHpuIpteU2OtCF8NcE1zoisx6j6AT/eEBWYAR7UKnG1YZUhsdyVgQ==");
        spmap.put("FORMATFILTLOGIC_LOB", "5vBldKnrHP5bvIUT60fbo4ZkI10wZnnklg5v7IDYNok1mc8UsxeJgAprpjY7gyTac/aDSDmsEoX2IyMRvsyumg==");
        spmap.put("GROUP", "JVs9Kf4WnL2IX86ugjf/Aq6FBK/QVwNO7VJPzCP9dWKJaA/8faI7q11KZ+4h8hb/QDf+Qbt7h3/3/TXEbSmRIA==");
        spmap.put("LFPFASSOC", "CipVPtSStUgP5C5P2eGt55Ch1jmORt980WGetUU3SblfUqEbTCoSZr4RXrf87xYQycu7DY6VLJd2bfuWc1UyHQ==");
        spmap.put("LOGFILE", "cqG1kqESDpkb3eLEmrFLqIr/sAGWSFAdYe2qqmBr8r++G7DN8EoZMz9g/+fFSRct8xcXcaukDkfl6LXRMXo9LA==");
        spmap.put("LOGREC", "Xz9Vz/4G8vnzT5ykCFN/nhLuuWugB1BImUfMDm+u7JMg8MitUcVztg+uO+H3pdsJX/jqQxearL6LlPL/NqpX/Q==");
        spmap.put("LOOKUP", "NlgHUeCe+dTldZqCQq+16UqXoaNKIqJfWTOe6o3V0mvlJPL0aMQqnvjPO353n1FTfZNC1OU/3j+DHLmr/v0cDQ==");
        spmap.put("LOOKUPSRCKEY", "bJWfp8j+jV+4ByfMk+DKdoa+HRw/JTOW/IjwFG2a/otrgQEvEPidzzoJ0oCh4s41E27DtXoDY1mRhUG2Bket3Q==");
        spmap.put("LOOKUPSTEP", "z8FU0vsC0dGZS7frHWRVW2v976XzU5F3alwXmWYUHjZ21WvZ/fHHcC9bkRUqG0i6R3bGpWZh4+ed561cuW659Q==");
        spmap.put("LRFIELD", "1ggiJO68FBmdos4Vv08PKjKlOkemZYQOi2ecQD5yu5tq0pQayKj0dt54bBN3JFPG/mVkyZmHgccBo38kWfntlQ==");
        spmap.put("LRFIELDATTR", "L6O8RZRTzORdSThtzpqBgGl6OWEStqIDdo9Uwn4N7Q/8L+M1RBsFLapbc7e+KV/zH4CSpb3tAd668jIvmP6fDw==");
        spmap.put("LRINDEX", "7Bc/ztJuBeh3k6kDQdFUPryn3Eiw8DsdeMVYWwTrLwVwEPMq4ZhYHuqm7P7XbimyPk6cFdza3Wl84iwCfHuCUw==");
        spmap.put("LRINDEXFLD", "UApkFrW/+vL0ZLZZ4XarLyzWr318Z4qBvOQ+nj0sYUtx5RiNSpMiHsB/Hr02ISUjE19Psu38qArfL8kka/KUIw==");
        spmap.put("LRLFASSOC", "i/zm9o4ke3mb1BdhcDOLq/cqPLpg1nJaw4B4kNERlrR2WrPLlP834k0O9iAesopq3qDoLcevsQyBpUldVa2Hwg==");
        spmap.put("MIGAUDIT", "myNAwMj9OEBoQ8xflCNVlADrc3uFwEiPSm5fhoxenRgySKAjV/A9iDGuwc51QmJFlHOiXIye6y2HvH78nuEU5g==");
        spmap.put("PHYFILE", "VqanzXNv8iRpjQRYgUEofmWHdmt7zHIT0Gm4WfqwvgX8pxW7fGGSlqsFiImN3P2dUYnDvQZe6NVsdLj5AF0PnA==");
        spmap.put("SECENVIRON", "1FLfwGr6qsa7deDKBLD25B4IdVIZ0iQ2US19ynnfKDYBokYtL1p0P5x1yOpeDfym39qAXpO5aS4ma005NVWWrA==");
        spmap.put("SECEXIT", "w5PJ2T3ZJBH6MU+4IDHTZzw4VD2PKFyT/UblLobHNsDjEs5cUCVZwohDk0ygiWJ6YjPOv6m0cRXPeyLByIxwyA==");
        spmap.put("SECLOGFILE", "P7fga5O86Egm+6ny+b83f8DIdVa5zAEKTbuOKYQ+JuBJD3TWb3wt1HovarOpNdFuY0mqQPLdm9c5dsLKDICo4g==");
        spmap.put("SECLOGREC", "3ANhFIMe9ksIIPBNcgiq6V4st3SO6Tjx+qxLr0xrRE+F7ZiCb1iu049tSmVMryBUpkgPqLrmH8YI8NIVotBNbg==");
        spmap.put("SECLOOKUP", "eWWvaXGNBYrIC/dq61OyuGHYzoSChCox4DWrwgw8Mxw6E/Y9OsfFrvHWzg4FtqivkLuMuyXDnY/tH5VRkBQrgg==");
        spmap.put("SECPHYFILE", "LPh3nli1VQIzNGfsnBwZaGq9YGlDzpZN5DTDKdOyQXYPS0WTIE+7DVNyKzebFjRhIquuOBafY5PbKyHzMIW2JA==");
        spmap.put("SECUSER", "NJV6ucsXrnWGucsclIOumfxchFDFJC52Wcj7WqrTrpoiugNP21gW3l5scy0l0NKymVmyg5/WcX+zgR5eIlKgKg==");
        spmap.put("SECVIEW", "VxV13aM5newiR4hneR7SBzXcU09mLIRgSvgq5mkzPvmCGHY9LPg1xVR58dSl5eAsRXVC/pDC8FFyS19LFi73hQ==");
        spmap.put("SECVIEWFOLDER", "6LfHzr4WsgRfJnqLwNbELRc+zZOgfXC5UAkm5dpKSSNFGUa487MSLsVFKPiOSKlDOFpwSjJzuVg+hO/7Wqwn7g==");
        spmap.put("USER", "Pf+9FXxuBiTuBWAjEmShmBPCJppMy9h6xBqOXn+kqw/0+Sv0TlyBAZb7B12ltVNHbkcCAGILVWD3yPTLU5DjvA==");
        spmap.put("VFVASSOC", "TFnGAs4xZFvYN/O46FK6BXeX2Y9hLqqC9F5OsNqOpP004xqhKgbJKnCh6VtuA3ndxc/SG3cUyS2N+nZs//XSXg==");
        spmap.put("VIEW", "9wmAiMB3/RdLG0xkNFaJIr9H1EPl3hlbDWRDFLFkE0PDzhfW3mfJeGXD+6Ogqag75Jqaki342b2Ydoj30/KQhQ==");
        spmap.put("VIEWCOLUMN", "uuIvwjYyAqRRpXGlAMbVZKnYb2HpDGNXdxV12+YSMTN31QBXx3bQCnWZRVKiLX4aYGVkNNzA1ycmRN4sOx1pCw==");
        spmap.put("VIEWCOLUMNSOURCE", "MgPu/PVfo19Pf333FNyJLEtuHChBPlhVZRZWDswAusjCiEBIn7L+IBPPR8Y8ayQq1qaPyyd15EO851ms9VuQJA==");
        spmap.put("VIEWFOLDER", "hLyeJWwKIqZCTiEBdqK4MB0FjjF+hwff3z3mHVTNI3pg1+QwMoo2yBkbSpA7GsBL2g0Gk1qX57rl5p67I/P+ww==");
        spmap.put("VIEWHEADERFOOTER", "0lFZPH0RY590qy7bIJsQXTCZiEiLAtrrIxRCDryhYdJYQqKbu9kjZF9tUg2Yw6624KKnpgt1cPU8QiIw0kBAFw==");
        spmap.put("VIEWLOGICDEPEND", "9kTcJfpgffcQgxshQidCXyu92wgmkuXwsG5gkFe2FSZi9gnbMS4rxqH/g3DksEsP2O/e94EwU5bD+DFzQvbc/Q==");
        spmap.put("VIEWSORTKEY", "2aXF7rlMEdjdBWe3cGAd6ySzGTgOODdJ1KfPzR2gvMVyx9kQELmU+9otCytvc+QgDJVjAq+Tp1ZkEPC0Fozzzw==");
        spmap.put("VIEWSOURCE", "TJn06dUdeAQxuKR5+Re2f2zpveg7zmCgmAwyA4Ktat3vwpjrRRY5VanKhkFo+VU4MiQdg7sf6kTS4uKnAc4sNA==");
        spmap.put("XOV_LRFLDATTR", "8zvuTQcjTQ3tf31QtjdorvbxpJq5IYehv/Msz8J6cOptWwOcSZgmMe74AoG4bsezkh3GlAcSb/dmX5g6etF9SA==");
        spmap.put("XOV_VIEWLRFLDATTR", "jEwo3ArwwFUYJ2sKt78425RKEakP4AqrXQNfteuKkkJ0hGPS7OxUohEMa7od7AW06HUzLaDqjlEsWRAgXRN05g==");
        spmap.put("X_FILEPARTITIONTBL", "z/AGsV1xofR9dzrOI8hIPj2wJIwt1AsnOu0pWV8MXF73aF1U3dMuT3tAvo+rxVUEnnrOo4GyE+tJZGVUOtbCWg==");
        spmap.put("X_JOINTARGETLRTBL", "RfNJh5f287WLYoW0NUU3SjbRlj5D/J8P8eS9XGema1LvpgcvzZL3Kviw8ZNOhKYEpkfDAl/Wfq/zgGesCLUUmg==");
        spmap.put("X_LRFILETBL", "dG9gxW5z1x2JcOQTEngUPfvYXA3U0jlHByqBLzhQQW7PK1I+AbWE6tV+qwWw8kqlbkisTMo/2P5crIlKci3P3Q==");
        spmap.put("X_LRFLDTBL", "6+BkkJFREGckBtvqYTqXaqYkqvWdtDYUGyQDkGZd+wh0iwRmdCFB/zKTwEW7N79bFKXHimHdSzK34vRxVoouHw==");
        spmap.put("X_LRINDEXFLDTBL", "0dGopR3sXwio7F8ult8vK4l+hR3Vy9qnK26kZGqDbzz2be0pw3ijhTbwjCjysCYGM2/ta2creriBEkhjj4AbqA==");
        spmap.put("X_SECGROUPSEXIT", "FAHc1kdwjzSPLbYzPxIc506hmA7scpv81cCuV9oPF50R0cVtA3TXMcEPrxS11nZlWPVZhjQxl67RBLtiCMKvcw==");
        spmap.put("X_SECGROUPSFILES", "7qVRq4Dn7JRq+4xGQmxIioWBLriiOHeTjagrZa7cjYU/xflvX3llqA/Kv7R0VLOcmLaAHkSoEvKR8mx24H28LQ==");
        spmap.put("X_SECGROUPSJOIN", "eMS3oytVd8KbXQWYI0bdSsRU8AqD0gkwNgXucmV3rMkBFBOHIj4f6kj6Rdawd75A7oIu1OqWTT/Xa79v+zZ2CQ==");
        spmap.put("X_SECGROUPSLR", "mN0ED8vaRRXVZBkNvrHa2tcVrBkEnkZGZXLeyB722nHmbtubbIVtd2UmiT8HtsFabi45P0bx2NSjib+xLtwRUg==");
        spmap.put("X_SECGROUPSPARTS", "fyndIWgYWYcBlSU7LeqK/CpWSRTLnTAudYFstcBqsQ0qjysHRenIYN0c3h4lENqiJ+vX8PWcM7GsYI9bwYdBEA==");
        spmap.put("X_SECGROUPSUSERS", "ttESYWaIXXDhvaGxHLgtpc5M5XhawDd+lKqr+hjWbwxvsYwyCmUjMMm0xJ/mTQ8Zxs+8MkEN7jrhatP7fi1/ow==");
        spmap.put("X_SECGROUPSVIEW", "Bi/qXcIEB0O5IisHJq8DbZ+K+XBDEY5iOiDbK0LfrUdDwywoDhMxGwmUOOPzhGZIRedDULdoUwX0CyZvHzyN4w==");
        spmap.put("X_SECGRPSENVTS", "p/4PpS7wEKlfLmbhL1V5iLliJH/a51R4e7oTia6s84RXCIHxLqH1DnJDiGhBzGPyMjEbjAzjcDA5NWXuYyh09Q==");
        spmap.put("X_SECGRPSVFLDRS", "wwokuEdb63VpWfTYiKCIhKiOKvHaMdVFgSp6mph8BSj92Kq/FTMnh/b042mrfatTzkD1NTzkIUhXVdg8S1qcjg==");
        spmap.put("X_VFVTBL", "eNUmpNSbZEbYM+cbL5tmkrIhVBQbGg2uV1yPKejhgXu6K1O8n5Bozxez/4lqTzyamWFOFM6GCkgX56OTzJcUJA==");
        spmap.put("X_VIEWLOGICDEPEND", "NT48hfDTq7dJXNA0xC75yC5A92b2DlXHfpwNoiHBtQrFCgHzPoemtZ82NtEgdYbmXE2bAlgmI+bGRGXlLJC9xQ==");
        spmap.put("X_VIEWLRFLD_LRFLD", "1MlwQJaN4nzhAYqwbFNKypK6uOi7d5MRl8lqSBbIhiXikIRT5x6DikUyvfzYQqgQMDmIXH+Lgw8pjFx87geUaw==");
        spmap.put("X_VIEWSORTKEYTBL", "BKNYrf6uDXhR6O4EAnwk5kHjAEHzAImkfcaMiF+ZudgyG3aEQb8ji6a1x6MPA2L4GAZ6W4xmd1GUpoydcvUeFQ==");
        spmap.put("X_VIEWSRCLRFILETBL", "/r8M9eNjw2/f92Nso6JFVnehZ7hy+KPyKgnZNhBBb/aTmHBiiXSAnrHNfb3EBK6RNim4vUYXIjvLZ8eUzLJ4CA==");

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
            rs = stmt.executeQuery("SELECT TBCREATOR, TBNAME, NAME, COLTYPE, LENGTH FROM SYSIBM.SYSCOLUMNS WHERE TBCREATOR LIKE '" + schema_mask + "' ORDER BY TBNAME, NAME");
            System.out.println("**** Created JDBC ResultSet object");

            // Print all of the data
            writer = new BufferedWriter(new FileWriter(System.getenv("HOMEPATH")+"\\tabledata.txt"));
            MessageDigest md = MessageDigest.getInstance(digestType); //MD5");
            while (rs.next()) {
                schema = rs.getString(1);
                tname = rs.getString(2);
                cname = rs.getString(3);
                typename = rs.getString(4);
                length = rs.getInt(5);

                if ( lastTab.equals(tname)) {
                    writer.write(schema + " " + tname + " " + cname + " " + typename + " " + length);
                    sb.append(schema + " " + tname + " " + cname + " " + typename + " " + length);
                }
                else{
                    if (sb.length() > 0 ) {
                        byte[] hashedBytes = md.digest((sb.toString()).getBytes());
                        String encodedHash = Base64.getEncoder().encodeToString(hashedBytes);

                        if (!makeHash) {
                            System.out.println("Table: " + tname + " Digest: " + digestType + ": " + encodedHash);
                        }
                        else {
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
                    writer.write(schema + " " + tname + " " + cname + " " + typename + " " + length);
                    
                    sb.delete(0, sb.length());
                    sb.append(schema + " " + tname + " " + cname + " " + typename + " " + length);
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
                System.out.println("\nAll table definitions match.\n");
            }
            else
            {
                System.out.println("\nOne or more tables do not match expected definitions !!!\n");
            }
        }
        else
        {
            System.out.println("\nLines of code generated for hashmap\n");
        }
    }
}