package com.geulkkoli.infrastructure;

public class EmptyDataException extends RuntimeException{
    public EmptyDataException(String message) {
        super(message);
    }
}
