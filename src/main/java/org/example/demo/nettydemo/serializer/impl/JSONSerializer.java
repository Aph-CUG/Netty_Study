package org.example.demo.nettydemo.serializer.impl;

import com.alibaba.fastjson.JSON;
import org.example.demo.nettydemo.serializer.Serializer;
import org.example.demo.nettydemo.serializer.SerializerAlgorithm;

public class JSONSerializer implements Serializer {

    @Override
    public byte getSerializerAlgorithm() {
        return SerializerAlgorithm.JSON;
    }

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
