package com.arise.astox.net.models;

public class ServerMessage extends ConnectionSolver {
    private final byte[] _byt;

    public byte[] bytes() {
        return _byt;
    }

    public ServerMessage(byte[] bytes, Object ... args){
        super(args);
        this._byt = bytes;
    }
}
