package com.arise.astox.net.servers.nio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NIOBytesSolver {
    List<byte[]> byteBufferList = new ArrayList<>();

    public void add(ByteBuffer byteBuffer){
        if (byteBuffer.capacity() - byteBuffer.remaining() > 0){
            byte [] copy = new byte[byteBuffer.capacity() - byteBuffer.remaining()];
            byteBuffer.rewind();
            byteBuffer.get(copy, 0, copy.length);
            byteBufferList.add(copy);
        }
    }

    public byte[] getAll(){
        int size = 0;
        for (byte[] bytes: byteBufferList){
            size += bytes.length;
        }
        byte[] concat = new byte[size];
        int ll = 0;

        for (int i = 0; i < byteBufferList.size(); i++){
            byte[] item = byteBufferList.get(i);
            for (int j = 0; j < item.length; j++){
                concat[j + ll] = item[j];
            }
            ll += item.length;
        }
        return concat;
    }
}
