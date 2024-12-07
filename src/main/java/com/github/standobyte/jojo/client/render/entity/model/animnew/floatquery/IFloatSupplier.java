package com.github.standobyte.jojo.client.render.entity.model.animnew.floatquery;

import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

//TODO math equations support
public interface IFloatSupplier {
    float get(AnimContext animContext);
    default boolean isNumericLiteral() { return false; }
    void multiplyNumerics(float multiplier);
    
    
    static final String NYI_PATTERN = ".*" + Pattern.quote("+-*/()?: ") + ".*";
    public static IFloatSupplier elementFromJson(JsonElement json) {
        if (!json.isJsonPrimitive()) {
            throw new IllegalArgumentException();
        }
        JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
        try {
            return new FloatQuery(jsonPrimitive.getAsFloat());
        }
        catch (NumberFormatException e) {
            String string = jsonPrimitive.getAsString();
            if (string.matches(NYI_PATTERN)) {
                throw new IllegalArgumentException(String.format(
                        "Math expressions or formulas are not yet supported (%s).", string));
            }
            FloatQuery.QueryType queryType = FloatQuery.QueryType.fromName(string);
            if (queryType == null) {
                throw new IllegalArgumentException(String.format("Unknown query (%s).", string));
            }
            return new FloatQuery(queryType);
        }
    }
}
