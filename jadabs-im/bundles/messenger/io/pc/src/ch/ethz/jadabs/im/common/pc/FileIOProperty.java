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

import ch.ethz.jadabs.api.IOProperty;


public class FileIOProperty implements IOProperty
{
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
            e.printStackTrace();
        }
	}
	
    public void save(String comment) {
        try {
            p.store(new FileOutputStream(path), comment);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

