package ch.ethz.jadabs.amonem.ui.explorer;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;

public class FileTreeLabelProvider extends LabelProvider
{
  public String getText(Object element)
  {
    return ((File) element).getName();
  }
}
