package org.mshare.main;

import org.mshare.file.FileAdapter.ItemContainer;
import org.mshare.file.MShareFileBrowser;
import org.mshare.main.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(
				getSupportFragmentManager())
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
		
		// TODO 需要判断长按的是否是gridView中的内容，使用instance判断v是否是ItemContainer的实例
		
		RuntimeException e = new RuntimeException("for print stack and heap");
		e.fillInStackTrace();
		e.printStackTrace();
		
		Object tag = v.getTag();
		ItemContainer item = null;
		
		if (tag != null) {
			item = (ItemContainer)tag;
			menu.setHeaderTitle(item.file.getName());
		}
		
		menu.add(GROUP_FILE_BROWSER, 0, 0, "剪切");
		menu.add(GROUP_FILE_BROWSER, 1, 1, "复制");
		menu.add(GROUP_FILE_BROWSER, 2, 2, "粘贴");
		menu.add(GROUP_FILE_BROWSER, 3, 3, "删除");
		
		// 判断当前文件是否是共享文件
		if (!item.file.isShared()) {
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
				
				
				break;
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE: // 点击的是不共享
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
