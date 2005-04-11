
package ch.ethz.jadabs.amonem.ui.perspective;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.explorer.Explorer;
import ch.ethz.jadabs.amonem.ui.starter.AmonemPlugin;
import ch.ethz.jadabs.amonem.ui.views.EditPeerView;
import ch.ethz.jadabs.amonem.ui.views.ErrorView;
import ch.ethz.jadabs.amonem.ui.views.GraphView;
import ch.ethz.jadabs.amonem.ui.views.InstallBundleView;
import ch.ethz.jadabs.amonem.ui.views.PeerListView;


public class Controller {

		// roots
	private static DAGGroup discoveryRoot = null;
	private static DAGGroup deployRoot = null;
	
		// SWT elements
	private static ViewPart newPeerView;
	private static ViewPart editPeerView;
	private static ViewPart propertyView;
	private static ViewPart installBundleView;
	private static ViewPart errorView;
	//private static String errorMas;
	private static IFolderLayout main;
	private static IFolderLayout property;
	
	private static Explorer explorer;
	
		// tags for history file
	private static String repositoryLocation = null;
	private static String repositoryLocationTagA = "<repositoryLocation>";
	private static String repositoryLocationTagB = "</repositoryLocation>";
	private static String platformLocation = null;
	private static String platformLocationTagA = "<platformLocation>";
	private static String platformLocationTagB = "</platformLocation>";
	private static String local = null;
	private static String localTagA = "<local>";
	private static String localTagB = "</local>";
	private static String xarg = null;
	private static String xargTagA = "<xarg>";
	private static String xargTagB = "</xarg>";
	private static String temp = null;
	private static String tempTagA = "<temp>";
	private static String tempTagB = "</temp>";
	
	private static String storedFileName = "directories.amo";
	
	private static File file;
	
	
	public static DAGGroup newGraph(){
		
		DAGGroup root = new DAGGroup("root");
		DAGGroup group1 = new DAGGroup("group1");
		DAGPeer peer10 = new DAGPeer("markus");
		DAGPeer peer11 = new DAGPeer("barbara");
		DAGPeer peer12 = new DAGPeer("andreas");
		DAGPeer peer13 = new DAGPeer("peer1");
		DAGPeer peer14 = new DAGPeer("peer2");
		DAGPeer peer15 = new DAGPeer("peer3");
		DAGPeer peer16 = new DAGPeer("peer4");
		root.addChild(peer13);
		root.addChild(peer14);
		root.addChild(peer15);
		root.addChild(peer16);
		//root.addChild(group1);
		//group1.addChild(peer10);
		//group1.addChild(peer11);
		//group1.addChild(peer12);
		
			
		return root;
	
		//return AmonemManager.amonemManager.getROOT();
	}
	
//	---------------------------------------------------------------------------
//	--------------------- actualRoot methods ----------------------------------
//	---------------------------------------------------------------------------

	/**
     * 
     * Call this method if you want the actual discovery root
     * 
     * @param none No parameters needed
     * @return actual discovery root
     */
	public static DAGGroup getDiscoveryRoot(){
		return AmonemUI.amonemManager.getDiscoveryROOT();
	}
	
	/**
     * 
     * Call this method to set the actual discovery root
     * 
     * @param root New discovery root
     * @return no return value
     */
	public static void setDiscoveryRoot(DAGGroup root){
		Controller.discoveryRoot = root;
	}
	
	/**
     * 
     * Call this method if you want the actual deploy root
     * 
     * @param none No parameters needed
     * @return actual deploy root
     */
	public static DAGGroup getDeployRoot(){
		return AmonemUI.amonemManager.getDeployROOT();
	}
	
	/**
     * 
     * Call this method to set the actual deploy root
     * 
     * @param root New deploy root
     * @return no return value
     */
	public static void setDeployRoot(DAGGroup root){
		Controller.deployRoot = root;
	}

//	---------------------------------------------------------------------------
//	--------------------- View methods ----------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Call this method to save a reference to this view. 
     * Later, this reference is needed to activate/hide the view
     * 
     * @param newPeerView View to save
     * @return no return value
     */
	public static void setNewPeerView(ViewPart newPeerView) {
		Controller.newPeerView = newPeerView;
	}
	
	/**
     * 
     * Call this method to save a reference to this view. 
     * Later, this reference is needed to activate/hide the view
     * 
     * @param editPeerView View to save
     * @return no return value
     */
	public static void setEditPeerView(ViewPart editPeerView) {
		Controller.editPeerView = editPeerView;
	}
	
	/**
     * 
     * Call this method to save a reference to this view. 
     * Later, this reference is needed to activate/hide the view
     * 
     * @param errorView View to save
     * @return no return value
     */
	public static void setErrorView(ViewPart errorView){
		Controller.errorView = errorView;
	}
	
	/**
     * 
     * Call this method to save a reference to this view. 
     * Later, this reference is needed to activate/hide the view
     * 
     * @param propertyView View to save
     * @return no return value
     */
	public static void setPropertyView(ViewPart propertyView){
		Controller.propertyView = propertyView;
	}
	
	/**
     * 
     * Call this method to save a reference to this view. 
     * Later, this reference is needed to activate/hide the view
     * 
     * @param installBundleView View to save
     * @return no return value
     */
	public static void setInstallBundleView(ViewPart installBundleView){
		Controller.installBundleView = installBundleView;
	}
	
	/**
     * 
     * Call this method to hide the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void hideNewPeerView(){
		newPeerView.getViewSite().getPage().hideView(newPeerView);
	}
	
	/**
     * 
     * Call this method to hide the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void hideEditPeerView(){
		editPeerView.getViewSite().getPage().hideView(editPeerView);
	}
	
	/**
     * 
     * Call this method to hide the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void hideErrorView(){
		errorView.getViewSite().getPage().hideView(errorView);
	}
	
	/**
     * 
     * Call this method to hide the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void hideInstallBundleView(){
		installBundleView.getViewSite().getPage().hideView(installBundleView);
	}
	
	/**
     * 
     * Call this method to activate the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void activateNewPeerView(){
		
		if(newPeerView == null){
			try {
				try {
					main.addView("NewPeerViewId");
				} catch (RuntimeException e) {

				}
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("NewPeerViewId");
			} catch (PartInitException e1) {
				
			}
		}
		else {
			try {
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("NewPeerViewId");
			} catch (PartInitException e) {
				System.out.println("Error: can't show NewPeerView");
			}
		}	
	}
	
	/**
     * 
     * Call this method to activate the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void activateEditPeerView(DAGPeer peer){
		
		EditPeerView.setPeer(peer);
		
		if(editPeerView == null){
			try {
				try {
					main.addView("EditPeerViewId");
				} catch (RuntimeException e) {

				}
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("EditPeerViewId");
			} catch (PartInitException e1) {
				
			}
		}
		else {
			try {
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("EditPeerViewId");
			} catch (PartInitException e) {
				System.out.println("Error: can't show EditPeerView");
			}
		}	
	}
	
	/**
     * 
     * Call this method to activate the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void activateErrorView(String msg){
		if(errorView == null){
			try {
				try {
					property.addView("ErrorViewId");
				} catch (RuntimeException e) {

				}
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("ErrorViewId");
			} catch (PartInitException e1) {
				
			}
		}
		else {
			try {
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("ErrorViewId");
			} catch (PartInitException e) {
				System.out.println("Error: can't show ErrorView");
			}
		}	
		
		ErrorView.setErrorText(msg);
	}
	
	/**
     * 
     * Call this method to activate the view
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void activatePropertyView(){
		if(propertyView == null){
			try {
				try {
					property.addView("PropertyViewId");
				} catch (RuntimeException e) {

				}
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("PropertyViewId");
			} catch (PartInitException e1) {
				
			}
		}
		else {
			try {
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("PropertyViewId");
			} catch (PartInitException e) {
				System.out.println("Error: can't show PropertyView");
			}
		}	

	}
	
	/**
     * 
     * Call this method to activate the view
     * 
     * @param peer On this peer the bundle will be installed
     * @return no return value
     */
	public static void activateInstallBundleView(DAGPeer peer){
		
		InstallBundleView.setPeer(peer);
		
		if(installBundleView == null){
			try {
				try {
					main.addView("InstallBundleViewId");
				} catch (RuntimeException e) {

				}
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("InstallBundleViewId");
			} catch (PartInitException e1) {
				
			}
		}
		else {
			try {
				AmonemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getPages()[0].showView("InstallBundleViewId");
			} catch (PartInitException e) {
				System.out.println("Error: can't show InstallBundleViewId");
			}
		}	
	}
	
	/**
     * 
     * Call this method to save a folder.
     * 
     * 
     * @param main Folder to save
     * @return no return value
     */
	public static void addMainFolder(IFolderLayout main){
		Controller.main = main;
	}
	
	/**
     * 
     * Call this method to save a folder.
     * 
     * 
     * @param property Folder to save
     * @return no return value
     */
	public static void addPropertyFolder(IFolderLayout property){
		Controller.property = property;
	}
	
//	---------------------------------------------------------------------------
//	--------------------- explorer methods ------------------------------------
//	---------------------------------------------------------------------------	
	
	/**
     * 
     * Call this method to save the actual explorer.
     * 
     * 
     * @param explorer Explorer to save
     * @return no return value
     */
	public static void setExplorer(Explorer explorer){
		Controller.explorer = explorer;
	}
	
	/**
     * 
     * Call this method to close the actual explorer.
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void closeExplorer(){
		if(explorer != null){
			explorer.close();
		}
	}

//	---------------------------------------------------------------------------
//	--------------------- store methods ---------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Get the file "user.home"/.amonem/directories.amo
     * If the file doesn't exit, the method create a new file
     * 
     * @param none No parameters needed
     * @return no return value
     */
	private static void getFile(){
		try{	
			String homeName = System.getProperty("user.home");
			String amonemName = homeName.concat("/.amonem");
			
			File amonemDir = new File(amonemName);						
			if(!amonemDir.exists()){
				if(!amonemDir.mkdir())return;
			}
			
			File amonemFile = new File(amonemName+Path.SEPARATOR+storedFileName);
			if(!amonemFile.exists()){
				amonemFile.createNewFile();			
			}
			file = amonemFile;
						
		}catch (Exception e1) {

		}
	}
	
	/**
     * 
     * Call this method to store 'str' between tagA and tabB in 
     * the file "user.home"/.amonem/directories.amo
     * 
     * 
     * @param tagA Start tag
     * @param tagB End tag
     * @param str String to store between tagA and tagB
     * @return no return value
     */
	private static void store(String tagA,String tagB,String str){
		
		if(file == null){
			getFile();
		}
		
		if(file.exists()){
			try {    	
				BufferedReader in = new BufferedReader(new FileReader(file));
				String tempStr = "";
				String s;
				while ((s = in.readLine()) != null) {
					tempStr = tempStr.concat(s);
				}
				in.close();
	        
				int a = tempStr.indexOf(tagA);
				int b = tempStr.indexOf(tagB);
				if(a<0){
					tempStr = tempStr.concat(tagA+str+tagB);
				}else{
					String A = tempStr.substring(0,a+tagA.length());
					String B = tempStr.substring(b,tempStr.length());
					tempStr = A+str+B;
				}
	        
				BufferedWriter out = new BufferedWriter(new FileWriter(file));
				out.write(tempStr);
				out.close();
				} catch (IOException e) {
	    	
				}
		}
		
	}
	
	/**
     * 
     * Call this method to load the string between tagA and tabB from 
     * file "user.home"/.amonem/directories.amo
     * 
     * 
     * @param tagA Start tag
     * @param tagB End tag
     * @return returns the string between tagA and tagB
     */
	private static String load(String tagA,String tagB){
		
		String str = "";
		
		if(file == null){
			getFile();
		}
		
		if(file.exists()){
			try {
				BufferedReader in;
				in = new BufferedReader(new FileReader(file));
				String tempStr = "";
				String s;
				while ((s = in.readLine()) != null) {
					tempStr = tempStr.concat(s);
				}
				in.close();		
	        
				int a = tempStr.indexOf(tagA);
				int b = tempStr.indexOf(tagB);
	        
				if(a<0){
					return str;
				}
				str = tempStr.substring(a+tagA.length(),b);
	        
			} catch (Exception e) {

			}
		}
		
		return str;
	}
	
	/**
     * 
     * Call this method to store the repository location.
     * 
     * 
     * @param repositoryLocation Location of the repository
     * @return no return value
     */
	public static void storeRepositoryLocation(String repositoryLocation){
		store(repositoryLocationTagA,repositoryLocationTagB,repositoryLocation);
		Controller.repositoryLocation = repositoryLocation;
	}
	
	/**
     * 
     * Call this method to load the repository location.
     * 
     * 
     * @param none No parameters needed
     * @return returns the repository location
     */
	public static String loadRepositoryLocation(){
		if(repositoryLocation == null){
			repositoryLocation = load(repositoryLocationTagA,repositoryLocationTagB);
		}
		return repositoryLocation;
	}
	
	/**
     * 
     * Call this method to store the platform location.
     * 
     * 
     * @param platformLocation Location of the platform
     * @return no return value
     */
	public static void storePlatformLocation(String platformLocation){
		store(platformLocationTagA,platformLocationTagB,platformLocation);
		Controller.platformLocation = platformLocation;
	}
	
	/**
     * 
     * Call this method to load the platform location.
     * 
     * 
     * @param none No parameters needed
     * @return returns the platform location
     */
	public static String loadPlatformLocation(){
		if(platformLocation == null){
			platformLocation = load(platformLocationTagA,platformLocationTagB);
		}
		return platformLocation;
	}
	
	/**
     * 
     * Call this method to store the local directory location.
     * 
     * 
     * @param local Location of the local directory
     * @return no return value
     */
	public static void storeLocal(String local){
		store(localTagA,localTagB,local);
		Controller.local = local;
	}
	
	/**
     * 
     * Call this method to laod the local directory location.
     * 
     * 
     * @param none No parameters needed
     * @return returns the local directory location
     */
	public static String loadLocal(){
		if(local == null){
			local = load(localTagA,localTagB);
		}
		return local;
	}
	
	/**
     * 
     * Call this method to store the xarg.
     * The xarg string is the full xarg name with full path
     * 
     * @param xarg Xarg
     * @return no return value
     */
	public static void storeXarg(String xarg){
		store(xargTagA,xargTagB,xarg);
		Controller.xarg = xarg;
	}
	
	/**
     * 
     * Call this method to load the xarg.
     * The xarg string is the full xarg name with full path
     * 
     * @param none No parameters needed
     * @return returns the xarg
     */
	public static String loadXarg(){
		if(xarg == null){
			xarg = load(xargTagA,xargTagB);
		}
		return xarg;
	}
	
	/**
     * 
     * Call this method to store the temp location.
     * 
     * 
     * @param temp Location of the temp directory
     * @return no return value
     */
	public static void storeTemp(String temp){
		store(tempTagA,tempTagB,temp);
		Controller.temp = temp;
	}
	
	/**
     * 
     * Call this method to load the temp location.
     * 
     * 
     * @param none No parameters needed
     * @return returns the temp directory
     */
	public static String loadTemp(){
		if(temp == null){
			temp = load(tempTagA,tempTagB);
		}
		return temp;
	}
	
	
//	---------------------------------------------------------------------------
//	--------------------- update ----------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Call this method to update all the graph and the peer lists
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static synchronized  void update(){
		discoveryRoot = getDiscoveryRoot();
		
		if(discoveryRoot == null)
		    return;
		
		PeerListView.updateAll();
		GraphView.update();
		
	}
	
	
}
