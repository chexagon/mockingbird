package com.crimsonhexagon.mockingbird.jsonschema;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import static java.nio.charset.Charset.forName;

/**
 * Utility for loading {@link Schema} objects.
 */
public class SchemaUtil {

    private static Charset UTF8 = forName("UTF-8");

    /**
     * Loads a {@link Schema} from a JSON Schema file located on the classpath.
     *
     * @param resourceLocation {@link String} JSON Schema location on the classpath
     * @return {@link Schema} JSON Schema object
     * @throws JSONException thrown when the JSON Schema fails to parse
     */
    public static Schema loadSchemaFromClasspathResource(String resourceLocation) throws JSONException {
        InputStream stream = SchemaUtil.class.getResourceAsStream(resourceLocation);
        if (stream == null) return null;

        JSONObject jsonSchema = new JSONObject(new JSONTokener(new InputStreamReader(stream, UTF8)));
        return SchemaLoader.load(jsonSchema);
    }

    private SchemaUtil() {
        // private constructor; static accessors only
    }
}
