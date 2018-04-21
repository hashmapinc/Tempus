package com.hashmapinc.server.actors.computation;

public class ComputationInitializationException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public ComputationInitializationException(String msg, Exception e) {
        super(msg, e);
    }

    public ComputationInitializationException(String msg) {
        super(msg, null);
    }
}
