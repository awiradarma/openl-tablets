/**
 *  OpenL Tablets,  2006
 *  https://sourceforge.net/projects/openl-tablets/ 
 */
package org.openl.rules.ui;

import org.openl.rules.webtools.FileTypeHelper;
import org.openl.rules.webtools.indexer.FileIndexer;
import org.openl.rules.webstudio.util.WebstudioTreeIterator;
import org.openl.util.Log;
import org.openl.util.TreeIterator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author snshor
 *
 */
public class ProjectIndexer extends FileIndexer
{
	String projectRoot;
	
	public ProjectIndexer(String projectRoot)
	{
		this.projectRoot = projectRoot;
		
		loadFiles();
	}
	
	
	synchronized void  loadFiles()
	{
		TreeIterator<File> fti = new WebstudioTreeIterator(new File(projectRoot), 0);
		
		HashMap<String, String> m = new HashMap<String, String>();
	    for (; fti.hasNext();) {
			File f = fti.next();
			String fileName = f.getName();
			if (FileTypeHelper.isExcelFile(fileName) || FileTypeHelper.isWordFile(fileName))
				try {
					String cp = f.getCanonicalPath(); 
					
					String name = f.getName();
					String existing = m.get(name);
					
					String preferrable = selectPreferrablePath(existing, cp);
					
					m.put(name, preferrable);
			} catch (IOException e) {
					e.printStackTrace();
				}
			
		}
	    
	    
	    
	    String[] res = new String[m.size()];
	    
	    Iterator<String> it = m.values().iterator();
	    for (int i = 0; i < res.length; i++) {
			res[i] = it.next();
		}

	    
	    setFiles(res);
		
	}


	/**
	 * @param existing
	 * @param cp
	 * @return
	 */
	private String selectPreferrablePath(String existing, String cp) 
	{
		if (existing == null)
			return cp;
		int i1 = findMaxIndexInPath(existing);
		int i2 = findMaxIndexInPath(cp);
		if (i1 == i2)
		{
			Log.warn("Two files with the same name: \n" + existing + "\n" + cp);
		}
		
		return i1 < i2 ? existing : cp;
	}

	int findMaxIndexInPath(String path)
	{
		int idx1 = path.lastIndexOf("bin");
		int idx2 = path.lastIndexOf("classes");
		return Math.max(idx1, idx2);
	}
	

	public void reset() 
	{
		loadFiles();
	}
	
	

}
