package ch.ethz.jadabs.amonem.ui.explorer;

import java.io.File;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class FileTableLabelProvider implements ITableLabelProvider
{
  public String getColumnText(Object obj, int i)
  {
    return ((File) obj).getName();
  }

  public void addListener(ILabelProviderListener ilabelproviderlistener)
  {
  }

  public void dispose()
  {
  }

  public boolean isLabelProperty(Object obj, String s)
  {
    return false;
  }

  public void removeListener(ILabelProviderListener ilabelproviderlistener)
  {
  }

  public Image getColumnImage(Object arg0, int arg1)
  {
    return null;
  }
}
