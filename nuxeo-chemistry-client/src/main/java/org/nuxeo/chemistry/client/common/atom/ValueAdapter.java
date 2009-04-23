/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.chemistry.client.common.atom;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.abdera.model.AtomDate;
import org.apache.chemistry.atompub.CMIS;
import org.apache.chemistry.property.PropertyType;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public abstract class ValueAdapter {

    public abstract Serializable readValue(String xml);
    
    public abstract String writeValue(Serializable val);
    
    public abstract Serializable[] createArray(int size);
    
    public abstract QName getPropertyName();
    
    public static final ValueAdapter STRING = new ValueAdapter() {
        public Serializable readValue(String xml) { return xml; }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new String[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_STRING; }
    };

    public static final ValueAdapter XML = new ValueAdapter() {
        public Serializable readValue(String xml) { return xml; }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new String[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_XML; }
    };

    public static final ValueAdapter HTML = new ValueAdapter() {
        public Serializable readValue(String xml) { return xml; }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new String[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_HTML; }
    };

    public static final ValueAdapter BOOLEAN = new ValueAdapter() {
        public Serializable readValue(String xml) { return Boolean.valueOf(xml); }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new Boolean[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_BOOLEAN; }
    };

    public static final ValueAdapter INTEGER = new ValueAdapter() {
        public Serializable readValue(String xml) { return Integer.valueOf(xml); }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new Integer[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_INTEGER; }
    };

    public static final ValueAdapter DECIMAL = new ValueAdapter() {
        public Serializable readValue(String xml) { return new BigDecimal(xml); }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new BigDecimal[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_DECIMAL; }
    };

    public static final ValueAdapter DATE = new ValueAdapter() {
        public Serializable readValue(String xml) { return AtomDate.valueOf(xml).getCalendar(); }
        // accepts both Calendar and Date
        public String writeValue(Serializable val) { return val.getClass() == Calendar.class 
            ? AtomDate.format(((Calendar)val).getTime()) : AtomDate.format((Date)val); }
        public Serializable[] createArray(int size) { return new Calendar[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_DATETIME; }
    };
    
    public static final ValueAdapter ID = new ValueAdapter() {
        public Serializable readValue(String xml) { return xml; }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new String[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_ID; }
    };
    
    public static final ValueAdapter URI = new ValueAdapter() {
        public Serializable readValue(String xml) { try { return new URI(xml); } catch (Exception e) {throw new IllegalArgumentException("Invalid URI: "+xml); } }
        public String writeValue(Serializable val) { return val.toString(); }
        public Serializable[] createArray(int size) { return new String[size]; }
        public QName getPropertyName() { return CMIS.PROPERTY_URI; }
    };

    private final static Map<String,ValueAdapter> adapters = new HashMap<String, ValueAdapter>();  
    static {
        adapters.put("String", STRING);
        adapters.put("Boolean", BOOLEAN);
        adapters.put("Integer", INTEGER);
        adapters.put("Decimal", DECIMAL);
        adapters.put("DateTime", DATE);
        adapters.put("Id", ID);
        adapters.put("Uri", URI);
        adapters.put("Xml", XML);
        adapters.put("Html", HTML);

        adapters.put("string", STRING);
        adapters.put("boolean", BOOLEAN);
        adapters.put("integer", INTEGER);
        adapters.put("decimal", DECIMAL);
        adapters.put("datetime", DATE);
        adapters.put("id", ID);
        adapters.put("uri", URI);
        adapters.put("xml", XML);
        adapters.put("html", HTML);

    }

    
    
    public static void registerAdapter(String type, ValueAdapter va) {
        adapters.put(type, va);
    }
    
    public static ValueAdapter getAdapter(String type) {
        return adapters.get(type);
    }

    public static ValueAdapter getAdapter(PropertyType type) {
        switch (type) {
        case STRING:
            return ValueAdapter.STRING;
        case BOOLEAN:
            return ValueAdapter.BOOLEAN;
        case DATETIME:
            return ValueAdapter.DATE;
        case ID:
            return ValueAdapter.ID;
        case INTEGER:
            return ValueAdapter.INTEGER;
        case URI:
            return ValueAdapter.URI;
        case DECIMAL:
            return ValueAdapter.DECIMAL;            
        case XML:
            return ValueAdapter.XML;
        case HTML:
            return ValueAdapter.HTML;
        }
        return null;
    }

}
