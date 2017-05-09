package xyz.aornice.tofq.furnisher.message;

import xyz.aornice.tofq.furnisher.message.payload.PutBuilder;
import xyz.aornice.tofq.furnisher.util.Builder;

public enum Operation {

    PUT(0, new PutBuilder()),
    REGISTER(1, null);

    private final int value;
    private final Builder builder;

    Operation(int value, Builder builder) {
        this.value = value;
        this.builder = builder;
    }

    public static Operation valueOf(int value) {
        switch (value) {
            case 0:
                return PUT;
            case 1:
                return REGISTER;
            default:
                return null;
        }
    }

    public int value() {
        return value;
    }

    public Builder getBuilder() {
        return builder;
    }

}
