package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.util.*;
import java.awt.*; 

public class ConfigParams {
    public static final int TYPE_UNDEFINED = 0;
    public static final int TYPE_INT       = 1;
    public static final int TYPE_DOUBLE    = 2;
    public static final int TYPE_STRING    = 3;
    public static final int TYPE_COLOR     = 4;

    protected Hashtable<String, String> values;
    protected Hashtable<String, Integer> types;

    public ConfigParams() {
        this.values = new Hashtable<String, String>();
        this.types = new Hashtable<String, Integer>();
    }

    protected String get(String key, String defaultVal, int type) {
        if( this.getType(key) == TYPE_UNDEFINED ) {
            this.setType(key, type);
        }
        if( this.values.containsKey(key) ) {
            return this.values.get(key);
        }
        this.values.put(key, defaultVal);
        return defaultVal;
    }

    protected void set(String key, String val, int type) {
        this.setType(key, type);
        this.values.put(key, val);
    }

    public boolean containsKey(String key) {
        return this.values.containsKey(key);
    }

    public Enumeration keys() {
        return this.values.keys();
    }

    public int getType(String key) {
        if( this.types.containsKey(key) ) {
            return this.types.get(key);
        }
        return TYPE_UNDEFINED;
    }

    public void setType(String key, int type) {
        this.types.put(key, type);
    }

    public String getString(String key, String defaultValue) {
        return this.get(key, defaultValue, TYPE_STRING);
    }

    public void setString(String key, String value) {
        this.set(key, value, TYPE_STRING);
    }

    public double getDouble(String key, double defaultValue) {
        return Double.parseDouble(this.get(key, Double.toString(defaultValue), TYPE_DOUBLE));
    }

    public void setDouble(String key, double val) {
        this.set(key, Double.toString(val), TYPE_DOUBLE);
    }

    public String printColor(Color c) {
        return Integer.toHexString(c.getRGB());
    }

    public Color parseColor(String s) {
        return Color.getColor(s);
    }

    public Color getColor(String key, Color defaultValue) {
        return this.parseColor(this.get(key, this.printColor(defaultValue), TYPE_COLOR));
    }

    public void setColor(String key, Color val) {
        this.set(key, this.printColor(val), TYPE_COLOR);
    }

}
