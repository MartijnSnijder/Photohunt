package snijder.martijn.photohunt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import snijder.martijn.photohunt.models.User;

/**
 * Created by SnijderMMartijn on 5-5-2016.
 */
public class HuntsFragment extends Fragment {

    private DrawerLayout mDrawer;
    private ListView listView;
    private ArrayList<String> hunts;
    private SharedPreferences pref;
    private String hunt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Hunts");
        mDrawer = (DrawerLayout) this.getActivity().findViewById(R.id.drawer);
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        View view = inflater.inflate(R.layout.fragment_hunts, container, false);

        hunts = new ArrayList<String>();
        String[] values = new String[] { "Maak een foto met een politieagent", "Maak een foto met een badeend",
                "Maak een foto terwijl je een pizza bakt", "Maak een foto met precies 7 mensen", "Maak een foto met een trein",
                "Maak een selfie in het midden van een rotonde", "Maak een foto met een bioscoop", "Maak een foto op een dak" };
        for (int i = 0; i < values.length; ++i) {
            hunts.add(values[i]);
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.activity_listview, hunts);
        listView = (ListView) view.findViewById(R.id.huntsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int item = position;
                hunt = hunts.get(item);
                String PREFS_NAME = "HUNT";
                String PREFS_KEY = "HUNT";
                SharedPreferences settings;
                SharedPreferences.Editor editor;
                settings = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                editor = settings.edit();
                editor.putString(PREFS_KEY, hunt);
                editor.commit();
                Fragment camera = new CameraFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_frame, camera);
                ft.commit();
            }
        });
        return view;
    }
}


