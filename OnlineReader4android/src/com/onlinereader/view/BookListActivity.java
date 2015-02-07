package com.onlinereader.view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onlinereader.data.AndroidBook;
import com.onlinereader.data.SocketGlobal;
import com.onlinereader.data.SysConstants;

public class BookListActivity extends Activity {
	private ListView lvBookList;
	private MyBookAdapter adapter;
	private ArrayList<AndroidBook> bookList;
	private Socket socket;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	
	private AndroidBook androidBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_list);
		
//		setTitle("Book List");
//		SlidingMenu menu = new SlidingMenu(this);
//		menu.setMode(SlidingMenu.LEFT);
//	    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//	    menu.setShadowWidthRes(R.dimen.shadow_width);
//	    menu.setShadowDrawable(R.drawable.shadow);
//	    menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//	    menu.setFadeDegree(0.35f);
//	    menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
//	    menu.setMenu(R.layout.menu);
		
		lvBookList = (ListView) findViewById(R.id.lv_book_list);
		bookList = (ArrayList<AndroidBook>) getIntent().getSerializableExtra("bookList");
		adapter = new MyBookAdapter();
		
		socket = SocketGlobal.socket;
		toServer = SocketGlobal.toServer;
		fromServer = SocketGlobal.fromServer;
		
		lvBookList.setAdapter(adapter);
		lvBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				androidBook = bookList.get(position);
				queryAndroidBook();
			}

			private void queryAndroidBook() {
				new Thread(){
					public void run() {
						try {
							androidBook.setSituation(SysConstants.QUERY_BOOK_CONTENT);
							toServer.writeUnshared(androidBook);
							toServer.flush();
							androidBook = (AndroidBook) fromServer.readUnshared();
							
							if(androidBook.getSituation() == SysConstants.REQUEST_READ_SUCCESS){
								Intent intent = new Intent(BookListActivity.this, ReadingActivity.class);
								intent.putExtra("content", androidBook.getContent());
								startActivity(intent);
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					};
				}.start();
			}
		});
	}

	class MyBookAdapter extends BaseAdapter {
		RelativeLayout view = null;
		ViewHolder viewHolder = null;

		@Override
		public int getCount() {
			return bookList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				view  = (RelativeLayout) View.inflate(BookListActivity.this, R.layout.book_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvBookName = (TextView) view.findViewById(R.id.tv_book_name);
				viewHolder.tvBookSize = (TextView) view.findViewById(R.id.tv_book_size);
				view.setTag(viewHolder);
			} else{
				view = (RelativeLayout) convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			
			viewHolder.tvBookName.setText(bookList.get(position).getName());
			viewHolder.tvBookSize.setText(bookList.get(position).getSize());
			
			return view;
		}
	}
	
	class ViewHolder {
		public TextView tvBookName;
		public TextView tvBookSize;
	}
}
