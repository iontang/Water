package designpatterns.reactor.demo3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class SimpleReactor {


    public static void main(String[] args) throws IOException {

        System.out.println("start   ");
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(1234));
        /**
         * 代码中socketServerChannel对象只注册了OP_ACCEPT事件
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) {
            System.out.println("selector.select() > 0");

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {

                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {

                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();

                    /**
                     * 而socketChannel对象只注册了OP_READ事件。
                     */
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);

                    System.out.println("Accept request from :" + socketChannel.getRemoteAddress());
                    socketChannel.register(selector, SelectionKey.OP_READ);

                } else if (key.isReadable()) {

                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = socketChannel.read(buffer);

                    if (count <= 0) {

                        socketChannel.close();
                        key.cancel();
                        System.out.println("Received invalide data, close the connection");
                        continue;

                    }

                    System.out.println("Received message {}" + new String(buffer.array()));

                }

                keys.remove(key);

            }
        }
    }


}
