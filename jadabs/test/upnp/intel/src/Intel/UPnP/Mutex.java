// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Mutex.java

package Intel.UPnP;


public class Mutex
{

    public Mutex()
    {
        TheLock = new Object();
    }

    protected Object TheLock;
}
