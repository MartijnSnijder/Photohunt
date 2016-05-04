package snijder.martijn.photohunt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import snijder.martijn.photohunt.models.ServerRequest;
import snijder.martijn.photohunt.models.ServerResponse;
import snijder.martijn.photohunt.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginFragment extends Fragment implements View.OnClickListener{
    private AppCompatButton btn_login;
    private EditText et_email,et_password,editold_password,editnew_password;
    private TextView tv_register,tv_reset_password, tv_message;
    private ProgressBar progress;
    private SharedPreferences pref;
    private DrawerLayout mDrawer;
    private LoginButton login;
    private ProfilePictureView profile;
    private CallbackManager callbackManager = null;
    private AccessTokenTracker mtracker = null;
    private ProfileTracker mprofileTracker = null;
    private String randompassword;
    private AlertDialog dialog;
    User user;

    FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            final Profile userprofile = Profile.getCurrentProfile();
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            Log.e("response: ", response + "");
                            try {
                                user = new User();
                                user.setFacebookID(object.getString("id").toString());
                                user.setEmail(object.getString("email").toString());
                                user.setName(object.getString("name").toString());
                                profile.setProfileId(user.getFacebookID());
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putBoolean(Constants.IS_LOGGED_IN, true);
                                editor.putString(Constants.EMAIL, user.getEmail());
                                editor.putString(Constants.NAME, user.getName());
                                editor.putString(Constants.UNIQUE_ID, user.getUnique_id());
                                editor.putString(Constants.FACEBOOK_ID, user.getFacebookID());
                                editor.apply();

                                NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.navigation_view);
                                View header = navigationView.getHeaderView(0);
                                TextView tv_namedrawer = (TextView) header.findViewById(R.id.tv_namedrawer);
                                tv_namedrawer.setText(pref.getString(Constants.NAME, ""));
                                TextView tv_emaildrawer = (TextView) header.findViewById(R.id.tv_emaildrawer);
                                tv_emaildrawer.setText(pref.getString(Constants.EMAIL, ""));

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            //Toast.makeText(getActivity(), getString(R.string.loggedin) + " " + user.getName(),Toast.LENGTH_SHORT).show();
                            randompassword = randomPassword();
                            facebookLoginProcess(user.getName(), user.getEmail(), randompassword, user.getFacebookID());
                        }

                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        mtracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.v("AccessTokenTracker", "oldAccessToken=" + oldAccessToken + "||" + "CurrentAccessToken" + currentAccessToken);
            }
        };

        mprofileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

                Log.v("Session Tracker", "oldProfile=" + oldProfile + "||" + "currentProfile" + currentProfile);

            }
        };

        mtracker.startTracking();
        mprofileTracker.startTracking();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        mDrawer = (DrawerLayout) this.getActivity().findViewById(R.id.drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        pref = getActivity().getPreferences(0);
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.navigation_view);
        View header = navigationView.getHeaderView(0);
        profile = (ProfilePictureView)header.findViewById(R.id.picture);
    }

    private void initViews(View view){

        pref = getActivity().getPreferences(0);
        btn_login = (AppCompatButton)view.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);

        tv_register = (TextView)view.findViewById(R.id.tv_register);
        tv_register.setOnClickListener(this);

        tv_reset_password = (TextView)view.findViewById(R.id.tv_reset_password);
        tv_reset_password.setOnClickListener(this);

        et_email = (EditText)view.findViewById(R.id.et_email);
        et_password = (EditText)view.findViewById(R.id.et_password);
        progress = (ProgressBar)view.findViewById(R.id.progress);

        login = (LoginButton) view.findViewById(R.id.login_button);
        login.setReadPermissions("public_profile", "email", "user_friends");
        login.setOnClickListener(this);
        login.setFragment(this);
        login.registerCallback(callbackManager, callback);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.tv_register:
                goToRegister();
                break;

            case R.id.btn_login:
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()) {

                    progress.setVisibility(View.VISIBLE);
                    loginProcess(email,password);

                } else {

                    Snackbar.make(getView(), R.string.fillfields, Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.tv_reset_password:
                goToResetPassword();
                break;
        }
    }

    private void showDialog(){

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.dialog_change_password, null);
    editold_password = (EditText)view.findViewById(R.id.et_old_password);
    editnew_password = (EditText)view.findViewById(R.id.et_new_password);
    editold_password.setText(randompassword);
    tv_message = (TextView)view.findViewById(R.id.tv_message);
    progress = (ProgressBar)view.findViewById(R.id.progress);
    builder.setView(view);
    builder.setTitle(R.string.addpass);
    builder.setPositiveButton(R.string.addpass, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });
    dialog = builder.create();
    dialog.show();
    dialog.setCancelable(false);
    dialog.setCanceledOnTouchOutside(false);
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String old_password = editold_password.getText().toString();
            String new_password = editnew_password.getText().toString();
            if(!old_password.isEmpty() && !new_password.isEmpty()){

                progress.setVisibility(View.VISIBLE);
                changePasswordProcess(pref.getString(Constants.EMAIL,""),old_password,new_password);

            }else {

                tv_message.setVisibility(View.VISIBLE);
                tv_message.setText(R.string.fillfields);
            }
        }
    });
}

    private void changePasswordProcess(String email,String old_password,String new_password){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        user.setOld_password(old_password);
        user.setNew_password(new_password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.CHANGE_PASSWORD_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                if (resp.getResult().equals(Constants.SUCCESS)) {
                    progress.setVisibility(View.GONE);
                    tv_message.setVisibility(View.GONE);
                    dialog.dismiss();
                    goToProfile();

                } else {
                    progress.setVisibility(View.GONE);
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText(R.string.errorpass);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG, "failed");
                progress.setVisibility(View.GONE);
                tv_message.setVisibility(View.VISIBLE);
                tv_message.setText(R.string.errorpass);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStop() {
        super.onStop();
        mtracker.stopTracking();
        mprofileTracker.stopTracking();
    }


    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isLoggedIn()) {

        }
    }

    public static String randomPassword() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 8; i < 18; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }



    private void facebookLoginProcess(String name, String email, String password, String facebookid) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        final User user2 = new User();
        user2.setName(name);
        user2.setEmail(email);
        user2.setPassword(password);
        user2.setFacebookID(facebookid);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.FACEBOOK_LOGIN);
        request.setUser(user2);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {
                ServerResponse resp = response.body();

                if (resp.getResult().equals(Constants.FBSUCCESS)) {
                    // Snackbar.make(getView(), "Facebook ID toegevoegd", Snackbar.LENGTH_LONG).show();
                    goToProfile();
                }

                else if (resp.getResult().equals(Constants.FBFAIL)){
                    Snackbar.make(getView(), R.string.fbcombi, Snackbar.LENGTH_LONG).show();
                }

                else if (resp.getResult().equals(Constants.NOEXIST)){
                    showDialog();
                }

                else {
                    Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                Log.d(Constants.TAG, "Error");
                Snackbar.make(getView(), t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void loginProcess(String email,String password){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.LOGIN_OPERATION);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();

                if(resp.getResult().equals(Constants.SUCCESS)){
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Constants.IS_LOGGED_IN,true);
                    editor.putString(Constants.EMAIL,resp.getUser().getEmail());
                    editor.putString(Constants.NAME,resp.getUser().getName());
                    editor.putString(Constants.UNIQUE_ID,resp.getUser().getUnique_id());
                    editor.apply();
                    goToProfile();

                }
                else{
                    Snackbar.make(getView(), R.string.invalidlogin, Snackbar.LENGTH_LONG).show();
                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Log.d(Constants.TAG,"failed");
                Snackbar.make(getView(), R.string.invalid, Snackbar.LENGTH_LONG).show();

            }
        });
    }

    private void goToResetPassword(){

        Fragment reset = new ResetPasswordFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,reset);
        ft.commit();
    }

    private void goToRegister(){

        Fragment register = new RegisterFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,register);
        ft.commit();
    }

    private void goToProfile(){

        Fragment profile = new ProfileFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,profile);
        ft.commit();
    }
}
