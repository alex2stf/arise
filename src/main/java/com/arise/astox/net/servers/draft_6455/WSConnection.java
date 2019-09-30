package com.arise.astox.net.servers.draft_6455;


import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerMessage;
import com.arise.core.tools.StringUtil;

import java.nio.ByteBuffer;
import java.util.Random;

public class WSConnection extends DuplexDraft.Connection {

    private final AbstractServer server;
    private final Object sock; //streeamable socket or socket channel
    private final Object ssl;
    private final Object key;
    private final String id;
//    private final Draft_6455 draft_6455 = new Draft_6455();

    @Override
    public String toString() {
        return "WSConnection{" +
                "id='" + id + '\'' +
                '}';
    }

    public WSConnection(AbstractServer server, Object transporter, Object wrapper, Object key){
        this.server = server;
        this.sock = transporter;
        this.ssl = wrapper;
        this.id = generateId(transporter);
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void send(String message) {
        WebSocketFrame frame = new WebSocketFrame();
        frame.setOpcode(WebSocketOpcode.TEXT);
        frame.setPayload(message);

//        TextFrame textFrame = new TextFrame();
//        textFrame.setPayload(ByteBuffer.wrap(message.getBytes()));
//        byte[] bytes = draft_6455.createBinaryFrame(textFrame).array();

        WebSocketFrame textFrame = WebSocketFrame.createTextFrame(message);
        byte[] bytes = createByteBufferFromFramedata(textFrame).array();


        server.registerMessage(new ServerMessage(bytes, sock, ssl, key));
    }

    private String generateId(Object _s){
        return StringUtil.dump(_s);
    }

    public Object transporter() {
        return sock;
    }

    private static final Random reuseableRandom = new Random();

    public ByteBuffer createByteBufferFromFramedata( WebSocketFrame framedata ) {
        byte[] payload = framedata.getPayload();
        ByteBuffer mes = ByteBuffer.wrap(payload);

        boolean mask = false; //role == Role.CLIENT;
        int sizebytes = getSizeBytes(mes);
        ByteBuffer buf = ByteBuffer.allocate( 1 + ( sizebytes > 1 ? sizebytes + 1 : sizebytes ) + ( mask ? 4 : 0 ) + mes.remaining() );
        byte optcode = (byte) framedata.getOpcode();
//        byte optcode = fromOpcode( framedata.getOpcode() );
        byte one = ( byte ) ( framedata.isFin() ? -128 : 0 );
        one |= optcode;
        buf.put( one );
        byte[] payloadlengthbytes = toByteArray( mes.remaining(), sizebytes );
        assert ( payloadlengthbytes.length == sizebytes );

        if( sizebytes == 1 ) {
            buf.put( ( byte ) ( payloadlengthbytes[0] | getMaskByte(mask) ) );
        } else if( sizebytes == 2 ) {
            buf.put( ( byte ) ( ( byte ) 126 | getMaskByte(mask)));
            buf.put( payloadlengthbytes );
        } else if( sizebytes == 8 ) {
            buf.put( ( byte ) ( ( byte ) 127 | getMaskByte(mask)));
            buf.put( payloadlengthbytes );
        } else
            throw new IllegalStateException( "Size representation not supported/specified" );

        if( mask ) {
            ByteBuffer maskkey = ByteBuffer.allocate( 4 );
            maskkey.putInt( reuseableRandom.nextInt() );
            buf.put( maskkey.array() );
            for( int i = 0; mes.hasRemaining(); i++ ) {
                buf.put( ( byte ) ( mes.get() ^ maskkey.get( i % 4 ) ) );
            }
        } else {
            buf.put( mes );
            //Reset the position of the bytebuffer e.g. for additional use
            mes.flip();
        }
        assert ( buf.remaining() == 0 ) : buf.remaining();
        buf.flip();
        return buf;
    }


    private byte fromOpcode( FrameCode frameCode) {
        if( frameCode == FrameCode.CONTINUOUS )
            return 0;
        else if( frameCode == FrameCode.TEXT )
            return 1;
        else if( frameCode == FrameCode.BINARY )
            return 2;
        else if( frameCode == FrameCode.CLOSING )
            return 8;
        else if( frameCode == FrameCode.PING )
            return 9;
        else if( frameCode == FrameCode.PONG )
            return 10;
        throw new IllegalArgumentException( "Don't know how to handle " + frameCode.toString() );
    }

    private static byte[] toByteArray( long val, int bytecount ) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for( int i = 0; i < bytecount; i++ ) {
            buffer[i] = ( byte ) ( val >>> ( highest - 8 * i ) );
        }
        return buffer;
    }


    private int getSizeBytes(ByteBuffer mes) {
        if (mes.remaining() <= 125) {
            return 1;
        } else if (mes.remaining() <= 65535) {
            return 2;
        }
        return 8;
    }

    private static byte getMaskByte(boolean mask) {
        return mask ? ( byte ) -128 : 0;
    }
}
