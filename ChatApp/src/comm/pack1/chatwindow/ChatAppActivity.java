package comm.pack1.chatwindow;

import java.util.Calendar;

import comm.pack1.chatwindow.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ChatAppActivity extends Activity
{
	int MAX_COUNT,CURRENT_COUNT,TEMP_COUNT;
	
	private final String NEW_CHAT_MSG="##1111##";
	private final String NEW_CHAT_REQUEST="##1119##";
	private final String NEW_CHAT_ACCEPTED="##1118##";
	private final String NEW_CHAT_REJECTED="##1117##";
	private final String CLOSE_CHAT_MSG="##1116##";
	private final int CHAT_MSG=1111;
	private final int CHAT_REQUEST=1119;
	private final int CHAT_ACCEPTED=1118;
	private final int CHAT_REJECTED=1117;
	private final int CHAT_CLOSE=1116;
	

	
	private final int ENTER_YOUR_NAME=1;
	private final int START_NEW_CHAT=2;
	private final int ACCEPT_NEW_CHAT=3;

	String RECEIVED_REQUEST_NAME;
	String RECEIVED_REQUEST_NUMBER;
	
	Dialog DIALOG_UR_NAME;
	Dialog DIALOG_NEW_CHAT;
	Dialog DIALOG_ACCEPT_CHAT;
	
	String MY_CHAT_NAME;
	
	int ChatLength[]=new int[20];
	String[][] TextMesg=new String[20][20];
	String[][] TextTime=new String[20][20];
	String RcvNumber[]=new String[20];
	String RcvName[]=new String[20];
	Button LabelBtn[]=new Button[20];
	ListView ChatList[]=new ListView[20];
	LinearLayout btnLayout;
	FrameLayout frame;
	EditText MsgBody;
	Button SendChatBtn;
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setTheme( android.R.style.Theme_Light );
        setContentView(R.layout.main_layout);
        
          IntentFilter filter = new IntentFilter( "android.provider.Telephony.SMS_RECEIVED" );
          filter.setPriority( IntentFilter.SYSTEM_HIGH_PRIORITY );
          registerReceiver( new SmsReceiver(), filter );
          
          InitialzeComponents();
          showDialog(ENTER_YOUR_NAME);

    }
 	
    protected void FilterOutGoingMsg(int type,String Name,String Number,String TxtMsg){
    	switch (type)
    	{
    	case CHAT_MSG:
    			TextMesg[CURRENT_COUNT][ChatLength[CURRENT_COUNT]]=TxtMsg;
    			TextTime[CURRENT_COUNT][ChatLength[CURRENT_COUNT]]=GetCurrentTime();
    			IncrChatLength(CURRENT_COUNT);
    			UpdateChatList(CURRENT_COUNT);
    			SendSMS(Number,NEW_CHAT_MSG+TxtMsg+NEW_CHAT_MSG);
    		break;
    	case CHAT_REQUEST: 	
    			SendSMS(Number, NEW_CHAT_REQUEST+MY_CHAT_NAME+NEW_CHAT_REQUEST);    			
    		break;
    	case CHAT_ACCEPTED:
    			SendSMS(Number, NEW_CHAT_ACCEPTED+MY_CHAT_NAME+NEW_CHAT_ACCEPTED);
    		break;
    	case CHAT_REJECTED:
    			SendSMS(Number, NEW_CHAT_REJECTED+MY_CHAT_NAME+NEW_CHAT_REJECTED);
    		break;
    	case CHAT_CLOSE:
    		break;
    	}
    }

    
    protected boolean FilterInComingMsg(String type,String SenderNo,String MainMsg){
    	boolean SkipMsg=false;
		Toast.makeText(getApplicationContext(), "THIS IS TYPE: "+GetMsgType(type)+"", Toast.LENGTH_LONG).show();
    	switch (GetMsgType(type))
    	{
    	case CHAT_MSG:
    		TEMP_COUNT=GetCurrentCount(SenderNo);
    			if(TEMP_COUNT==-1){
    				Toast.makeText(getApplicationContext(), "KHATARNAK ERROR HAI BE....!", Toast.LENGTH_LONG).show();
    			}else{
    					TextMesg[TEMP_COUNT][ChatLength[TEMP_COUNT]]=MainMsg;
    					TextTime[TEMP_COUNT][ChatLength[TEMP_COUNT]]=GetCurrentTime();
    					UpdateChatList(TEMP_COUNT);
    					IncrChatLength(TEMP_COUNT);
    					Toast.makeText(getApplicationContext(), "NEW MSG FROM: "+RcvName[TEMP_COUNT] , Toast.LENGTH_LONG).show();
    			}    	
    			UpdateChatList(CURRENT_COUNT);
    		break;
    	case CHAT_REQUEST:
    			RECEIVED_REQUEST_NAME=MainMsg;
    			RECEIVED_REQUEST_NUMBER=SenderNo;
    			showDialog(ACCEPT_NEW_CHAT);
    		break;
    	case CHAT_ACCEPTED:
			AddNewTab(MAX_COUNT,MainMsg,SenderNo);
			MAX_COUNT++;  
    		break;
    	case CHAT_REJECTED: 
    			Toast.makeText(getApplicationContext(), MainMsg+" REJECTED your Chat Request", Toast.LENGTH_LONG).show();    		
    		break;
    	case CHAT_CLOSE:
    		
    		break;
    		default:  Toast.makeText( getApplicationContext(), "INBOX:\nYOU GOT A NEW MSG..! ", Toast.LENGTH_SHORT ).show();
    					SkipMsg=true;
    	}
    	return SkipMsg;
    }


private int GetMsgType(String type) {
			if(type.equals(NEW_CHAT_MSG))
					return CHAT_MSG;
			if(type.equals(NEW_CHAT_REQUEST))
					return CHAT_REQUEST;
			if(type.equals(NEW_CHAT_ACCEPTED))
					return CHAT_ACCEPTED;
			if(type.equals(NEW_CHAT_REJECTED))
					return CHAT_REJECTED;
			if(type.equals(CLOSE_CHAT_MSG))
					return CHAT_CLOSE;
		return 0;
	}
    
class SmsReceiver extends BroadcastReceiver 
{
	public static final String SMS_EXTRA_NAME = "pdus";
	public void onReceive( Context context, Intent intent ) 
	{
		boolean SkipMsg=false;
		Bundle extras = intent.getExtras();
		String addr = "";
		String messages = "";
        if ( extras != null )
        {
            Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME );
            for ( int i = 0; i < smsExtra.length; ++i )
            {
            	SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
            	String body = sms.getMessageBody().toString();
            	String address = sms.getOriginatingAddress();
            	messages=body;
            	addr=address;
            }
            // Display SMS message
            try{
            	SkipMsg=FilterInComingMsg(messages.substring(0, 8), addr, messages.substring(8, messages.length()-8));
            }catch(Exception ex){}
        }
        // If we don't need this msg then DO NOT call the next line....
        if(!SkipMsg)
         this.abortBroadcast(); 
	}
}

public void SendSMS(String RcvNumber,String MsgData){
	SmsManager msgg=SmsManager.getDefault();
	msgg.sendTextMessage(RcvNumber, null, MsgData, null, null);
}

protected void InitialzeComponents(){
	MAX_COUNT=0;
    //Myctx=this.getApplicationContext();
    btnLayout=(LinearLayout) findViewById(R.id.btn_LinearLayout);
    MsgBody=(EditText) findViewById(R.id.msgbodytxt);
    SendChatBtn=(Button) findViewById(R.id.sendchatbtn);
    frame=(FrameLayout) findViewById(R.id.chatframelayout);
    SendChatBtn.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
				if(MAX_COUNT==0){
					Toast.makeText(getApplicationContext(), "No Receiver Added", Toast.LENGTH_LONG).show();
				}else{
					FilterOutGoingMsg(CHAT_MSG, RcvName[CURRENT_COUNT], RcvNumber[CURRENT_COUNT], MsgBody.getText().toString());
				}

		}
	});
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater i= getMenuInflater();
    i.inflate(R.menu.add_remove_menu, menu);
    return true;
}

public boolean onOptionsItemSelected(MenuItem i){
switch(i.getItemId()){
case R.id.newchat_menu:showDialog(START_NEW_CHAT);
	break;
case R.id.exit_menu:System.exit(0);
	break;
}
 	return false;
}

@Override
protected Dialog onCreateDialog(int id) {
switch (id) {

case ENTER_YOUR_NAME:
    LayoutInflater factory = LayoutInflater.from(this);
    final View textEntryView = factory.inflate(R.layout.your_name, null);
    DIALOG_UR_NAME=new AlertDialog.Builder(ChatAppActivity.this)
        .setTitle("STARTING Up...")
        .setView(textEntryView)
        .setPositiveButton("Start Chat", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	EditText name1=(EditText)DIALOG_UR_NAME.findViewById(R.id.your_name_txt);
            	MY_CHAT_NAME=name1.getText().toString();
                Toast.makeText(getApplicationContext(),"YOUR NAME IS: "+ MY_CHAT_NAME, Toast.LENGTH_LONG).show();
            }
        })
        .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	System.exit(0);
            }
        })
        .create();
    return DIALOG_UR_NAME;
case START_NEW_CHAT:
    LayoutInflater factory1 = LayoutInflater.from(this);
    final View textEntryView1 = factory1.inflate(R.layout.new_chat_number, null);
    DIALOG_NEW_CHAT= new AlertDialog.Builder(ChatAppActivity.this)
        .setTitle("New Chat Detalis...")
        .setView(textEntryView1)
        .setPositiveButton("Start Chat", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	EditText number=(EditText)DIALOG_NEW_CHAT.findViewById(R.id.Number_txt_view);
    	    	if(number.getText().toString()==null){
    	    		number.setError("Enter Name..");
    	    	}else{
    	    		Toast.makeText(getApplicationContext(), "CALLING FILTER FOR OUTGOING", Toast.LENGTH_LONG).show();
            	FilterOutGoingMsg(CHAT_REQUEST,null,number.getText().toString(),null);
    	    	}
            }
        })
        .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	finish();
            }
        })
        .create();
    return DIALOG_NEW_CHAT;
case ACCEPT_NEW_CHAT:
    DIALOG_ACCEPT_CHAT= new AlertDialog.Builder(ChatAppActivity.this)
        .setTitle("New Chat Detalis...")
        .setMessage("Name: "+RECEIVED_REQUEST_NAME+"\nNumber :"+RECEIVED_REQUEST_NUMBER+"\n Do you wish to ACCEPT ?")
        .setPositiveButton("Start Chat", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
    				AddNewTab(MAX_COUNT,RECEIVED_REQUEST_NAME,RECEIVED_REQUEST_NUMBER);
    				MAX_COUNT++;                    	
            		FilterOutGoingMsg(CHAT_ACCEPTED, RECEIVED_REQUEST_NAME, RECEIVED_REQUEST_NUMBER, MY_CHAT_NAME);
            }
        })
        .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            		FilterOutGoingMsg(CHAT_REJECTED, RECEIVED_REQUEST_NAME, RECEIVED_REQUEST_NUMBER, MY_CHAT_NAME);
            }
        })
        .create();
    return DIALOG_ACCEPT_CHAT;
}
return null;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
protected void PutInFront(View v){
	int y;
	for(y=0;y<MAX_COUNT;y++)
		if(ChatList[y].getId()==(100+v.getId()))
			ChatList[y].bringToFront();
}

// NOT NECESSARY may be...
protected boolean CheckIfPresent(int x){
	if(RcvName[x].equalsIgnoreCase(MsgBody.getText().toString()))
		return true;
	return false;
}

protected void AddNewTab(final int cnt,String name,String numbr){
	RcvName[cnt]=name;
	RcvNumber[cnt]=numbr;
	LabelBtn[cnt]=AddNewLabelButton(name+cnt);
	btnLayout.addView(LabelBtn[cnt]);
	ChatList[cnt]=AddNewListView();			
	CURRENT_COUNT=cnt;
	LabelBtn[cnt].setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			PutInFront(v);
			CURRENT_COUNT=cnt;
			UpdateChatList(CURRENT_COUNT);
		}
	});
}

protected Button AddNewLabelButton(String name){
	Button LabelButton=new Button(getApplicationContext());
	LabelButton.setId(MAX_COUNT);
	LabelButton.setText(name);
	return LabelButton;
}

protected ListView AddNewListView(){
	ListView lv=new ListView(getApplicationContext());
	lv.setId(100+MAX_COUNT);
	return lv;
}

protected String GetCurrentTime(){
	final Calendar c = Calendar.getInstance();
    int mYear = c.get(Calendar.YEAR);
    int mMonth = c.get(Calendar.MONTH);
    int mDay = c.get(Calendar.DAY_OF_MONTH);
    int mHour = c.get(Calendar.HOUR_OF_DAY);
    int mMinute = c.get(Calendar.MINUTE);
    return "["+mDay+" / "+mMonth+" / "+mYear+" ]  [ "+mHour+" : "+mMinute+" ] ";
}

private int GetCurrentCount(String senderNo) {
	  int i;
	  for(i=0;i<20;i++)
	  if(RcvNumber[i].equals(senderNo))
		  return i;
	return -1;
}

private void IncrChatLength(int currentCount2) {
	  if(ChatLength[currentCount2]==19){
		  for(int i=0;i<19;i++){
			  TextMesg[i]=TextMesg[i+1];
			  TextTime[i]=TextTime[i+1];
		  }
	  }else{
		  ChatLength[currentCount2]++;
	  }
}

private void UpdateChatList(int currentCount2) {
		if(ChatLength[currentCount2]!=0){
			frame.removeAllViews();
			frame.addView(ChatList[currentCount2]);
			ChatList[currentCount2].setAdapter(new MyAdapter(getApplicationContext()));
			ChatList[currentCount2].smoothScrollToPosition(ChatLength[currentCount2]);
		//IncrChatLength(currentCount2);
		}else{
			frame.removeAllViews();
		}
		
	}

class MyAdapter extends BaseAdapter{
	LayoutInflater minflate;
	ViewHolder holder;
	int Length;
	public MyAdapter(Context cc){
        minflate = LayoutInflater.from(cc);
        Length=ChatLength[CURRENT_COUNT];
	}
	public int getCount() {
		return Length;
	}
	public Object getItem(int position) {
		return position;
	}
	public long getItemId(int position) {
			return position;
	}
    public View getView(int position, View convertView, ViewGroup parent) {        
        if (convertView == null) {
            convertView = minflate.inflate(R.layout.chat_msg_layout, null);
            holder = new ViewHolder();
            holder.Msgtext = (TextView) convertView.findViewById(R.id.MsgTxtView);
            holder.Timetext = (TextView) convertView.findViewById(R.id.TimeTxtView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.Msgtext.setText(TextMesg[CURRENT_COUNT][position]);
        holder.Timetext.setText(TextTime[CURRENT_COUNT][position]);
        return convertView;
    }
}
        class ViewHolder {
            TextView Msgtext;
            TextView Timetext;
            }
}



    
  