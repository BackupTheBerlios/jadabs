/*
 * Created on 17.02.2004, ETH Zurich
 * 
 */
package ch.ethz.iks.jxme.bluetooth;

/**
 * @author Daniel Kaeppeli, jdan[at]kaeppe.li
 *
 */
public interface IConnectionFilter {

	public boolean acceptsConnection(String uri);
	
}
