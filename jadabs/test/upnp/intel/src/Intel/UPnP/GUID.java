// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GUID.java

package Intel.UPnP;

import java.util.Random;

public class GUID
{

    private GUID()
    {
        parts = new int[32];
        for(int x = 0; x < 32; x++)
            parts[x] = random.nextInt(16);

    }

    public static GUID NewGuid()
    {
        return new GUID();
    }

    private String ConvertFromInt(int i)
    {
        String c = "";
        switch(i)
        {
        case 10: // '\n'
            c = "a";
            break;

        case 11: // '\013'
            c = "b";
            break;

        case 12: // '\f'
            c = "c";
            break;

        case 13: // '\r'
            c = "d";
            break;

        case 14: // '\016'
            c = "e";
            break;

        case 15: // '\017'
            c = "f";
            break;

        default:
            c = String.valueOf(i);
            break;
        }
        return c;
    }

    public String toString()
    {
        String RetVal = "";
        RetVal = "{";
        for(int i = 0; i < 8; i++)
            RetVal = String.valueOf(RetVal) + String.valueOf(ConvertFromInt(parts[i]));

        RetVal = String.valueOf(String.valueOf(RetVal)).concat("-");
        for(int i = 9; i < 13; i++)
            RetVal = String.valueOf(RetVal) + String.valueOf(ConvertFromInt(parts[i]));

        RetVal = String.valueOf(String.valueOf(RetVal)).concat("-");
        for(int i = 13; i < 17; i++)
            RetVal = String.valueOf(RetVal) + String.valueOf(ConvertFromInt(parts[i]));

        RetVal = String.valueOf(String.valueOf(RetVal)).concat("-");
        for(int i = 17; i < 21; i++)
            RetVal = String.valueOf(RetVal) + String.valueOf(ConvertFromInt(parts[i]));

        RetVal = String.valueOf(String.valueOf(RetVal)).concat("-");
        for(int i = 21; i < 32; i++)
            RetVal = String.valueOf(RetVal) + String.valueOf(ConvertFromInt(parts[i]));

        RetVal = String.valueOf(String.valueOf(RetVal)).concat("}");
        return RetVal;
    }

    private int parts[];
    private static Random random = new Random();

}
