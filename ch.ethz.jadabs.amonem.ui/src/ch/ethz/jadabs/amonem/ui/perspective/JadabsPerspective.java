package ch.ethz.jadabs.amonem.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class JadabsPerspective implements IPerspectiveFactory {
	
			// layout
		public void createInitialLayout(IPageLayout layout) {

			 defineLayout(layout);
			
		}
		
		
		public void defineLayout(IPageLayout layout) {
			
	        	// Editors are placed for free
	        String editorArea = layout.getEditorArea();
	        layout.setEditorAreaVisible(false); 

	        	// Place navigator and outline to left of editor area
	        IFolderLayout lists = layout.createFolder("List", IPageLayout.LEFT, (float) 0.26, editorArea);
	        lists.addView("PeersViewId");

	        	// bottom area
	        IFolderLayout property = layout.createFolder("Properties", IPageLayout.BOTTOM, (float) 0.80, editorArea);
	        property.addView("PropertyViewId");
	        property.addPlaceholder("ErrorViewId");
	        Controller.addPropertyFolder(property);
	        
	        	// main area
	        IFolderLayout main = layout.createFolder("Main", IPageLayout.TOP, (float) 0.00, editorArea); 
	        main.addView("GraphViewId"); 
	        main.addPlaceholder("NewPeerViewId");
	        main.addPlaceholder("EditPeerViewId");
	        main.addPlaceholder("InstallBundleViewId");
	        Controller.addMainFolder(main);
	        
		}
				
}
