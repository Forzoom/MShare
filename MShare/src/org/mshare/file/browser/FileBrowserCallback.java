package org.mshare.file.browser;

public interface FileBrowserCallback {

	public void onBackButtonClick();
	
	public void onItemClick(FileBrowserFile file);
	
	// �������LongClick������
	public void onItemLongClick(FileBrowserFile file);
	
}
