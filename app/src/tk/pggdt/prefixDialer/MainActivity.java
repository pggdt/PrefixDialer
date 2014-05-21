package tk.pggdt.prefixDialer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tk.pggdt.prefixDialer.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {
    /**联系人名称**/  
    private ArrayList<String> mContactsName = new ArrayList<String>();       
    /**联系人号码**/  
    private ArrayList<String> mContactsNumber = new ArrayList<String>(); 
    /**通话类型**/  
    private ArrayList<String> mContactsType = new ArrayList<String>(); 
    /**通话日期**/  
    private ArrayList<String> mContactsDate = new ArrayList<String>(); 
	private EditText mobileText;
	private EditText pfText;
	public String prefix;
	Context mContext = null;
    ListView mListView = null;  
    //Date date;
    MyLogListAdapter myAdapter = null;
    private String numToDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        pfText = (EditText) findViewById(R.id.pf);
        
        SharedPreferences pref=getSharedPreferences("user", Context.MODE_PRIVATE);
        prefix=pref.getString("prefix", "");
        
        pfText.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {  

            @Override  

            public void onFocusChange(View v, boolean hasFocus) {  

                if(!hasFocus) {
                    prefix = pfText.getText().toString();
                    SharedPreferences preferences=getSharedPreferences("user",Context.MODE_PRIVATE);
                    Editor editor=preferences.edit();
                    editor.putString("prefix", prefix);
                    editor.commit();
                    
		        } else {		
		        // 此处为获取焦点时的处理内容		
		        }
            }
        });
        
        mobileText = (EditText) findViewById(R.id.mobile);
        Button button = (Button) this.findViewById(R.id.button);
        Button button2 = (Button) this.findViewById(R.id.button2);
        pfText.setText(prefix);
        button.setOnClickListener(new ButtonClickListener1());
        button2.setOnClickListener(new ButtonClickListener2());
        mContext = this;
        mListView = (ListView) findViewById(R.id.list_calllog);
        getPhoneLog();
        myAdapter = new MyLogListAdapter(mContext);  
        setListAdapter(myAdapter);
        
 /*      mListView.setOnItemClickListener(new OnItemClickListener() {  
        	  
            @Override  
            public void onItemClick(AdapterView<?> adapterView, View view,  
                int position, long id) {  
            AddCallLog(mContactsNumber.get(position),0);
            //调用系统方法拨打电话  
            Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri  
                .parse("tel:818," + mContactsNumber.get(position)));  
            startActivity(dialIntent); 
    	    DeleteCall();
            }
        }); */
       super.onCreate(savedInstanceState);
    }
    
    
    public final class ButtonClickListener1 implements View.OnClickListener{
		public void onClick(View v) {
			String number = mobileText.getText().toString();
			Intent intent = new Intent();
			intent.setAction("android.intent.action.CALL");
			intent.setData(Uri.parse("tel:"+prefix+ number));
			startActivity(intent);
			Toast.makeText(mContext, getResources().getString(R.string.toast_calling) + prefix+ number,Toast.LENGTH_LONG).show();
			AddCallLog(number,0);
			finish(); 
		}
    }
    
    public final class ButtonClickListener2 implements View.OnClickListener{
		public void onClick(View v) {
			Intent intent1= new Intent();
			intent1.setClass(MainActivity.this, ContactsActivity.class);
			startActivity(intent1);
		}
    }
    

    protected void onListItemClick(ListView l,View v,int position,long id){
    	super.onListItemClick(l, v, position, id);
        //调用系统方法拨打电话  
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri  
            .parse("tel:"+prefix+ mContactsNumber.get(position)));  
        Toast.makeText(mContext, getResources().getString(R.string.toast_calling) +mContactsName.get(position)+" "+prefix+ mContactsNumber.get(position),Toast.LENGTH_LONG).show();
        startActivity(dialIntent); 
        AddCallLog(mContactsNumber.get(position),0);
		finish(); 
    }

    @SuppressLint("SimpleDateFormat")
	private void getPhoneLog() {  
        ContentResolver resolver = mContext.getContentResolver();  
      
        // 获取手机联系人  
        Cursor phoneCursor = resolver.query(CallLog.Calls.CONTENT_URI,new String[]{CallLog.Calls.NUMBER,CallLog.Calls.CACHED_NAME,CallLog.Calls.TYPE, CallLog.Calls.DATE}, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);  
      
      
        if (phoneCursor != null) {  
	            while (phoneCursor.moveToNext()) {  
	      
	            //得到手机号码  
	            String phoneNumber = phoneCursor.getString(0);  
	            //当手机号码为空的或者为空字段 跳过当前循环  
	            if (TextUtils.isEmpty(phoneNumber)){
	                continue;  
	            }
	            if(phoneNumber.startsWith(prefix)&&(prefix.length()>0)){
	            	numToDel=phoneNumber;
	            	DeleteCall();
	            	continue;
	            }
	              
	            //得到联系人名称  
	            String contactName = phoneCursor.getString(1);  
	            
	            //得到通话类型  
	            String contactType="";
	            switch(phoneCursor.getInt(2)){
	            case 1:contactType =getResources().getString(R.string.label_incoming);
	            break;
	            case 2:contactType =getResources().getString(R.string.label_outgoing);
	            break;
	            case 3:contactType =getResources().getString(R.string.label_missed);
	            break;
	            }
	            
	            //得到通话日期
	            SimpleDateFormat sfd = new SimpleDateFormat("MM-dd HH:mm:ss");
	            Date date = new Date(Long.parseLong(phoneCursor.getString(3))); 
	            String time= sfd.format(date); 
	            //得到联系人ID  
//	            Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);  
	      
	            
	              
	            mContactsName.add(contactName);  
	            mContactsNumber.add(phoneNumber);  
	            mContactsType.add(contactType);
	            mContactsDate.add(time);
	            }  
      
            phoneCursor.close();  
        } 
    }    
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch (item.getItemId()) {
            case R.id.action_about:
                // 显示关于页    
            	new AboutDialog(this).show();
                return true;  
            case R.id.action_exit:
                // 退出程序  
                finish();  
                return true;  
            default:  
                //返回False交由系统正常处理菜单，返回True则由本程序处理  
                return false;  
        }  
    } 
    
    //Adaptor
    class MyLogListAdapter extends BaseAdapter {  
	    public MyLogListAdapter(Context context) {  
	        mContext = context;  
	    }  
  
	    public int getCount() {  
	        //设置绘制数量  
	        return mContactsName.size();  
	    }  
	  
	    @Override  
	    public boolean areAllItemsEnabled() {  
	        return false;  
	    }  
	  
	    public Object getItem(int position) {  
	        return position;  
	    }  
	  
	    public long getItemId(int position) {  
	        return position;  
	    }  
	  
	    public View getView(int position, View convertView, ViewGroup parent) {  
	        TextView title = null;  
	        TextView text = null;   
	        TextView date = null;  
	        TextView type = null; 
		        if (convertView == null || position < mContactsNumber.size()) {  
		        convertView = LayoutInflater.from(mContext).inflate(  
		            R.layout.loglist, null);   
		        title = (TextView) convertView.findViewById(R.id.log_title);  
		        text = (TextView) convertView.findViewById(R.id.log_text);  
		        date=(TextView) convertView.findViewById(R.id.log_date); 
		        type=(TextView) convertView.findViewById(R.id.log_type);
		        }  
	        //绘制联系人名称  
	        title.setText(mContactsName.get(position));  
	        //绘制联系人号码  
	        text.setText(mContactsNumber.get(position));  
	        date.setText(mContactsDate.get(position)); 
	        type.setText(mContactsType.get(position)); 
	        
	        return convertView;  
	    }
    }
    
    
    private void DeleteCall(){    	
    	getContentResolver().delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER+"=?" , new String[]{numToDel});
    }
    
    //增加通话记录
    private void AddCallLog(String number,long i){
    	ContentValues content = new ContentValues();
    	content.put(CallLog.Calls.TYPE, CallLog.Calls.OUTGOING_TYPE);
    	content.put(CallLog.Calls.NUMBER,number);
    	content.put(CallLog.Calls.DATE, System.currentTimeMillis()+i);
    	//content.put(CallLog.Calls.NEW, "1");//0已看1未看
    	getContentResolver().insert(CallLog.Calls.CONTENT_URI, content);
    }
    
    
    public class AboutDialog extends AlertDialog {   
        public AboutDialog(Context context) {   
            super(context);
            WebView webview = new WebView(context);
            webview.setOverScrollMode(View.OVER_SCROLL_NEVER);//禁止水平滚动
            webview.loadUrl(getResources().getString(R.string.html_url));//读取asset文件夹内的文件
            setView(webview);   
        }   
    }
    

}
