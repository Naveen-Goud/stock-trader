package com.trading.trading.exception;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String message) { super(message); }
}
