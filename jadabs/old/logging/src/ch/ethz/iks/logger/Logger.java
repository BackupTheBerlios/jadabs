/*
 * Created on Jan 16, 2004
 *
 */
package ch.ethz.iks.logger;

/**
 * @author andfrei
 *
 */
public class Logger implements ILogger
{

    private static Logger defaultLogger;
    private static ILogger realLogger;

    private static Logger Instance(){
        
        if (defaultLogger == null){
            defaultLogger = new Logger();
            
            if (realLogger == null)
                realLogger = new ConsoleLogger(ConsoleLogger.LEVEL_ERROR);   
        }
            
        return defaultLogger;
            
    }

    //---------------------------------------------
	// DefaultLogger methods
	//---------------------------------------------
    
    /**
     * Return a Logger, per default use a singleton. Could also be changed to use 
     * log4j.
     * 
     * @param name following the package.class format
     */
    public static ILogger getLogger(String name)
    {
        return Instance();
    }

    public static ILogger getLogger(Class clazz)
    {
        return Instance();
    }
    
    public void setLogger(ILogger logger){
        realLogger = logger;
    }

    //---------------------------------------------
	// Logger dispatcher methods 
	//---------------------------------------------
    
	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#debug(java.lang.String)
	 */
	public void debug(String message)
	{
		realLogger.debug(message);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#debug(java.lang.String, java.lang.Throwable)
	 */
	public void debug(String message, Throwable throwable)
	{
        realLogger.debug(message, throwable);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#isDebugEnabled()
	 */
	public boolean isDebugEnabled()
	{
		return realLogger.isDebugEnabled();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#info(java.lang.String)
	 */
	public void info(String message)
	{
        realLogger.info(message);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#info(java.lang.String, java.lang.Throwable)
	 */
	public void info(String message, Throwable throwable)
	{
        realLogger.info(message, throwable);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#isInfoEnabled()
	 */
	public boolean isInfoEnabled()
	{
		return realLogger.isInfoEnabled();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#warn(java.lang.String)
	 */
	public void warn(String message)
	{
        realLogger.warn(message);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#warn(java.lang.String, java.lang.Throwable)
	 */
	public void warn(String message, Throwable throwable)
	{
        realLogger.warn(message, throwable);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#isWarnEnabled()
	 */
	public boolean isWarnEnabled()
	{
		return realLogger.isWarnEnabled();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#error(java.lang.String)
	 */
	public void error(String message)
	{
        realLogger.error(message);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#error(java.lang.String, java.lang.Throwable)
	 */
	public void error(String message, Throwable throwable)
	{
        realLogger.error(message, throwable);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#isErrorEnabled()
	 */
	public boolean isErrorEnabled()
	{
		return realLogger.isErrorEnabled();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#fatalError(java.lang.String)
	 */
	public void fatalError(String message)
	{
        realLogger.fatalError(message);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
	 */
	public void fatalError(String message, Throwable throwable)
	{
        realLogger.fatalError(message, throwable);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#isFatalErrorEnabled()
	 */
	public boolean isFatalErrorEnabled()
	{
		return realLogger.isFatalErrorEnabled();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.logger.Logger#getChildLogger(java.lang.String)
	 */
	public ILogger getChildLogger(String name)
	{
		return realLogger.getChildLogger(name);
	}

}
