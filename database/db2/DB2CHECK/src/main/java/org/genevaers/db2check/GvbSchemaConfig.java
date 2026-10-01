package org.genevaers.db2check;

import java.io.BufferedWriter;
import java.sql.*;
import java.util.HashMap;

public class GvbSchemaConfig {

    private String digestType;
    private Connection con;
    private String schema_mask;
    private Boolean makeHash;
    private Boolean makeDef;
    private BufferedWriter fwriter;
    private BufferedWriter hwriter;
    private BufferedWriter[] dwriter;
    private HashMap<String, String> spmap;
    private HashMap<String, String> tbmap;
    private HashMap<String, String> ixmap;
    private HashMap<String, String> fkmap;

    // Constructor
    public GvbSchemaConfig(String digestType, Connection con, String schema_mask, Boolean makeHash, Boolean makeDef,
        BufferedWriter fwriter, BufferedWriter hwriter, BufferedWriter dwriter[],
        HashMap<String, String> spmap, HashMap<String, String> tbmap, HashMap<String, String> ixmap, HashMap<String, String> fkmap)
    {
        this.digestType = digestType;
        this.con = con;
        this.schema_mask = schema_mask;
        this.makeHash = makeHash;
        this.makeDef = makeDef;
        this.fwriter = fwriter;
        this.hwriter = hwriter;
        this.dwriter = dwriter;
        this.spmap = spmap;
        this.tbmap = tbmap;
        this.ixmap = ixmap;
        this.fkmap = fkmap;
    }

    public String getDigestType() { return digestType; }
    public Connection getCon() { return con; }
    public String getSchemaMask() { return schema_mask; }
    public Boolean getMakeHash() { return makeHash; }
    public Boolean getMakeDef() { return makeDef; }
    public BufferedWriter getFwriter() { return fwriter; }
    public BufferedWriter getHwriter() { return hwriter; }
    public BufferedWriter[] getDwriter() { return dwriter; }
    public HashMap<String, String> getSpmap() { return spmap;}
    public HashMap<String, String> getTbmap() { return tbmap;}
    public HashMap<String, String> getIxmap() { return ixmap;}
    public HashMap<String, String> getFkmap() { return fkmap;}

    public void setDigestType(String digestType) { this.digestType = digestType; }
    public void setCon(Connection con) {this.con = con; }
    public void setSchemaMask(String schema_mask) {this.schema_mask = schema_mask; }
    public void setMakeHash(Boolean makeHash) {this.makeHash = makeHash; }
    public void setMakeDef(Boolean makeDef) {this.makeDef = makeDef; }
    public void setFwriter(BufferedWriter fwriter) {this.fwriter = fwriter; }
    public void setHwriter(BufferedWriter hwriter) {this.hwriter = hwriter; }
    public void setDwriter(BufferedWriter[] dwriter) {this.dwriter = dwriter; }
    public void setSpmap(HashMap<String, String> spmap) {this.spmap = spmap;}
    public void setTbmap(HashMap<String, String> tbmap) {this.tbmap = tbmap;}
    public void setIxmap(HashMap<String, String> ixmap) {this.ixmap = ixmap;}
    public void setFkmap(HashMap<String, String> fkmap) {this.fkmap = fkmap;}
}
