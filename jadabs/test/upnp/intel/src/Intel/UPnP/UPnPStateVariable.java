// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPStateVariable.java

package Intel.UPnP;

import java.util.ArrayList;

// Referenced classes of package Intel.UPnP:
//            AssociationNode

public class UPnPStateVariable
{

    public String getName()
    {
        return VariableName;
    }

    public String[] getAllowedStringValues()
    {
        return Allowed;
    }

    public UPnPStateVariable(String VarName)
    {
        DefValue = null;
        CurrentValue = "";
        VariableName = VarName;
        Allowed = null;
        SendEvent = false;
        AssociationList = new ArrayList();
    }

    public UPnPStateVariable(String VarName, Object VarValue)
    {
        this(VarName, VarValue, ((String []) (null)));
    }

    public UPnPStateVariable(String VarName, Class ClassType, boolean SendEvents)
    {
        SendEvent = SendEvents;
        DefValue = null;
        CurrentValue = "";
        VariableName = VarName;
        Allowed = null;
        SendEvent = SendEvents;
        AssociationList = new ArrayList();
        VarType = ConvertToUPnPType(ClassType);
    }

    public UPnPStateVariable(String VarName, Object VarValue, String AllowedValues[])
    {
        DefValue = VarValue;
        CurrentValue = VarValue;
        VariableName = VarName;
        Allowed = AllowedValues;
        SendEvent = true;
        VarType = ConvertToUPnPType(VarValue.getClass());
        if(VarType.compareTo("boolean") == 0)
            VarValue = VarValue.toString().toLowerCase();
        AssociationList = new ArrayList();
    }

    public Class GetJavaClass()
        throws ClassNotFoundException
    {
        return ConvertFromUPnPType(VarType);
    }

    public static Class ConvertFromUPnPType(String TheType)
        throws ClassNotFoundException
    {
        if(TheType.compareTo("string") == 0)
            return Class.forName("java.lang.String");
        if(TheType.compareTo("boolean") == 0)
            return Class.forName("java.lang.Boolean");
        if(TheType.compareTo("uri") == 0)
            return Class.forName("java.net.URL");
        if(TheType.compareTo("float") == 0)
            return Class.forName("java.lang.Double");
        if(TheType.compareTo("int") == 0)
            return Class.forName("java.lang.Integer");
        if(TheType.compareTo("i1") == 0)
            return Class.forName("java.lang.Byte");
        if(TheType.compareTo("i2") == 0)
            return Class.forName("java.lang.Short");
        if(TheType.compareTo("i4") == 0)
            return Class.forName("java.lang.Integer");
        if(TheType.compareTo("number") == 0)
            return Class.forName("java.lang.Integer");
        if(TheType.compareTo("r4") == 0)
            return Class.forName("java.lang.Float");
        if(TheType.compareTo("r8") == 0)
            return Class.forName("java.lang.Double");
        else
            return Class.forName("java.lang.Object");
    }

    public String toString()
    {
        return getName();
    }

    public static String ConvertToUPnPType(Class JavaClass)
    {
        String name = JavaClass.getName();
        if(name.compareTo("java.lang.String") == 0)
            return "string";
        if(name.compareTo("java.lang.Boolean") == 0)
            return "boolean";
        if(name.compareTo("java.net.URL") == 0)
            return "uri";
        if(name.compareTo("java.lang.Character") == 0)
            return "ui1";
        if(name.compareTo("java.lang.Integer") == 0)
            return "int";
        if(name.compareTo("java.lang.Short") == 0)
            return "i2";
        if(name.compareTo("java.lang.Byte") == 0)
            return "i1";
        if(name.compareTo("java.lang.Float") == 0)
            return "r4";
        if(name.compareTo("java.lang.Double") == 0)
            return "r8";
        if(name.compareTo("[Ljava.lang.String;") == 0)
            return "string";
        if(name.compareTo("[Ljava.lang.Boolean;") == 0)
            return "boolean";
        if(name.compareTo("[Z") == 0)
            return "boolean";
        if(name.compareTo("[Ljava.net.URL;") == 0)
            return "uri";
        if(name.compareTo("[C") == 0)
            return "ui1";
        if(name.compareTo("[Ljava.net.Character;") == 0)
            return "ui1";
        if(name.compareTo("[I") == 0)
            return "int";
        if(name.compareTo("[Ljava.lang.Integer;") == 0)
            return "int";
        if(name.compareTo("[S") == 0)
            return "i2";
        if(name.compareTo("[Ljava.lang.Short;") == 0)
            return "i2";
        if(name.compareTo("[B") == 0)
            return "i1";
        if(name.compareTo("[Ljava.lang.Byte;") == 0)
            return "i1";
        if(name.compareTo("[F") == 0)
            return "r4";
        if(name.compareTo("[Ljava.lang.Float;") == 0)
            return "r4";
        if(name.compareTo("[D") == 0)
            return "r8";
        if(name.compareTo("[Ljava.lang.Double;") == 0)
            return "r8";
        else
            return JavaClass.getName();
    }

    public String BuildProperty()
    {
        String XMLPayLoad = "";
        if(!SendEvent)
        {
            return "";
        } else
        {
            XMLPayLoad = String.valueOf(String.valueOf(XMLPayLoad)).concat("<e:property>\n");
            XMLPayLoad = String.valueOf(XMLPayLoad) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("<")).append(getName()).append(">").append(getValue().toString()).append("</").append(getName()).append(">\n"))));
            XMLPayLoad = String.valueOf(String.valueOf(XMLPayLoad)).concat("</e:property>\n");
            return XMLPayLoad;
        }
    }

    public void AddAssociation(String ActionName, String ArgumentName)
    {
        AssociationNode temp = new AssociationNode(ActionName, ArgumentName);
        AssociationList.add(temp);
    }

    public AssociationNode[] GetAssociations()
    {
        Object x[] = AssociationList.toArray();
        AssociationNode y[] = new AssociationNode[x.length];
        for(int i = 0; i < x.length; i++)
            y[i] = (AssociationNode)x[i];

        return y;
    }

    public Object getValue()
    {
        return CurrentValue;
    }

    public void setValue(Object value)
    {
        CurrentValue = value;
    }

    public String getValueType()
    {
        return VarType;
    }

    public String GetStateVariableXML()
    {
        String XML = "";
        if(SendEvent)
            XML = "   <stateVariable sendEvents=\"yes\">\r\n";
        else
            XML = "   <stateVariable sendEvents=\"no\">\r\n";
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("      <name>")).append(VariableName).append("</name>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("      <dataType>")).append(getValueType()).append("</dataType>\r\n"))));
        if(Allowed != null)
        {
            XML = String.valueOf(String.valueOf(XML)).concat("      <allowedValueList>\r\n");
            for(int z = 0; z < Allowed.length; z++)
                XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("         <allowedValue>")).append(Allowed[z]).append("</allowedValue>\r\n"))));

            XML = String.valueOf(String.valueOf(XML)).concat("      </allowedValueList>\r\n");
        }
        if(DefValue != null)
            XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("      <defaultValue>")).append(DefValue.toString()).append("</defaultValue>\r\n"))));
        XML = String.valueOf(String.valueOf(XML)).concat("   </stateVariable>\r\n");
        return XML;
    }

    protected Object CurrentValue;
    protected Object DefValue;
    public String VarType;
    public boolean SendEvent;
    protected ArrayList AssociationList;
    protected String Allowed[];
    protected String VariableName;
}
