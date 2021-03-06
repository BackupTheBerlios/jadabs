/*
 * Created on Feb 22, 2005
 *
 */
package ch.ethz.jadabs.pluginLoader.test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.pluginLoader.api.PluginLoader;


/**
 * @author andfrei
 * 
 */
public class TestPluginLoader implements BundleActivator
{

    PluginLoader pluginLoader;
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {
        ServiceReference sref = bc.getServiceReference(PluginLoader.class.getName());
        pluginLoader = (PluginLoader)bc.getService(sref);
        
        System.out.println("TestPluginLoader started...");
        
        testFilter();
        
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception
    {
        
    }

    
    private void testFilter()
    {
        
        Vector filters = new Vector();
        
//        filters.add( "Extension/id:PeerNetwork ¦ " +
//        		"Platform/id:mservices.wlab.ethz.ch, " +
//        			"name:mservices, version:0.1.0, provider-name:ETHZ-IKS; " +
//        			"Property/name:processor, value:armv4l; " +
//        		"Property/name:os, value:i386; " +
//        		"Property/name:display, value:no; " +
//        		"Property/name:vm, value:cdc/fp; " +
//        		"Property/name:vm-version, value:1.0.1; " +
//        		"OSGiContainer/id:osgi; "+
//        		"OSGiContainer/id:core-osgi-daop; " +
//        		"NetIface/type:wlan/managed, connection:static, " +
//        			"configuration:internet, name:mservices, " +
//        			"essid:wlan, mode:managed, iface:eth0, " +
//        			"ip:192.168.55.10; " +
//        		"NetIface/type:internet, " +
//        			"ext-type:wlan/managed ¦ R");
//        
//        filters.add( " ¦ ¦ R");
//        
//        filters.add(" ¦ Platform/id:mservices.wlab.ethz.ch, " +
//        			"name:mservices, version:0.1.0, provider-name:ETHZ-IKS; " +
//        			"Property/name:processor, value:armv4l; " +
//        		"Property/name:os, value:i386; " +
//        		"Property/name:display, value:no; " +
//        		"Property/name:vm, value:cdc/fp; " +
//        		"Property/name:vm-version, value:1.0.1; " +
//        		"OSGiContainer/id:osgi; "+
//        		"OSGiContainer/id:core-osgi-daop; " +
//        		"NetIface/type:wlan/managed, connection:static, " +
//        			"configuration:internet, name:mservices, " +
//        			"essid:wlan, mode:managed, iface:eth0, " +
//        			"ip:192.168.55.10; " +
//        		"NetIface/type:internet, " +
//        			"ext-type:wlan/managed ¦ R");
//        
//           
//        filters.add(" ¦ Property/name:vm, value:cdc/fp; " +
//                "Property/name:os, value:i386; " +
//                "Property/name:vm-version, value:1.3; " +
//                "OSGiContainer/id:osgi; "+
//                "OSGiContainer/id:core-osgi-daop " +
//    			" ¦ PROV");
        
//        filters.add(" ¦ Platform/id:nokia6600.wlab.ethz.ch, " +
//    			"name:nokia6600, version:0.1.0, provider-name:ETHZ-IKS; " +
//    		"Property/name:processor, value:arm9; " +
//    		"Property/name:os, value:linux; " +
//		    "Property/name:display, value:176x208; " +
//		    "Property/name:vm, value:cldc/midp; " +
//		    "Property/name:vm-version, value:1.0.1; " +
//		    "OSGiContainer/id:j2me-osgi; " +
//		    "NetIface/type:bt-jsr82, connection:dynamic, " +
//		        "name:bt-hotspot; " +
//		    "NetIface/type:gsm, connection:dynamic, " +
//		        "name:GSM ¦ OPD, INS");
        
//        filters.add(" ¦"+  
//                "Platform/id:nokia6600.wlab.ethz.ch, " +
//            	"name:nokia6600, version:0.1.0, provider-name:ETHZ-IKS; " +
//            "Property/name:processor, value:arm9; " +
//            "Property/name:os, value:linux; " +
//            "Property/name:display, value:176x208; " +
//            "Property/name:vm, value:cldc/midp; " +
//            "Property/name:vm-version, value:1.0.1; " +
//            "OSGiContainer/id:j2me-osgi; " +
//            "NetIface/type:bt-jsr82, connection:dynamic, " +
//                "name:bt-hotspot; " +
//            "NetIface/type:gsm, connection:dynamic, " +
//                "name:GSM ¦ OPD,PRO");
        
//        filters.add(" ¦"+  
//			"Platform/id:ppc.wlab.ethz.ch, " +
//			"name:ppc1, version:0.1.0, provider-name:ETHZ-IKS; " +
//			"Property/name:processor, value:arm9; " +
//			"Property/name:os, value:ppc; " +
//			"Property/name:display, value:176x208; " +
//			"Property/name:vm, value:dotnetcf; " +
//			"Property/name:vm-version, value:1.1; " +
//			"OSGiContainer/id:dotnet-osgi; " +
//			"NetIface/type:internet ¦ OPD,PRO");
        
        
        // test IM filters
//        filters.add( " Extension/id:IM | " +
//                "Platform/id:device.ethz.ch, name:deviceservices, " +
//                    "version:0.1.0, provider-name:ETHZ-IKS; " +
//                "Property/processor:i386; " +
//                "Property/os:linux; " +
//                "Property/display:no; " +
//                "Property/vm:cdc/fp; " +
//                "Property/vm-version:1.0.1; " +
//                "OSGiContainer/id:osgi; " +
//                "OSGiContainer/id:osgi-daop; " +
//                "NetIface/type:adhoc, connection:dynamic, " +
//                    "name:bt-hotspot,iface:eth1, ip:192.168.55.55; " +
//                "NetIface/type:internet, ext-type:wlan/managed | R");
        
        filters.add(" Extension/id:sip | Platform/id:device.ethz.ch, name:deviceservices, version:0.1.0, provider-name:ETHZ-IKS; Property/processor:i386; Property/os:linux; Property/display:no; Property/vm:cdc/fp; Property/vm-version:1.0.1; OSGiContainer/id:osgi; OSGiContainer/id:osgi-daop; NetIface/type:adhoc, connection:dynamic, name:bt-hotspot, iface:eth1, ip:192.168.55.55; NetIface/type:internet, ext-type:wlan/managed | R");
        for (Enumeration en = filters.elements(); en.hasMoreElements();)
            queryAndPrintResult((String)en.nextElement());

        
    }
    
    
    private void queryAndPrintResult(String filter)
    {
        System.out.println();
        System.out.println("========================================");
        System.out.println("query for: "+ filter+"\n");
        
        Iterator it;
        try
        {
            it = pluginLoader.getMatchingPlugins(filter,null);
            
            
            for(;it.hasNext();)
                System.out.println("uuid: "+(String)it.next());
            
            
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("========================================");
        System.out.println();
    }
}
