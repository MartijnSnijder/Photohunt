package snijder.martijn.photohunt;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by SnijderMMartijn on 5-5-2016.
 */
public class FriendsFragment extends Fragment {

    private DrawerLayout mDrawer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        mDrawer = (DrawerLayout) this.getActivity().findViewById(R.id.drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        return view;
    }
}
