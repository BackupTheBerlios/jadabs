package ch.ethz.iks.evolution.mgr;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.LocalComponentLoader;
import ch.ethz.iks.proxy.cop.ProxyComponentResource;

/**
 * A Local component loader that supports runtime evolution of components loaded.
 * On insertion of a newer version of a runnning component, it initiates an runtime component upgrade.
 * It replaces the LocalComponentLoader in use at startup of the evolution component.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class LocalCopScanner extends LocalComponentLoader {

	private static Logger LOG = Logger.getLogger(LocalCopScanner.class);

	public LocalCopScanner() {
		super();
	}

	public LocalCopScanner(String pcoprepdir) {
		super(pcoprepdir);
	}

	/**
	 * initiates a runtime component evolution from oldCopres to newCopres
	 */
	protected void replace(IComponentResource oldCopres, IComponentResource newCopres) {
		if (oldCopres instanceof ProxyComponentResource) {
			oldCopres = ((ProxyComponentResource)oldCopres).getOriginalComponent();
		}
		try {
			
			IEvolutionManager darwin = EvolutionManager.getManager((UpgradeableComponentResource)oldCopres, (UpgradeableComponentResource)newCopres);
			if ( !darwin.isUpgrading()) {
				LOG.info("Starting online component evolution of "+oldCopres.getCodeBase()+" from version "+oldCopres.getVersion()+" to "+newCopres.getVersion());
				darwin.startRuntimeUpgrade();
			}
		} catch (Throwable t) {
			LOG.error(" component does not support online upgrades "+oldCopres.getCodeBase());
			super.replace(oldCopres, newCopres); // offline upgrade (withdraw old, insert new)
		}
			
	}
	
	public boolean startLoader(int timeinterval) {
		// overridden just for output
		LOG.info("Starting new Loader...");
		return super.startLoader(timeinterval);
	}

}
