/*
 * Created on Nov 14, 2004
 *
 */
package gov.nist.sip.proxy;


/**
 * @author andfrei
 * 
 */
public class ProxyAdmin
{

    public boolean PROXY_STARTED = false;
    
    public Object syncstart = new Object();
    
    public String configfile;
    
    public Proxy proxy;
    
    public ProxyAdmin() throws Exception
    {
        
    }
    
    public boolean isProxyStarted()
    {
        return PROXY_STARTED;
    }
    
    public Proxy startProxy(String configfile) throws Exception
    {
        if (configfile != null)
            this.configfile = configfile;
        
        proxy = new Proxy(this.configfile);
        
        synchronized(syncstart)
        {
            proxy.start();
            PROXY_STARTED = true;
            syncstart.notify();
        }
        
        return proxy;
    }
    
    public void stopProxy() throws Exception
    {
        if (PROXY_STARTED)
        {
            proxy.stop();
            PROXY_STARTED = false;
        }
    }
    
}
