// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Stub.java

package javax.xml.rpc;


public interface Stub
{

    public abstract void _setProperty(String s, Object obj);

    public abstract Object _getProperty(String s);

    public static final String USERNAME_PROPERTY = "javax.xml.rpc.security.auth.username";
    public static final String PASSWORD_PROPERTY = "javax.xml.rpc.security.auth.password";
    public static final String ENDPOINT_ADDRESS_PROPERTY = "javax.xml.rpc.service.endpoint.address";
    public static final String SESSION_MAINTAIN_PROPERTY = "javax.xml.rpc.session.maintain";

}
