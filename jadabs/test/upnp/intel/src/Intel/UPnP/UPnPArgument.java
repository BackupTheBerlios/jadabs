// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPArgument.java

package Intel.UPnP;


// Referenced classes of package Intel.UPnP:
//            UPnPStateVariable

public class UPnPArgument
{

    public UPnPArgument()
    {
    }

    public UPnPArgument(String name, Object val)
    {
        Name = name;
        DataValue = val;
        if(val != null)
            DataType = val.getClass().toString();
        else
            DataType = "System.Void";
        IsReturnValue = false;
    }

    public String Name;
    public String Direction;
    public String DataType;
    public Object DataValue;
    public boolean IsReturnValue;
    public UPnPStateVariable RelatedStateVar;
}
