package devicegate.conf;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaoke on 17-5-6.
 */
public class Configure {

    private static final String V_XML_PATH = V.VAR_FILE_PATH;

    private Map<String, String> XML_VAR_MAP = null;
    
    private void readFromXml() throws DocumentException {
        File f = new File(V_XML_PATH);
        System.out.println(f.getAbsoluteFile());
        SAXReader reader = new SAXReader();
        Document doc = reader.read(f);
        Element root = doc.getRootElement();
        List<?> vs = root.elements("v");
        for (Object obj : vs) {
            if (obj instanceof Element) {
                Element ve = (Element) obj;
                String name = ve.attributeValue("name");
                Object value = ve.getData();
                if (name != null) {
                    XML_VAR_MAP.put(name.trim(), value.toString().trim());
                }
            }
        }
    }

    private void check() {
        if (XML_VAR_MAP == null) {
            synchronized (this) {
                if (XML_VAR_MAP == null) {
                    XML_VAR_MAP = new HashMap<String, String>();
                    try {
                        readFromXml();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                        if (XML_VAR_MAP != null) {
                            XML_VAR_MAP.clear();
                        }
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private String mapGet(String key) {
        check();
        return XML_VAR_MAP.get(key);
    }
    
    public String getString(String key) {
        String e = mapGet(key);
        if (e != null) {
            return e;
        } else {
            return "NULL";
        }
    }

    public String getStringOrElse(String key, String defualt) {
        check();
        String e = mapGet(key);
        if (e != null) {
            return e;
        } else {
            return defualt;
        }
    }

    public int getInt(String key) {
        return Integer.parseInt(XML_VAR_MAP.get(key));
    }

    public int getIntOrElse(String key, int defualt) {
        try {
            String e = mapGet(key);
            if (e != null) {
                return Integer.parseInt(e.toString());
            } else {
                return defualt;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualt;
        }
    }

    public long getLong(String key) {
        return Long.parseLong(XML_VAR_MAP.get(key));
    }

    public long getLongOrElse(String key, long defualt) {
        try {
            String e = mapGet(key);
            if (e != null) {
                return Long.parseLong(e.toString());
            } else {
                return defualt;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualt;
        }
    }

    public float getFloat(String key) {
        return Float.parseFloat(XML_VAR_MAP.get(key));
    }

    public float getFloatOrElse(String key, float defualt) {
        try {
            String e = mapGet(key);
            if (e != null) {
                return Float.parseFloat(e.toString());
            } else {
                return defualt;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualt;
        }
    }


    public double getDouble(String key) {
        return Double.parseDouble(XML_VAR_MAP.get(key));
    }

    public double getDoubleOrElse(String key, double defualt) {
        try {
            String e = mapGet(key);
            if (e != null) {
                return Double.parseDouble(e.toString());
            } else {
                return defualt;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defualt;
        }
    }
}
