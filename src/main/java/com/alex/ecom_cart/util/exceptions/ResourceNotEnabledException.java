package com.alex.ecom_cart.util.exceptions;

public class ResourceNotEnabledException extends RuntimeException{
    private static final String MESAAGE_ERROR = "Cannot proceed: %s is not enabled";

    public ResourceNotEnabledException(String resourceName) {
        super(String.format(MESAAGE_ERROR, resourceName));
    }
}
