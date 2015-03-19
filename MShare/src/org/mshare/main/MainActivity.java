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
		// ��ȡActionBar����
		actionBar = getActionBar();
		// ��ȡViewPager
		viewPager = (ViewPager) findViewById(R.id.pager);
		// ����һ��FragmentPagerAdapter���󣬸ö�����ΪViewPager�ṩ���Fragment
		newconn = (Button)findViewById(R.id.newconn);
		FragmentPagerAdapter pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
		{
			// ��ȡ��positionλ�õ�Fragment
			@Override
			public Fragment getItem(int position) {
				fragment = new DummyFragment();
				Bundle args = new Bundle();
				args.putInt(DummyFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			/**
			 * �÷����ķ���ֵi������Adapter�ܹ��������ٸ�Fragment
			 */
			@Override
			public int getCount() {
				return 1;
			}
			// �÷����ķ���ֵ����ÿ��Fragment�ı���
			@Override
			public CharSequence getPageTitle(int position) {
				switch (position) {
					case 0:
						return "�������";
				}
				return null;
			}
		};
		// ����ActionBarʹ��Tab������ʽ
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// ����pagerAdapter������������ȫ��Fragment��
		// ÿ��Fragment��Ӧ����һ��Tab��ǩ
		for (int i = 0; i < pagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab().setText(pagerAdapter.getPageTitle(i)).setTabListener(this));
		}
		
		// ΪViewPager�������FragmentPagerAdapter
		viewPager.setAdapter(pagerAdapter);  //��
		// ΪViewPager������¼�������
		viewPager.setOnPageChangeListener(
			new ViewPager.SimpleOnPageChangeListener() {
				// ��ViewPager��ʾ��Fragment�����ı�ʱ�����÷���
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
		//��ѡ�е��ļ�
		LocalBrowserFile selectFile = fragment.getFileBrowser().getSelectFile();
		// v���������GridView
		if(mshareFileManage.getSelected() && !selectFile.isFile()) {
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+4, 3, "ճ��");
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+5, 4, "ȡ��");
		} else {
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+1, 0, "����");
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+2, 1, "����");
			menu.add(GROUP_FILE_BROWSER, Menu.FIRST+3, 2, "ɾ��");
		}
		
		
		
		// TODO �жϵ�ǰ�ļ��Ƿ��ǹ����ļ�����Ҫ���ļ���������ݴ����Ĺ����������ļ��Ƿ��ǹ����
		
		if (!fragment.getFileBrowser().getSelectFile().isShared()) {
			// ���ڷǹ�����ļ�����Ҫ���ļ�����Ϊ����״̬
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE, 4, "����");
		} else {
			menu.add(GROUP_FILE_BROWSER, MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE, 4, "������");
		}
		
		//super.onCreateContextMenu(menu, v, menuInfo);
		*/
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		/*
		int itemId = item.getItemId();

		// ��ù���Ա�˻��������

		// TODO ������û�п�����ʱ�򣬺�����������
		SharedLinkSystem system = null;
		String fakePath = null, realPath = null;
		Token token = FsService.getAdminToken();

		// ��ѡ�е��ļ�
		LocalBrowserFile selectFile = fragment.getFileBrowser().getSelectFile();
		//��ѡ���ļ���·��,�ļ���
		String path = selectFile.getPath();
		String name = selectFile.getName();

		
		switch (itemId) {
			
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_SHARE: // ��������ǹ���
				
				// ��õ�ǰ������Ķ���
				// ���б�������ļ�����������SharedLink�ĸ�Ŀ¼��,����Ҫ�����е�SharedLink����������ǰ�����Ƕ���Ĭ���˻����в���
				// TODO ��ǰ������Ĭ���˻���SharedPreferences���в���,���������Ĳ�����Ҫ��FTP���������н���

				if (!selectFile.exists()) {
					// TODO �ļ������ڵ�ʱ����Ҫ�ܹ�ˢ������
					Toast.makeText(this, "�ļ���������", Toast.LENGTH_SHORT).show();
				}
				//�����ļ��������ڸ�Ŀ¼��
				fakePath = SharedLinkSystem.SEPARATOR + selectFile.getName();
				realPath = selectFile.getAbsolutePath();
				if (token.isValid()) {
					system = token.getSystem();
					// �������
					SharedLink sharedLink = SharedLink.newSharedLink(fakePath, realPath);
					system.persist(sharedLink);
					system.addSharedLink(sharedLink, SharedLinkSystem.FILE_PERMISSION_ADMIN);
				}
				
				break;
			case MShareFileBrowser.CONTEXT_MENU_ITEM_ID_UNSHARE: // ������ǲ�����

				// TODO ���е��ļ��������ڸ�Ŀ¼�£����������ܻ����
				// TODO ��Ҫ���ļ����е�����ɾ��
				// TODO ��β������ļ��������
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
	 *  ��ָ��Tab��ѡ��ʱ�����÷���
	 */
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
	
	
}
