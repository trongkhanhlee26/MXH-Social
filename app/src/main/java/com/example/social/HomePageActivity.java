package com.example.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fragment.HomeFragment;
import fragment.MessageFragment;
import fragment.MyprofileFragment;
import fragment.NotificationsFragment;
import fragment.PostFragment;
import fragment.SearchFragment;
import fragment.SettingFragment;

public class HomePageActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView buttonDrawerToggle;
    NavigationView navigationView;
    private ImageView avtImage;
    private TextView tvUserName, tvUserEmail, tvUserStatus;

    private static final int FRAGMENT_HOME = 1;
    private static final int FRAGMENT_MYPROFILE = 2;
    private static final int FRAGMENT_POST = 3;
    private static final int FRAGMENT_SEARCH = 4;
    private static final int FRAGMENT_MESSAGE = 5;
    private static final int FRAGMENT_NOTIFICATIONS = 6;
    private static final int FRAGMENT_SETTING = 7;

    private int currentFragment = FRAGMENT_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);

        replaceFragment(new HomeFragment());
        currentFragment = FRAGMENT_HOME;

        buttonDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
                showUserInformation();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemId = menuItem.getItemId();

                if (itemId == R.id.navHome) {
                    Toast.makeText(HomePageActivity.this, "Home Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new HomeFragment());
                    currentFragment = FRAGMENT_HOME;
                }
                if (itemId == R.id.navMyProfile) {
                    Toast.makeText(HomePageActivity.this, "My Profile Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new MyprofileFragment());
                    currentFragment = FRAGMENT_MYPROFILE;
                }
                if (itemId == R.id.navPost) {
                    Toast.makeText(HomePageActivity.this, "Post Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new PostFragment());
                    currentFragment = FRAGMENT_POST;
                }
                if (itemId == R.id.navNotifications) {
                    Toast.makeText(HomePageActivity.this, "Notifications Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new NotificationsFragment());
                    currentFragment = FRAGMENT_NOTIFICATIONS;
                }
                if (itemId == R.id.navSearch) {
                    Toast.makeText(HomePageActivity.this, "Search Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new SearchFragment());
                    currentFragment = FRAGMENT_SEARCH;
                }
                if (itemId == R.id.navMessage) {
                    Toast.makeText(HomePageActivity.this, "Message Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new MessageFragment());
                    currentFragment = FRAGMENT_MESSAGE;
                }
                if (itemId == R.id.navSetting) {
                    Toast.makeText(HomePageActivity.this, "Setting Clicked!", Toast.LENGTH_SHORT).show();
                    replaceFragment(new SettingFragment());
                    currentFragment = FRAGMENT_SETTING;
                }
                if (itemId == R.id.navLogout) {
                    Toast.makeText(HomePageActivity.this, "Log out Clicked!", Toast.LENGTH_SHORT).show();
                    updateOnlineStatus(false);
                    resetToDefaultMode();
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(HomePageActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                drawerLayout.close();
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            switch (currentFragment) {
                case FRAGMENT_HOME:
                    Toast.makeText(this, "You are on the main page", Toast.LENGTH_SHORT).show();
                    return;
                case FRAGMENT_MYPROFILE:
                case FRAGMENT_POST:
                case FRAGMENT_SEARCH:
                case FRAGMENT_MESSAGE:
                case FRAGMENT_NOTIFICATIONS:
                case FRAGMENT_SETTING:
                    replaceFragment(new HomeFragment());
                    currentFragment = FRAGMENT_HOME;
                    return;
            }
        }
        super.onBackPressed();
    }

    private void initUi() {
        navigationView = findViewById(R.id.nagivationView);
        avtImage = navigationView.getHeaderView(0).findViewById(R.id.userImage);
        tvUserName = navigationView.getHeaderView(0).findViewById(R.id.textUserName);
        tvUserEmail = navigationView.getHeaderView(0).findViewById(R.id.textUserEmail);
        tvUserStatus = navigationView.getHeaderView(0).findViewById(R.id.textUserStatus);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame,fragment);
        fragmentTransaction.commit();
    }

    private void showUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullname = dataSnapshot.child("fullName").getValue(String.class);
                    tvUserName.setText(fullname);

                    String imageUrl = dataSnapshot.child("image").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(HomePageActivity.this).load(imageUrl).error(R.drawable.avatar).into(avtImage);
                    } else {
                        avtImage.setImageResource(R.drawable.avatar);
                    }

                    boolean onlineStatus = dataSnapshot.child("onlineStatus").getValue(Boolean.class);
                    if (onlineStatus) {
                        tvUserStatus.setText("Online");
                    } else {
                        tvUserStatus.setText("Offline");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load user information", Toast.LENGTH_SHORT).show();
            }
        });

        String email = user.getEmail();
        tvUserEmail.setText(email);
    }

    private void updateOnlineStatus(boolean onlineStatus) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            userRef.child("onlineStatus").setValue(onlineStatus);
        }
    }

    private void resetToDefaultMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateOnlineStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateOnlineStatus(false);
    }
}
