/*
 * Created on Apr 16, 2003
 *
 * $Id: IComponent.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

/**
 * IComponent defines the interface which every loadable component has to satisfy in one of its classes.
 * The class will be the main class in the component package. This is the class which is first instantiated
 * when a component gets loaded.
 * 
 * @author andfrei
 */
public interface IComponent {

	public String COMPONENT_TYPE = "jadabs-cop";

	public static final String factoryMethod = "createComponentMain"; // the method to create a component main class (instead of calling its default constructor)
    
    /**
     * Initialize the component with the given ComponentContext, the component shouldn't use
     * the ComponentRepository.Instance anymore because of IoC.
     * 
     * @param context
     */
    public void init(IComponentContext context);

	/**
	 * Start the component with args.
	 * 
	 * @param args
	 */
	public void startComponent(String[] args);
	
	/**
	 * Stop the component.
	 *
	 */
	public void stopComponent();
	
	/**
	 * Dispose the component by releasing all resource, for example 
	 * to release a database connection.
	 * 
	 * TODO: change the component interface to allow disposing the component, needs
	 * changes in all components.
	 */
	 public void disposeComponent();



}
