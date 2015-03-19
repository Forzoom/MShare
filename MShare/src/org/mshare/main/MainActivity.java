package org.mshare.main;

import java.io.File;

import org.mshare.file.MshareFileManage;
import org.mshare.file.browser.LocalBrowserFile;
import org.mshare.file.browser.MShareFileBrowser;
import org.mshare.file.browser.MShareFileAdapter.ItemContainer;
import org.mshare.file.share.SharedLink;
import org.mshare.file.share.SharedLinkSystem;
import org.mshare.ftp.server.Account;
import org.mshare.ftp.server.AccountFactory;
import org.mshare.ftp.server.AccountFactory.Token;
import org.mshare.ftp.server.FsService;
import org.mshare.main.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
//import android.view.View.OnClickListener; 
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author 
 * @version 
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final int GROUP_FILE_BROWSER = 1;
	MshareFileManage mshareFileManage;
	ViewPager viewPager;
	ActionBar actionBar;
	Button newconn,joinconn;
	
	/**
	 * 
	 */
	DummyFragment fragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mshareFileManage = new MshareFileManage();
		mshareFileManage.setContext(this);
		// 获取ActionBar对象
		actionBar = getActionBar();
		// 获取ViewPager
		viewPager = (ViewPager) findViewById(R.id.pager);
		// 创建一个FragmentPagerAdapter对象，该对象负责为ViewPager提供多个Fragment
		newconn = (Button)findViewById(R.id.newconn);
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{
			// 获取第position位置的Fragment
			@Override
			public Fragment getItem(int position) {
				fragment = new DummyFragment();
				Bundle args = new Bundle();
				args.putInt(DummyFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			/**
			 * 该方法的返回值i表明该Adapter总共包括多少个Fragment
			 */
			@Override
			public int getCount() {
				return 1;
			}
			// 该方法的返回值决定每个Fragment的标题
			@Override
			public CharSequence getPageTitle(int position) {
				switch (position) {
					case 0:
						return "共享服务";
				}
				return null;
			}
		};
		// 设置ActionBar使用Tab导航方式
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// 遍历pagerAdapter对象所包含的全部Fragment。
		// 每个Fragment对应创建一个Tab标签
		for (int i = 0; i < pagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab().setText(pagerAdapter.getPageTitle(i)).setTabListener(this));
		}
		
		// 为ViewPager组件设置FragmentPagerAdapter
		viewPager.setAdapter(pagerAdapter);  //①
		// 为ViewPager组件绑定事件监听器
		viewPager.setOnPageChangeListener(
			new ViewPager.SimpleOnPageChangeListener() {
				// 当ViewPager显示的Fragment发生改变时激发该方法
				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);
				}
			});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		/*
		//被选中的文件
		LocalBrowserFile selectFile = fragment.getFileBrowser().getSelectFile();
		// v所代表的是GridView
		if(mshareFileManage.getSelected() && !selectFile.isFile()) {
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+4, 3, "粘贴");
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+5, 4, "取消");
		} else {
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+1, 0, "剪切");
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+2, 1, "复制");
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+3, 2, "删除");
		}
		
		
		
		// TODO 判断当前文件是否是共享文件，需要在文件浏览器内容创建的过程中设置文件是否是共享的
		
		if (!fragment.getFileBrowser().getSelectFile().isShared()) {
			// 对于非共享的文件，需要将文件设置为共享状态
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE, 4, "共享");
		} else {
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE, 4, "不共享");
		}
		
		//super.onCreateContextMenu(menu, v, menuInfo);
		*/
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		/*
		int itemId = item.getItemId();

		// 获得管理员账户相关内容

		// TODO 服务器没有开启的时候，好像会出现问题
		SharedLinkSystem system = null;
		String fakePath = null, realPath = null;
		Token token = FsService.getAdminToken();

		// 被选中的文件
		LocalBrowserFile selectFile = fragment.getFileBrowser().getSelectFile();
		//被选中文件的路径,文件名
		String path = selectFile.getPath();
		String name = selectFile.getName();

		
		switch (itemId) {
			
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE: // 当点击的是共享
				
				// 获得当前被点击的对象
				// 所有被共享的文件都将保存在SharedLink的根目录下,这需要对所有的SharedLink都动作？当前仅仅是对于默认账户进行操作
				// TODO 当前仅仅对默认账户的SharedPreferences进行操作,但是这样的操作需要和FTP服务器进行交互

				if (!selectFile.exists()) {
					// TODO 文件不存在的时候，需要能够刷新内容
					Toast.makeText(this, "文件名不存在", Toast.LENGTH_SHORT).show();
				}
				//共享文件将放置在根目录下
				fakePath = SharedLinkSystem.SEPARATOR + selectFile.getName();
				realPath = selectFile.getAbsolutePath();
				if (token.isValid()) {
					system = token.getSystem();
					// 添加内容
					SharedLink sharedLink = SharedLink.newSharedLink(fakePath, realPath);
					system.persist(sharedLink);
					system.addSharedLink(sharedLink, SharedLinkSystem.FILE_PERMISSION_ADMIN);
				}
				
				break;
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE: // 点击的是不共享

				// TODO 所有的文件都假设在根目录下，但这样可能会出错
				// TODO 需要将文件树中的内容删除
				// TODO 如何才能在文件浏览器中
				if (token.isValid()) {
					system = token.getSystem();
					fakePath = SharedLinkSystem.SEPARATOR + selectFile.getName();
					system.unpersist(fakePath);
					system.deleteSharedLink(fakePath);
				}

				break;
			
			case Menu.FIRST+1:
				mshareFileManage.copySelect(path, name, true);
				break;
			case Menu.FIRST+2:
				mshareFileManage.copySelect(path, name, false);
				break;
			case Menu.FIRST+3:
				mshareFileManage.deleteAll(path);
				fragment.getFileBrowser().refresh();
				break;
			case Menu.FIRST+4:
				mshareFileManage.paste(path);
				fragment.getFileBrowser().refresh();
				break;
			case Menu.FIRST+5:
				mshareFileManage.copyCancel();
				break;
			default:
				break;
		}
		*/
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

	/**
	 *  当指定Tab被选中时激发该方法
	 */
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
	
	
}
