
package ch.ethz.jadabs.amonem.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.explorer.Explorer;
import ch.ethz.jadabs.amonem.ui.perspective.AmonemUI;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;


public class InstallBundleView extends ViewPart{

	
	private static Text textBundle;
		// current peer
	private static DAGPeer peer;

	
//	---------------------------------------------------------------------------
//	--------------------- layout ----------------------------------------------
//	---------------------------------------------------------------------------

	/**
     * 
     * This method is called to create the SWT elements of the view.
     * 
     * 
     * @param parent Base composite of the view
     * @return no return value
     */
	public void createPartControl(Composite parent) {
		
		Controller.setInstallBundleView(this);
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setBounds(0,0,600,600);
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setBounds(0,0,scrolledComposite.getBounds().height,scrolledComposite.getBounds().width);
		
		scrolledComposite.setContent(composite);
		
		final Label labelBundle = new Label(composite, SWT.NONE);
		labelBundle.setBounds(25, 25, 145, 15);
		labelBundle.setText("Bundle:");
		
		textBundle = new Text(composite, SWT.BORDER);
		textBundle.setBounds(185, 20, 300, 25);
		textBundle.setText("bundle location");
		
		final Button browseJar = new Button(composite, SWT.NONE);
		browseJar.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Explorer.browse(textBundle,Explorer.FILE);
			}
		});
		browseJar.setBounds(500, 20, 100, 25);
		browseJar.setText("browse");
		
		final Button closeButton = new Button(composite, SWT.NONE);
		closeButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Controller.closeExplorer();
				Controller.hideInstallBundleView();
			}
		});
		closeButton.setBounds(195, 200, 100, 25);
		closeButton.setText("close");

		final Button getRepositoryButton = new Button(composite, SWT.NONE);
		getRepositoryButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				AmonemUI.amonemManager.installBundle(peer.getName(),textBundle.getText());	
				Controller.closeExplorer();
				Controller.hideInstallBundleView();
			}	
		
		});
		getRepositoryButton.setBounds(300, 200, 100, 25);
		getRepositoryButton.setText("install");
		
}

	/**
     * 
     * This method is called to set the peer on which a bundle should be installed.
     * 
     * 
     * @param peer Peer on which the bundle will be istalled
     * @return no return value
     */
	public static void setPeer(DAGPeer peer){
		InstallBundleView.peer = peer;
	}
	
	
	public void setFocus() {
		
		
	}

}
