package org.mshare.file;

/**
 * ��ΪrealPath�Ĵ���Ʒ���Ա㱣���������ݣ�����������realPath
 * ����lastModifier���ھͲ��ܱ��棬����ʹ��SharedPreferences������Ҳ������һ���õķ���
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
