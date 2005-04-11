
package ch.ethz.jadabs.amonem.ui.views;


import java.io.File;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.ethz.jadabs.amonem.deploy.AmonemDeploySkeleton;
import ch.ethz.jadabs.amonem.manager.DAGBundle;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.manager.RepositorySkeleton;
import ch.ethz.jadabs.amonem.ui.explorer.Explorer;
import ch.ethz.jadabs.amonem.ui.perspective.AmonemUI;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;

public class EditPeerView extends ViewPart{
	
		// repository
	private static Vector repository;
	
		// jar lists
	private static Vector defaultJars;
	private static List listLeft;
	private static List listRight;
	
		// current peer
	private static DAGPeer peer;
	
		// directories
	private static Text textJarLocation;
	private static Text textMavenRepoLoc;
	private static Text textLocal;
	private static Text textXargs;
	private static Text textTemp;
	
		// platform
	private static String platformLocation;
	private static Vector platforms = new Vector();
	private static Combo platformCombo;
	
	
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
		
		Controller.setEditPeerView(this);
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setBounds(0,0,600,600);
		
		final Composite composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setBounds(0,0,scrolledComposite.getBounds().height,scrolledComposite.getBounds().width);
		
		scrolledComposite.setContent(composite);
		
		final Label labelJarsPath = new Label(composite, SWT.NONE);
		labelJarsPath.setBounds(25, 25, 145, 15);
		labelJarsPath.setText("Repository:");
		
		textJarLocation = new Text(composite, SWT.BORDER);
		textJarLocation.setBounds(185, 20, 300, 25);
		textJarLocation.setText(Controller.loadRepositoryLocation());
		
		final Button browseJar = new Button(composite, SWT.NONE);
		browseJar.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Explorer.browse(textJarLocation,Explorer.FILE);
			}
		});
		browseJar.setBounds(500, 20, 100, 25);
		browseJar.setText("browse");
		
		final Label labelMavenRepoLoc = new Label(composite, SWT.NONE);
		labelMavenRepoLoc.setBounds(25, 60, 155, 20);
		labelMavenRepoLoc.setText("Location of 'maven repository':");

		textMavenRepoLoc = new Text(composite, SWT.BORDER);
		textMavenRepoLoc.setBounds(185, 55, 300, 25);
		textMavenRepoLoc.setText(Controller.loadPlatformLocation());
		
		final Button browseMaven = new Button(composite, SWT.NONE);
		browseMaven.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Explorer.browse(textMavenRepoLoc,Explorer.DIRECTORY);
			}
		});
		browseMaven.setBounds(500, 55, 100, 25);
		browseMaven.setText("browse");
				
		final Label labelLocal = new Label(composite, SWT.NONE);
		labelLocal.setBounds(25, 95, 100, 25);
		labelLocal.setText("Local deploy path:");

		textLocal = new Text(composite, SWT.BORDER);
		textLocal.setBounds(185, 90, 300, 25);
		textLocal.setText(Controller.loadLocal());
		
		final Button browseLocal = new Button(composite, SWT.NONE);
		browseLocal.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Explorer.browse(textLocal,Explorer.DIRECTORY);
			}
		});
		browseLocal.setBounds(500, 90, 100, 25);
		browseLocal.setText("browse");

		final Label labelXargs = new Label(composite, SWT.NONE);
		labelXargs.setBounds(25, 130, 100, 25);
		labelXargs.setText("xargs template:");

		textXargs = new Text(composite, SWT.BORDER);
		textXargs.setBounds(185, 125, 300, 25);
		textXargs.setText(Controller.loadXarg());
		
		final Button browseXargs = new Button(composite, SWT.NONE);
		browseXargs.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Explorer.browse(textXargs,Explorer.FILE);
			}
		});
		browseXargs.setBounds(500, 125, 100, 25);
		browseXargs.setText("browse");

		final Label labedTemp = new Label(composite, SWT.NONE);
		labedTemp.setBounds(25, 165, 100, 25);
		labedTemp.setText("Temp:");

		textTemp = new Text(composite, SWT.BORDER);
		textTemp.setBounds(185, 160, 300, 25);
		textTemp.setText(Controller.loadTemp());

		final Button browseTemp = new Button(composite, SWT.NONE);
		browseTemp.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Explorer.browse(textTemp,Explorer.DIRECTORY);
			}
		});
		browseTemp.setBounds(500, 160, 100, 25);
		browseTemp.setText("browse");
		
		
		final Label labelName = new Label(composite, SWT.NONE);
		labelName.setBounds(25, 255, 45, 20);
		labelName.setText("Name:");

		final Label textName = new Label(composite, SWT.BORDER);
		textName.setBounds(130, 250, 160, 25);
		if(peer == null){
			Controller.activateErrorView("no peer");
			return;
		}
		textName.setText(peer.getName());
		
		final Label labelJars = new Label(composite, SWT.NONE);
		labelJars.setBounds(25, 330, 35, 20);
		labelJars.setText("jars:");
		
		final Label labelAllJars = new Label(composite, SWT.CENTER);
		labelAllJars.setBounds(105, 330, 100, 15);
		labelAllJars.setText("all jars");

		final Label labelJarsForNewPeer = new Label(composite, SWT.CENTER);
		labelJarsForNewPeer.setBounds(340, 330, 100, 15);
		labelJarsForNewPeer.setText("installed bundles");

		listLeft = new List(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		listLeft.setBounds(80, 355, 175, 160);

		listRight = new List(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		listRight.setBounds(305, 355, 175, 160);
		
		final Button button = new Button(composite, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				try{
					if(listLeft.getSelectionCount() > 0){
						int count = listLeft.getSelectionCount();
						for(int i=0;i<count;i++){
							listRight.add(listLeft.getSelection()[0]);
							listLeft.remove(listLeft.getSelection()[0]);
						}
					}
//					if(listLeft.getItemCount()>0){
//						listRight.add(listLeft.getSelection()[0]);
//						listLeft.remove(listLeft.getSelection()[0]);
//					}
				}catch(Exception ex){
				}
			}
		});
		button.setBounds(265, 400, 30, 25);
		button.setText("->");

		final Button button_1 = new Button(composite, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				try{
					if(listRight.getSelectionCount() > 0){
						int count = listRight.getSelectionCount();
						for(int i=0;i<count;i++){
							listLeft.add(listRight.getSelection()[0]);
							listRight.remove(listRight.getSelection()[0]);
						}
					}
//					if((listRight.getItemCount()>0) && (listRight.getSelection()[0]!=null)){
//						listLeft.add(listRight.getSelection()[0]);
//						listRight.remove(listRight.getSelection()[0]);
//					}
				}catch(Exception ex){
				}
			}
		});
		button_1.setBounds(265, 435, 30, 25);
		button_1.setText("<-");

		
		final Button button_4 = new Button(composite, SWT.NONE);
		button_4.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if(listRight.getSelectionCount()>0){
					String selectedItem = listRight.getSelection()[0];
					int selectedIndex = listRight.getSelectionIndices()[0];
					if(selectedIndex > 0){
						String tempItem = listRight.getItem(selectedIndex-1);
						listRight.setItem(selectedIndex-1,selectedItem);
						listRight.setItem(selectedIndex,tempItem);
						listRight.select(selectedIndex-1);
					}
				}
			}
		});
		button_4.setBounds(490, 400, 35, 25);
		button_4.setText("up");

		final Button button_5 = new Button(composite, SWT.NONE);
		button_5.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if(listRight.getSelectionCount()>0){
					String selectedItem = listRight.getSelection()[0];
					int selectedIndex = listRight.getSelectionIndices()[0];
					if(selectedIndex < listRight.getItemCount()-1){
						String tempItem = listRight.getItem(selectedIndex+1);
						listRight.setItem(selectedIndex+1,selectedItem);
						listRight.setItem(selectedIndex,tempItem);
						listRight.select(selectedIndex+1);
					}
				}
			}
		});
		button_5.setBounds(490, 435, 35, 25);
		button_5.setText("down");
		
		fillRight();
		
		final Label labelPlatform = new Label(composite, SWT.NONE);
		labelPlatform.setBounds(25, 295, 55, 20);
		labelPlatform.setText("Platform:");
		
		final Combo platformCombo = new Combo(composite, SWT.NONE);
		platformCombo.setBounds(130, 290, 225, 20);
		
		getPlatforms();
		selectPeerPlatform(platformCombo,listRight);	
		
		final Button getRepositoryButton = new Button(composite, SWT.NONE);
		getRepositoryButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
					// check directories 
				if(textJarLocation.getText().equals("") 
						|| textXargs.getText().equals("")
						|| textMavenRepoLoc.getText().equals("")
						|| textTemp.getText().equals("")
						|| textLocal.getText().equals("")){
					Controller.activateErrorView("not all infos available !");
					return;
				}

					// check directories
				checkLocation(textTemp);
				checkLocation(textLocal);

				AmonemUI.amonemManager.setFolders(textTemp.getText(),textLocal.getText(),textXargs.getText(),textTemp.getText());
				repository = AmonemUI.amonemManager.getRepository(textJarLocation.getText());

				fillRight();
				
				getPlatforms();
				
				selectPeerPlatform(platformCombo,listRight);
								
				fillLeft();
									
				Controller.storeRepositoryLocation(textJarLocation.getText());
				Controller.storeXarg(textXargs.getText());
				Controller.storeLocal(textLocal.getText());
				Controller.storeTemp(textTemp.getText());
					
			}
		});
		getRepositoryButton.setBounds(300, 200, 100, 25);
		getRepositoryButton.setText("apply");
			
		final Button cancelButton = new Button(composite, SWT.NONE);
		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Controller.closeExplorer();
				Controller.hideEditPeerView();
			}
		});
		cancelButton.setBounds(460, 555, 100, 25);
		cancelButton.setText("cancel");
	
		final Button deleteButton = new Button(composite, SWT.NONE);
		deleteButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				AmonemUI.amonemManager.deletePeer(peer.getName());
				Controller.closeExplorer();
				Controller.hideEditPeerView();
			}
		});
		deleteButton.setBounds(130, 555, 100, 25);
		deleteButton.setText("delete");

		if(Controller.getDiscoveryRoot().getElement(peer.getName()) != null){
			final Button killButton = new Button(composite, SWT.NONE);
			killButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					AmonemUI.amonemManager.kill(peer.getName());
					Controller.closeExplorer();
					Controller.hideEditPeerView();
				}
			});
			killButton.setBounds(240, 555, 100, 25);
			killButton.setText("kill");
		}
		
		if(Controller.getDeployRoot().getElement(peer.getName())!=null && Controller.getDiscoveryRoot().getElement(peer.getName())==null){
			final Button deployButton = new Button(composite, SWT.NONE);
			deployButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					AmonemDeploySkeleton skeleton;
					
					save();
					
					AmonemUI.amonemManager.setFolders(textTemp.getText(),textLocal.getText(),textXargs.getText(),textTemp.getText());
					repository = AmonemUI.amonemManager.getRepository(textJarLocation.getText());

					platformLocation = textMavenRepoLoc.getText()+File.separator+"osgi"+File.separator+"jars";
					
					if(listRight.getItemCount() <= 0 
							|| platformCombo.getSelectionIndex()<0
							|| textName.getText().length()<=0){
						Controller.activateErrorView("need all infos !");
						return;
					}
					skeleton = AmonemUI.amonemManager.getSkeleton(textName.getText(),textTemp.getText(),textLocal.getText(),textXargs.getText());
					skeleton.setJavaPath(System.getProperty("java.home") + "/bin/java");
					System.out.println(System.getProperty("java.home") + "/bin/java");
					
					
					String platform = platformCombo.getItem(platformCombo.getSelectionIndex());
					int dot_index = platform.substring(0,platform.length()-1).lastIndexOf(".");//.jar
					int index = platform.substring(0,dot_index-1).lastIndexOf(".");
					while(!platform.substring(index,index+1).equals("-") && index > 0){
						index--;
					}
					String uuid = "osgi:"+platform.substring(0,index)+":"+platform.substring(index+1,dot_index)+":";
					System.out.println("file:///"+platformLocation+"/"+platform);
					System.out.println(uuid);
					
					skeleton.setPlatform(uuid,"file:///"+platformLocation+"/"+platform);						
					for(int i=0;i<listRight.getItemCount();i++){
						String jar = (String)listRight.getItem(i);
						RepositorySkeleton temp;
						for(int j=0;j<repository.size();j++){
							if(((RepositorySkeleton)repository.get(j)).getJar().equals(jar)){
								skeleton.addJar(((RepositorySkeleton)repository.get(j)).getUuid(),((RepositorySkeleton)repository.get(j)).getUpdatelocation());
							}
						}
					}
										
					
					AmonemUI.amonemManager.newPeer(skeleton);
					Controller.closeExplorer();
					Controller.hideEditPeerView();
				}
			});
			deployButton.setBounds(350, 555, 100, 25);
			deployButton.setText("deploy");
		}
		
		//if(Controller.getDeployRoot().getElement(peer.getName())!=null && Controller.getDiscoveryRoot().getElement(peer.getName())==null){
			final Button saveButton = new Button(composite, SWT.NONE);
			saveButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					save();
					PeerListView.bundleUpdateInDeploy(peer);
					Controller.closeExplorer();
					Controller.hideEditPeerView();
				}
			});
			saveButton.setBounds(20, 555, 100, 25);
			saveButton.setText("save");
		//}
		
				
	}


	public void setFocus() {

		
		
	}
	
	/**
     * 
     * This method is called to set the peer which will be modified in 
     * the EditPeerView.
     * 
     * 
     * @param peer This peer will be set
     * @return no return value
     */
	public static void setPeer(DAGPeer peer){
		EditPeerView.peer = peer;
	}
	
	/**
     * 
     * This method is called to get the actual peer which will be modified in 
     * the EditPeerView.
     * 
     * 
     * @param none No parameters needed
     * @return returns the actual peer
     */
	public static DAGPeer getPeer(){
		return peer;
	}
	
//	---------------------------------------------------------------------------
//	--------------------- Platform --------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method is called to get all platforms from disc.
     * The method checks 'mavenRepoLoc'/osgi/jars for platforms and fills
     * a vector (paltrofms)
     * 
     * 
     * @param none No parameters needed
     * @return returns true if 'search' was succesfull, otherwise false
     */
	private static boolean getPlatforms(){
		
		String platformLocation = textMavenRepoLoc.getText()+"/osgi/jars";
		
		platforms = new Vector();
		
		File f = new File(platformLocation);
		
		if(!f.exists())return false;

		if(f.isFile()){
			System.out.println("choose a directory");
		}
		else if(f.isDirectory()){
			File[] files = f.listFiles();
			for(int i=0;i<files.length;i++){
				platforms.add(files[i].getName());
			}
		}
		
		if(platforms.size()>0){
			return true;
		}
		return false;
		
	}
	
	/**
     * 
     * If platform of actual peer is in listRight, this method deletes the platform
     * from listRight and adds it in platformCombo
     * 
     * 
     * @param platformCombo This Combo contains available platforms
     * @param listRight Coontains available platforms
     * @return no return value
     */
	private static void selectPeerPlatform(Combo platformCombo, List listRight){
		
		String selectedPlatform = "";
		
		platformCombo.removeAll();

		for(int i=0;i<listRight.getItemCount();i++){
			if(platforms.contains((String)listRight.getItem(i).concat(".jar"))){
				selectedPlatform = (String)listRight.getItem(i).concat(".jar");
				platformCombo.add(listRight.getItem(i).concat(".jar"));
				platformCombo.select(platformCombo.indexOf(listRight.getItem(i).concat(".jar")));
				listRight.remove(i);
			}
		}
				
		addAditionalPlatforms(selectedPlatform,platformCombo);
		
	}
	
	/**
     * 
     * Fill platformCombo with 'new' platforms
     * 
     * 
     * @param selectedPlatform Platform of actual peer
     * @param platformCombo This combo is filled with available platforms
     * @return no return value
     */
	private static void addAditionalPlatforms(String selectedPlatform,Combo platformCombo){
		
		for(int i=0;i<platforms.size();i++){
			if(!((String)platforms.get(i)).equals(selectedPlatform)){
				platformCombo.add((String)platforms.get(i));
			}
		}
	}
	
//	---------------------------------------------------------------------------
//	--------------------- Location --------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method checks a directory. It guarantees that each path ends
     * with a file seperator.
     * 
     * 
     * @param text Path
     * @return no return value
     */
	private static void checkLocation(Text text){
		
		char lastChar = text.getText().charAt(text.getText().length()-1);
		
		if(lastChar != File.separatorChar){
			text.setText(text.getText()+File.separatorChar);
		}
	}
	
//	---------------------------------------------------------------------------
//	--------------------- properties ------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method fills the right list. 
     * The right list contains all installed bundles
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	private void fillRight(){
				
		listRight.removeAll();
		
		Vector bundles = peer.getBundles();
		
		for(int i=0;i<bundles.size();i++){
			listRight.add(((DAGBundle)bundles.get(i)).getName());
		}
			
	}
	
	/**
     * 
     * This method fills the left list. 
     * The left list contains all 'unused' bundles
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	private static void fillLeft(){
		
		String repoString;
		Vector right = new Vector();
		
		for(int i=0;i<listRight.getItemCount();i++){
			right.add((String)listRight.getItem(i));
		}
		
		
		for(int i=0;i<repository.size();i++){
			
			repoString = (String)((RepositorySkeleton)repository.get(i)).getJar();
			
			if(right.contains(repoString)){
				peer.getBundle(repoString).setUUID(((RepositorySkeleton)repository.get(i)).getUuid());
				peer.getBundle(repoString).setUpdateLocation(((RepositorySkeleton)repository.get(i)).getUpdatelocation());
			}else{
				if(listLeft.indexOf(repoString) < 0){
					listLeft.add(repoString);
				}
			}
			
		}
		
	}


	
//	---------------------------------------------------------------------------
//	--------------------- save ------------------------------------------------
//	---------------------------------------------------------------------------

	/**
     * 
     * Call this method to save the changes made on actual peer.
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	private static void save(){
		
		// bundles
		Vector peerBundles = peer.getBundles();
		Vector listBundles = new Vector();
		DAGBundle bundle;
				
		for(int i=0;i<listRight.getItemCount();i++){

			if(peer.getBundle(listRight.getItem(i)) == null){				
				
				for(int j=0;j<repository.size();j++){
					
					if(((RepositorySkeleton)repository.get(j)).getJar().equals(listRight.getItem(i))){
						
						bundle = new DAGBundle();
						
						bundle.setName(listRight.getItem(i));
						bundle.setUUID(((RepositorySkeleton)repository.get(j)).getUuid());
						bundle.setUpdateLocation(((RepositorySkeleton)repository.get(j)).getUpdatelocation());
						peer.setBundle(bundle);
						
					}
				}
				
			}
			
		}

		for(int i=0;i<listLeft.getItemCount();i++){
			peer.removeBundle(listLeft.getItem(i));		
		}
	
	}
	
}