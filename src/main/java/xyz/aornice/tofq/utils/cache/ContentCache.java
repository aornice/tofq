package xyz.aornice.tofq.utils.cache;

import java.util.List;

/**
 * Created by shen on 2017/5/13.
 */
public interface ContentCache extends Cache<List<byte[]>> {
    int notCacheSize();
}
