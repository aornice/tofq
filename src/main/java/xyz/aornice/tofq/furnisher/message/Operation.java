package xyz.aornice.tofq.furnisher.message;

import xyz.aornice.tofq.furnisher.message.payload.PayloadBuilder;
import xyz.aornice.tofq.furnisher.message.payload.PutBuilder;
import xyz.aornice.tofq.furnisher.message.payload.PutResponseBuilder;

public enum Operation {

    PUT(0, new PutBuilder()),
    PUT_RESP(1, new PutResponseBuilder());

    private final int value;
    private final PayloadBuilder builder;

    Operation(int value, PayloadBuilder builder) {
        this.value = value;
        this.builder = builder;
    }

    public static Operation valueOf(int value) {
        switch (value) {
            case 0:
                return PUT;
            case 1:
                return PUT_RESP;
            default:
                return null;
        }
    }

    public int value() {
        return value;
    }

    public PayloadBuilder getBuilder() {
        return builder;
    }

}
