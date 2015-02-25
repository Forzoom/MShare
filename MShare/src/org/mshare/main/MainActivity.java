package org.mshare.main;

import java.io.File;

import org.mshare.file.FileAdapter.ItemContainer;
import org.mshare.file.MShareFileBrowser;
import org.mshare.file.SharedLinkSystem;
import org.mshare.ftp.server.Account;
import org.mshare.ftp.server.AccountFactory;
import org.mshare.ftp.server.AccountFactory.Token;
import org.mshare.ftp.server.FsService;
import org.mshare.main.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
//import android.view.View.OnClickListener; 
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
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
public class MainActivity extends FragmentActivity
	implements ActionBar.TabListener
{
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int GROUP_FILE_BROWSER = 1;
	ViewPager viewPager;
	ActionBar actionBar;
	Button newconn,joinconn;
	
	private File shareActionFile = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 获取ActionBar对象
		actionBar = getActionBar();
		// 获取ViewPager
		viewPager = (ViewPager) findViewById(R.id.pager);
		// 创建一个FragmentPagerAdapter对象，该对象负责为ViewPager提供多个Fragment
		newconn = (Button)findViewById(R.id.newconn);
//		newconn.setOnClickListener(new android.view.View.OnClickListener() {
//			
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(MainActivity.this
//						,NewConn.class);
//				startActivity(intent);
//			}
//		});
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{
			// 获取第position位置的Fragment
			@Override
			public Fragment getItem(int position)
			{
				Fragment fragment = new DummyFragment();
				Bundle args = new Bundle();
				args.putInt(DummyFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			// 该方法的返回值i表明该Adapter总共包括多少个Fragment
			@Override
			public int getCount()
			{
				return 2;
			}
			// 该方法的返回值决定每个Fragment的标题
			@Override
			public CharSequence getPageTitle(int position)
			{
				switch (position)
				{
					case 0:
						return "共享服务";
					case 1:
						return "本地文件";
				}
				return null;
			}
		};
		// 设置ActionBar使用Tab导航方式
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// 遍历pagerAdapter对象所包含的全部Fragment。
		// 每个Fragment对应创建一个Tab标签
		for (int i = 0; i < pagerAdapter.getCount(); i++)
		{
			actionBar.addTab(actionBar.newTab()
				.setText(pagerAdapter.getPageTitle(i))
				.setTabListener(this));
		}
		// 为ViewPager组件设置FragmentPagerAdapter
		viewPager.setAdapter(pagerAdapter);  //①
		// 为ViewPager组件绑定事件监听器
		viewPager.setOnPageChangeListener(
			new ViewPager.SimpleOnPageChangeListener()
			{
				// 当ViewPager显示的Fragment发生改变时激发该方法
				@Override
				public void onPageSelected(int position)
				{
					actionBar.setSelectedNavigationItem(position);
				}
			});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		// TODO 无法判断当前所长按的是否是gridView中的内容
		// 好像每次生成ContextMenu的时候，都会调用这个方法，那么就可以通过
		// ItemContainer来获得当前所选择的文件是什么
		
		Object tag = v.getTag();
		ItemContainer item = null;
		
		if (tag != null) {
			item = (ItemContainer)tag;
			menu.setHeaderTitle(item.file.getName());
		}
		
		shareActionFile = item.file;
		
		menu.add(GROUP_FILE_BROWSER, 0, 0, "剪切");
		menu.add(GROUP_FILE_BROWSER, 1, 1, "复制");
		menu.add(GROUP_FILE_BROWSER, 2, 2, "粘贴");
		menu.add(GROUP_FILE_BROWSER, 3, 3, "删除");
		
		// TODO 判断当前文件是否是共享文件，需要在文件浏览器内容创建的过程中设置文件是否是共享的
		// 暂时使用这种方式来判定文件是否是共享的
		if (!item.file.isShared()) {
			// 对于非共享的文件，需要将文件设置为共享状态
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE, 4, "共享");
		} else {
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE, 4, "不共享");
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		int itemId = item.getItemId();
		
		switch (itemId) {
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE: // 当点击的是共享
				
				// 获得当前被点击的对象
				// 所有被共享的文件都将保存在SharedLink的根目录下,这需要对所有的SharedLink都动作？当前仅仅是对于默认账户进行操作
				// TODO 当前仅仅对默认账户的SharedPreferences进行操作,但是这样的操作需要和FTP服务器进行交互
				// 所以对于Account中的内容可能需要修改部分内容为静态
				
				Token token = FsService.getToken();
				
				if (!shareActionFile.exists()) {
					// TODO 文件不存在的时候，需要能够刷新内容
					Toast.makeText(this, "文件名不存在", Toast.LENGTH_SHORT).show();
				}
				// 共享文件将放置在根目录下
				String fakePath = SharedLinkSystem.SEPARATOR + shareActionFile.getName();
				String realPath = shareActionFile.getAbsolutePath();
				FsService.getToken().persist(fakePath, realPath);
				// 需要操作所有sessionThread文件树
				
				break;
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE: // 点击的是不共享

				// TODO 所有的文件都假设在根目录下，但这样可能会出错
				// TODO 需要将文件树中的内容删除
				FsService.getToken().unpersist(SharedLinkSystem.SEPARATOR + shareActionFile.getName());
				
				break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction)
	{
	}

	// 当指定Tab被选中时激发该方法
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction)
	{
		viewPager.setCurrentItem(tab.getPosition());  //②
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction)
	{
	}
}
