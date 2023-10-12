package org.example.demo.nettydemo.serializer;

import org.example.demo.nettydemo.serializer.impl.JSONSerializer;

public interface Serializer {


    Serializer DEFAULT = new JSONSerializer();

    /**
     * 获取序列化算法
     * @return
     */
    byte getSerializerAlgorithm();

    /**
     * java对象转成二进制数据
     * @param object
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 二进制数据转成Java对象
     * @param clazz
     * @param bytes
     * @return
     * @param <T>
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);
}
