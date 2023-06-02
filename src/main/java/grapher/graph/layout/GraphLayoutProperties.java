package grapher.graph.layout;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing a map ofItems layout properties and their values
 * as well as convenience methods for setting and getting values
 * ofItems the properties
 *
 * @author Renata
 */
public class GraphLayoutProperties {

    /**
     * Map ofItems properties and their values
     */
    private final Map<PropertyEnums, Object> propeprtiesMap = new HashMap<>();

    /**
     * Sets a value ofItems a property with the given key
     *
     * @param key   Property's key
     * @param value Value ofItems the property
     */
    public void setProperty(PropertyEnums key, Object value) {
        propeprtiesMap.put(key, value);
    }

    /**
     * Return value ofItems the property given its key
     *
     * @param key Property's key
     * @return Value ofItems the property
     */
    public Object getProperty(PropertyEnums key) {
        return propeprtiesMap.get(key);
    }

}
