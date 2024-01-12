package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.LastHttpContent;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

public final class HttpClientCodec extends CombinedChannelDuplexHandler {
   private final Queue queue;
   private boolean done;
   private final AtomicLong requestResponseCounter;
   private final boolean failOnMissingResponse;

   public HttpClientCodec() {
      this(4096, 8192, 8192, false);
   }

   public void setSingleDecode(boolean singleDecode) {
      ((HttpResponseDecoder)this.inboundHandler()).setSingleDecode(singleDecode);
   }

   public boolean isSingleDecode() {
      return ((HttpResponseDecoder)this.inboundHandler()).isSingleDecode();
   }

   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, false);
   }

   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse) {
      this(maxInitialLineLength, maxHeaderSize, maxChunkSize, failOnMissingResponse, true);
   }

   public HttpClientCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean failOnMissingResponse, boolean validateHeaders) {
      this.queue = new ArrayDeque();
      this.requestResponseCounter = new AtomicLong();
      this.init(new HttpClientCodec.Decoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders), new HttpClientCodec.Encoder());
      this.failOnMissingResponse = failOnMissingResponse;
   }

   private final class Decoder extends HttpResponseDecoder {
      Decoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
         super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders);
      }

      protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List out) throws Exception {
         if(HttpClientCodec.this.done) {
            int readable = this.actualReadableBytes();
            if(readable == 0) {
               return;
            }

            out.add(buffer.readBytes(readable));
         } else {
            int oldSize = out.size();
            super.decode(ctx, buffer, out);
            if(HttpClientCodec.this.failOnMissingResponse) {
               int size = out.size();

               for(int i = oldSize; i < size; ++i) {
                  this.decrement(out.get(i));
               }
            }
         }

      }

      private void decrement(Object msg) {
         if(msg != null) {
            if(msg instanceof LastHttpContent) {
               HttpClientCodec.this.requestResponseCounter.decrementAndGet();
            }

         }
      }

      protected boolean isContentAlwaysEmpty(HttpMessage msg) {
         int statusCode = ((HttpResponse)msg).getStatus().code();
         if(statusCode == 100) {
            return true;
         } else {
            HttpMethod method = (HttpMethod)HttpClientCodec.this.queue.poll();
            char firstChar = method.name().charAt(0);
            switch(firstChar) {
            case 'C':
               if(statusCode == 200 && HttpMethod.CONNECT.equals(method)) {
                  HttpClientCodec.this.done = true;
                  HttpClientCodec.this.queue.clear();
                  return true;
               }
               break;
            case 'H':
               if(HttpMethod.HEAD.equals(method)) {
                  return true;
               }
            }

            return super.isContentAlwaysEmpty(msg);
         }
      }

      public void channelInactive(ChannelHandlerContext ctx) throws Exception {
         super.channelInactive(ctx);
         if(HttpClientCodec.this.failOnMissingResponse) {
            long missingResponses = HttpClientCodec.this.requestResponseCounter.get();
            if(missingResponses > 0L) {
               ctx.fireExceptionCaught(new PrematureChannelClosureException("channel gone inactive with " + missingResponses + " missing response(s)"));
            }
         }

      }
   }

   private final class Encoder extends HttpRequestEncoder {
      private Encoder() {
      }

      protected void encode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
         if(msg instanceof HttpRequest && !HttpClientCodec.this.done) {
            HttpClientCodec.this.queue.offer(((HttpRequest)msg).getMethod());
         }

         super.encode(ctx, msg, out);
         if(HttpClientCodec.this.failOnMissingResponse && msg instanceof LastHttpContent) {
            HttpClientCodec.this.requestResponseCounter.incrementAndGet();
         }

      }
   }
}
