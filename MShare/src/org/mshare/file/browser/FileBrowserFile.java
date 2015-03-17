package org.mshare.file.browser;

public interface FileBrowserFile {

	public boolean isFile();
	
	public boolean isDirectory();
	
	public String getName();
	
	public String getAbsolutePath();
	
	public boolean canRead();
	
	public boolean canWrite();
}
