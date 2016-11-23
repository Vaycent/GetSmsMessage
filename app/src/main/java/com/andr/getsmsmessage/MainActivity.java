package com.andr.getsmsmessage;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button getMsgBtn;
    private TextView msgText;
    private String subscriberNum="";

    private SmsObserver smsObserver;

    private String getAccount, getFlow;
    private Dialog mProgressDialog;

    private Handler dialogHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getMsgBtn = (Button)findViewById(R.id.get_message_btn);
        getMsgBtn.setOnClickListener(this);
        msgText = (TextView) findViewById(R.id.msg_result_tx);

        smsObserver = new SmsObserver(this, smsHandler);
        Uri uri = Uri.parse("content://sms/");
        this.getContentResolver().registerContentObserver(uri, true, smsObserver);


        String[] permissionList={Manifest.permission.READ_PHONE_STATE,Manifest.permission.SEND_SMS};
        checkPermissions(permissionList,169);

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.getContentResolver().unregisterContentObserver(smsObserver);
        dialogHandler.removeCallbacks(runnableLoading);

    }

    @Override
    protected void onResume(){
        super.onResume();
        getMsgBtn.setVisibility(View.VISIBLE);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.get_message_btn:
                getAccount();
                getFlow();
                loadingDialog();
                break;
            default:
                break;
        }
    }


    private void SendSms( String Num, String context){
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> list = manager.divideMessage(context);
        for(String text:list){
            manager.sendTextMessage(Num, null, text, null, null);
        }
    }

    private void getAccount(){
        String phone = "";
        String context = "";
        if(IMEI.isChinaMobile(this)){
            phone="10086";
            context="ye";
        }else if(IMEI.isChinaTelecom(this)){
            phone="10001";
            context="102";
        }else if(IMEI.isChinaUnicom(this)){
            phone="10010";
            context="cxye";
        }else{
            Toast.makeText(this,"未知的SIM卡运营商",Toast.LENGTH_LONG);
        }

        if(!phone.equals("")&&!context.equals("")){
            subscriberNum=phone;
            SendSms(phone,context);
        }

    }

    private void getFlow(){
        String phone = "";
        String context = "";
        if(IMEI.isChinaMobile(this)){
            phone="10086";
            context="cxll";
        }else if(IMEI.isChinaTelecom(this)){
            phone="10001";
            context="cxll";
        }else if(IMEI.isChinaUnicom(this)){
            phone="10010";
            context="cxll";
        }else{
            Toast.makeText(this,"未知的SIM卡运营商",Toast.LENGTH_LONG);
        }

        if(!phone.equals("")&&!context.equals("")){
            subscriberNum=phone;
            SendSms(phone,context);
        }
    }

    public void getSmsFromPhone() {
        ContentResolver resolver = getContentResolver();
        Uri uri = Uri.parse("content://sms/");
        String[] projection = new String[] { "_id","body","address","person","date","read"};
        String selection = " address = ? and read = ?";
        String[] selectionArgs = {subscriberNum,"0"};
        String sortOrder = "_id desc limit 2";
        Cursor cur = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        String msgStr="";
        System.out.println("cur.getCount():"+cur.getCount());

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex("_id"));
                String smsbody = cur.getString(cur.getColumnIndex("body"));
                String address = cur.getString(cur.getColumnIndex("address"));
                String person = cur.getString(cur.getColumnIndex("person"));
                String date = cur.getString(cur.getColumnIndex("date"));
                String read = cur.getString(cur.getColumnIndex("read"));

//                System.out.println("id:"+id);
//                System.out.println("smsbody:"+smsbody);
//                System.out.println("address:"+address);
//                System.out.println("person:"+person);
//                System.out.println("date:"+date);
//                System.out.println("read:"+read);
                msgStr += smsbody + "\n\n";
            }
            cur.close();
            msgText.setText(msgStr);
            getMsgBtn.setVisibility(View.INVISIBLE);
        }
    }

    class SmsObserver extends ContentObserver {

        public SmsObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            msgText.setText("");
            getSmsFromPhone();
        }
    }

    public Handler smsHandler = new Handler() {public void handleMessage(android.os.Message msg) {System.out.println("smsHandler");
        }
    };

    private void loadingDialog(){
        showProgressDialog();

        dialogHandler.postDelayed(runnableLoading, 4000);
    }

    private Runnable runnableLoading = new Runnable() {
        public void run() {
            dismissProgressDialog();
        }
    };

    private Dialog showProgressDialog() {
        if (mProgressDialog == null) {
            Dialog dialog = new Dialog(this, R.style.selectorDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // dialog.setTitle("Data Loading");
            // dialog.setMessage("Please wait...");
            // dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.loading_layout_tp);
            mProgressDialog = dialog;
        }
        mProgressDialog.show();
        return mProgressDialog;
    }

    private void dismissProgressDialog() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {

                mProgressDialog.dismiss();
            }

        } catch (Exception e) {
            // We don't mind. android cleared it for us.

            e.printStackTrace();
        }
    }

    private void checkPermissions(String[] permissionList, int requestCode) {
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        for(int i=0;i<permissionList.length;i++){
            int permissionCheckStatus = ContextCompat.checkSelfPermission(this, permissionList[i]);
            boolean isGranted=permissionCheckStatus == PackageManager.PERMISSION_GRANTED?true:false;
//            boolean shouldShowPermission= ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            if(!isGranted){
                requestPermissions(permissionList, requestCode);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 169) {
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] == PackageManager.PERMISSION_DENIED)
                    this.finish();
            }
        }
    }

}
