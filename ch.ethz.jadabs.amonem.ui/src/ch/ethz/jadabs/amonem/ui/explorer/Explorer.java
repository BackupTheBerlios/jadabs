package ch.ethz.jadabs.amonem.ui.explorer;

import java.io.File;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.ethz.jadabs.amonem.ui.perspective.AmonemUI;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;

public class Explorer extends ApplicationWindow{
	
		// SWT elements
	private static TableViewer tbv;			
	private static Text text;	
	private static Explorer explorer;
	
		// explorer
	private static TreeViewer tv;
		
		// Strings for buttons
	private static String fieldName;
	public static String buttonString = "ok";
	public static String loadString = "load";
	public static String saveString = "save";
	public static String savePeerString = "save peer";
	public static String okString = "ok";
	
		// String for "deploy"
	private Text textTemp;
	private Text textLocal;
	private Text textXargs;
	
		// root
	private static File[] roots;
	
		// mode
	private static int mode;
	public static int DIRECTORY = 0;
	public static int FILE = 1;
	
	
	 /**
     * 
     * Constructor
     * 
     */
	public Explorer(){
		super(null);
	}

	
	 /**
     * 
     * This method creates the SWT elements for an explorer
     * 
     * @param parent Parent is the base composite
     */
	protected Control createContents(Composite parent){
		
		parent.setBounds(0,0,550,500);
		
			// upper sash
		SashForm sash = new SashForm(parent, SWT.VERTICAL | SWT.NULL);	
		sash.setBounds(0,0,550,480);
			// lower sash
		SashForm sash_form = new SashForm(sash, SWT.VERTICAL | SWT.NULL);
		sash_form.setBounds(0,0,200,200);
		
		Composite composite = new Composite(sash,SWT.NONE);
		composite.setBounds(0,0,345,220);
		
		final Label label = new Label(composite, SWT.NONE);
		label.setBounds(15, 15, 60, 20);
		label.setText("Root:");
		
		final Combo combo = new Combo(composite, SWT.NONE);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tv.setInput(new File(combo.getText()));
			}
		});
		combo.setBounds(80, 10, 100, 20);
		File[] roots = File.listRoots();
		for(int i=0;i<roots.length;i++){
			combo.add(roots[i].toString());
		}
		combo.select(0);
    
			// explorer
		tv = new TreeViewer(sash_form);
		tv.setContentProvider(new FileTreeContentProvider());
		tv.setLabelProvider(new FileTreeLabelProvider());
		tv.setInput(new File(combo.getText()));		
		
		final Label labelFile = new Label(composite, SWT.NONE);
		labelFile.setBounds(15, 45, 30, 25);
		if(buttonString.startsWith(saveString)){
			labelFile.setText("Konfig:");
		}else{
			labelFile.setText("File:");
		}
			
		final Text fileName = new Text(composite,SWT.BORDER);
		fileName.setBounds(80,40,350,25);
			
		final Label labelPath = new Label(composite, SWT.NONE);
		labelPath.setBounds(15, 75, 45, 25);
		labelPath.setText("Path:");
		
		final Label pathName = new Label(composite,SWT.NONE);
		pathName.setBounds(80,75,300,25);
		
		if(buttonString.equals(loadString)){
			final Label labedTemp = new Label(composite, SWT.NONE);
			labedTemp.setBounds(15, 230, 100, 25);
			labedTemp.setText("Temp:");

			textTemp = new Text(composite, SWT.BORDER);
			textTemp.setBounds(130, 230, 300, 25);
			textTemp.setText(Controller.loadTemp());
			
			final Label labelXargs = new Label(composite, SWT.NONE);
			labelXargs.setBounds(15, 195, 100, 25);
			labelXargs.setText("xargs template:");

			textXargs = new Text(composite, SWT.BORDER);
			textXargs.setBounds(130, 195, 300, 25);
			textXargs.setText(Controller.loadXarg());
			
			final Label labelLocal = new Label(composite, SWT.NONE);
			labelLocal.setBounds(15, 160, 100, 25);
			labelLocal.setText("Local deploy path:");

			textLocal = new Text(composite, SWT.BORDER);
			textLocal.setBounds(130, 160, 300, 25);
			textLocal.setText(Controller.loadLocal());
			
		}
		
		final Button exitButton = new Button(composite, SWT.NONE);
		exitButton.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				closeExplorer();
			}
		});
		exitButton.setBounds(50, 110, 100, 25);
		exitButton.setText("Cancel");
		
		if(buttonString == loadString){
			final Button deployButton = new Button(composite, SWT.NONE);
			deployButton.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					
					String fullPath = "";
					
					char lastChar = pathName.getText().charAt(pathName.getText().length()-1);
					if(lastChar != File.separatorChar){
						pathName.setText(pathName.getText()+File.separatorChar);
					}
						
					if(fileName.getText().length() <= 0){
						Controller.activateErrorView("choose a filename !");
						System.out.println("choose a filename !");
						return;
					}
					
					AmonemUI.amonemManager.setFolders(textTemp.getText(),textLocal.getText(),textXargs.getText(),textTemp.getText());

					AmonemUI.amonemManager.importDAG(pathName.getText()+fileName.getText(),true);
	
					Controller.closeExplorer();
									
				}
			});
			deployButton.setBounds(270, 110, 100, 25);
			deployButton.setText("Deploy");
		}
		
		Button button = new Button(composite,0);
		button.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				
//				if(pathName.getText().length()<=0 || fileName.getText().length()<=0){
//					System.out.println("path directory error");
//					return;
//				}
				
				
				String fullPath = "";
//				if(pathName.getText().endsWith(String.valueOf(Path.SEPARATOR)) || pathName.getText().endsWith("\\")){
//					fullPath = pathName.getText()+fileName.getText();
//				}else{
//					fullPath = pathName.getText()+String.valueOf(Path.SEPARATOR)+fileName.getText();
//				}
//				if(pathName.getText().lastIndexOf("/") != pathName.getText().firstIndexOf("/")){
//					
//				}
//				System.out.println(pathName.getText());
//				System.out.println(fileName.getText());
//				System.out.println(pathName.getText()+fileName.getText());
//				
				char lastChar = pathName.getText().charAt(pathName.getText().length()-1);
//				if(!(lastChar == Path.SEPARATOR || lastChar == '\\')){
//					pathName.setText(pathName.getText()+Path.SEPARATOR);
//				}
				
				if(lastChar != File.separatorChar){
					pathName.setText(pathName.getText()+File.separatorChar);
				}
								
				//System.out.println(pathName.getText());
				
				if(buttonString == okString){
					if(mode == DIRECTORY){
						text.setText(pathName.getText());
					}else if(mode == FILE){
						text.setText(pathName.getText()+fileName.getText());
					}
				}else{ 
						// fileName not needed
					if(buttonString == savePeerString){
						AmonemUI.amonemManager.exportPeer(pathName.getText(),fieldName);
					}
						// fileName needed
					else{
						if(fileName.getText().length() <= 0){
							Controller.activateErrorView("choose a filename !");
							System.out.println("choose a filename !");
							return;
						}
						//System.out.println(pathName.getText()+Path.SEPARATOR+fileName.getText());
						if(buttonString == loadString){
							AmonemUI.amonemManager.importDAG(pathName.getText()+fileName.getText(),false);
						}else if(buttonString == saveString){
							AmonemUI.amonemManager.exportDAG(pathName.getText(),fileName.getText());
						}
					}
				}
				Controller.closeExplorer();
			}
		});
		button.setBounds(160, 110, 100, 25);
		button.setText(buttonString);
		
		
		tv.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				File selected_file = (File)selection.getFirstElement();
				if(selected_file.isDirectory()){
					pathName.setText(selected_file.toString());
					fileName.setText("");
				}
				if(selected_file.isFile()){
					fileName.setText(selected_file.getName());
					pathName.setText(selected_file.toString().substring(0,selected_file.toString().length()-selected_file.getName().length()));					
				}
			}
		});
    
		parent.pack();
		parent.update();
		
		return sash;
	}

	/**
     * 
     * This method opens a new basic explorer window
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void main(String[] args){
		Explorer w = new Explorer();
		w.setBlockOnOpen(true);
		w.open();
		Display.getCurrent().dispose();
	}
	
	/**
     * 
     * This method opens a new explorer window
     * 
     * @param none No parameters needed
     * @return no return value
     */
	private static void init(){
		explorer = new Explorer();
		Controller.setExplorer(explorer);
		explorer.setBlockOnOpen(true);
		explorer.open();
	}
	
	
//	---------------------------------------------------------------------------
//	--------------------- methods ---------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method opens a new explorer window (with "load" properties)
     * 
     * @param none No parameters needed
     * @return none no return value
     */
	public static void load(){
		if(explorer != null){
			explorer.close();
		}
		buttonString = loadString;
		init();
	}
	
	/**
     * 
     * This method opens a new explorer window (with "save all peers" properties)
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static void saveAll(){
		if(explorer != null){
			explorer.close();
		}
		buttonString = saveString;
		init();
	}
	
	/**
     * 
     * This method opens a new explorer window (with "save one peer" properties)
     * 
     * @param peerName Name of the peer you want to save
     * @return no return value
     */
	public static void savePeer(String peerName){
		if(explorer != null){
			explorer.close();
		}
		Explorer.fieldName = peerName;
		buttonString = savePeerString;
		init();
	}
	
	/**
     * 
     * This method opens a new explorer window (with "browse" properties).
     * Choose the mode (DIRECTORY or FILE) to browse for directories or files.
     * 
     * @param text textfield to fill with file or directory
     * @param mode specifies if you browse for DIRECTORY or FILE
     * @return none no return value
     */
	public static void browse(Text text,int mode){
		if(explorer != null){
			explorer.close();
		}
		Explorer.mode = mode;
		buttonString = okString;
		Explorer.text = text;
		init();
	}
	
	/**
     * 
     * This method close an explorer window
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public void closeExplorer(){
		explorer = null;
		close();
	}
	
}