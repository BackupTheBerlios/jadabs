// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StringFormatter.java

package Intel.UPnP;


public class StringFormatter
{

    public StringFormatter()
    {
    }

    public static String EscapeString(String InString)
    {
        String RetVal = "";
        for(int i = 0; i < InString.length(); i++)
            switch(InString.charAt(i))
            {
            case 38: // '&'
                RetVal = String.valueOf(String.valueOf(RetVal)).concat("&amp;");
                break;

            case 60: // '<'
                RetVal = String.valueOf(String.valueOf(RetVal)).concat("&lt;");
                break;

            case 62: // '>'
                RetVal = String.valueOf(String.valueOf(RetVal)).concat("&gt;");
                break;

            default:
                RetVal = String.valueOf(RetVal) + String.valueOf(String.valueOf(InString.charAt(i)));
                break;
            }

        return RetVal;
    }

    public static String UnEscapeString(String InString)
    {
        InString = Replace(InString, "&lt;", "<");
        InString = Replace(InString, "&gt;", ">");
        InString = Replace(InString, "&amp;", "&");
        return InString;
    }

    public static String Replace(String InString, String Original, String ChangeTo)
    {
        int len = InString.length();
        int olen = Original.length();
        String RetVal = "";
        for(int i = 0; i < len; i++)
            if(i + olen <= len)
            {
                String t = InString.substring(i, i + olen);
                if(t.compareTo(Original) == 0)
                {
                    RetVal = String.valueOf(RetVal) + String.valueOf(ChangeTo);
                    i += olen - 1;
                } else
                {
                    RetVal = String.valueOf(RetVal) + String.valueOf(t.substring(0, 1));
                }
            } else
            {
                RetVal = String.valueOf(RetVal) + String.valueOf(InString.substring(i, i + 1));
            }

        return RetVal;
    }
}
