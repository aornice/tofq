package xyz.aornice.tofq.network.serialize;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

/**
 * Created by drfish on 11/05/2017.
 */
public class JsonSerializable implements TofqSerializable {
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    @Override
    public byte[] serialize(Object obj) throws Exception {
        String json = JSON.toJSONString(obj, false);
        if (json != null) {
            return json.getBytes(CHARSET_UTF8);
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        String json = new String(data, CHARSET_UTF8);
        return JSON.parseObject(json, clazz);
    }
}
