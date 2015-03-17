package org.mshare.file.browser;

public interface FileBrowserCallback {

	public void onBackButtonClick();
	
	public void onItemClick(FileBrowserFile file);
	
	// 用来获得LongClick的内容
	public void onItemLongClick(FileBrowserFile file);
	
}
