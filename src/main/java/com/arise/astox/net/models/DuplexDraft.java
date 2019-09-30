package com.arise.astox.net.models;

import java.io.Closeable;
import java.io.InputStream;

public abstract class DuplexDraft<RequestDataType extends ServerRequest, ResponseDataType extends ServerResponse> {
    public  abstract Connection createConnection(AbstractServer server, Object transporter, Object wrapper, Object key);
    public  abstract boolean isValidHandshakeRequest(RequestDataType request);
    public  abstract ResponseDataType getHandshakeResponse(RequestDataType request);
    public  abstract DuplexInputStream buildInputStream(InputStream inputStream);

    public  abstract void parseBytes(byte[] readedBytes, Connection connection, ParseEvent parseEvent);

    public interface ParseEvent{
        void onFrameFound(Frame frame, Connection connection);
        void onError(Throwable err);
    }

    public static abstract class Connection {
        public abstract void send(String message);
    }

    public static abstract class Frame {
        public abstract String getPayloadText();
        public abstract byte[] getPayload();
        public abstract int getPayloadLength();
        public abstract int getCloseCode();
        public abstract String getCloseReason();
        public abstract boolean isCloseFrame();
    }

    public interface DuplexInputStream extends Closeable {
        Frame readFrame() throws Exception;
    }
}
