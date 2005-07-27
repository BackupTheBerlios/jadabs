/*
 * Created on 27.07.2005
 *
 */
package ch.ethz.jadabs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RepoGeneration
{

    private static String repoDir = ".";
    
    private static String repositoryFile;
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {

        if (args.length == 1)
            repoDir = args[0];

        repositoryFile = repoDir + File.separatorChar+"repository.xml";
        
        try {
            List list = getOPDListing(new File(repoDir+File.separatorChar+"repository"));
        
            FileOutputStream fo = new FileOutputStream(repositoryFile);
            
            generateRepositoryFile(list, fo);
            
            fo.close();
        } catch (FileNotFoundException fne)
        {
            fne.printStackTrace();
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        
        
    }

    /**
     * Get all opds in the repository file system.
     * 
     * @param startingDir
     *            Directory to start.
     * @return List of all opd files
     * @throws FileNotFoundException
     */
    public static List getOPDListing(File startingDir) throws FileNotFoundException
    {
        List result = new ArrayList();

        File[] filesAndDirs = startingDir.listFiles();
        List filesDirs = Arrays.asList(filesAndDirs);
        Iterator filesIter = filesDirs.iterator();
        File file = null;
        while (filesIter.hasNext())
        {
            file = (File) filesIter.next();
            if (!file.isFile())
            {
                List deeperList = getOPDListing(file);
                result.addAll(deeperList);
            } else if (file.getName().endsWith(".opd"))
            {
                result.add(file);
            }
        }
        return result;
    }
    
    public static void generateRepositoryFile(List list, OutputStream outstream) throws IOException 
    {
        // create repository header
        StringBuffer sb = new StringBuffer();
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<?xml-stylesheet type=\"text/xsl\" href=\"repository.xsl\"?>\n\n");
        
        sb.append("<plugins>\n\n");
                
        sb.append("<repository-bundle-name>OSGi-Repository</repository-bundle-name>\n" +
                "<date>"+ new Date()+"</date>\n");
        outstream.write(sb.toString().getBytes());
        
        for(Iterator it = list.iterator();it.hasNext();)
        {
            File opdFile = (File)it.next();
            
            InputStream in = new FileInputStream(opdFile);
            
            int b;
            while ((b = in.read()) != -1)
                outstream.write(b);
            
            in.close();
        }
        
        // add footer
        outstream.write("</plugins>".getBytes());
    }
}
