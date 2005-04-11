/*
 * Created on 04.12.2004
 *
 */
package ch.ethz.jadabs.amonem.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author andfrei
 *
 */
public class JadabsView extends ViewPart{

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(final Composite parent) {
		
		Composite composite = new JadabsManagerComposite(parent,SWT.BORDER);
				
	
//		final Button button = new Button(composite, SWT.BORDER);
//		button.addMouseListener(new MouseAdapter() {
//			public void mouseDown(MouseEvent e) {
//				
//				
//	 			
//			}
//		});
//		button.setBounds(5, 5, 10, 20);
//		button.setText("Start");
//	
//		Label nameLabel = new Label(composite, SWT.NONE);
//		nameLabel.setBounds(25, 20, 60, 25);
//		nameLabel.setText("Name:");
//		
//		Label nameFieldLabel = new Label(composite, SWT.NONE);
//		nameFieldLabel.setBounds(50, 50, 60, 25);
	
	}

	
	
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
}
