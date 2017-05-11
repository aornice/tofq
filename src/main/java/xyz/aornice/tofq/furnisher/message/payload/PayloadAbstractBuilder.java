package xyz.aornice.tofq.furnisher.message.payload;

import xyz.aornice.tofq.furnisher.util.support.AbstractBuilder;

public abstract class PayloadAbstractBuilder<E extends Payload> extends AbstractBuilder implements PayloadBuilder {

    PayloadAbstractBuilder() {
        super();
    }

    PayloadAbstractBuilder(int initial) {
        super(initial);
    }

    @SuppressWarnings("unchecked")
    protected E build() {
        return (E)super.build();
    }
}
