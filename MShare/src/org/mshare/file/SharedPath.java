package org.mshare.file;

public class SharedPath {
	// TYPE_FILE fakePath realPath
	// TYPE_DIRECTORY fakePath realPath
	// TYPE_FAKE_DIRECTORY fakePath lastModified
	String fakePath = null;
	String realPath = null;
	long lastModified = 0l;
	
	private SharedPath() {}
	public static SharedPath newFilePath(String fakePath, String realPath) {
		SharedPath sp = new SharedPath();
		sp.fakePath = fakePath;
		sp.realPath = realPath;
		return sp;
	}
	
	public static SharedPath newDirectoryPath(String fakePath, String realPath) {
		SharedPath sp = new SharedPath();
		sp.fakePath = fakePath;
		sp.realPath = realPath;
		return sp;
	}
	
	public static SharedPath newFakeDirectoryPath(String fakePath, long lastModified) {
		SharedPath sp = new SharedPath();
		sp.fakePath = fakePath;
		sp.lastModified = lastModified;
		return sp;
	}
}
