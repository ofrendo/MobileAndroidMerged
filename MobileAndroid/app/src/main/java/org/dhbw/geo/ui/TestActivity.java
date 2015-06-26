package org.dhbw.geo.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;


import org.dhbw.geo.map.Maps;
import org.dhbw.geo.R;
import org.dhbw.geo.backend.BackendCallback;
import org.dhbw.geo.backend.BackendController;
import org.dhbw.geo.backend.JSONConverter;
import org.dhbw.geo.database.*;
import org.dhbw.geo.hardware.HardwareController;
import org.dhbw.geo.hardware.NotificationFactory;
import org.dhbw.geo.hardware.SMSFactory;
import org.dhbw.geo.services.ConditionService;
import org.dhbw.geo.services.ContextManager;
import org.dhbw.geo.ui.RuleFragments.RuleContainer;

import java.util.ArrayList;


public class TestActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    private DBConditionFence mconditionFence;
    private PendingIntent mPendingIntent;
    private DBFence addedFence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // register context
        ContextManager.setContext(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //ERROR In Emulator !?


        // Set correct radio button for wifi status
        //HardwareController.getInstance().setContext(this);
        boolean wifiStatus = HardwareController.getInstance().isWifiEnabled();
        RadioButton startButton = (wifiStatus == true) ?
                (RadioButton) this.findViewById(R.id.radioButtonWifiEnable) :
                (RadioButton) this.findViewById(R.id.radioButtonWifiDisable);
        startButton.setChecked(true);

        Log.i(TAG, "Start wifi status is: " + wifiStatus);

        // Test api calls
        testBackendApiCalls();
    }

    public void testBackendApiCalls() {
        BackendController backendController = new BackendController(new BackendCallback() {
            public void actionPerformed(String result) {
                Log.i("Test BackendCallback", "getAllFenceGroups: " + result);
                ArrayList<DBConditionFence> groups = JSONConverter.getFenceGroups(result);
            }
        });
        backendController.getAllFenceGroups();

        // Test create fencegroup, create fence and delete fence
        BackendController backendController2 = new BackendController(new BackendCallback() {
            public void actionPerformed(String result) {
                Log.i("Test BackendCallback", "createFenceGroup: " + result);
                DBConditionFence fenceGroup = JSONConverter.getFenceGroup(result);
                final int server_fence_group_id = fenceGroup.getServerId();

                BackendController backendController3 = new BackendController(new BackendCallback() {
                    public void actionPerformed(String result) {
                        Log.i("Test BackendCallback", "createFence: " + result);
                        DBFence fence = JSONConverter.getFence(result);
                        int fence_id = (int) fence.getId();

                        BackendController backendController4 = new BackendController(new BackendCallback() {
                            public void actionPerformed(String result) {
                                Log.i("Test BackendCallback", "deleteFence: " + result);
                            }
                        });
                        backendController4.deleteFence(server_fence_group_id, fence_id);

                    }
                });
                DBFence testFence = new DBFence(4, 1, 1, 1);
                backendController3.createFence(server_fence_group_id, testFence);
            }
        });
        DBConditionFence testFenceGroup = new DBConditionFence();
        testFenceGroup.setType("testType");
        testFenceGroup.setName("testName");
        backendController2.createFenceGroup(testFenceGroup);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void onWifiEnable(View view) {
        HardwareController.getInstance().setWifi(true);
    }

    public void onWifiDisable(View view) {
        HardwareController.getInstance().setWifi(false);
    }

    public void onSoundStreamMusic(View view) {
        // Set sound status radio group to correct music setting
        boolean currentStatus = HardwareController.getInstance().getAudioStatus(AudioManager.STREAM_MUSIC);
        RadioButton currentStatusButton = (currentStatus == true) ?
                (RadioButton) this.findViewById(R.id.radioButtonSoundOn) :
                (RadioButton) this.findViewById(R.id.radioButtonSoundMute);
        currentStatusButton.setChecked(true);
    }
    public void onSoundStreamRing(View view) {
        // Set sound status radio group to correct music setting
        boolean currentStatus = HardwareController.getInstance().getAudioStatus(AudioManager.STREAM_RING);
        RadioButton currentStatusButton = (currentStatus == true) ?
                (RadioButton) this.findViewById(R.id.radioButtonSoundOn) :
                (RadioButton) this.findViewById(R.id.radioButtonSoundMute);
        currentStatusButton.setChecked(true);
    }

    public void onNotificationClick(View view) {
        //Sends a test notification
        NotificationFactory.createNotification(this, "Test", "Hello world!", false);
    }

    public void onOngoingNotificationClick(View view) {
        //Sends a test permanent notification
        NotificationFactory.createNotification(this, "Test perm", "Hello world! Perm", true);
    }

    public void onTestSMSSend(View view) {
        //Sends a test SMS
        EditText inputNumber = (EditText) this.findViewById(R.id.editTextTestSMS);
        String number = inputNumber.getText().toString();
        SMSFactory.createSMS(number, "Hallo! Dies ist eine automatisch generierte test SMS von der App die wir gerade programmieren.");
    }

    public void onRuleClick (View view ){
        //Neues Intent anlegen
        Intent nextScreen = new Intent(getApplicationContext(), RuleContainer.class);

        //Intent mit den Daten fuellen
        // nextScreen.putExtra("Vorname", inputVorname.getText().toString());
        // nextScreen.putExtra("Nachname", inputNachname.getText().toString());

        // Log schreiben fuer Logausgabe
        //Log.e("n", inputVorname.getText()+"."+ inputNachname.getText());

        // Intent starten und zur zweiten Activity wechseln
        startActivity(nextScreen);
    }

    public void onOpenMapClick(View view){
        //Neues Intent anlegen
        Intent nextScreen = new Intent(getApplicationContext(), Maps.class);
        // Intent starten und zur zweiten Activity wechseln
        startActivity(nextScreen);
    }

    public void testDatabaseStuff(View view){
        // delete old data
        DBHelper dbHelper = DBHelper.getInstance();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DBHelper.TABLE_ACTION_SIMPLE);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_ACTION_SOUND);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_ACTION_BRIGHTNESS);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_ACTION_NOTIFICATION);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_ACTION_MESSAGE);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_CONDITION_FENCE);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_FENCE);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_CONDITION_TIME);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_DAY_STATUS);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_RULE_CONDITION);
        db.execSQL("DELETE FROM " + DBHelper.TABLE_RULE);
        db.execSQL("DELETE FROM sqlite_sequence");

        // test action start and stop
        /*DBRule rule = new DBRule();
        rule.setActive(true);
        rule.setName("Test rule");
        rule.writeToDB();
        DBConditionTime conditionTime = new DBConditionTime();
        Calendar now = Calendar.getInstance();
        conditionTime.addDay(now.get(Calendar.DAY_OF_WEEK));
        now.add(Calendar.MINUTE, 4);
        conditionTime.setStart(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        rule.addCondition(conditionTime);
        conditionTime.writeToDB();
        DBActionSimple wifi = new DBActionSimple();
        wifi.setActive(true);
        wifi.setType(DBActionSimple.TYPE_WIFI);
        wifi.setStatus(true);
        rule.addAction(wifi);
        wifi.writeToDB();
        DBActionNotification notification = new DBActionNotification();
        notification.setActive(true);
        notification.setMessage("Let's rule!");
        rule.addAction(notification);
        notification.writeToDB();

        /*DBRule fenceRule = new DBRule();
        fenceRule.setActive(true);
        fenceRule.setName("Test rule");
        fenceRule.writeToDB();
        DBConditionFence conditionFence = new DBConditionFence();
        conditionFence.setType(DBConditionFence.TYPE_ENTER);
        conditionFence.setName("Test Condition Fence");
        fenceRule.addCondition(conditionFence);
        conditionFence.writeToDB();
        DBFence fence = new DBFence();
        fence.setLatitude(49.474292);
        fence.setLongitude(8.534501);
        fence.setRadius(30);
        conditionFence.addFence(fence);
        fence.writeToDB();
        DBFence fence2 = new DBFence();
        fence2.setLatitude(50.57828);
        fence2.setLongitude(8.777);
        fence2.setRadius(30);
        conditionFence.addFence(fence2);
        fence2.writeToDB();
        dbHelper.logDB();
      //  if(conditionFence.isConditionMet()) Log.d("Main", "im Fence!");

        // Create the test database
        // create home rule
        /*DBRule homeRule = new DBRule();
        homeRule.setActive(true);
        homeRule.setName("Home Rule");
        homeRule.writeToDB();
        // create home fence condition (only 1 fence)
        DBConditionFence homeCondition = new DBConditionFence();
        homeCondition.setType(DBConditionFence.TYPE_ENTER);
        homeCondition.setName("Home Location");
        homeRule.addCondition(homeCondition);
        homeCondition.writeToDB();
        // create the fence
        fence = new DBFence();
        fence.setLatitude(49.474292);
        fence.setLongitude(8.534501);
        fence.setRadius(30);
        homeCondition.addFence(fence);
        fence.writeToDB();
        // create a wifi action
        wifi = new DBActionSimple();
        homeRule.addAction(wifi);
        wifi.setStatus(true); // turn wifi on
        wifi.setActive(true);
        wifi.setType(DBActionSimple.TYPE_WIFI);
        wifi.writeToDB();
        // create a notification
        DBActionNotification homeNot = new DBActionNotification();
        homeRule.addAction(homeNot);
        homeNot.setMessage("Home Sweet Home!");
        homeNot.setActive(true);
        homeNot.writeToDB();

        // create work time rule
        DBRule workRule = new DBRule();
        workRule.setActive(true);
        workRule.setName("Work Rule");
        workRule.writeToDB();
        // create time condition
        DBConditionTime workTime = new DBConditionTime();
        workTime.setName("Working Time");
        workTime.addDay(Calendar.MONDAY);
        workTime.addDay(Calendar.TUESDAY);
        workTime.addDay(Calendar.WEDNESDAY);
        workTime.addDay(Calendar.THURSDAY);
        workTime.addDay(Calendar.FRIDAY);
        workTime.setStart(8, 0);
        workTime.setEnd(16, 30);
        workRule.addCondition(workTime);
        workTime.writeToDB();
        workTime.updateAlarm();

        // create time condition
        DBConditionTime workTime2 = new DBConditionTime();
        workTime2.setName("Working Time2");
        workTime2.addDay(Calendar.MONDAY);
        workTime2.addDay(Calendar.TUESDAY);
        workTime2.addDay(Calendar.WEDNESDAY);
        workTime2.addDay(Calendar.THURSDAY);
        workTime2.addDay(Calendar.FRIDAY);
        workTime2.setStart(8, 0);
        workTime2.setEnd(16, 30);
        workRule.addCondition(workTime2);
        workTime2.writeToDB();
        workTime2.updateAlarm();

        // create sound action
        DBActionSound music = new DBActionSound();
        music.setActive(true);
        music.setType(AudioManager.STREAM_MUSIC);
        music.setStatus(DBActionSound.STATUS_MUTE);
        workRule.addAction(music);
        music.writeToDB();
        DBActionSound phone = new DBActionSound();
        phone.setStatus(DBActionSound.STATUS_SOUND);
        phone.setType(AudioManager.STREAM_RING);
        phone.setVolume(20);
        phone.setActive(true);
        workRule.addAction(phone);
        phone.writeToDB();

        // Only for setuptests
        // TODO: if geofencsetup is working delete this

        mconditionFence = conditionFence;*/

        // write rule without name to database
        DBRule rule = new DBRule();
        //rule.setName("test");
        rule.writeToDB();

        // log database
        dbHelper.logDB();


    }

    public void onClickAddFence(View view) {

        // Test add fence
        addedFence = new DBFence();
        addedFence.setLatitude(49.543047);
        addedFence.setLongitude(8.663150);
        addedFence.setRadius(30);
        mconditionFence.addFence(addedFence);
        addedFence.writeToDB();

        Intent addFence = new Intent(this, ConditionService.class);
        addFence.setAction(ConditionService.ADDGEO);
        addFence.putExtra("PendingIntent", mPendingIntent);
        addFence.putExtra("DBFenceID", addedFence.getId());
        addFence.putExtra("DBConditionFenceID", mconditionFence.getId());
        startService(addFence);
    }

    public void onClickRemoveFence(View view) {
        addedFence.deleteFromDB();
        Intent removeFence = new Intent(this, ConditionService.class);
        removeFence.setAction(ConditionService.REMOVEGEO);
        removeFence.putExtra("PendingIntent", mPendingIntent);
        startService(removeFence);
    }
}
