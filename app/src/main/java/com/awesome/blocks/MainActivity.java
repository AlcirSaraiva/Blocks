package com.awesome.blocks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

public class MainActivity extends AppCompatActivity {
    private View decorView;
    private Context context;
    private Activity activityContext;
    private LinearLayout mainLayout;
    private GameView gameView;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isOnline = false;
    private final Handler looping = new Handler();
    private Runnable runnable;
    private int loopingCycle = 400;
    private boolean rewarded = false;
    private AudioManager audioManager;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int screenWidth, screenHeight;
    private String TAG = "AppTag: ";

    // ads
    private boolean bannerAdLoaded = false, adRewardedLoaded = false, adInterstitialLoaded = false;
    private boolean adsInitialized = false;
    private boolean adsVerbose = true;
    private ConsentInformation consentInformation;
    private ImageView adBannerAR, splashScreen;
    private int adViewHeight = 0;
    private FrameLayout adContainerView;
    private AdView adMobBannerView;
    private InterstitialAd adMobInterstitial;
    private RewardedAd adMobRewarded;
    private String adMobBannerId = "ca-app-pub-8261651469212664/2268281719"; // test id ca-app-pub-3940256099942544/6300978111
    private String adMobInterstitialId = "ca-app-pub-8261651469212664/7899032281"; // test id ca-app-pub-3940256099942544/1033173712
    private String adMobRewardedId = "ca-app-pub-8261651469212664/9737484293"; // test id ca-app-pub-3940256099942544/5224354917
    private boolean readyToSetAds = false, legalTextAlreadyCalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        activityContext = MainActivity.this;
        decorView = this.getWindow().getDecorView();

        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setFocusable(true);
        mainLayout.setFocusableInTouchMode(true);
        adBannerAR = findViewById(R.id.adBannerAR);
        adContainerView = findViewById(R.id.ad_view_container);
        splashScreen = findViewById(R.id.splashScreen);

        new Handler().postDelayed(() -> {
            if (splashScreen != null) {
                splashScreen.setVisibility(View.GONE);
            }
        }, 3000);

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
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            screenWidth = getWindowManager().getCurrentWindowMetrics().getBounds().width();
            screenHeight = getWindowManager().getCurrentWindowMetrics().getBounds().height();
        } else {
            Display display = getWindowManager().getDefaultDisplay();
            Point screenSize = new Point();
            display.getRealSize(screenSize);
            screenWidth = screenSize.x;
            screenHeight = screenSize.y;
        }

        if (screenWidth > screenHeight) {
            int temp = screenWidth;
            screenWidth = screenHeight;
            screenHeight = temp;
        }

        System.out.println(TAG + "Screen " + screenWidth + " x " + screenHeight + " pixels");

        // Adaptive banner
        int adWidth = (int) (screenWidth / displayMetrics.density);
        AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, adWidth);
        adViewHeight = adSize.getHeightInPixels(context);

        System.out.println(TAG + "Large Adaptive Banner height in px " + adViewHeight);
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
        if (legalTextAlreadyCalled) return;
        legalTextAlreadyCalled = true;

        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(context)
                .addTestDeviceHashedId("9543E8AA74711590DB5201C0EE961CB3")
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .build();

        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(debugSettings)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(context);
        consentInformation.requestConsentInfoUpdate(activityContext, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(activityContext,
                        loadAndShowError -> {
                            if (loadAndShowError != null) {
                                System.out.println(TAG + "Consent form error: " + loadAndShowError.getMessage());
                            }
                            if (consentInformation.canRequestAds()) {
                                initializeAds();
                            }
                        }),
                requestConsentError -> {
                    System.out.println(TAG + "Consent update error: " + requestConsentError.getMessage());
                    if (consentInformation.canRequestAds()) {
                        initializeAds();
                    }
                });
    }

    private void initializeAds() {
        if (!adsInitialized) {
            adsInitialized = true;
            if (adsVerbose) System.out.println(TAG + "Initializing ads...");
            MobileAds.initialize(context, initializationStatus -> {
                if (adsVerbose) System.out.println(TAG + "AdMob initialized");
                runOnUiThread(() -> readyToSetAds = true);
            });
        }
    }

    private void setAdMobAds() {
        if (!bannerAdLoaded) {
            adMobBannerView = new AdView(context);

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
                int adWidth = (int) (screenWidth / getResources().getDisplayMetrics().density);
                AdSize adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, adWidth);
                adMobBannerView.setAdUnitId(adMobBannerId);
                adMobBannerView.setAdSize(adSize);
                adContainerView.removeAllViews();
                adContainerView.addView(adMobBannerView);
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
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isOnline = true;
                runOnUiThread(() -> showLegalTextIfNeeded());
            }

            @Override
            public void onLost(@NonNull Network network) {
                isOnline = false;
            }
        };
        manager.registerDefaultNetworkCallback(networkCallback);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = manager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = manager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
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
            if (networkCallback != null) {
                ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                manager.unregisterNetworkCallback(networkCallback);
            }
        } catch (Exception e) {
            System.out.println(TAG + "unregisterNetworkCallback: " + e.getMessage());
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
