package ch.ethz.jadabs.amonem.ui.views;


import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import ch.ethz.jadabs.amonem.manager.DAGBundle;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGIterator;
import ch.ethz.jadabs.amonem.manager.DAGMember;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.explorer.Explorer;
import ch.ethz.jadabs.amonem.ui.perspective.AmonemUI;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;
import ch.ethz.jadabs.servicemanager.ServiceReference;



public class PeerListView extends ViewPart {
	
		// SWT elements
	private static Composite parent;	
	private static TabFolder tabFolder;
	private static TabItem peerTabItem;
	private static TabItem groupTabItem;
	private static Tree peerTreeDi;
	private static Tree peerTreeDe;
	private static TreeItem WorldGroupItem;
	private static TreeItem peerItem;
	private static Menu menu;
	
		// strings in tree
	private static String groupsString = "World Group";
	private static String peersString = "Peers";
	private static String pipeString = "pipe";
	private static String propertyString = "property";
	private static String bundleString = "bundles";
	private static String bundleChildString = "bundleChild";
	private static String serviceString = "services";
	private static String serviceChildString = "servicesChild";
	
	private static boolean init = false;


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

		PeerListView.parent = parent;
		
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 1;
		composite.setLayout(gridLayout);
		
			// toolbar
		final ToolBar toolBar = new ToolBar(composite, SWT.FLAT);
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

			// load
		final ToolItem loadItem = new ToolItem(toolBar, SWT.PUSH);
		loadItem.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/open.gif"));
		loadItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Explorer.load();				
			}
		});
		loadItem.setText("Load");

			// save
		final ToolItem saveItem = new ToolItem(toolBar, SWT.PUSH);
		saveItem.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/save.gif"));
		saveItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Explorer.saveAll();
			}
		});
		saveItem.setText("Save");

			// new peer
		final ToolItem newPeerItem = new ToolItem(toolBar, SWT.PUSH);
		newPeerItem.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/add.gif"));
		newPeerItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Controller.activateNewPeerView();
			}
		});
		newPeerItem.setText("New Peer");
		
			// refresh
		final ToolItem refreshItem = new ToolItem(toolBar, SWT.PUSH);
		refreshItem.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/refresh.gif"));
		refreshItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Controller.update();
			}
		});
		refreshItem.setText("Refresh");
		
			// test
		//final ToolItem test = new ToolItem(toolBar, SWT.PUSH);
		//test.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/refresh.gif"));
		//test.addSelectionListener(new SelectionAdapter() {
		//	public void widgetSelected(SelectionEvent e) {
		//		PropertyView.update();
		//	}
		//});
		//test.setText("test");
		
		
			// tabs
		final TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

			// tab discovery
		final TabItem diItem = new TabItem(tabFolder, SWT.NONE);
		diItem.setText("Discovery");

			// tab deploy
		final TabItem deItem = new TabItem(tabFolder, SWT.NONE);
		deItem.setText("Deploy");

			// tag composite for discovery
		final Composite compositeDi = new Composite(tabFolder,SWT.NONE);
		final GridLayout gridLayoutDi = new GridLayout();
		gridLayoutDi.horizontalSpacing = 1;
		compositeDi.setLayout(gridLayoutDi);
		diItem.setControl(compositeDi);
		
		final ToolBar tiniToolBarDi = new ToolBar(compositeDi, SWT.NONE);

//		final ToolItem toolItem = new ToolItem(tiniToolBarDi, SWT.PUSH);
//		toolItem.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/open.gif"));
//		toolItem.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {				
//			}
//		});
//		toolItem.setText("New item");
//		
//		final ToolItem toolItem2 = new ToolItem(tiniToolBarDi, SWT.PUSH);
//		toolItem2.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/open.gif"));
//		toolItem2.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {				
//			}
//		});
//		toolItem2.setText("New item");
		
			// tag composite for deploy
		final Composite compositeDe = new Composite(tabFolder,SWT.NONE);
		final GridLayout gridLayoutDe = new GridLayout();
		gridLayoutDe.horizontalSpacing = 1;
		compositeDe.setLayout(gridLayoutDe);
		deItem.setControl(compositeDe);
		
		final ToolBar tiniToolBarDe = new ToolBar(compositeDe, SWT.NONE);

//		final ToolItem toolItem3 = new ToolItem(tiniToolBarDe, SWT.PUSH);
//		toolItem3.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/open.gif"));
//		toolItem3.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {				
//			}
//		});
//		toolItem3.setText("New item");
//		
//		final ToolItem toolItem4 = new ToolItem(tiniToolBarDe, SWT.PUSH);
//		toolItem4.setImage(SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/open.gif"));
//		toolItem4.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {				
//			}
//		});
//		toolItem4.setText("New item");
		
			// discovery tree
		peerTreeDi = new Tree(compositeDi, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		peerTreeDi.setLayoutData(new GridData(GridData.FILL_BOTH));
		peerTreeDi.addMouseListener(new MouseAdapter() {
		    
			public void mouseDown(MouseEvent e) 
			{
				Controller.activatePropertyView();
				TreeItem[] items = ((Tree)e.getSource()).getSelection();
				
				if(items.length > 0){ // item selected
				    
					final TreeItem selectedItem = items[0]; // selected item
					
						//	 button left
					if(e.button == 1)
					{  
							// "Peers" or "Groups" selected
						if(selectedItem.getText().equals(groupsString) || selectedItem.getText().equals(peersString))
						{
							// nothing
						}
							// peer selected
						else if(selectedItem.getData() instanceof DAGPeer)
						{ // peerItem selected (in Peers or Groups)
							GraphView.highlitePeer((DAGPeer) selectedItem.getData());					
						}
							// property selected
						else if(inPeers(selectedItem) && (selectedItem.getParentItem().getData() instanceof DAGPeer))
						{ // is property
							DAGPeer selectedPeer = (DAGPeer)selectedItem.getParentItem().getData();
							PropertyView.drawProperties(selectedItem.getText(),selectedPeer);
						}
							// bundle selected
						else if(selectedItem.getData().equals(bundleChildString))
						{ // in bundle
							DAGPeer selectedPeer = (DAGPeer)selectedItem.getParentItem().getParentItem().getData();
							PropertyView.drawProperties(selectedItem.getText(),selectedPeer);
						}
							// service selected
						else if(selectedItem.getData().equals(serviceChildString))
						{
						    DAGPeer selectedPeer = (DAGPeer)selectedItem.getParentItem().getParentItem().getData();
							PropertyView.showServiceAdvertisement(selectedItem.getText(),selectedPeer);
						}
					}else{// button right
						menu = new Menu(peerTreeDi);
							// peer selected
						if(selectedItem.getData() instanceof DAGPeer){
								// install bundle
							MenuItem installBundleItem = new MenuItem(menu, SWT.CASCADE);
							installBundleItem.setText("install bundle");
							installBundleItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									Controller.activateInstallBundleView((DAGPeer)selectedItem.getData());
								}
								public void widgetDefaultSelected(SelectionEvent e) {		
									Controller.activateInstallBundleView((DAGPeer)selectedItem.getData());
								}
							});
							// bundle string selected
						}else if(selectedItem.getText().equals(bundleString)){
								// update
							MenuItem updateItem = new MenuItem(menu, SWT.CASCADE);
							updateItem.setText("update");
							updateItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									((DAGPeer)selectedItem.getParentItem().getData()).getFramework().getBundles();
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									((DAGPeer)selectedItem.getParentItem().getData()).getFramework().getBundles();
								}
							});
						}
							// bundle selected
						else if(selectedItem.getData().equals(bundleChildString)){
								// start bundle
							PropertyView.drawProperties(selectedItem.getText(),(DAGPeer)selectedItem.getParentItem().getParentItem().getData());
							MenuItem startItem = new MenuItem(menu, SWT.CASCADE);
							startItem.setText("start bundle");
							startItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.startBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									AmonemUI.amonemManager.startBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
							});
								// stop bundle
							MenuItem stopItem = new MenuItem(menu, SWT.CASCADE);
							stopItem.setText("stop bundle");
							stopItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.stopBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									AmonemUI.amonemManager.stopBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
							});
								// remove bundle
							MenuItem removeItem = new MenuItem(menu, SWT.CASCADE);
							removeItem.setText("remove bundle");
							removeItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.removeBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									AmonemUI.amonemManager.removeBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
							});
						}
						// services selected
						else if(selectedItem.getData().equals(serviceString)){
								// start bundle
//							PropertyView.drawProperties(selectedItem.getText(),(DAGPeer)selectedItem.getParentItem().getParentItem().getData());
							MenuItem startItem = new MenuItem(menu, SWT.CASCADE);
							startItem.setText("get Services");
							startItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.getServiceAdvertisements(selectedItem.getParentItem().getText(),null);
								}
								public void widgetDefaultSelected(SelectionEvent e) {
								    AmonemUI.amonemManager.getServiceAdvertisements(selectedItem.getParentItem().getText(),null);
								}
							});

						}		
						
						peerTreeDi.setMenu(menu);	
					}
				}
			}
		});
		
			 // deploy tree
		peerTreeDe = new Tree(compositeDe, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		peerTreeDe.setBounds(15, 55, 195, 410);
		peerTreeDe.setLayoutData(new GridData(GridData.FILL_BOTH));
		peerTreeDe.addMouseListener(new MouseAdapter() {
		    
			public void mouseDown(MouseEvent e) 
			{
				Controller.activatePropertyView();
				TreeItem[] items = ((Tree)e.getSource()).getSelection();
				
				if(items.length > 0)
				{ // item selected
				    
					final TreeItem selectedItem = items[0];
					
					if(e.button == 1) // button left
					{  
							// "Peers" or "Groups" selected
						if(selectedItem.getText().equals(groupsString) || selectedItem.getText().equals(peersString)){
							// nothing
						}
							// peer selected
						else if(selectedItem.getData() instanceof DAGPeer)
						{ // peerItem selected (in Peers or Groups)
							DAGPeer p = (DAGPeer)Controller.getDiscoveryRoot().getElement(((DAGPeer)selectedItem.getData()).getName());
							if(p instanceof DAGPeer)
							{
							    GraphView.highlitePeer(p);	
							}
						}
							// property selected
						else if(selectedItem.getParentItem().getData() instanceof DAGPeer)
						{ // is property
							DAGPeer selectedPeer = (DAGPeer)selectedItem.getParentItem().getData();
							PropertyView.drawProperties(selectedItem.getText(),selectedPeer);
						}
							// bundle selected
						else if(selectedItem.getData().equals(bundleChildString))
						{ // in bundle
							DAGPeer selectedPeer = (DAGPeer)selectedItem.getParentItem().getParentItem().getData();
							PropertyView.drawProperties(selectedItem.getText(),selectedPeer);
						}
							
					} else {// button right
						menu = new Menu(peerTreeDe);
							// peer selected
						if(selectedItem.getData() instanceof DAGPeer){
								// edit
							MenuItem editItem = new MenuItem(menu, SWT.CASCADE);
							editItem.setText("edit");
							editItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									Controller.activateEditPeerView((DAGPeer)selectedItem.getData());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									Controller.activateEditPeerView((DAGPeer)selectedItem.getData());
								}
							});
								// kill
							MenuItem killItem = new MenuItem(menu, SWT.CASCADE);
							killItem.setText("kill");
							killItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.kill(selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {		
									AmonemUI.amonemManager.kill(selectedItem.getText());
								}
							});
								// delete
							MenuItem deleteItem = new MenuItem(menu, SWT.CASCADE);
							deleteItem.setText("delete");
							deleteItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.deletePeer(selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {		
									AmonemUI.amonemManager.deletePeer(selectedItem.getText());
									}
							});
								// save
							MenuItem saveItem = new MenuItem(menu, SWT.CASCADE);
							saveItem.setText("save");
							saveItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									Explorer.savePeer(selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {		
									Explorer.savePeer(selectedItem.getText());
								}
							});
							// bundle string selected
						} else if(selectedItem.getText().equals(bundleString)){
								// update
							MenuItem updateItem = new MenuItem(menu, SWT.CASCADE);
							updateItem.setText("update");
							updateItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									((DAGPeer)selectedItem.getParentItem().getData()).getFramework().getBundles();
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									((DAGPeer)selectedItem.getParentItem().getData()).getFramework().getBundles();
								}
							});
						}
							// bundle selected
						else if(selectedItem.getData().equals(bundleChildString)){
								// start bundle
							PropertyView.drawProperties(selectedItem.getText(),(DAGPeer)selectedItem.getParentItem().getParentItem().getData());
							MenuItem startItem = new MenuItem(menu, SWT.CASCADE);
							startItem.setText("start bundle");
							startItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.startBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									AmonemUI.amonemManager.startBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
							});
								// stop bundle
							MenuItem stopItem = new MenuItem(menu, SWT.CASCADE);
							stopItem.setText("stop bundle");
							stopItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.stopBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									AmonemUI.amonemManager.stopBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
							});
								// remove bundle
							MenuItem removeItem = new MenuItem(menu, SWT.CASCADE);
							removeItem.setText("remove bundle");
							removeItem.addSelectionListener(new SelectionListener() {
								public void widgetSelected(SelectionEvent e) {
									AmonemUI.amonemManager.removeBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
								public void widgetDefaultSelected(SelectionEvent e) {
									AmonemUI.amonemManager.removeBundle(selectedItem.getParentItem().getParentItem().getText(),selectedItem.getText());
								}
							});
						}
						
						peerTreeDe.setMenu(menu);	
					}
				}
			}
		});
		
		
		init = true;
		updateAll();
	
	}
	
	
//	---------------------------------------------------------------------------
//	--------------------- tree methods ----------------------------------------
//	---------------------------------------------------------------------------

	/**
     * 
     * This method is called to check if a item is within the "Groups" tree.
     * 
     * 
     * @param item Item to check
     * @return returns true if item is inside the "Groups" tree, otherwise false
     */
	public static boolean inGroups(TreeItem item){
		TreeItem tempItem = item;
		if(tempItem.getText() == WorldGroupItem.getText())return false;// is root	
		do{
			tempItem = tempItem.getParentItem();
			if(tempItem == null)return false;
		}while((tempItem.getText()!=peerItem.getText()) && (tempItem.getText()!=WorldGroupItem.getText()));
		if(tempItem.getText() == WorldGroupItem.getText()){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	/**
     * 
     * This method is called to check if a item is within the "Peers" tree.
     * 
     * 
     * @param item Item to check
     * @return returns true if item is inside the "Peers" tree, otherwise false
     */
	public static boolean inPeers(TreeItem item){
		TreeItem tempItem = item;
		if(item.getText() == peerItem.getText())return false;
		do{
			tempItem = tempItem.getParentItem();
			if(tempItem == null)return false;
		}while((tempItem.getText()!=peerItem.getText()) && (tempItem.getText()!=WorldGroupItem.getText()));
		if(tempItem.getText() == peerItem.getText()){
			return true;
		}
		else{
			return false;
		}
	}

	/**
     * 
     * This method fills the discovery tree with the elements "Groups","Peers" and its subelements.
     * The information for the subelements comes from root
     * 
     * 
     * @param tree The discovery tree to fill
     * @param root The discovety root
     * @return no return value
     */
	public static void setDiTree(Tree tree, DAGGroup root){
		
		TreeItem item;
		DAGGroup group;
		DAGPeer peer;
		
			//root item
		WorldGroupItem = new TreeItem(tree,0);
		WorldGroupItem.setText(groupsString);
		WorldGroupItem.setData(groupsString);
		
			//peers item
		peerItem = new TreeItem(tree,0);
		peerItem.setText(peersString);
		peerItem.setData(peersString);
		
		DAGIterator iterator = new DAGIterator(root);
		iterator.newPeerEnumeration();
		
			//fill "Peers" with peers
		while(iterator.hasMorePeers()){
				//add peer to list
			peer = (DAGPeer)iterator.getNextPeer();
			item = new TreeItem(peerItem,0);
			item.setText(peer.getName());
			item.setData(peer);
				//add properties to peer
			addPropertiesToPeer(item,peer);
		}
		
			// fill "Groups" with peers	
		fillGroup(root,WorldGroupItem);
	
	}

	/**
     * 
     * This method fills the deploy tree with peers.
     * The information for the peers comes from root
     * 
     * 
     * @param tree The deploy tree to fill
     * @param root The deploy root
     * @return no return value
     */
	public static void setDeTree(Tree tree, DAGGroup root){
		
		DAGPeer peer;
		TreeItem item;
		
		DAGIterator iterator = new DAGIterator(root);
		iterator.newPeerEnumeration();
		
			//fill list with peers
		while(iterator.hasMorePeers()){
				//add peer to list
			peer = (DAGPeer)iterator.getNextPeer();
			item = new TreeItem(tree,0);
			item.setText(peer.getName());
			item.setData(peer);
				//add properties to peer
			addPropertiesToPeer(item,peer);
		}
		
	}
	
	/**
     * 
     * This method is called to add properties (services, bundles, ...) to a item.
     * 
     * @param item Add properties to this item
     * @param peer Properties of this peer are added to item
     * @return no return value
     */
	private static void addPropertiesToPeer(TreeItem item,DAGPeer peer){
		
		TreeItem propertyItem;
		
			// pipe
		propertyItem = new TreeItem(item,0);
		propertyItem.setText(pipeString);
		propertyItem.setData(pipeString);
			// property
		propertyItem = new TreeItem(item,0);
		propertyItem.setText(propertyString);
		propertyItem.setData(propertyString);
			// services
		propertyItem = new TreeItem(item, 0);
		propertyItem.setText(serviceString);
		propertyItem.setData(serviceString);
			// bundle
		propertyItem = new TreeItem(item,0);
		propertyItem.setText(bundleString);
		propertyItem.setData(bundleString);
		addBundles(propertyItem,peer);
		
	}
	
	/**
     * 
     * This method is called to add bundle properties to a item.
     * 
     * @param item Add properties to this item
     * @param peer Bundle information comes from this peer
     * @return no return value
     */
	private static void addBundles(TreeItem item, DAGPeer peer)
	{
		
		Vector bundles = peer.getBundles();
		
		DAGBundle b;
		TreeItem bItem;
		for(int i=0;i<bundles.size();i++){
			b = (DAGBundle)bundles.get(i);
			bItem = new TreeItem(item,0);
			bItem.setText(b.getName());
			bItem.setData(bundleChildString);
		}
		
	}
	
	
	/**
     * 
     * This method fills the groupItem with group informations.
     * 
     * @param group Group with peers and groups
     * @param groupItem Item to fill with peers and groups
     * @return no return value
     */
	private static void fillGroup(DAGGroup group, TreeItem groupItem){
		
		if(group.getChildren().size() <= 0)return;
		
		DAGMember member;
		TreeItem item;	
		
			// groups
		for(int i=0;i<group.getChildren().size();i++){			
			member = (DAGMember) group.getChildren().get(i);						
			if(member instanceof DAGGroup){				
				item = new TreeItem(groupItem,0);
				item.setText(member.getName());
				fillGroup((DAGGroup)member,item);				
			}			
		}
		
			// peers
		for(int i=0;i<group.getChildren().size();i++){			
			member = (DAGMember) group.getChildren().get(i);			
			if(member instanceof DAGPeer){
				item = new TreeItem(groupItem,0);
				item.setText(member.getName());
				item.setData((DAGPeer)member);				
			}		
		}
		
	}
	
	

//	---------------------------------------------------------------------------
//	--------------------- get/set for item's names  ---------------------------
//	---------------------------------------------------------------------------			

	/**
     * 
     * Get "Groups" string
     * 
     * @param none No parameters needed
     * @return returns the name
     */
	public static String getGroupsString() {
		return groupsString;
	}

	/**
     * 
     * Get "Peers" string
     * 
     * @param none No parameters needed
     * @return returns the name
     */
	public static String getPeersString() {
		return peersString;
	}

	/**
     * 
     * Get pipes string
     * 
     * @param none No parameters needed
     * @return returns the name
     */
	public static String getPipeString() {
		return pipeString;
	}

	/**
     * 
     * Get property string
     * 
     * @param none No parameters needed
     * @return returns the name
     */
	public static String getPropertyString() {
		return propertyString;
	}
	
	/**
     * 
     * Get bundle string
     * 
     * @param none No parameters needed
     * @return returns the name
     */
	public static String getBundleString(){
		return bundleString;
	}
	
	/**
     * 
     * Get bundleChild string
     * 
     * @param none No parameters needed
     * @return returns the name
     */
	public static String getBundleChildString(){
		return bundleChildString;
	}

//	---------------------------------------------------------------------------
//	--------------------- collapse  -------------------------------------------
//	---------------------------------------------------------------------------	
	
	/**
     * 
     * Call this method to collapse all subelements of a peer
     * 
     * @param peer Subelements of this peer are collapsed
     * @return returns the name
     */
	public static void collapse(DAGPeer peer){
		
		if(!peerItem.getExpanded()){
			peerItem.setExpanded(true);
		}
				
		TreeItem[] items = peerItem.getItems();
		for(int i=0;i<items.length;i++){
			if(items[i].getText() == peer.getName()){
				items[i].setExpanded(true);
			}else{
				items[i].setExpanded(false);
			}
		}
		
	}
	
//	---------------------------------------------------------------------------
//	--------------------- update  ---------------------------------------------
//	---------------------------------------------------------------------------	

	/**
     * 
     * Call this method to rebuild the treestructures
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static synchronized void updateAll(){
		
		if(!init)return;
		
		final DAGGroup fDiRoot = Controller.getDiscoveryRoot();
		final DAGGroup fDeRoot = Controller.getDeployRoot();
		
		Thread guit = new Thread (){	
				public void run(){	
					if(parent.isDisposed())return;
					
					peerTreeDi.removeAll();
					setDiTree(peerTreeDi, fDiRoot);	
					
					peerTreeDe.removeAll();
					setDeTree(peerTreeDe, fDeRoot);
				}
		};
		
		parent.getDisplay().asyncExec(guit);
		
	}
	
	/**
     * 
     * Updates discoveryTree when new peer appears
     * 
     * @param peer Peer to add
     * @return no return value
     */
	public static synchronized void addPeerInDiscoveryUpdate(DAGPeer peer){
				
		if(!init)return;
		
		final DAGPeer fpeer = peer;
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())return;
									
					// add peer in discovery list
				TreeItem[] items = peerTreeDi.getItems();
				
					// get "Peers"
				TreeItem peersItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(peersString)){
						peersItem = items[i];
					}
				}
				if(peersItem == null){
					return;
				}
				
					// check if peer is already there
				TreeItem[] tempItems = peersItem.getItems();
				for(int i=0;i<tempItems.length;i++){
					if(tempItems[i].getText().equals(fpeer.getName())){
						return;
					}
				}
				
					// get "Groups"
				TreeItem groupsItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(groupsString)){
						groupsItem = items[i];
					}
				}
				if(groupsItem == null){
					return;
				}
				
				TreeItem newItem = new TreeItem(peersItem,0);
				newItem.setText(fpeer.getName());
				newItem.setData(fpeer);
				addPropertiesToPeer(newItem,fpeer);
				
				newItem = new TreeItem(groupsItem,0);
				newItem.setText(fpeer.getName());
				newItem.setData(fpeer);
				
			}
		};
		
		parent.getDisplay().asyncExec(guit);
		
	}
	
	/**
     * 
     * Updates deployTree when new peer appears
     * 
     * @param peer Peer to add
     * @return no return value
     */
	public static synchronized void addPeerInDeployUpdate(DAGPeer peer){
		
		if(!init)return;
		
		final DAGPeer fpeer = peer;
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())return;
									
					// check if peer is already there
				TreeItem[] items = peerTreeDe.getItems();
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeer.getName())){
						return;
					}
				}
				
					// add peer in deploy list
				TreeItem newItem = new TreeItem(peerTreeDe,0);
				newItem.setText(fpeer.getName());
				newItem.setData(fpeer);
				addPropertiesToPeer(newItem,fpeer);
				
			}
		};
		
		parent.getDisplay().asyncExec(guit);
		
	}
	
	/**
     * 
     * Updates trees (discovery AND deploy) when peer disappears
     * 
     * @param peer Peer to delete
     * @return no return value
     */
	public static void deletePeerUpdate(DAGPeer peer){
			
		if(!init)return;
		
		final DAGPeer fpeer = peer;
		
		Thread guit = new Thread (){	
			public void run(){	
				
					// delete peer from deploy list
				TreeItem[] items = peerTreeDe.getItems();
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeer.getName())){
						items[i].dispose();
					}
				}
				
			}
		};
		
		parent.getDisplay().asyncExec(guit);
		
	}
	
	/**
     * 
     * Updates deployTree when peer is removed
     * 
     * @param peer Peer to remove
     * @return no return value
     */
	public static void removePeerUpdate(DAGPeer peer){
		
		if(!init)return;
		
		final DAGPeer fpeer = peer;
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())return;
							
					// delete peer from discovery list
				TreeItem[] items = peerTreeDi.getItems();
				
					// get "Peers"
				TreeItem peersItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(peersString)){
						peersItem = items[i];
					}
				}
				if(peersItem == null){
					return;
				}
				
				items = peersItem.getItems();
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeer.getName())){
						items[i].dispose();
					}
				}
				
				items = peerTreeDi.getItems();
				
				 	// get "Groups"
				TreeItem groupsItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(groupsString)){
						groupsItem = items[i];
					}
				}
				if(groupsItem == null){
					return;
				}
				
				items = groupsItem.getItems();
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeer.getName())){
						items[i].dispose();
					}
				}
				
			}
		};
		
		parent.getDisplay().asyncExec(guit);
		
	}

	
	/**
     * 
     * Updates discoveryTree when bundle changed
     * 
     * @param peer Bundle of this peer changed
     * @return no return value
     */
	public static synchronized void bundleUpdateInDiscovery(DAGPeer peer){
		
		if(!init)return;
				
		final DAGPeer fpeer = peer;
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())return;
				
					// update in discovery
				TreeItem[] items = peerTreeDi.getItems();
				
					// get "Peers"
				TreeItem peersItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(peersString)){
						peersItem = items[i];
					}
				}
				if(peersItem == null){
					return;
				}
				
				items = peersItem.getItems();
				
					// get peer
				TreeItem peerItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeer.getName())){
						peerItem = items[i];
					}
				}
				if(peerItem == null){
					return;
				}
				
				items = peerItem.getItems();
				
					// get bundle
				TreeItem bundleItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(bundleString)){
						bundleItem = items[i];
					}
				}
				if(bundleItem == null){
					return;
				}
				
				boolean wasExpanded;
				if(bundleItem.getExpanded()){
					bundleItem.setExpanded(false);
					wasExpanded = true;
				}
				else{
					wasExpanded = false;
				}
				
				items = bundleItem.getItems();
				
					// delete all bundles
				for(int i=0;i<items.length;i++){
					items[i].dispose();
				}
				
					// fill with bundles
				addBundles(bundleItem,fpeer);
				
					// as before
				bundleItem.setExpanded(wasExpanded);
						
			}
		};
		
		parent.getDisplay().asyncExec(guit);

	}
	
	/**
     * 
     * Updates deployTree when bundle changed
     * 
     * @param peer Bundle of this peer changed
     * @return no return value
     */
	public static synchronized void bundleUpdateInDeploy(DAGPeer peer)
	{
		
		if(!init)return;
				
		final DAGPeer fpeer = peer;
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())return;
						
					// update in deploy
				TreeItem[] items = peerTreeDe.getItems();
				
				TreeItem peersItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeer.getName())){
						peersItem = items[i];
					}
				}
				if(peersItem == null){
					return;
				}
				
				items = peersItem.getItems();
				
					// get bundle
				TreeItem bundleItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(bundleString)){
						bundleItem = items[i];
					}
				}
				if(bundleItem == null){
					return;
				}

				boolean wasExpanded;
				if(bundleItem.getExpanded()){
					bundleItem.setExpanded(false);
					wasExpanded = true;
				}
				else{
					wasExpanded = false;
				}
				
				items = bundleItem.getItems();
				
					// delete all bundles
				for(int i=0;i<items.length;i++){
					items[i].dispose();
				}
				
					// fill with bundles
				addBundles(bundleItem,fpeer);
				
					// as before
				bundleItem.setExpanded(wasExpanded);
			
			}
		};
		
			
		parent.getDisplay().asyncExec(guit);

	}
	
	public static synchronized void serviceReferenceAdded(ServiceReference sref)
	{
		if(!init)
		    return;
		
		final String fpeername = sref.getPeer();
		final ServiceReference fsref = sref; 
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())
				    return;
						
					// update in deploy
				TreeItem[] items = peerTreeDi.getItems()[1].getItems();
				
				TreeItem peersItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(fpeername)){
						peersItem = items[i];
					}
				}
				if(peersItem == null){
					return;
				}
				
				items = peersItem.getItems();
				
					// get service
				TreeItem serviceItem = null;
				for(int i=0;i<items.length;i++){
					if(items[i].getText().equals(serviceString)){
						serviceItem = items[i];
					}
				}
				if(serviceItem == null){
					return;
				}
		
				boolean wasExpanded;
				if(serviceItem.getExpanded()){
				    serviceItem.setExpanded(false);
					wasExpanded = true;
				}
				else{
					wasExpanded = false;
				}
				
				items = serviceItem.getItems();
				
					// delete all bundles
//				for(int i=0;i<items.length;i++){
//					items[i].dispose();
//				}
				
					// add service
				TreeItem newitem = new TreeItem(serviceItem, SWT.NULL);
				newitem.setText(fsref.getID());
				newitem.setData(serviceChildString);
				
				
					// as before
				serviceItem.setExpanded(wasExpanded);
			
			}
		};
		
			
		parent.getDisplay().asyncExec(guit);
	}
	
	public void setFocus() {
 
	}
	
	
}