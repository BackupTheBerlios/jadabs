package ch.ethz.iks.eventsystem.migr2_v3;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.mgr.IEvolutionManager;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.evolution.state.InvocationBuffer;
import ch.ethz.iks.evolution.step.DefaultUpgradeStrategy;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.TransparentProxyFactory;

/**
 * Custom implementation of behaviour of the eventsystem during upgrade. 
 * This implementation ignores incoming invocations during upgrade.
 * =>  component: adapter2_3_escop/
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class EventsystemUpgradeHandler extends DefaultUpgradeStrategy {

	
	private InvocationBuffer q = new InvocationBuffer();
	private static Logger LOG = Logger.getLogger(EventsystemUpgradeHandler.class);
	private Object newCopMain;

	public EventsystemUpgradeHandler() {
		super();
	}
	
	public EventsystemUpgradeHandler(IEvolutionManager upgradeMgr, IComponent oldESimpl, IComponent newESimpl) throws OnlineUpgradeFailedException {
		this();
		initServiceMigr(upgradeMgr, oldESimpl, newESimpl);
		this.newCopMain = newESimpl;
	}
	
	public EventsystemUpgradeHandler(IEvolutionManager upgradeMgr, AdapterComponentResource migrCop) throws OnlineUpgradeFailedException {
		this();
		initLibraryMigr(upgradeMgr, migrCop);
	}


	public void initServiceMigr(IEvolutionManager upgradeMgr, IComponent oldESimpl, IComponent newESimpl) throws OnlineUpgradeFailedException {
		
		if (oldESimpl == null || newESimpl == null) throw new OnlineUpgradeFailedException(" ES impl versions must not be null");
		if (! (oldESimpl.getClass().getName().equals("ch.ethz.iks.eventsystem.svc.EventServiceImpl"))) throw new OnlineUpgradeFailedException("oldES: Expected an Implementation of IEventService but found "+oldESimpl.getClass().getName());
		if (! (newESimpl.getClass().getName().equals("ch.ethz.iks.eventsystem.svc.EventServiceImpl"))) throw new OnlineUpgradeFailedException("newES: Expected an Implementation of IEventService but found "+newESimpl.getClass().getName());
		this.newCopMain=newESimpl;
		super.initServiceMigr(upgradeMgr, oldESimpl, newESimpl);
	}
	
	
	
	public boolean doLaunchNewVersion() {
		return true;
	}
	
	
	/* implements custom instantiation of new version objects to copy the state 
	*  of the old version component on state transfer
	*/ 
	protected Object createNew(Object oldObj) {
		Constructor con = null;
		Object newObj = super.createNew(oldObj);
		if (newObj != null) return newObj;
		try {	
			if (oldObj instanceof Class) { 
				UpgradeableComponentResource newCop = (UpgradeableComponentResource) UpgradeableComponentResourceFactory.getComponentResourceByContent(this.newCopMain.getClass().getName());
				Class newClass = newCop.loadClass(((Class)oldObj).getName(), false);
				//LOG.info("[LOAD]   new version of class "+newClass+" v"+newCop.getVersion());
			}
	  
	    	Class oldC = oldObj.getClass();
            String cn = oldC.getName();
			LOG.info("********** [ADAPTER]   creating New for oldObj="+oldObj+"...");
            // TODO: map to new type: implemented one-to-one name correspondance
			Object o = mgr.getCurrentVersion();
            ComponentResource newCop = (ComponentResource)o ;
			
			//UpgradeableComponentResource newCop = (UpgradeableComponentResource) UpgradeableComponentResourceFactory.getComponentResourceByContent(cn);

            Class newC = newCop.loadClass(cn, true); 
                   													
			String[] fieldNames;
            if (cn.endsWith("FilterImpl")) {
            	fieldNames = new String[] {"filterevent"};
            	/*Field field = oldC.getDeclaredField("filterevent");
            	field.setAccessible(true);
                initArgs = new Object[] {field.get(oldObj)};
                if (TransparentProxyFactory.belongToSameCop(initArgs[0], oldObj)) {
                	// migrate argument first
                	initArgs[0] = this.transferState(initArgs[0],null,true);
                }
                Class iEvent = field.getType();
                iEvent = newCop.loadClass(iEvent.getName(), true);//load new version class definition
                field.setAccessible(false);	
                params = new Class[] {iEvent};
                */
            } else if (cn.endsWith("InQueue")) {
                fieldNames = new String[] {"m_eventservice"};
                /*initArgs = new Object[] {this.newCopMain};
                params = new Class[] {this.newCopMain.getClass()};
                */
            } else if (cn.endsWith("OutQueue")) {
				fieldNames = new String[] {"m_pnet"};
            } else {
            	/* TODO check if there is a constructor which takes parameters which are assignable from some fields
            	Constructor[] constr = newC.getDeclaredConstructors();
            	for (int i = 0; i < constr.length; i++) {
					Constructor c = constr[i];
					Class[] constrParams = c.getParameterTypes();
					
            		Field[] members = oldC.getClass().getDeclaredFields();
            		for (int i = 0; i < members.length; i++) {
						members[i].getType();
						members[i].setAccessible(true);
						member[i].get(oldObj);
						members[i].setAccessible(false);
					}
            		fieldNames = newString[]{};
            	}*/
            	fieldNames = new String[0];
            }
            Object[] initArgs = new Object[fieldNames.length];
            Class[] params = new Class[fieldNames.length];
            
            for (int k = 0; k < fieldNames.length; k++) {
				Field field = oldC.getDeclaredField(fieldNames[k]);
				field.setAccessible(true);
				initArgs[k] = field.get(oldObj);
				if (TransparentProxyFactory.belongToSameCop(initArgs[k], oldObj)) {
					// migrate argument first
					initArgs[k] = this.transferState(initArgs[k],null,true);
				}
				Class fieldType = field.getType();
				fieldType = newCop.loadClass(fieldType.getName(), true);//load new version class definition
				field.setAccessible(false);	
				params[k] =fieldType;
            }

            con = newC.getDeclaredConstructor(params); 
            con.setAccessible(true);
            newObj = con.newInstance(initArgs);
			LOG.info(" [ADAPTER]   ... created New "+newObj);
			
	    } catch (Exception e) {
	    	LOG.error("[FAILED] to upgrade...",e);
        	throw new OnlineUpgradeFailedException(e);
        }  finally {
			if (con != null) {
				con.setAccessible(false);
			}
			return newObj;
        }  
                
	}
	
	
	public Object [] getLibraryInterface( IComponentResource cop) {
		throw new OnlineUpgradeFailedException("Eventsystem is NOT a Library but a Service component");
	}

	
	public Object invoke(Object proxy, int methodCode, String declaringClass, Object[] args) throws Throwable {
		// ignoring invocations during upgrade
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#onUpgradeStart()
	 */
	public void onUpgradeStart() throws OnlineUpgradeFailedException {
		
	}



	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#updateOriginal(java.lang.Object, java.lang.Object)
	 */
	public void updateOriginal(Object oldHidden, Object newHidden) {
		
	}
	
	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#invoke(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object[])
	 */
	public Object invoke(Object proxy, String methodName, String declaringClass, Object[] args) throws Throwable {
		// TODO NOT YET IMPLEMENTED: +EventSystemUpgradeHandler.invoke
		throw new RuntimeException("NOT YET IMPLEMENTED: +EventSystemUpgradeHandler.invoke");
		//return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// TODO NOT YET IMPLEMENTED: +EventSystemUpgradeHandler.invoke
		throw new RuntimeException("NOT YET IMPLEMENTED: +EventSystemUpgradeHandler.invoke");
		//return null;
	}

	
	
	
	public boolean doMigrate(Field member) {
		String name = member.getName();
		if (name.equals("eventListeners")) {
			LOG.info("********** [ COPY ]   allowed to copy field "+member.getDeclaringClass()+"."+member.getType()+" "+member.getName() );
			return true;
		} else if (name.equals("peerName")) {
		LOG.info("********** [ COPY ]   allowed to copy field "+member.getDeclaringClass()+"."+member.getType()+" "+member.getName() );
		return true;
	}
		
		return false;
	}


}
