// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Locator.java

package org.xml.sax;


public interface Locator
{

    public abstract String getPublicId();

    public abstract String getSystemId();

    public abstract int getLineNumber();

    public abstract int getColumnNumber();
}
