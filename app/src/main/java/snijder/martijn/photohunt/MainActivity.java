package snijder.martijn.photohunt;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;

import java.util.ArrayList;

import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;
import de.halfbit.tinybus.wires.ConnectivityWire;
import snijder.martijn.photohunt.models.User;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private TinyBus mBus;
    private CoordinatorLayout coordinatorLayout;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ProfilePictureView profile;
    private TextView name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer();
        pref = getPreferences(0);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        checkPermission();
        initFragment();
        setDrawerItems();

        // Thirdparty library voor internetcheck
        mBus = TinyBus.from(this);
        if (savedInstanceState == null) {
            mBus.wire(new ConnectivityWire(ConnectivityWire.ConnectionStateEvent.class));
        }
    }

    public void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.home:
                        drawerLayout.closeDrawers();
                        Fragment profile = new ProfileFragment();
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_frame, profile);
                        ft.commit();
                        break;
                    case R.id.friends:
                        drawerLayout.closeDrawers();
                        FriendsFragment friends = new FriendsFragment();
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_frame, friends);
                        ft.commit();
                        break;
                    case R.id.settings:
                        drawerLayout.closeDrawers();
                        profile = new ProfileFragment();
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_frame, profile);
                        ft.commit();
                        break;
                    case R.id.logout:
                        logoutDialog();
                        break;
                }
                return true;
            }
        });
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.changepass,R.string.storagepermission){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            requestPermission();
            return false;
        }
    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            Toast.makeText(this,R.string.storagepermission, Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    protected void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    public void setDrawerItems() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View header = navigationView.getHeaderView(0);
        profile = (ProfilePictureView)header.findViewById(R.id.picture);
        name = (TextView)header.findViewById(R.id.tv_namedrawer);
        email = (TextView)header.findViewById(R.id.tv_emaildrawer);
        try {
            profile.setProfileId(pref.getString(Constants.FACEBOOK_ID, ""));
            name.setText(pref.getString(Constants.NAME, ""));
            email.setText(pref.getString(Constants.EMAIL, ""));
        }
        catch (Exception e)
        {}
    }

    @Subscribe
    public void onConnectivityEvent(ConnectivityWire.ConnectionStateEvent event) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, getString(R.string.noconnection), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        if (!event.isConnected()) {
            snackbar.show();
        } else {
            snackbar.dismiss();
        }
    }

    private void initFragment(){
        Fragment fragment;
        if(pref.getBoolean(Constants.IS_LOGGED_IN,false)){
            fragment = new ProfileFragment();
        }else {
            fragment = new LoginFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, fragment);
        ft.commit();
    }

    protected void logoutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.logquest)
                .setCancelable(true)
                .setPositiveButton(R.string.log, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    protected void logout() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constants.IS_LOGGED_IN, false);
        editor.putString(Constants.EMAIL, "");
        editor.putString(Constants.NAME, "");
        editor.putString(Constants.UNIQUE_ID, "");
        editor.putString(Constants.FACEBOOK_ID, "");
        editor.apply();
        name.setText("");
        email.setText("");
        LoginManager.getInstance().logOut();
        Fragment login = new LoginFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame, login);
        ft.commit();
    }
}
