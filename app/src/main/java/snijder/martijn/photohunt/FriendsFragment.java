package snijder.martijn.photohunt;

import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


/**
 * Created by SnijderMMartijn on 5-5-2016.
 */
public class FriendsFragment extends Fragment implements View.OnClickListener{

    private DrawerLayout mDrawer;
    private AppCompatButton btn_invite;
    private String appLinkUrl, appPreviewImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.friends);
        mDrawer = (DrawerLayout) this.getActivity().findViewById(R.id.drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        btn_invite = (AppCompatButton)view.findViewById(R.id.btn_invite);
        btn_invite.setOnClickListener(this);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {

                        JSONArray friendslist;
                        ArrayList<String> names = new ArrayList<String>();
                        ArrayList<String> pictures = new ArrayList<String>();
                        try {
                            JSONArray rawName = response.getJSONObject().getJSONArray("data");
                            friendslist = rawName;
                            for (int l=0; l < friendslist.length(); l++) {
                                names.add(friendslist.getJSONObject(l).getString("name"));
                                pictures.add(friendslist.getJSONObject(l).getString("id"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_listview, names);
                        ListView listView = (ListView) getActivity().findViewById(R.id.listView);
                        listView.setAdapter(adapter);
                    }
                }
        ).executeAsync();
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

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
