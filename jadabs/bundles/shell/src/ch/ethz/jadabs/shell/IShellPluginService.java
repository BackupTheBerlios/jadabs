/*
 * Created on 16.06.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.shell;


/**
 * @author rjan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IShellPluginService {
   public void registerPlugin(IShellPlugin plugin);
   public void unregisterPlugin(IShellPlugin plugin);
}
