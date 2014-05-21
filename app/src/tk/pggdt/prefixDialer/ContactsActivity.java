package tk.pggdt.prefixDialer;

import java.io.InputStream;
import java.util.ArrayList;

import tk.pggdt.prefixDialer.R;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
  
public class ContactsActivity extends ListActivity {  
  
    Context mContext = null;  
  
    /**获取库Phone表字段**/  
    private static final String[] PHONES_PROJECTION = new String[] {  
        Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };  
     
    /**联系人显示名称**/  
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;  
      
    /**电话号码**/  
    private static final int PHONES_NUMBER_INDEX = 1;  
      
    /**头像ID**/  
    private static final int PHONES_PHOTO_ID_INDEX = 2;  
     
    /**联系人的ID**/  
    private static final int PHONES_CONTACT_ID_INDEX = 3;  
      
  
    /**联系人名称**/  
    private ArrayList<String> mContactsName = new ArrayList<String>();  
      
    /**联系人号码**/  
    private ArrayList<String> mContactsNumber = new ArrayList<String>();  
  
    /**联系人头像**/  
    private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();  
      
    ListView mListView = null;  
    MyListAdapter myAdapter = null;  

    private String prefix;
  
	@Override  
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    mContext = this;  
    mListView = this.getListView();  
    /**得到手机通讯录联系人信息**/  
    getPhoneContacts();  
    SharedPreferences pref=getSharedPreferences("user", Context.MODE_PRIVATE);
    prefix=pref.getString("prefix", "");
  
    myAdapter = new MyListAdapter(this);  
    setListAdapter(myAdapter);  
  
  
    mListView.setOnItemClickListener(new OnItemClickListener() {  
  
        @Override  
        public void onItemClick(AdapterView<?> adapterView, View view,  
            int position, long id) {  
        //调用系统方法拨打电话  
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri  
            .parse("tel:"+prefix+ mContactsNumber.get(position)));  
        Toast.makeText(mContext, getResources().getString(R.string.toast_calling)+mContactsName.get(position)+" "+prefix+ mContactsNumber.get(position), Toast.LENGTH_LONG).show();
        startActivity(dialIntent); 
        AddCallLog(mContactsNumber.get(position),0);
		finish(); 
        }
    });  
   
    }  
  
    /**得到手机通讯录联系人信息**/  
    private void getPhoneContacts() {  
    ContentResolver resolver = mContext.getContentResolver();  
  
    // 获取手机联系人  
    Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, "display_name COLLATE LOCALIZED");  
  
  
    if (phoneCursor != null) {  
        while (phoneCursor.moveToNext()) {  
  
        //得到手机号码  
        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
        //当手机号码为空的或者为空字段 跳过当前循环  
        if (TextUtils.isEmpty(phoneNumber))  
            continue;  
          
        //得到联系人名称  
        String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);  
          
        //得到联系人ID  
        Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);  
  
        //得到联系人头像ID  
        Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);  
          
        //得到联系人头像Bitmap  
        Bitmap contactPhoto = null;  
  
        //photoid 大于0表示联系人有头像 如果没有给此人设置头像则给他一个默认的  
        if(photoid > 0 ) {  
            Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);  
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);  
            contactPhoto = BitmapFactory.decodeStream(input);  
        }else {  
            contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.contact_photo);  
        }  
          
        mContactsName.add(contactName);  
        mContactsNumber.add(phoneNumber);  
        mContactsPhonto.add(contactPhoto);  
        }  
  
        phoneCursor.close();  
    }  
    }  
      
    /**得到手机SIM卡联系人人信息  
    private void getSIMContacts() {  
    ContentResolver resolver = mContext.getContentResolver();  
    // 获取Sims卡联系人  
    Uri uri = Uri.parse("content://icc/adn");  
    Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,  
        null);  
  
    if (phoneCursor != null) {  
        while (phoneCursor.moveToNext()) {  
  
        // 得到手机号码  
        String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);  
        // 当手机号码为空的或者为空字段 跳过当前循环  
        if (TextUtils.isEmpty(phoneNumber))  
            continue;  
        // 得到联系人名称  
        String contactName = phoneCursor  
            .getString(PHONES_DISPLAY_NAME_INDEX);  
  
        //Sim卡中没有联系人头像  
          
        mContactsName.add(contactName);  
        mContactsNumber.add(phoneNumber);  
        }  
  
        phoneCursor.close();  
    }  
    }  
     * @return **/
    
    //增加通话记录
    private void AddCallLog(String number,long i){
    	ContentValues content = new ContentValues();
    	content.put(CallLog.Calls.TYPE, CallLog.Calls.OUTGOING_TYPE);
    	content.put(CallLog.Calls.NUMBER,number);
    	content.put(CallLog.Calls.DATE, System.currentTimeMillis()+i);
    	//content.put(CallLog.Calls.NEW, "1");//0已看1未看
    	getContentResolver().insert(CallLog.Calls.CONTENT_URI, content);
    }
      
    class MyListAdapter extends BaseAdapter {  
    public MyListAdapter(Context context) {  
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
        ImageView iamge = null;  
        TextView title = null;  
        TextView text = null;  
        if (convertView == null || position < mContactsNumber.size()) {  
        convertView = LayoutInflater.from(mContext).inflate(  
            R.layout.colorlist, null);  
        iamge = (ImageView) convertView.findViewById(R.id.color_image);  
        title = (TextView) convertView.findViewById(R.id.color_title);  
        text = (TextView) convertView.findViewById(R.id.color_text);  
        }  
        //绘制联系人名称  
        title.setText(mContactsName.get(position));  
        //绘制联系人号码  
        text.setText(mContactsNumber.get(position));  
        //绘制联系人头像  
        iamge.setImageBitmap(mContactsPhonto.get(position));  
        return convertView;  
    }  
  
    }  
}