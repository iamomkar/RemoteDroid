package com.creativeminds.remotedroid;
 
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
import fi.iki.elonen.NanoHTTPD; 
 
/** 
 * Created by Mikhael LOPEZ on 14/12/2015. 
 */ 
public class AndroidWebServer extends NanoHTTPD { 

    private Context context;
    private List<SMSModel> smsModels = new ArrayList<>();
    private List<SMSModel> callModels = new ArrayList<>();

    public AndroidWebServer(Context context,int port) {
        super(port);
        this.context = context;
        getMessages();
    } 
 
    public AndroidWebServer(String hostname, int port) {
        super(hostname, port);
    } 
 
    @Override 
    public Response serve(IHTTPSession session) {
        smsModels.clear();
        callModels.clear();
        String msg="";
        Map<String, String> parms = session.getParms();
        Log.d("URI",session.getUri());
        String uri = session.getUri();
        if(uri.equals("/")){
             msg = "<html><body><h1>Remote Droid Home</h1>\n";
            msg += "<form action='?' method='get'>\n  <p>Navigation Menu: <br><hr>" +
                    "<button formaction='messages' type='submit'>Recent Messages</button><br><br>" +
                    "<button formaction='sendmsg' type='submit'>Send Messages</button><br><br>" +
                    "<button formaction='calllogs' type='submit'>Recent Calls</button><br><br>" +
                    "<button formaction='call' type='submit'> Send Call</button><br><br>" +
                    "</p>\n" + "</form>\n";

        }else if(uri.equalsIgnoreCase("/messages")){
            getMessages();
             msg = "<html><body><h1>Recent Messages</h1>\n";
            for(int k = 0;k < smsModels.size();k++){
                String content = "<hr><h3>"+k+"]  "+smsModels.get(k).getAddress()+"</h3><h4>"+smsModels.get(k).getBody()+"</h4>";
                msg = msg.concat(content);
            }

        }else if(uri.equalsIgnoreCase("/sendmsg")){

             if(parms.get("number") == null && parms.get("message") == null){
                 msg = "<html><body><h1>Send Message</h1>\n";
                 msg += "<form action='?' method='get'>\n  <p>Enter Number : <input type='number' name='number'><br><br>" +
                         "Enter Message : <input type='text' name='message'><br><hr>" +
                         "<button type='submit'> Send Message</button>" +
                         "</p>\n" + "</form>\n";
             }else {
                 msg = "<html><body><h1>Send Message</h1>\n";
                 sendMessage(parms.get("number"),parms.get("message"));
                 msg += "<h2>Message Sent</h2>";
                 //Log.d("TAG","Inside Send Message");
             }
        }else if(uri.equalsIgnoreCase("/calllogs")){
            getCallLog();
             msg = "<html><body><h1>Recent Call Logs</h1>\n";
            for(int k = 0;k < callModels.size();k++){
                String content = "<hr><h3>"+k+"]  "+callModels.get(k).getDate()+"<br>"+callModels.get(k).getAddress()+"</h3><h4>"+callModels.get(k).getBody()+"</h4>";
                msg = msg.concat(content);
            }

        }else if(uri.equalsIgnoreCase("/call")){
            if(parms.get("number") == null){
                msg = "<html><body><h1>Send Message</h1>\n";
                msg += "<form action='?' method='get'>\n  <p>Enter Number : <input type='number' name='number'><br><br>" +
                        "<button type='submit'> Send Call</button>" +
                        "</p>\n" + "</form>\n";
            }else {
                msg = "<html><body><h1>Send Call</h1>\n";
                sendCall(parms.get("number"));
                msg += "<h2>Call Sent</h2>";
                //Log.d("TAG","Inside Send Message");
            }

        }else {
            msg = "<html><body><h1>404 Not Found</h1>\n";
        }
       /* if (parms.get("username") == null) {
            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
        } else { 
            msg += "<p>Hello, " + parms.get("username") + "!</p>";
        } */

        return newFixedLengthResponse( msg + "</body></html>\n" );
    }

    private void getMessages(){
        try {
            Cursor cur = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            String msgInfo = "";
            int j = 0;
            if (cur.moveToFirst()) { /* false = no sms */

                do {
                    j++;
                    msgInfo += "\n\n" + j + "\n";
                    for (int i = 0; i < cur.getColumnCount(); i++) {
                        // msgInfo += i+"  " + cur.getColumnName(i) + ":" + cur.getString(i)+"\n";
                    }
                    SMSModel smsModel = new SMSModel();
                    smsModel.setID(Integer.parseInt(cur.getString(cur.getColumnIndex(Telephony.Sms._ID))));
                    smsModel.setAddress(cur.getString(cur.getColumnIndex(Telephony.Sms.ADDRESS)));
                    smsModel.setBody(cur.getString(cur.getColumnIndex(Telephony.Sms.BODY)));
                    smsModel.setDate(cur.getString(cur.getColumnIndex(Telephony.Sms.DATE)));
                    smsModels.add(smsModel);
                    //Toast.makeText(this, msgInfo, Toast.LENGTH_SHORT).show();
                    cur.moveToNext();
                } while (j < 50);
                //}while (cur.moveToNext()) ;
            }
            cur.close();
        }catch (Exception e){
            Toast.makeText(context,"Grant Message Permission in Settings",Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String no,String msg){
        try {
            SmsManager sms = SmsManager.getDefault();

            sms.sendTextMessage(no, null, msg, null, null);

        }catch (Exception e){

           // Toast.makeText(context,"Grant Message Permission in Settings",Toast.LENGTH_SHORT).show();
        }
    }

    private void getCallLog(){
        try {
            Uri allCalls = Uri.parse("content://call_log/calls");
            Cursor c = context.getContentResolver().query(allCalls, null, null, null, null);
            String msgInfo = "";
            int j = 0;
            if (c.moveToLast()) { /* false = no sms */

                do {
                    j++;
                    SMSModel smsModel = new SMSModel();
                    smsModel.setID(Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.DURATION))));
                    smsModel.setAddress(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                    if(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)).equals(String.valueOf(CallLog.Calls.INCOMING_TYPE))){
                        smsModel.setBody("Incoming");
                    } else if(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)).equals(String.valueOf(CallLog.Calls.OUTGOING_TYPE))){
                        smsModel.setBody("Outgoing");
                    } else if(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)).equals(String.valueOf(CallLog.Calls.REJECTED_TYPE))){
                        smsModel.setBody("Rejected");
                    }else if(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)).equals(String.valueOf(CallLog.Calls.BLOCKED_TYPE))){
                        smsModel.setBody("Blocked");
                    }else if(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)).equals(String.valueOf(CallLog.Calls.ANSWERED_EXTERNALLY_TYPE))){
                        smsModel.setBody("Incoming");
                    }
                    smsModel.setDate(c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)));
                    callModels.add(smsModel);
                    c.moveToPrevious();
                } while (j < 50);
                //}while (cur.moveToNext()) ;
            }
            c.close();
        }catch (Exception e){
            e.printStackTrace();
            //Toast.makeText(context,"Grant Message Permission in Settings",Toast.LENGTH_SHORT).show();
        }



        }

    private void sendCall(String no){
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);

            intent.setData(Uri.parse("tel:" + no));
            context.startActivity(intent);

        }catch (SecurityException e){
                e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
            // Toast.makeText(context,"Grant Message Permission in Settings",Toast.LENGTH_SHORT).show();
        }
    }

} 