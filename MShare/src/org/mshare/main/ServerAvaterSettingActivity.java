package org.mshare.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ServerAvaterSettingActivity extends Activity {
	private static final String TAG = ServerAvaterSettingActivity.class.getSimpleName();

	private GridView gridView;

	// ������
	private int selectPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_avater_setting);

		// Ĭ����ѡ���position��0
		selectPosition = 0;

		// ������Ҫ��ʾ��ͷ��ͼƬԤ�ȼ��أ�����������ز����ã�������ɿ���
		Bitmap[] bitmaps = new Bitmap[2];
		bitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.avater);
		bitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.avater_1);

		gridView = (GridView)findViewById(R.id.server_avater_setting_grid_view);
		gridView.setOnItemClickListener(new ServerAvaterItemClickListener());
		ServerAvaterSettingAdapter adapter = new ServerAvaterSettingAdapter(bitmaps);
		gridView.setAdapter(adapter);
	}

	private class ServerAvaterSettingAdapter extends BaseAdapter {

		// ��ʱʹ��id����ʾ��Ҫ��ʾ������

		Bitmap[] bitmaps;

		public ServerAvaterSettingAdapter(Bitmap[] bitmaps) {
			this.bitmaps = bitmaps;
			// ������Ӧ������
		}

		@Override
		public int getCount() {
			return bitmaps.length;
		}

		@Override
		public Object getItem(int position) {
			return bitmaps[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// ��Ҫ��֤��Ӧ��Bitmap���Ǻ��ʵĳߴ�����ݣ���ô����ô���أ�

			Bitmap targetBitmap = bitmaps[position];
			// �ж��Ƿ���Ҫ��Bitmap���д���

			if (position == selectPosition) {
//				Bitmap bitmapSelected = Bitmap.createBitmap()
			}

			if (convertView != null) {

				ServerAvaterTag tag = (ServerAvaterTag)convertView.getTag();
				tag.avater.setImageBitmap(bitmaps[position]);
				tag.avater.setAvaterSelected(position == selectPosition);

			} else {
				convertView = LayoutInflater.from(ServerAvaterSettingActivity.this).inflate(R.layout.server_avater_setting_item, null);

				ServerAvaterImageView thumbnail = (ServerAvaterImageView)convertView.findViewById(R.id.server_avater_setting_item_image);
				thumbnail.setImageBitmap(bitmaps[position]);
				thumbnail.setAvaterSelected(position == selectPosition);
				// ��������
				thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);

				ServerAvaterTag tag = new ServerAvaterTag();
				tag.avater = thumbnail;

				convertView.setTag(tag);
			}


			// ��Ҫ�Բ�ͬ��Bitmap���д���

			return convertView;
		}
	}

	/**
	 * ��ǰ�ĵ�����
	 */
	private class ServerAvaterItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// ��Ӧѡ�������
			selectPosition = position;


			// ����view�Ƿ���ImageView
			if (view instanceof ServerAvaterImageView) {
				Log.e(TAG, "special tag, removed later! the view is a imageView, nice!");
			}
		}
	}

	private class ServerAvaterTag {
		private ServerAvaterImageView avater;
	}
}
