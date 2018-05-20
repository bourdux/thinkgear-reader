package org.jbourdon.thinkgear.producer;

public class SupplierErrorException extends RuntimeException {

    public SupplierErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
