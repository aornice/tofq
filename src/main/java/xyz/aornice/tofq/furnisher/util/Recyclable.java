package xyz.aornice.tofq.furnisher.util;


import io.netty.util.ReferenceCounted;

public interface Recyclable extends ReferenceCounted {

    boolean releaseDeep();

}
