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
		// ��ȡActionBar����
		actionBar = getActionBar();
		// ��ȡViewPager
		viewPager = (ViewPager) findViewById(R.id.pager);
		// ����һ��FragmentPagerAdapter���󣬸ö�����ΪViewPager�ṩ���Fragment
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
			// ��ȡ��positionλ�õ�Fragment
			@Override
			public Fragment getItem(int position)
			{
				Fragment fragment = new DummyFragment();
				Bundle args = new Bundle();
				args.putInt(DummyFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			// �÷����ķ���ֵi������Adapter�ܹ��������ٸ�Fragment
			@Override
			public int getCount()
			{
				return 2;
			}
			// �÷����ķ���ֵ����ÿ��Fragment�ı���
			@Override
			public CharSequence getPageTitle(int position)
			{
				switch (position)
				{
					case 0:
						return "�������";
					case 1:
						return "�����ļ�";
				}
				return null;
			}
		};
		// ����ActionBarʹ��Tab������ʽ
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// ����pagerAdapter������������ȫ��Fragment��
		// ÿ��Fragment��Ӧ����һ��Tab��ǩ
		for (int i = 0; i < pagerAdapter.getCount(); i++)
		{
			actionBar.addTab(actionBar.newTab()
				.setText(pagerAdapter.getPageTitle(i))
				.setTabListener(this));
		}
		// ΪViewPager�������FragmentPagerAdapter
		viewPager.setAdapter(pagerAdapter);  //��
		// ΪViewPager������¼�������
		viewPager.setOnPageChangeListener(
			new ViewPager.SimpleOnPageChangeListener()
			{
				// ��ViewPager��ʾ��Fragment�����ı�ʱ�����÷���
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
		menu.add(0, 0, 0, "����");
		menu.add(0, 1, 1, "����");
		menu.add(0, 2, 2, "ճ��");
		menu.add(0, 3, 3, "ɾ��");
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction)
	{
	}

	// ��ָ��Tab��ѡ��ʱ�����÷���
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction)
	{
		viewPager.setCurrentItem(tab.getPosition());  //��
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction)
	{
	}
}
