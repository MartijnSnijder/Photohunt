package snijder.martijn.photohunt;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import de.halfbit.tinybus.Subscribe;
import de.halfbit.tinybus.TinyBus;
import de.halfbit.tinybus.wires.ConnectivityWire;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private TinyBus mBus;
    private CoordinatorLayout coordinatorLayout;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getPreferences(0);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        checkPermission();
        initFragment();

        // Thirdparty library voor internetcheck
        mBus = TinyBus.from(this);
        if (savedInstanceState == null) {
            mBus.wire(new ConnectivityWire(ConnectivityWire.ConnectionStateEvent.class));
        }
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

            Toast.makeText(this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,fragment);
        ft.commit();
    }

}
