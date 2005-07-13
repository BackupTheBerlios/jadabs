/*
 * Created on Jan 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.common.pc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.ioapi.IOProperty;


public class FileIOProperty implements IOProperty
{
    private static Logger LOG = Logger.getLogger(FileIOProperty.class.getName());
    
	private String path;
	private Properties p;
	
	public FileIOProperty(String path) {
        this.path = path; 
        p = new Properties();
        load();
	}
	
	public FileIOProperty() {
	    
	}
	
	public void setPath(String path) {
        this.path = path; 
        p = new Properties();
        load();
	}
	
	public void load() {
		try {
            p.load(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            try {
                new FileWriter(path);
                p.load(new FileInputStream(path));
            } catch (IOException e1) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            LOG.error("error in reading from: "+path);
        }
	}
	
    public void save(String comment) {
        try {
            p.store(new FileOutputStream(path), comment);
        } catch (FileNotFoundException e) {
            LOG.error("file not found: "+path);
        } catch (IOException e) {
            LOG.error("could not save to: "+path);
        }
    }
	
	public String getProperty(String property, String def) {
		return p.getProperty(property, def);
	}

	public void setProperty(String property, String value) {
		p.setProperty(property, value);
	}

	public void clear() {
		p.clear();
	}
	
	
}

