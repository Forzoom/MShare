package org.mshare.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * 创建TreeNode的过程中，需要为其指定type
 * @author HM
 *
 */
public class SharedFile {
	// 需要文件关系的映射，输入的是需要的文件路径，返回的是真实的文件路径
	// 但是这样可能会使调用者感觉到困难
	
	static final int TYPE_UNKNOWN = 0x0;
	static final int TYPE_FILE = 0x1;
	static final int TYPE_DIRECTORY = 0x2;
	static final int TYPE_FAKE_DIRECTORY = 0x3;
	
	// 所有的子树用HashMap来包含
	private HashMap<String, SharedFile> map = new HashMap<String, SharedFile>();
	// 完整的路径
	private SharedFile parent = null;
	private String filePath = "";
	private String fileName = "";
	// 因为File只是一个包装，所以没关系
	private String realPath = "";
	int type = TYPE_UNKNOWN;

	SharedFile(String realPath, String filePath, String fileName) {
		this.realPath = realPath;
		this.filePath = filePath;
		this.fileName = fileName; 
	}
	
	void setType(int type) {
		this.type = type;
	}
	
	boolean isFile() {
		return type == TYPE_FILE;
	}
	
	boolean isDirectory() {
		return type == TYPE_DIRECTORY;
	}
	
	HashMap<String, SharedFile> list() {
		if (isDirectory()) {
			return map;
		} else {
			return null;
		}
	}
	
	public String getAbsolutePath() {
		return filePath;
	}
	
	// TODO 可能消耗大量的内存资源
	void print() {
		if (isDirectory()) {
			System.out.println("(" + fileName);
			Set<String> set = map.keySet();
			Iterator<String> iterator = set.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				map.get(key).print();
			}
			System.out.println(")");
		} else if (isFile()) {
			System.out.println(fileName);
		}
		
	}
}