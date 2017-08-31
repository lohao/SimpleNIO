package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/*
    NIO的核心就是Selector，对于客户端的每一次请求到来时不需要立即创建一个进程进行处理
    以TCP的形式从网络中读取数据
 */

/**
 * Created by frmark on 2017/8/30.
 */
public class WebServer {

    public static void main(java.lang.String[] args) throws IOException, InterruptedException {

        // setup server socket channel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //bind IP+Port
        ssc.socket().bind(new InetSocketAddress("localhost", 5000));
        ssc.configureBlocking(false);

        //开启多路复用IO
        Selector selector = Selector.open();
        //保证selector能够监听到多个channel
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer readBuffer = ByteBuffer.allocate(32);
        ByteBuffer writeBuffer = ByteBuffer.allocate(32);

        //相对写， 把数据写入到position和limit之间
        writeBuffer.put("recived".getBytes());
        //limit = position position = 0； 翻转就是将一个处于存数据状态的缓冲区变为一个处于准备取数据的状态
        writeBuffer.flip();
        while (true) {
            //选择一些I／O操作准备好的管道，每个管道对应着一个key，这个方法是一个阻塞的选择操作，
            // 当至少有一个通道被选择时才返回。
            selector.select();
            //一组选择的通道的集合，也就是一组客户端的连接
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                //测试是否可以联通客户端
                if (key.isAcceptable()) {
                    //建立相应测channel
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    readBuffer.clear();
                    channel.read(readBuffer);
                    readBuffer.flip();
                    System.out.println(new java.lang.String(readBuffer.array()));
                    Thread.sleep(5000);
                    key.interestOps(SelectionKey.OP_WRITE);
                } else if (key.isWritable()) {
                    writeBuffer.rewind();
                    SocketChannel channel = (SocketChannel) key.channel();
                    channel.write(writeBuffer);
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }
}
