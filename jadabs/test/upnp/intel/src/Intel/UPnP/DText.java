// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DText.java

package Intel.UPnP;


public class DText
{

    public DText()
    {
    }

    public int DCOUNT(String TheText, String DLMTR)
    {
        int NumChars = TheText.length();
        int Marks = 1;
        for(int id = 0; id < NumChars; id++)
            if(TheText.substring(id, id + 1).compareTo(DLMTR) == 0)
                Marks++;

        return Marks;
    }

    public String FIELD(String TheText, String DLMTR, int TokID)
    {
        int StartPos = -1;
        int EndPos = -1;
        int ID = 0;
        int pos = 0;
        if(DCOUNT(TheText, DLMTR) < TokID || TheText == "")
            return "";
        do
        {
            if(TokID == 1)
            {
                StartPos = 0;
                if(TheText.substring(0, 1).compareTo(DLMTR) == 0)
                    break;
            }
            if(TheText.substring(pos, pos + 1).compareTo(DLMTR) == 0)
                ID++;
            if(StartPos == -1 && TokID - 1 == ID)
                StartPos = pos + 1;
            if(ID >= TokID)
                EndPos = pos - 1;
            if(TheText.length() == pos + 1 && EndPos == -1)
                EndPos = pos;
            pos++;
        } while(EndPos == -1);
        if(TokID == 1 && TheText.substring(0, 1).compareTo(DLMTR) == 0)
            return "";
        else
            return TheText.substring(StartPos, EndPos + 1);
    }

    protected String DataString;
    protected String PreData;
    protected String PostData;
}
