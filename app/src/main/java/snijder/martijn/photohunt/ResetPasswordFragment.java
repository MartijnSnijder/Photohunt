package snijder.martijn.photohunt;

/**
 * Created by SnijderMMartijn on 2-5-2016.
 */
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import snijder.martijn.photohunt.models.ServerRequest;
import snijder.martijn.photohunt.models.ServerResponse;
import snijder.martijn.photohunt.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResetPasswordFragment extends Fragment implements View.OnClickListener{

    private AppCompatButton btn_reset;
    private EditText et_email,et_code,et_password;
    private TextView tv_timer;
    private TextView tv_remaining;
    private ProgressBar progress;
    private boolean isResetInitiated = false;
    private String email;
    private CountDownTimer countDownTimer;
    private InputMethodManager imm;
    private DrawerLayout mDrawer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_reset,container,false);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        mDrawer = (DrawerLayout) this.getActivity().findViewById(R.id.drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        initViews(view);
        return view;
    }

    private void initViews(View view){

        btn_reset = (AppCompatButton)view.findViewById(R.id.btn_reset);
        tv_timer = (TextView)view.findViewById(R.id.timer);
        tv_remaining = (TextView)view.findViewById(R.id.tv_remaining);
        et_code = (EditText)view.findViewById(R.id.et_code);
        et_email = (EditText)view.findViewById(R.id.et_email);
        et_password = (EditText)view.findViewById(R.id.et_password);
        et_password.setVisibility(View.GONE);
        et_code.setVisibility(View.GONE);
        tv_timer.setVisibility(View.GONE);
        tv_remaining.setVisibility(View.GONE);
        btn_reset.setOnClickListener(this);
        progress = (ProgressBar)view.findViewById(R.id.progress);

    }

    @Override
    public void onClick(View v) {
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        switch (v.getId()){

            case R.id.btn_reset:

                if(!isResetInitiated) {

                    email = et_email.getText().toString();
                    if (!email.isEmpty()) {
                        progress.setVisibility(View.VISIBLE);
                        initiateResetPasswordProcess(email);
                    } else {

                        Snackbar.make(getView(), R.string.fillfields, Snackbar.LENGTH_LONG).show();
                    }
                } else {

                    String code = et_code.getText().toString();
                    String password = et_password.getText().toString();

                    if(!code.isEmpty() && !password.isEmpty()){

                        finishResetPasswordProcess(email,code,password);
                    } else {

                        Snackbar.make(getView(), R.string.fillfields, Snackbar.LENGTH_LONG).show();
                    }

                }

                break;
        }
    }

    private void initiateResetPasswordProcess(String email){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_INITIATE);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if(resp.getResult().equals(Constants.SUCCESS)){

                    Snackbar.make(getView(), R.string.checkemail, Snackbar.LENGTH_LONG).show();
                    et_email.setVisibility(View.GONE);
                    et_code.setVisibility(View.VISIBLE);
                    et_password.setVisibility(View.VISIBLE);
                    tv_remaining.setVisibility(View.VISIBLE);
                    tv_timer.setVisibility(View.VISIBLE);
                    btn_reset.setText(R.string.changepass);
                    isResetInitiated = true;
                    startCountdownTimer();

                } else if(resp.getResult().equals(Constants.NOEMAIL)) {

                    Snackbar.make(getView(), R.string.noemail, Snackbar.LENGTH_LONG).show();

                }

                else {
                    Snackbar.make(getView(), "Error", Snackbar.LENGTH_LONG).show();

                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Log.d(Constants.TAG,"failed");
                Snackbar.make(getView(), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();

            }
        });
    }

    private void finishResetPasswordProcess(String email,String code, String password){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);

        User user = new User();
        user.setEmail(email);
        user.setCode(code);
        user.setPassword(password);
        ServerRequest request = new ServerRequest();
        request.setOperation(Constants.RESET_PASSWORD_FINISH);
        request.setUser(user);
        Call<ServerResponse> response = requestInterface.operation(request);

        response.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, retrofit2.Response<ServerResponse> response) {

                ServerResponse resp = response.body();
                Snackbar.make(getView(), resp.getMessage(), Snackbar.LENGTH_LONG).show();

                if(resp.getResult().equals(Constants.SUCCESS)){

                    Snackbar.make(getView(), R.string.succespass, Snackbar.LENGTH_LONG).show();
                    countDownTimer.cancel();
                    isResetInitiated = false;
                    goToLogin();

                } else {

                    Snackbar.make(getView(), R.string.errorpass, Snackbar.LENGTH_LONG).show();

                }
                progress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

                progress.setVisibility(View.INVISIBLE);
                Log.d(Constants.TAG,"failed");
                Snackbar.make(getView(), t.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();

            }
        });
    }

    private void startCountdownTimer(){
        countDownTimer = new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                String timer = new String("" + millisUntilFinished / 1000);
                tv_timer.setText(timer);
            }

            public void onFinish() {
                try {
                    Snackbar.make(getView(), R.string.timeout, Snackbar.LENGTH_LONG).show();
                    goToLogin();
                }
                catch (Exception e)
                {
                    //Todo dingen
                }
            }
        }.start();
    }

    private void goToLogin(){

        Fragment login = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_frame,login);
        ft.commit();
    }
}