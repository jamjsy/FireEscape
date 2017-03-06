package com.ZOE.FireEscape.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.ZOE.FireEscape.R;
import com.ZOE.FireEscape.Utils.ActivityCollector;
import com.ZOE.FireEscape.Utils.PingYinUtil;
import com.ZOE.FireEscape.Utils.PinyinComparator;
import com.ZOE.FireEscape.entity.Info;
import com.ZOE.FireEscape.ui.view.SideBar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContactActivity extends Activity implements OnScrollListener, OnItemClickListener, OnItemLongClickListener {
	/** Called when the activity is first created. */
	private ListView lvContact;
	private SideBar indexBar;
	private WindowManager mWindowManager;
	private TextView txtOverlay; // 用来放在WindowManager中显示提示字符
	protected Map<String, Info> list = new HashMap<String, Info>();
	public static Map<Integer, String> p2s = new HashMap<Integer, String>();
	public static String[] mNicks;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		ActivityCollector.addActivity(this);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		list = getContacts();
		findView();
	}

	private void findView() {
		lvContact = (ListView) this.findViewById(R.id.lvContact);
		lvContact.setOnScrollListener(this);
		lvContact.setOnItemClickListener(this);
		lvContact.setOnItemLongClickListener(this);
		lvContact.setAdapter(new ContactAdapter(this, list));
		indexBar = (SideBar) findViewById(R.id.sideBar);
		indexBar.setListView(lvContact);
		txtOverlay = (TextView) LayoutInflater.from(ContactActivity.this).inflate(R.layout.list_position, null);
		txtOverlay.setVisibility(View.INVISIBLE);
		indexBar.setTextView(txtOverlay);
	}

	@Override
	protected void onDestroy()
	{
		//mWindowManager.removeView(txtOverlay);
		super.onDestroy();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private Map<String, Info> getContacts() {
		// 存放多个电话号码
		String phoneNumbers = "";
		Map<String, Info> map = new HashMap<String, Info>();
		try
		{
			Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			while (cursor.moveToNext())
			{
				String phoneName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (hasPhone.compareTo("1") == 0)
				{
					Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null,
							null);
					while (phones.moveToNext())
					{
						String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						/* 判断是否有多个号码 */
						if (phones.getCount() > 1)
						{
							/* 如果有多个号码以空格 隔开 方便后面取值 */
							phoneNumbers = phoneNumbers + ";" + phoneNumber;
						} else
						{
							phoneNumbers = phoneNumber;
						}

					}
					phones.close();
				}
				map.put(phoneName, new Info(false, phoneNumbers));
			}
			cursor.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return map;
	}

	static class ContactAdapter extends BaseAdapter implements SectionIndexer {
		private Context mContext;

		private Map<String, Info> m = new HashMap<String, Info>();

		@SuppressWarnings("unchecked")
		public ContactAdapter(Context mContext, Map<String, Info> s)
		{
			this.mContext = mContext;
			this.m = s;
			if (m != null)
			{
				Set<String> set = m.keySet();
				mNicks = new String[set.size()];
				set.toArray(mNicks);
			}
			// 排序(实现了中英文混排)
			Arrays.sort(mNicks, new PinyinComparator());
		}

		@Override
		public int getCount() {
			return mNicks.length;
		}

		@Override
		public Object getItem(int position) {
			return mNicks[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int index = position;
			final String nickName = mNicks[index];
			p2s.put(position, nickName);
			ViewHolder viewHolder = null;
			if (convertView == null)
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvCatalog = (TextView) convertView.findViewById(R.id.contactitem_catalog);
				viewHolder.ivAvatar = (ImageView) convertView.findViewById(R.id.contactitem_avatar_iv);
				viewHolder.tvNick = (TextView) convertView.findViewById(R.id.contactitem_nick);
				viewHolder.numbers = (TextView) convertView.findViewById(R.id.numbers);
				viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.contactitem_select_cb);
				viewHolder.checkbox_fl = (FrameLayout) convertView.findViewById(R.id.contactitem_select_cb_fl);
				convertView.setTag(viewHolder);
			} else
			{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			String catalog = null;
			String lastCatalog = null;
			catalog = PingYinUtil.converterToFirstSpell(nickName).substring(0, 1);
			if (position == 0)
			{
				viewHolder.tvCatalog.setVisibility(View.VISIBLE);
				viewHolder.tvCatalog.setText(catalog);
			} else
			{
				lastCatalog = PingYinUtil.converterToFirstSpell(mNicks[index - 1]).substring(0, 1);
				if (catalog.equals(lastCatalog))
				{
					viewHolder.tvCatalog.setVisibility(View.GONE);
				} else
				{
					viewHolder.tvCatalog.setVisibility(View.VISIBLE);
					viewHolder.tvCatalog.setText(catalog);
				}
			}
			viewHolder.checkbox.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v) {
					String nk = (String) v.getTag();
					if (m.get(nk).isIs_Checked())
					{
						m.get(nk).setIs_Checked(false);
					} else
					{
						m.get(nk).setIs_Checked(true);
					}

				}
			});
			viewHolder.checkbox_fl.setOnClickListener(new OnClickListener()

			{

				@Override
				public void onClick(View v) {
					v.findViewById(R.id.contactitem_select_cb).performClick();
				}
			});
			viewHolder.checkbox.setChecked(m.get(nickName).isIs_Checked());
			viewHolder.ivAvatar.setImageResource(R.drawable.default_avatar);
			viewHolder.tvNick.setText(nickName);
			viewHolder.numbers.setText(m.get(nickName).getNumber());
			viewHolder.checkbox.setTag(nickName);
			return convertView;
		}

		static class ViewHolder {
			TextView tvCatalog;// 目录
			ImageView ivAvatar;// 头像
			TextView tvNick;// 昵称
			TextView numbers;// 电话号
			CheckBox checkbox;// 选择框
			FrameLayout checkbox_fl;// 扩大选择框
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < mNicks.length; i++)
			{
				String l = PingYinUtil.converterToFirstSpell(mNicks[i]).substring(0, 1);
				char firstChar = l.toUpperCase().charAt(0);
				if (firstChar == section)
				{
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String telNum = list.get(p2s.get(position)).getNumber();
		if (telNum == null || telNum.trim().equals(""))
		{
			Toast.makeText(this, "电话号为空", Toast.LENGTH_SHORT).show();
		}
		else 
		{
			String[] str = telNum.split(";");
			telNum = str[0];
			Intent telI = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+telNum));
			startActivity(telI);
		}
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		AlertDialog diamondAl = new AlertDialog.Builder(this).setMessage("更多功能")
				.setNegativeButton("确定", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setPositiveButton("取消", new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
		diamondAl.show();
		return false;
	}
}