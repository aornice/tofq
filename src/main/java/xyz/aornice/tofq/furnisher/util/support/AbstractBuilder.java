package xyz.aornice.tofq.furnisher.util.support;

import io.netty.util.ReferenceCounted;
import xyz.aornice.tofq.furnisher.util.Builder;
import xyz.aornice.tofq.furnisher.util.Recyclable;
import xyz.aornice.tofq.util.Pool;
import xyz.aornice.tofq.util.support.MQueuePool;

import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractBuilder<E extends AbstractBuilder.Element> implements Builder<E>{

    private final static int DEFAULT_INITIAL = 1 << 4;

    Pool<E> pool = new MQueuePool<>();

    protected AbstractBuilder() {
        this(DEFAULT_INITIAL);
    }

    protected AbstractBuilder(int initial) {
        for (int i = 0; i < initial; i++) pool.offer(buildPartial());
    }

    @Override
    public E build() {
        E e = pool.poll();
        if (e != null) return e;
        e = buildPartial();
        return e;
    }

    void releaseToPool(E e) {
        e.reset();
        pool.offer(e);
    }

    abstract protected E buildPartial();

    public abstract class Element implements Recyclable {
        private AtomicInteger count = new AtomicInteger(1);

        @Override
        public int refCnt() {
            return count.get();
        }

        public ReferenceCounted retain() {
            count.incrementAndGet();
            return this;
        }

        @Override
        public ReferenceCounted retain(int increment) {
            count.addAndGet(increment);
            return this;
        }

        @Override
        public ReferenceCounted touch() {
            return this;
        }

        @Override
        public ReferenceCounted touch(Object hint) {
            return this;
        }

        @Override
        public boolean release() {
            return release(1);
        }

        @Override
        public boolean release(int decrement) {
            if (count.addAndGet(-decrement) > 0) return false;
            releaseHelper();
            releaseToPool((E) this);
            return true;
        }

        public boolean releaseDeep() {
            return releaseDeep(1);
        }

        public boolean releaseDeep(int decrement) {
            if (count.decrementAndGet() > 0) return false;
            releaseDeepHelper();
            releaseToPool((E) this);
            return false;
        }

        public abstract void reset();

        protected abstract void releaseHelper();

        protected abstract void releaseDeepHelper();

    }

    public class ElementAdapter extends Element {

        @Override
        public void reset() {
        }

        @Override
        protected void releaseHelper() {
        }

        @Override
        protected void releaseDeepHelper() {
        }
    }
}
