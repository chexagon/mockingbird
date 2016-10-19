package com.crimsonhexagon.mockingbird.jsonschema;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
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
    public static Schema loadSchemaFromClasspathResource(String resourceLocation) {
	    return loadSchemaFromInputStream(SchemaUtil.class.getResourceAsStream(resourceLocation));
    }

	/**
	 * Loads a {@link Schema} from a JSON Schema {@link File}.
	 *
	 * @param file {@link File} JSON Schema file
	 * @return {@link Schema} JSON Schema object
	 * @throws JSONException thrown when the JSON Schema fails to parse
	 */
    public static Schema loadSchemaFromFile(File file) {
	    try {
		    return loadSchemaFromInputStream(new FileInputStream(file));
	    } catch (FileNotFoundException e) {
		    // missing file, in this case we return null
	    }
	    return null;
    }

	/**
	 * Loads a {@link Schema} from a JSON Schema {@link InputStream}.
	 *
	 * @param inputStream {@link InputStream} JSON Schema input stream
	 * @return {@link Schema} JSON Schema object
	 * @throws JSONException thrown when the JSON Schema fails to parse
	 */
    public static Schema loadSchemaFromInputStream(InputStream inputStream) {
	    if (inputStream == null) return null;

	    JSONObject jsonSchema = new JSONObject(new JSONTokener(new InputStreamReader(inputStream, UTF8)));
	    return SchemaLoader.load(jsonSchema);

    }

    private SchemaUtil() {
        // private constructor; static accessors only
    }
}
