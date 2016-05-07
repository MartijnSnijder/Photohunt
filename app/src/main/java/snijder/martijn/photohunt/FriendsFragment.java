package snijder.martijn.photohunt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


/**
 * Created by SnijderMMartijn on 5-5-2016.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener {

    private DrawerLayout mDrawer;
    private AppCompatButton btn_invite;
    private String appLinkUrl, appPreviewImage, naam, afbeelding;
    private ListView listView;
    private ArrayList<String> names, pictures;
    private TextView tv_message;
    private ProfilePictureView profilepicture;
    private AlertDialog dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.friends);
        mDrawer = (DrawerLayout) this.getActivity().findViewById(R.id.drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        btn_invite = (AppCompatButton) view.findViewById(R.id.btn_invite);
        btn_invite.setOnClickListener(this);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        JSONArray friendslist;
                        names = new ArrayList<String>();
                        pictures = new ArrayList<String>();
                        try {
                            JSONArray rawName = response.getJSONObject().getJSONArray("data");
                            friendslist = rawName;
                            for (int l = 0; l < friendslist.length(); l++) {
                                names.add(friendslist.getJSONObject(l).getString("name"));
                                pictures.add(friendslist.getJSONObject(l).getString("id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_listview, names);
                        listView = (ListView) getActivity().findViewById(R.id.listView);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                int item = position;
                                naam = names.get(item);
                                afbeelding = pictures.get(item);
                                showDialog();
                            }
                        });
                    }
                }
        ).executeAsync();
        return view;
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_profile, null);
        tv_message = (TextView)view.findViewById(R.id.tv_message);
        profilepicture = (ProfilePictureView)view.findViewById(R.id.profilepicture);
        builder.setView(view);
        builder.setTitle(R.string.profile);
        profilepicture.setProfileId(afbeelding);
        tv_message.setText(naam);
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_invite:

                appLinkUrl = "https://fb.me/506547789542967";
                appPreviewImage = "http://s32.postimg.org/d5asi8fnp/logopreview.jpg";

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(appLinkUrl)
                            .setPreviewImageUrl(appPreviewImage)
                            .build();
                    AppInviteDialog.show(this, content);
                }
                break;
        }
    }
}


