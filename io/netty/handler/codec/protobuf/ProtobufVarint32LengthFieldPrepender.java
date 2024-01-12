package io.netty.handler.codec.protobuf;

import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class ProtobufVarint32LengthFieldPrepender extends MessageToByteEncoder {
   protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
      int bodyLen = msg.readableBytes();
      int headerLen = CodedOutputStream.computeRawVarint32Size(bodyLen);
      out.ensureWritable(headerLen + bodyLen);
      CodedOutputStream headerOut = CodedOutputStream.newInstance(new ByteBufOutputStream(out), headerLen);
      headerOut.writeRawVarint32(bodyLen);
      headerOut.flush();
      out.writeBytes(msg, msg.readerIndex(), bodyLen);
   }
}
