package com.gady.pengwings;

import java.lang.*;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by Andy on 2015-02-21.
 */
public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private ImageView chatHead;
    private ImageView removeChatHead;
    private TextView message;
    private WindowManager.LayoutParams chatHeadParams;
    private WindowManager.LayoutParams removeChatHeadParams;
    private WindowManager.LayoutParams menuParams;
    private WindowManager.LayoutParams messageParams;
    private Point size;
    private View menuView;
    private LayoutInflater inflater;
    private Handler mHandler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        size = new Point();
        windowManager.getDefaultDisplay().getSize(size);

        // Add the view for the menu
        inflater = LayoutInflater.from(this);
        menuView = inflater.inflate(R.layout.layout_menu, null);
        menuView.setVisibility(View.GONE);

        menuParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        windowManager.addView(menuView, menuParams);

        // Add the view for the thing that removes the chat head
        removeChatHead = new ImageView(this);
        removeChatHead.setImageResource(R.drawable.iglooicon);
        removeChatHead.setAdjustViewBounds(true);
        removeChatHead.setMaxWidth(150);
        removeChatHead.setMaxHeight(150);
        removeChatHead.setVisibility(View.GONE);

        removeChatHeadParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        removeChatHeadParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        removeChatHeadParams.x = 0;
        removeChatHeadParams.y = 45;

        windowManager.addView(removeChatHead, removeChatHeadParams);

        // Add the view for the chat head
        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.private_left);
        chatHead.setAdjustViewBounds(true);
        chatHead.setMaxWidth(150);
        chatHead.setMaxHeight(150);

        chatHeadParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        chatHeadParams.gravity = Gravity.TOP | Gravity.LEFT;
        chatHeadParams.x = 0;
        chatHeadParams.y = 10;

        windowManager.addView(chatHead, chatHeadParams);

        // Add the message box for the chat head
        message = new TextView(this);
        message.setVisibility(View.GONE);

        messageParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        messageParams.gravity = Gravity.TOP | Gravity.LEFT;
        messageParams.x = chatHead.getWidth();
        messageParams.y = chatHeadParams.y;

        windowManager.addView(message, messageParams);


        // Add the touch listener for the service
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private float savedX;
            private float savedY;
            private boolean menuOpen = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(!menuOpen) {
                            removeChatHead.setVisibility(View.VISIBLE);
                        }

                        removeChatHead.setVisibility(View.VISIBLE);
                        initialX = chatHeadParams.x;
                        initialY = chatHeadParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        // This is just a test
                        renderMessage("HELLO :)");

                        return true;
                    case MotionEvent.ACTION_UP:
                        int[] loc = new int[2];
                        removeChatHead.getLocationOnScreen(loc);

                        // If the chat head is close to the 'igloo' then remove the chat head
                        if(((chatHeadParams.y <= loc[1]) && (chatHeadParams.y >= loc[1] - chatHead.getHeight())) &&
                                ((chatHeadParams.x + chatHead.getWidth()/2 >= loc[0]) && (chatHeadParams.x + chatHead.getWidth()/2 <= removeChatHead.getWidth() + loc[0]))) {
                            chatHead.setVisibility(View.GONE);
                            stopSelf();
                        } else {    // Otherwise
                            // If the menu was open, restore it
                            if(menuOpen) {
                                chatHeadParams.x = 0;
                                chatHeadParams.y = 10;

                                menuView.setVisibility(View.VISIBLE);
                                windowManager.updateViewLayout(chatHead, chatHeadParams);
                            } else {    // Otherwise snap the chat head to the appropriate side of the screen
                                if(chatHeadParams.x <= size.x/2) {
                                    chatHeadParams.x = 0;
                                    chatHead.setImageResource(R.drawable.private_left);
                                } else {
                                    chatHeadParams.x = size.x;
                                    chatHead.setImageResource(R.drawable.private_right);
                                }

                                chatHeadParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                                windowManager.updateViewLayout(chatHead, chatHeadParams);
                            }
                        }
                        removeChatHead.setVisibility(View.GONE);

                        // If the user taps on the chat head then toggle the menu visibility
                        if(Math.abs(event.getRawX() - initialTouchX) <= 5 && Math.abs(event.getRawY() - initialTouchY) <= 5){
                            if(menuOpen){
                                // If the menu was open, close it
                                menuOpen = false;
                                menuView.setVisibility(View.GONE);

                                chatHeadParams.x = (int)savedX;
                                chatHeadParams.y = (int)savedY;
                                windowManager.updateViewLayout(chatHead, chatHeadParams);
                            } else {
                                // Otherwise open it

                                // Updates progress bar values to appropriate values (from 0 - 100 set by a global variable) - currently uses filler value
                                updateProgress((ProgressBar) menuView.findViewById(R.id.appetiteBar), (TextView) menuView.findViewById(R.id.appetiteProgressView), 35, 100 );
                                updateProgress((ProgressBar) menuView.findViewById(R.id.fitnessBar), (TextView) menuView.findViewById(R.id.fitnessProgressView), 75, 100 );
                                updateProgress((ProgressBar) menuView.findViewById(R.id.moodBar), (TextView) menuView.findViewById(R.id.moodProgressView), 95, 100 );
                                menuOpen = true;
                                menuView.setVisibility(View.VISIBLE);

                                savedX = initialX;
                                savedY = initialY;

                                chatHeadParams.x = 0;
                                chatHeadParams.y = 10;
                                chatHead.setImageResource(R.drawable.private_left);
                                windowManager.updateViewLayout(chatHead, chatHeadParams);
                            }
                        }

                        // This is just a test
                        renderMessage("GOOD BYE");
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // This is just a test
                        renderMessage("WHOAAAAAAAAAAAAAAAAAAAAAAA");

                        if(menuOpen) {
                            menuView.setVisibility(View.GONE);
                        }

                        chatHeadParams.x = initialX + (int) (event.getRawX() - (initialTouchX + chatHead.getWidth()/2));
                        chatHeadParams.y = initialY + (int) (event.getRawY() - (initialTouchY - chatHead.getHeight()/4));
                        windowManager.updateViewLayout(chatHead, chatHeadParams);
                        return true;
                }
                return false;
            }
        });
    }

    public void updateProgress(final ProgressBar progressBar, TextView textView, final int current, int max) {
        progressBar.setMax(max);
        progressBar.setProgress(current);
        textView.setText(current + "/" + max);
    }

    public void renderMessage(final String msg) {
        message.setText(msg);
        message.setMaxWidth((int)(size.x - chatHead.getX()));
        if(chatHeadParams.x <= size.x/2) {
            messageParams.gravity = Gravity.TOP | Gravity.LEFT;
        } else {
            messageParams.gravity = Gravity.TOP | Gravity.RIGHT;
        }
        message.setVisibility(View.VISIBLE);

        messageParams.x = chatHead.getWidth();
        messageParams.y = chatHeadParams.y;
        windowManager.updateViewLayout(message, messageParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
    }

}
