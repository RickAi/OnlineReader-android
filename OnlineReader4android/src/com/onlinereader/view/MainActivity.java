package com.onlinereader.view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onlinereader.data.AndroidBook;
import com.onlinereader.data.AndroidUser;
import com.onlinereader.data.SocketGlobal;
import com.onlinereader.data.SysConstants;


public class MainActivity extends Activity implements OnClickListener{
	private Socket socket;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	
	private EditText etUsername;
	private EditText etPassword;
	private Button btnRegister;
	private Button btnLogin;
	
	private AndroidUser androidUser;
	private ArrayList<AndroidBook> bookList;
	private MyHandler handler;
	private Message msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        init();
        setListeners();
    }

	private void setListeners() {
		btnLogin.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
	}

	private void init() {
		etUsername = (EditText) findViewById(R.id.et_username);
		etPassword = (EditText) findViewById(R.id.et_password);
		btnRegister = (Button) findViewById(R.id.btn_register);
		btnLogin = (Button) findViewById(R.id.btn_login);
		
		androidUser = new AndroidUser();
	}

	@Override
	public void onClick(View view) {
		if(judegeInput(etUsername, etPassword)){
			return ;
		}
		
		androidUser.setUsername(etUsername.getText().toString());
		androidUser.setPassword(etPassword.getText().toString());
		androidUser.setSituation(SysConstants.LOG_IN);
		
		handler = new MyHandler();
		identityInfo();
	}


	private void identityInfo() {
		new Thread(){
			public void run() {
				try {
					socket = new Socket(SysConstants.CONNECTION_ADDRESS, SysConstants.CONNECTION_PORT);
					toServer = new ObjectOutputStream(socket.getOutputStream());
					fromServer = new ObjectInputStream(socket.getInputStream());
					
					SocketGlobal.socket = socket;
					SocketGlobal.toServer = toServer;
					SocketGlobal.fromServer = fromServer;
					
					toServer.writeUnshared(androidUser);
					toServer.flush();
					androidUser = (AndroidUser) fromServer.readObject();
					msg = new Message();
					msg.what = androidUser.getSituation();
					handler.sendMessage(msg);
					
					if(androidUser.getSituation() == SysConstants.LOG_IN_SUCCESS){
						androidUser.setSituation(SysConstants.INIT_BOOK_LIST);
						toServer.writeUnshared(androidUser);
						toServer.flush();
						bookList = (ArrayList<AndroidBook>) fromServer.readObject();
						androidUser = (AndroidUser) fromServer.readObject();
						msg = new Message();
						msg.what = androidUser.getSituation();
						handler.sendMessage(msg);
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} 
			};
		}.start();
	}

	protected void closeConnection() {
		try {
			toServer.close();
			fromServer.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean judegeInput(EditText etUsername, EditText etPassword) {
		if(TextUtils.isEmpty(etUsername.getText().toString())
				|| TextUtils.isEmpty(etPassword.getText().toString())){
			Toast.makeText(this, "The username or password cannot be empty!", Toast.LENGTH_LONG).show();
			return true;
		} 
		
		return false;
	}
	
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == SysConstants.LOG_IN_SUCCESS){
				Toast.makeText(MainActivity.this, "Login success!", Toast.LENGTH_LONG).show();
			} else if(msg.what == SysConstants.LOG_IN_FAIL){
				Toast.makeText(MainActivity.this, "Login fail!", Toast.LENGTH_LONG).show();
			} else if(msg.what == SysConstants.INIT_BOOK_LIST){
				Intent intent = new Intent(MainActivity.this, BookListActivity.class);
				intent.putExtra("bookList", bookList);
				startActivity(intent);
			}
			
			super.handleMessage(msg);
		}
		
	}
    
}
