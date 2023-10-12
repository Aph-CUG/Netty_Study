package org.example.demo.nettydemo.protocol.command;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.example.demo.nettydemo.serializer.Serializer;
import org.example.demo.nettydemo.serializer.impl.JSONSerializer;

import java.util.HashMap;
import java.util.Map;

import java.nio.Buffer;

import static org.example.demo.nettydemo.protocol.command.Command.LOGIN_REQUEST;

//编码： 将数据编码到二进制包中
public class PacketCodeC {
    private static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer> serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializer();
        serializerMap.put(serializer.getSerializerAlgorithm(), serializer);
    }


    public ByteBuf encode(Packet packet) {
        //使用Netty的ByteBuf分配器来创建，ioBuffer()方法返回适配IO读写相关的内存
        //会尽可能创建一个直接内存   不受JVM管理的内存空间，写道IO缓冲区的效果更高
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();

        //序列化Java对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);

        // 编码打包
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }


    public Packet decode(ByteBuf byteBuf) {
        // 跳过魔数
        byteBuf.skipBytes(4);

        //跳过版本号
        byteBuf.skipBytes(1);

        //读取序列化算法
        byte serializerAlgorithm = byteBuf.readByte();

        //读取指令
        byte command = byteBuf.readByte();

        //读取数据包长度
        int length = byteBuf.readInt();

        //读取数据
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);


        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializerAlgorithm);

        if(requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }
        return null;

    }



    private Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }
}
