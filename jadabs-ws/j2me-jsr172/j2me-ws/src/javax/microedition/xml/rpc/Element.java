// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Element.java

package javax.microedition.xml.rpc;

import javax.xml.namespace.QName;

// Referenced classes of package javax.microedition.xml.rpc:
//            Type

public class Element extends Type
{

    public Element(QName name, Type type, int minOccurs, int maxOccurs, boolean nillable)
        throws IllegalArgumentException
    {
        super(9);
        if(name == null || type == null || (type instanceof Element))
            throw new IllegalArgumentException();
        this.name = name;
        contentType = type;
        if(minOccurs < 0 || maxOccurs <= 0 && maxOccurs != -1)
            throw new IllegalArgumentException("[min|max]Occurs must >= 0");
        if(maxOccurs < minOccurs && maxOccurs != -1)
        {
            throw new IllegalArgumentException("maxOccurs must > minOccurs");
        } else
        {
            this.minOccurs = minOccurs;
            isOptional = minOccurs == 0;
            this.maxOccurs = maxOccurs;
            isArray = maxOccurs > 1 || maxOccurs == -1;
            isNillable = nillable;
            return;
        }
    }

    public Element(QName name, Type type)
        throws IllegalArgumentException
    {
        super(9);
        if(name == null || type == null || (type instanceof Element))
        {
            throw new IllegalArgumentException();
        } else
        {
            this.name = name;
            contentType = type;
            minOccurs = 1;
            maxOccurs = 1;
            isArray = false;
            isOptional = false;
            isNillable = false;
            return;
        }
    }

    public final QName name;
    public final Type contentType;
    public final boolean isNillable;
    public final boolean isArray;
    public final boolean isOptional;
    public final int minOccurs;
    public final int maxOccurs;
    public static final int UNBOUNDED = -1;

    static 
    {
        UNBOUNDED = -1;
    }
}
