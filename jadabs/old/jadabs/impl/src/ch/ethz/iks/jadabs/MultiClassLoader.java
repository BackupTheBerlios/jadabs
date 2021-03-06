/*
 * Created on Jul 8, 2003
 * 
 * $Id: MultiClassLoader.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import java.util.Hashtable;

import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * A simple test class loader capable of loading from multiple sources, such as
 * local files or a URL.
 * 
 * This class is derived from an article by Chuck McManis
 * http://www.javaworld.com/javaworld/jw-10-1996/indepth.src.html with large
 * modifications.
 * 
 * Note that this has been updated to use the non-deprecated version of
 * defineClass() -- JDM.
 * 
 * @author Jack Harich - 8/18/97
 * @author John D. Mitchell - 99.03.04
 * @author Andreas Frei - 25.4.2003
 */
public abstract class MultiClassLoader extends ClassLoader {

    private static ILogger LOG             = Logger
                                                   .getLogger(MultiClassLoader.class);

    //---------- Fields --------------------------------------
    private Hashtable      classes         = new Hashtable();

    private char           classNameReplacementChar;

    protected boolean      monitorOn       = true;

    protected boolean      sourceMonitorOn = true;

    //---------- Initialization ------------------------------
    public MultiClassLoader() {

    }

    //---------- Superclass Overrides ------------------------

    /**
     * This is a simple version for external clients since they will always want
     * the class resolved before it is returned to them.
     */
    public Class loadClass(String className) throws ClassNotFoundException
    {
        return (loadClass(className, true));
    }

    //---------- Abstract Implementation ---------------------
    public synchronized Class loadClass(String className, boolean resolveIt)
            throws ClassNotFoundException
    {
        final boolean log = className.endsWith("TestComponentMain");
        Class result;
        byte[] classBytes;
        if (LOG.isDebugEnabled())
        {
            LOG.debug(this.toString() + ">> MultiClassLoader.loadClass("
                    + className + ", " + resolveIt + ")");
        }
        //----- Check our local cache of classes
        result = (Class) classes.get(className);

        if (result != null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(this.toString() + ">> returning cached result.");
            }
            return result;
        }

        //----- Check with the primordial class loader
        try
        {
            result = super.findSystemClass(className);
            if (LOG.isDebugEnabled())
            {
                LOG.debug(this.toString()
                        + ">> returning system class (in CLASSPATH).");
            }
            return result;
        } catch (ClassNotFoundException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(this.toString() + ">> Not a system class.");
            }
        }

        //----- Try to load it from preferred source
        // Note loadClassBytes() is an abstract method
        classBytes = loadClassBytes(className);

        if (classBytes == null)
        {
            // try to load Class from a dependency
            try
            {
                result = loadClassFromDependency(className, resolveIt);
                if (LOG.isDebugEnabled())
                {
                    LOG.debug(this.toString()
                            + ">> returning dependency class.");
                }
                return result;
            } catch (ClassNotFoundException e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug(this.toString() + ">> Not a dependency class.");
                }
            }

            throw new ClassNotFoundException();
        } else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug(this.toString()
                        + ">> class found in component (loadClassBytes).");
            }
        }

        //----- Define it (parse the class file)
        result = defineClass(className, classBytes, 0, classBytes.length);
        if (result == null) { throw new ClassFormatError(); }

        //----- Resolve if necessary
        if (resolveIt) resolveClass(result);

        // Done
        classes.put(className, result);
        if (LOG.isDebugEnabled())
        {
            LOG.debug(this.toString() + ">> Returning newly loaded class.");
        }
        return result;
    }

    //---------- Public Methods ------------------------------
    /**
     * This optional call allows a class name such as "COM.test.Hello" to be
     * changed to "COM_test_Hello", which is useful for storing classes from
     * different packages in the same retrival directory. In the above example
     * the char would be '_'.
     */
    public void setClassNameReplacementChar(char replacement)
    {
        classNameReplacementChar = replacement;
    }

    //---------- Protected Methods ---------------------------
    protected abstract byte[] loadClassBytes(String className);

    protected abstract Class loadClassFromDependency(String className,
            boolean resolveIt) throws ClassNotFoundException;

    protected String formatClassName(String className)
    {
        if (classNameReplacementChar == '\u0000')
        {
            // '/' is used to map the package to the path
            return className.replace('.', '/') + ".class";
        } else
        {
            // Replace '.' with custom char, such as '_'
            return className.replace('.', classNameReplacementChar) + ".class";
        }
    }

    //	protected void monitor(String text) {
    //	    if (monitorOn) print(text);
    //	}

    //--- Std
    //	protected static void print(String text) {
    //	    System.out.println(text);
    //	}

} // End class
