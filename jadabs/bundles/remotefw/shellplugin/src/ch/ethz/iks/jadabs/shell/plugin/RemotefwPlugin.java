package ch.ethz.iks.jadabs.shell.plugin;

import ch.ethz.iks.jadabs.shell.svc.IShellPlugin;

/**
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class RemotefwPlugin implements IShellPlugin {

	/**
	 * @see ch.ethz.iks.jadabs.shell.svc.IShellPlugin#getQualifier()
	 */
	public String getQualifier() {
		return new String("remote");
	}

	/**
	 * @see ch.ethz.iks.jadabs.shell.svc.IShellPlugin#delegateCommand(java.lang.String[])
	 */
	public void delegateCommand(String[] cmd) {
		System.out.println(cmd[0]);
	}

	/**
	 * @see ch.ethz.iks.jadabs.shell.svc.IShellPlugin#printHelp()
	 */
	public void printHelp() {
		System.out.println("RemoteFW Shell Plugin:");
		System.out.println("remote.<peername>.<command>");
	}

}
