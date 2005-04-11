
package ch.ethz.jadabs.amonem.ui.perspective;


import ch.ethz.jadabs.amonem.PeerListener;
import ch.ethz.jadabs.amonem.manager.DAGPipe;
import ch.ethz.jadabs.amonem.ui.views.PeerListView;
import ch.ethz.jadabs.servicemanager.ServiceReference;


public class PeerListenerImpl implements PeerListener{

	/**
     * 
     * This method is called by the manager after changes on a pipe or if a new pipe is added.
     * 
     * @param dagpipe This pipe caused the event
     * @return no return value
     */
	public void pipeAdded(DAGPipe dagpipe) {

		Controller.update();
		
	}

	/**
     * 
     * This method is called by the manager after a pipe deletion
     * 
     * @param dagpipe This pipe caused the event
     * @return no return value
     */
	public void pipeRemoved(DAGPipe dagpipe) {

		Controller.update();
		
	}

    /*
     */
    public void serviceReferenceAdded(ServiceReference sref)
    {
        PeerListView.serviceReferenceAdded(sref);
    }

}
