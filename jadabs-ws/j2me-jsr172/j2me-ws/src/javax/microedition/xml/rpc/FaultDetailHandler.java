// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FaultDetailHandler.java

package javax.microedition.xml.rpc;

import javax.xml.namespace.QName;

// Referenced classes of package javax.microedition.xml.rpc:
//            Element

public interface FaultDetailHandler
{

    public abstract Element handleFault(QName qname);
}
