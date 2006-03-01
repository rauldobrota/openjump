package com.vividsolutions.jump.workbench.datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.parameter.ParameterList;
import com.vividsolutions.jump.parameter.ParameterListSchema;
import com.vividsolutions.jump.util.Block;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.SimpleStringEncrypter;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.plugin.datastore.ConnectionDescriptorPanel;

/**
 * Contains a ParameterList and its associated DataStoreDriver.
 */
public class ConnectionDescriptor {
    private DataStoreDriver driver;

    private ParameterList parameterList;

    private String dataStoreDriverClassName;

    public int hashCode() {
        // Implement #hashCode so that ConnectionDescriptor works
        // as a HashMap key. But just set it to 0 for now, to
        // avoid the work of creating code to generate a proper hash.
        // This will unfortunately force a linear scan of the keys whenever
        // HashMap#get is used; however, this will not be a big problem, as
        // I don't expect there to be many keys in HashMaps of
        // ConnectionDescriptors [Jon Aquino 2005-03-07]
        return 0;
    }

    public DataStoreConnection createConnection() throws Exception {
        return getDriver().createConnection(parameterList);
    }

    public boolean equals(Object other) {
        return equals((ConnectionDescriptor) other);
    }

    private boolean equals(ConnectionDescriptor other) {
        if (!(other instanceof ConnectionDescriptor)) {
            // This case includes null. [Jon Aquino 2005-03-16]
            return false;
        }
        return getDriver().getClass() == other.getDriver().getClass()
                && getParameterListWithoutPassword().equals(
                        other.getParameterListWithoutPassword());
    }

    public ParameterList getParameterList() {
        return parameterList;
    }

    public String toString() {
        return getDriver().getName()
                + ":"
                + StringUtil
                        .toCommaDelimitedString(
                                CollectionUtil
                                        .select(
                                                parameterValues(getParameterListWithoutPassword()),
                                                new Block() {
                                                    public Object yield(
                                                            Object name) {
                                                        // Don't include null
                                                        // parameters e.g.
                                                        // passwords [Jon Aquino
                                                        // 2005-03-15]
                                                        return Boolean
                                                                .valueOf(name != null);
                                                    }
                                                })).replaceAll(", ", ":");
    }

    private List parameterValues(ParameterList parameterList) {
        List parameterValues = new ArrayList();
        for (Iterator i = Arrays.asList(parameterList.getSchema().getNames())
                .iterator(); i.hasNext();) {
            String name = (String) i.next();
            parameterValues.add(parameterList.getParameter(name));
        }
        return parameterValues;
    }

    public ParameterList getParameterListWithoutPassword() {
        ParameterList parameterListWithoutPassword = new ParameterList(
                parameterList);
        if (passwordParameterName(parameterList.getSchema()) != null) {
            parameterListWithoutPassword.setParameter(
                    passwordParameterName(parameterList.getSchema()), null);
        }
        return parameterListWithoutPassword;
    }

    public void setParameterListWithObfuscatedPassword(
            PersistentParameterList parameterListWithObfuscatedPassword) {
        ParameterList parameterList = new ParameterList(
                parameterListWithObfuscatedPassword);
        if (passwordParameterName(parameterList.getSchema()) != null) {
            parameterList.setParameter(passwordParameterName(parameterList
                    .getSchema()), unobfuscate(parameterList
                    .getParameterString(passwordParameterName(parameterList
                            .getSchema()))));
        }
        setParameterList(parameterList);
    }

    public PersistentParameterList getParameterListWithObfuscatedPassword() {
        ParameterList parameterListWithObfuscatedPassword = new ParameterList(
                parameterList);
        if (passwordParameterName(parameterList.getSchema()) != null) {
            parameterListWithObfuscatedPassword
                    .setParameter(
                            passwordParameterName(parameterList.getSchema()),
                            obfuscate(parameterList
                                    .getParameterString(passwordParameterName(parameterList
                                            .getSchema()))));
        }
        return new PersistentParameterList(parameterListWithObfuscatedPassword);
    }

    private String obfuscate(String s) {
        return s != null ? new SimpleStringEncrypter().encrypt(s) : null;
    }

    private String unobfuscate(String s) {
        return s != null ? new SimpleStringEncrypter().decrypt(s) : null;
    }

    public void setDataStoreDriverClassName(String dataStoreDriverClassName) {
        this.dataStoreDriverClassName = dataStoreDriverClassName;
    }

    public String getDataStoreDriverClassName() {
        return getDriver().getClass().getName();
    }

    public void setParameterList(ParameterList parameterList) {
        this.parameterList = parameterList;
    }

    public ConnectionDescriptor() {
    }

    public ConnectionDescriptor(Class dataStoreDriverClass,
            ParameterList parameterList) {
        setDataStoreDriverClassName(dataStoreDriverClass.getName());
        setParameterList(parameterList);
    }

    private DataStoreDriver getDriver() {
        // Lazily initialize the driver, as the driver class may be
        // missing from the classpath, and we don't want an exception
        // during loading of the persistent blackboard.
        // [Jon Aquino 2005-03-11]
        if (driver == null) {
            try {
                driver = (DataStoreDriver) Class.forName(
                        dataStoreDriverClassName).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return driver;
    }

    public static String passwordParameterName(ParameterListSchema schema) {
        for (Iterator i = Arrays.asList(schema.getNames()).iterator(); i
                .hasNext();) {
            String name = (String) i.next();
            if (name.equalsIgnoreCase("password")) {
                return name;
            }
        }
        return null;
    }

    public static class PersistentParameterList extends ParameterList {
        public PersistentParameterList() {
            super(new ParameterListSchema(new String[] {}, new Class[] {}));
        }

        public PersistentParameterList(ParameterList parameterList) {
            this();
            setSchema(new PersistentParameterListSchema(parameterList
                    .getSchema()));
            setNameToValueMap(nameToValueMap(parameterList));
        }

        private static Map nameToValueMap(ParameterList parameterList) {
            Map nameToValueMap = new HashMap();
            for (Iterator i = Arrays.asList(
                    parameterList.getSchema().getNames()).iterator(); i
                    .hasNext();) {
                String name = (String) i.next();
                nameToValueMap.put(name, parameterList.getParameter(name));
            }
            return nameToValueMap;
        }

        public Map getNameToValueMap() {
            return nameToValueMap(this);
        }

        public void setNameToValueMap(Map nameToValueMap) {
            for (Iterator i = nameToValueMap.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                setParameter(name, nameToValueMap.get(name));
            }
        }

        public void setSchema(PersistentParameterListSchema schema) {
            initialize(schema);
        }
    }

    public static class PersistentParameterListSchema extends
            ParameterListSchema {
        public PersistentParameterListSchema() {
            super(new String[] {}, new Class[] {});
        }

        public PersistentParameterListSchema(ParameterListSchema schema) {
            this();
            initialize(schema.getNames(), schema.getClasses());
        }

        public List getPersistentNames() {
            return Arrays.asList(getNames());
        }

        public void addPersistentName(String name) {
            String[] newNames = new String[getNames().length + 1];
            System.arraycopy(getNames(), 0, newNames, 0, getNames().length);
            newNames[getNames().length] = name;
            initialize(newNames, getClasses());
        }

        public List getPersistentClasses() {
            return Arrays.asList(getClasses());
        }

        public void addPersistentClass(Class c) {
            Class[] newClasses = new Class[getClasses().length + 1];
            System.arraycopy(getClasses(), 0, newClasses, 0,
                    getClasses().length);
            newClasses[getClasses().length] = c;
            initialize(getNames(), newClasses);
        }
    }
}