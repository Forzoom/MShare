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
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
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
		
		// TODO �޷��жϵ�ǰ���������Ƿ���gridView�е�����
		// ����ÿ������ContextMenu��ʱ�򣬶�����������������ô�Ϳ���ͨ��
		// ItemContainer����õ�ǰ��ѡ����ļ���ʲô
		
		Object tag = v.getTag();
		ItemContainer item = null;
		
		if (tag != null) {
			item = (ItemContainer)tag;
			menu.setHeaderTitle(item.file.getName());
		}
		
		shareActionFile = item.file;
		
		menu.add(GROUP_FILE_BROWSER, 0, 0, "����");
		menu.add(GROUP_FILE_BROWSER, 1, 1, "����");
		menu.add(GROUP_FILE_BROWSER, 2, 2, "ճ��");
		menu.add(GROUP_FILE_BROWSER, 3, 3, "ɾ��");
		
		// TODO �жϵ�ǰ�ļ��Ƿ��ǹ����ļ�����Ҫ���ļ���������ݴ����Ĺ����������ļ��Ƿ��ǹ����
		// ��ʱʹ�����ַ�ʽ���ж��ļ��Ƿ��ǹ����
		if (!item.file.isShared()) {
			// ���ڷǹ�����ļ�����Ҫ���ļ�����Ϊ����״̬
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE, 4, "����");
		} else {
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE, 4, "������");
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		int itemId = item.getItemId();
		
		switch (itemId) {
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE: // ��������ǹ���
				
				// ��õ�ǰ������Ķ���
				// ���б�������ļ�����������SharedLink�ĸ�Ŀ¼��,����Ҫ�����е�SharedLink����������ǰ�����Ƕ���Ĭ���˻����в���
				// TODO ��ǰ������Ĭ���˻���SharedPreferences���в���,���������Ĳ�����Ҫ��FTP���������н���
				// ���Զ���Account�е����ݿ�����Ҫ�޸Ĳ�������Ϊ��̬
				
				Token token = FsService.getToken();
				
				if (!shareActionFile.exists()) {
					// TODO �ļ������ڵ�ʱ����Ҫ�ܹ�ˢ������
					Toast.makeText(this, "�ļ���������", Toast.LENGTH_SHORT).show();
				}
				// �����ļ��������ڸ�Ŀ¼��
				String fakePath = SharedLinkSystem.SEPARATOR + shareActionFile.getName();
				String realPath = shareActionFile.getAbsolutePath();
				FsService.getToken().persist(fakePath, realPath);
				// ��Ҫ��������sessionThread�ļ���
				
				break;
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE: // ������ǲ�����

				// TODO ���е��ļ��������ڸ�Ŀ¼�£����������ܻ����
				// TODO ��Ҫ���ļ����е�����ɾ��
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
