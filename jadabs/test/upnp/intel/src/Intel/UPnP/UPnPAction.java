// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPAction.java

package Intel.UPnP;

import java.util.*;

// Referenced classes of package Intel.UPnP:
//            UPnPInvokeException, UPnPArgument, UPnPStateVariable

public class UPnPAction
{

    public UPnPAction()
    {
        ArgList = new ArrayList();
    }

    public String toString()
    {
        return Name;
    }

    public boolean ValidateArgs(UPnPArgument Args[])
        throws UPnPInvokeException
    {
        int NumShouldHave = ArgList.size();
        if(HasReturnValue())
            NumShouldHave--;
        if(Args.length != NumShouldHave)
            throw new UPnPInvokeException("Incorrect number of Args");
        for(int i = 0; i < Args.length; i++)
        {
            UPnPArgument a = GetArg(Args[i].Name);
            if(a != null)
            {
                if(a.Direction.compareTo("in") != 0)
                    continue;
                try
                {
                    if(UPnPStateVariable.ConvertFromUPnPType(a.RelatedStateVar.VarType).getName().compareTo(Args[i].DataValue.getClass().getName()) != 0)
                        throw new UPnPInvokeException(String.valueOf(String.valueOf((new StringBuffer("Expecting ")).append(UPnPStateVariable.ConvertFromUPnPType(a.RelatedStateVar.VarType)).append(" but instead got ").append(Args[i].DataValue.getClass().getName()))));
                }
                catch(ClassNotFoundException cnfe)
                {
                    throw new UPnPInvokeException("Class Error: ".concat(String.valueOf(String.valueOf(cnfe.toString()))));
                }
            } else
            {
                throw new UPnPInvokeException(String.valueOf(String.valueOf(Args[i].Name)).concat(" was not found"));
            }
        }

        return true;
    }

    public boolean HasReturnValue()
    {
        boolean RetVal = false;
        int cnt = ArgList.size();
        int x = 0;
        do
        {
            if(x >= cnt)
                break;
            if(((UPnPArgument)ArgList.get(x)).IsReturnValue)
            {
                RetVal = true;
                break;
            }
            x++;
        } while(true);
        return RetVal;
    }

    public UPnPArgument GetRetArg()
    {
        Iterator en = ArgList.iterator();
        UPnPArgument RetVal = null;
        do
        {
            if(!en.hasNext())
                break;
            UPnPArgument a = (UPnPArgument)en.next();
            if(!a.IsReturnValue)
                continue;
            RetVal = a;
            break;
        } while(true);
        return RetVal;
    }

    public UPnPArgument GetArg(String ArgName)
    {
        Iterator en = ArgList.iterator();
        UPnPArgument RetVal = null;
        do
        {
            if(!en.hasNext())
                break;
            UPnPArgument a = (UPnPArgument)en.next();
            if(a.Name.compareTo(ArgName) != 0)
                continue;
            RetVal = a;
            break;
        } while(true);
        return RetVal;
    }

    public void AddArgument(UPnPArgument Arg)
    {
        ArgList.add(Arg);
    }

    public UPnPArgument[] getArgumentList()
    {
        UPnPArgument RetVal[] = new UPnPArgument[ArgList.size()];
        for(int x = 0; x < RetVal.length; x++)
            RetVal[x] = (UPnPArgument)ArgList.get(x);

        return RetVal;
    }

    public String Name;
    protected ArrayList ArgList;
}
