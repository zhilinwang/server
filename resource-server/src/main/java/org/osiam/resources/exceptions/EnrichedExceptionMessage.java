package org.osiam.resources.exceptions;

public class EnrichedExceptionMessage {

    private ExceptionType exceptionType;
    private String errorMessage;
    
    public EnrichedExceptionMessage(){
        //for json
    }
    
    public EnrichedExceptionMessage( ExceptionType exceptionType, String errorMessage){
        this.exceptionType = exceptionType;
        this.errorMessage = errorMessage;
    }
    
    public ExceptionType getExceptionType(){
        return exceptionType;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
    
}
