package org.mshare.file.browser;

public interface FileBrowserCallback {

	public void onCrumbClick(FileBrowserFile file);
	
	public void onBackButtonClick(FileBrowserFile file);
	
	public void onItemClick(FileBrowserFile file);
	
	// �������LongClick������
	public void onItemLongClick(FileBrowserFile file);
	
	// ����ָ����ǰû��file��selected
	public void onGridViewClick();
	// ������ǰ���ļ���
	public void onRefreshButtonClick(FileBrowserFile file);
}
