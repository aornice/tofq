package xyz.aornice.tofq.furnisher.message;

import xyz.aornice.tofq.furnisher.util.Recyclable;

public interface Message extends Recyclable{

    Operation getOp();

    Recyclable getPayload();

}
