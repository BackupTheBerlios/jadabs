/*
 * Created on Dec 3, 2003
 *
 */
package ch.ethz.iks.jxme.bluetooth;

import java.io.IOException;
import java.util.Hashtable;

import nanoxml.XMLParseException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.iks.jxme.IPeerNetwork;
import ch.ethz.iks.jxme.bluetooth.impl.BTPeerNetwork;
import ch.ethz.iks.jxme.configurator.PAConfigurator;

/**
 * @author daniel
 * @author andfrei
 *  
 */
public class BluetoothService implements BundleActivator {

	private static BluetoothService thisService = null;
	private static Logger LOG = Logger.getLogger(BluetoothService.class.getName());

	private BTPeerNetwork _peer = null;
	private boolean _isRendezVousServer = false;
	private int _timeOut = -1;

	public BluetoothService() {

	}

	public void init(BundleContext context) {

		if (LOG.isInfoEnabled()) {
			LOG.info("\t=====> initialize Bluetooth component");
		}

		try {
			PAConfigurator config = new PAConfigurator((String) context
					.getProperty(IPeerNetwork.JXME_PEERNAME));
			Hashtable params = config.getParameters("bluetooth");

			if (LOG.isDebugEnabled()) {
				LOG.debug("PeerType: " + (String) params.get("peerType"));
			}

			if (((String) params.get("peerType"))
					.equalsIgnoreCase("rendez-vous")) {
				_isRendezVousServer = true;

				if (LOG.isDebugEnabled()) {
					LOG.debug("start rendez-vous peer");
				}

			} else {
				_isRendezVousServer = false;
				if (LOG.isDebugEnabled()) {
					LOG.debug("start regular peer");
				}
			}

			if (params.get("timeout") != null) {
				String timeout = (String) params.get("timeout");
				_timeOut = Integer.parseInt(timeout);
			}

		} catch (XMLParseException e) {
			LOG.fatal("Can't parse the configuration file!", e);
		} catch (IOException e) {
			LOG.fatal("Can't read the configuration file", e);
		}

	}

	public boolean isRendezVousPeer() {
		return _peer.isRendezVousServer();
	}

	public int numberOfConnections() {
		return _peer.numberOfConnections();
	}

	/*
	 */
	public void start() throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("\t=====>start Bluetooth component");
		}

		if (_timeOut > 0 && _isRendezVousServer) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("start BTPeerNetwork(" + _isRendezVousServer + ", "
						+ _timeOut + ")");
			}
			_peer = new BTPeerNetwork(_isRendezVousServer, _timeOut);

			//_peer.connect();
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("start BTPeerNetwork(" + _isRendezVousServer + ")");
			}
			_peer = new BTPeerNetwork(_isRendezVousServer);
			try {
				_peer.connect();
			} catch (IOException e) {
				LOG.error("There is no other Bluetooth device available.", e);
			}
		}
		_peer.create(null, null, null);

	}

	public void start(BundleContext context) throws Exception {
		init(context);
		start();
	}

	/*
	 */
	public void stop(BundleContext context) throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("\t=====> close Bluetooth component");
		}
		// call close method to activate aspect
		_peer.close(null, null, null);
		_peer.closeAllConnections();
		_peer.stop();

	}

}