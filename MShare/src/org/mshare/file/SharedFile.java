package org.mshare.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * ����TreeNode�Ĺ����У���ҪΪ��ָ��type
 * @author HM
 *
 */
public class SharedFile {
	// ��Ҫ�ļ���ϵ��ӳ�䣬���������Ҫ���ļ�·�������ص�����ʵ���ļ�·��
	// �����������ܻ�ʹ�����߸о�������
	
	static final int TYPE_UNKNOWN = 0x0;
	static final int TYPE_FILE = 0x1;
	static final int TYPE_DIRECTORY = 0x2;
	static final int TYPE_FAKE_DIRECTORY = 0x3;
	
	// ���е�������HashMap������
	private HashMap<String, SharedFile> map = new HashMap<String, SharedFile>();
	// ������·��
	private SharedFile parent = null;
	private String filePath = "";
	private String fileName = "";
	// ��ΪFileֻ��һ����װ������û��ϵ
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
	
	// TODO �������Ĵ������ڴ���Դ
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