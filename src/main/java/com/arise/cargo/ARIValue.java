package com.arise.cargo;

public class ARIValue {



    private final Object[] _args;
    private final ARIProp _prop;
    private final Scope _scope;


    private ARIValue(ARIProp prop, Scope scope, Object ... values) {
        this._args = values;
        this._prop = prop;
        this._scope = scope;
    }

    private ARIValue(ARIProp prop, Object ... values){
        this(prop, Scope.CREATE, values);
    }

    public Object value() {
        return _args[0];
    }
    public Object[] values() {
        return _args;
    }

    public int argsSize(){
        return _args != null ? _args.length : 0;
    }

    public ARIProp prop() {
        return _prop;
    }
    public Scope scope() {
        return _scope;
    }

    public static ARIValue like(ARIProp prop, String ... values){
        return new ARIValue(prop, Scope.LIKE, values);
    }

    public static ARIValue in(ARIProp prop, Object ... values){
        return new ARIValue(prop, Scope.IN, values);
    }

    public static ARIValue eq(ARIProp prop, Object value){
        return new ARIValue(prop, Scope.EQUALS, value);
    }

    public static ARIValue of(ARIProp prop, Object value){
        return new ARIValue(prop, Scope.CREATE, value);
    }

    public enum Scope {
        CREATE, LIKE, EQUALS, IN
    }

}
