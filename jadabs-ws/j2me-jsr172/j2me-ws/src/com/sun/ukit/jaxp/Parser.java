// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Parser.java

package com.sun.ukit.jaxp;

import java.io.*;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

// Referenced classes of package com.sun.ukit.jaxp:
//            Attrs, Input, ReaderUTF8, ReaderUTF16, 
//            Pair

public final class Parser extends SAXParser
    implements Locator
{

    public Parser(boolean nsaware)
    {
        mIsNSAware = nsaware;
        mBuff = new char[128];
        mAttrs = new Attrs();
        mPref = pair(mPref);
        mPref.name = "";
        mPref.value = "";
        mPref.chars = NONS;
        mNoNS = mPref;
        mPref = pair(mPref);
        mPref.name = "xml";
        mPref.value = "http://www.w3.org/XML/1998/namespace";
        mPref.chars = XML;
        mXml = mPref;
    }

    public String getPublicId()
    {
        return mInp == null ? null : mInp.pubid;
    }

    public String getSystemId()
    {
        return mInp == null ? null : mInp.sysid;
    }

    public int getLineNumber()
    {
        return -1;
    }

    public int getColumnNumber()
    {
        return -1;
    }

    public boolean isNamespaceAware()
    {
        return mIsNSAware;
    }

    public boolean isValidating()
    {
        return false;
    }

    public void parse(InputStream src, DefaultHandler handler)
        throws SAXException, IOException
    {
        if(src == null || handler == null)
        {
            throw new IllegalArgumentException("");
        } else
        {
            parse(new InputSource(src), handler);
            return;
        }
    }

    public void parse(InputSource is, DefaultHandler handler)
        throws SAXException, IOException
    {
        if(is == null || handler == null)
        {
            throw new IllegalArgumentException("");
        } else
        {
            mHand = handler;
            mInp = new Input((short)512);
            setinp(is);
            parse(handler);
            return;
        }
    }

    private void parse(DefaultHandler handler)
        throws SAXException, IOException
    {
        mPEnt = new Hashtable();
        mEnt = new Hashtable();
        mDoc = mInp;
        mChars = mInp.chars;
        mHand.setDocumentLocator(this);
        mHand.startDocument();
        mSt = 1;
        char ch;
        while((ch = next()) != '\uFFFF') 
label0:
            switch(chtyp(ch))
            {
            case 32: // ' '
                break;

            case 60: // '<'
                ch = next();
                switch(ch)
                {
                case 63: // '?'
                    pi();
                    break label0;

                case 33: // '!'
                    ch = next();
                    back();
                    if(ch == '-')
                        comm();
                    else
                        dtd();
                    break label0;
                }
                if(mSt == 5)
                    panic("");
                back();
                mSt = 4;
                elm();
                mSt = 5;
                break;

            default:
                panic("");
                break;
            }
        if(mSt != 5)
            panic("");
        mHand.endDocument();
        for(; mAttL != null; mAttL = del(mAttL))
            while(mAttL.list != null) 
            {
                if(mAttL.list.list != null)
                    del(mAttL.list.list);
                mAttL.list = del(mAttL.list);
            }

        for(; mElm != null; mElm = del(mElm));
        for(; mPref != mXml; mPref = del(mPref));
        while(mInp != null) 
            pop();
        if(mDoc != null && mDoc.src != null)
            try
            {
                mDoc.src.close();
            }
            catch(IOException ioe) { }
        mPEnt = null;
        mEnt = null;
        mDoc = null;
        mHand = null;
        break MISSING_BLOCK_LABEL_637;
        Exception exception;
        exception;
        mHand.endDocument();
        for(; mAttL != null; mAttL = del(mAttL))
            while(mAttL.list != null) 
            {
                if(mAttL.list.list != null)
                    del(mAttL.list.list);
                mAttL.list = del(mAttL.list);
            }

        for(; mElm != null; mElm = del(mElm));
        for(; mPref != mXml; mPref = del(mPref));
        while(mInp != null) 
            pop();
        if(mDoc != null && mDoc.src != null)
            try
            {
                mDoc.src.close();
            }
            catch(IOException ioe) { }
        mPEnt = null;
        mEnt = null;
        mDoc = null;
        mHand = null;
        throw exception;
    }

    private void dtd()
        throws SAXException, IOException
    {
        String str = null;
        String name = null;
        Pair psid = null;
        if(!"DOCTYPE".equals(name(false)))
            panic("");
        mSt = 2;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
            switch(st)
            {
            case 0: // '\0'
                if(chtyp(ch) != ' ')
                {
                    back();
                    name = name(mIsNSAware);
                    wsskip();
                    st = 1;
                }
                break;

            case 1: // '\001'
                switch(chtyp(ch))
                {
                case 65: // 'A'
                    back();
                    psid = pubsys(' ');
                    st = 2;
                    break;

                case 91: // '['
                    back();
                    st = 2;
                    break;

                case 62: // '>'
                    back();
                    st = 3;
                    break;

                default:
                    panic("");
                    break;
                }
                if(psid == null)
                    break;
                if(mHand.resolveEntity(psid.name, psid.value) != null)
                    panic("");
                del(psid);
                mHand.skippedEntity("[dtd]");
                break;

            case 2: // '\002'
                switch(chtyp(ch))
                {
                case 91: // '['
                    dtdint();
                    st = 3;
                    break;

                case 62: // '>'
                    back();
                    st = 3;
                    break;

                default:
                    panic("");
                    break;

                case 32: // ' '
                    break;
                }
                break;

            case 3: // '\003'
                switch(chtyp(ch))
                {
                case 62: // '>'
                    st = -1;
                    break;

                default:
                    panic("");
                    break;

                case 32: // ' '
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

        mSt = 3;
    }

    private void dtdint()
        throws SAXException, IOException
    {
        for(short st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 60: // '<'
                    ch = next();
                    switch(ch)
                    {
                    case 63: // '?'
                        pi();
                        break label0;

                    case 33: // '!'
                        ch = next();
                        back();
                        if(ch == '-')
                        {
                            comm();
                            break label0;
                        }
                        bntok();
                        switch(bkeyword())
                        {
                        case 110: // 'n'
                            dtdent();
                            break;

                        case 97: // 'a'
                            dtdattl();
                            break;

                        case 101: // 'e'
                            dtdelm();
                            break;

                        case 111: // 'o'
                            dtdnot();
                            break;

                        default:
                            panic("");
                            break;
                        }
                        st = 1;
                        break;

                    default:
                        panic("");
                        break;
                    }
                    break;

                case 37: // '%'
                    pent(' ');
                    break;

                case 93: // ']'
                    st = -1;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 1: // '\001'
                switch(ch)
                {
                case 62: // '>'
                    st = 0;
                    break;

                default:
                    panic("");
                    break;

                case 9: // '\t'
                case 10: // '\n'
                case 13: // '\r'
                case 32: // ' '
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void dtdent()
        throws SAXException, IOException
    {
        String str = null;
        char val[] = null;
        Input inp = null;
        Pair ids = null;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 37: // '%'
                    ch = next();
                    back();
                    if(chtyp(ch) == ' ')
                    {
                        wsskip();
                        str = name(false);
                        switch(chtyp(wsskip()))
                        {
                        case 65: // 'A'
                            ids = pubsys(' ');
                            if(wsskip() == '>')
                            {
                                inp = new Input();
                                inp.pubid = ids.name;
                                inp.sysid = ids.value;
                                mPEnt.put(str, inp);
                            } else
                            {
                                panic("");
                            }
                            del(ids);
                            st = -1;
                            break;

                        case 34: // '"'
                        case 39: // '\''
                            bqstr('-');
                            val = new char[mBuffIdx + 1];
                            System.arraycopy(mBuff, 1, val, 1, val.length - 1);
                            val[0] = ' ';
                            inp = new Input(val);
                            inp.pubid = mInp.pubid;
                            inp.sysid = mInp.sysid;
                            mPEnt.put(str, inp);
                            st = -1;
                            break;

                        default:
                            panic("");
                            break;
                        }
                    } else
                    {
                        pent(' ');
                    }
                    break;

                default:
                    back();
                    str = name(false);
                    st = 1;
                    break;
                }
                break;

            case 1: // '\001'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 34: // '"'
                case 39: // '\''
                    back();
                    bqstr('-');
                    if(mEnt.get(str) == null)
                    {
                        val = new char[mBuffIdx];
                        System.arraycopy(mBuff, 1, val, 0, val.length);
                        inp = new Input(val);
                        inp.pubid = mInp.pubid;
                        inp.sysid = mInp.sysid;
                        mEnt.put(str, inp);
                    }
                    st = -1;
                    break label0;

                case 65: // 'A'
                    back();
                    ids = pubsys(' ');
                    switch(wsskip())
                    {
                    case 62: // '>'
                        inp = new Input();
                        inp.pubid = ids.name;
                        inp.sysid = ids.value;
                        mEnt.put(str, inp);
                        break;

                    case 78: // 'N'
                        if("NDATA".equals(name(false)))
                        {
                            wsskip();
                            mHand.unparsedEntityDecl(str, ids.name, ids.value, name(false));
                            break;
                        }
                        // fall through

                    default:
                        panic("");
                        break;
                    }
                    del(ids);
                    st = -1;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void dtdelm()
        throws SAXException, IOException
    {
        wsskip();
        name(mIsNSAware);
        do
        {
            char ch = next();
            switch(ch)
            {
            case 62: // '>'
                back();
                return;

            case 65535: 
                panic("");
                break;
            }
        } while(true);
    }

    private void dtdattl()
        throws SAXException, IOException
    {
        char elmqn[] = null;
        Pair elm = null;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
            switch(st)
            {
            case 0: // '\0'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 58: // ':'
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                    back();
                    elmqn = qname(mIsNSAware);
                    elm = find(mAttL, elmqn);
                    if(elm == null)
                    {
                        elm = pair(mAttL);
                        elm.chars = elmqn;
                        mAttL = elm;
                    }
                    st = 1;
                    break;

                case 37: // '%'
                    pent(' ');
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 1: // '\001'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 58: // ':'
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                    back();
                    dtdatt(elm);
                    if(wsskip() == '>')
                        return;
                    break;

                case 37: // '%'
                    pent(' ');
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void dtdatt(Pair elm)
        throws SAXException, IOException
    {
        char attqn[] = null;
        Pair att = null;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 58: // ':'
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                    back();
                    attqn = qname(mIsNSAware);
                    att = find(elm.list, attqn);
                    if(att == null)
                    {
                        att = pair(elm.list);
                        att.chars = attqn;
                        elm.list = att;
                    } else
                    {
                        att.id = 'c';
                        if(att.list != null)
                            del(att.list);
                        att.list = null;
                    }
                    wsskip();
                    st = 1;
                    break;

                case 37: // '%'
                    pent(' ');
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 1: // '\001'
                switch(ch)
                {
                case 32: // ' '
                    break label0;

                case 40: // '('
                    back();
                    att.id = 'u';
                    st = 2;
                    break label0;

                case 37: // '%'
                    pent(' ');
                    break label0;
                }
                back();
                bntok();
                att.id = bkeyword();
                switch(att.id)
                {
                case 111: // 'o'
                    if(wsskip() != '(')
                        panic("");
                    st = 2;
                    break;

                case 78: // 'N'
                case 82: // 'R'
                case 84: // 'T'
                case 99: // 'c'
                case 105: // 'i'
                case 110: // 'n'
                case 114: // 'r'
                case 116: // 't'
                    wsskip();
                    st = 4;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 2: // '\002'
                if(ch != '(')
                    panic("");
                ch = wsskip();
                switch(chtyp(ch))
                {
                case 45: // '-'
                case 46: // '.'
                case 58: // ':'
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                case 100: // 'd'
                    switch(att.id)
                    {
                    case 117: // 'u'
                        bntok();
                        break;

                    case 111: // 'o'
                        mBuffIdx = -1;
                        bname(false);
                        break;

                    default:
                        panic("");
                        break;
                    }
                    wsskip();
                    st = 3;
                    break;

                case 37: // '%'
                    next();
                    pent(' ');
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 3: // '\003'
                switch(ch)
                {
                case 41: // ')'
                    wsskip();
                    st = 4;
                    break label0;

                case 124: // '|'
                    wsskip();
                    switch(att.id)
                    {
                    case 117: // 'u'
                        bntok();
                        break;

                    case 111: // 'o'
                        mBuffIdx = -1;
                        bname(false);
                        break;

                    default:
                        panic("");
                        break;
                    }
                    wsskip();
                    break;

                case 37: // '%'
                    pent(' ');
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 4: // '\004'
                switch(ch)
                {
                case 9: // '\t'
                case 10: // '\n'
                case 13: // '\r'
                case 32: // ' '
                    break;

                case 35: // '#'
                    bntok();
                    switch(bkeyword())
                    {
                    case 70: // 'F'
                        switch(wsskip())
                        {
                        case 34: // '"'
                        case 39: // '\''
                            st = 5;
                            break;

                        default:
                            st = -1;
                            break;
                        }
                        break;

                    case 73: // 'I'
                    case 81: // 'Q'
                        st = -1;
                        break;

                    default:
                        panic("");
                        break;
                    }
                    break;

                case 34: // '"'
                case 39: // '\''
                    back();
                    st = 5;
                    break;

                case 37: // '%'
                    pent(' ');
                    break;

                default:
                    back();
                    st = -1;
                    break;
                }
                break;

            case 5: // '\005'
                switch(ch)
                {
                case 34: // '"'
                case 39: // '\''
                    back();
                    bqstr('-');
                    att.list = pair(null);
                    att.list.chars = new char[att.chars.length + mBuffIdx + 3];
                    System.arraycopy(att.chars, 1, att.list.chars, 0, att.chars.length - 1);
                    att.list.chars[att.chars.length - 1] = '=';
                    att.list.chars[att.chars.length] = ch;
                    System.arraycopy(mBuff, 1, att.list.chars, att.chars.length + 1, mBuffIdx);
                    att.list.chars[att.chars.length + mBuffIdx + 1] = ch;
                    att.list.chars[att.chars.length + mBuffIdx + 2] = ' ';
                    st = -1;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void dtdnot()
        throws SAXException, IOException
    {
        wsskip();
        String name = name(false);
        wsskip();
        Pair ids = pubsys('N');
        mHand.notationDecl(name, ids.name, ids.value);
        del(ids);
    }

    private void elm()
        throws SAXException, IOException
    {
        Pair pref = mPref;
        mElm = pair(mElm);
        mElm.chars = qname(mIsNSAware);
        mElm.name = mElm.local();
        Pair elm = find(mAttL, mElm.chars);
        mAttrIdx = '\0';
        Pair att = pair(null);
        att.list = elm == null ? null : elm.list;
        attr(att);
        del(att);
        mBuffIdx = -1;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
            case 1: // '\001'
                switch(ch)
                {
                case 62: // '>'
                    if(mIsNSAware)
                    {
                        mElm.value = rslv(mElm.chars);
                        mHand.startElement(mElm.value, mElm.name, "", mAttrs);
                    } else
                    {
                        mHand.startElement("", "", mElm.name, mAttrs);
                    }
                    mItems = null;
                    st = st != 0 ? -1 : 2;
                    break label0;

                case 47: // '/'
                    if(st != 0)
                        panic("");
                    st = 1;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 2: // '\002'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    bappend(ch);
                    break;

                case 60: // '<'
                    bflash();
                    // fall through

                default:
                    back();
                    st = 3;
                    break;
                }
                break;

            case 3: // '\003'
                switch(ch)
                {
                case 38: // '&'
                    ent('c');
                    break label0;

                case 60: // '<'
                    bflash();
                    switch(next())
                    {
                    case 47: // '/'
                        mBuffIdx = -1;
                        bname(mIsNSAware);
                        char chars[] = mElm.chars;
                        if(chars.length == mBuffIdx + 1)
                        {
                            for(char i = '\001'; i <= mBuffIdx; i++)
                                if(chars[i] != mBuff[i])
                                    panic("");

                        } else
                        {
                            panic("");
                        }
                        if(wsskip() != '>')
                            panic("");
                        ch = next();
                        st = -1;
                        break;

                    case 33: // '!'
                        ch = next();
                        back();
                        switch(ch)
                        {
                        case 45: // '-'
                            comm();
                            break;

                        case 91: // '['
                            cdat();
                            break;

                        default:
                            panic("");
                            break;
                        }
                        break;

                    case 63: // '?'
                        pi();
                        break;

                    default:
                        back();
                        elm();
                        break;
                    }
                    mBuffIdx = -1;
                    if(st != -1)
                        st = 2;
                    break label0;

                case 13: // '\r'
                    if(next() != '\n')
                        back();
                    bappend('\n');
                    break;

                default:
                    bappend(ch);
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

        if(mIsNSAware)
            mHand.endElement(mElm.value, mElm.name, "");
        else
            mHand.endElement("", "", mElm.name);
        mElm = del(mElm);
        for(; mPref != pref; mPref = del(mPref))
            mHand.endPrefixMapping(mPref.name);

    }

    private void attr(Pair att)
        throws SAXException, IOException
    {
        Pair next;
        char norm;
        next = null;
        norm = 'c';
        wsskip();
        JVM INSTR lookupswitch 2: default 176
    //                   47: 36
    //                   62: 36;
           goto _L1 _L2 _L2
_L2:
        Pair def = att.list;
          goto _L3
_L5:
        if(def.list == null)
            continue; /* Loop/switch isn't completed */
        Pair act;
        for(act = att.next; act != null; act = act.next)
            if(act.eqname(def.chars))
                break;

        if(act != null)
            continue; /* Loop/switch isn't completed */
        push(new Input(def.list.chars));
        attr(att);
        if(next != null)
            del(next);
        return;
        def = def.next;
_L3:
        if(def != null) goto _L5; else goto _L4
_L4:
        mAttrs.setLength(mAttrIdx);
        mItems = mAttrs.mItems;
        if(next != null)
            del(next);
        return;
_L1:
        att.chars = qname(mIsNSAware);
        att.name = att.local();
        String type = "CDATA";
        if(att.list != null)
        {
            Pair attr = find(att.list, att.chars);
            if(attr != null)
                switch(attr.id)
                {
                case 105: // 'i'
                    type = "ID";
                    norm = 'i';
                    break;

                case 114: // 'r'
                    type = "IDREF";
                    norm = 'i';
                    break;

                case 82: // 'R'
                    type = "IDREFS";
                    norm = 'i';
                    break;

                case 110: // 'n'
                    type = "ENTITY";
                    norm = 'i';
                    break;

                case 78: // 'N'
                    type = "ENTITIES";
                    norm = 'i';
                    break;

                case 116: // 't'
                    type = "NMTOKEN";
                    norm = 'i';
                    break;

                case 84: // 'T'
                    type = "NMTOKENS";
                    norm = 'i';
                    break;

                case 117: // 'u'
                    type = "NMTOKEN";
                    norm = 'i';
                    break;

                case 111: // 'o'
                    type = "NOTATION";
                    norm = 'i';
                    break;

                case 99: // 'c'
                    norm = 'c';
                    break;

                case 79: // 'O'
                case 80: // 'P'
                case 81: // 'Q'
                case 83: // 'S'
                case 85: // 'U'
                case 86: // 'V'
                case 87: // 'W'
                case 88: // 'X'
                case 89: // 'Y'
                case 90: // 'Z'
                case 91: // '['
                case 92: // '\\'
                case 93: // ']'
                case 94: // '^'
                case 95: // '_'
                case 96: // '`'
                case 97: // 'a'
                case 98: // 'b'
                case 100: // 'd'
                case 101: // 'e'
                case 102: // 'f'
                case 103: // 'g'
                case 104: // 'h'
                case 106: // 'j'
                case 107: // 'k'
                case 108: // 'l'
                case 109: // 'm'
                case 112: // 'p'
                case 113: // 'q'
                case 115: // 's'
                default:
                    panic("");
                    break;
                }
        }
        wsskip();
        if(next() != '=')
            panic("");
        bqstr(norm);
        String val = new String(mBuff, 1, mBuffIdx);
        if(!mIsNSAware || !isdecl(att, val))
        {
            mAttrIdx++;
            next = pair(att);
            next.list = att.list;
            attr(next);
            mAttrIdx--;
            char idx = (char)(mAttrIdx << 3);
            mItems[idx + 1] = att.qname();
            mItems[idx + 2] = att.name;
            mItems[idx + 3] = val;
            mItems[idx + 4] = type;
            mItems[idx + 0] = att.chars[0] == 0 ? "" : rslv(att.chars);
        } else
        {
            mHand.startPrefixMapping(mPref.name, mPref.value);
            next = pair(att);
            next.list = att.list;
            attr(next);
        }
        break; /* Loop/switch isn't completed */
        exception;
        if(next != null)
            del(next);
        throw exception;
        Exception exception;
        if(next != null)
            del(next);
        return;
    }

    private void comm()
        throws SAXException, IOException
    {
        if(mSt == 0)
            mSt = 1;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
            switch(st)
            {
            case 0: // '\0'
                if(ch == '-')
                    st = 1;
                else
                    panic("");
                break;

            case 1: // '\001'
                if(ch == '-')
                    st = 2;
                else
                    panic("");
                break;

            case 2: // '\002'
                if(ch == '-')
                    st = 3;
                break;

            case 3: // '\003'
                st = ch != '-' ? 2 : 4;
                break;

            case 4: // '\004'
                if(ch == '>')
                    st = -1;
                else
                    panic("");
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void pi()
        throws SAXException, IOException
    {
        String str = null;
        mBuffIdx = -1;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
            switch(st)
            {
            case 0: // '\0'
                switch(chtyp(ch))
                {
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                    back();
                    str = name(false);
                    st = 1;
                    break;

                case 32: // ' '
                    str = "";
                    st = 2;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 1: // '\001'
                if(chtyp(ch) == ' ')
                    break;
                back();
                if(mXml.name.equals(str.toLowerCase()))
                {
                    panic("");
                } else
                {
                    if(mSt == 0)
                        mSt = 1;
                    st = 2;
                }
                mBuffIdx = -1;
                break;

            case 2: // '\002'
                if(ch == '?')
                    st = 3;
                else
                    bappend(ch);
                break;

            case 3: // '\003'
                if(ch == '>')
                {
                    mHand.processingInstruction(str, new String(mBuff, 0, mBuffIdx + 1));
                    st = -1;
                } else
                {
                    bappend('?');
                    bappend(ch);
                    st = 2;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void cdat()
        throws SAXException, IOException
    {
        mBuffIdx = -1;
        for(short st = 0; st >= 0;)
        {
            char ch = next();
            switch(st)
            {
            case 0: // '\0'
                if(ch == '[')
                    st = 1;
                else
                    panic("");
                break;

            case 1: // '\001'
                if(chtyp(ch) == 'A')
                {
                    bappend(ch);
                    break;
                }
                if(!"CDATA".equals(new String(mBuff, 0, mBuffIdx + 1)))
                    panic("");
                back();
                st = 2;
                break;

            case 2: // '\002'
                if(ch != '[')
                    panic("");
                mBuffIdx = -1;
                st = 3;
                break;

            case 3: // '\003'
                if(ch != ']')
                    bappend(ch);
                else
                    st = 4;
                break;

            case 4: // '\004'
                if(ch != ']')
                {
                    bappend(']');
                    bappend(ch);
                    st = 3;
                } else
                {
                    st = 5;
                }
                break;

            case 5: // '\005'
                if(ch != '>')
                {
                    bappend(']');
                    bappend(']');
                    bappend(ch);
                    st = 3;
                } else
                {
                    bflash();
                    st = -1;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private String name(boolean ns)
        throws SAXException, IOException
    {
        mBuffIdx = -1;
        bname(ns);
        return new String(mBuff, 1, mBuffIdx);
    }

    private char[] qname(boolean ns)
        throws SAXException, IOException
    {
        mBuffIdx = -1;
        bname(ns);
        char chars[] = new char[mBuffIdx + 1];
        System.arraycopy(mBuff, 0, chars, 0, mBuffIdx + 1);
        return chars;
    }

    private void pubsys(Input inp)
        throws SAXException, IOException
    {
        Pair pair = pubsys(' ');
        inp.pubid = pair.name;
        inp.sysid = pair.value;
        del(pair);
    }

    private Pair pubsys(char flag)
        throws SAXException, IOException
    {
        Pair ids = pair(null);
        String str = name(false);
        if("PUBLIC".equals(str))
        {
            bqstr('i');
            ids.name = new String(mBuff, 1, mBuffIdx);
            switch(wsskip())
            {
            case 34: // '"'
            case 39: // '\''
                bqstr(' ');
                ids.value = new String(mBuff, 1, mBuffIdx);
                break;

            default:
                if(flag != 'N')
                    panic("");
                ids.value = null;
                break;
            }
            return ids;
        }
        if("SYSTEM".equals(str))
        {
            ids.name = null;
            bqstr(' ');
            ids.value = new String(mBuff, 1, mBuffIdx);
            return ids;
        } else
        {
            panic("");
            return null;
        }
    }

    private String eqstr(char flag)
        throws SAXException, IOException
    {
        if(flag == '=')
        {
            wsskip();
            if(next() != '=')
                panic("");
        }
        bqstr(flag);
        return new String(mBuff, 1, mBuffIdx);
    }

    private void ent(char flag)
        throws SAXException, IOException
    {
        short idx = (short)(mBuffIdx + 1);
        Input inp = null;
        String str = null;
        mESt = '\u0100';
        bappend('&');
        for(short st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
            case 1: // '\001'
                switch(chtyp(ch))
                {
                case 45: // '-'
                case 46: // '.'
                case 100: // 'd'
                    if(st != 1)
                        panic("");
                    // fall through

                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                    bappend(ch);
                    eappend(ch);
                    st = 1;
                    break label0;

                case 58: // ':'
                    if(mIsNSAware)
                        panic("");
                    bappend(ch);
                    eappend(ch);
                    st = 1;
                    break label0;

                case 59: // ';'
                    if(mESt < '\u0100')
                    {
                        mBuffIdx = (short)(idx - 1);
                        bappend(mESt);
                        st = -1;
                        break label0;
                    }
                    if(mSt == 2)
                    {
                        bappend(';');
                        st = -1;
                        break label0;
                    }
                    str = new String(mBuff, idx + 1, mBuffIdx - idx);
                    inp = (Input)mEnt.get(str);
                    mBuffIdx = (short)(idx - 1);
                    if(inp != null)
                    {
                        if(inp.chars == null)
                        {
                            InputSource is = mHand.resolveEntity(inp.pubid, inp.sysid);
                            if(is != null)
                            {
                                push(new Input((short)512));
                                setinp(is);
                                mInp.pubid = inp.pubid;
                                mInp.sysid = inp.sysid;
                            } else
                            {
                                bflash();
                                if(flag != 'c')
                                    panic("");
                                mHand.skippedEntity(str);
                            }
                        } else
                        {
                            push(inp);
                        }
                    } else
                    {
                        bflash();
                        if(flag != 'c')
                            panic("");
                        mHand.skippedEntity(str);
                    }
                    st = -1;
                    break label0;

                case 35: // '#'
                    if(st != 0)
                        panic("");
                    st = 2;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 2: // '\002'
                switch(chtyp(ch))
                {
                case 100: // 'd'
                    bappend(ch);
                    break label0;

                case 59: // ';'
                    try
                    {
                        ch = (char)Short.parseShort(new String(mBuff, idx + 1, mBuffIdx - idx), 10);
                    }
                    catch(NumberFormatException nfe)
                    {
                        panic("");
                    }
                    mBuffIdx = (short)(idx - 1);
                    bappend(ch);
                    st = -1;
                    break label0;

                case 97: // 'a'
                    if(mBuffIdx == idx && ch == 'x')
                    {
                        st = 3;
                        break label0;
                    }
                    // fall through

                default:
                    panic("");
                    break;
                }
                break;

            case 3: // '\003'
                switch(chtyp(ch))
                {
                case 65: // 'A'
                case 97: // 'a'
                case 100: // 'd'
                    bappend(ch);
                    break label0;

                case 59: // ';'
                    try
                    {
                        ch = (char)Short.parseShort(new String(mBuff, idx + 1, mBuffIdx - idx), 16);
                    }
                    catch(NumberFormatException nfe)
                    {
                        panic("");
                    }
                    mBuffIdx = (short)(idx - 1);
                    bappend(ch);
                    st = -1;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

    }

    private void pent(char flag)
        throws SAXException, IOException
    {
        short idx = (short)(mBuffIdx + 1);
        Input inp = null;
        String str = null;
        bappend('%');
        if(mSt != 2)
            return;
        bname(false);
        str = new String(mBuff, idx + 2, mBuffIdx - idx - 1);
        if(next() != ';')
            panic("");
        inp = (Input)mPEnt.get(str);
        mBuffIdx = (short)(idx - 1);
        if(inp != null)
        {
            if(inp.chars == null)
            {
                InputSource is = mHand.resolveEntity(inp.pubid, inp.sysid);
                if(is != null)
                {
                    if(flag != '-')
                        bappend(' ');
                    push(new Input((short)512));
                    setinp(is);
                    mInp.pubid = inp.pubid;
                    mInp.sysid = inp.sysid;
                } else
                {
                    mHand.skippedEntity("%" + str);
                }
            } else
            {
                if(flag == '-')
                {
                    inp.chIdx = '\001';
                } else
                {
                    bappend(' ');
                    inp.chIdx = '\0';
                }
                push(inp);
            }
        } else
        {
            mHand.skippedEntity("%" + str);
        }
    }

    private boolean isdecl(Pair name, String value)
    {
        if(name.chars[0] == 0)
        {
            if("xmlns".equals(name.name))
            {
                mPref = pair(mPref);
                mPref.value = value;
                mPref.name = "";
                mPref.chars = NONS;
                return true;
            }
        } else
        if(name.eqpref(XMLNS))
        {
            int len = name.name.length();
            mPref = pair(mPref);
            mPref.value = value;
            mPref.name = name.name;
            mPref.chars = new char[len + 1];
            mPref.chars[0] = (char)(len + 1);
            name.name.getChars(0, len, mPref.chars, 1);
            return true;
        }
        return false;
    }

    private String rslv(char qname[])
        throws SAXException
    {
        for(Pair pref = mPref; pref != null; pref = pref.next)
            if(pref.eqpref(qname))
                return pref.value;

        panic("");
        return null;
    }

    private char wsskip()
        throws SAXException, IOException
    {
        char ch;
label0:
        do
        {
            ch = next();
            switch(ch)
            {
            case 9: // '\t'
            case 10: // '\n'
            case 13: // '\r'
            case 32: // ' '
                break;

            case 65535: 
                panic("");
                // fall through

            default:
                back();
                break label0;
            }
        } while(true);
        return ch;
    }

    private void panic(String msg)
        throws SAXException
    {
        mHand.fatalError(new SAXParseException(msg, this));
    }

    private void bname(boolean ns)
        throws SAXException, IOException
    {
        char pos = (char)(mBuffIdx + 1);
        char idx = pos;
        short st = (short)(!ns ? 2 : 0);
        bappend('\0');
        while(st >= 0) 
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
            case 2: // '\002'
                switch(chtyp(ch))
                {
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                    bappend(ch);
                    st++;
                    break;

                case 58: // ':'
                    back();
                    st++;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 1: // '\001'
            case 3: // '\003'
                switch(chtyp(ch))
                {
                case 45: // '-'
                case 46: // '.'
                case 65: // 'A'
                case 88: // 'X'
                case 95: // '_'
                case 97: // 'a'
                case 100: // 'd'
                    bappend(ch);
                    break label0;

                case 58: // ':'
                    bappend(ch);
                    if(!ns)
                        break label0;
                    if(idx != pos)
                        panic("");
                    idx = (char)mBuffIdx;
                    if(st == 1)
                        st = 2;
                    break;

                default:
                    back();
                    mBuff[pos] = idx;
                    return;
                }
                break;

            default:
                panic("");
                break;
            }
        }
    }

    private void bntok()
        throws SAXException, IOException
    {
        mBuffIdx = -1;
        bappend('\0');
label0:
        do
        {
            char ch = next();
            switch(chtyp(ch))
            {
            case 45: // '-'
            case 46: // '.'
            case 58: // ':'
            case 65: // 'A'
            case 88: // 'X'
            case 95: // '_'
            case 97: // 'a'
            case 100: // 'd'
                bappend(ch);
                break;

            default:
                back();
                break label0;
            }
        } while(true);
    }

    private char bkeyword()
        throws SAXException, IOException
    {
        String str = new String(mBuff, 1, mBuffIdx);
        switch(str.length())
        {
        case 3: // '\003'
        case 4: // '\004'
        default:
            break;

        case 2: // '\002'
            return !"ID".equals(str) ? 63 : 'i';

        case 5: // '\005'
            switch(mBuff[1])
            {
            case 73: // 'I'
                return !"IDREF".equals(str) ? 63 : 'r';

            case 67: // 'C'
                return !"CDATA".equals(str) ? 63 : 'c';

            case 70: // 'F'
                return !"FIXED".equals(str) ? 63 : 'F';
            }
            break;

        case 6: // '\006'
            switch(mBuff[1])
            {
            case 73: // 'I'
                return !"IDREFS".equals(str) ? 63 : 'R';

            case 69: // 'E'
                return !"ENTITY".equals(str) ? 63 : 'n';
            }
            break;

        case 7: // '\007'
            switch(mBuff[1])
            {
            case 73: // 'I'
                return !"IMPLIED".equals(str) ? 63 : 'I';

            case 78: // 'N'
                return !"NMTOKEN".equals(str) ? 63 : 't';

            case 65: // 'A'
                return !"ATTLIST".equals(str) ? 63 : 'a';

            case 69: // 'E'
                return !"ELEMENT".equals(str) ? 63 : 'e';
            }
            break;

        case 8: // '\b'
            switch(mBuff[2])
            {
            case 78: // 'N'
                return !"ENTITIES".equals(str) ? 63 : 'N';

            case 77: // 'M'
                return !"NMTOKENS".equals(str) ? 63 : 'T';

            case 79: // 'O'
                return !"NOTATION".equals(str) ? 63 : 'o';

            case 69: // 'E'
                return !"REQUIRED".equals(str) ? 63 : 'Q';
            }
            break;
        }
        return '?';
    }

    private void bqstr(char flag)
        throws SAXException, IOException
    {
        Input inp = mInp;
        mBuffIdx = -1;
        bappend('\0');
        for(short st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
                switch(ch)
                {
                case 39: // '\''
                    st = 2;
                    break;

                case 34: // '"'
                    st = 3;
                    break;

                default:
                    panic("");
                    break;

                case 9: // '\t'
                case 10: // '\n'
                case 13: // '\r'
                case 32: // ' '
                    break;
                }
                break;

            case 2: // '\002'
            case 3: // '\003'
                switch(ch)
                {
                case 39: // '\''
                    if(st == 2 && mInp == inp)
                        st = -1;
                    else
                        bappend(ch);
                    break label0;

                case 34: // '"'
                    if(st == 3 && mInp == inp)
                        st = -1;
                    else
                        bappend(ch);
                    break label0;

                case 38: // '&'
                    ent(' ');
                    break label0;

                case 37: // '%'
                    pent(flag);
                    break label0;

                case 13: // '\r'
                    if(flag != ' ')
                    {
                        if(next() != '\n')
                            back();
                        ch = '\n';
                    }
                    // fall through

                default:
                    switch(flag)
                    {
                    case 105: // 'i'
                        switch(ch)
                        {
                        case 9: // '\t'
                        case 10: // '\n'
                        case 32: // ' '
                            if(mBuffIdx > 0 && mBuff[mBuffIdx] != ' ')
                                bappend(' ');
                            break;

                        default:
                            bappend(ch);
                            break;
                        }
                        break label0;

                    case 99: // 'c'
                        switch(ch)
                        {
                        case 9: // '\t'
                        case 10: // '\n'
                            bappend(' ');
                            break;

                        default:
                            bappend(ch);
                            break;
                        }
                        break;

                    default:
                        bappend(ch);
                        break;
                    }
                    break;
                }
                break;

            case 1: // '\001'
            default:
                panic("");
                break;
            }
        }

        if(flag == 'i' && mBuff[mBuffIdx] == ' ')
            mBuffIdx--;
    }

    private void bflash()
        throws SAXException
    {
        if(mBuffIdx >= 0)
        {
            mHand.characters(mBuff, 0, mBuffIdx + 1);
            mBuffIdx = -1;
        }
    }

    private void bappend(char ch)
    {
        try
        {
            mBuffIdx++;
            mBuff[mBuffIdx] = ch;
        }
        catch(Exception exp)
        {
            char buff[] = new char[mBuff.length << 1];
            System.arraycopy(mBuff, 0, buff, 0, mBuff.length);
            mBuff = buff;
            mBuff[mBuffIdx] = ch;
        }
    }

    private void eappend(char ch)
    {
        switch(mESt)
        {
        default:
            break;

        case 256: 
            switch(ch)
            {
            case 108: // 'l'
                mESt = '\u0101';
                break;

            case 103: // 'g'
                mESt = '\u0102';
                break;

            case 97: // 'a'
                mESt = '\u0103';
                break;

            case 113: // 'q'
                mESt = '\u0107';
                break;

            default:
                mESt = '\u0200';
                break;
            }
            break;

        case 257: 
            mESt = ch != 't' ? '\u0200' : '<';
            break;

        case 258: 
            mESt = ch != 't' ? '\u0200' : '>';
            break;

        case 259: 
            switch(ch)
            {
            case 109: // 'm'
                mESt = '\u0104';
                break;

            case 112: // 'p'
                mESt = '\u0105';
                break;

            default:
                mESt = '\u0200';
                break;
            }
            break;

        case 260: 
            mESt = ch != 'p' ? '\u0200' : '&';
            break;

        case 261: 
            mESt = ch != 'o' ? '\u0200' : '\u0106';
            break;

        case 262: 
            mESt = ch != 's' ? '\u0200' : '\'';
            break;

        case 263: 
            mESt = ch != 'u' ? '\u0200' : '\u0108';
            break;

        case 264: 
            mESt = ch != 'o' ? '\u0200' : '\u0109';
            break;

        case 265: 
            mESt = ch != 't' ? '\u0200' : '"';
            break;

        case 34: // '"'
        case 38: // '&'
        case 39: // '\''
        case 60: // '<'
        case 62: // '>'
            mESt = '\u0200';
            break;
        }
    }

    private void setinp(InputSource is)
        throws SAXException, IOException
    {
        Reader reader = null;
        mChIdx = '\0';
        mChLen = '\0';
        mChars = mInp.chars;
        mInp.src = null;
        if(is.getCharacterStream() != null)
        {
            reader = is.getCharacterStream();
            xml(reader);
        } else
        if(is.getByteStream() != null)
        {
            if(is.getEncoding() != null)
            {
                String encoding = is.getEncoding().toUpperCase();
                if(encoding.equals("UTF-16"))
                    reader = bom(is.getByteStream(), 'U');
                else
                    reader = enc(encoding, is.getByteStream());
                xml(reader);
            } else
            {
                reader = bom(is.getByteStream(), ' ');
                if(reader == null)
                {
                    reader = enc("UTF-8", is.getByteStream());
                    reader = enc(xml(reader), is.getByteStream());
                } else
                {
                    xml(reader);
                }
            }
        } else
        {
            panic("");
        }
        mInp.src = reader;
        mInp.pubid = is.getPublicId();
        mInp.sysid = is.getSystemId();
    }

    private Reader bom(InputStream is, char hint)
        throws SAXException, IOException
    {
        int val = is.read();
        switch(val)
        {
        case 239: 
            if(hint == 'U')
                panic("");
            if(is.read() != 187)
                panic("");
            if(is.read() != 191)
                panic("");
            return new ReaderUTF8(is);

        case 254: 
            if(is.read() != 255)
                panic("");
            return new ReaderUTF16(is, 'b');

        case 255: 
            if(is.read() != 254)
                panic("");
            return new ReaderUTF16(is, 'l');

        case -1: 
            mChars[mChIdx++] = '\uFFFF';
            return new ReaderUTF8(is);
        }
        if(hint == 'U')
            panic("");
        switch(val & 0xf0)
        {
        case 192: 
        case 208: 
            mChars[mChIdx++] = (char)((val & 0x1f) << 6 | is.read() & 0x3f);
            break;

        case 224: 
            mChars[mChIdx++] = (char)((val & 0xf) << 12 | (is.read() & 0x3f) << 6 | is.read() & 0x3f);
            break;

        case 240: 
            throw new UnsupportedEncodingException();

        default:
            mChars[mChIdx++] = (char)val;
            break;
        }
        return null;
    }

    private String xml(Reader reader)
        throws SAXException, IOException
    {
        String str = null;
        String enc = "UTF-8";
        short st;
        if(mChIdx != 0)
            st = (short)(mChars[0] != '<' ? -1 : 1);
        else
            st = 0;
        while(st >= 0) 
        {
            int val;
            char ch = (val = reader.read()) < 0 ? '\uFFFF' : (char)val;
            mChars[mChIdx++] = ch;
            switch(st)
            {
            case 0: // '\0'
                switch(ch)
                {
                case 60: // '<'
                    st = 1;
                    break;

                case 65279: 
                    ch = (val = reader.read()) < 0 ? '\uFFFF' : (char)val;
                    mChars[mChIdx - 1] = ch;
                    st = (short)(ch != '<' ? -1 : 1);
                    break;

                default:
                    st = -1;
                    break;
                }
                break;

            case 1: // '\001'
                st = (short)(ch != '?' ? -1 : 2);
                break;

            case 2: // '\002'
                st = (short)(ch != 'x' && ch != 'X' ? -1 : 3);
                break;

            case 3: // '\003'
                st = (short)(ch != 'm' && ch != 'M' ? -1 : 4);
                break;

            case 4: // '\004'
                st = (short)(ch != 'l' && ch != 'L' ? -1 : 5);
                break;

            case 5: // '\005'
                switch(ch)
                {
                case 9: // '\t'
                case 10: // '\n'
                case 13: // '\r'
                case 32: // ' '
                    st = 6;
                    break;

                default:
                    st = -1;
                    break;
                }
                break;

            case 6: // '\006'
                if(ch == '?')
                    st = 7;
                break;

            case 7: // '\007'
                st = (short)(ch == '>' ? -2 : 6);
                break;

            default:
                panic("");
                break;
            }
        }
        mChLen = mChIdx;
        mChIdx = '\0';
        if(st == -1)
            return enc;
        mChIdx = '\005';
        for(st = 0; st >= 0;)
        {
            char ch = next();
label0:
            switch(st)
            {
            case 0: // '\0'
                if(chtyp(ch) != ' ')
                {
                    back();
                    st = 1;
                }
                break;

            case 1: // '\001'
            case 2: // '\002'
            case 3: // '\003'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 65: // 'A'
                case 95: // '_'
                case 97: // 'a'
                    back();
                    str = name(false).toLowerCase();
                    if("version".equals(str))
                    {
                        if(st != 1)
                            panic("");
                        if(!"1.0".equals(eqstr('=')))
                            panic("");
                        st = 2;
                        break label0;
                    }
                    if("encoding".equals(str))
                    {
                        if(st != 2)
                            panic("");
                        enc = eqstr('=').toUpperCase();
                        st = 3;
                        break label0;
                    }
                    if("standalone".equals(str))
                    {
                        if(st == 1)
                            panic("");
                        str = eqstr('=');
                        st = 4;
                    } else
                    {
                        panic("");
                    }
                    break label0;

                case 63: // '?'
                    if(st == 1)
                        panic("");
                    back();
                    st = 4;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            case 4: // '\004'
                switch(chtyp(ch))
                {
                case 32: // ' '
                    break;

                case 63: // '?'
                    if(next() != '>')
                        panic("");
                    if(mSt == 0)
                        mSt = 1;
                    st = -1;
                    break;

                default:
                    panic("");
                    break;
                }
                break;

            default:
                panic("");
                break;
            }
        }

        return enc;
    }

    private Reader enc(String name, InputStream is)
        throws UnsupportedEncodingException
    {
        if(name.equals("UTF-8"))
            return new ReaderUTF8(is);
        if(name.equals("UTF-16LE"))
            return new ReaderUTF16(is, 'l');
        if(name.equals("UTF-16BE"))
            return new ReaderUTF16(is, 'b');
        else
            return new InputStreamReader(is, name);
    }

    private void push(Input inp)
    {
        mInp.chLen = mChLen;
        mInp.chIdx = mChIdx;
        inp.next = mInp;
        mInp = inp;
        mChars = inp.chars;
        mChLen = inp.chLen;
        mChIdx = inp.chIdx;
    }

    private void pop()
    {
        if(mInp.src != null)
        {
            try
            {
                mInp.src.close();
            }
            catch(IOException ioe) { }
            mInp.src = null;
        }
        mInp = mInp.next;
        if(mInp != null)
        {
            mChars = mInp.chars;
            mChLen = mInp.chLen;
            mChIdx = mInp.chIdx;
        } else
        {
            mChars = null;
            mChLen = '\0';
            mChIdx = '\0';
        }
    }

    private char chtyp(char ch)
        throws SAXException
    {
        if(ch < '\200')
            return (char)asctyp[ch];
        if(ch == '\uFFFF')
            panic("");
        return 'X';
    }

    private char next()
        throws IOException
    {
        if(mChIdx >= mChLen)
        {
            if(mInp.src == null)
            {
                pop();
                return next();
            }
            int Num = mInp.src.read(mChars, 0, mChars.length);
            if(Num < 0)
            {
                if(mInp != mDoc)
                {
                    pop();
                    return next();
                }
                mChars[0] = '\uFFFF';
                mChLen = '\001';
            } else
            {
                mChLen = (char)Num;
            }
            mChIdx = '\0';
        }
        return mChars[mChIdx++];
    }

    private void back()
        throws SAXException
    {
        if(mChIdx <= 0)
            panic("");
        mChIdx--;
    }

    private Pair find(Pair chain, char qname[])
    {
        for(Pair pair = chain; pair != null; pair = pair.next)
            if(pair.eqname(qname))
                return pair;

        return null;
    }

    private Pair pair(Pair next)
    {
        Pair pair;
        if(mDltd != null)
        {
            pair = mDltd;
            mDltd = pair.next;
        } else
        {
            pair = new Pair();
        }
        pair.next = next;
        return pair;
    }

    private Pair del(Pair pair)
    {
        Pair next = pair.next;
        pair.name = null;
        pair.value = null;
        pair.chars = null;
        pair.list = null;
        pair.next = mDltd;
        mDltd = pair;
        return next;
    }

    public static final String FAULT = "";
    private static final short BUFFSIZE_READER = 512;
    private static final short BUFFSIZE_PARSER = 128;
    private static final short BUFFSIZE_ENTITY = 32;
    public static final char EOS = 65535;
    private Pair mNoNS;
    private Pair mXml;
    private DefaultHandler mHand;
    private Hashtable mEnt;
    private Hashtable mPEnt;
    private boolean mIsNSAware;
    private short mSt;
    private char mESt;
    private char mBuff[];
    private short mBuffIdx;
    private Pair mPref;
    private Pair mElm;
    private Pair mAttL;
    private Input mInp;
    private Input mDoc;
    private char mChars[];
    private char mChLen;
    private char mChIdx;
    private Attrs mAttrs;
    private String mItems[];
    private char mAttrIdx;
    private Pair mDltd;
    private static final char NONS[];
    private static final char XML[];
    private static final char XMLNS[];
    private static final byte asctyp[];

    static 
    {
        FAULT = "";
        BUFFSIZE_READER = 512;
        BUFFSIZE_PARSER = 128;
        BUFFSIZE_ENTITY = 32;
        EOS = '\uFFFF';
        NONS = new char[1];
        NONS[0] = '\0';
        XML = new char[4];
        XML[0] = '\004';
        XML[1] = 'x';
        XML[2] = 'm';
        XML[3] = 'l';
        XMLNS = new char[6];
        XMLNS[0] = '\006';
        XMLNS[1] = 'x';
        XMLNS[2] = 'm';
        XMLNS[3] = 'l';
        XMLNS[4] = 'n';
        XMLNS[5] = 's';
        asctyp = new byte[128];
        short i;
        for(i = 0; i <= 31;)
            asctyp[i++] = 122;

        asctyp[9] = 32;
        asctyp[13] = 32;
        asctyp[10] = 32;
        for(; i <= 47; i++)
            asctyp[i] = (byte)i;

        while(i <= 57) 
            asctyp[i++] = 100;
        for(; i <= 64; i++)
            asctyp[i] = (byte)i;

        while(i <= 90) 
            asctyp[i++] = 65;
        for(; i <= 96; i++)
            asctyp[i] = (byte)i;

        while(i <= 122) 
            asctyp[i++] = 97;
        for(; i <= 127; i++)
            asctyp[i] = (byte)i;

    }
}
