package xyz.aornice.tofq.utils.impl;

import java.util.List;

/**
 * Created by shen on 2017/4/14.
 */
public class Deposit {
    private List<byte[]> msgs;

    public Deposit(List<byte[]> msgs) {
        this.msgs = msgs;
    }

    public List<byte[]> getMsgs() {
        return msgs;
    }

}
