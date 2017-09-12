package security.car.alarm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import security.fragment.AboutSectionFragment;
import security.fragment.EventsSectionFragment;
import security.fragment.SettingsSectionFragment;
import security.fragment.StatusSectionFragment;
import security.service.CarAlarmGuard;
import security.utils.CarAlarmServiceConnection;
import security.utils.Const;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private CarAlarmServiceConnection mConnection = null;
    private Fragment fragment = null;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private SharedPreferences prefs;
    private Context mContext=null;
    private int mSection=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Avvio il Servizio di Allarme
        Intent serviceIntent = new Intent(this,CarAlarmGuard.class);
        startService(serviceIntent);

        mContext=getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            Const.PERMISSION_LOCATION_REQUEST_CODE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            if (ActivityCompat.checkSelfPermission(mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Const.PERMISSION_WRITE_EXTERNAL_STORAGE_CODE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Const.PERMISSION_CALL_PHONE_CODE);
            }

        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(this.getClass().getCanonicalName(),"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mConnection!=null) mConnection.destroy();
    }

    @Override
    protected void onResume() {
        new Thread(new MainThread()).start();

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_status) {
            // Handle the camera action
            fragment = new StatusSectionFragment();
            ((StatusSectionFragment)fragment).setContext(this,mConnection);
            mSection=0;
        } else if (id == R.id.nav_logs) {
            fragment = new EventsSectionFragment();
            ((EventsSectionFragment)fragment).setContext(this,mConnection);
            mSection=1;
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsSectionFragment();
            ((SettingsSectionFragment)fragment).setContext(this,mConnection);
            mSection=2;
        } else if (id == R.id.nav_about) {
            fragment = new AboutSectionFragment();
            ((AboutSectionFragment)fragment).setContext(this,mConnection);
            mSection=3;
        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.glm.caralarm");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Do You have an old smartphone, do you want to protect your car, motorcycle, scooter from thieves!\n" +
                    "\n" +
                    "Car Alarm Guard is what you're looking for!\n" +
                    "\n");
            startActivity(Intent.createChooser(intent, "Share"));
        } else if (id == R.id.nav_rate) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.glm.caralarm")));
        }else if(id == R.id.nav_support){
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"glmlabs2011@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "SUPPORT - Car Alarm");
            email.putExtra(Intent.EXTRA_TEXT, "Request support for ");
            email.setType("message/rfc822");
            startActivity(Intent.createChooser(email, "Choose an Email client :"));
        }

        if (fragment != null) {

            mFragmentManager.beginTransaction().setCustomAnimations(R.anim.in_from_left,R.anim.out_to_left).replace(R.id.main_content, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class MainThread implements Runnable{

        @Override
        public void run() {
            prefs = mContext.getSharedPreferences("carAlarm", Context.MODE_ENABLE_WRITE_AHEAD_LOGGING);
            mConnection = new CarAlarmServiceConnection(mContext);

            if (mSection==0) {
                // Handle the camera action
                fragment = new StatusSectionFragment();
                ((StatusSectionFragment)fragment).setContext(mContext,mConnection);

            } else if (mSection==1) {
                fragment = new EventsSectionFragment();
                ((EventsSectionFragment)fragment).setContext(mContext,mConnection);
                mSection=1;
            } else if (mSection==2) {
                fragment = new SettingsSectionFragment();
                ((SettingsSectionFragment)fragment).setContext(mContext,mConnection);
                mSection=2;
            } else if (mSection==3) {
                fragment = new AboutSectionFragment();
                ((AboutSectionFragment)fragment).setContext(mContext,mConnection);
                mSection=3;
            }
            if (fragment != null) {
                   mFragmentManager.beginTransaction().setCustomAnimations(R.anim.in_from_left,R.anim.out_to_left).replace(R.id.main_content, fragment).commit();
            }
        }
    }
}
