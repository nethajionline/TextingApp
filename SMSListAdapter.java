package com.example.karthikeyan.fingerprintcheck;

/**
 * Created by karthikeyan on 5/10/2017.
 */

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Locale;

/**
 * Created by karthikeyan on 5/9/2017.
 */

public class SMSListAdapter  extends BaseAdapter
{

    private Context mContext;
    Cursor cursor;
    CheckBox deletecheck;
    public SMSListAdapter(Context context,Cursor cur)
    {
        super();
        mContext=context;
        cursor=cur;

    }
    static class ViewHolder {
        protected TextView msg;
        protected TextView number;
        protected CheckBox check;
        protected ImageView image;
    }



    public int getCount()
    {
        // return the number of records in cursor
        return cursor.getCount();
    }

    // getView method is called for each item of ListView
    public View getView(int position, View view, ViewGroup parent)
    {
        // inflate the layout for each item of listView
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.rowlayout, null);
        final ViewHolder viewHolder = new ViewHolder();
        // move the cursor to required position
        cursor.moveToPosition(position);

        // fetch the sender number and sms body from cursor
        String senderNumber=cursor.getString(cursor.getColumnIndex("address"));
        String smsBody=cursor.getString(cursor.getColumnIndex("body"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        Date smsDayTime = new Date(Long.valueOf(date));


        // get the reference of textViews
        TextView textViewConatctNumber=(TextView)view.findViewById(R.id.textViewSMSSender);
        TextView textViewSMSBody=(TextView)view.findViewById(R.id.textViewMessageBody);
        TextView receivedTime=(TextView)view.findViewById(R.id.receivedTime);
        deletecheck=(CheckBox) view.findViewById(R.id.deletecheck) ;
        ImageView contactimage=(ImageView)view.findViewById(R.id.list_image);
        String EncBody= Main2Activity.encrypt(smsBody,senderNumber);


        // Set the Sender number and smsBody to respective TextViews
        textViewConatctNumber.setText(senderNumber);
        textViewSMSBody.setText(smsBody);
        receivedTime.setText(smsDayTime.toString());
        contactimage.setImageBitmap(getByteContactPhoto(senderNumber));
        return view;
    }



    public Bitmap getByteContactPhoto(String contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = mContext.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.DATA15}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream( new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}