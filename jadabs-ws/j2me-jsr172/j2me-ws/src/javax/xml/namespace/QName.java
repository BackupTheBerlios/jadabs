// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   QName.java

package javax.xml.namespace;


public class QName
{

    public QName(String namespaceURI, String localPart)
    {
        this(namespaceURI, localPart, "");
    }

    public QName(String namespaceURI, String localPart, String prefix)
    {
        if(namespaceURI == null)
            this.namespaceURI = "";
        else
            this.namespaceURI = namespaceURI;
        if(localPart == null)
            throw new IllegalArgumentException("local part cannot be \"null\" when creating a QName");
        this.localPart = localPart;
        if(prefix == null)
        {
            throw new IllegalArgumentException("prefix cannot be \"null\" when creating a QName");
        } else
        {
            this.prefix = prefix;
            return;
        }
    }

    public QName(String localPart)
    {
        this("", localPart, "");
    }

    public String getNamespaceURI()
    {
        return namespaceURI;
    }

    public String getLocalPart()
    {
        return localPart;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public boolean equals(Object objectToTest)
    {
        if(objectToTest == null || !(objectToTest instanceof QName))
        {
            return false;
        } else
        {
            QName qName = (QName)objectToTest;
            return namespaceURI.equals(qName.namespaceURI) && localPart.equals(qName.localPart);
        }
    }

    public int hashCode()
    {
        return namespaceURI.hashCode() ^ localPart.hashCode();
    }

    public String toString()
    {
        if(namespaceURI.equals(""))
            return localPart;
        else
            return "{" + namespaceURI + "}" + localPart;
    }

    public static QName valueOf(String qNameAsString)
    {
        if(qNameAsString == null)
            throw new IllegalArgumentException("cannot create QName from \"null\"");
        if(qNameAsString.length() == 0)
            return new QName("");
        if(qNameAsString.charAt(0) != '{')
            return new QName("", qNameAsString, "");
        int endOfNamespaceURI = qNameAsString.indexOf('}');
        if(endOfNamespaceURI == -1)
            throw new IllegalArgumentException("cannot create QName from \"" + qNameAsString + "\", missing closing \"}\"");
        if(endOfNamespaceURI == qNameAsString.length() - 1)
            throw new IllegalArgumentException("cannot create QName from \"" + qNameAsString + "\", missing local part");
        else
            return new QName(qNameAsString.substring(1, endOfNamespaceURI), qNameAsString.substring(endOfNamespaceURI + 1), "");
    }

    private final String namespaceURI;
    private final String localPart;
    private final String prefix;
}
