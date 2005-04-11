
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
import ch.ethz.jadabs.amonem.manager.RepositorySkeleton;
import ch.ethz.jadabs.amonem.ui.explorer.Explorer;
import ch.ethz.jadabs.amonem.ui.perspective.AmonemUI;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;


public class NewPeerView extends ViewPart{
	
		// platrorm
	private static String platformLocation;
	private static Vector repository;
	private static Vector platforms;
	private static Vector defaultJars;
	
		// bundle lists
	private static List listLeft;
	private static List listRight;
	
		// directory fields
	private static Text textJarLocation;
	private static Text textMavenRepoLoc;
	private static Text textLocal;
	private static Text textXargs;
	private static Text textTemp;
	
	
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
		
		Controller.setNewPeerView(this);
		
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
		
		final Button closeButton = new Button(composite, SWT.NONE);
		closeButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Controller.hideNewPeerView();
			}
		});
		closeButton.setBounds(195, 200, 100, 25);
		closeButton.setText("close");

		final Button getRepositoryButton = new Button(composite, SWT.NONE);
		getRepositoryButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if(textJarLocation.getText().equals("") 
						|| textMavenRepoLoc.getText().equals("") 
						|| textTemp.getText().equals("")
						|| textXargs.getText().equals("")
						|| textLocal.getText().equals("")){
					//System.out.println("not all infos available !");
					Controller.activateErrorView("not all infos available !");
					return;
				}
				
				checkLocation(textMavenRepoLoc);
				checkLocation(textTemp);
				checkLocation(textLocal);
				
				AmonemUI.amonemManager.setFolders(textTemp.getText(),textLocal.getText(),textXargs.getText(),textTemp.getText());
				repository = AmonemUI.amonemManager.getRepository(textJarLocation.getText());
					
				if(!getPlatforms()){
					Controller.activateErrorView("no platforms");
					return;
				}
				
				setJarFields(composite,repository,platforms);
				
				textJarLocation.setEditable(false);
				textMavenRepoLoc.setEditable(false);
				textLocal.setEditable(false);
				textXargs.setEditable(false);
				textTemp.setEditable(false);
					
				Controller.storeRepositoryLocation(textJarLocation.getText());
				Controller.storePlatformLocation(textMavenRepoLoc.getText());
				Controller.storeLocal(textLocal.getText());
				Controller.storeXarg(textXargs.getText());
				Controller.storeTemp(textTemp.getText());
				
					
			}
		});
		getRepositoryButton.setBounds(300, 200, 100, 25);
		getRepositoryButton.setText("get");
		
		
				
	}


	public void setFocus() {

		
		
	}
	
//	---------------------------------------------------------------------------
//	--------------------- Platform --------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method is called to get all platforms from disc.
     * The method checks 'mavenRepoLoc'/osgi/jars for platforms and
     * fills a vector (platforms)
     * 
     * 
     * @param none no parameters needed
     * @return returns true if 'search' was succesfull, otherwise false
     */
	private static boolean getPlatforms(){
		
		platformLocation = textMavenRepoLoc.getText()+"/osgi/jars";
		
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
     * This method fills the rightList with default bundles.
     * 
     * 
     * @param repository Repository with bundles
     * @return no return value
     */
	private void fillRight(Vector repository){
		
		defaultJars = new Vector();
		defaultJars.add("swt/jars/swt-3.0RC1-linux-osgi.jar");
		defaultJars.add("xpp3/jars/xpp3-1.1.3.3_min-osgi.jar");
		defaultJars.add("xstream/jars/xstream-1.0.1-osgi.jar");
		defaultJars.add("log4j/jars/log4j-1.2.8-osgi.jar");
		defaultJars.add("jadabs/jars/concurrent-0.7.1.jar");
		defaultJars.add("jadabs/jars/jxme-osgi-0.7.1.jar");
		defaultJars.add("jadabs/jars/jxme-udp-0.7.1.jar");
		defaultJars.add("jadabs/jars/jxme-services-api-0.7.1.jar");
		defaultJars.add("jadabs/jars/jxme-services-impl-0.7.1.jar");
		defaultJars.add("jadabs/jars/remotefw-api-0.7.1.jar");
		defaultJars.add("jadabs/jars/remotefw-impl-0.7.1.jar");
		defaultJars.add("jadabs/jars/jadabs-maingui-0.7.1.jar");
		
		listRight.removeAll();
		
		for(int i=0;i<repository.size();i++){
			if(defaultJars.contains(((RepositorySkeleton)repository.get(i)).getJar())){
				listRight.add((String)defaultJars.get(i));
			}
		}
			
	}

	/**
     * 
     * This method fills the lists (leftList,rightList) with bundles.
     * leftList contains all unused and available bundles.
     * rightList contains all default bundles.
     * 
     * 
     * @param repository Repository with all available bundles
     * @return no return value
     */
	private void fillLists(Vector repository){
	
		fillRight(repository);
		
		String[] rightStrings = listRight.getItems();
		Vector right = new Vector();
		for(int i=0;i<rightStrings.length;i++){
			right.add(rightStrings[i]);
		}
		
		listLeft.removeAll();
		
		
		for(int i=0;i<repository.size();i++){
			if(!right.contains((String) ((RepositorySkeleton)repository.get(i)).getJar())){
				listLeft.add((String) ((RepositorySkeleton)repository.get(i)).getJar());
			}
		}
		

	}
	
	/**
     * 
     * This method sets new SWT elements after a click on "get repository.xml"
     * 
     * 
     * @param composite Draw new SWT elements on this composite
     * @param repository
     * @param platform All available platforms
     * @return no return value
     */
	private void setJarFields(Composite composite, final Vector repository, Vector platforms){
		
		final Label labelName = new Label(composite, SWT.NONE);
		labelName.setBounds(25, 255, 45, 20);
		labelName.setText("Name:");

		final Text textName = new Text(composite, SWT.BORDER);
		textName.setBounds(130, 250, 160, 25);
		
		final Label labelPlatform = new Label(composite, SWT.NONE);
		labelPlatform.setBounds(25, 295, 55, 20);
		labelPlatform.setText("Platform:");
		
		final Combo platformCombo = new Combo(composite, SWT.NONE);
		platformCombo.setBounds(130, 290, 225, 20);
		for(int i=0;i<platforms.size();i++){
			platformCombo.add((String)platforms.get(i));
		}
		if(platformCombo.getItemCount()>0){
			platformCombo.select(0);
		}
		
		final Label labelJars = new Label(composite, SWT.NONE);
		labelJars.setBounds(25, 330, 35, 20);
		labelJars.setText("jars:");
		
		final Label labelAllJars = new Label(composite, SWT.CENTER);
		labelAllJars.setBounds(105, 330, 100, 15);
		labelAllJars.setText("all jars");

		final Label labelJarsForNewPeer = new Label(composite, SWT.CENTER);
		labelJarsForNewPeer.setBounds(340, 330, 100, 15);
		labelJarsForNewPeer.setText("jars for new peer");

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
		
		try{
			fillLists(repository);
		}catch(Exception e){
			
		}
	
		final Button okButton = new Button(composite, SWT.NONE);
		okButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				AmonemDeploySkeleton skeleton;
				if(listRight.getItemCount() <= 0 
						|| platformCombo.getSelectionIndex()<0
						|| textName.getText().length()<=0){
					Controller.activateErrorView("need all infos !");
					return;
				}
				skeleton = AmonemUI.amonemManager.getSkeleton(textName.getText(),textTemp.getText(),textLocal.getText(),textXargs.getText());
				skeleton.setJavaPath(System.getProperty("java.home") + "/bin/java");
				//System.out.println(System.getProperty("java.home") + "/bin/java");
				
				
				String platform = platformCombo.getItem(platformCombo.getSelectionIndex());
				int dot_index = platform.substring(0,platform.length()-1).lastIndexOf(".");//.jar
				int index = platform.substring(0,dot_index-1).lastIndexOf(".");
				//System.out.println(platform.substring(index,index+1));
				while(!platform.substring(index,index+1).equals("-") && index > 0){
					index--;
				}
				String uuid = "osgi:"+platform.substring(0,index)+":"+platform.substring(index+1,dot_index)+":";
				//String uuid = "osgi:"+platform.substring(0,platform.indexOf(".jar"))+"::";
				//System.out.println("file:///"+platformLocation+"/"+platform);
				//System.out.println(uuid);
				
				skeleton.setPlatform(uuid,"file:///"+platformLocation+"/"+platform);						
				for(int i=0;i<listRight.getItemCount();i++){
					String jar = (String)listRight.getItem(i);
					RepositorySkeleton temp;
					for(int j=0;j<repository.size();j++){
						if(((RepositorySkeleton)repository.get(j)).getJar().equals(jar)){
							skeleton.addJar(((RepositorySkeleton)repository.get(j)).getUuid(),((RepositorySkeleton)repository.get(j)).getUpdatelocation());
//							System.out.println("UUID: " + ((RepositorySkeleton)repository.get(j)).getUuid() + ", Upd.loc: " + ((RepositorySkeleton)repository.get(j)).getUpdatelocation());
						}
					}
				}
									
				
				AmonemUI.amonemManager.newPeer(skeleton);
				
				Controller.closeExplorer();
				Controller.hideNewPeerView();
			}
		});
		okButton.setBounds(455, 535, 100, 25);
		okButton.setText("ok");

		final Button cancelButton = new Button(composite, SWT.NONE);
		cancelButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				Controller.closeExplorer();
				Controller.hideNewPeerView();
			}
		});
		cancelButton.setBounds(350, 535, 100, 25);
		cancelButton.setText("cancel");

		
	}

}
