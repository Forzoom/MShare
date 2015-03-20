package org.mshare.file.browser;

public interface FileBrowserCallback {

	public void onCrumbClick(FileBrowserFile file);
	
	public void onBackButtonClick(FileBrowserFile file);
	
	public void onItemClick(FileBrowserFile file);
	
	/**
	 * 不要求刷新
	 * @param file
	 */
	public void onItemLongClick(FileBrowserFile file);
	
	// 用来指明当前没有file被selected
	public void onGridViewClick();
	// 传出当前的文件夹
	public void onRefreshButtonClick(FileBrowserFile file);
}
