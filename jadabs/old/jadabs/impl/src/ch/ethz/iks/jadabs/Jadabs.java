/*
 * Created on May 2, 2003
 *
 * $Id: Jadabs.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import java.util.Hashtable;

//import org.apache.log4j.PropertyConfigurator;

import ch.ethz.iks.logger.ConsoleLogger;
import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * DefaultBootStrap is the main class which can be used to start the Jadabs
 * infrastructure.
 * When started a core bootstrapping system is initialized which allows
 * to insert new components at runtime. This Bootstrap infrastructure is
 * in its core independent from Prose (dAOP). By just inserting components
 * at runtime and stopping the component by hand, dAOP doesn't have to
 * be used.  
 * <br>
 * To enable components to be replaced at runtime with a new version or
 * connection components at runtime, dAOP is needed. Prose, a dAOP 
 * extended jvm allows to crosscut running applications. Therefore
 * Jadabs has to be started with the Prose enabled jvm.
 * 
 * 
 * @author andfrei
 */
public class Jadabs implements BootstrapConstants  {

    /** Initialize the Logger with the ERROR level */
    private static Logger LOG = (Logger)Logger.getLogger(Jadabs.class);
//  {
//      PropertyConfigurator.configure("./log4j.properties");
//  }

	
	/** BootStrap as singleton */
	private static Jadabs jadabs;
	
	/* Property constants  */
	private ComponentManager copmgr;
	
	private String resourcesDir = "./bin/ext";
	
	/** Properties contains the startup arguments, use the consttants to access the values
	 * of th properties.
	 */
	protected Hashtable properties = new Hashtable();
	
	/* an object to lock the default bootstrap service from beeing finished
	 * and therefore canceling the container.
	 */
//	private Object lockobj = new Object();
	private boolean running = true;				// the bootstrap is running when started
	
	// eventid
	private int id = 0;

	private String[] cmdlineArgs;
	
	/**
	 * DefaultBootStrap is a singleton, since there will be only one Extension System.
	 *
	 */
	public Jadabs(){

		jadabs = this;
	}
	
	/**
	 * DefaultBootStrap is a singleton, if it has not been initialized it will create a
	 * default BootStrap without any parameters. Is still has the possibility to 
	 * instantiate extensions.
	 * 
	 * @return
	 */
	public static Jadabs Instance(){
		
		if (jadabs == null){
			jadabs = new Jadabs();
		
			jadabs.init(null);
		}
		
		return jadabs;
	}
	
	/**
	 * DefaultBootstrap can be extended, but be sure to call init and
	 * start from the superclass.
	 * 
	 * A possible scenario to extend this class could be to insert
	 * extensions already on the start.
	 * 
	 * @param args like the command line arguments
	 */
	public static void main(String[] args) {
		
		jadabs = new Jadabs();
		
		jadabs.init(args);
		jadabs.start();
	}

	
	/**
	 * DefaultBootstrap can be extended, but be sure to initialize the superclass also.
	 * 
	 * @param args like the command line arguments
	 */
	public void init(String[] args)
    {	
        // parse args: exit on help, set logger
        parseArgs2Property(args);
     
	}

    private void parseArgs2Property(String[] args)
    {
        this.cmdlineArgs = args;
        int i = 0;
        while( args != null && i < args.length && args[i].startsWith("-")){
                
            String arg = args[i++].toLowerCase();
                
            if (arg.equals("-name")){
                if (i < args.length){
                    properties.put(PEERNAME, args[i++]);
                }
            }
            else if (arg.equals("-pcoprep")){
                if (i < args.length){
                    properties.put(PCOPREP, args[i++]);
                }
            }
            else if (arg.equals("-rlo")){
                properties.put(REPOSITORY_LOAD_ONCE, "true");
            }
            else if (arg.equals("-log")){
                String loglevel = args[i++];
                int level;
                
                if (loglevel.toUpperCase().equals("DEBUG")){
                    properties.put(LOG_LEVEL, new Integer(ILogger.LEVEL_DEBUG));
                    level = ILogger.LEVEL_DEBUG;
                }
                else if (loglevel.toUpperCase().equals("INFO")){
                    properties.put(LOG_LEVEL, new Integer(ILogger.LEVEL_INFO));
                    level = ILogger.LEVEL_INFO;
                }
                else if (loglevel.toUpperCase().equals("WARN")){
                    properties.put(LOG_LEVEL, new Integer(ILogger.LEVEL_WARN));
                    level = ILogger.LEVEL_WARN;
                }
                else if (loglevel.toUpperCase().equals("FATAL")){
                    properties.put(LOG_LEVEL, new Integer(ILogger.LEVEL_FATAL));
                    level = ILogger.LEVEL_FATAL;   
                }
                else if (loglevel.toUpperCase().equals("DISABLED")){
                    properties.put(LOG_LEVEL, new Integer(ILogger.LEVEL_DISABLED));
                    level = ILogger.LEVEL_DISABLED;   
                }
                else{
                    properties.put(LOG_LEVEL, new Integer(ILogger.LEVEL_ERROR));
                    level = ILogger.LEVEL_ERROR;   
                }
                 
                LOG.setLogger(new ConsoleLogger(level));
                
                LOG.info("set level to:"+loglevel);
            }
            else if (arg.equals("-help")){
                StringBuffer sb = new StringBuffer();

                sb.append("jadabs [-help] [-prose] [-name <peername>] [-pcoprep <dir>] [-rlo]\n");
                sb.append(" -help:      to print this message\n");
                sb.append(" -prose:     if you are using components with PROSE (dAOP) aspects\n");
                sb.append(" -name:      specify a Peername if you want to give your container an alias or\n" +
                                "           to run different containers on the same machine\n");
                sb.append(" -pcoprep:   speify the repository where new jar files can be uploaded\n");
                sb.append(" -rlo:       Repository-Load-Once starts the repository checker only at startup (once)\n");
                sb.append(" -log:     Log everything in jadabs with one of following levels: \n" +
                        "                 DEBUG, INFO, WARN, ERROR, FATAL, DISABLED, \n"+
                        "                  default is ERROR if not specified\n");
                
                System.out.println(sb.toString());
                
                System.exit(0);
            }
        }    
    }
    
	/**
	 * Return the properties, it should at leat contain the name of the peer, PEERNAME.
	 * 
	 * @return
	 */
	public Hashtable getProperties(){
		return properties;
	}

	/**
	 * Returns a specific property.
	 * 
	 * @param key
	 * @return
	 */
	public Object getProperty(String key){
		return properties.get(key);
	}

    /**
     * Start Jadabs with a LocalLoader.
     * 
     */
	public void start()
    {
		copmgr = ComponentManager.Instance();
        boolean started = copmgr.startLocalComponentLoader();
		
        if (started)
             System.out.println("Jadabs, has been started ...");
        else
            System.out.println("Jadabs, exited!");
	}
	
	/**
	 * stops the DefaultBootstrap Container, stops all component services and
	 * releases the resources.
	 *
	 */
	public void stop(){

		copmgr.stop();

		running = false;
//		lockobj.notify();
		
		System.out.println("Jadabs, DefaultBootStrap has been stopped!");
	}
	
	// =================== helper methods ====================
//	
//	public static void traceClassLoader(Class clas){
//		
//		ClassLoader cl = clas.getClassLoader();
//		
//		while(cl != null){
//			
//			LOG.info(cl.toString());
//			cl = cl.getParent();			
//		}
//		
//	}

	public static void remoteWait(){
		
		try {
			while (true)
				Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String[] getCommandlineArguments() {
		return cmdlineArgs;
	}

}
