package ch.ethz.iks.evolution.cop;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.IResourceFactory;

/**
 * Factory to create UpgradeableComponentResource objects 
 * Provides searching in the repository for a class.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class UpgradeableComponentResourceFactory implements IResourceFactory {

	private static HashMap resIndex = new HashMap(100);
	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IResourceFactory#createResource(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public IComponentResource createResource(String urnid, String codebase, String classname) {
		return new UpgradeableComponentResource(urnid, codebase, classname, -1);
	}
	
	public IComponentResource createResource(String urnid, String codebase, String classname, int version) {
			return new UpgradeableComponentResource(urnid, codebase, classname, version);
	}


	/**
	 * Given a classname, returns the component this class is defined in.
	 * Just searches inside UpgradeableComponentResource objects registered in the repository.
	 * @param classInComponent
	 * @return
	 */
	public static IComponentResource getComponentResourceByContent(String classInComponent) {
		
		/*if (classInComponent.startsWith("java.")) {
			return null; // to speed up
		}*/
		Object res = resIndex.get(classInComponent);
	    if (res instanceof Boolean) {
	    	return null; // java library class
	    } else if (res != null) {
			return (IComponentResource)res;
		}
		// not in cache, searching all cops
		Collection values = (Collection) ComponentRepository.Instance().getComponentResources().values();
		Iterator cops = values.iterator();
		
		while (cops.hasNext()) {
			IComponentResource iCop = (IComponentResource) cops.next();
			try {
				UpgradeableComponentResource copRes = (UpgradeableComponentResource) iCop;
				if (copRes.contains(classInComponent)) {
					resIndex.put(classInComponent, copRes);
					return copRes;
				}
			} catch (ClassCastException cce) {
				continue;/*
				if (iCop instanceof ProxyComponentResource) return null;
				else if (iCop instanceof MigrComponentResource) return null;
				else if (iCop instanceof ComponentResource) {
					if (resIndex.containsValue(iCop)) {
						// cop has been already indexed, search next cop
						continue;
					}
					// not upgradable cop, // manually search jar contents
					Vector classes = null;
					try {
						classes = UpgradeableComponentLoader.getJarContents(iCop.getCodeBase());
						//System.out.println(iCop.getCodeBase()+"\n\n contains "+classes.toString());
					} catch (IOException e) {
						System.out.println("ERROR on caching jar contents of "+iCop.getCodeBase()+": "+e);
					}
					Iterator iter = classes.iterator(); 
					while(iter.hasNext()) {
						// cache all once
						Object c = iter.next();
						
						resIndex.put(c, iCop);
					}
					res = resIndex.get(classInComponent);
					if (res instanceof ComponentResource) {
						// searching for a class just being cached
						return (IComponentResource)res;
					} // else continue to next cop
				}*/
			}
		}
		// class does not belong to any component => may be a runtime class
		resIndex.put(classInComponent, Boolean.TRUE);
		
		return null;
	}
	
	public static void clearCache() {
		resIndex.clear();
	}


	/*public IComponentContext createContext(IComponentResource copRes) {
		return new IComponentContext() {

			public Object getComponent(String mainname) {
				IComponentResource copres = ComponentRepository.Instance().getComponentResourceByClassname(mainname);
				IProxy p4mainobj = (IProxy) copres.getExtObject();
				LOG.info("get proxy of main "+p4mainobj.dump()));
			}

			public Object getProperty(String name) {
				return Jadabs.Instance().getProperty(name);
			}
			
		};
	}*/

	


}
