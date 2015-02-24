package org.mshare.file;

public class SharedPath {
	// TYPE_FILE fakePath realPath
	// TYPE_DIRECTORY fakePath realPath
	// TYPE_FAKE_DIRECTORY fakePath lastModified
	String fakePath = null;
	String realPath = null;
	long lastModified = 0l;
	
	public SharedPath() {}
}
