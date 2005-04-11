
package ch.ethz.jadabs.amonem.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import ch.ethz.jadabs.amonem.ui.perspective.Controller;


public class ErrorView extends ViewPart{

		// SWT elements
	private static ScrolledComposite scrolledComposite;
	private static Label text;
	
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
		
		Controller.setErrorView(this);
		
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(0,0,600,600);
		
		Composite parentComposite = new Composite(scrolledComposite, SWT.NONE);
		parentComposite.setBounds(0,0,scrolledComposite.getBounds().height,scrolledComposite.getBounds().width);
		
		scrolledComposite.setContent(parentComposite);
				
		text = new Label(parentComposite, SWT.BORDER);
		text.setBounds(10, 10, 500, 250);
				
	}


	/**
     * 
     * This method is called to set the error message which has to be displayed
     * 
     * 
     * @param msg Error message
     * @return no return value
     */
	public static void setErrorText(String msg){
		text.setText(msg);
	}
	
	/**
     * 
     * This method is called to get the last error message.
     * 
     * 
     * @param none No parameters needed
     * @return returns the last error message
     */
	public static String getText(){
		return text.getText();
	}
	
	/**
     * 
     * This method is called to set an error message.
     * 
     * 
     * @param txt Message to set
     * @return returns the last error message
     */
	public static void setText(String txt){
		ErrorView.text.setText(txt);
	}
	
	

	
	public void setFocus() {

		
	}

}
