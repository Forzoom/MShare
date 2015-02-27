package org.mshare.file;

/**
 * 作为realPath的代替品，以便保存更多的内容，而不仅仅是realPath
 * 例如lastModifier现在就不能保存，而且使用SharedPreferences来保存也许并不是一个好的方法
 * @author HM
 *
 */
public class SharedPath {
	// TYPE_FILE fakePath realPath
	// TYPE_DIRECTORY fakePath realPath
	// TYPE_FAKE_DIRECTORY fakePath lastModified
	String fakePath = null;
	String realPath = null;
	long lastModified = 0l;
	
	public SharedPath() {}
}
