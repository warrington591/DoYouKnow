package com.bloomfield.warrington.doyouknow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsSpinner;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "MainActivity";
    private FloatingActionButton fab;
    public static Activity mainActivity;

    private DatabaseReference mPostReference;
    private FirebaseListAdapter<ChatMessage> adapter;
    private String facebookUserId="";
    private List<? extends UserInfo> now = new ArrayList<>();
    private Handler myListView= new Handler(Looper.myLooper());
    private ListView listOfMessages;
    private FirebaseUser user;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                        }
                    });
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
//        fab = (FloatingActionButton)findViewById(R.id.fab);
//        listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager(), getApplicationContext()));
        tabLayout =(TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Toast.makeText(getApplicationContext(),
                            "Welcome " + FirebaseAuth.getInstance()
                                    .getCurrentUser()
                                    .getDisplayName(),
                            Toast.LENGTH_LONG)
                            .show();
//
//                    // Load chat room contents
//                    displayChatMessages();
//                    scrollMyListViewToBottom();
//

                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                    .setLogo(R.drawable.temp_logo)
                                    .build(),SIGN_IN_REQUEST_CODE
                    );


                }
            }
        };



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "response is: "+response.toString());
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
//                displayChatMessages();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this,
                            "Sign-in canceled",
                            Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "response is: sign in failed");

                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this,
                            "No internet connection",
                            Toast.LENGTH_LONG)
                            .show();
                    Log.d(TAG, "response is: no internet connection");

                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this,
                            "Unknown Error",
                            Toast.LENGTH_LONG)
                            .show();

                    Log.d(TAG, "response is: unknown error");

                    return;
                }
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mainActivity = this;
        mAuth.addAuthStateListener(mAuthListener);
        if(user!=null){
        FirebaseMessaging.getInstance().subscribeToTopic("updates");}

        Log.d(TAG, "t: "+ FirebaseInstanceId.getInstance().getToken());
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.messages, FirebaseDatabase.getInstance().getReference()) {

            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                final ImageView avatar = (ImageView) v.findViewById(R.id.avatar);
                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                String testing  = model.getImage();

                if(testing==null || testing.equals("") || testing.equals("null")){
                    Glide.with(getApplicationContext()).load("").asBitmap().placeholder(R.drawable.blank).centerCrop().into(new BitmapImageViewTarget(avatar) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }else{

                    Glide.with(getApplicationContext()).load(testing).asBitmap().centerCrop().into(new BitmapImageViewTarget(avatar) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }


                // Format the date before showing it
                messageTime.setText(DateFormat.format("hh:mm a", model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }


    private void scrollMyListViewToBottom() {
        myListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listOfMessages.setSelection(listOfMessages.getCount() - 1);
            }
        });
    }
}
