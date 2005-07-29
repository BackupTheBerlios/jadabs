/*
 * Created on 27.07.2005
 *
 */
package ch.ethz.jadabs.http;


public interface HttpSite
{

    /**
     * Register a PageHandler for a given index, for example bundles.
     * PageHandler is called when the index is triggered.
     * 
     * 
     * @param index
     * @param pageHandler
     */
    void registerPageHandler(String index, PageHandler pageHandler);
    
    /**
     * Unregister a registered PageHandler.
     * 
     * @param pageHandler
     */
    void unregisterPageHandler(String index, PageHandler pageHandler);
}
