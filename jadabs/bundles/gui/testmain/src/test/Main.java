/*
 * Created on Nov 18, 2004
 *
 */
package test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author andfrei
 *  
 */
public class Main
{

    public static void main(String[] args)
    {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Main");
        Menu mb = new Menu(shell, SWT.BAR);
        shell.setMenuBar(mb);
        Menu menu = new Menu(shell, SWT.POP_UP);
        MenuItem item = new MenuItem(menu, SWT.CASCADE);
        item.setText("item 1");
        MenuItem item2 = new MenuItem(menu, SWT.CASCADE);
        item2.setText("item 2");
        shell.setMenu(menu);
        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}