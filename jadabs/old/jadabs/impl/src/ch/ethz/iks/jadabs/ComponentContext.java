/*
 * Created on Feb 20, 2004
 *
 */
package ch.ethz.iks.jadabs;

/**
 * @author andfrei
 *  
 */
public class ComponentContext implements IComponentContext
{

    IComponentResource m_copres;

    public ComponentContext(IComponentResource copres) {
        m_copres = copres;
    }

    /**
     * Get the component with the given name.
     */
    public Object getComponent(String mainname)
    {
        IComponentResource copres = ComponentRepository.Instance()
                .getComponentResourceByClassname(mainname);
        return copres.getExtObject();

    }

    public Object getProperty(String name)
    {
        Object pvalue = Jadabs.Instance().getProperty(name);
        
        if (name.equals(BootstrapConstants.PCOPREP))
            if (pvalue == null)
                pvalue = BootstrapConstants.PCOPREP_DEFAULT;
        
        return pvalue;
    }

    public String getComponentVersion()
    {
        return String.valueOf(m_copres.getVersion());
    }

}