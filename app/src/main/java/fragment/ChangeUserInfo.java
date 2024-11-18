package fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.social.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeUserInfo extends AppCompatActivity {

    private EditText newNameEditText, newBirthEditText;
    private RadioGroup genderRadioGroup;
    private Button changeNowButton;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);

        // Khởi tạo Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userRef = database.getReference("Users").child(userId);
        }
        newNameEditText = findViewById(R.id.newFullName);
        newBirthEditText = findViewById(R.id.birth);
        genderRadioGroup = findViewById(R.id.gender);
        changeNowButton = findViewById(R.id.changeNowBtn);

        changeNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = newNameEditText.getText().toString();
                String newBirth = newBirthEditText.getText().toString();
                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();

                if (selectedGenderId != -1) {
                    RadioButton selectedGenderButton = findViewById(selectedGenderId);
                    String newGender = selectedGenderButton.getText().toString();

                    if (!TextUtils.isEmpty(newName) && !TextUtils.isEmpty(newBirth)) {

                        if (containsNumber(newName)) {
                            Toast.makeText(ChangeUserInfo.this, "Full Name cannot contain numbers!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userRef.child("fullName").setValue(newName);
                        userRef.child("dateOfBirth").setValue(newBirth);
                        userRef.child("gender").setValue(newGender)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Cập nhật thành công
                                            Toast.makeText(ChangeUserInfo.this, "User information has been updated", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            // Xử lý khi cập nhật thất bại
                                            Toast.makeText(ChangeUserInfo.this, "Updating user information failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(ChangeUserInfo.this, "Please complete all information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangeUserInfo.this, "Please select gender", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean containsNumber(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

}
