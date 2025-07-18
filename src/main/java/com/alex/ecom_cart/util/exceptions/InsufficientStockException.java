package com.alex.ecom_cart.util.exceptions;

public class InsufficientStockException extends RuntimeException{
    private static final String ERROR_MESSAGE = "insufficient stock for this product : %s";
    public InsufficientStockException(String productName) {
        super(String.format(ERROR_MESSAGE, productName));
    }
}
