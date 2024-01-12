package org.apache.commons.codec.binary;

import org.apache.commons.codec.binary.BaseNCodec;
import org.apache.commons.codec.binary.StringUtils;

public class Base32 extends BaseNCodec {
   private static final int BITS_PER_ENCODED_BYTE = 5;
   private static final int BYTES_PER_ENCODED_BLOCK = 8;
   private static final int BYTES_PER_UNENCODED_BLOCK = 5;
   private static final byte[] CHUNK_SEPARATOR = new byte[]{(byte)13, (byte)10};
   private static final byte[] DECODE_TABLE = new byte[]{(byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)26, (byte)27, (byte)28, (byte)29, (byte)30, (byte)31, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)0, (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15, (byte)16, (byte)17, (byte)18, (byte)19, (byte)20, (byte)21, (byte)22, (byte)23, (byte)24, (byte)25};
   private static final byte[] ENCODE_TABLE = new byte[]{(byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)71, (byte)72, (byte)73, (byte)74, (byte)75, (byte)76, (byte)77, (byte)78, (byte)79, (byte)80, (byte)81, (byte)82, (byte)83, (byte)84, (byte)85, (byte)86, (byte)87, (byte)88, (byte)89, (byte)90, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55};
   private static final byte[] HEX_DECODE_TABLE = new byte[]{(byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)0, (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15, (byte)16, (byte)17, (byte)18, (byte)19, (byte)20, (byte)21, (byte)22, (byte)23, (byte)24, (byte)25, (byte)26, (byte)27, (byte)28, (byte)29, (byte)30, (byte)31, (byte)32};
   private static final byte[] HEX_ENCODE_TABLE = new byte[]{(byte)48, (byte)49, (byte)50, (byte)51, (byte)52, (byte)53, (byte)54, (byte)55, (byte)56, (byte)57, (byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)71, (byte)72, (byte)73, (byte)74, (byte)75, (byte)76, (byte)77, (byte)78, (byte)79, (byte)80, (byte)81, (byte)82, (byte)83, (byte)84, (byte)85, (byte)86};
   private static final int MASK_5BITS = 31;
   private final int decodeSize;
   private final byte[] decodeTable;
   private final int encodeSize;
   private final byte[] encodeTable;
   private final byte[] lineSeparator;

   public Base32() {
      this(false);
   }

   public Base32(boolean useHex) {
      this(0, (byte[])null, useHex);
   }

   public Base32(int lineLength) {
      this(lineLength, CHUNK_SEPARATOR);
   }

   public Base32(int lineLength, byte[] lineSeparator) {
      this(lineLength, lineSeparator, false);
   }

   public Base32(int lineLength, byte[] lineSeparator, boolean useHex) {
      super(5, 8, lineLength, lineSeparator == null?0:lineSeparator.length);
      if(useHex) {
         this.encodeTable = HEX_ENCODE_TABLE;
         this.decodeTable = HEX_DECODE_TABLE;
      } else {
         this.encodeTable = ENCODE_TABLE;
         this.decodeTable = DECODE_TABLE;
      }

      if(lineLength > 0) {
         if(lineSeparator == null) {
            throw new IllegalArgumentException("lineLength " + lineLength + " > 0, but lineSeparator is null");
         }

         if(this.containsAlphabetOrPad(lineSeparator)) {
            String sep = StringUtils.newStringUtf8(lineSeparator);
            throw new IllegalArgumentException("lineSeparator must not contain Base32 characters: [" + sep + "]");
         }

         this.encodeSize = 8 + lineSeparator.length;
         this.lineSeparator = new byte[lineSeparator.length];
         System.arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
      } else {
         this.encodeSize = 8;
         this.lineSeparator = null;
      }

      this.decodeSize = this.encodeSize - 1;
   }

   void decode(byte[] in, int inPos, int inAvail, BaseNCodec.Context context) {
      if(!context.eof) {
         if(inAvail < 0) {
            context.eof = true;
         }

         for(int i = 0; i < inAvail; ++i) {
            byte b = in[inPos++];
            if(b == 61) {
               context.eof = true;
               break;
            }

            byte[] buffer = this.ensureBufferSize(this.decodeSize, context);
            if(b >= 0 && b < this.decodeTable.length) {
               int result = this.decodeTable[b];
               if(result >= 0) {
                  context.modulus = (context.modulus + 1) % 8;
                  context.lbitWorkArea = (context.lbitWorkArea << 5) + (long)result;
                  if(context.modulus == 0) {
                     buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 32 & 255L));
                     buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 24 & 255L));
                     buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 16 & 255L));
                     buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 8 & 255L));
                     buffer[context.pos++] = (byte)((int)(context.lbitWorkArea & 255L));
                  }
               }
            }
         }

         if(context.eof && context.modulus >= 2) {
            byte[] buffer = this.ensureBufferSize(this.decodeSize, context);
            switch(context.modulus) {
            case 2:
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 2 & 255L));
               break;
            case 3:
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 7 & 255L));
               break;
            case 4:
               context.lbitWorkArea >>= 4;
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 8 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea & 255L));
               break;
            case 5:
               context.lbitWorkArea >>= 1;
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 16 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 8 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea & 255L));
               break;
            case 6:
               context.lbitWorkArea >>= 6;
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 16 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 8 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea & 255L));
               break;
            case 7:
               context.lbitWorkArea >>= 3;
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 24 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 16 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea >> 8 & 255L));
               buffer[context.pos++] = (byte)((int)(context.lbitWorkArea & 255L));
               break;
            default:
               throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
         }

      }
   }

   void encode(byte[] in, int inPos, int inAvail, BaseNCodec.Context context) {
      if(!context.eof) {
         if(inAvail < 0) {
            context.eof = true;
            if(0 == context.modulus && this.lineLength == 0) {
               return;
            }

            byte[] buffer = this.ensureBufferSize(this.encodeSize, context);
            int savedPos = context.pos;
            switch(context.modulus) {
            case 0:
               break;
            case 1:
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 3) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea << 2) & 31];
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               break;
            case 2:
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 11) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 6) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 1) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea << 4) & 31];
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               break;
            case 3:
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 19) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 14) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 9) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 4) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea << 1) & 31];
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               buffer[context.pos++] = 61;
               break;
            case 4:
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 27) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 22) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 17) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 12) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 7) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 2) & 31];
               buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea << 3) & 31];
               buffer[context.pos++] = 61;
               break;
            default:
               throw new IllegalStateException("Impossible modulus " + context.modulus);
            }

            context.currentLinePos += context.pos - savedPos;
            if(this.lineLength > 0 && context.currentLinePos > 0) {
               System.arraycopy(this.lineSeparator, 0, buffer, context.pos, this.lineSeparator.length);
               context.pos += this.lineSeparator.length;
            }
         } else {
            for(int i = 0; i < inAvail; ++i) {
               byte[] buffer = this.ensureBufferSize(this.encodeSize, context);
               context.modulus = (context.modulus + 1) % 5;
               int b = in[inPos++];
               if(b < 0) {
                  b += 256;
               }

               context.lbitWorkArea = (context.lbitWorkArea << 8) + (long)b;
               if(0 == context.modulus) {
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 35) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 30) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 25) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 20) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 15) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 10) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)(context.lbitWorkArea >> 5) & 31];
                  buffer[context.pos++] = this.encodeTable[(int)context.lbitWorkArea & 31];
                  context.currentLinePos += 8;
                  if(this.lineLength > 0 && this.lineLength <= context.currentLinePos) {
                     System.arraycopy(this.lineSeparator, 0, buffer, context.pos, this.lineSeparator.length);
                     context.pos += this.lineSeparator.length;
                     context.currentLinePos = 0;
                  }
               }
            }
         }

      }
   }

   public boolean isInAlphabet(byte octet) {
      return octet >= 0 && octet < this.decodeTable.length && this.decodeTable[octet] != -1;
   }
}
