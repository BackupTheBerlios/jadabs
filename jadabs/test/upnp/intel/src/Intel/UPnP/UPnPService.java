// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPService.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            UPnPAction, UPnPStateVariable, HTTPMessage, UPnPArgument, 
//            XMLTextReader, UPnPInvokeException, HTTPSession, UPnPDevice, 
//            AssociationNode, StringFormatter

public class UPnPService
{
    private class SubscriberInfo
    {

        public String SID;
        public String CallbackURL;
        public long Expires;
        public long SEQ;

        private SubscriberInfo()
        {
        }

        SubscriberInfo(._cls1 x$1)
        {
            this();
        }
    }

    private class InvokeListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            HTTPSession.SessionEvent se = (HTTPSession.SessionEvent)e;
            if(e.getID() == HTTPSession.SESSION_CONNECT)
                ((HTTPSession)e.getSource()).Send(Packet);
            if(e.getID() != HTTPSession.SESSION_CONNECT_FAILED);
            if(e.getID() == HTTPSession.SESSION_DATA_AVAILABLE)
                HandleInvokeRequest((HTTPMessage)se.StateObject, (HTTPSession)e.getSource(), MethodName, Args);
        }

        protected String MethodName;
        protected UPnPArgument Args[];
        protected HTTPMessage Packet;

        public InvokeListener(String _MethodName, UPnPArgument _Args[], HTTPMessage _packet)
        {
            MethodName = _MethodName;
            Args = _Args;
            Packet = _packet;
        }
    }

    public class UPnPServiceEvent extends ActionEvent
    {

        public UPnPInvokeException FailedException;
        public String MethodName;
        public Object RetValue;
        public UPnPArgument Args[];

        public UPnPServiceEvent(Object src, String _MethodName, UPnPInvokeException e)
        {
            super(src, UPnPService.INVOKE_FAILED, "INVOKE_FAILED");
            FailedException = null;
            MethodName = null;
            RetValue = null;
            Args = null;
            MethodName = _MethodName;
            FailedException = e;
        }

        public UPnPServiceEvent(Object src, String _MethodName, Object RetVal, UPnPArgument RetArgs[])
        {
            super(src, UPnPService.INVOKE_SUCCESS, "INVOKE_SUCCESS");
            FailedException = null;
            MethodName = null;
            RetValue = null;
            Args = null;
            MethodName = _MethodName;
            RetValue = RetVal;
            Args = RetArgs;
        }
    }


    public String toString()
    {
        return GetServiceURN();
    }

    public UPnPAction[] getActions()
    {
        UPnPAction RetVal[] = new UPnPAction[RemoteMethodMap.size()];
        int c = 0;
        for(Iterator i = RemoteMethodMap.values().iterator(); i.hasNext();)
        {
            RetVal[c] = (UPnPAction)i.next();
            c++;
        }

        return RetVal;
    }

    public String GetServiceURN()
    {
        return _ServiceURN;
    }

    public void SetServiceURN(String value)
    {
        if(!value.toUpperCase().startsWith("URN:SCHEMAS-UPNP-ORG:SERVICE:"))
            _ServiceURN = String.valueOf(String.valueOf((new StringBuffer("urn:schemas-upnp-org:service:")).append(value).append(":").append(Version())));
        else
            _ServiceURN = value;
    }

    public void addActionListener(ActionListener a)
    {
        EventList.add(a);
    }

    public void TriggerEvent(ActionEvent e)
    {
        Object x[] = EventList.toArray();
        for(int i = 0; i < x.length; i++)
            ((ActionListener)x[i]).actionPerformed(e);

    }

    public UPnPService(double Version)
    {
        ParentDevice = null;
        EventList = new ArrayList();
        StateVariables = new HashMap();
        MethodMap = new HashMap();
        MethodArgMap = new HashMap();
        VarAssociation = new Hashtable();
        RemoteMethodMap = new HashMap();
        SubscriberTable = Collections.synchronizedMap(new HashMap());
        EventSID = 0;
        String VersionString = String.valueOf(Version);
        if(Version == (double)0)
        {
            Major = new Integer(1);
            Minor = new Integer(0);
        } else
        if(VersionString.indexOf(".") == 0)
        {
            Major = new Integer(Integer.parseInt(VersionString));
            Minor = new Integer(0);
        } else
        {
            Major = new Integer(Integer.parseInt(VersionString.substring(0, VersionString.indexOf("."))));
            Minor = new Integer(Integer.parseInt(VersionString.substring(VersionString.indexOf(".") + 1)));
        }
    }

    public UPnPService(double Version, String SvcID, String ServiceType, Object InstanceObject)
    {
        this(Version);
        SetServiceURN(ServiceType);
        ServiceID = SvcID;
        SCPDURL = String.valueOf(String.valueOf((new StringBuffer("{")).append(ServiceID).append("}scpd.xml")));
        ControlURL = String.valueOf(String.valueOf((new StringBuffer("{")).append(ServiceID).append("}control")));
        EventURL = String.valueOf(String.valueOf((new StringBuffer("{")).append(ServiceID).append("}event")));
        ServiceInstance = InstanceObject;
    }

    public boolean IsMatchURL(String FullURL, String PartialURL)
    {
        int x = FullURL.lastIndexOf("/");
        String TheURL = "";
        if(x == -1)
            TheURL = FullURL;
        else
            TheURL = FullURL.substring(x + 1);
        return TheURL.compareTo(PartialURL) == 0;
    }

    public void SetStandardType(String value)
    {
        _ServiceURN = String.valueOf(String.valueOf((new StringBuffer("urn:schemas-upnp-org:service:")).append(value).append(":").append(Version())));
    }

    public UPnPStateVariable[] getStateVariables()
    {
        UPnPStateVariable RetVal[] = new UPnPStateVariable[StateVariables.size()];
        Iterator i = StateVariables.values().iterator();
        for(int x = 0; i.hasNext(); x++)
            RetVal[x] = (UPnPStateVariable)i.next();

        return RetVal;
    }

    public String Version()
    {
        String v = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(String.valueOf(Major))))).append(".").append(String.valueOf(Minor))));
        Double dv = new Double(v);
        return dv.toString();
    }

    public String GetServiceXML()
    {
        return String.valueOf(String.valueOf((new StringBuffer("<service><serviceType>")).append(GetServiceURN()).append("</serviceType><serviceId>").append(ServiceID).append("</serviceId><SCPDURL>").append(SCPDURL).append("</SCPDURL><controlURL>").append(ControlURL).append("</controlURL><eventSubURL>").append(EventURL).append("</eventSubURL></service>")));
    }

    protected synchronized String GetNewSID()
    {
        EventSID++;
        return String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(ParentDevice.UniqueDeviceName).append("-").append(ServiceID).append("-").append(String.valueOf(EventSID))));
    }

    public void _CancelEvent(String SID)
    {
        SubscriberTable.remove(SID);
    }

    public boolean _RenewEvent(String SID, String Timeout)
    {
        SubscriberInfo sinfo = null;
        Object tmp = SubscriberTable.get(SID);
        if(tmp == null)
            return false;
        sinfo = (SubscriberInfo)tmp;
        int to = Integer.parseInt(Timeout);
        if(to == 0)
            sinfo.Expires = -1L;
        else
            sinfo.Expires = (new Date()).getTime() + (long)(to * 1000);
        SubscriberTable.put(SID, sinfo);
        return true;
    }

    public HTTPMessage _SubscribeEvent(String SID[], String CallbackURL, String Timeout)
    {
        SubscriberInfo sinfo = new SubscriberInfo(null);
        sinfo.SID = GetNewSID();
        sinfo.CallbackURL = CallbackURL;
        sinfo.SEQ = 1L;
        int to = Integer.parseInt(Timeout);
        if(to == 0)
            sinfo.Expires = -1L;
        else
            sinfo.Expires = (new Date()).getTime() + (long)(to * 1000);
        SubscriberTable.put(sinfo.SID, sinfo);
        HTTPMessage Packet = new HTTPMessage();
        Packet.Directive = "NOTIFY";
        Packet.AddTag("Content-Type", "text/xml");
        Packet.AddTag("NT", "upnp:event");
        Packet.AddTag("NTS", "upnp:propchange");
        Packet.AddTag("SID", sinfo.SID);
        Packet.AddTag("SEQ", "0");
        Packet.SetStringBuffer(BuildEventXML());
        SID[0] = sinfo.SID;
        return Packet;
    }

    protected String BuildEventXML()
    {
        String XMLPayLoad = "";
        Object SVVals[] = StateVariables.values().toArray();
        XMLPayLoad = "<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\">\n";
        for(int i = 0; i < SVVals.length; i++)
        {
            UPnPStateVariable SVar = (UPnPStateVariable)SVVals[i];
            XMLPayLoad = String.valueOf(XMLPayLoad) + String.valueOf(SVar.BuildProperty());
        }

        XMLPayLoad = String.valueOf(String.valueOf(XMLPayLoad)).concat("</e:propertyset>\n");
        return XMLPayLoad;
    }

    public void AddStateVariable(UPnPStateVariable NewVar)
    {
        StateVariables.put(NewVar.getName(), NewVar);
        AssociationNode Nodes[] = NewVar.GetAssociations();
        for(int x = 0; x < Nodes.length; x++)
        {
            Hashtable ArgTable = (Hashtable)VarAssociation.get(Nodes[x].ActionName);
            if(ArgTable == null)
                ArgTable = new Hashtable();
            ArgTable.put(Nodes[x].ArgName, NewVar);
            VarAssociation.put(Nodes[x].ActionName, ArgTable);
        }

    }

    protected void AddAction(UPnPAction action)
    {
        RemoteMethodMap.put(action.Name, action);
    }

    public void AddMethod(String PublicMethodName, String ArgNames[])
        throws Exception
    {
        Method methods[] = ServiceInstance.getClass().getMethods();
        Method TheMethod = null;
        int mid = 0;
        do
        {
            if(mid >= methods.length)
                break;
            if(methods[mid].getName().compareTo(PublicMethodName) == 0)
            {
                MethodMap.put(PublicMethodName, methods[mid]);
                MethodArgMap.put(PublicMethodName, ArgNames);
                TheMethod = methods[mid];
                break;
            }
            mid++;
        } while(true);
        if(TheMethod == null)
            throw new Exception(String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(PublicMethodName)))).append(" does not exist in ").append(ServiceInstance.getClass().getName()))));
        if(TheMethod.getReturnType().getName().compareTo("void") != 0)
        {
            UPnPStateVariable sv = new UPnPStateVariable(String.valueOf(String.valueOf((new StringBuffer("A_ARG_TYPE_")).append(PublicMethodName).append("_RetType"))), TheMethod.getReturnType(), false);
            sv.AddAssociation(PublicMethodName, "_ReturnValue");
            AddStateVariable(sv);
        }
        Class pinfo[] = TheMethod.getParameterTypes();
        for(int x = 0; x < pinfo.length; x++)
        {
            UPnPStateVariable sv = new UPnPStateVariable(String.valueOf(String.valueOf((new StringBuffer("A_ARG_TYPE_")).append(PublicMethodName).append("_").append(((String[])MethodArgMap.get(PublicMethodName))[x]))), pinfo[x], false);
            sv.AddAssociation(PublicMethodName, ((String[])MethodArgMap.get(PublicMethodName))[x]);
            AddStateVariable(sv);
        }

    }

    protected UPnPStateVariable GetAssociatedVar(String ActionName, String Arg)
    {
        Hashtable ArgTable = (Hashtable)VarAssociation.get(ActionName);
        if(ArgTable == null)
            return null;
        else
            return (UPnPStateVariable)ArgTable.get(Arg);
    }

    public String SCPDFile()
    {
        int i = SCPDURL.lastIndexOf("/");
        if(i == -1)
            return "/".concat(String.valueOf(String.valueOf(SCPDURL)));
        else
            return SCPDURL.substring(i);
    }

    public static String SerializeObjectInstance(Object data)
    {
        String ObjectType = data.getClass().getName();
        String RetVal = "";
        if(ObjectType.compareTo("java.net.URL") == 0)
            RetVal = ((URL)data).toExternalForm();
        else
            RetVal = data.toString();
        return RetVal;
    }

    public String GetSCPDXml()
    {
        ArrayList OutList = new ArrayList();
        ArrayList InList = new ArrayList();
        String XML = String.valueOf(String.valueOf((new StringBuffer("<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\"><specVersion><major>")).append(Major.toString()).append("</major><minor>").append(Minor.toString()).append("</minor></specVersion>")));
        if(MethodMap.size() > 0)
        {
            Object LMLValue[] = MethodMap.values().toArray();
            XML = String.valueOf(String.valueOf(XML)).concat("  <actionList>\r\n");
            for(int i = 0; i < LMLValue.length; i++)
            {
                Method CurrentMethod = (Method)LMLValue[i];
                Class pInfo[] = CurrentMethod.getParameterTypes();
                XML = String.valueOf(String.valueOf(XML)).concat("    <action>\r\n");
                XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("      <name>")).append(CurrentMethod.getName()).append("</name>\r\n"))));
                if(pInfo.length != 0 || CurrentMethod.getReturnType().getName().compareTo("void") != 0)
                {
                    XML = String.valueOf(String.valueOf(XML)).concat("      <argumentList>\r\n");
                    OutList.clear();
                    InList.clear();
                    if(CurrentMethod.getReturnType().getName().compareTo("void") != 0)
                    {
                        UPnPArgument ThisArg = new UPnPArgument();
                        ThisArg.Direction = "out";
                        ThisArg.IsReturnValue = true;
                        ThisArg.Name = "_ReturnValue";
                        ThisArg.DataType = CurrentMethod.getReturnType().getName();
                        OutList.add(ThisArg);
                    }
                    for(int pid = 0; pid < pInfo.length; pid++)
                    {
                        UPnPArgument ThisArg = new UPnPArgument();
                        ThisArg.Name = ((String[])MethodArgMap.get(CurrentMethod.getName()))[pid];
                        ThisArg.DataType = pInfo[pid].getName();
                        if(pInfo[pid].isArray())
                        {
                            ThisArg.Direction = "out";
                            OutList.add(ThisArg);
                        } else
                        {
                            ThisArg.Direction = "in";
                            InList.add(ThisArg);
                        }
                    }

                    for(int x = 0; x < InList.size(); x++)
                    {
                        UPnPArgument ThisArg = (UPnPArgument)InList.get(x);
                        XML = String.valueOf(String.valueOf(XML)).concat("        <argument>\r\n");
                        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("          <name>")).append(ThisArg.Name).append("</name>\r\n"))));
                        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("          <direction>")).append(ThisArg.Direction).append("</direction>\r\n"))));
                        UPnPStateVariable RelatedVar = GetAssociatedVar(CurrentMethod.getName(), ThisArg.Name);
                        if(RelatedVar != null)
                            XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("              <relatedStateVariable>")).append(RelatedVar.getName()).append("</relatedStateVariable>\r\n"))));
                        XML = String.valueOf(String.valueOf(XML)).concat("        </argument>\r\n");
                    }

                    for(int x = 0; x < OutList.size(); x++)
                    {
                        UPnPArgument ThisArg = (UPnPArgument)OutList.get(x);
                        XML = String.valueOf(String.valueOf(XML)).concat("        <argument>\r\n");
                        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("          <name>")).append(ThisArg.Name).append("</name>\r\n"))));
                        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("          <direction>")).append(ThisArg.Direction).append("</direction>\r\n"))));
                        if(ThisArg.IsReturnValue)
                            XML = String.valueOf(String.valueOf(XML)).concat("          <retval></retval>\r\n");
                        UPnPStateVariable RelatedVar = GetAssociatedVar(CurrentMethod.getName(), ThisArg.Name);
                        if(RelatedVar != null)
                            XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("              <relatedStateVariable>")).append(RelatedVar.getName()).append("</relatedStateVariable>\r\n"))));
                        XML = String.valueOf(String.valueOf(XML)).concat("        </argument>\r\n");
                    }

                    XML = String.valueOf(String.valueOf(XML)).concat("      </argumentList>\r\n");
                }
                XML = String.valueOf(String.valueOf(XML)).concat("    </action>\r\n");
            }

            XML = String.valueOf(String.valueOf(XML)).concat("  </actionList>\r\n");
        }
        XML = String.valueOf(String.valueOf(XML)).concat("  <serviceStateTable>\r\n");
        Object SVVals[] = StateVariables.values().toArray();
        for(int i = 0; i < SVVals.length; i++)
            XML = String.valueOf(XML) + String.valueOf(((UPnPStateVariable)SVVals[i]).GetStateVariableXML());

        XML = String.valueOf(String.valueOf(XML)).concat("  </serviceStateTable>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("</scpd>\r\n");
        return XML;
    }

    public Object InvokeLocal(String MethodName, ArrayList VarList)
        throws Exception
    {
        if(MethodName.compareTo("QueryStateVariable") == 0)
        {
            UPnPArgument SArg = (UPnPArgument)VarList.get(0);
            UPnPStateVariable SV = (UPnPStateVariable)StateVariables.get(SArg.DataValue);
            if(SV != null)
                return SV.getValue();
        }
        Method x = (Method)MethodMap.get(MethodName);
        if(x == null)
            throw new Exception(String.valueOf(String.valueOf(MethodName)).concat(" not found"));
        Class pInfo[] = x.getParameterTypes();
        Object InVarArr[] = new Object[pInfo.length];
        for(int init = 0; init < InVarArr.length; init++)
            if(pInfo[init].getName().compareTo("java.lang.String") != 0)
            {
                String TypeString = pInfo[init].getName();
                Class TheType = Class.forName(TypeString);
                InVarArr[init] = CreateObjectInstance(TheType, null);
            } else
            {
                InVarArr[init] = "";
            }

label0:
        for(int id = 0; id < VarList.size(); id++)
        {
            int id2 = 0;
            do
            {
                if(id2 >= pInfo.length)
                    continue label0;
                if(((String[])MethodArgMap.get(MethodName))[id2].compareTo(((UPnPArgument)VarList.get(id)).Name) == 0)
                {
                    if(pInfo[id2].getName().compareTo("java.lang.String") != 0)
                        InVarArr[id2] = CreateObjectInstance(pInfo[id2], (String)((UPnPArgument)VarList.get(id)).DataValue);
                    else
                        InVarArr[id2] = ((UPnPArgument)VarList.get(id)).DataValue;
                    continue label0;
                }
                id2++;
            } while(true);
        }

        Object RetVal = x.invoke(ServiceInstance, InVarArr);
        VarList.clear();
        for(int id = 0; id < InVarArr.length; id++)
        {
            if(!pInfo[id].isArray())
                continue;
            String name = pInfo[id].getName();
            Object out = null;
            if(name.compareTo("[Ljava.lang.String;") == 0)
                out = ((String[])InVarArr[id])[0];
            if(name.compareTo("[Ljava.lang.Boolean;") == 0)
                out = ((Boolean[])InVarArr[id])[0];
            if(name.compareTo("[Z") == 0)
                out = new Boolean(((boolean[])InVarArr[id])[0]);
            if(name.compareTo("[Ljava.net.URL;") == 0)
                out = ((URL[])InVarArr[id])[0];
            if(name.compareTo("[Ljava.lang.Character;") == 0)
                out = ((Character[])InVarArr[id])[0];
            if(name.compareTo("[C") == 0)
                out = new Character(((char[])InVarArr[id])[0]);
            if(name.compareTo("[java.lang.Integer;") == 0)
                out = ((Integer[])InVarArr[id])[0];
            if(name.compareTo("[I") == 0)
                out = new Integer(((int[])InVarArr[id])[0]);
            if(name.compareTo("[Ljava.lang.Short;") == 0)
                out = ((Short[])InVarArr[id])[0];
            if(name.compareTo("[S") == 0)
                out = new Short(((short[])InVarArr[id])[0]);
            if(name.compareTo("[Ljava.lang.Byte;") == 0)
                out = ((Byte[])InVarArr[id])[0];
            if(name.compareTo("[B") == 0)
                out = new Byte(((byte[])InVarArr[id])[0]);
            if(name.compareTo("[Ljava.lang.Float;") == 0)
                out = ((Float[])InVarArr[id])[0];
            if(name.compareTo("[F") == 0)
                out = new Float(((float[])InVarArr[id])[0]);
            if(name.compareTo("[Ljava.lang.Double;") == 0)
                out = ((Double[])InVarArr[id])[0];
            if(name.compareTo("[D") == 0)
                out = new Double(((double[])InVarArr[id])[0]);
            VarList.add(new UPnPArgument(((String[])MethodArgMap.get(MethodName))[id], out));
        }

        return RetVal;
    }

    public static Object CreateObjectInstance(Class ObjectType, String data)
        throws Exception
    {
        Object RetObj = null;
        String name = ObjectType.getName();
        Class TypeParm[] = new Class[1];
        TypeParm[0] = Class.forName("java.lang.String");
        Object Arg[] = new Object[1];
        Arg[0] = "";
        if(name.compareTo("[Ljava.lang.String;") == 0)
            return new String[1];
        if(name.compareTo("[Ljava.lang.Boolean;") == 0)
            return new Boolean[1];
        if(name.compareTo("[Z") == 0)
            return new Boolean[1];
        if(name.compareTo("[Ljava.net.URL;") == 0)
            return new URL[1];
        if(name.compareTo("[Ljava.lang.Character;") == 0)
            return new Character[1];
        if(name.compareTo("[C") == 0)
            return new char[1];
        if(name.compareTo("[Ljava.lang.Integer;") == 0)
            return new Integer[1];
        if(name.compareTo("[I") == 0)
            return new int[1];
        if(name.compareTo("[Ljava.lang.Short;") == 0)
            return new Short[1];
        if(name.compareTo("[S") == 0)
            return new short[1];
        if(name.compareTo("[Ljava.lang.Byte;") == 0)
            return new Byte[1];
        if(name.compareTo("[B") == 0)
            return new byte[1];
        if(name.compareTo("[Ljava.lang.Float;") == 0)
            return new Float[1];
        if(name.compareTo("[F") == 0)
            return new float[1];
        if(name.compareTo("[Ljava.lang.Double;") == 0)
            return new Double[1];
        if(name.compareTo("[D") == 0)
            return new double[1];
        boolean def = true;
        if(ObjectType.getName().compareTo("java.lang.Object") == 0)
        {
            RetObj = "";
            def = false;
            if(data != null)
                RetObj = data;
        }
        if(ObjectType.getName().compareTo("java.lang.String") == 0)
        {
            if(data == null)
                RetObj = "";
            else
                RetObj = data;
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Boolean") == 0)
        {
            if(data == null)
                RetObj = Boolean.FALSE;
            else
            if(data.equals("1"))
                RetObj = new Boolean(true);
            else
                RetObj = Boolean.valueOf(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Character") == 0)
        {
            if(data == null)
                RetObj = new Character('\0');
            else
                RetObj = new Character(data.toCharArray()[0]);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Integer") == 0)
        {
            if(data == null)
                RetObj = Integer.decode("0");
            else
                RetObj = Integer.decode(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Short") == 0)
        {
            if(data == null)
                RetObj = Short.decode("0");
            else
                RetObj = Short.decode(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Long") == 0)
        {
            if(data == null)
                RetObj = Long.decode("0");
            else
                RetObj = Long.decode(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Double") == 0)
        {
            if(data == null)
                RetObj = Double.valueOf("0");
            else
                RetObj = Double.valueOf(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Float") == 0)
        {
            if(data == null)
                RetObj = Float.valueOf("0");
            else
                RetObj = Float.valueOf(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.lang.Byte") == 0)
        {
            if(data == null)
                RetObj = Byte.decode("0");
            else
                RetObj = Byte.decode(data);
            def = false;
        }
        if(ObjectType.getName().compareTo("java.net.URL") == 0)
        {
            if(data == null)
                RetObj = new URL("http://127.0.0.1/");
            else
                RetObj = new URL(data);
            def = false;
        }
        if(def)
        {
            Method mi = ObjectType.getMethod("Parse", TypeParm);
            if(mi != null)
            {
                RetObj = mi.invoke(null, Arg);
            } else
            {
                Constructor ci = ObjectType.getConstructor(TypeParm);
                if(ci != null)
                {
                    Arg[0] = "";
                    try
                    {
                        if(data == null)
                        {
                            RetObj = ci.newInstance(Arg);
                        } else
                        {
                            Arg[0] = data;
                            RetObj = ci.newInstance(Arg);
                        }
                    }
                    catch(Exception e)
                    {
                        throw new Exception("Could not instantiate ".concat(String.valueOf(String.valueOf(ObjectType.getName()))));
                    }
                }
            }
        }
        return RetObj;
    }

    public static UPnPService Parse(String XML)
    {
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        UPnPService RetVal = new UPnPService(1.0D);
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("service") == 0)
        {
            XMLDoc.Read();
            for(; XMLDoc.getLocalName().compareTo("service") != 0; XMLDoc.Read())
            {
                boolean def = true;
                if(XMLDoc.getLocalName().compareTo("serviceType") == 0)
                {
                    def = false;
                    RetVal._ServiceURN = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("serviceId") == 0)
                {
                    def = false;
                    RetVal.ServiceID = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("SCPDURL") == 0)
                {
                    def = false;
                    RetVal.SCPDURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("controlURL") == 0)
                {
                    def = false;
                    RetVal.ControlURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("eventSubURL") == 0)
                {
                    def = false;
                    RetVal.EventURL = XMLDoc.ReadString();
                }
                if(def)
                    XMLDoc.Skip();
            }

        }
        XMLDoc.dispose();
        return RetVal;
    }

    public void ParseSCPD(String XML)
    {
        if(XML.compareTo("") == 0)
            return;
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("scpd") == 0)
        {
            XMLDoc.Read();
label0:
            for(; XMLDoc.getLocalName().compareTo("scpd") != 0; XMLDoc.Read())
            {
                if(XMLDoc.getLocalName().compareTo("actionList") == 0)
                {
                    XMLDoc.Read();
                    do
                    {
                        if(XMLDoc.getLocalName().compareTo("actionList") == 0)
                            continue label0;
                        if(XMLDoc.getLocalName().compareTo("action") == 0)
                            ParseActionXml(String.valueOf(String.valueOf((new StringBuffer("<action>\r\n")).append(XMLDoc.ReadInnerXML()).append("</action>"))));
                        XMLDoc.Read();
                    } while(true);
                }
                if(XMLDoc.getLocalName().compareTo("serviceStateTable") == 0)
                {
                    XMLDoc.Read();
                    do
                    {
                        if(XMLDoc.getLocalName().compareTo("serviceStateTable") == 0)
                            continue label0;
                        if(XMLDoc.getLocalName().compareTo("stateVariable") == 0)
                            ParseStateVarXml(String.valueOf(String.valueOf((new StringBuffer("<stateVariable>\r\n")).append(XMLDoc.ReadInnerXML()).append("</stateVariable>\r\n"))));
                        XMLDoc.Read();
                    } while(true);
                }
                XMLDoc.Skip();
            }

        }
    }

    protected void ParseStateVarXml(String XML)
    {
        if(XML.compareTo("") == 0)
            return;
        String name = "";
        String DataType = "";
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("stateVariable") == 0)
        {
            XMLDoc.Read();
            for(; XMLDoc.getLocalName().compareTo("stateVariable") != 0; XMLDoc.Read())
            {
                if(XMLDoc.getLocalName().compareTo("name") == 0)
                    name = XMLDoc.ReadString();
                if(XMLDoc.getLocalName().compareTo("dataType") == 0)
                    DataType = XMLDoc.ReadString();
            }

            try
            {
                AddStateVariable(new UPnPStateVariable(name, UPnPStateVariable.ConvertFromUPnPType(DataType), false));
            }
            catch(Exception exception) { }
            for(Iterator en = RemoteMethodMap.values().iterator(); en.hasNext();)
            {
                UPnPAction a = (UPnPAction)en.next();
                UPnPArgument alist[] = a.getArgumentList();
                int x = 0;
                while(x < alist.length) 
                {
                    if(alist[x].RelatedStateVar.getName().compareTo(name) == 0)
                        alist[x].RelatedStateVar.VarType = DataType;
                    x++;
                }
            }

        }
        XMLDoc.dispose();
    }

    protected void ParseActionXml(String XML)
    {
        UPnPAction action = new UPnPAction();
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        XMLDoc.Read();
        XMLDoc.Read();
label0:
        for(; XMLDoc.getLocalName().compareTo("action") != 0; XMLDoc.Read())
        {
            if(XMLDoc.getLocalName().compareTo("name") == 0)
                action.Name = XMLDoc.ReadString();
            if(XMLDoc.getLocalName().compareTo("argumentList") != 0)
                continue;
            XMLDoc.Read();
            do
            {
                do
                    if(XMLDoc.getLocalName().compareTo("argumentList") == 0)
                        continue label0;
                while(XMLDoc.getLocalName().compareTo("argument") != 0);
                UPnPArgument arg = new UPnPArgument();
                XMLDoc.Read();
                for(; XMLDoc.getLocalName().compareTo("argument") != 0; XMLDoc.Read())
                {
                    if(XMLDoc.getLocalName().compareTo("name") == 0)
                        arg.Name = XMLDoc.ReadString();
                    if(XMLDoc.getLocalName().compareTo("retval") == 0)
                        arg.IsReturnValue = true;
                    if(XMLDoc.getLocalName().compareTo("direction") == 0)
                        arg.Direction = XMLDoc.ReadString();
                    if(XMLDoc.getLocalName().compareTo("relatedStateVariable") == 0)
                        arg.RelatedStateVar = new UPnPStateVariable(XMLDoc.ReadString());
                }

                action.AddArgument(arg);
                XMLDoc.Read();
            } while(true);
        }

        AddAction(action);
        XMLDoc.dispose();
    }

    public void InvokeAsync(String MethodName, UPnPArgument InVarArr[])
        throws UPnPInvokeException
    {
        HTTPMessage request = new HTTPMessage();
        if(InVarArr == null)
            InVarArr = new UPnPArgument[0];
        UPnPAction action = (UPnPAction)RemoteMethodMap.get(MethodName);
        if(action == null)
            throw new UPnPInvokeException(String.valueOf(String.valueOf(MethodName)).concat(" is not currently defined in this object"));
        action.ValidateArgs(InVarArr);
        URL curi = null;
        InetAddress destIP;
        try
        {
            curi = new URL(ControlURL);
            destIP = InetAddress.getByName(curi.getHost());
        }
        catch(Exception ex)
        {
            throw new UPnPInvokeException("Invalid Control URL: ".concat(String.valueOf(String.valueOf(ControlURL))));
        }
        int destPort = curi.getPort();
        String sName = curi.getPath();
        request.Directive = "POST";
        request.DirectiveObj = sName;
        request.AddTag("Host", String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(curi.getHost())))).append(":").append(destPort))));
        request.AddTag("Content-Type", "text/xml");
        request.AddTag("SoapAction", String.valueOf(String.valueOf((new StringBuffer("\"")).append(GetServiceURN()).append("#").append(MethodName).append("\""))));
        String Body = "<s:Envelope\r\n";
        Body = String.valueOf(String.valueOf(Body)).concat("    xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n");
        Body = String.valueOf(String.valueOf(Body)).concat("      s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n");
        Body = String.valueOf(String.valueOf(Body)).concat("  <s:Body>\r\n");
        Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <u:")).append(MethodName).append(" xmlns:u=\"").append(GetServiceURN()).append("\">\r\n"))));
        for(int ID = 0; ID < InVarArr.length; ID++)
            if(action.GetArg(InVarArr[ID].Name).Direction.compareTo("in") == 0)
                Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("      <")).append(InVarArr[ID].Name).append(">").append(StringFormatter.EscapeString(SerializeObjectInstance(InVarArr[ID].DataValue))).append("</").append(InVarArr[ID].Name).append(">\r\n"))));

        Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    </u:")).append(MethodName).append(">\r\n"))));
        Body = String.valueOf(String.valueOf(Body)).concat("  </s:Body>\r\n");
        Body = String.valueOf(String.valueOf(Body)).concat("</s:Envelope>\r\n");
        request.SetStringBuffer(Body);
        HTTPSession NewSession = new HTTPSession(null, 0, destIP, destPort, new InvokeListener(MethodName, InVarArr, request), ParentDevice.POOL, null);
    }

    protected void HandleInvokeRequest(HTTPMessage response, HTTPSession WebSession, String MethodName, UPnPArgument Args[])
    {
        WebSession.Close();
        UPnPAction action = (UPnPAction)RemoteMethodMap.get(MethodName);
        if(response.StatusCode != 200)
        {
            TriggerEvent(new UPnPServiceEvent(this, MethodName, new UPnPInvokeException(response.StatusData)));
            return;
        }
        XMLTextReader XMLDoc = new XMLTextReader(response.GetStringBuffer());
        String sb = StringFormatter.UnEscapeString(response.GetStringBuffer());
        String MethodTag = "";
        ArrayList VarList = new ArrayList();
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("Envelope") == 0)
        {
            XMLDoc.Read();
            if(XMLDoc.getLocalName().compareTo("Body") == 0)
            {
                XMLDoc.Read();
                MethodTag = XMLDoc.getLocalName();
                XMLDoc.Read();
                if(XMLDoc.getLocalName().compareTo("Body") != 0)
                    for(; XMLDoc.getLocalName().compareTo(MethodTag) != 0; XMLDoc.Read())
                    {
                        UPnPArgument VarArg = new UPnPArgument(XMLDoc.getLocalName(), StringFormatter.UnEscapeString(XMLDoc.ReadInnerXML()));
                        VarList.add(VarArg);
                    }

            }
        }
        XMLDoc.dispose();
        Object RetVal = null;
        String tRetVal = "";
        UPnPArgument InVarArr[] = Args;
        Object temp[] = new Object[1];
        int StartIDX = 0;
        if(((UPnPAction)RemoteMethodMap.get(MethodName)).HasReturnValue())
        {
            tRetVal = (String)((UPnPArgument)VarList.get(0)).DataValue;
            UPnPArgument ThisArg = action.GetArg(((UPnPArgument)VarList.get(0)).Name);
            try
            {
                RetVal = CreateObjectInstance(ThisArg.RelatedStateVar.GetJavaClass(), tRetVal);
            }
            catch(Exception rne)
            {
                RetVal = tRetVal;
            }
            StartIDX = 1;
        }
        for(int ID = StartIDX; ID < VarList.size(); ID++)
        {
            for(int ix = 0; ix < InVarArr.length; ix++)
            {
                if(InVarArr[ix].Name.compareTo(((UPnPArgument)VarList.get(ID)).Name) != 0)
                    continue;
                try
                {
                    UPnPArgument ThisArg = action.GetArg(InVarArr[ix].Name);
                    InVarArr[ix].DataValue = CreateObjectInstance(ThisArg.RelatedStateVar.GetJavaClass(), (String)((UPnPArgument)VarList.get(ID)).DataValue);
                    InVarArr[ix].DataType = ThisArg.RelatedStateVar.GetJavaClass().getName();
                }
                catch(Exception exception) { }
            }

        }

        TriggerEvent(new UPnPServiceEvent(this, MethodName, RetVal, InVarArr));
    }

    public Integer Major;
    public Integer Minor;
    public String _ServiceURN;
    public String ServiceID;
    public String SCPDURL;
    public String ControlURL;
    public String EventURL;
    public String EventCallbackURL;
    public UPnPDevice ParentDevice;
    public ArrayList EventList;
    public static int INVOKE_SUCCESS = 1;
    public static int INVOKE_FAILED = -1;
    public static int SUBSCRIBE_SUCCESS = 2;
    public static int SUBSCRIBE_FAILED = -2;
    public static int UPNPEVENT_RECEIVED = 3;
    protected Map StateVariables;
    protected Map MethodMap;
    protected Map MethodArgMap;
    protected Hashtable VarAssociation;
    protected Map RemoteMethodMap;
    protected Map SubscriberTable;
    protected Object ServiceInstance;
    protected int EventSID;

}
