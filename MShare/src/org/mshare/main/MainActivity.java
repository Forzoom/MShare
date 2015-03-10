package org.mshare.main;

import java.io.File;

import org.mshare.file.browser.MShareFile;
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
				return 2;
			}
			// �÷����ķ���ֵ����ÿ��Fragment�ı���
			@Override
			public CharSequence getPageTitle(int position) {
				switch (position) {
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
		Drawable shareIcon = getResources().getDrawable(R.drawable.share);
		Drawable fileIcon = getResources().getDrawable(R.drawable.tab_file);
		Drawable[] icons = new Drawable[2];
		icons[0] = shareIcon;
		icons[1] = fileIcon;
		for (int i = 0; i < pagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab().setText(pagerAdapter.getPageTitle(i)).setIcon(icons[i]).setTabListener(this));
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
		
		// v���������GridView
		menu.add(GROUP_FILE_BROWSER, 0, 0, "����");
		menu.add(GROUP_FILE_BROWSER, 1, 1, "����");
		menu.add(GROUP_FILE_BROWSER, 2, 2, "ճ��");
		menu.add(GROUP_FILE_BROWSER, 3, 3, "ɾ��");
		
		// TODO �жϵ�ǰ�ļ��Ƿ��ǹ����ļ�����Ҫ���ļ���������ݴ����Ĺ����������ļ��Ƿ��ǹ����
		
		if (!fragment.getFileBrowser().getSelectFile().isShared()) {
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

		// ��ù���Ա�˻��������
		// TODO ������û�п�����ʱ�򣬺�����������
		SharedLinkSystem system = null;
		String fakePath = null, realPath = null;
		Token token = FsService.getAdminToken();

		// ��ѡ�е��ļ�
		MShareFile selectFile = fragment.getFileBrowser().getSelectFile();

		
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
		}
		
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
