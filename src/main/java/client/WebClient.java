package client;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by frmark on 2017/8/30.
 */
public class WebClient {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        //setup socket channel
        SocketChannel socketChannel = SocketChannel.open();
        //作为客户端连接服务器
        socketChannel.connect(new InetSocketAddress("localhost", 5000));
        //分配缓存区内存大小
        ByteBuffer readBuffer = ByteBuffer.allocate(32);
        ByteBuffer writeBuffer = ByteBuffer.allocate(32);

        String processName = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        writeBuffer.put(processName.getBytes());
        writeBuffer.flip();

        //一般往buffer写数据或者读数据的时候，对缓存区进行重置
        writeBuffer.rewind();
        socketChannel.write(writeBuffer);
        FutureTask<ByteBuffer> futureTask = new FutureTask<ByteBuffer>(new Callable<ByteBuffer>() {
            @Override
            public ByteBuffer call() throws Exception {
                readBuffer.clear();
                socketChannel.read(readBuffer);
                return readBuffer;
            }
        });
        new Thread(futureTask).start();
        System.out.println("main continue");
        System.out.println(new String(futureTask.get().array()));
    }
}
