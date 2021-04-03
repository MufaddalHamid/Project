package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.Model.DriverInfoModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.internal.RegisterListenerMethod;
import com.google.android.gms.common.api.internal.RegistrationMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference driverInfoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        delaysplashscreen();
    }

    @Override
    protected void onStop() {
        if(firebaseAuth != null && listener !=  null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }

    private void init() {
        ButterKnife.bind(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        driverInfoRef=firebaseDatabase.getReference(Common.Driver_Info_Reference);
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaaseAuth -> {
            FirebaseUser user = myFirebaaseAuth.getCurrentUser();
            if (user != null)
               {
                  CheckUserFromFirebase();
               }
            else
                showLoginLayout();
        };
    }
    private void CheckUserFromFirebase()
    {
        driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            Toast.makeText(SplashScreenActivity.this,"User registered",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SplashScreenActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.DialogTheme);
        View itemview= LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        TextInputEditText edt_first_name=(TextInputEditText)itemview.findViewById(R.id.edt_first_name);
        TextInputEditText edt_last_name=(TextInputEditText)itemview.findViewById(R.id.edt_last_name);
        TextInputEditText edt_phone_number=(TextInputEditText)itemview.findViewById(R.id.edt_phone_number);
        Button btn_continue=(Button)itemview.findViewById(R.id.btn_register);

        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()!=null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
        edt_phone_number.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());

        builder.setView(itemview);
        AlertDialog dialog=builder.create();
        dialog.show();
        btn_continue.setOnClickListener(v -> {
               if(TextUtils.isEmpty(edt_first_name.getText().toString()))
               {
                   Toast.makeText(this,"Enter First Name",Toast.LENGTH_SHORT).show();
               }
              else if(TextUtils.isEmpty(edt_last_name.getText().toString()))
                {
                    Toast.makeText(this,"Enter last Name",Toast.LENGTH_SHORT).show();
                }
              else if(TextUtils.isEmpty(edt_phone_number.getText().toString()))
                {
                    Toast.makeText(this,"Enter phone Number",Toast.LENGTH_SHORT).show();
                }
              else
               {
                   DriverInfoModel driverInfoModel =new DriverInfoModel();
                   driverInfoModel.setFirstname(edt_first_name.getText().toString());
                   driverInfoModel.setLastname(edt_last_name.getText().toString());
                   driverInfoModel.setPhonenumber(edt_phone_number.getText().toString());
                   driverInfoModel.setRating(0.0);
                   driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(driverInfoModel)
                           .addOnFailureListener(e ->
                           {
                               dialog.dismiss();
                               Toast.makeText(SplashScreenActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                           })
                           .addOnSuccessListener(aVoid -> {
                               dialog.dismiss();
                               Toast.makeText(SplashScreenActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                           });


               }
        });
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAuthMethodPickerLayout(authMethodPickerLayout)
        .setIsSmartLockEnabled(false).setTheme(R.style.LoginTheme)
        .setAvailableProviders(providers)
        .build(),LOGIN_REQUEST_CODE);
    }

    private void delaysplashscreen(){

        progressBar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(() -> firebaseAuth.addAuthStateListener(listener));
     //10:20 line 83
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else
            {
                Toast.makeText(this,"[ERROR]: "+response.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
}