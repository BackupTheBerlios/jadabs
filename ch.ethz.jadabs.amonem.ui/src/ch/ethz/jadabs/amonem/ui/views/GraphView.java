
package ch.ethz.jadabs.amonem.ui.views;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGIterator;
import ch.ethz.jadabs.amonem.manager.DAGMember;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;



public class GraphView extends ViewPart{

		// SWT elements
	private static Text text;
	private static Composite parent;
	private static Label label;
	private static Menu menu;
	private static GC gc;
	
		// root
	private static DAGGroup root;
	
		// selected peer
	private static DAGPeer selectedPeer = null;
	
		// peer image
	private static Image image;
	
		// flags
	private static boolean mouseDown = false;
	private static int draw = 1;

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

		GraphView.parent = parent;
		image = SWTResourceManager.getImage("ch/ethz/jadabs/amonem/ui/views/pda.gif");
		label = new Label(parent,1);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		gc = new GC(label);
		menu = new Menu(label);
		
		label.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				try{
					if(!mouseDown){
						drawAllPipes(Controller.getDiscoveryRoot());
					}
					drawAllNodes(Controller.getDiscoveryRoot());
				}
				catch(Exception ex){
					
				}
			}		
		});
		label.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				selectedPeer = isPeer(e.x,e.y);
				if(e.button == 1){
					mouseDown = true;
					if(selectedPeer != null){
						selectedPeer = isPeer(e.x,e.y);
						PeerListView.collapse(selectedPeer);
					}
				}
				else{ // e.button == 2
					menu = new Menu(label);
					if(selectedPeer != null){
						MenuItem editItem = new MenuItem(menu, SWT.CASCADE);
						editItem.setText("install bundle");
						editItem.addSelectionListener(new SelectionListener() {
							public void widgetSelected(SelectionEvent e) {
								//System.out.println("install bundle");
								Controller.activateInstallBundleView(selectedPeer);
								}
							public void widgetDefaultSelected(SelectionEvent e) {
								//System.out.println("install bundle");
								Controller.activateInstallBundleView(selectedPeer);}
						});
//						if(Controller.getDeployRoot().getElement(selectedPeer.getName())!=null){
//							MenuItem deleteItem = new MenuItem(menu, SWT.CASCADE);
//							deleteItem.setText("kill");
//							deleteItem.addSelectionListener(new SelectionListener() {
//								public void widgetSelected(SelectionEvent e) {
//									AmonemUI.amonemManager.kill(selectedPeer.getName());
//									System.out.println("kill");
//								}
//								public void widgetDefaultSelected(SelectionEvent e) {
//									AmonemUI.amonemManager.kill(selectedPeer.getName());
//									System.out.println("kill");
//								}
//							});
//						}
					}else{	// right click not on peer
						//MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
						//menuItem.setText("new peer");
						//menuItem.addSelectionListener(new SelectionListener() {
						//	public void widgetSelected(SelectionEvent e) {
						//		Controller.activateNewPeerView();								
						//	}
						//	public void widgetDefaultSelected(SelectionEvent e) {
						//		Controller.activateNewPeerView();
						//	}
						//});
					}
					label.setMenu(menu);
				}
			}
			public void mouseUp(MouseEvent e){
				if(e.button == 1){
					mouseDown = false;
					drawAll(Controller.getDiscoveryRoot());
				}
			}
		});
		label.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				//DAGPeer hiddenPeer;
//				if(mouseDown && (selectedPeer!=null)){
//					label.redraw(selectedPeer.getX()-image.getImageData().width/2-25,selectedPeer.getY()-image.getImageData().height/2-25,image.getImageData().width+80,image.getImageData().height+80,true);
//					//label.update();
//					selectedPeer.setX(e.x);
//					selectedPeer.setY(e.y);
//					drawNode(selectedPeer);
//					label.update();
//					//hiddenPeer = isPeer(movingPeer.getX()-image.getImageData().width/2-30,movingPeer.getY()-image.getImageData().height/2-30);
//					//if(hiddenPeer!=null)drawNode(hiddenPeer);
//					//hiddenPeer = isPeer(movingPeer.getX()-image.getImageData().width/2-30,movingPeer.getY()+image.getImageData().height/2+30);
//					//if(hiddenPeer!=null)drawNode(hiddenPeer);
//					//hiddenPeer = isPeer(movingPeer.getX()+image.getImageData().width/2+30,movingPeer.getY()-image.getImageData().height/2-30);
//					//if(hiddenPeer!=null)drawNode(hiddenPeer);
//					//hiddenPeer = isPeer(movingPeer.getX()+image.getImageData().width/2+30,movingPeer.getY()+image.getImageData().height/2+30);
//					//if(hiddenPeer!=null)drawNode(hiddenPeer);
//					//canvas.update();	
//					
//					
//				}
				if(mouseDown && (selectedPeer!=null)){
					//draw ^= 1;
					//if(draw == 1)return;
					label.redraw();
					selectedPeer.setX(e.x);
					selectedPeer.setY(e.y);
					//drawAllPipes(Controller.getRoot());
					//drawAllNodes(Controller.getRoot());
					label.update();
				}
			}
		});
	
		init = true;
		update();
			
	}

	
	
//	---------------------------------------------------------------------------
//	--------------------- isPeer method ---------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Check if at (mouse)position x,y is a peer
     * 
     * 
     * @param x X position of the mouse
     * @param y Y position of the mouse
     * @return returns peer at position x,y (if there is one), otherwise null
     */
	private DAGPeer isPeer(int x, int y){

		DAGPeer peer;
		
		DAGIterator iterator = new DAGIterator(Controller.getDiscoveryRoot());
		iterator.newPeerEnumeration();
		
		while(iterator.hasMorePeers()){
			peer = (DAGPeer)iterator.getNextPeer();
			if((peer.getX()-image.getImageData().width/2<x) && (peer.getY()-image.getImageData().height/2<y) && (peer.getX()+image.getImageData().width/2>x) && (peer.getY()+image.getImageData().height/2>y)){
				return peer;
			}	
		}
		
		return null;
		
	}
	
//	---------------------------------------------------------------------------
//	--------------------- highlite peer ---------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Call this method to highlite a peer
     * 
     * 
     * @param peer This peer will be highlited
     * @return no return value
     */
	public static void highlitePeer(DAGPeer peer){
		
		redrawGraph();
		gc.drawImage(image,0,0,image.getImageData().width,image.getImageData().height,peer.getX()-image.getImageData().width/2-5,peer.getY()-image.getImageData().height/2-5,image.getImageData().width+10,image.getImageData().height+10);
		
	}
	
//	---------------------------------------------------------------------------
//	--------------------- graph methods ---------------------------------------
//	---------------------------------------------------------------------------

	/**
     * 
     * Given a group, this method computes the position of all direct subgroups/peers (but
     * not deeper levels)
     * 
     * 
     * @param members Compute position of this group
     * @param middleX Position in the middle of the drawspace
     * @param width Width of the drawspace
     * @param middleY Position in the middle of the drawspace
     * @param height Height of the drawspace
     * @return no return value
     */
	private static void computeLocation(Vector members, int middleX, int width, int middleY, int height){
		
		if(members == null) return;
	    if(members.size() == 0) return;

	    double angle = 360.0 / members.size();
	    double posX;
	    double posY;
	    int x = middleX-(width/2);
	    int y = middleY-(height/2);
	    
	    DAGMember member;
	    DAGGroup dg;
	   
	    for(int i=0;i<members.size();i++) {
	    	member = (DAGMember) members.get(i);
	    	posX = middleX + Math.sin(Math.toRadians(angle * i)) * (height / 2);
	    	posY = middleY - Math.cos(Math.toRadians(angle * i)) * (width / 2);
	    	member.setX((int)posX);
	    	member.setY((int)posY);
	    	if(member instanceof DAGGroup){
	    		dg = (DAGGroup)member;
	    		dg.setHeight(height/3);
	    		dg.setWidth(width/3);
	    	}
	    }
	  }
	
	/**
     * 
     * Computes all positions of the peers, groups and subgroups
     * 
     * 
     * @param member Root group 
     * @return no return value
     */
	public static void computeGraph(DAGMember member){
				
		if(member instanceof DAGPeer){
			return;// nop :-)
		}
		else{	//instanceof ElementGroup
			DAGGroup dg = (DAGGroup)member;
			Vector newChildren = new Vector();
			for(int i=0;i<dg.getChildren().size();i++){
				if(((DAGMember)(dg.getChildren()).get(i)).getParents().get(0) == dg){
					newChildren.addElement(dg.getChildren().get(i));
				}
			}
			computeLocation(newChildren,dg.getX(),dg.getHeight(),dg.getY(),dg.getWidth());
			for(int i=0;i<newChildren.size();i++){
				computeGraph((DAGMember)newChildren.get(i));
			}
		}
		
	}
	
	/**
     * 
     * This method is called to gerenate the graph
     * 
     * 
     * @param root Top group (i.e. WorldGroup)
     * @param height Height of the composite
     * @param width Width of the composite
     * @return no return value
     */
	public static void generateGraph(DAGGroup root, int height, int width){
		
		if(root == null) return;
		
		int middleX = height/2;
		int middleY = width/2;
		
		computeLocation(root.getChildren(),middleX,width/2,middleY,height/2);

		for(int i=0;i<root.getChildren().size();i++){
				computeGraph((DAGMember)(root.getChildren().get(i)));		
		}
		
	}
	
//	---------------------------------------------------------------------------
//	--------------------- draw methods ----------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Call this method to draw all pipes
     * Not yet implementet, because no pipes are available
     * 
     * 
     * @param 
     * @return 
     */
	private static void drawPipe(){
		// pipes not yet available
	}
	
	/**
     * 
     * Call this method to draw all pipes
     * 
     * 
     * @param root Draw pipes of this DAG
     * @return no return value
     */
	private static void drawAllPipes(DAGGroup root){
		
		DAGPeer peer;
		Vector a = new Vector();
		int c = 0;
		
		DAGIterator iterator = new DAGIterator(root);
		iterator.newPeerEnumeration();
		
		while(iterator.hasMorePeers()){
			peer = (DAGPeer)iterator.getNextPeer();
			a.add(c,peer);
			c++;
		}
		
		gc = new GC(label);
		gc.setLineWidth(3);
		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		
		for(int i=0;i<c;i++){
			for(int j=i;j<c;j++){
				gc.drawLine(((DAGPeer)a.get(i)).getX(),((DAGPeer)a.get(i)).getY(),((DAGPeer)a.get(j)).getX(),((DAGPeer)a.get(j)).getY());
			}
		}
		
		label.update();
		gc = new GC(label);
		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		for(int i=0;i<c;i++){
			gc.fillOval(((DAGPeer)a.get(i)).getX()-image.getImageData().height/2-10,((DAGPeer)a.get(i)).getY()-image.getImageData().width/2-10,image.getImageData().height+20,image.getImageData().width+20);
		}
		
		label.update();
				
	}
	
	/**
     * 
     * Call this method to draw one name
     * 
     * 
     * @param peer Draw the name of this peer
     * @return no return value
     */
	private static void drawName(DAGPeer peer){
				
		gc.drawText(peer.getName(),peer.getX()-peer.getName().length()/2,peer.getY()+image.getBounds().height/2+5,false);

	}
	
	/**
     * 
     * Call this method to draw all names
     * 
     * 
     * @param root Draw names of this DAG
     * @return no return value
     */
	private static void drawAllNames(DAGGroup root){
		
		gc = new GC(label);
		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		
		DAGPeer peer;
		
		DAGIterator iterator = new DAGIterator(root);
		iterator.newPeerEnumeration();
		
		while(iterator.hasMorePeers()){
			drawName((DAGPeer)iterator.getNextPeer());
		}
		
		label.update();
				
	}
	
	/**
     * 
     * Call this method to draw one nodes
     * 
     * 
     * @param peer Draw this peer
     * @return no return value
     */
	public static void drawNode(DAGPeer peer){
		
		gc.drawImage(image,peer.getX()-image.getBounds().height/2,peer.getY()-image.getBounds().width/2);
		
		gc = new GC(label);
		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		drawName(peer);
		
	}
	
	/**
     * 
     * Call this method to draw all nodes
     * 
     * 
     * @param root Draw this DAG
     * @return no return value
     */
	private static synchronized void drawAllNodes(DAGGroup root){
		
		DAGPeer peer;
		
		DAGIterator iterator = new DAGIterator(root);
		iterator.newPeerEnumeration();
		
		while(iterator.hasMorePeers()){
			drawNode((DAGPeer)iterator.getNextPeer());
		}
		
		label.update();
		
	}
	
	/**
     * 
     * Call this method to redraw all (nodes,pipes,...)
     * 
     * 
     * @param root Draw this DAG
     * @return no return value
     */
	public static synchronized void drawAll(DAGGroup root){
		if(root == null){return;}
		gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		label.redraw();
		label.update();
		drawAllPipes(root);
		drawAllNodes(root);
	}
	
	
//	---------------------------------------------------------------------------
//	--------------------- update ----------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * Updates the graph
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static synchronized void update(){

		if(!init)return;
		
		final DAGGroup froot = Controller.getDiscoveryRoot();
		
		Thread guit = new Thread (){	
			public void run(){
				if(parent.isDisposed())return;
				try{
					generateGraph(froot,parent.getSize().x,parent.getSize().y);
					drawAll(froot);
				}catch(Exception ex){
					System.out.println("Problem updating graph");
				}	
			}
	};
	
	parent.getDisplay().asyncExec(guit);
	
			
	}
	
	
	public void setFocus() {
		
		drawAll(Controller.getDiscoveryRoot());
		
	}
	
	/**
     * 
     * Call this method to redraw the graph
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static synchronized void redrawGraph(){
		
		drawAll(Controller.getDiscoveryRoot());
		
	}
	
}
