package fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.social.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {
    Switch switcher;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        switcher = view.findViewById(R.id.switchmode);

        String userId = getCurrentUserId(); // Phương thức lấy ID người dùng hiện tại
        sharedPreferences = requireActivity().getSharedPreferences("USER_PREF_" + userId, Context.MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("night_mode", false);

        switcher.setChecked(nightMode);

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                editor = sharedPreferences.edit();
                editor.putBoolean("night_mode", isChecked);
                editor.apply();
            }
        });

        return view;
    }

    private String getCurrentUserId() {
        // Implement this method to return the current user's ID
        // For example, from a singleton UserManager or similar
        // return UserManager.getInstance().getCurrentUserId();
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
