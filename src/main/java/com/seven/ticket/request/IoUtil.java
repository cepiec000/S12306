package com.seven.ticket.request;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/4 17:22
 * @Version V1.0
 **/
public class IoUtil {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int DEFAULT_MIDDLE_BUFFER_SIZE = 16384;
    public static final int DEFAULT_LARGE_BUFFER_SIZE = 32768;
    public static final int EOF = -1;

    public IoUtil() {
    }

    public static long copyByNIO(InputStream in, OutputStream out, int bufferSize) throws IOException {
        return copy(Channels.newChannel(in), Channels.newChannel(out), bufferSize);
    }

    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize <= 0 ? 8192 : bufferSize);
        long size = 0L;

        try {
            while(in.read(byteBuffer) != -1) {
                byteBuffer.flip();
                size += (long)out.write(byteBuffer);
                byteBuffer.clear();
            }
        } catch (IOException var8) {
            throw new IOException(var8);
        }finally {
            out.close();
            in.close();
        }
        return size;
    }
}
