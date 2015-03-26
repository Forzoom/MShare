package org.mshare.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_avater_setting);

		// 将所需要显示的头像图片预先加载，放在这里加载并不好，容易造成卡顿
		Bitmap[] bitmaps = new Bitmap[2];
		bitmaps[0] = BitmapFactory.decodeResource(getResources(), R.drawable.avater);
		bitmaps[1] = BitmapFactory.decodeResource(getResources(), R.drawable.avater_1);

		gridView = (GridView)findViewById(R.id.server_avater_setting_grid_view);
		gridView.setOnItemClickListener(new ServerAvaterItemClickListener());
		ServerAvaterSettingAdapter adapter = new ServerAvaterSettingAdapter(bitmaps);
		gridView.setAdapter(adapter);
	}

	private class ServerAvaterSettingAdapter extends BaseAdapter {

		// 暂时使用id来表示需要显示的内容

		Bitmap[] bitmaps;

		public ServerAvaterSettingAdapter(Bitmap[] bitmaps) {
			this.bitmaps = bitmaps;
			// 创建对应的数据
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
			if (convertView != null) {

				ServerAvaterTag tag = (ServerAvaterTag)convertView.getTag();
				tag.imageView.setImageBitmap(bitmaps[position]);

			} else {
				convertView = LayoutInflater.from(ServerAvaterSettingActivity.this).inflate(R.layout.server_avater_setting_item, null);

				ImageView thumbnail = (ImageView)convertView.findViewById(R.id.server_avater_setting_item_image);
				thumbnail.setImageBitmap(bitmaps[position]);
				// 居中铺满
				thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

				ServerAvaterTag tag = new ServerAvaterTag();
				tag.imageView = thumbnail;

				convertView.setTag(tag);
			}
			return convertView;
		}
	}

	private class ServerAvaterItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// 对应选择的内容
		}
	}

	private class ServerAvaterTag {
		private ImageView imageView;
	}
}
