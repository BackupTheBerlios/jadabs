/*
 * Created on Feb 21, 2004
 *
 */
package ch.ethz.iks.jadabs;

/**
 * @author andfrei
 *
 */
public interface BootstrapConstants
{
	/* Property constants  */
    /* Property defaults   */

	/** Constant to set Peername */
	public static final String PEERNAME = "peername";

	/** Start JADABS with the local loader but load the components only at startup.*/
	public static final String REPOSITORY_LOAD_ONCE = "reploadonce";

	/** Local directory for persistent Component Repository 	 */
	public static final String PCOPREP = "pcoprep";
    public static final String PCOPREP_DEFAULT = "./pcoprep";
    
    /** Set Log level for initial startup of jadabs */
    public static final String LOG_LEVEL = "loglevel";

}