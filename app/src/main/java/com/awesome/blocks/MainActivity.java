package com.awesome.blocks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    private Context context;
    private Activity activityContext;
    private LinearLayout mainLayout;
    private GameView gameView;
    private BroadcastReceiver receiver;
    private boolean isOnline = false;
    private final Handler looping = new Handler();
    private Runnable runnable;
    private int loopingCycle = 400;
    private boolean rewarded = false;
    private AudioManager audioManager;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int screenWidth, screenHeight;
    private String cachePath;
    private String TAG = "AppTag: ";

    // ads
    private boolean bannerAdLoaded = false, adRewardedLoaded = false, adInterstitialLoaded = false;
    private boolean adsInitialized = false;
    private boolean adsVerbose = true;
    private ConsentInformation consentInformation;
    private AdView adMobBannerView;
    private int adViewHeight = 0, bannerViewHeightDp = 0;
    private InterstitialAd adMobInterstitial;
    private String adMobInterstitialId = "ca-app-pub-8261651469212664/7899032281";
    private RewardedAd adMobRewarded;
    private String adMobRewardedId = "ca-app-pub-3940256099942544/5224354917";
    private boolean readyToSetAds = false, legalTextAlreadyCalled = false;

    private ImageView adBannerAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        activityContext = MainActivity.this;
        cachePath = context.getExternalCacheDir().getAbsolutePath();
        decorView = this.getWindow().getDecorView();

        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setFocusable(true);
        mainLayout.setFocusableInTouchMode(true);
        adBannerAR = findViewById(R.id.adBannerAR);

        getScreenDimensions();

        RelativeLayout.LayoutParams adBannerARLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adViewHeight);
        try {
            adBannerAR.setLayoutParams(adBannerARLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        sharedPref = getSharedPreferences("com.awesome.blocks", Context.MODE_PRIVATE);
        isOnline = isNetworkAvailable();

        setConnectivityListener();

        gameView = new GameView(context);
        mainLayout.addView(gameView);
        loadGameTextures();

        /*
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            rankingCapable = false;
            System.out.println(TAG + e.getMessage());
        }

        try {
            inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        } catch (Exception e) {
            rankingCapable = false;
            System.out.println(TAG + "StrictMode.ThreadPolicy: " + e.getMessage());
        }
        */

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        runnable = new Runnable() {
            @Override
            public void run() {
                threadCycle();
                looping.postDelayed(this, loopingCycle);
            }
        };
        looping.postDelayed(runnable, loopingCycle);
    }

    private void threadCycle() {
        if (readyToSetAds) {
            setAdMobAds();
            readyToSetAds = false;
        }
        if (gameView != null) {
            if (gameView.getWaitingForReward()) {
                gameView.setWaitingForReward(false);
                showRewardedAd();
            }
            if (rewarded) {
                rewarded = false;
                gameView.reward();
            }
            if (gameView.getInterstitial()) {
                gameView.setInterstitial(false);
                showAdMobInterstitialAd();
            }
            if (gameView.getFullScreen()) {
                gameView.setFullScreen(false);
                if (decorView != null) {
                    Common.fullScreen(MainActivity.this, decorView);
                }
            }
        }
    }

    public void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int currentOrientation = context.getResources().getConfiguration().orientation;
        WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        Display display = null;
        try {
            display = wm.getDefaultDisplay();
        } catch (NullPointerException e) {
            System.out.println(TAG + "wm.getDefaultDisplay: " + e.getMessage());
        }

        if (display != null) {
            Point screenSize = new Point();
            display.getRealSize(screenSize);
            screenWidth = screenSize.x;
            screenHeight = screenSize.y;
        } else {
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }

        if (screenWidth > screenHeight) {
            int temp = screenWidth;
            screenWidth = screenHeight;
            screenHeight = temp;
        }

        System.out.println(TAG + "Screen " + screenWidth + " x " + screenHeight + " pixels");

        float screenHeightDp = screenHeight / displayMetrics.density;

        System.out.println(TAG + "Screen height in dp " + screenHeightDp);

        if (screenHeightDp <= 400) {
            bannerViewHeightDp = 32;
        } else if (screenHeightDp > 400 && screenHeightDp <= 720) {
            bannerViewHeightDp = 50;
        } else {
            bannerViewHeightDp = 90;
        }
        adViewHeight = Math.round(bannerViewHeightDp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        System.out.println(TAG + "Banner in dp " + bannerViewHeightDp);
        System.out.println(TAG + "Banner in px " + adViewHeight);

        if (AdSize.SMART_BANNER.getHeightInPixels(context) > adViewHeight) {
            adViewHeight = AdSize.SMART_BANNER.getHeightInPixels(context);
            System.out.println(TAG + "banner in px corrected " + adViewHeight);
        }
    }

    private void loadGameTextures() {
        Bitmap texture;
        int memDivisions = 10;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        texture = BitmapFactory.decodeResource(context.getResources(), R.drawable.texture, bitmapOptions);
        long imageSizeInBytes = (long) bitmapOptions.outWidth * bitmapOptions.outHeight * getBytesPerPixel(bitmapOptions.inPreferredConfig);

        // check if the image is too big
        int scaleToUse = 1;
        while ((imageSizeInBytes / scaleToUse / scaleToUse) * memDivisions > getFreeMemory()) {
            scaleToUse ++;
        }

        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = scaleToUse;

        texture = null;
        try {
            texture = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.texture, bitmapOptions);
        } catch (Exception e) {
            System.out.println(TAG + "texture: " + e.getMessage());
        }
        if (texture != null) {
            gameView.sendTexture(texture);
        }
    }

    // ads

    private void showLegalTextIfNeeded() {
        if (!legalTextAlreadyCalled) {
            legalTextAlreadyCalled = true;
            ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
            consentInformation = UserMessagingPlatform.getConsentInformation(context);
            consentInformation.requestConsentInfoUpdate(activityContext, params,
                    new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                        @Override
                        public void onConsentInfoUpdateSuccess() {
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activityContext,
                                    (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                        if (loadAndShowError != null) {
                                            // Consent gathering failed.
                                            System.out.println(TAG + loadAndShowError.getErrorCode() + ": " + loadAndShowError.getMessage());
                                        }
                                        System.out.println(TAG + "Consent has been gathered");
                                        readyToSetAds = true;
                                    });
                        }
                    },
                    new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                        @Override
                        public void onConsentInfoUpdateFailure(@NonNull FormError requestConsentError) {
                            // Consent gathering failed.
                            System.out.println(TAG + requestConsentError.getErrorCode() + ": " + requestConsentError.getMessage());
                        }
                    });
        }
    }

    private void initializeAds() {
        if (!adsInitialized) {
            // AdMob initialization
            try {
                if (adsVerbose) System.out.println(TAG + "Initializing ads...");
                MobileAds.initialize(context, new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                        if (adsVerbose) System.out.println(TAG + "AdMob initialized");
                        for (Map.Entry<String, AdapterStatus> entry : initializationStatus.getAdapterStatusMap().entrySet()) {
                            if (adsVerbose) System.out.println(TAG + entry.getKey() + ": " + entry.getValue().getInitializationState());
                        }
                    }
                });

            } catch (Exception e) {
                if (adsVerbose) System.out.println(TAG + "MobileAds.initialize: " + e.getMessage());
            } finally {
                adsInitialized = true;
            }
        }
    }

    private void setAdMobAds() {
        if (!bannerAdLoaded) {
            adMobBannerView = findViewById(R.id.adView);
            setAdMobBannerListener();
            loadAdMobBannerAd();
        }

        if (!adInterstitialLoaded) {
            loadAdMobInterstitialAd();
        }

        if (!adRewardedLoaded) {
            loadAdMobRewardedAd();
        }
    }

    private void setAdMobBannerListener() {
        adMobBannerView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerAdLoaded = true;
                if (adsVerbose) System.out.println(TAG + "AdMob banner loaded");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                int errorCode = adError.getCode();
                String errorMessage = adError.getMessage();
                bannerAdLoaded = false;
                if (errorCode == AdRequest.ERROR_CODE_NETWORK_ERROR) loadAdMobBannerAd();
                if (adsVerbose) System.out.println(TAG + "AdMob banner: " + errorMessage);
            }
        });
    }

    private void loadAdMobBannerAd() {
        if (adMobBannerView != null) {
            try {
                adMobBannerView.loadAd(new AdRequest.Builder().build());
            } catch (Exception e) {
                System.out.println(TAG + "loadAdMobBannerAd: " + e.getMessage());
            }
        }
    }

    private void loadAdMobInterstitialAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(context, adMobInterstitialId, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    adMobInterstitial = interstitialAd;
                    adInterstitialLoaded = true;
                    if (adsVerbose) System.out.println(TAG + "AdMob interstitial loaded");

                    adMobInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            if (adsVerbose) System.out.println(TAG + "AdMob interstitial close callback called by the ad listener");
                            loadAdMobInterstitialAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            int errorCode = adError.getCode();
                            String errorMessage = adError.getMessage();
                            if (adsVerbose) System.out.println(TAG + "AdMob interstitial: " + errorMessage);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            adMobInterstitial = null;
                            if (adsVerbose) System.out.println(TAG + "AdMob interstitial showed");
                        }
                    });
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                    adInterstitialLoaded = false;
                    adMobInterstitial = null;
                    int errorCode = adError.getCode();
                    String errorMessage = adError.getMessage();
                    if (adsVerbose) System.out.println(TAG + "AdMob interstitial: " + errorMessage);
                    if (errorCode == AdRequest.ERROR_CODE_NETWORK_ERROR) loadAdMobInterstitialAd();
                }
            });


        } catch (Exception e) {
            System.out.println(TAG + "loadAdMobInterstitialAd: " + e.getMessage());
        }
    }

    private void showAdMobInterstitialAd() {
        if (adMobInterstitial != null) {
            adMobInterstitial.show(activityContext);
        }
    }

    private void loadAdMobRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(context, adMobRewardedId, adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        System.out.println(TAG + "RewardedAd.load: " + loadAdError.getMessage());
                        adMobRewarded = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        adMobRewarded = rewardedAd;
                        adRewardedLoaded = true;
                        System.out.println(TAG + "AdMob rewarded loaded");
                        setAdRewardedListener();
                    }
                });
    }

    private void setAdRewardedListener() {
        adMobRewarded.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                System.out.println(TAG + "Rewarded was shown.");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                System.out.println(TAG + "Rewarded failed to show.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                System.out.println(TAG + "Rewarded was dismissed.");
                adMobRewarded = null;
                adRewardedLoaded = false;
                loadAdMobRewardedAd();
            }
        });
    }

    private void showRewardedAd() {
        if (adMobRewarded != null) {
            adMobRewarded.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    rewarded = true;
                    System.out.println(TAG + "Reward earned");
                }
            });
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.ad_not_ready, Toast.LENGTH_SHORT).show();
        }
    }

    // IO

    public boolean onTouchEvent(MotionEvent me) {
        if (gameView != null) {
            int index = me.getActionIndex();
            int xPos = (int) me.getX(index);
            int yPos = (int) me.getY(index);
            if (me.getActionMasked() == MotionEvent.ACTION_DOWN || me.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                gameView.sendTouchData("down", xPos, yPos - adViewHeight);
            } else if (me.getActionMasked() == MotionEvent.ACTION_MOVE) {
                gameView.sendTouchData("move", xPos, yPos - adViewHeight);
            } else if (me.getActionMasked() == MotionEvent.ACTION_UP || me.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                gameView.sendTouchData("up", xPos, yPos - adViewHeight);
            }
        }
        return true;
    }

    // environment

    private long getFreeMemory() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMem = runtime.totalMemory() - runtime.freeMemory();
        final long maxHeapSize = runtime.maxMemory();
        return maxHeapSize - usedMem;
    }

    static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    // online stuff

    public void setConnectivityListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context cxt, Intent intent) {
                isOnline = isNetworkAvailable();
                if (isOnline) {
                    showLegalTextIfNeeded();
                    initializeAds();
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    // overrides

    @Override
    protected void onResume() {
        super.onResume();
        if (decorView != null) Common.fullScreen(MainActivity.this, decorView);
        looping.postDelayed(runnable, loopingCycle);
    }

    @Override
    protected void onPause() {
        super.onPause();
        looping.removeCallbacks(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            System.out.println(TAG + "unregisterReceiver: " + e.getMessage());
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (gameView != null) {
                    gameView.sendBackPressed();
                }
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            }
        }

        return true;
    }
}
