package com.bloomfield.warrington.doyouknow;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Warrington on 4/24/17.
 */

public class Fragment1 extends Fragment {

    private FloatingActionButton fab;
    EditText input;
    private DatabaseReference mPostReference;

    private FirebaseListAdapter<ChatMessage> adapter;
    private Handler myListView= new Handler(Looper.myLooper());
    private ListView listOfMessages;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment, container, false);
        input = (EditText) view.findViewById(R.id.input);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollMyListViewToBottom();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fab = (FloatingActionButton) getView().findViewById(R.id.fab);
        listOfMessages = (ListView)getView().findViewById(R.id.list_of_messages);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String usingUrl = String.valueOf(user.getPhotoUrl());

                if(usingUrl.equals("null")){
                    usingUrl = "";
                }

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName(), usingUrl)
                        );

                // Clear the input
                input.setText("");

                adapter.notifyDataSetChanged();
                scrollMyListViewToBottom();

                Log.d("TokenTag", "t: "+ FirebaseInstanceId.getInstance().getToken());

            }
        });

        adapter = new FirebaseListAdapter<ChatMessage>(MainActivity.mainActivity, ChatMessage.class, R.layout.messages, FirebaseDatabase.getInstance().getReference()) {

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

                if(testing==null || testing.equals("") || testing.equals("null") && GlobalApplication.globalContext()!=null){
                    Glide.with(MainActivity.mainActivity).load("").asBitmap().placeholder(R.drawable.blank).centerCrop().into(new BitmapImageViewTarget(avatar) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(GlobalApplication.globalContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                }else{

                    Glide.with(MainActivity.mainActivity).load(testing).asBitmap().centerCrop().into(new BitmapImageViewTarget(avatar) {
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

        FirebaseMessaging.getInstance().subscribeToTopic("updates");
        mPostReference = FirebaseDatabase.getInstance().getReference();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Object post = dataSnapshot.getValue();
                Log.d("Test", "onDataChange: ");
                scrollMyListViewToBottom();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.d("Test", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mPostReference.addValueEventListener(postListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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
