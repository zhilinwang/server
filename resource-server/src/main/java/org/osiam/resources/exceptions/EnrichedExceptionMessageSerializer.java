package org.osiam.resources.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Class to serialize an EnrichedExceptionMessage and to wrap the JsonProcessingException
 *
 */
public class EnrichedExceptionMessageSerializer {

    public static String serialize(EnrichedExceptionMessage enrichedExceptionMessage){
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String exceptionString;
        try {
            exceptionString = objectWriter.writeValueAsString(enrichedExceptionMessage);
        } catch (JsonProcessingException e) {
            exceptionString = "Could not serialize Exception: " + enrichedExceptionMessage.toString();
        }
        return exceptionString;
    }
}
