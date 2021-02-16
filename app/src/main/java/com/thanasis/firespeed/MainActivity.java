package com.thanasis.firespeed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class MainActivity extends AppCompatActivity {
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, signOut, doChangeDisplayName, changeDisplayName;
    private TextView welcomeMsg;

    private EditText oldEmail, newEmail, password, newPassword, displayName;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = firebaseAuth -> {
            FirebaseUser user1 = firebaseAuth.getCurrentUser();
            if (user1 == null) {
                // user auth state is changed - user is null
                // launch login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        };

        welcomeMsg = findViewById(R.id.welcome_msg);
        // Open form buttons
        btnChangeEmail = findViewById(R.id.change_email_button);
        btnChangePassword = findViewById(R.id.change_password_button);
        btnSendResetEmail = findViewById(R.id.sending_pass_reset_button);
        changeDisplayName = findViewById(R.id.change_name_button);

        // Action Buttons
        btnRemoveUser = findViewById(R.id.remove_user_button);
        changeEmail = findViewById(R.id.changeEmail);
        changePassword = findViewById(R.id.changePass);
        sendEmail = findViewById(R.id.send);
        doChangeDisplayName = findViewById(R.id.change_name);
        signOut = findViewById(R.id.sign_out);

        // Edit text fields
        oldEmail = findViewById(R.id.old_email);
        newEmail = findViewById(R.id.new_email);
        password = findViewById(R.id.password);
        newPassword = findViewById(R.id.newPassword);
        displayName = findViewById(R.id.display_name);

        // Hide forms
        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        displayName.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);
        doChangeDisplayName.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavBar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.ic_home:
                    break;
                case R.id.ic_speeder:
                    Bundle bundleSpeeder = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
                    Intent intentSpeeder = new Intent(MainActivity.this, SpeedometerActivity.class);
                    startActivity(intentSpeeder, bundleSpeeder);
                    break;
                case R.id.ic_list:
                    Bundle bundleList = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
                    Intent intentList = new Intent(MainActivity.this, ListActivity.class);
                    startActivity(intentList, bundleList);
                    break;
                case R.id.ic_map:
                    Bundle bundleMap = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
                    Intent intentMap = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intentMap, bundleMap);
                    break;
            }
            return false;
        });

        welcomeMsg.setText("Welcome " + user.getDisplayName());
        displayName.setText(user.getDisplayName());

        changeDisplayName.setOnClickListener(v -> {
            oldEmail.setVisibility(View.GONE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            displayName.setVisibility(View.VISIBLE);

            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.GONE);
            doChangeDisplayName.setVisibility(View.VISIBLE);

        });

        doChangeDisplayName.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            doChangeDisplayName.setVisibility(View.GONE);
            if (user != null && !displayName.getText().toString().trim().equals("")) {
                updateUserDetails(displayName.getText().toString().trim(), user);

            } else if (displayName.getText().toString().trim().equals("")) {
                displayName.setError("Enter Display Name");
                progressBar.setVisibility(View.GONE);
                doChangeDisplayName.setVisibility(View.VISIBLE);
            }
        });

        btnChangeEmail.setOnClickListener(v -> {
            oldEmail.setVisibility(View.VISIBLE);
            newEmail.setVisibility(View.VISIBLE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            displayName.setVisibility(View.GONE);

            changeEmail.setVisibility(View.VISIBLE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.GONE);
            doChangeDisplayName.setVisibility(View.GONE);
        });


        changeEmail.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            changeEmail.setVisibility(View.GONE);
            if (user != null && !newEmail.getText().toString().trim().equals("")) {
                user.updateEmail(newEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                    signOut();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                    changeEmail.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            } else if (newEmail.getText().toString().trim().equals("")) {
                newEmail.setError("Enter email");
                progressBar.setVisibility(View.GONE);
            }
        });

        btnChangePassword.setOnClickListener((v) -> {
            oldEmail.setVisibility(View.GONE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.VISIBLE);
            newPassword.setVisibility(View.VISIBLE);
            displayName.setVisibility(View.GONE);

            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.VISIBLE);
            sendEmail.setVisibility(View.GONE);
            doChangeDisplayName.setVisibility(View.GONE);
        });

        changePassword.setOnClickListener((v)-> {
            changePassword.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            if (user != null && !newPassword.getText().toString().trim().equals("")) {
                if (newPassword.getText().toString().trim().length() < 6) {
                    newPassword.setError("Password too short, enter minimum 6 characters");
                    progressBar.setVisibility(View.GONE);
                    changePassword.setVisibility(View.VISIBLE);
                } else {
                    user.updatePassword(newPassword.getText().toString().trim())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Password is updated, sign in with the new password!", Toast.LENGTH_SHORT).show();
                                    signOut();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    changePassword.setVisibility(View.VISIBLE);
                                }
                            });
                }
            } else if (newPassword.getText().toString().trim().equals("")) {
                newPassword.setError("Enter password");
                progressBar.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
            }
        });

        btnSendResetEmail.setOnClickListener((v)->{
            oldEmail.setVisibility(View.VISIBLE);
            newEmail.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            newPassword.setVisibility(View.GONE);
            displayName.setVisibility(View.GONE);

            changeEmail.setVisibility(View.GONE);
            changePassword.setVisibility(View.GONE);
            sendEmail.setVisibility(View.VISIBLE);
            doChangeDisplayName.setVisibility(View.GONE);
        });

        sendEmail.setOnClickListener((v)->{
            sendEmail.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            if (!oldEmail.getText().toString().trim().equals("")) {
                auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Reset password email is sent!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                sendEmail.setVisibility(View.VISIBLE);
                            }
                        });
            } else {
                oldEmail.setError("Enter email");
                progressBar.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
            }

        });

        btnRemoveUser.setOnClickListener((v)->{
            progressBar.setVisibility(View.VISIBLE);
            if (user != null) {
                user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Your profile is deleted.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                            finish();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            }
        });

        signOut.setOnClickListener(v -> signOut());

    }
    private void updateUserDetails(String displayName, final FirebaseUser user){
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        user.updateProfile(profileChangeRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "User display name updated!", Toast.LENGTH_SHORT).show();
                        welcomeMsg.setText("Welcome " + user.getDisplayName());
                        doChangeDisplayName.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }else {
                        Toast.makeText(MainActivity.this,"Something went wrong.",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

//    public boolean isServiceOk(){
//        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//
//        if(available == ConnectionResult.SUCCESS) {
//            Toast.makeText(this, "Maps Up And Running", Toast.LENGTH_LONG).show();
//            return true;
//        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
//            Toast.makeText(this, "Fixable error occurred", Toast.LENGTH_LONG).show();
//            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, ERROR_DIALOG_REQUEST);
//            dialog.show();
//        }else {
//            Toast.makeText(this, "Fatal Error. Can't make map requests.", Toast.LENGTH_LONG).show();
//        }
//        return false;
//    }
}
