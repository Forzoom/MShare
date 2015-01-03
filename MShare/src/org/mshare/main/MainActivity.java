package org.mshare.main;

import org.mshare.file.FileAdapter.ItemContainer;
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
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.TableLayout;

/**
 * 
 * @author 
 * @version 
 */
public class MainActivity extends FragmentActivity
	implements ActionBar.TabListener
{
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
		Object tag = v.getTag();
		if (tag != null) {
			ItemContainer item = (ItemContainer)tag;
			menu.setHeaderTitle(item.file.getName());
		}
		menu.add(0, 0, 0, "剪切");
		menu.add(0, 1, 1, "复制");
		menu.add(0, 2, 2, "粘贴");
		menu.add(0, 3, 3, "删除");
		super.onCreateContextMenu(menu, v, menuInfo);
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
