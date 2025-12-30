package com.karandev.aether.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.sql.results.graph.collection.internal.ListInitializer;
import org.springframework.http.HttpStatus;

import javax.management.InstanceAlreadyExistsException;
import java.time.Instant;
import java.util.List;

public record ApiError(
        HttpStatus status,
        String message,
        Instant timeStamp,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<ApiFieldError> errors
) {

    public ApiError(HttpStatus status, String message){
        this(status, message, Instant.now(), null);
    }

    public ApiError(HttpStatus status, String message, List<ApiFieldError> errors) {
        this(status, message, Instant.now(), errors);
    }
}

record ApiFieldError(String field, String message){}
