package com.example.redisdemo.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //获取客户端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        String message = byteBuf.toString(CharsetUtil.UTF_8);
        String[] split = message.split("\n\t");
        for(int i = 0;i < split.length;i++){
            String[] strings = split[i].split(" ");
            String command = strings[0];
            String response = null;

            switch(command){
                case "set":
                    //获取文件
                    RandomAccessFile write = new RandomAccessFile(
                            "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data1.txt","rw");
                    FileChannel channel = write.getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                    channel.read(buffer);
                    buffer.flip();

                    //将需要set的对象转为byte[]数组
                    byte[] keyByte = strings[1].getBytes();
                    byte[] bByte = " ".getBytes();
                    byte[] valueByte = strings[2].getBytes();
                    byte[] bByte1 = "\n".getBytes();

                    //创建新的buffer替换老的buffer
                    ByteBuffer newBuffer = ByteBuffer.allocate(
                            buffer.remaining()+keyByte.length+bByte.length + valueByte.length + bByte1.length);

                    //将老的buffer的内容转换为String
                    String getString = Charset.forName("UTF-8").decode(buffer).toString();
                    String[] kV = getString.split("\n");

                    //记录老的buffer的长度
                    int originalLimit = buffer.limit();
                    int originalength = 0;
                    for(String s : kV){
                        String[] keyValue = s.split(" ");
                        //记录不需要被替换内容的长度，因为从0开始，需要+1
                        originalength = originalength + keyValue.length + 1;
                        if(keyValue[0].equals(strings[1])){
                            //标记不需要替换内容的头尾位置
                            buffer.limit(originalength);
                            buffer.position(0);
                            while (buffer.hasRemaining()){
                                newBuffer.put(buffer.get());
                            }
                            //将需要替换的部分用新的内容覆盖
                            newBuffer.put(keyByte).put(bByte).put(valueByte).put(bByte1);
                            //记录替换内容的长度，将剩余的部分写进新的buffer中
                            int replaceLength = keyValue.length + bByte.length + valueByte.length + bByte1.length;
                            //必须先设置limit，不然会因为超界而报错
                            buffer.limit(originalLimit);
                            //keyValue[1].length() < valueByte.length 需要考虑
                            if(keyValue[1].length() > valueByte.length){
                                replaceLength = keyValue.length + bByte.length + keyValue[1].length() + bByte1.length;
                                buffer.position(originalength+replaceLength -1);
                            }else{
                                buffer.position(originalength+replaceLength -1);
                            }

                            while (buffer.hasRemaining()){
                                newBuffer.put(buffer.get());
                            }
                            newBuffer.flip();
                            channel.truncate(0);
                            channel.write(newBuffer);
                            response = "ok";
                            write.close();
                            break;
                        }
                    }
                    if(!Objects.equals(response, "ok")){
                        newBuffer.put(buffer).put(keyByte).put(bByte).put(valueByte).put(bByte1).flip();
                        //channel.truncate(0);
                        channel.write(newBuffer);
                        response = "ok";
                        write.close();
                    }
                    break;
                case "get":
                    RandomAccessFile read = new RandomAccessFile(
                            "C:\\Users\\cfcz4\\OneDrive\\Desktop\\data1.txt","rw");
                    FileChannel readChannel = read.getChannel();
                    long length = read.length();
                    ByteBuffer byteBuffer = ByteBuffer.allocate((int) length);
                    readChannel.read(byteBuffer);
                    byteBuffer.flip();

                    String string = Charset.forName("UTF-8").decode(byteBuffer).toString();
                    String[] split1 = string.split("\n");

                    for(String s : split1){
                        String[] keyValue = s.split(" ");
                        if(keyValue[0].equals(strings[1])){
                            response = keyValue[1];
                            break;
                        }
                    }
                    read.close();
                    break;
                default:
                    response = "wrong";
            }
            System.out.println("收到客户端" + ctx.channel().remoteAddress() + "发送的消息：" + response);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //发送消息给客户端
        ctx.writeAndFlush(Unpooled.copiedBuffer("服务端已连接?", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }


}
