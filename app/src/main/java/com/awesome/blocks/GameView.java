package com.awesome.blocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class GameView extends View {
    // global
    private Context context;
    private String action = "";
    private int touchX = 0, touchY = 0, touchInitY = -1;
    private Bitmap texture = null;
    private Rect rectOrigin, rectDestin;
    private Canvas c = null;
    private int canvasWidth = 0, canvasHeight = 0;
    private String gameState = "start";
    private String info = "";
    private int fontSize, fontCharSpace, fontLineSpace;
    private int currentLevel, unlockedLevel;
    public SharedPreferences sharedPref;
    public SharedPreferences.Editor editor;
    private float[] hsv = new float[3];
    private SoundPool sound;
    private int soundPop, soundToc, soundPlim, soundDown, soundDanger, soundStar, soundLost, soundWon, soundBomb, soundFF, soundShock;
    private long lastTime, soundTimeGap = 50;
    private boolean waitingForReward = false, rewarded = false, interstitial = false;
    private int retryTimes, winTimes, timesUntilInterstitial = 3;
    private int backIconSize, leftIconX, rightIconX;
    private Typeface typeface;
    private long touchGrabTime, touchGrabGap = 300;
    private boolean touchGrab = false, fullScreen = false, adPrefDialog = false;

    private String event = "";
    private int eventAnimIncr, eventAnimCurrentDist, eventAnimTotalDist, eventOriginalIncr, eventX, eventChar;

    // menu
    private int menuBGColor, menuScreenBlockStrokeWidth, menuScreenBlockSize;
    private int menuPlayIconSize, menuPlayIconX, menuPlayIconY, menuRankingIconX, menuRankingIconY;
    private int menuTitleX, menuTitleY, menuTitleWidth, menuTitleHeight;
    private Paint menuScreenBlockPaint, menuScreenAnimBlockPaint, menuScreenAnimBlockPaint2;
    private int jj, jjDiv;
    // levels
    private int levelsButtonSize, levelsButtonX;
    private Paint levelsButtonFillPaint, levelsButtonStrokePaint, levelsButtonTextPaint;
    private int realNumberOfLevels, numberOfLevels;
    private int levelsScreenPositionY;
    // game
    private int i, j;
    private int horizontalNumberOfBlocks = 15, enemyTotalGridHeight = 70, gameVisibleRows, lastInvisibleRow;
    private int blockSize, shotSize, halfShotSize, sliderXMinLimit, sliderXMaxLimit;
    private int highestPossibleNumber;
    private double shotX, shotY, lastShotX, lastShotY, firstShotOnGroundX;
    private double distanceAfterShot, distanceTrail, distanceTrailStatic, initialShotDistance;
    private double sliderX, sliderY;
    private boolean sliderGrabbed = false;
    private boolean touchFree = false;
    private int gameAreaTopY, gameAreaBottomY, sliderSize, sliderBGWidth, sliderBGHeight, sliderBGY;
    private int gameAreaBottomSize, starSize, starBGBlockWidth, starBGTotalWidth, starX, starY, starLevel, starIncr;
    private long lastStarGain, starGainTimeGap = 200;
    private boolean firstStarAchieved, secondStarAchieved, thirdStarAchieved;
    private Paint enemyFillPaint, enemyStrokePaint;
    private int gameBGColor;
    private float tempDistance = 1;
    private Paint infoPaint, gameBGPaint, blockNumberPaint, panelPaint;
    private String slider;
    private int enemyBetweenSpaces;
    private int[][] enemyHiddenGrid;
    private final int GRID_IMMOVABLE_BLOCK = -2, GRID_MOVABLE_BLOCK = -1, GRID_EMPTY = 0;
    private final int GRID_HORIZ_LASER = -5, GRID_VERT_LASER = -6, GRID_MULTI_LASER = -7, GRID_RANDOM_ANGLE = -8;
    private final int GRID_FADE1 = -9, GRID_FADE2 = -10, GRID_FADE3 = -11, GRID_FADE4 = -12, GRID_FADE5 = -13, GRID_FADE6 = -14, GRID_FADE7 = -15, GRID_FADE8 = -16;
    private int gameAreaHeight;
    private double shotOnGridX, shotOnGridY;
    private boolean alive, firstTouchedGround;
    private int numberOfShots, aliveShots, arrayTurn;
    private double[] shotArrayX, shotArrayY, angle;
    private boolean[] shotActive;
    private int currentComboRow, maxComboRows;
    private boolean comboRowsAnim;
    private long comboRowsTimeGap = 200;
    private boolean danger, dangerAlreadyTriggered;
    public boolean deadDialog, pauseDialog;
    public int threeButtonDialogX, threeButtonDialogY, threeButtonDialogWidth, threeButtonDialogHeight;
    public int dialogButtonX, dialogButtonY, dialogButtonWidth, dialogButtonHeight, dialogButtonGap, shadowGap;
    private Paint dialogButtonFillPaint, dialogButtonStrokePaint, dialogButtonTextPaint, shadowPaint;
    private int numberOfBombs;
    private boolean levelBombReceived;
    private Paint bombTextPaint;
    private int panelColor, bombDamage;
    private boolean usingBomb;
    private boolean won, speedUpIcon;
    private int totalPoints, levelPoints, pointsStarStepIncr, pointsWholeStarIncr, pointsSingle, pointsThirdStarIncr;
    private Paint winPointsTextPaint, levelPointsPaint, totalPointsPaint;
    private boolean horizHit, vertHit, multiHit;
    private int horizHitXX, horizHitY, vertHitX, vertHitYY, multiHitX, multiHitY, multiHitXX, multiHitYY;
    private int charWidthA, charWidthB, charWidthC, charHeightA;
    private long speedingCycle, speedingCycleStart;
    private int starAnimCounter, starAnimIncr, starAnimStep = 3, starAnimSize;
    private boolean drawsHand = false;
    private int handX, handY, handFactor, handIncr;
    private boolean firstBomb;
    private long timeOfBomb;
    private Paint levelTextPaint;
    private int levelTextY;

    // initialization

    public GameView(Context ctx) {
        super(ctx);
        context = ctx;
        initializeView();
    }

    public GameView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        context = ctx;
        initializeView();
    }

    public GameView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        context = ctx;
        initializeView();
    }

    protected void onDraw(Canvas canvas) {
        c = canvas;
        switch (gameState) {
            case "start":
                if (canvasWidth != 0 && canvasHeight != 0) {
                    initVariables();
                    initSound();
                    gameState = "menu";
                }
                break;
            case "menu":
                if (texture != null) {
                    menuScreen();
                    handlesMenuTouch();
                } else {
                    info = context.getString(R.string.texture_loading_failed);
                }
                break;
            case "levels":
                levelsScreen();
                handlesLevelsTouch();
                break;
            case "game":
                drawsGameBackground();
                drawsGrid();
                if (event.length() > 0) {
                    animEvent();
                }
                drawsGameTopAndBottomPanel();
                drawsStarsBar();
                drawsLeftIcon();
                drawsRightIcon();
                drawsPauseIcon();
                if (!won) {
                    drawsPoints();
                } else {
                    drawsWonDialog();
                }
                handlesGameTouch();
                if (comboRowsAnim) {
                    if (System.currentTimeMillis() - lastTime >= comboRowsTimeGap) {
                        movesGridDown();
                        currentComboRow ++;
                        lastTime = System.currentTimeMillis();
                    }
                    if (currentComboRow == maxComboRows) {
                        touchFree = true;
                        comboRowsAnim = false;
                    }
                } else if (pauseDialog) {
                    drawsPauseDialog();
                } else if (touchFree) {
                    drawsSlider();
                    drawsShotTrail();
                    if (drawsHand) {
                        drawsHandOverSlider();
                    }
                    if (firstBomb && numberOfBombs == 1) {
                        drawsBombHand();
                    }
                    if (usingBomb) {
                        handlesBomb();
                    }
                } else {
                    if (alive) { // balls moving
                        // speed icon
                        if (System.currentTimeMillis() - speedingCycleStart > speedingCycle) {
                            speedingCycleStart = System.currentTimeMillis();
                            distanceAfterShot *= 1.25f;
                            if (sound != null) {
                                sound.play(soundFF, 1, 1, 2, 0, 1);
                            }
                            speedUpIcon = true;
                            Handler handlerSpeed = new Handler();
                            handlerSpeed.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    speedUpIcon = false;
                                }
                            }, 1500);
                        }

                        drawsSprite("stopIcon", (canvasWidth / 2) - (gameAreaBottomSize / 2), gameAreaBottomY);
                        if (aliveShots > 0) {
                            for (int ns = 0; ns < arrayTurn; ns ++) { // goes through all the shots
                                if (isShotAlive(ns)) {
                                    //c.drawCircle((int) shotArrayX[ns] + halfShotSize, (int) shotArrayY[ns] + halfShotSize, shotSize / 2, panelPaint);
                                    drawsSprite("shot", (int) shotArrayX[ns], (int) shotArrayY[ns]);
                                }
                            }
                            if (horizHit) drawsHorizHit();
                            if (vertHit) drawsVertHit();
                            if (multiHit) drawsMultiHit();

                            if (firstTouchedGround) {
                                drawsSprite("previewShot", (int) firstShotOnGroundX, gameAreaBottomY - shotSize);
                            }
                            if (arrayTurn < numberOfShots) {
                                arrayTurn ++;
                            }
                        } else {
                            allShotsHitGround();
                            if (hasWon()) {
                                wins();
                            } else if (hasEnoughFreeSpace()) {
                                currentComboRow = 0;
                                event = "comboRow";
                                eventChar = (int)Math.floor(Math.random() * 6);
                                comboRowsAnim = true;
                                lastTime = System.currentTimeMillis();
                            } else {
                                movesGridDown();
                                if (isPlayerAlive()) {
                                    touchFree = true;
                                } else {
                                    dies();
                                }
                            }
                        }
                    } else if (deadDialog) {
                        drawsDeadDialog();
                        if (rewarded) {
                            rewarded = false;

                            for (j = enemyTotalGridHeight - 1; j > enemyTotalGridHeight - 4; j --) {
                                for (i = horizontalNumberOfBlocks - 1; i >= 0; i --) {
                                    enemyHiddenGrid[i][j] = GRID_EMPTY;
                                }
                            }

                            shotX = (canvasWidth / 2) - halfShotSize;
                            lastShotX = shotX;
                            danger = false;
                            dangerAlreadyTriggered = false;

                            for (i = 0; i < numberOfShots; i ++) {
                                shotArrayX[i] = shotX;
                                shotArrayY[i] = shotY;
                                shotActive[i] = true;
                                angle[i] = 270;
                            }

                            event = "rewarded";
                            eventChar = (int)Math.floor(Math.random() * 11);

                            deadDialog = false;
                            touchFree = true;
                            alive = true;

                            if (hasWon()) {
                                wins();
                            }
                        }
                    }
                }
                break;
        }
        writesText(info, 20, 50, infoPaint);
        this.invalidate();
    }

    private void initializeView() {
        rectOrigin = new Rect();
        rectDestin = new Rect();
        infoPaint = new Paint();
        levelsButtonFillPaint = new Paint();
        levelsButtonStrokePaint = new Paint();
        levelsButtonTextPaint = new Paint();
        gameBGPaint = new Paint();
        blockNumberPaint = new Paint();
        enemyFillPaint = new Paint();
        enemyStrokePaint = new Paint();
        dialogButtonFillPaint = new Paint();
        dialogButtonStrokePaint = new Paint();
        dialogButtonTextPaint = new Paint();
        shadowPaint = new Paint();
        bombTextPaint = new Paint();
        winPointsTextPaint = new Paint();
        levelPointsPaint = new Paint();
        totalPointsPaint = new Paint();
        menuScreenBlockPaint = new Paint();
        menuScreenAnimBlockPaint = new Paint();
        menuScreenAnimBlockPaint2 = new Paint();
        panelPaint = new Paint();
        typeface =Typeface.createFromAsset(context.getAssets(),"fonts/blocko.ttf");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void initVariables() {
        // menu
        menuBGColor = 0xfffef0c3;
        menuScreenBlockStrokeWidth = (int)(canvasWidth * 0.006);
        menuScreenBlockSize = canvasWidth / 24;
        menuScreenBlockPaint.setStyle(Paint.Style.STROKE);
        menuScreenBlockPaint.setStrokeWidth(menuScreenBlockStrokeWidth);
        menuScreenBlockPaint.setColor(0x40ffffff);
        menuTitleWidth = canvasWidth;
        menuTitleHeight = menuScreenBlockSize * 8;
        menuTitleX = 0;
        menuTitleY = (menuScreenBlockSize * 9) - (menuScreenBlockStrokeWidth / 2);
        menuPlayIconSize = menuScreenBlockSize * 6;
        menuPlayIconX = (menuScreenBlockSize * 4) - (menuScreenBlockStrokeWidth / 2);
        menuPlayIconY = (menuScreenBlockSize * 24) - (menuScreenBlockStrokeWidth / 2);
        menuRankingIconX = (menuScreenBlockSize * 14) - (menuScreenBlockStrokeWidth / 2);
        menuRankingIconY = (menuScreenBlockSize * 24) - (menuScreenBlockStrokeWidth / 2);
        backIconSize = menuScreenBlockSize * 3;
        leftIconX = menuScreenBlockSize;
        rightIconX = canvasWidth - menuScreenBlockSize - backIconSize;
        menuScreenAnimBlockPaint.setStyle(Paint.Style.FILL);
        menuScreenAnimBlockPaint2.setStyle(Paint.Style.FILL);
        jj = 250;
        jjDiv = 300;
        // levels
        realNumberOfLevels = 80;
        numberOfLevels = realNumberOfLevels + 1;
        levelsButtonSize = (int)(canvasWidth * 0.22f);
        levelsButtonFillPaint.setStyle(Paint.Style.FILL);
        levelsButtonStrokePaint.setStyle(Paint.Style.STROKE);
        levelsButtonStrokePaint.setStrokeWidth((int)(canvasWidth * 0.005));
        levelsButtonTextPaint.setFakeBoldText(true);
        levelsButtonTextPaint.setTextSize((int)(canvasWidth * 0.17f));
        levelsButtonTextPaint.setTypeface(typeface);
        // game
        winTimes = 0;
        fontSize = (int)(canvasWidth * 0.026f);
        blockSize = canvasWidth / horizontalNumberOfBlocks;
        shotSize = blockSize / 5;
        halfShotSize = shotSize / 2;
        gameAreaTopY = (int)(canvasHeight * 0.1f);
        gameAreaBottomSize = (int)(canvasWidth * 0.206f);
        gameVisibleRows = (canvasHeight - gameAreaTopY - gameAreaBottomSize) / blockSize;
        lastInvisibleRow = enemyTotalGridHeight - gameVisibleRows;
        maxComboRows = gameVisibleRows / 3;
        gameAreaBottomSize = canvasHeight - gameAreaTopY - (gameVisibleRows * blockSize);
        gameAreaBottomY = canvasHeight - gameAreaBottomSize;
        gameAreaHeight = gameAreaBottomY - gameAreaTopY;
        distanceTrailStatic = blockSize / 3 * 2;
        distanceTrail = distanceTrailStatic;
        initialShotDistance = blockSize / 2;//2;
        distanceAfterShot = initialShotDistance;
        sliderSize = (int)(canvasWidth * 0.18f);
        sliderBGWidth = (int)(canvasWidth * 0.45f);
        sliderBGHeight = (int)(canvasWidth * 0.018f);
        sliderXMinLimit = (canvasWidth - sliderBGWidth) / 2;
        sliderXMaxLimit = sliderXMinLimit + sliderBGWidth;
        touchFree = true;
        sliderX = (canvasWidth / 2) - (sliderSize / 2);
        sliderY = gameAreaBottomY + ((canvasHeight - gameAreaBottomY - sliderSize) / 2);
        sliderBGY = (int) (sliderY + (sliderSize / 2) - (sliderBGHeight / 2));
        slider = "sliderUnpressed";
        infoPaint.setColor(Color.BLUE);
        infoPaint.setFakeBoldText(true);
        infoPaint.setTextSize(fontSize);
        blockNumberPaint.setColor(Color.BLACK);
        blockNumberPaint.setFakeBoldText(true);
        blockNumberPaint.setTextSize(fontSize);
        fontCharSpace = (int) (fontSize * 0.63f);
        fontLineSpace = (int) (fontSize * 1.42f);
        panelColor = 0xffc8c9d5;
        gameBGPaint.setStyle(Paint.Style.FILL);
        gameBGPaint.setColor(gameBGColor);
        panelPaint.setStyle(Paint.Style.FILL);
        panelPaint.setColor(panelColor);
        enemyFillPaint.setStyle(Paint.Style.FILL);
        gameBGColor = 0xff242329;
        enemyBetweenSpaces = (int)(canvasWidth * 0.005f);
        shotX = (canvasWidth / 2) - halfShotSize;
        shotY = gameAreaBottomY - shotSize;
        starSize = (int)(canvasWidth * 0.08f);
        starAnimSize = starSize / 4;
        starBGBlockWidth = (int)(canvasWidth * 0.023f);
        starBGTotalWidth = canvasWidth - (gameAreaTopY * 3);
        starX = (canvasWidth - starBGTotalWidth) / 2;
        starY = gameAreaTopY / 2;
        starLevel = 0;
        starIncr = starBGBlockWidth / 4;
        numberOfShots = 100;
        aliveShots = numberOfShots;
        dialogButtonFillPaint.setStyle(Paint.Style.FILL);
        dialogButtonFillPaint.setColor(0xffffec7d);
        dialogButtonStrokePaint.setStyle(Paint.Style.STROKE);
        dialogButtonStrokePaint.setStrokeWidth((int)(canvasWidth * 0.006));
        dialogButtonStrokePaint.setColor(0xff0e1a81);
        dialogButtonTextPaint.setFakeBoldText(true);
        dialogButtonTextPaint.setTextSize((int)(canvasWidth * 0.05f));
        dialogButtonTextPaint.setColor(0xff0e1a81);
        dialogButtonTextPaint.setTypeface(typeface);
        threeButtonDialogWidth = menuScreenBlockSize * 16;
        threeButtonDialogHeight = menuScreenBlockSize * 20;
        threeButtonDialogX = (canvasWidth - threeButtonDialogWidth) / 2;
        threeButtonDialogY = (canvasHeight - threeButtonDialogHeight) / 2;
        dialogButtonWidth = (int)(canvasWidth * 0.455f);
        dialogButtonHeight = (int)(canvasHeight * 0.077f);
        dialogButtonX = (int)(canvasWidth * 0.2725f);
        dialogButtonY = (int)(canvasHeight * 0.323f);
        dialogButtonGap = (int)(canvasHeight * 0.13f);
        shadowGap = (int)(canvasWidth * 0.016f);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(0x33000000);
        retryTimes = 0;
        unlockedLevel = sharedPref.getInt("unlockedLevel", 1);
        numberOfBombs = sharedPref.getInt("numberOfBombs", 0);
        levelBombReceived = sharedPref.getBoolean("levelBombReceived", false);
        bombTextPaint.setFakeBoldText(true);
        bombTextPaint.setTextSize((int)(canvasWidth * 0.035f));
        bombTextPaint.setColor(panelColor);
        bombTextPaint.setTypeface(typeface);
        bombDamage = 25;
        totalPoints = sharedPref.getInt("totalPoints", 0);
        pointsSingle = 1;
        pointsStarStepIncr = 5;
        pointsWholeStarIncr = 50;
        pointsThirdStarIncr = 200;
        winPointsTextPaint.setFakeBoldText(true);
        winPointsTextPaint.setTextSize((int)(canvasWidth * 0.06f));
        winPointsTextPaint.setColor(0xff0e1a81);
        winPointsTextPaint.setTypeface(typeface);
        levelPointsPaint.setColor(0xff666eb9);
        levelPointsPaint.setFakeBoldText(true);
        levelPointsPaint.setTextSize((int)(canvasWidth * 0.05f));
        levelPointsPaint.setTypeface(typeface);
        totalPointsPaint.setColor(0xff8d7900);
        totalPointsPaint.setFakeBoldText(true);
        totalPointsPaint.setTextSize((int)(canvasWidth * 0.05f));
        totalPointsPaint.setTypeface(typeface);
        speedingCycle = 10000;
        charWidthA = (blockSize / 2) - (int)(fontSize  * 0.35f);
        charWidthB = (blockSize / 2) - (int)(fontSize  * 0.6f);
        charWidthC = (blockSize / 2) - (int)(fontSize  * 0.92f);
        charHeightA = (blockSize / 2) + (int)(fontSize * 0.4f);
        eventAnimCurrentDist = 0;
        eventAnimTotalDist = gameAreaBottomSize;
        eventOriginalIncr = eventAnimTotalDist / 20;
        eventAnimIncr = eventOriginalIncr;
        eventX = -1;
        handFactor = blockSize / 10;
        firstBomb = sharedPref.getBoolean("firstBomb", true);
        handY = gameAreaBottomY - blockSize;

        levelTextPaint = new Paint();
        levelTextPaint.setColor(0xff2f2e36);
        levelTextPaint.setTextSize(canvasWidth * 0.3f);
        levelTextPaint.setTypeface(typeface);
        levelTextY = gameAreaBottomY;

        //editor = sharedPref.edit();
        //editor.putInt("numberOfBombs", 18); // 18
        //editor.putInt("unlockedLevel", 19); // 19
        //editor.putInt("totalPoints", 20028); // 20028
        //editor.apply();
    }

    public void initSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            sound = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(2)
                    .build();
        } else {
            sound = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }

        soundPop = sound.load(context, R.raw.pop, 1);
        soundToc = sound.load(context, R.raw.toc, 1);
        soundDown = sound.load(context, R.raw.row_down, 1);
        soundPlim = sound.load(context, R.raw.plim, 1);
        soundDanger = sound.load(context, R.raw.danger, 1);
        soundStar = sound.load(context, R.raw.star, 1);
        soundLost = sound.load(context, R.raw.lost, 1);
        soundWon = sound.load(context, R.raw.won, 1);
        soundBomb = sound.load(context, R.raw.bomb, 1);
        soundFF = sound.load(context, R.raw.ff, 1);
        soundShock = sound.load(context, R.raw.shock, 1);
    }

    // game

    public void initLevel(int level) {
        String levelContent;

        switch (level) {
            case 1:
                levelContent = "###################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################$$$$$$$$$$$$$$$##-##-###-##-###''''''#''''''#&&&&&&&&&&&&&&&###############$$$$$$$$$$$$$$$###############)))))))))))))))%%%%%%%%%%%%%%%################%%%%%%%%%%%%%%#))))))))))))))###############%%%%%%%%%%%%%%#))))))))))))))#######################################################################################################################################################";
                break;
            case 2:
                levelContent = "########################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################+#+#+#+#+#+#+##'#'#'#'#'#'#'########$#######'''''''''''''''+++++++++++++++#######$#######+++++++)+++++++'''''''a'''''''$+#+$+#)#+$+#+$$'#'$'#'#'$'#'$$+#+$+#)#+$+#+$$'#'$'#'#'$'#'$$+#+$+#)#+$+#+$$'#'$'#'#'$'#'$$+#+$+#)#+$+#+$$'#'$'#'#'$'#'$$+#+$+#)#+$+#+$$'#'$'#'#'$'#'$$+#+$+#)#+$+#+$######################################################################################################################################################";
                break;
            case 3:
                levelContent = "#######################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################,#,,,,,,,,,,,#,###(((((((((###%#%%%%%%%%%%%#%$#$$$$$$$$$$$#$###############%%%%%%%%%%%%%%%+++#+++++++#+++'''''''#'''''''###############$$$$$$$#$$$$$$$%%%%%%%#%%%%%%%#)))))))))))))#,#,,,,,$,,,,,#,((#((#(((#((#((%%%#%%%%%%%#%%%#$$$#$$$$$#$$$#(((((#(((#(((((%a#%%%#%#%%%#a%,,,$,#,#,#,$,,,######################################################################################################################################################";
                break;
            case 4:
                levelContent = "##############################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################,#-,,,,,,,,,-#,(#-(((((((((-#(%#-%%%%%%%%%-#%$#-$$$$$$$$$-#$##-#########-##$$-$$$$#$$$$-$$###############,##,,,,,,,,,##,(#-(((((((((-#(%#-%%%%%%%%%-#%$##$$$$$$$$$##$##-#a#####a#-##%%-%%%%%%%%%-%%++#+++++++++#++''-''''#''''-''%%-%%%%%%%%%-%%$$#$$$$#$$$$#$$%%-%%%%#%%%%-%%$)-)))))))))-)$,$#,,,,,,,,,#$,((-((((#((((-((%%-$.%%#%%%$-%%$$#$$$$#$$$$#$$((-(($(#($((-((%%-%%%$#$%%%-%%,,#%,,,$,,,%#,,######################################################################################################################################################";
                break;
            case 5:
                levelContent = "########################################################################################################################################################################################################################################################################################################################################################################++#++++#++++#++++#+$$+#+$$+#++####'#)#)#'#####%###)))))###%#'#)))))))))))#'##)))))))))))###%))/######))%#'#))#######))#'##))##-#-##))###%))#3#/#3#))%#'#))##3#3##))#'##))#######))###%))/#####/))%#'#)))))))))))#'#%)))))))))))%####a###2###a#######a#####a#####+#+#+#+#+#+#+#$'$'$'$'$'$'$'$$#$#$#$#$#$#$#$'''''''''''''''++#++++#++++#++###############+++++++++++++++'''''''''''''''#+#+#+#+#+#+#+#'''''''''''''''+#+++#+++#+++#+'''''''''''''''%+%##+%+%+##%+%'''''''''''''''%+%+%+%+%+%+%+%'''''''''''''''%###%+%+%+%###%'''''''#'''''''/$/#)//%//)#/$/######################################################################################################################################################";
                break;
            case 6:
                levelContent = "########################################################################################################################################################################################################################################################################################################################################################################################+#+#+#/#+#+#+##+#+#+#/#+#+#+##+#)#+#/#+#)#+##+$+#$#/#$#+$+##'#+#+#/#+#+#'##+#)#+#/#+#)#+##+$+$+$/$+$+-+####%#'###'#%###/)))))))))))))/+'#a#######a#'+#')))))))))))'#+')))))))))))'+################+#+#+#/#+#+#+##+#+#+#/#+#+#+##+#)#+#/#+#)#+##+$+#$#/#$#+$+##'#+#+#/#+#+#'##+#)#+#/#+#)#+##+$+$+$/$+$+-+#/)))))))))))))/+'#a#######a#'+#')))))))))))'#+')))))))))))'+/#))#######))#/+'))###,###))'+#'#)#######)#'#+'))#1#f#1#))'+/#))#######))#/+'))#a#,#a#))'+#'#)#######)#'#+')))))))))))'+/')))))))))))'/+'###$###$###'+$-------------$######################################################################################################################################################";
                break;
            case 7:
                levelContent = "#########################################################################################################################################################################################################################################################################################################################################################$-%-%-%-%-%-%-$$))))))'))))))$$#############$$#+++++++++++#$$#+++++++++++#$$#############$$%///////////%$#####a##a######$-%-%-%-%-%-%-$$))))))'))))))$$a###########a$$#+++++++++++#$$#+++++++++++#$$###a#####a###$$%///////////%$###############$-%-%-%-%-%-%-$$))))))'))))))$$######a######$$#+++++++++++#$$#+++++++++++#$$#############$$%///////////%$$%a#########a%$$%#)))))))))#%$$%###########%$$%#---#1#---#%$$%#---#1#---#%$$%###########%$$%#)))))))))#%$$%###########%$$%///////////%$$#############$$#+++++++++++#$$#+++++++++++#$$#############$#)))))))))))))#######################################################################################################################################################";
                break;
            case 8:
                levelContent = "#######################################################################################################################################################################################################################################---#########---------#####-)))))))))-####')))))))))'###-)))))))))))-##-)))))))))))-##-))###)###))-##-))###)###))-##')####)####)'##-)#a##)##a#)-##-)####)####)-##-)###)))###)-##')###)))###)'##-)-##)))##-)-##-))--)#)--))-##-)))))#)))))-###')))#)#)))'####-)))))))))-#####,,,,,,,,,######,,,,,,,,,######+%+%+%+%+######+#+#+#+#+########################---#########---------#####-)))))))))-####')))))))))'###-)))))))))))-##-)))))))))))-##-))###)###))-##-))###)###))-##')####)####)'##-)#7##)##7#)-##-)####)####)-##-)###)))###)-##')###)))###)'##-)-##)))##-)-##-))--)#)--))-##-)))))#)))))-###')))#)#)))'####-)))))))))-#####,,,,,,,,,######,,,,,,,,,######+%+%+%+%+######+#+#+#+#+#########################################################################################################################################################";
                break;
            case 9:
                levelContent = "####################################################################################################################################################################################################################################################################################################)############)))))#########)))))))#######)))))))))#####))+)))))+))####)))))-)))))###))))--#--))))##))))-###-))))##)))-#####-)))##)))-##=##-)))##))#-##2##-#))##)*)-#####-)*)##))))-###-))))##))))--#--))))###)##))-))##)####)))))))))))###+#)))))))))#+###a+)))))))+a####+##)))))##+###+##+##)##+##+####+#######+########a#)#a#######+##)))))##+####*#)))))))#*###+#)))))))))#+###)))))))))))####)))))-)))))###))))--#--))))##))))-###-))))##)))-#####-)))##)))-##7##-)))##)))-##2##-)))##)))-#####-)))##))))-a#a-))))##))))--#--))))###)))))-)))))####)))))))))))#####)))))))))#######)))))))#########)))))######a#####)#####a#######################################################################################################################################################";
                break;
            case 10:
                levelContent = "##################a#5+.+5#a#####a##++.++##a###3#a##5#5##a#3#33####5#5####33#31###5#5###13####1##-#-##1#######1#####1########1#####1######a##1#a#1##a###a###1###1###a#######1#1############1#1#########11##0##11#########'#'########+55+...+55+######5+...+5######+55+...+55+#######)###)#####--+55+...+55+----5555+.+5555----5555+c+5555--#--55++.++55--##,-+#++.++#+-,##,-+#++.++#+-,##,#+#++.++#+#,##,-+6++.++6+-,###+-+++#+++-+####.-++-#-++-.#######5+.+5########a#5+.+5#a#####a##++.++##a###3#a##5#5##a#3#33####5#5####33#31###5#5###13####1##-#-##1#######1#####1########1#####1######a##1#a#1##a###a###1###1###a#######1#1############1#1#########11##0##11#####1--/#0#/--1###,-#+/#'#/+#-,##,-+#+,.,+#+-,##,-+++,.,+++-,#--##5+.'.+5##----#155+.+551#----5555+.+5555--#--1#++'++#1--##,-#++#d#++#-,##,-++++K++++-,##,-+7+#K#+7+-,##,-+6#+.+#6+-,##'+#+++'+++#+'###.-#+-.-+#-.####,-%-,.,-%-,#####+++###+++#######-#####-##########################################################################################################################################################";
                break;
            case 11:
                levelContent = "##################################################################################################################################################################################################################################################################################################################################################################################################################################################################%#000#222#000#%###.###4###.####,,,,//A//,,,,#%#000#2#2#000#%###.#,#K#,#.####,,,,//2//,,,,#%#000#222#000#%###.##A4A##.####,,,,//2//,,,,#%#000#222#000#%###.###4###.####,,,,//2//,,,,#%#000#222#000#%###.###4###.####,,,,//2//,,,,#%#000#222#000#%###.###4###.####,,,,//A//,,,,#%#000#2#2#000#%###.#,#K#,#.####,,,,//2//,,,,#%#000#222#000#%###.##A4A##.####,,,,//2//,,,,#%#000#222#000#%###.###4###.####,,,,//2//,,,,#%#000#222#000#%###.###4###.####,,,,//2//,,,,#######################################################################################################################################################";
                break;
            case 12:
                levelContent = "###########################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################K#############K#$#)#*****#)#$#1#+)$#'''#$)+#1#$#)#+$K$+#)#$#'#')#$'3'$#)'#'#$#)$#+K+#$)#$#1#3)##777##)3#1#$#)#$#$#$#)#$#7#+)/##$##/)+#7#$#)$#)))#$)#$#1##)#$---$#)##1#))))))#))))))##$#)#*****#)#$#1#+)$#'''#$)+#1#$#)#+$#$+#)#$#'#')#$'3'$#)'#'#$#)$#+K+#$)#$#1#3)##777##)3#1#$#)#$#$#$#)#$#7#+)/##$##/)+#7#$#)$#)))#$)#$#1##)#$---$#)##1#))))))#))))))#######################################################################################################################################################";
                break;
            case 13:
                levelContent = "####################################################################################################################################################################################################################################################################################################################################################################################################################################4444#44444#4444#222#22#22#222#0#00#00#00#00#0..#....#....#.....#.......#...0000#00000#0000#2222#222#2222##44444#4#44444#6666666#666666644####444####442222#22222#2222#00#0#####0#00#.......#.......$##%#######%##$.......#.......0000000#0000000222/222#222/2224444#######4444/6/6#66666#6/6/4444#44444#4444#222#22#22#222#0#00#00#00#00#0..#....#....#.....#.......#...000##00000##000#22#2#222#2#22##44#44#4#44#44#666#666#666#66644####444####44222)#22222#)222#00#0#####0#00#.......#.......######################################################################################################################################################";
                break;
            case 14:
                levelContent = "#################################################,#,$,#,####,#,##,$$$,##,#,#,,#,$$2$$,#,,####,,$$2$$,,#####,$$,$$$,$$,###,$$$$,$,$$$$,##4$$$$#,#$$$$4##4#,,##-##,,#4##4,//,#-#,//,4##4,//,#-#,//,4##4#,,##-##,,#4#22#####-#####222233333e333332222''''777''''22//////777//////$$$$$$777$$$$$$222222222222222#%#%#%#%#%#%#%#-#-#-#-#-#-#-#-####,#,$,#,####,#,##,$$$,##,#,#,,#,$$2$$,#,,####,,$$2$$,,#####,$$,$$$,$$,###,$$$$,$,$$$$,##4$$$$#,#$$$$4##4#,,##-##,,#4##4,//,#-#,//,4##4,//,#-#,//,4##4#,,##-##,,#4#22#####-#####222233333d333332222''''777''''22//////777//////$$$$$$777$$$$$$222222222222222#%#%#%#%#%#%#%#-#-#-#-#-#-#-#-####,#,$,#,####,#,##,$$$,##,#,#,,#,$$2$$,#,,####,,$$2$$,,#####,$$,$$$,$$,###,$$$$,$,$$$$,##4$$$$#,#$$$$4##4#,,##-##,,#4##4,//,#-#,//,4##4,//,#-#,//,4##4#,,##-##,,#4#22#####-#####222233333-333332222''''777''''22//////777//////$$$$$$777$$$$$$222222222222222#%#%#%#%#%#%#%#-#-#-#-#-#-#-#-######################################################################################################################################################";
                break;
            case 15:
                levelContent = "##############################$$$$$$$-$$$$$$$))))##-%-##)))))))##-%%%-##)))))##-%%5%%-##)))##-%%5(5%%-##)##-%%5(2(5%%-###-%%5(2$2(5%%-#-%%5(2$<$2(5%%-#-%%5(2$2(5%%-###-%%5(2(5%%-##)##-%%5(5%%-##)))##-%%5%%-##)))))##-%%%-##)))))))##-%-##))))$$$$$##-##$$$$$'''''''''''''''&&&&&&&&&&&&&&&%%%%%%%%%%%%%%%$$$$$$$$$$$$$$$77777777777777########$########77777777777777#######$#######77777777777777########$#######737373737373737#######$#######/7/7/7/7/7/7/7/$$$$$$$-$$$$$$$))))##-%-##)))))))##-%%%-##)))))##-%%5%%-##)))##-%%5(5%%-##)##-%%5(2(5%%-###-%%5(2$2(5%%-#-%%5(2$A$2(5%%-#-%%5(2$2(5%%-###-%%5(2(5%%-##)##-%%5(5%%-##)))##-%%5%%-##)))))##-%%%-##)))))))##-%-##))))$$$$$##-##$$$$$'''''''''''''''&&&&&&&&&&&&&&&%%%%%%%%%%%%%%%$$$$$$$$$$$$$$$#*+,-./01234567###############77777777777777#################77777777777777###############77777777777777#################77777777777777###############77777777777777#######################################################################################################################################################";
                break;
            case 16:
                levelContent = ",#,#,,,#,,##,,,,#,#,###,#,#,##,,,#,,,#,,##,,,,#,#,###,#,#,##,#,#,,,#,#,#,,,###############3333333#3333333000#444#444#000################1.*$*#7#*$*.1##1.*$$*#*$$*.1#K#1.*$$*$$*.1#K$##1.*$$$*.1##$))K#1.*$*.1#K))$##1.**$**.1##$K#$'*$***$*'$#K#$$'$$*$*$$'$$##$$'$**$**$'$$#20.,#,,,,,#,.02###############000#444#444#000################1.*$*###*$*.1##1.*$$*#*$$*.1#A#1.*$$*$$*.1#A$##1.*$$$*.1##$))A#1.*$*.1#A))$##1.*$$$*.1##$A#1.*$$*$$*.1#A#1.*$$*#*$$*.1##1.*$*###*$*.1#20.,#,,,,,#,.02###############000#444#444#000################1.*$*###*$*.1##1.*$$*#*$$*.1#<#1.*$$*$$*.1#<$##1.*$$$*.1##$))<#1.*$*.1#<))$##1.**$**.1##$<#$'*$***$*'$#<#$$'$$*$*$$'$$##$$'$**$**$'$$#20.,#,,,,,#,.02###############000#444#444#000################1.*$*###*$*.1##1.*$$*#*$$*.1#7#1.*$$*$$*.1#7$##1.*$$$*.1##$))7#1.*$*.1#7))$##1.*$$$*.1##$7#1.*$$*$$*.1#7#1.*$$*#*$$*.1##1.*$*###*$*.1#20.,#,,,,,#,.02###############000#444#444#000######################################################################################################################################################";
                break;
            case 17:
                levelContent = "#####################################K#####################################))?))?))#))?))?))?))?))#))?))?)?))?))#))?))?)?))?))#))?))?))?))?))#))?))?)))?))?))#))?))?)))?))?))#))?))?))?))?))#))?))?)))))))#)))))));((;((#((;((;((;((;((#((;((;(((;((;((#((;((;(((;((;((#((;((;((;((;((#((;((;(;((;((#((;((;(;((;((#((;((;((;((;((#((;((;(((;((;((#((;((;(''7''7''#''7''7''7''7''#''7''7'7''7''#''7''7'7''7''#''7''7''7''7''#''7''7'''7''7''#''7''7'''7''7''#''7''7''7''7''#''7''7'7''7''#''7''7'3&&3&&#&&3&&3&&3&&3&&#&&3&&3&&&3&&3&&#&&3&&3&&&3&&3&&#&&3&&3&&3&&3&&#&&3&&3&3&&3&&#&&3&&3&3&&3&&#&&3&&3&&3&&3&&#&&3&&3&&&3&&3&&#&&3&&3&%%/%%/%%#%%/%%/%%/%%/%%#%%/%%/%/%%/%%#%%/%%/%/%%/%%#%%/%%/%%/%%/%%#%%/%%/%%%/%%/%%#%%/%%/%%%/%%/%%#%%/%%/%%/%%/%%#%%/%%/%/%%/%%#%%/%%/%+$$+$$#$$+$$+$$+$$+$$#$$+$$+$$$+$$+$$#$$+$$+$$$+$$+$$#$$+$$+$$+$$+$$#$$+$$+$+$$+$$#$$+$$+$+$$+$$#$$+$$+$$+$$+$$#$$+$$+$$$+$$+$$#$$+$$+$$$+$$+$$#$$+$$+######################################################################################################################################################";
                break;
            case 18:
                levelContent = "#######_##############_##############_##############_##############d##############_##############_##############_#######A######_######A33333%%0%%333331111##0$0##1111///##0$*$0##///--##0$*-*$0##--+%%0$*-#-*$0%%+--##0$*-*$0##--///##0$*$0##///1111##0$0##111133333%%0%%33333<######Z######<33333%%0%%333331111##0$0##1111///##0$*$0##///--##0$*-*$0##--+%%0$*-#-*$0%%+--##0$*-*$0##--///##0$*$0##///1111##0$0##111133333%%0%%333337######U######733333%%0%%333331111##0$0##1111///##0$*$0##///--##0$*-*$0##--+%%0$*-#-*$0%%+--##0$*-*$0##--///##0$*$0##///1111##0$0##111133333%%0%%333332######P######233333%%0%%333331111##0$0##1111///##0$*$0##///--##0$*-*$0##--+%%0$*-#-*$0%%+--##0$*-*$0##--///##0$*$0##///1111##0$0##111133333%%0%%33333-######K######-33333%%0%%333331111##0$0##1111///##0$*$0##///--##0$*-*$0##--+%%0$*-#-*$0%%+--##0$*-*$0##--///##0$*$0##///1111##0$0##111133333%%0%%33333#######%#######AAAAAAAAAAAAAAA######################################################################################################################################################";
                break;
            case 19:
                levelContent = "##########################################################################################+##+##7(7##+##+7##+##+%+##+##7*#+%+#+%+#+%+#*7##+%+%%%+%+##7*,##+%&%&%+##,*7#++%&$%$&%++#7*+%%%%%%%%%%%+*7#++%&$%$&%++#7*,##+%&%&%+##,*7##+%+%%%+%+##7*#+%+#+%+#+%+#*7##+##+%+##+##7+#+%+#*(*#+%+#+%+%+##7(7##+%+%&%+##,*(*,##+%&$&%++#7(7#++%&$%%%%%+*(*+%%%%%$&%++#7(7#++%&$&%+##,*(*,##+%&%+%+##7(7##+%+%+#+%+#*(*#+%+#++##+##7(7##+##+7##+##+%+##+##7*#+%+#+%+#+%+#*7##+%+%%%+%+##7*,##+%&%&%+##,*7#++%&$%$&%++#7*+%%%%%%%%%%%+*7#++%&$%$&%++#7*,##+%&%&%+##,*7##+%+%%%+%+##7*#+%+#+%+#+%+#*7##+##+%+##+##7+#+%+#*(*#+%+#+%+%+##7(7##+%+%&%+##,*(*,##+%&$&%++#7(7#++%&$%%%%%+*(*+%%%%%$&%++#7(7#++%&$&%+##,*(*,##+%&%+%+##7(7##+%+%+#+%+#*(*#+%+#++##+##7(7##+##+7##+##+%+##+##7*#+%+#+%+#+%+#*7##+%+%%%+%+##7*,##+%&%&%+##,*7#++%&$%$&%++#7*+%%%%%%%%%%%+*7#++%&$%$&%++#7*,##+%&%&%+##,*7##+%+%%%+%+##7*#+%+#+%+#+%+#*7##+##+%+##+##7######################################################################################################################################################";
                break;
            case 20:
                levelContent = "##############################################<2##(-$-(##2<##2###-$$$-###2##2##-$$7$$-##2##<2##-$$$-##2<##<<2##-$-##2<<#6(((((#6#(((((644****###****44##2<<<###<<<2####(2<<#-#<<2(##-(##2<#$#<2##(-$-###2#$#2###-$$$-##2#7#2##-$$$-##2<#$#<2##-$-##2<<#$#<<2##-#,,,,,,-,,,,,,#$&$&$&$&$&$&$&$#.#.#.#.#.#.#.#<<<<<<7<7<<<<<<#(((((666(((((##****44#44****##<<<2#####2<<<##<<2(##-##(2<<##<2##(-$-(##2<##2###-$$$-###2##2##-$$7$$-##2##<2##-$$$-##2<##<<2##-$-##2<<#,,,,,,#-#,,,,,,$&$&$&$&$&$&$&$#.#.#.#.#.#.#.#7<<<<<<<<<<<<<76(((((#6#(((((644****###****44##2<<<###<<<2####(2<<#-#<<2(##-(##2<#$#<2##(-$-###2#$#2###-$$$-##2#7#2##-$$$-##2<#$#<2##-$-##2<<#$#<<2##-#,,,,,,-,,,,,,#$&$&$&$&$&$&$&$#.#.#.#.#.#.#.#<<<<<<7<7<<<<<<#(((((666(((((##****44#44****##<<<2#####2<<<##<<2(##-##(2<<##<2##(-$-(##2<##2###-$$$-###2##2##-$$7$$-##2##<2##-$$$-##2<##<<2##-$-##2<<#,,,,,,#-#,,,,,,$&$&$&$&$&$&$&$#.#.#.#.#.#.#.#7<<<<<<<<<<<<<7######################################################################################################################################################";
                break;
            case 21:
                levelContent = "#############################################$$$$$$$A$$$$$$$777$7777777$777-$$$$$7#7$$$$$-$$$$$$$A$$$$$$$,(,(,#####,(,(,0(0(0000000(0(0))0'''')''''0))777$7777777$777$$$$$$$A$$$$$$$,(,(,#####,(,(,0(0(0000000(0(0))0'''')''''0))d77$7777777$77d$$$$$$$A$$$$$$$777$7777777$777-$$$$$7#7$$$$$-A$AAA$7#7$AAA$AA$$$A$$#$$A$$$AAAA$AAA#AAA$AAA$$A$$$$#$$$$A$$---)---)---)---##,(,(,#,(,(,##000(0(000(0(000''''0)))))0''''777$7777777$777$$$$$$$A$$$$$$$777$7777777$7777$$$$$-#-$$$$$77$AAA$A#A$AAA$7$$A$$$A#A$$$A$$AAA$AAAcAAA$AAA$$$$A$$#$$A$$$$777)777)777)777,(,(,#####,(,(,0(0(0000000(0(0))0'''')''''0))777$7777777$777$$$$$$$A$$$$$$$777$7777777$777-$$$$$7#7$$$$$-A$AAA$7#7$AAA$AA$$$A$$#$$A$$$AAAA$AAA#AAA$AAA$$A$$$$#$$$$A$$777)777)777)777##,(,(,#,(,(,##000(0(000(0(000''''0)))))0''''777$7777777$777$$$$$$$A$$$$$$$777$7777777$7777$$$$$-#-$$$$$77$AAA$A#A$AAA$7$$A$$$A#A$$$A$$AAA$AAA#AAA$AAA$$$$A$$#$$A$$$$AAA7AAAAAAA7AAA######################################################################################################################################################";
                break;
            case 22:
                levelContent = "##############################-##.#*#'#*#.##-$-#.#*#$#*#.#-$$%-.#*#-#*#.-%$$'%-#*#'#*#-%'$$___-##c##-___$$''''-'-'-''''$$'''''-'-'''''$$<(<(<(A(<(<(<$$$$$$$$-$$$$$$$$%-.#*#-#*#.-%$$'%-#*#'#*#-%'$$___-##c##-___$$''''-'-'-''''$$'''''-'-'''''$$<(<(<(A(<(<(<$$$$$$$$-$$$$$$$-##.#*###*#.##-$-#.#*###*#.#-$$%-.#*###*#.-%$$'%-#*###*#-%'$$___-#####-___$$''''-'#'-''''$$'''''-#-'''''$$<(<(<(#(<(<(<$$$$$$$$#$$$$$$$(<(<(<(#(<(<(<(###############-##.#*#'#*#.##-$-#.#*#$#*#.#-$$%-.#*#-#*#.-%$$'%-#*#'#*#-%'$$___-##c##-___$$''''-'-'-''''$$'''''-'-'''''$$<(<(<(A(<(<(<$$$$$$$$-$$$$$$$(<(<(<(<(<(<(<(-##.#*###*#.##-$-#.#*###*#.#-$$%-.#*###*#.-%$$'%-#*###*#-%'$$___-#####-___$$''''-'#'-''''$$'''''-#-'''''$$<(<(<(#(<(<(<$$$$$$$$#$$$$$$$(<(<(<(#(<(<(<(###############-##.#*#'#*#.##-$-#.#*#$#*#.#-$$%-.#*#-#*#.-%$$'%-#*#'#*#-%'$$___-##c##-___$$''''-'-'-''''$$'''''-'-'''''$$<(<(<(A(<(<(<$$$$$$$$-$$$$$$$(<(<(<(<(<(<(<(######################################################################################################################################################";
                break;
            case 23:
                levelContent = "###########################################################################AAAAAAA##AAAAAA#######A#######AAAAAA#fAAAAAAA#######7#######7777777#7777777#####*###*###########*#*#######2##22#*#22##2#2#22##*&*##22#26666#*#*#*#6666AAAAAA##AAAAAAA#######7#######7777777#7777777#####*###*###########*#*#######2##22#*#22##2#2#22##*&*##22#26666#*#*#*#6666#######4########-----#4#-----##-'''-#4#-'''-##-'2'-#4#-'2'-##-'2'-#4#-'2'-##22##2#&#2##22#*##22#2*2#22##*#*#666646666#*#f######4######f#-----#4#-----##-'''-#4#-'''-##-'2'-#4#-'2'-##-'2'-#7#-'2'-#7777777#7777777#######A#######AAAAAAA##AAAAAA#######A#######AAAAAA##AAAAAAA#######7#######7777777#7777777#####*###*###########*#*#######2##22#*#22##2#2#22##*&*##22#26666#*#*#*#6666#######4########-----#4#-----##-'''-#4#-'''-##-'2'-#4#-'2'-##-'2'-#4#-'2'-#777777777777777################AAAAAAAAAAAAAA###############AAAAAAAAAAAAAA################777777777777777######################################################################################################################################################";
                break;
            case 24:
                levelContent = "*2*$K#'#'#K$*2**2*$-##2##-$*2**2*$K#'#'#K$*2*#*2*$#####$*2*###*2**$K$**2*##*2*$K#'#'#K$*2**2*$-##2##-$*2**2*$K#'#'#K$*2*##-$*2*2*2*$-##f##*22***22*##f##*2**$$$**2*###*2*$$$K$$$*2*#*2*$$#####$$*2**2*$K#'#'#K$*2**2*$-##2##-$*2**2*$K#'#'#K$*2****$$#####$*2*#$$$$$$$K$**2*##,%%%%%%%*22*##,',##'''*2**##,'*##$###*###$##*2**##K#2#K##**2*22*###*###*22*$**2*#7$7#*2**$$$$*2*#K#*2*$$$##$$*2*#*2*$$##'#K$*2*#*2*$K#'##-$*2*2*2*$-##'#K$*2*#*2*$K#'##$*2*##***$$##$**2*##K$$$$$$$*22*##,%,%%%%%%2**##,'*',##'''*###,''2'',###**##,'''2''',##**##,'''2''',##*A##,'''A''',##A#A##,''2'',##A#2#A##,'2',##A#2#A##,''2'',##A#######***######AA##**222**##AAf##*22***22*##f##*2**$$$**2*###*2*$$$K$$$*2*#*2*$$#####$$*2**2*$K#'#'#K$*2**2*$-##2##-$*2**2*$K#'#'#K$*2****$$#####$*2*#$$$$$$$K$**2*##,%%%%%%%*22*##,',##'''*2**##,''',###*2*###,''''',##*2*##,'''''',##*2*##,'''''',##AAA##,''''',##A#2#A##,''',##A#222#A##,''',##A#2#A##,''######################################################################################################################################################";
                break;
            case 25:
                levelContent = "(2##*$$$$$*##2((2-$*$A$A$*$-2((2-#*$A$A$*$-2((2##*$$$$$*##2(#(2#$,#-#,$#2(#'#(2#$(#($#2(#'''#(22$/$22(#'''''#((222(##'''7#$<<'$#$'<<$#7#7#$<<'%'<<$#7#%#7#$<<A<<$#7#%A%#7#$<#<$#7#%A#A%#7#$$$#7#%A#$#A%#7#K#7#%A#$K$#A%#7#7#%A#$K$'<<$#7#7#$<<'$'<<$#7#%#7#$<<'<<$#7#%A%#7#$<<<$#7#%A#A%#7#$<$#7#%A#$#A%#7#$#7#%A#$K$#A%#7#7#%A#$K#K$#A%#72((#'''2'''#((2$$2(#''/''#(22$($#2(#'#'#(2#$(#,$#2(#-#(2#$,#$$*##2($(2##*$$A$*$-2($(2-$*$AA$*$-2($(2-#*$A$$*##2($(2##*$$#,$#2(#-#(2#$,#($#2(#'#'#(2#$($22(#''/''#(22$2(##'''2'''#((2'''#((222((#'''''#(22$/$$2(#'''#(2#$(#($#2(#'#(2#$,#-#,$#2(#(2##*$$$$$*##2((2-$*$A$A$*$-2((2-#*$A$A$*$-2((2##*$$$$$*##2(#(2#$,#-#,$#2(#'#(2#$(#($#2(#'''#(22$/$22(#'''''#((222(##'''7#$<<'$#$'<<$#7#7#$<<'%'<<$#7#%#7#$<<A<<$#7#%A%#7#$<#<$#7#%A#A%#7#$$$#7#%A#$#A%#7#K#7#%A#$K$#A%#7#7#%A#$K$'<<$#7#7#$<<'$'<<$#7#%#7#$<<'<<$#7#%A%#7#$<<<$#7#%A#A%#7#$<$#7#%A#$#A%#7#$#7#%A#$K$#A%#7#7#%A#$K#K$#A%#7######################################################################################################################################################";
                break;
            case 26:
                levelContent = "#################7####U####7##*#$a#(,@,(#a$#*##0$#,@$@,#$0##*#$0#@$f$@#0$#*##00#,@$@,#00##$#*##(,@,(##*#$5##A#(,@,(##A#5*#$2#,@$@,#2$#*7#$)#@$f$@#)$#7)#$$#,@$@,#$$#))#)##(,@,(##)#)7#$a#44444#a$#7,#0$#&&&&&#$0#,,#$0#*(7(*#0$#,,#00#(A#A(#00#,##*###*0*###*##*##A#,#7#,##A#*##$2#$(0($#2$##*#$##$$7$$##$#*##$$#$(0($#$$##$#95#,#7#,#59#$5#%,##202##,%#5*#a###'7'###a#*7#KK#__c__#KK#7,#a##&&&&&##a#,,##0#(*7*(#0##,,#0##A(#(A##0#,##00#*#0#*#00##*##*##,7,##*##*##A##($0$(#A###*#2$#$$7$$#$2#*###$#($0$(#$###$#$$##,7,##$$#$5#59#2#0#2#95#5*#,%#'#7#'#%,#*7#$a#44444#a$#7,#0$#&&&&&#$0#,,#$0#*(7(*#0$#,,#00#(A#A(#00#,##*###*0*###*##*##A#,#7#,##A#*##$2#$(0($#2$##*#$##$$7$$##$#*##$$#$(0($#$$##$#95#,#7#,#59#$5#%,##202##,%#5*#a###'7'###a#*7#KK#__c__#KK#7,#a##&&&&&##a#,,##0#(*7*(#0##,,#0##A(#(A##0#,##00b*#0#*b00##*##*b#,7,#b*##*#bA##($0$(#A#b#*#2$b$$7$$b$2#*###$b($0$(b$###$#$$##,7,##$$#$5#59#2#0#2#95#5*#,%#'#7#'#%,#*######################################################################################################################################################";
                break;
            case 27:
                levelContent = "2$$$$$#a#$$$$$2$7777777777777$2$$$$$#a#$$$$$2-(-(-(#a#(-(-(-,$($($#a#$($($,7a7777*7*7777a7f$%&'()a)('&%$f#7777777777777########a#######KaKKKK#a#KKKKaK#######a########7777777777777##'#'#'#'#'#'#'#777777$7$777777#$$$$$2a2$$$$$##(-(-(-a-(-(-(##$($($,a,$($($#*77777a#a77777*)('&%$faf$%&'()777777#7#777777#######a########KKKKKaaaKKKKK########a#######777777#7#777777#'#'#'#'#'#'#'#$7777777777777$2$$$$$#a#$$$$$2-(-(-(#a#(-(-(-,$($($#a#$($($,7a7777*7*7777a7f$%&'()a)('&%$f#7777777777777########a#######KaKKKK#a#KKKKaK#######a########7777777777777##'#'#'#'#'#'#'#777777$7$777777#$$$$$2a2$$$$$##(-(-(-a-(-(-(##$($($,a,$($($#*77777a#a77777*)('&%$faf$%&'()777777#7#777777#######a########KKKKKaaaKKKKK########a#######777777#7#777777#'#'#'#'#'#'#'#$7777777777777$2$$$$$#a#$$$$$2-(-(-(#a#(-(-(-,$($($#a#$($($,7a7777*7*7777a7f$%&'()a)('&%$f#7777777777777########a#######KaKKKK#a#KKKKaK#######a########7777777777777#######################################################################################################################################################";
                break;
            case 28:
                levelContent = ")+)+-#&0&#-+)+)+)+%-##0##-%+)+0+)++-#0#-++)+00)))%-#0#-%)))00++++-#0#-++++0##))%-#0#-%))#################72&##-----##&272&##-+)+)+-##&2&##-)%)+)%)-##&&#-+%))0))%+-#&&#-)+))$))+)-#&&#-+)+)0)+)+-#&##-%+)+0+)+%-##--##&27-72&##--)+-##&2+2&##-+))%)-##&+&##-)%)))%+-#&0&#-+%))))+)-#&$&#-)+)))+)+-#&0&#-+)+)+)+%-##0##-%+)+0+)++-#0#-++)+00)))%-#0#-%)))00++++-#0#-++++0##))%-#0#-%))#################72&##-----##&272&##-+)+)+-##&2&##-)%)+)%)-##&&#-+%))0))%+-#&&#-)+))$))+)-#&&#-+)+)0)+)+-#&##-%+)+0+)+%-###-++)+000+)++-##-%)))000)))%-##-++++000++++-##-%))##0##))%-#--##&27-72&##--)+-##&2+2&##-+))%)-##&+&##-)%)))%+-#&0&#-+%))))+)-#&$&#-)+)))+)+-#&0&#-+)+)+)+%-##0##-%+)+0+)++-#0#-++)+00)))%-#0#-%)))00++++-#0#-++++0##))%-#0#-%))#################72&##-----##&272&##-+)+)+-##&2&##-)%)+)%)-##&&#-+%))0))%+-#&&#-)+))$))+)-#&&#-+)+)0)+)+-#&##-%+)+0+)+%-###-++)+000+)++-##-%)))000)))%-##-++++000++++-##-%))##0##))%-#######################################################################################################################################################";
                break;
            case 29:
                levelContent = "((222#(#(#,,,%%22###(((((###,,###...aaa...###,,###((%((###22%%,,,#(#(#222((a#a#a#a#a#a#a#a$<#(22###,,%#<&#$#$((2#,%%&#&####$$(2a,%&&#####(((2#.#,%%%##((222#(#(#,,,%%22###(((((###,,###...aaa...###,,###((%((###22%%,,,#(#(#222((##%%%,#.#2(((#####&&%,a2($$####&#&%%,#2(($#$#&<#%,,###22(#<$#,,%#<&#$<#(22#,%%&#&###$#$((2,%&&###a###$$(2#,%%%##.##(((2#(#,,,%%#((222#(((###,,(22###((a...###a###...a((###22%,,###(((#222((#%%,,,#(#2(((##.##%%%,#2($$###a###&&%,2(($#$###&#&%%,#22(#<$#&<#%,,#7$$$7$$$7$$$7$$$$7$$$7$$$7$$7$$<#(22###,,%#<&#$#$((2#,%%&#&####$$(2a,%&&#####(((2#.#,%%%##((222#(#(#,,,%%22###(((((###,,###...aaa...###,,###((%((###22%%,,,#(#(#222((##%%%,#.#2(((#####&&%,a2($$####&#&%%,#2(($#$#&<#%,,###22(#<$#,,%#<&#$<#(22#,%%&#&###$#$((2,%&&###a###$$(2#,%%%##.##(((2#(#,,,%%#((222#(((###,,(22###((a...###a###...a((###22%,,###(((#222((#%%,,,#(#2(((##.##%%%,#2($$###a###&&%,2(($#$###&#&%%,#22(#<$#&<#%,,#######################################################################################################################################################";
                break;
            case 30:
                levelContent = "7,7,7,7,7,7,7,7#%,%,%#_#%,%,%#<#%2%#7_<#%2%#7#%,%,%#d#%,%,%#%,%*%,%_%,%*%,%#4#4#4K4K4#4#4#7#7#7#7#7#7#7#7,%*#*%,7,%*#*%,%,$*%,%_%,$*%,%#%,%,%#_#%,%,%#<#%2%#7_<#%2%#7#%,%,%#d#%,%,%#%,%*%,%_%,%*%,%,%*#*%,#,%*#*%,a##a##a#a##a##a$$$$$$$f$$$$$$$$K-A--7U$K-A--7#%#%#%#%#%#%#%#-K-A--7U7--A-K-$#$#$#$#$#$#$#$_______c_______'$*A*$'_'$*A*$'$'$*$'$_$'$*$'$#$'$'$#_#$'$'$#7#$'$#7_7#$'$#7A$'$'$#d#$'$'$A$'$*$'$_$'$*$'$'$*#*$'#'$*#*$'a$#a#$a#a##a##a$$$$$$$f$$$$$$$7--A-K$U$K-A--7#'#'#'#'#'#'#'#7--A-K-U-K-A--77#7#7#7#7#7#7#7,%*#*%,7,%*#*%,%,$*%,%_%,$*%,%#%,%,%#_#%,%,%#<#%2%#7_<#%2%#7#%,%,%#d#%,%,%#%,%*%,%_%,%*%,%,%*#*%,#,%*#*%,aa#a#######a#aa$$$$$$$f$$$$$$$$K-A--7U$K-A--7U#7#7#7#7#7#7#U-K-A--7U7--A-K-###############_______c_______'$*#*$'_'$*#*$'$'$*$'$_$'$*$'$#$'$'$#_#$'$'$#7#$'$#7_7#$'$#7#$'$'$#d#$'$'$#$'$*$'$_$'$*$'$'$*#*$'#'$*#*$'$$$$$$$f$$$$$$$7--A-K$U$K-A--7###############7--A-K-U-K-A--7#####################################################################################################################################################################";
                break;
            case 31:
                levelContent = "(##2##(#(##2##(K(###(KaK(###(K(##.##(a(##.##(K(##.(K<K(.##(K(##2##(#(##2##(K(###(KaK(###(K(##.##(a(##.##(K(##.(K<K(.##(K(##<#.(((.#<##(#.#(<#.#.#<(#.##.##(<#0#<(##.#.####(<#<(####.27#7272#2727#72###############-*##-*-0-*-##*-$<#<$<(#(<$<#<$<&##<$<,<$<##&<&<#<(<$#$<(<#<&#(#(##2#2##(#(#(K#K(##a##(K#K(#(#(##.a.##(#(#(K#K(.#<#.(K#K(#(#(.#<(<#.(#(#.##.#<(#(<#.##..###<(#0#(<###.#.#<(#####(<#.#72#2727#7272#27##b#########b##*-b-*-#0#-*-b-*<$#(<$<#<$<(#$<&<b<$<#,#<$<b<&<&b$<(<#<(<$b&<(##2##(#(##2##(K(###(KaK(###(K(##.##(a(##.##(K(##.(K<K(.##(K(##<#.(((.#<##(#.#(<#.#.#<(#.##.##(<#0#<(##.#.####(<#<(####.27#7272#2727#72###############-*##-*-0-*-##*-$<#<$<(#(<$<#<$<&##<$<,<$<##&<&<#<(<$#$<(<#<&#(#(##2#2##(#(#(K#K(##a##(K#K(#(#(##.a.##(#(#(K#K(.#<#.(K#K(#(#(.#<(<#.(#(#.##.#<(#(<#.##..###<(#0#(<###.#.#<(#####(<#.#72#2727#7272#27##b#########b##*-b-*-#0#-*-b-*<$#(<$<#<$<(#$<&<b<$<#,#<$<b<&<&b$<(<#<(<$b&<######################################################################################################################################################";
                break;
            case 32:
                levelContent = "($$(($$$$$(($$(#-$(($-$-$(($-##-$K($-$-$K($-##7$(($7$7$(($7##7$$$$7$7$$$$7###2$$2#-#2$$2##2#2$$2#-#2$$2#22##KK#####KK##2%_%__7_#_7__%_%$_$__$_#_$__$_$7AKAAKA7AKAAKA7#7K77K7#7K77K7#a#7##7###7##7#a$#7a#7#$#7#a7#$%#2##2#%#2##2#%'#2#a2#'#2a#2#'+#-##-#+#-##-#+/#-a#-#/#-#a-#/3#(##(<3<(##(#37#$#a$#7#$a#$#7($$(($$$$$(($$(#-$(($-$-$(($-##-$(($-$-$(($-##7$(($7$7$(($7##7$$$$7$7$$$$7###2$$2#-#2$$2##2#2$$2#-#2$$2#22##KK#####KK##2%U%UU7U#U7UU%U%$_$__$_#_$__$_$7A$AA$A7A$AA$A7#7K77K7#7K77K7#a#7##7###7##7#a$#7a#7#$#7#a7#$%#2##2#%#2##2#%'#2#a2#'#2a#2#'+#-##-#+#-##-#+/#-a#-#/#-#a-#/3#(##(<3<(##(#37#$#a$#7#$a#$#7($$($$($($$($$(($-#-$($($-#-$(($-#-$($($-#-$(($7#7$($($7#7$($$7#7$$$$$7#7$$$2#a#2$-$2#a#2$$2#2#2$-$2#2#2$K##2##K#K##2##KU7U%_%_#_%_%U7U_$_$_$_#_$_$_$_A$A7A$A7A$A7A$A7K7#7K7#7K7#7K7#7###7###7###7##7#$#7#$#7#$#7##2#%#2#%#2#%#2##2#'#2#'#2#'#2##-#+#-#+#-#+#-##-#/#-#/#-#/#-##(#3#(#3#(#3#(##$#7#$#7#$#7#$#######################################################################################################################################################";
                break;
            case 33:
                levelContent = "222'##'2'##'22222'##'222'##'222'##'22222'##'2'##'2222222'##'777777727777777-------2-------$$*2##A2A##2*$$*$$*2##2##2*$$*#*$$*2#2#2*$$*###*$$*222*$$*##A##*$$*2*$$*##AA##2*$$2$$*2##A##2*$$*2*$$*2###2*$$*#2#*$$*2#2*$$*##2##*$$*2*$$*##A2A##*$$*-------2-------'##'2222222'##'2'##'22222'##'222'##'222'##'22222'##'2'##'222777777727777777$$*2##A_A##2*$$*$$*2##Y##2*$$*#*$$*2#Y#2*$$*###*$$*2Y2*$$*##222'##'2'##'22222'##'222'##'222'##'22222'##'2'##'2222222'##'777777727777777-------2-------$$*2##A2A##2*$$*$$*2##2##2*$$*#*$$*2#2#2*$$*###*$$*222*$$*##A##*$$*2*$$*##AA##2*$$2$$*2##A##2*$$*2*$$*2###2*$$*#2#*$$*2#2*$$*##2##*$$*2*$$*##A2A##*$$*-------2-------'##'2222222'##'2'##'22222'##'222'##'222'##'22222'##'2'##'222777777727777777$$*2##A_A##2*$$*$$*2##Y##2*$$*#*$$*2#Y#2*$$*###*$$*2Y2*$$*##A##*$$*Y*$$*##A_YYYYYYeYYYYYY_222'##'Y'##'22222'##'2Y2'##'222'##'22Y22'##'2'##'222Y222'##'7777777_7777777#####################################################################################################################################################################";
                break;
            case 34:
                levelContent = "B##****-----##B$B##***----##B$$$B##**---##B$$27$B##*2-##B$72277$B##2##B$772222$$B#2#B$$222##*****B-----##B##****#----##B$B##***#---##B$$$B##**2--##B$$27$B##*2-##B$72277$B##2##B$772222$$B#-#B$$222###2$$B-B$$2###222$$B#-#B$$222277$B##2##B$77227$B##*2*##B$72$$B##**2**##B$$-----##B##*****----##B$B##****---##B$$$B##***--##B$$2$$B##**-##B$72227$B##*##B$7722277$B###B$$222-222$$B#B$$222---222$$B#B$$222-222$$B###B$7722277$B##*##B$72227$B##***##B$$2$$B##**$B##***----##B$$$B##**---##B$$27$B##*2-##B$72277$B##2##B$772222$$B#2#B$$222##*****B-----##B##****#----##B$B##***#---##B$$$B##**2--##B$$27$B##*2-##B$72277$B##2##B$772222$$B#-#B$$222###2$$B-B$$2###222$$B#-#B$$222277$B##2##B$77227$B##*2*##B$72$$B##**2**##B$$-----##B##*****----##B$B##****---##B$$$B##***--##B$$2$$B##**-##B$72227$B##*##B$7722277$B###B$$222-222$$B#B$$222---222$$B#B$$222-222$$B###B$7722277$B##*##B$72227$B##***##B$$2$$B##**#####################################################################################################################################################################";
                break;
            case 35:
                levelContent = "####77/#/77####7#77//(#(//77#7#7///(-#-(///7#'///(-B#B-(///'$$$##%7#7%##$$$####77/#/77####7#77//(#(//77#7#7///(-#-(///7#'///(-B#B-(///'-'/(-BQ#QB-(/'-'///(-B#B-(///'#7///(-#-(///7#7#77//(#(//77#7####77/#/77####$$$##%7#7%##$$$7%##$$$#$$$##%7/77#########77/(//77#7#7#77//(-(///7###7///(-B-(///'#'///(-BQB-(/'-#-'/(-BQB-(///'#'///(-B-(///7###7///(-(//77#7#7#77//(/77#########77/7%##$$$#$$$##%7'///(-B#B-(///'#7///(-#-(///7#7#77//(#(//77#7####77/#/77####$$$##%7#7%##$$$QB-(/'-#-'/(-BQ$$$##%7#7%##$$$####77/#/77####7#77//(#(//77#7#7///(-#-(///7#'///(-B#B-(///'$$$##%7#7%##$$$####77/#/77####7#77//(#(//77#7#7///(-#-(///7#'///(-B#B-(///'-'/(-BQ#QB-(/'-'///(-B#B-(///'#7///(-#-(///7#7#77//(#(//77#7####77/#/77####$$$##%7#7%##$$$7%##$$$#$$$##%7/77#########77/(//77#7#7#77//(-(///7#b#7///(-B-(///'b'///(-BQB-(/'-b-'/(-BQB-(///'b'///(-B-(///7#b#7///(-(//77#7#7#77//(/77#########77/7%##$$$#$$$##%7#####################################################################################################################################################################";
                break;
            case 36:
                levelContent = "$$$$$$$$$$$$$$$(a$a(a$a$a(a$a((a$a(a$a$a(a$a((a$a(a$a$a(a$a(7#7#7#7#7#7#7#77#7#7#7#7#7#7#77#7#7#7#7#7#7#7###############BBBBBBBB#BBBBBB###############VVVVVV#VVVVVVVV7777777777777777##a#2-A-2#a##77&&&&-###-&&&&77##a#A#a#A#a##77&&&&-###-&&&&77##a#2-A-2#a##7777777777777777fK#'#'#'#'#'#KfAAAAAAAAAAAAAAA$$$$$$$$$$$$$$$#-&&&&7#7&&&&-##A#a##7a7##a#A##-&&&&7#7&&&&-#-2#a##7A7##a#2-777777777777777#'#'#Kf'fK#'#'#AAAAAAAAAAAAAAA$$$$$$$$$$$$$$$(a$a(a$a$a(a$a((a$a(a$a$a(a$a((a$a(a$a$a(a$a(7#7#7#7#7#7#7#77#7#7#7#7#7#7#77#7#7#7#7#7#7#7###############BBBBBBBB#BBBBBB###############VVVVVV#VVVVVVVV7777777777777777##a#2-A-2#a##77&&&&-###-&&&&77##a#A#a#A#a##77&&&&-###-&&&&77##a#2-A-2#a##7777777777777777fK#'#'#'#'#'#KfAAAAAAAAAAAAAAA$$$$$$$$$$$$$$$$a(a$a(a(a$a(a$$a(a$a(a(a$a(a$$a(a$a(a(a$a(a$7#7#7#7#7#7#7#77#7#7#7#7#7#7#77#7#7#7#7#7#7#7################BBBBBBBBBBBBBB###############VVVVVVVVVVVVVV######################################################################################################################################################################";
                break;
            case 37:
                levelContent = "U#4#'&*A*&'#4#U'$'P&*(K(*&P'$'U#4#'&*A*&'#4#U#:#0.#&#&#.0#:#*#F##.###.##F#*#a##.#'a'#.##a#$##*##&F&##*##$#$*#a(#,#(a#*$#<#$*##&*&##*$#<2###<<#P#<<###2<#<<%%<#<%%<<#<#<%%7%%a%%7%%<#<#77%7%#%7%77#<A7%%#%7#7%#%%7A<%###%7*7%###%<$##,##'2'##,##$##a##&#2#&##a###$$#&#a2a#&#$$##:#0.#&#&#.0#:#U#4#'&*A*&'#4#U'$'#&*(K(*&#'$'U#4#'&*A*&'#4#U#:#0.#&#&#.0#:#*#F#P.###.P#F#*#a##.#'a'#.##a#$##*##&F&##*##$#$*#a(#,#(a#*$#<#$*##&*&##*$#<2###<<#P#<<###2<#<<%%<#<%%<<#<#<%%7%%a%%7%%<#<#77%7%#%7%77#<A7%%#%7#7%#%%7A<%###%7*7%###%<#<<#%7#,#7%#<<####<%7###7%<#####a<%a#$#a%<a##'##,##$F$##,##'#&#Pa##,##aP#&#a#&#$$#*#$$#&#a&#.0#:###:#0.#&*&'#4#U#U#4#'&*(*&#'$'a'$'#&*(*&'#4#U#U#4#'&*&#.0#:###:#0.#&#.##F#***#F##.#'#.##a#,#a##.#'&##*##$#$##*##&P(a#*$#$#$*#a(P&##*$#<2<#$*##&#<<###222###<<#<%%<<#<2<#<<%%<%%7%%<###<%%7%%%7%77#<A<#77%7%7%#%%7AKA7%%#%77%###%<A<%###%7#7%#<<###<<#%7##7%<#######<%7##a%<a##a##a<%a######################################################################################################################################################################";
                break;
            case 38:
                levelContent = ":##A##44:##A##4@###.#@4@###.#@&<#U#<&@&<#U#<&####:&4&####:&4@#A#:&@4@#A#:&@&<#.#<&@&<#.#<&4&:####&4&:####@&:#U#@4@&:#U#@&<#.#<&@&<#.#<&##A#:&4&##A#:&4@###:&@4@###:&@&<#U#<&@&<#U#<&4&:#.##&4&:#.##4&:##A##4&:##A#&<##.##4&<##.##:##A##4&:##A##4@###:&@@@###:&@&<#U#<&&&<#U#<&4&:#.##44&:#.##4&:##A#@4&:##A#&<##.##&&<##.##:##A##44:##A##4@###.#@4@###.#@&<#U#<&@&<#U#<&####:&4&####:&4@#A#:&@4@#A#:&@&<#.#<&@&<#.#<&4&:####&4&:####@&:#U#@4@&:#U#@@###.#@&@###.#@&<#U#<&4&<#U#<&####:&4@####:&4@#A#:&@&@#A#:&@&<#.#<&#&<#.#<&4&:####44&:####@&:#U#@&@&:#U#@&<#.#<&@&<#.#<&##A#:&44##A#:&4@###:&@@@###:&@&<#U#<&&&<#U#<&4&:#.##44&:#.##4&:##A#@4&:##A#&<##.##&&<##.##:##A##44:##A##4@###.#@4@###.#@&<#U#<&@&<#U#<&####:&4&####:&4@#A#:&@4@#A#:&@&<#.#<&@&<#.#<&4&:####&4&:####@&:#U#@4@&:#U#@&<#.#<&@&<#.#<&##A#:&4&##A#:&4@###:&@4@###:&@&<#U#<&@&<#U#<&4&:#.##&4&:#.##4&:##A##4&:##A#&<##.##4&<##.##:##A##4&:##A##4#####################################################################################################################################################################";
                break;
            case 39:
                levelContent = "*(&$6$8,8$6$&(*444&6#8*8#6&444#(4#6$8(8$6#4(#2#4&6#8&8#6&4#22(4#6$8$8$6#4(22#4&6#8#8#6&4#22(4#6$8#8$6#4(22#4&6#8<8#6&4#2#,*(&$###$&(*,#,#####,#,#####,###KKK#:#KKK###.#K###K#K###K#..###_#K#K#_###..#K###K8K###K#.###KKK###KKK###K#####,#,#####K#K#0.,*6*,.0#K##K#.,*(.(*,.#K##K#,*(&4&(*,#K#K#,*(&$,$&(*,#KJ##,,,,6,,,,##J#H##*******##H###F##(((((##F##H##D##&&&##D##H#F##B##$##B##F###D##@###@##D##F##B##>#>##B##F#D##@##<##@##D###B##>###>##B##D##@##<#<##@##D#B##>##:##>##B###@##<###<##@##B##>##:#:##>##B#@##<##8##<##@###>##:###:##>##@##<##8#8##<##@8888888688888888$#$#$#.#$#$#$88#66666466666#88$6$&(*,*(&$6$88#6&4446444&6#88$6#4(#*#(4#6$88#6&4#242#4&6#88$6#4(2#2(4#6$88#6&4#2#2#4&6#88$6#4(2#2(4#6$88#6&4#2#2#4&6#8#$&(*,###,*(&$#,#####,8,#####,#KKK#######KKK#K###K#...#K###KK#_###.8.###_#KK###K#...#K###K#KKK#######KKK#,#####K#K#####,*,.0#K###K#0.,*(*,.#K#_#K#.,*(&(*,#K###K#,*(&$&(*,#KKK#,*(&$#####################################################################################################################################################################";
                break;
            case 40:
                levelContent = "#######AKKKK#KK#FFFFF#AFFFF####P###P#K#####UU#P#P#P#ZUUUUUU#KK#P###U##########KKKK##PPPPPP#KKK###U############K##PPPPPP##KKK###U############K##PPPPPP#KKKKKK#K33333P################XXXX4XXX4XXXXXX#######ISSSS#SS#NNNNN#INNNN####N###N#I#####``#N#N#N#XSSSSSS#NNNN#####NNNNN######SSS#N###N#SSSSSS###N#N#N########III#N####NNNNNNI###IIII#######I#III###NNNNNN#I#####I#11111N#IIIIIII##N#N#N#XSSSSSS#II#N###S##########IIII##NNNNNN#III###S############I##NNNNNN#IIIIII#I11111N################TTTT0TTT0TTTTTTOOOO#OOO#######JJJJ#####JJJJJ######OOO#J###J#OOOOOO###J#J#J################ZZZZZZZKZZZZZZZ#######KUUUU#UU#PPPPP#KPPPP####P###P#K#####UU#P#P#P#ZUUUUUU#KK#P###U##########KKKK##PPPPPP#KKK###U############K##PPPPPP#KKKKKK#K33333P################ZZZZ6ZZZ6ZZZZZZUUUU#UUU#######KKKK#####KKKKK######PPP#K###K#PPPPPP###K#K#K########FFF#K####KKKKKKF###FFFF#######F#FFF###KKKKKK#F#####F#.....K#FFFFFFF######################################################################################################################################################################";
                break;
            case 41:
                levelContent = "<2###2<<<2###2<<2#7##222##7#2<2##77#####77##2###7,77777,7#####7,,,,,,,,,7###7,,,,,,,,,,,7###7,,K,,,K,,7####7,,,,,,,,,7##2##77,,2,,77##2A2###7,,,7###2AKA2###777###2AK,,,,7##,##7,,,,<<<<<<<<<<<<<<<$KKKKKKKKKKKKK$###############KKKKKKK$KKKKKKK$$$$$$$$$$$$$$$#*#A#*#A#*#A#*#A$A$A$A$A$A$A$A#A#*#A#*#A#*#A#<<222<<<<<222<<KKKKKK$K$KKKKKK-----#####-----KKKKKKK$KKKKKKK################*#A#*#A#*#A#*#A$A$A$A$A$A$A$A#A#*#A#*#A#*#A#<<222<<<<<222<<<2###2<<<2###2<2##7#2<2<2#7##2##77##2#2##77##77,7###7###7,77,,,,7##,##7,,,,,,,,,7#,#7,,,,,,K,,7##,##7,,K,,,,,7##,##7,,,,,,77##222##77,,,7###2A,A2###7,7###2AK7KA2###7$KKKKKKKKKKKKK$###############KKKKKKK$KKKKKKK################*#A#*#A#*#A#*#A$A$A$A$A$A$A$A#A#*#A#*#A#*#A#<<222<<<<<222<<<2###2<<<2###2<<2#7##222##7#2<2##77#####77##2###7,77777,7#####7,,,,,,,,,7###7,,,,,,,,,,,7###7,,K,,,K,,7####7,,,,,,,,,7##2##77,,2,,77##2A2###7,,,7###2AKA2###777###2AK#####################################################################################################################################################################";
                break;
            case 42:
                levelContent = "#K#K#K#K#K#K#K##K#K#K#K#K#K#K##K###K###K###K#KKKKKK#K#KKKKKK2$#(((((((((#$2.2$#7#<K<#7#$2.###-#<8K8<#-###A2##<84K48<##2AA.2#847U748#2.AA*.2<84K4882.*AK-*.#<8K8<#.*-KA*..##<K<##2.*AA.2##_###_##2.AA2#$$#_#_#$$#2A$$$#$$$_$$$#$$$AAA##_$#$_##AAAAAAA##_$_##AAAAAAAAA##_##AAAAAAAAAAA###AAAAAA((((##2A2##((((#####2.A.2#######&#2.*A*.2#&##_###.*-K-*.###_#_##2.*A*..##_###_##2.A.2##_##A##_##2A2##_##AAA##_#####_##AAAAA##_###_##AAAAAAA##_#_##AAAAAAAAA##_##AAAAAAAAAAA###AAAAAA_______c_______###K###K###K####K#K#K#K#K#K#K##K#K#K#K#K#K#K##K#K#K#K#K#K#K##K#K#K#K#K#K#K##K###K###K###K#KKKKKK#K#KKKKKK2##(((((((((##2.2###########2.*.2#&##_##&#2.*-*.###_#_###.*-*..##_###_##2.*.2##_##A##_##2.2##_##AAA##_##2##_##AAAAA##_###_##AAAAAAA##_#_##AAAAAAAAA##_##AAAAAAAAAAA###AAAAAAAAAAAAA#_______c_______###K###K###K####K#K#K#K#K#K#K##K#K#K#K#K#K#K##K#K#K#K#K#K#K##K#K#K#K#K#K#K##K###K###K###K##KKKKKKKKKKKKK######################################################################################################################################################################";
                break;
            case 43:
                levelContent = "F###<#FaF#<###F#F#<#<FFF<#<#F#P#F#<#F#F#<#F#P#,#F##FPF##F#,#,#a>F#F#F#F>a#,#F##FF#F#FF##F####_###U###_###F###.)&#&).###F##F##.)U).##F##FF#A##.#.##A#FF##A2A##F##A2A###A2#2A###A2#2A#A2#2#2A#A2#2#2A2#2A2#2A2#2A2#2A2A#A2A#A2A#A2A#A#_#A#_#A#_#A#A2#2#2A#A2#2#2A2#2A2#2A2#2A2#2A2A#A2A#A2A#A2A#A#_#A#_#A#_#A#F#<###FaF###<#FF<#<#F#F#F#<#<FF#<#F#P#P#F#<#FF###<#FaF#<###F#F#<#<FFF<#<#F#P#F#<#F#F#<#F#P#,#F##FPF##F#,#,#a>F#F#F#F>a#,##>##FF,FF##>##F#FF##F#F##FF#F#F##FF#F#FF##F####_###U###_###F###.)&#&).###F##F##.)U).##F##FF#A##.#.##A#FF##A2A##F##A2A###A2#2A###A2#2A#A2#2#2A#A2#2#2A2#2A2#2A2#2A2#2A2A#A2A#A2A#A2A#A#_#A#_#A#_#A#F#<###FaF###<#FF<#<#F#F#F#<#<FF#<#F#P#P#F#<#FF##F#,#P#,#F##FF#F>a#,#,#a>F#FFF##>##,##>##FFF##FF#F#F#FF##F#FF##F#F#F##FF####_###U###_###&).###F#F###.)&).##F##U##F##.).##A#FF#FF#A##.##A2A##F##A2A###A2#2A###A2#2A#A2#2#2A#A2#2#2A2#2A2#2A2#2A2#2A2A#A2A#A2A#A2A#A#_#A#_#A#_#A######################################################################################################################################################################";
                break;
            case 44:
                levelContent = "K##`##4#4##`##K#K###4#_#4###K#K#K#4#####4#K#K#K#K#4###4#K#K###K#K#4`4#K#K###4#K#K###K#K#4#4#4#K#K#K#K#4#4###4#K#4#K#4###$#K%K$K`K$K%K#$#$K%K$K`K$K%K$##$K%K$K`K$K%K$#$#K%K$K`K$K%K#$#$K%K$K`K$K%K$##$K%K$K`K$K%K$#$#K%K$KdK$K%K#$#$K%K$K`K$K%K$##$K%K$K`K$K%K$#$#K%K$K`K$K%K#$#$K%K$K`K$K%K$##$K%K$K`K$K%K$#$#K%K$K`K$K%K#$#$K%K$K`K$K%K$################$,$,$,$,$,$,$,$#K#4###4###4#K#K#K#4#4#4#4#K#K#K#K#4###4#K#K#4#K#K##`##K#K#4#4#K#K###K#K#4###4#K#K#K#K#4###4###K#K#K###4#4##`##K#K##`##4#4###K#K#K###4###4#K#K#K#K#4###4#K#K###K#K#4#4#K#K##`##K#K#4#K#K#4###4#K#K#K#K#4#4#4#4#K#K#K#4###4###4#K#K$K%K#$`$#K%K$KK$K%K$#`#$K%K$KK$K%K$#`#$K%K$KK$K%K#$`$#K%K$KK$K%K$#`#$K%K$KK$K%K$#`#$K%K$KK$K%K#$d$#K%K$KK$K%K$#`#$K%K$KK$K%K$#`#$K%K$KK$K%K#$`$#K%K$KK$K%K$#`#$K%K$KK$K%K$#`#$K%K$KK$K%K#$`$#K%K$KK$K%K$#`#$K%K$K###############$,$,$,$,$,$,$,$,$,$,$,$,$,$,$,_______________###############_______________#####################################################################################################################################################################";
                break;
            case 45:
                levelContent = "_#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#_#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#KKKKKKKKKKKKKKK###############KKKKKKKKKKKKKKK################_____________##_###########_##_#KKKKKKKKK#_##_#K#######K#_##_#K#77777#K#_##_#K#7#a#7#K#_##_#K#77777#K#_##_#K#######K#_##_#KKKKKKKKK#_##_###########_##_____________################KKKKKKKKKKKKKKK###############KKKKKKKKKKKKKKK################A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#A#2##7##2##7##2###2##7##2##7##2###2##7##2##7##2#2##7##2##7##2#2##7##2##7##2###2##7##2##7##2###2##7##2##7##2#2##7##2##7##2#2##7##2##7##2###a#a#a#a#a#a#a#_#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#__#_#_#_#_#_#_#_#7#A##7#7##A#7#7#A#A7#7#7A#A#7AA#$#AA$AA#$#AA#K#K#K#K#K#K#K#K#K#K#K#K#K#K#K#####################################################################################################################################################################";
                break;
            case 46:
                levelContent = "_______________2222#A###A#2222#22###AAA###A2#)))))AUAUA))A))))))))AAA))A))))))))))A)))A)))))))))AAA)A))))_#_##AAAAA##_#__#_##AAAAA##_#__#_##A###A##_#__#_##A###A##_#__#_#AA###AA#_#_#A#2222#2222#A#A###A2#A#22###AUA))A))A)))))AUA))A)))A))))))A)))A)))A)))))))A)A))))A))))))AAA##_#_A_#_##AAAA##_#_A_#_##AA#A##_#_#_#_##A##A##_#_#_#_##A##AA#_#_#_#_#AA#))))))AAA)A))))_#_##AAAAA##_#__#_##AAAAA##_#__#_##A###A##_#__#_##A###A##_#__#_#AA###AA#_#_#22####7####22#2222#A###A#2222#22###AAA###A2#)))))AUAUA))A))))))))AAA))A))))))))))A)))A)))####22#7#22#####A#2222#2222#A#A###A2#A#22###AUA))A))A)))))AUA))A)))A))))))A)))A)))A)))))))A)A))))A))))))AAA##_#_A_#_##AAAA##_#_A_#_##AA#A##_#_#_#_##A##A##_#_#_#_##A##AA#_#_#_#_#AA##22####7####22#2222#A###A#2222#22###AAA###A2#)))))AUAUA))A))))))))AAA))A))))))))))A)))A)))))))))AAA)A))))_#_##AAAAA##_#__#_##AAAAA##_#__#_##A###A##_#__#_##A###A##_#__#_#AA###AA#_#_#####################################################################################################################################################################";
                break;
            case 47:
                levelContent = "AAK##_#K#_##KAAKAAK##_-_##KAAKKAAAK_#-#_KAAAK-KAAK#_-_#KAAK-K#KK#_#-#_#KK#K######_K_######_#;##K###K##;#_#;#;##K#K##;#;#_#;#;##K##;#;#_#;#;##UKU##;#;#_#;##KAKAK##;#_#;##KAAKAAK##;#_##KAAK-KAAK##_#;KAAA(-(AAAK;#_#KAAK---KAAK#_#;#KK#(-(#KK#;#_######K######_KAAAK_#-#_KAAAK-KAAK#;-;#KAAK-K#KK#_#-#_#KK#K######;K;######KAAK##;-;##KAAK##;#;#;K;#;#;##U##;#_#K#_#;##UAK##;#;K;#;##KAAAK##_#K#_##KAA_##KAAK-KAAK##_#_KAAAK-KAAAK_#_#KAAK---KAAK#_#_#KK#K-K#KK#_#_######K######_#_##KAAKAAK##_#_#;##K###K##;#_#_#;##K#K##;#_#_#;#;##K##;#;#_#_#;##UKU##;#_#_#;##KAKAK##;#_#K##;#_#_#;##K#K##;#_###_#;##K##;#;#;K;#;#;##U##;#_#K#_#;##UAK##;#;K;#;##KAAAK##_#K#_##KAAKAAK##;-;##KAAKKAAAK_#-#_KAAAK-KAAK#;-;#KAAK-K#KK#_#-#_#KK#K######;K;######_#;##K###K##;#_#;#;##K#K##;#;#_#;#;##K##;#;#_#;#;##UKU##;#;#_#;##KAKAK##;#_#;##KAAKAAK##;#_##KAAK-KAAK##_#;KAAA(-(AAAK;#_#KAAK---KAAK#_#;#KK#(-(#KK#;#_######K######_#####################################################################################################################################################################";
                break;
            case 48:
                levelContent = "##64/#71###64/#71###64/#71###64/#71###64/#71###64/#71###64/#71###64/#71###6#,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?#MFI,>#cMFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,#K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:#41KL##41KL##4171###64/#71###64/#71###64/#71###64/#71###64/#71###64/#71###6#,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?#MFI,>#cMFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,#K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:#41KL##41KL##4171###64/#71###64/#71###64/#71###64/#71###64/#71###64/#71###6#,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?##,?#MFI,>#cMFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,>##MFI,#K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)#5*##K:-)######################################################################################################################################################################";
                break;
            case 49:
                levelContent = "#AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF#/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F#B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4#+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+a#+a#+#a+#a+##(5;)8>##(5;)8>##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF#/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F#B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4#+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+a#+a#+#a+#a+##(5;)8>##(5;)8>##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF+3,-##AF#/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F##/F#B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4D##B)G4#+##+##+##+##+##+##+##+##+##+##+##+##+##+##+##+a#+a#+#a+#a+######################################################################################################################################################################";
                break;
            case 50:
                levelContent = "34#$(+...+($#43344#$(+.+($#44334K4#$(+($#4K4334444#$($#444432222222$22222223#$(+..+..+($#3AAAAAAAAAAAAAAA34#$$$#4#$$$#433#$((($#$((($#33$(+++($(+++($33$(+..+(+..+($3#######f#######4444444A4444444#$$$#43434#$$$#$((($#3#3#$((($(+++($3$3$(+++(+..+($3(3$(+..+..+($#3+3#$(+...+($#43.34#$(+.+($#443.344#$(+($#4K43+34K4#$($#44443(34444#$7777777$7777777#######a#######34#$(+...+($#43344#$(+.+($#44334K4#$(+($#4K4334444#$($#444432222222$22222223#$(+..+..+($#3AAAAAAAAAAAAAAA34#$$$#4#$$$#433#$((($#$((($#33$(+++($(+++($33$(+..+(+..+($3#######f#######4444444A4444444#$$$#43434#$$$#$((($#3#3#$((($(+++($3$3$(+++(+..+($3(3$(+..+..+($#3+3#$(+...+($#43.34#$(+.+($#443.344#$(+($#4K43+34K4#$($#44443(34444#$AAAAAAA$AAAAAAA#######a#######4444444A444444434#$$$#4#$$$#433#$((($#$((($#33$(+++($(+++($33$(+..+(+..+($33#$(+..+..+($#334#$(+...+($#43344#$(+.+($#44334K4#$(+($#4K4334444#$($#444434444444$4444444#####################################################################################################################################################################";
                break;
            case 51:
                levelContent = "#3D@G+/f#3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D#H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;2#8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(#49##49##49##49##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D#H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##;23F?Kf#;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;2#8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(#49##49##49##49##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D@G+/##3D#H?+##H?+##H?+##H?+##H?+##H?+##H?+##Hf+##H?+##H?+##H?+##H?+##H?+##H?+##H?+##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;23F?K##;2#8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.;B2##8(.#####################################################################################################################################################################";
                break;
            case 52:
                levelContent = "#U-%#$-U-$#%-U##U-%#$-U-$#%-U#_U-%#$-U-$#%-U_#U-%#$-U-$#%-U##6-%#$-6-$#%-6##.=9####.=#5###.=95###.=9####.=95###.=95###.=95###.=95###.=95####=95###.=95#*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##MDF#?##MD#K?##MDFK###MDF#?##MDFK?##MDFK###MDFK?##MDFK?##MDFK?###DFK?##MDFK#4+##4+##4###4+#U#####U#####U##U#####U#####U##U#####U#####U##U#####U#####U##.=9####.=#5###.=95###.=9####.=95###.=95###.=95###.=95###.=95####=95###.=95#*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##*0H##MDF#?##MD#K?##MDFK###MDF#?##MDFK?##MDFK###MDFK?##MDFK?##MDFK?###DFK?##MDFK#4+##4+##4###4+#U#####U#####U##U#####U#####U##U#####U#####U##U#####U#####U##.=9####.=#5###.=95###.=9####.=95###.=95###.=95###.=95###.=95####=95###.=95#*0H##*0H##*0H##*0Hb#*0H#b*0H##*0Hb#*0H#b*0H##*0Hb#*0H#b*0H##*0H##*0H##*0H##MDF#?##MD#K?##MDFK###MDF#?##MDFK?##MDFK###MDFK?##MDFK?##MDF#####################################################################################################################################################################";
                break;
            case 53:
                levelContent = ")A%#%A)#)A%#%A)A%#)#%A#A%#)#%A%#7A7#%#%#7A7#%#)K_K)###)K_K)#%#7A7#%#%#7A7#%A%#)#%A#A%#)#%A)A%#%A)#)A%#%A)%A)#)A%#%A)#)A%#%A)A%###%A)A%#7#%A%#7#7#%A%#7A)#_#)A#A)#_#)A7#%A%#7#7#%A%#7#%A)A%###%A)A%#%A)#)A%#%A)#)A%%#7A7#%#%#7A7#%A%#)#%A#A%#)#%A)A%#%A)#)A%#%A)#)A_A)###)A_A)#)A%#%A)#)A%#%A)A%#)#%A#A%#)#%A%#7A7#%#%#7A7#%7#%A%#7#7#%A%#7#%A)A%###%A)A%#%A)#)A%#%A)#)A%A)#_#)A#A)#_#)A%A)#)A%#%A)#)A%#%A)A%###%A)A%#7#%A%#7#7#%A%#77777777#7777777###############7777777#7777777)A%#%A)#)A%#%A)A%#)#%A#A%#)#%A%#7A7#%#%#7A7#%#)K_K)###)K_K)#%#7A7#%#%#7A7#%A%#)#%A#A%#)#%A)A%#%A)#)A%#%A)%A)#)A%#%A)#)A%#%A)A%###%A)A%#7#%A%#7#7#%A%#7A)#_#)A#A)#_#)A7#%A%#7#7#%A%#7#%A)A%###%A)A%#%A)#)A%#%A)#)A%%#7A7#%#%#7A7#%A%#)#%A#A%#)#%A)A%#%A)#)A%#%A)#)A_A)###)A_A)#)A%#%A)#)A%#%A)A%#)#%A#A%#)#%A%#7A7#%#%#7A7#%7#%A%#7#7#%A%#7#%A)A%#b#%A)A%#%A)#)A%b%A)#)A%A)#_#)AbA)#_#)A%A)#)A%b%A)#)A%#%A)A%#b#%A)A%#7#%A%#7b7#%A%#7#######b#############################b##############b##############b##############b##############b##############b##############b#####################################";
                break;
            case 54:
                levelContent = "7#7-7#7-7#7-7#77#777#777#777#7##aa###a##aa####AAA#AAA#AAA#AA7#777#777#777#77#7-7#7-7#7-7#77#777#777#777#7##aa##aa##aa####AAA#AAA#AAA#AA#A#A#A#A#A#A#A##AAA#AAA#AAA#AA#aa##aa##aa##a#KKK#KKK#KKK#KKKK#K#K#K#K#K#K#KKKK#KKK#KKK#KKK7#777#777#777#77#7-7#7-7#7-7#77#777#777#777#7##aa##aa##aa####AAA#AAA#AAA#AA#A#A#A#A#A#A#A##AAA#AAA#AAA#AA#aa##aa##aa##a#KKK#KKK#KKK#KKKK#K#K#K#K#K#K#KKKK#KKK#KKK#KKK#A#A#A#A#A#A#A##AAA#AAA#AAA#AA#aa##a###aa##a#KKK#KKK#KKK#KKKK#K#K#K#K#K#K#KKKK#KKK#KKK#KKK7#777#777#777#77#7-7#7-7#7-7#77#777#777#777#7##aa###a##aa####AAA#AAA#AAA#AA7#777#777#777#77#7-7#7-7#7-7#77#777#777#777#7##aa##aa##aa####AAA#AAA#AAA#AA#A#A#A#A#A#A#A##AAA#AAA#AAA#AA#aa##aa##aa##a#KKK#KKK#KKK#KKKK#K#K#K#K#K#K#KKKK#KKK#KKK#KKK7#777#777#777#77#7-7#7-7#7-7#77#777#777#777#7##aa##aa##aa####AAA#AAA#AAA#AA#A#A#A#A#A#A#A##AAA#AAA#AAA#AA#aa##aa##aa##a#KKK#KKK#KKK#KKKK#K#K#K#K#K#K#KKKK#KKK#KKK#KKK#####################################################################################################################################################################";
                break;
            case 55:
                levelContent = "&$&Z&$&Z&$&Z&$&Z$Z#Z$Z#Z$Z#Z$Z&$&Z&$&Z&$&Z&$&#a###a###a###a#%$%U%$%U%$%U%$%U$U#U$U#U$U#U$U%$%U%$%U%$%U%$%#a###a###a###a#$$$K$$$K$$$K$$$K#K$K$K#K$K#K$K$$$K$$$K$$$K$$$#a###a###a###a#'$'_'$'_%$%U%$%_$_#_$_#U$U#U$U'$'_'$'_%$%U%$%#a###a###a###a#&$&Z&$&Z$$$K$$$Z$Z#Z$Z#K#K$K$K&$&Z&$&Z$$$K$$$#a###a###a###a#%$%U%$%U'$'_'$'U$U#U$U#_$_#_$_%$%U%$%U'$'_'$'#a###a###a###a#$$$K$$$K&$&Z&$&K$K#K$K#Z$Z#Z$Z$$$K$$$K&$&Z&$&#a###a###a###a#'$'_'$'_'$'_'$'_$_#_$_#_$_#_$_'$'_'$'_'$'_'$'#a###a###a###a#&$&Z&$&Z&$&Z&$&Z$Z#Z$Z#Z$Z#Z$Z&$&Z&$&Z&$&Z&$&#a###a###a###a#%$%U%$%U%$%U%$%U$U#U$U#U$U#U$U%$%U%$%U%$%U%$%#a###a###a###a#$$$K$$$K$$$K$$$K$K#K$K#K#K$K$K$$$K$$$K$$$K$$$#a###a###a###a#'$'_'$'_'$'_'$'_$_#_$_#_$_#_$_'$'_'$'_'$'_'$'#a###a###a###a#&$&Z&$&Z&$&Z&$&Z$Z#Z$Z#Z$Z#Z$Z&$&Z&$&Z&$&Z&$&#a###a###a###a#%$%U%$%U%$%U%$%U$U#U$U#U$U#U$U%$%U%$%U%$%U%$%#a###a###a###a#$$$K$$$K$$$K$$$K#K$K$K#K$K#K$K$$$K$$$K$$$K$$$#####################################################################################################################################################################";
                break;
            case 56:
                levelContent = "A;7###___###7;A;a##__$$$__##a;7##_$$$$$$$_##7##_$$$$K$$$$_####_$$KK%KK$$_###_$$K'-7-'K$$_##_$$K'-7-'K$$_###_$$KK%KK$$_####_$$$$K$$$$_##7##_$$$$$$$_##7;a##__$$$__##a;A;7###___###7;AKK$$_##%##_$$KK$$$$_##K##_$$$$$$$_##7$7##_$$$$__##a;$;a##__$_###7;A_A;7###__###7;A_A;7###_$__##a;$;a##__$$$$_##7$7##_$$$$$$$_##K##_$$$$KK$$_##%##_$$KK-'K$$_#7#_$$K'-#_$$K'-7-'K$$_###_$$KK%KK$$_####_$$$$K$$$$_##7##_$$$$$$$_##7;a##__$$$__##a;A;7###___###7;AA;7###___###7;A;a##__$$$__##a;7##_$$$$$$$_##7##_$$$$K$$$$_####_$$KK%KK$$_###_$$K'-7-'K$$_#_###7;A_A;7###_$__##a;$;a##__$$$$_##7$7##_$$$$$$$_##K##_$$$$KK$$_##%##_$$KK-'K$$_#7#_$$K'--'K$$_#7#_$$K'-KK$$_##%##_$$KK$$$$_##K##_$$$$$$$_##7$7##_$$$$__##a;$;a##__$_###7;A_A;7###_A;7###___###7;A;a##__$$$__##a;7##_$$$$$$$_##7##_$$$$K$$$$_####_$$KK%KK$$_###_$$K'-7-'K$$_##_$$K'-7-'K$$_###_$$KK%KK$$_####_$$$$K$$$$_##7##_$$$$$$$_##7;a##__$$$__##a;A;7###___###7;A#####################################################################################################################################################################";
                break;
            case 57:
                levelContent = "==#=========#==A#AAAA#A#AAAA#A#EEEEE#E#EEEEE#IIIIIII#IIIIIIIE####EEEEE####EAA#AAAAAAAAA#AA##=#==###==#=##9999999#9999999###'##%#%##'###9999999#9999999=======#=======AAA;AAA#AAA;AAA###EEEE#EEEE###II#I;I;I;I;I#IIEE#EEEEEEEEE#EEAA#AAA###AAA#AA==#==#=#=#==#==9999#99#99#9999#99#9999999#999==##=======##==A#A#AA#A#AA#A#A#EE#EE#E#EE#EE#III#III#III#IIIE####EEEEE####EAA#/AAAAAAA/#AA##=#==###==#=##9999999#9999999EEEE#EEEEE#EEEE#AAA#AA#AA#AAA#=#==#==#==#==#=99#9999#9999#99999#9999999#999====#=====#====#AAAA#AAA#AAAA##EEEEE#E#EEEEE#IIIIIII#IIIIIIIEE####EEE####EEAAAA#AAAAA#AAAA#==#=#####=#==#9999999#9999999%##'#######'##%9999999#9999999=======#=======AAA;AAA#AAA;AAAEEEE#######EEEE;I;I#IIIII#I;I;EEEE#EEEEE#EEEE#AAA#AA#AA#AAA#=#==#==#==#==#=99#9999#9999#99999#9999#99#999===##=====##===#AA#A#AAA#A#AA##EE#EE#E#EE#EE#III#III#III#IIIEE####EEE####EEAAA/#AAAAA#/AAA#==#=#####=#==#9999999#9999999#####################################################################################################################################################################";
                break;
            case 58:
                levelContent = "######_#_######88#5#&#8#&#5#88/#&5;#M/M#;5&#/&;#5#&#_#&#5#;&/&#5/#/S/#/5#&/;#&5#&#_#&#5&#;_##5S#M_M#S5##_#&#5#&#&#&#5#&###G5;#_&_#;5G##5#&5#&#5#&#5&#5A&#5##MAM##5#&A555555###55555588#5#&#8#&#5#88######_#_######88#5#&#8#&#5#88/#&5;#M/M#;5&#/&;#5#&#_#&#5#;&/&#5/#/S/#/5#&/;#&5#&#_#&#5&#;_##5S#M_M#S5##_#&#5#&#&#&#5#&###G5;#_&_#;5G##5#&5#&#5#&#5&#5A&#5##MAM##5#&A555555###55555588#5#&#8#&#5#88/#&5;#M/M#;5&#/&;#5#&###&#5#;&/&#5/#/S/#/5#&/;#&5#&#_#&#5&#;_##5S#M_M#S5##_#&#5#&#&#&#5#&###G5;#_&_#;5G##5#&5#&#5#&#5&#5A&#5##MAM##5#&A555555###555555_#############_#&#5#88888#5#&#M#;5&#///#&5;#M#&#5#;&_&;#5#&#/#/5#&/S/&#5/#/#&#5&#;_;#&5#&#M#S5##___##5S#M#&#5#&#&#&#5#&#_#;5G##&##G5;#_#&#5&#555#&5#&#M##5#&AAA&#5##M#555555#555555##&#5#88888#5#&#M#;5&#///#&5;#M#&#5#;&#&;#5#&#/#/5#&/S/&#5/#/#&#5&#;_;#&5#&#M#S5##___##5S#M#&#5#&#&#&#5#&#_#;5G##&##G5;#_#&#5&#555#&5#&#M##5#&AAA&#5##M#555555#555555######################################################################################################################################################################";
                break;
            case 59:
                levelContent = "###A###Q###A###DD<<<<#_#<<<<DDL#FFF#)#)#FFF#L#<#A###f###A#<#DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L_##A###Q###A##_DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L###A###Q###A###DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L###A###Q###A###DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L###A###Q###A###DD<<<<#_#<<<<DDL#FFF#)#)#FFF#L#<#A###f###A#<#DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L_##A###Q###A##_DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L###A###Q###A###DD<<<<#L#<<<<DDL#FFF#)L)#FFF#L###A###Q###A###DD<<<<#L#<<<<DD)#FFF#LLL#FFF#)###A###Q###A####<<<<DD_DD<<<<#)#FFF#L#L#FFF#)###A#<#f#<#A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A##_Q_##A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A###Q###A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A###Q###A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A###Q###A####<<<<DD_DD<<<<#)#FFF#L#L#FFF#)###A#<#f#<#A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A##_Q_##A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A###Q###A####<<<<DDLDD<<<<#)#FFF#LLL#FFF#)###A###Q###A####<<<<DDLDD<<<<######################################################################################################################################################################";
                break;
            case 60:
                levelContent = "%%%%%%UUU%%%%%%CCCCCCCCCCCCCCC####9#9%9#9####9#9##9%%%9##9#9#99#9%%C%%9#99####99%%C%%99#####9%%9%%%9%%9###9%%%%9%9%%%%9##M%%%%#9#%%%%M##M#99##<##99#M##M9AA9#<#9AA9M##M9AA9#<#9AA9M##M#99##<##99#M#CC#####<#####CCCCKKKKK`KKKKKCCCC----UUU----CCAAAAAAUUUAAAAAA%%%%%%UUU%%%%%%CCCCCCCCCCCCCCC#(#(#(#(#(#(#(#<#<#<#<#<#<#<#<9#9####%####9#9%9##9#9%9#9##9%%%9#99#C#99#9%%%%99###C###99%%%9%%9##%##9%%9%9%%%%9#%#9%%%%9#%%%%M#9#M%%%%###99#M#<#M#99###9AA9M#<#M9AA9##9AA9M#<#M9AA9###99#M#<#M#99#######CC<CC#####KKKKKCC_CCKKKKKU----CCUCC----UUAAAAAAUAAAAAAUU%%%%%%U%%%%%%UCCCCCCCCCCCCCCC#(#(#(#(#(#(#(#<#<#<#<#<#<#<#<####9#9%9#9####9#9##9%%%9##9#9#99#9%%C%%9#99####99%%C%%99#####9%%9%%%9%%9###9%%%%9%9%%%%9##M%%%%#9#%%%%M##M#99##<##99#M##M9AA9#<#9AA9M##M9AA9#<#9AA9M##M#99##<##99#M#CC#####<#####CCCCKKKKK<KKKKKCCCC----UUU----CCAAAAAAUUUAAAAAA%%%%%%UUU%%%%%%CCCCCCCCCCCCCCC#(#(#(#(#(#(#(#<#<#<#<#<#<#<#<#####################################################################################################################################################################";
                break;
            case 61:
                levelContent = "G_G_G_G_G_G_G_G&&&&&&&A&&&&&&&5555##A)A##5555555##A)))A##55555##A))Y))A##555##A))Y2Y))A##5##A))Y2P2Y))A###A))Y2P&P2Y))A#A))Y2P&_&P2Y))A#A))Y2P&P2Y))A###A))Y2P2Y))A##5##A))Y2Y))A##555##A))Y))A##55555##A)))A##5555555##A)A##5555&&&&&##A##&&&&&///////////////,,,,,,,,,,,,,,,)))))))))))))))&&&&&&&&&&&&&&&______________########&########______________#######&#######______________########&#######_S_S_S_S_S_S_S_#######&#######G_G_G_G_G_G_G_G&&&&&&&A&&&&&&&5555##A)A##5555555##A)))A##55555##A))Y))A##555##A))Y2Y))A##5##A))Y2P2Y))A###A))Y2P&P2Y))A#A))Y2P&_&P2Y))A#A))Y2P&P2Y))A###A))Y2P2Y))A##5##A))Y2Y))A##555##A))Y))A##55555##A)))A##5555555##A)A##5555&&&&&##A##&&&&&///////////////,,,,,,,,,,,,,,,)))))))))))))))&&&&&&&&&&&&&&&#8;>ADGJMPSVY\\_###############______________#################______________###############______________#################______________###############______________######################################################################################################################################################################";
                break;
            case 62:
                levelContent = ">#>#>>>#>>##>>>>#>#>###>#>#>##>>>#>>>#>>##>>>>#>#>###>#>#>##>#>#>>>#>#>#>>>#S#S###S#S#S###JJJ#VVV#VVV#JJJ################MD8&8#_#8&8DM##MD8&&8#8&&8DM#_#MD8&&8&&8DM#_&##MD8&&&8DM##&55_#MD8&8DM#_55&##MD88&88DM##&_#&/8&888&8/&#_#&&/&&8&8&&/&&##&&/&88&88&/&&#PJD>#>>>>>#>DJP###############JJJ#VVV#VVV#JJJ################MD8&8###8&8DM##MD8&&8#8&&8DM#_#MD8&&8&&8DM#_&##MD8&&&8DM##&55_#MD8&8DM#_55&##MD8&&&8DM##&_#MD8&&8&&8DM#_#MD8&&8#8&&8DM##MD8&8###8&8DM#PJD>#>>>>>#>DJP###############JJJ#VVV#VVV#JJJ################MD8&8###8&8DM##MD8&&8#8&&8DM#^#MD8&&8&&8DM#^&##MD8&&&8DM##&55^#MD8&8DM#^55&##MD88&88DM##&^#&/8&888&8/&#^#&&/&&8&8&&/&&##&&/&88&88&/&&#PJD>#>>>>>#>DJP###############JJJ#VVV#VVV#JJJ################MD8&8###8&8DM##MD8&&8#8&&8DM#_#MD8&&8&&8DM#_&##MD8&&&8DM##&55_#MD8&8DM#_55&##MD8&&&8DM##&_#MD8&&8&&8DM#_#MD8&&8#8&&8DM##MD8&8###8&8DM#PJD>#>>>>>#>DJP###############JJJ#VVV#VVV#JJJ#####################################################################################################################################################################";
                break;
            case 63:
                levelContent = "77##=%1=1%=##77;;;##=%%%=##;;;????##=1=##????CCCCC''7''CCCCC71%=''3#3''=%17CCCCC''7''CCCCC????##=1=##????;;;##=%%%=##;;;77##=%1=1%=##77????##=%=##????;;;##=%1%=##;;;77##=%171%=##773''=%17#71%=''377##=%171%=##77;;;##=%1%=##;;;????##=%=##????CCCCC''=''CCCCCU######a######U1%=##77=77##=%1%=##;;;%;;;##=%=##????1????##=''CCCCC7CCCCC''3''=%17#71%=''3''CCCCC7CCCCC''=##????1????##=%=##;;;%;;;##=%1%=##77=77##=%1K######a######KCCCCC''=''CCCCC????##=%=##????;;;##=%1%=##;;;77##=%171%=##773''=%17#71%=''377##=%171%=##77;;;##=%1%=##;;;????##=%=##????CCCCC''=''CCCCCA######a######A''CCCCC=CCCCC''=##????%????##=%=##;;;1;;;##=%1%=##77777##=%171%=''3#3''=%171%=##77777##=%1%=##;;;1;;;##=%=##????%????##=''CCCCC=CCCCC''######7a7######CCCCC''=''CCCCC????##=%=##????;;;##=%1%=##;;;77##=%171%=##773''=%17#71%=''377##=%171%=##77;;;##=%1%=##;;;????##=%=##????CCCCC''=''CCCCC#######'#######_______________#####################################################################################################################################################################";
                break;
            case 64:
                levelContent = "_##;);))););##_8#;);#;);#;);#8_##;##;);##;##_;#;);#828#;);#;););##_2_##;););##;##_2_##;##;_##;##;);##;##_8#;);#;);#;);#8_##;);))););##_8>##;),),);##>8_#;;),&)&,);;#_8;)))))))))));8_#;;),&)&,);;#_8>##;),),);##>8_##;);))););##_8#;);#;);#;);#8_##;##;);##;##_;#;);#828#;);#;););##_2_##;);),);##>828>##;),&,);;#_2_#;;),&)))));828;)))))&,);;#_2_#;;),&,);##>828>##;),););##_2_##;););#;);#828#;);#;;##;##_2_##;##;_##;##;);##;##_8#;);#;);#;);#8_##;);))););##_8>##;),),);##>8_#;;),&)&,);;#_8;)))))))))));8_#;;),&)&,);;#_8>##;),),);##>8_##;);))););##_8#;);#;);#;);#8_##;##;);##;##_;#;);#828#;);#;););##_2_##;);),);##>828>##;),&,);;#_2_#;;),&)))));828;)))))&,);;#_2_#;;),&,);##>828>##;),););##_2_##;););#;);#828#;);#;;##;##_2_##;##;_##;##;);##;##_8#;);#;);#;);#8_##;);))););##_8>##;),),);##>8_#;;),&)&,);;#_8;)))))))))));8_#;;),&)&,);;#_8>##;),),);##>8_##;);))););##_8#;);#;);#;);#8_##;##;);##;##_#####################################################################################################################################################################";
                break;
            case 65:
                levelContent = "3_33_33#33_33_3_33_33#33_33_33_33_33#33_33_333_33_33#33_33_333_33_33#33_33_33_33_33#33_33_3_33_33#33_33_3_33_33#33_33_33_33_33#33_33_333_33_33#33_33_333_33_33#33_33_33_33_33#33_33_3333333#3333333^00^00#00^00^00^00^00#00^00^000^00^00#00^00^000^00^00#00^00^00^00^00#00^00^0^00^00#00^00^0^00^00#00^00^00^00^00#00^00^000^00^00#00^00^0,,W,,W,,#,,W,,W,,W,,W,,#,,W,,W,W,,W,,#,,W,,W,W,,W,,#,,W,,W,,W,,W,,#,,W,,W,,,W,,W,,#,,W,,W,,,W,,W,,#,,W,,W,,W,,W,,#,,W,,W,W,,W,,#,,W,,W,K))K))#))K))K))K))K))#))K))K)))K))K))#))K))K)))K))K))#))K))K))K))K))#))K))K)K))K))#))K))K)K))K))#))K))K))K))K))#))K))K)))K))K))#))K))K)''A''A''#''A''A''A''A''#''A''A'A''A''#''A''A'A''A''#''A''A''A''A''#''A''A'''A''A''#''A''A'''A''A''#''A''A''A''A''#''A''A'A''A''#''A''A'7%%7%%#%%7%%7%%7%%7%%#%%7%%7%%%7%%7%%#%%7%%7%%%7%%7%%#%%7%%7%%7%%7%%#%%7%%7%7%%7%%#%%7%%7%7%%7%%#%%7%%7%%7%%7%%#%%7%%7%%%7%%7%%#%%7%%7%%%7%%7%%#%%7%%7#####################################################################################################################################################################";
                break;
            case 66:
                levelContent = "I-----#I#-----I#UUA##7%7##AUU##UA##-7%7-##AU##A###7%%%7###A##A##7%%K%%7##A##UA##7%%%7##AU##UUA##7%7##AUU#I-----#I#-----IEE1111###1111EE##AUUU###UUUA####-AUU#7#UUA-##7-##AU#%#UA##-7%7###A#%#A###7%%%7##A#K#A##7%%%7##AU#%#UA##7%7##AUU#%#UUA##7#5555557555555#%)%)%)%)%)%)%)%#9#9#9#9#9#9#9#UUUUUUKUKUUUUUU#-----III-----##1111EE#EE1111##UUUA#####AUUU##UUA-##7##-AUU##UA##-7%7-##AU##A###7%%%7###A##A##7%%K%%7##A##UA##7%%%7##AU##UUA##7%7##AUU#555555#7#555555%)%)%)%)%)%)%)%#9#9#9#9#9#9#9#KUUUUUUUUUUUUUKI-----#I#-----IEE1111###1111EE##AUUU###UUUA####-AUU#7#UUA-##7-##AU#%#UA##-7%7###A#%#A###7%%%7##A#K#A##7%%%7##AU#%#UA##7%7##AUU#%#UUA##7#5555557555555#%)%)%)%)%)%)%)%#9#9#9#9#9#9#9#UUUUUUKUKUUUUUU#-----III-----##1111EE#EE1111##UUUA#####AUUU##UUA-##7##-AUU##UA##-7%7-##AU##A###7%%%7###A##A##7%%K%%7##A##UA##7%%%7##AU##UUA##7%7##AUU#555555#7#555555%)%)%)%)%)%)%)%#9#9#9#9#9#9#9#KUUUUUUUUUUUUUK#####################################################################################################################################################################";
                break;
            case 67:
                levelContent = "=-=-=======-=-=5-5-5#####5-5-5%%%%%%%_%%%%%%%KKK%KKKKKKK%KKK7%%%%%K#K%%%%%7%%%%%%%_%%%%%%%5-5-5#####5-5-5=-=-=======-=-=//=++++/++++=//KKK%KKKKKKK%KKK%%%%%%%_%%%%%%%5-5-5#####5-5-5=-=-=======-=-=//=++++/++++=//dKK%KKKKKKK%KKd%%%%%%%_%%%%%%%KKK%KKKKKKK%KKK7%%%%%K#K%%%%%7_%___%K#K%___%__%%%_%%#%%_%%%____%___#___%___%%_%%%%#%%%%_%%777/777/777/777##5-5-5#5-5-5##===-=-===-=-===++++=/////=++++KKK%KKKKKKK%KKK%%%%%%%_%%%%%%%KKK%KKKKKKK%KKKK%%%%%7#7%%%%%KK%___%_#_%___%K%%_%%%_#_%%%_%%___%___c___%___%%%%_%%#%%_%%%%KKK/KKK/KKK/KKK5-5-5#####5-5-5=-=-=======-=-=//=++++/++++=//KKK%KKKKKKK%KKK%%%%%%%_%%%%%%%KKK%KKKKKKK%KKK7%%%%%K#K%%%%%7_%___%K#K%___%__%%%_%%#%%_%%%____%___#___%___%%_%%%%#%%%%_%%KKK/KKK/KKK/KKK##5-5-5#5-5-5##===-=-===-=-===++++=/////=++++KKK%KKKKKKK%KKK%%%%%%%_%%%%%%%KKK%KKKKKKK%KKKK%%%%%7#7%%%%%KK%___%_#_%___%K%%_%%%_#_%%%_%%___%___#___%___%%%%_%%#%%_%%%%___K_______K___#####################################################################################################################################################################";
                break;
            case 68:
                levelContent = "%___7##c##7___%7##9#1#+#1#9##7%7#9#1#%#1#9#7%%'79#1#7#1#97'%%+'7#1#+#1#7'+%%___7##c##7___%%++++7+7+7++++%%+++++7+7+++++%%U-U-U-_-U-U-U%%%%%%%%7%%%%%%%%'79#1#7#1#97'%%+'7#1#+#1#7'+%%___7##c##7___%%++++7+7+7++++%%+++++7+7+++++%%U-U-U-_-U-U-U%%%%%%%%7%%%%%%%7##9#1###1#9##7%7#9#1###1#9#7%%'79#1###1#97'%%+'7#1###1#7'+%%___7#####7___%%++++7+#+7++++%%+++++7#7+++++%%U-U-U-#-U-U-U%%%%%%%%#%%%%%%%-U-U-U-#-U-U-U-###############7##9#1#+#1#9##7%7#9#1#%#1#9#7%%'79#1#7#1#97'%%+'7#1#+#1#7'+%%___7##c##7___%%++++7+7+7++++%%+++++7+7+++++%%U-U-U-_-U-U-U%%%%%%%%7%%%%%%%-U-U-U-U-U-U-U-7##9#1###1#9##7%7#9#1###1#9#7%%'79#1###1#97'%%+'7#1###1#7'+%%___7#####7___%%++++7+#+7++++%%+++++7#7+++++%%U-U-U-#-U-U-U%%%%%%%%#%%%%%%%-U-U-U-#-U-U-U-###############7##9#1#+#1#9##7%7#9#1#%#1#9#7%%'79#1#7#1#97'%%+'7#1#+#1#7'+%%___7##c##7___%%++++7+7+7++++%%+++++7+7+++++%%U-U-U-_-U-U-U%%%%%%%%7%%%%%%%-U-U-U-U-U-U-U-#####################################################################################################################################################################";
                break;
            case 69:
                levelContent = "#<<<<<#K#<<<<<##<---<#K#<---<##<-G-<#K#<-G-<##<-G-<#U#<-G-<#_______##______#######_#######______#f_______#######U#######UUUUUUU#UUUUUUU#####3###3###########3#3#######G##GG#3#GG##G#G#GG##3)3##GG#GQQQQ#3#3#3#QQQQ______##_______#######U#######UUUUUUU#UUUUUUU#####3###3###########3#3#######G##GG#3#GG##G#G#GG##3)3##GG#GQQQQ#3#3#3#QQQQ#######K########<<<<<#K#<<<<<##<---<#K#<---<##<-G-<#K#<-G-<##<-G-<#K#<-G-<##GG##G#)#G##GG#3##GG#G3G#GG##3#3#QQQQKQQQQ#3#f######K######f#<<<<<#K#<<<<<##<---<#K#<---<##<-G-<#K#<-G-<##<-G-<#U#<-G-<#UUUUUUU#UUUUUUU#######_#######_______##______#######_#######______##_______#######U#######UUUUUUU#UUUUUUU#####3###3###########3#3#######G##GG#3#GG##G#G#GG##3)3##GG#GQQQQ#3#3#3#QQQQ#######K########<<<<<#K#<<<<<##<---<#K#<---<##<-G-<#K#<-G-<##<-G-<#K#<-G-<#UUUUUUUUUUUUUUU################______________###############______________################UUUUUUUUUUUUUUU#####################################################################################################################################################################";
                break;
            case 70:
                levelContent = "1A1%7##A##7%1A11A1%_#+#+#_%1A1#1A1%#####%1A1###1A11%_%11A1##1A1%_#+#+#_%1A11A1%7##A##7%1A11A1%_#+#+#_%1A1##7%1A1A1A1%7##f##1AA111AA1##f##1A11%%%11A1###1A1%%%_%%%1A1#1A1%%#####%%1A11A1%_#+#+#_%1A11A1%7##A##7%1A11A1%_#+#+#_%1A1111%%#####%1A1#%%%%%%%_%11A1##5'''''''1AA1##5+5##+++1A11##5+1##%###1###%##1A11##_#A#_##11A1AA1###1###1AA1%11A1#K%K#1A11%%%%1A1#_#1A1%%%##%%1A1#1A1%%##+#_%1A1#1A1%_#+##7%1A1A1A1%7##+#_%1A1#1A1%_#+##%1A1##111%%##%11A1##_%%%%%%%1AA1##5'5''''''A11##5+1+5##+++1###5++A++5###11##5+++A+++5##11##5+++A+++5##1_##5+++_+++5##_#_##5++A++5##_#A#_##5+A+5##_#A#_##5++A++5##_#######111######__##11AAA11##__f##1AA111AA1##f##1A11%%%11A1###1A1%%%_%%%1A1#1A1%%#####%%1A11A1%_#+#+#_%1A11A1%7##A##7%1A11A1%_#+#+#_%1A1111%%#####%1A1#%%%%%%%_%11A1##5'''''''1AA1##5+5##+++1A11##5+++5###1A1###5+++++5##1A1##5++++++5##1A1##5++++++5##___##5+++++5##_#A#_##5+++5##_#AAA#_##5+++5##_#A#_##5++#####################################################################################################################################################################";
                break;
            case 71:
                levelContent = "-A7%1%_%_%1%7A--A7#1%_%_%1%7A--A##1%%%%%1##A-#-A#%5#7#5%#A-#+#-A#%-#-%#A-#+++#-AA%;%AA-#+++++#--AAA-##+++K#%UU+%#%+UU%#K#K#%UU+'+UU%#K#'#K#%UU_UU%#K#'_'#K#%U#U%#K#'_#_'#K#%%%#K#'_#%#_'#K#_#K#'_#%_%#_'#K#K#'_#%_%+UU%#K#K#%UU+%+UU%#K#'#K#%UU+UU%#K#'_'#K#%UUU%#K#'_#_'#K#%U%#K#'_#%#_'#K#%#K#'_#%_%#_'#K#K#'_#%_#_%#_'#KA--#+++A+++#--A%%A-#++;++#-AA%-%#A-#+#+#-A#%-#5%#A-#7#-A#%5#%%1##A-%-A##1%%_%1%7A-%-A7%1%__%1%7A-%-A7#1%_%%1##A-%-A##1%%#5%#A-#7#-A#%5#-%#A-#+#+#-A#%-%AA-#++;++#-AA%A-##+++A+++#--A+++#--AAA--#+++++#-AA%;%%A-#+++#-A#%-#-%#A-#+#-A#%5#7#5%#A-#-A##1%%%%%1##A--A7%1%_%_%1%7A--A7#1%_%_%1%7A--A##1%%%%%1##A-#-A#%5#7#5%#A-#+#-A#%-#-%#A-#+++#-AA%;%AA-#+++++#--AAA-##+++K#%UU+%#%+UU%#K#K#%UU+'+UU%#K#'#K#%UU_UU%#K#'_'#K#%U#U%#K#'_#_'#K#%%%#K#'_#%#_'#K#_#K#'_#%_%#_'#K#K#'_#%_%+UU%#K#K#%UU+%+UU%#K#'#K#%UU+UU%#K#'_'#K#%UUU%#K#'_#_'#K#%U%#K#'_#%#_'#K#%#K#'_#%_%#_'#K#K#'_#%_#_%#_'#K#####################################################################################################################################################################";
                break;
            case 72:
                levelContent = "##K####U####K##1#%a#-5]5-#a%#1##=%#5]%]5#%=##1#%=#]%f%]#=%#1##==#5]%]5#==##%#1##-5]5-##1#%G##_#-5]5-##_#G1#%A#5]%]5#A%#1K#%/#]%f%]#/%#K/#%%#5]%]5#%%#//#/##-5]5-##/#/K#%a#EEEEE#a%#K5#=%#)))))#%=#55#%=#1-K-1#=%#55#==#-_#_-#==#5##1###1=1###1##1##_#5#K#5##_#1##%A#%-=-%#A%##1#%##%%K%%##%#1##%%#%-=-%#%%##%#OG#5#K#5#GO#%G#'5##A=A##5'#G1#a###+K+###a#1K#KK#__c__#KK#K5#a##)))))##a#55##=#-1K1-#=##55#=##_-#-_##=#5##==#1#=#1#==##1##1##5K5##1##1##_##-%=%-#_###1#A%#%%K%%#%A#1###%#-%=%-#%###%#%%##5K5##%%#%G#GO#A#=#A#OG#G1#5'#+#K#+#'5#1K#%a#EEEEE#a%#K5#=%#)))))#%=#55#%=#1-K-1#=%#55#==#-_#_-#==#5##1###1=1###1##1##_#5#K#5##_#1##%A#%-=-%#A%##1#%##%%K%%##%#1##%%#%-=-%#%%##%#OG#5#K#5#GO#%G#'5##A=A##5'#G1#a###+K+###a#1K#KK#__c__#KK#K5#a##)))))##a#55##=#-1K1-#=##55#=##_-#-_##=#5##==b1#=#1b==##1##1b#5K5#b1##1#b_##-%=%-#_#b#1#A%b%%K%%b%A#1###%b-%=%-b%###%#%%##5K5##%%#%G#GO#A#=#A#OG#G1#5'#+#K#+#'5#1#####################################################################################################################################################################";
                break;
            case 73:
                levelContent = "%KKKKKKKKKKKKK%A%%%%%#a#%%%%%A7-7-7-#a#-7-7-75%-%-%#a#%-%-%5KaKKKK1K1KKKKaKf%')+-/a/-+)'%f#KKKKKKKKKKKKK########a#######UaUUUU#a#UUUUaU#######a########KKKKKKKKKKKKK##+#+#+#+#+#+#+#KKKKKK%K%KKKKKK#%%%%%AaA%%%%%##-7-7-7a7-7-7-##%-%-%5a5%-%-%#1KKKKKa#aKKKKK1/-+)'%faf%')+-/KKKKKK#K#KKKKKK#######a########UUUUUaaaUUUUU########a#######KKKKKK#K#KKKKKK#+#+#+#+#+#+#+#%KKKKKKKKKKKKK%A%%%%%#a#%%%%%A7-7-7-#a#-7-7-75%-%-%#a#%-%-%5KaKKKK1K1KKKKaKf%')+-/a/-+)'%f#KKKKKKKKKKKKK########a#######UaUUUU#a#UUUUaU#######a########KKKKKKKKKKKKK##+#+#+#+#+#+#+#KKKKKK%K%KKKKKK#%%%%%AaA%%%%%##-7-7-7a7-7-7-##%-%-%5a5%-%-%#1KKKKKa#aKKKKK1/-+)'%faf%')+-/KKKKKK#K#KKKKKK#######a########UUUUUaaaUUUUU########a#######KKKKKK#K#KKKKKK#+#+#+#+#+#+#+#%KKKKKKKKKKKKK%A%%%%%#a#%%%%%A7-7-7-#a#-7-7-75%-%-%#a#%-%-%5KaKKKK1K1KKKKaKf%')+-/a/-+)'%f#KKKKKKKKKKKKK########a#######UaUUUU#a#UUUUaU#######a########KKKKKKKKKKKKK######################################################################################################################################################################";
                break;
            case 74:
                levelContent = "3/3'7##=##7'3/3=3/337#=#733/3==///'7#=#7'///==33337#=#73333=##//'7#=#7'//#################KA)##77777##)AKA)##73/3/37##)A)##7/'/3/'/7##))#73'//=//'37#))#7/3//%//3/7#))#73/3/=/3/37#)##7'3/3=3/3'7##77##)AK7KA)##77/37##)A3A)##73//'/7##)3)##7/'///'37#)=)#73'////3/7#)%)#7/3///3/37#)=)#73/3/3/3'7##=##7'3/3=3/337#=#733/3==///'7#=#7'///==33337#=#73333=##//'7#=#7'//#################KA)##77777##)AKA)##73/3/37##)A)##7/'/3/'/7##))#73'//=//'37#))#7/3//%//3/7#))#73/3/=/3/37#)##7'3/3=3/3'7###733/3===3/337##7'///===///'7##73333===33337##7'//##=##//'7#77##)AK7KA)##77/37##)A3A)##73//'/7##)3)##7/'///'37#)=)#73'////3/7#)%)#7/3///3/37#)=)#73/3/3/3'7##=##7'3/3=3/337#=#733/3==///'7#=#7'///==33337#=#73333=##//'7#=#7'//#################KA)##77777##)AKA)##73/3/37##)A)##7/'/3/'/7##))#73'//=//'37#))#7/3//%//3/7#))#73/3/=/3/37#)##7'3/3=3/3'7###733/3===3/337##7'///===///'7##73333===33337##7'//##=##//'7######################################################################################################################################################################";
                break;
            case 75:
                levelContent = "AA###-----###55###999aaa999###55###--'--###AA''555#-#-#AAA--a#a#a#a#a#a#a#a%U#-AA###55'#U)#%#%--A#5'')#)####%%-Aa5'))#####---A#9#5'''##--AAA#-#-#555''AA###-----###55###999aaa999###55###--'--###AA''555#-#-#AAA--##'''5#9#A---#####))'5aA-%%####)#)''5#A--%#%#)U#'55###AA-#U%#55'#U)#%U#-AA#5'')#)###%#%--A5'))###a###%%-A#5'''##9##---A#-#555''#--AAA#---###55-AA###--a999###a###999a--###AA'55###---#AAA--#''555#-#A---##9##'''5#A-%%###a###))'5A--%#%###)#)''5#AA-#U%#)U#'55#K%%%K%%%K%%%K%%%%K%%%K%%%K%%K%%U#-AA###55'#U)#%#%--A#5'')#)####%%-Aa5'))#####---A#9#5'''##--AAA#-#-#555''AA###-----###55###999aaa999###55###--'--###AA''555#-#-#AAA--##'''5#9#A---#####))'5aA-%%####)#)''5#A--%#%#)U#'55###AA-#U%#55'#U)#%U#-AA#5'')#)###%#%--A5'))###a###%%-A#5'''##9##---A#-#555''#--AAA#---###55-AA###--a999###a###999a--###AA'55###---#AAA--#''555#-#A---##9##'''5#A-%%###a###))'5A--%#%###)#)''5#AA-#U%#)U#'55######################################################################################################################################################################";
                break;
            case 76:
                levelContent = "K5K5K5K5K5K5K5K#'5'5'#_#'5'5'#U#'A'#K_U#'A'#K#'5'5'#d#'5'5'#'5'1'5'_'5'1'5'#E#E#EPEPE#E#E#K#K#K#K#K#K#K#K5'1#1'5K5'1#1'5'5%1'5'_'5%1'5'#'5'5'#_#'5'5'#U#'A'#K_U#'A'#K#'5'5'#d#'5'5'#'5'1'5'_'5'1'5'5'1#1'5#5'1#1'5a##a##a#a##a##a%%%%%%%f%%%%%%%%P7_77KY%P7_77K#'#'#'#'#'#'#'#7P7_77KYK77_7P7%#%#%#%#%#%#%#%_______c_______+%1_1%+_+%1_1%+%+%1%+%_%+%1%+%#%+%+%#_#%+%+%#K#%+%#K_K#%+%#K_%+%+%#d#%+%+%_%+%1%+%_%+%1%+%+%1#1%+#+%1#1%+a%#a#%a#a##a##a%%%%%%%f%%%%%%%K77_7P%Y%P7_77K#+#+#+#+#+#+#+#K77_7P7Y7P7_77KK#K#K#K#K#K#K#K5'1#1'5K5'1#1'5'5%1'5'_'5%1'5'#'5'5'#_#'5'5'#U#'A'#K_U#'A'#K#'5'5'#d#'5'5'#'5'1'5'_'5'1'5'5'1#1'5#5'1#1'5aa#a#######a#aa%%%%%%%f%%%%%%%%P7_77KY%P7_77KY#K#K#K#K#K#K#Y7P7_77KYK77_7P7###############_______c_______+%1#1%+_+%1#1%+%+%1%+%_%+%1%+%#%+%+%#_#%+%+%#K#%+%#K_K#%+%#K#%+%+%#d#%+%+%#%+%1%+%_%+%1%+%+%1#1%+#+%1#1%+%%%%%%%f%%%%%%%K77_7P%Y%P7_77K###############K77_7P7Y7P7_77K#####################################################################################################################################################################";
                break;
            case 77:
                levelContent = "P-###-PaP-###-P-##9##-a-##9##-P-##9-PUP-9##-P-##A##-#-##A##-P-###-PaP-###-P-##9##-a-##9##-P-##9-PUP-9##-P-##U#9---9#U##-#9#-U#9#9#U-#9##9##-U#=#U-##9#9####-U#U-####9AK#KAKA#AKAK#KA###############71##717=717##17%U#U%U-#-U%U#U%U)##U%U5U%U##)U)U#U-U%#%U-U#U)#-#-##A#A##-#-#-P#P-##a##-P#P-#-#-##9a9##-#-#-P#P-9#U#9-P#P-#-#-9#U-U#9-#-#9##9#U-#-U#9##99###U-#=#-U###9#9#U-#####-U#9#KA#AKAK#KAKA#AK###############17#717#=#717#71U%#-U%U#U%U-#%U)U#U%U#5#U%U#U)U)#%U-U#U-U%#)U-##A##-#-##A##-P-###-PaP-###-P-##9##-a-##9##-P-##9-PUP-9##-P-##U#9---9#U##-#9#-U#9#9#U-#9##9##-U#=#U-##9#9####-U#U-####9AK#KAKA#AKAK#KA###############71##717=717##17%U#U%U-#-U%U#U%U)##U%U5U%U##)U)U#U-U%#%U-U#U)#-#-##A#A##-#-#-P#P-##a##-P#P-#-#-##9a9##-#-#-P#P-9#U#9-P#P-#-#-9#U-U#9-#-#9##9#U-#-U#9##99###U-#=#-U###9#9#U-#####-U#9#KA#AKAK#KAKA#AK##b#########b##17b717#=#717b71U%#-U%U#U%U-#%U)UbU%U#5#U%UbU)U)b%U-U#U-U%b)U#####################################################################################################################################################################";
                break;
            case 78:
                levelContent = "#7%--%7%7%--%7##7%P-%7%7%P-%7##K%--%K%K%--%K##K%%%%K%K%%%%K###A%%A#7#A%%A##A#A%%A#7#A%%A#AA##PP#####PP##A'_'__K_#_K__'_'%_%__%_#_%__%_%K_P__P_K_P__P_K#KPKKPK#KPKKPK#a#K##K###K##K#a%#Ka#K#%#K#aK#%'#A##A#'#A##A#'+#A#aA#+#Aa#A#+3#7##7#3#7##7#3;#7a#7#;#7#a7#;C#-##-UCU-##-#CK#%#a%#K#%a#%#K-%%--%%%%%--%%-#7%--%7%7%--%7##7%--%7%7%--%7##K%--%K%K%--%K##K%%%%K%K%%%%K###A%%A#7#A%%A##A#A%%A#7#A%%A#AA##PP#####PP##A'Z'ZZKZ#ZKZZ'Z'%_%__%_#_%__%_%K_%__%_K_%__%_K#KPKKPK#KPKKPK#a#K##K###K##K#a%#Ka#K#%#K#aK#%'#A##A#'#A##A#'+#A#aA#+#Aa#A#+3#7##7#3#7##7#3;#7a#7#;#7#a7#;C#-##-UCU-##-#CK#%#a%#K#%a#%#K-%%-%%-%-%%-%%--%7#7%-%-%7#7%--%7#7%-%-%7#7%--%K#K%-%-%K#K%-%%K#K%%%%%K#K%%%A#a#A%7%A#a#A%%A#A#A%7%A#A#A%P##A##P#P##A##PZKZ'_'_#_'_'ZKZ_%_%_%_#_%_%_%__%_K_%_K_%_K_%_KPK#KPK#KPK#KPK#K###K###K###K##K#%#K#%#K#%#K##A#'#A#'#A#'#A##A#+#A#+#A#+#A##7#3#7#3#7#3#7##7#;#7#;#7#;#7##-#C#-#C#-#C#-##%#K#%#K#%#K#%######################################################################################################################################################################";
                break;
            case 79:
                levelContent = "AAA+##+A+##+AAAAA+##+AAA+##+AAA+##+AAAAA+##+A+##+AAAAAAA+##+AAAAAAAAAAAAAAA7777777A7777777%%1A##_A_##A1%%1%%1A##A##A1%%1#1%%1A#A#A1%%1###1%%1AAA1%%1##_##1%%1A1%%1##__##A1%%A%%1A##_##A1%%1A1%%1A###A1%%1#A#1%%1A#A1%%1##A##1%%1A1%%1##_A_##1%%17777777A7777777+##+AAAAAAA+##+A+##+AAAAA+##+AAA+##+AAA+##+AAAAA+##+A+##+AAAAAAAAAAAAAAAAAA%%1A##___##A1%%1%%1A##Z##A1%%1#1%%1A#Z#A1%%1###1%%1AZA1%%1##AAA+##+A+##+AAAAA+##+AAA+##+AAA+##+AAAAA+##+A+##+AAAAAAA+##+AAAAAAAAAAAAAAA7777777A7777777%%1A##_A_##A1%%1%%1A##A##A1%%1#1%%1A#A#A1%%1###1%%1AAA1%%1##_##1%%1A1%%1##__##A1%%A%%1A##_##A1%%1A1%%1A###A1%%1#A#1%%1A#A1%%1##A##1%%1A1%%1##_A_##1%%17777777A7777777+##+AAAAAAA+##+A+##+AAAAA+##+AAA+##+AAA+##+AAAAA+##+A+##+AAAAAAAAAAAAAAAAAA%%1A##___##A1%%1%%1A##Z##A1%%1#1%%1A#Z#A1%%1###1%%1AZA1%%1##_##1%%1Z1%%1##__ZZZZZZeZZZZZZ_AAA+##+Z+##+AAAAA+##+AZA+##+AAA+##+AAZAA+##+A+##+AAAZAAA+##+AAAAAAA_AAAAAAA#####################################################################################################################################################################";
                break;
            case 80:
                levelContent = "B##111170000##B%B##1117000##B%%%B##11700##B%%5>%B##150##B%>55>>%B##5##B%>>5555%%B#5#B%%555##11111B00000##B##1111#0000##B%B##111#000##B%%%B##11500##B%%5>%B##150##B%>55>>%B##5##B%>>5555%%B#7#B%%555###5%%B7B%%5###555%%B#7#B%%5555>>%B##5##B%>>55>%B##151##B%>5%%B##11511##B%%00000##B##111110000##B%B##1111000##B%%%B##11100##B%%5%%B##110##B%>555>%B##1##B%>>555>>%B###B%%5557555%%B#B%%555777555%%B#B%%5557555%%B###B%>>555>>%B##1##B%>555>%B##111##B%%5%%B##11%B##1117000##B%%%B##11700##B%%5>%B##150##B%>55>>%B##5##B%>>5555%%B#5#B%%555##11111B00000##B##1111#0000##B%B##111#000##B%%%B##11500##B%%5>%B##150##B%>55>>%B##5##B%>>5555%%B#7#B%%555###5%%B7B%%5###555%%B#7#B%%5555>>%B##5##B%>>55>%B##151##B%>5%%B##11511##B%%00000##B##111110000##B%B##1111000##B%%%B##11100##B%%5%%B##110##B%>555>%B##1##B%>>555>>%B###B%%5557555%%B#B%%555777555%%B#B%%5557555%%B###B%>>555>>%B##1##B%>555>%B##111##B%%5%%B##11#####################################################################################################################################################################";
                break;
            default:
                levelContent = "###########################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################################G##############################################";
                break;
        }

        // shows interstitial each (timesUntilInterstitial) times
        if (winTimes == timesUntilInterstitial) {
            winTimes = 0;
            interstitial = true;
        }

        enemyHiddenGrid = new int[horizontalNumberOfBlocks][enemyTotalGridHeight];
        int sPos = 0;
        int value;
        highestPossibleNumber = 0;

        // convert (levelContent) chars to game block values
        for (j = 0; j < enemyTotalGridHeight; j ++) {
            for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                value = (levelContent.charAt(sPos) - 35) * 5;
                if (value == 310) {
                    enemyHiddenGrid[i][j] = GRID_MOVABLE_BLOCK;
                } else if (value == 315) {
                    enemyHiddenGrid[i][j] = GRID_IMMOVABLE_BLOCK;
                } else if (value == 320) {
                    enemyHiddenGrid[i][j] = GRID_HORIZ_LASER;
                } else if (value == 325) {
                    enemyHiddenGrid[i][j] = GRID_VERT_LASER;
                } else if (value == 330) {
                    enemyHiddenGrid[i][j] = GRID_MULTI_LASER;
                } else if (value == 335) {
                    enemyHiddenGrid[i][j] = GRID_RANDOM_ANGLE;
                } else {
                    enemyHiddenGrid[i][j] = (levelContent.charAt(sPos) - 35) * 5;
                }

                // calculates highestPossibleNumber
                if (enemyHiddenGrid[i][j] > highestPossibleNumber) {
                    highestPossibleNumber = enemyHiddenGrid[i][j];
                }

                sPos ++;
            }
        }

        shotX = (canvasWidth / 2) - halfShotSize;
        lastShotX = shotX;
        alive = true;
        touchFree = true;
        sliderGrabbed = false;
        slider = "sliderUnpressed";
        danger = false;
        dangerAlreadyTriggered = false;
        starLevel = 0;
        firstStarAchieved = false;
        secondStarAchieved = false;
        thirdStarAchieved= false;

        sliderX = (canvasWidth / 2) - (sliderSize / 2);
        shotArrayX = new double[numberOfShots];
        shotArrayY = new double[numberOfShots];
        shotActive = new boolean[numberOfShots];
        angle = new double[numberOfShots];
        for (i = 0; i < numberOfShots; i ++) {
            shotArrayX[i] = shotX;
            shotArrayY[i] = shotY;
            shotActive[i] = true;
            angle[i] = 270;
        }

        deadDialog = false;
        pauseDialog = false;
        usingBomb = false;
        won = false;
        levelPoints = 0;
        starAnimCounter = 0;

        if (level == 1) {
            drawsHand = true;
            handIncr = handFactor * -1;
            handX = (canvasWidth / 2) - blockSize;
        }

        if (sound != null) {
            sound.play(soundPlim, 1, 1, 0, 0, 1);
        }
        aliveShots = numberOfShots;
        gameState = "game";
    }

    public void shoot() {
        sliderGrabbed = false;
        touchX = -1;
        touchY = -1;
        for (i = 1; i < numberOfShots; i ++) {
            angle[i] = angle[0];
        }
        slider = "sliderUnpressed";
        action = "";
        firstTouchedGround = false;
        arrayTurn = 1;
        lastTime = System.currentTimeMillis();
        lastStarGain = System.currentTimeMillis();
        speedingCycleStart = System.currentTimeMillis();
        speedUpIcon = false;
        horizHit = false;
        vertHit = false;
        multiHit = false;
        distanceAfterShot = initialShotDistance;
        comboRowsAnim = false;
        touchFree = false;
    }

    public void stopButton() {
        touchX = -1;
        touchY = -1;
        allShotsHitGround();
        if (hasWon()) {
            wins();
        } else if (hasEnoughFreeSpace()) {
            currentComboRow = 0;
            event = "comboRow";
            eventChar = (int)Math.floor(Math.random() * 6);
            comboRowsAnim = true;
            lastTime = System.currentTimeMillis();
        } else {
            movesGridDown();
            if (isPlayerAlive()) {
                touchFree = true;
            } else {
                dies();
            }
        }
    }

    public void usesBomb() {
        event = "bomb";
        eventChar = (int)Math.floor(Math.random() * 5);
        touchX = -1;
        touchY = -1;
        usingBomb = true;

        numberOfBombs --;
        editor = sharedPref.edit();
        editor.putInt("numberOfBombs", numberOfBombs);
        if (firstBomb) {
            firstBomb = false;
            editor.putBoolean("firstBomb", firstBomb);
        }
        editor.apply();
        gameBGColor = 0xffffffff;
        gameBGPaint.setColor(gameBGColor);
        timeOfBomb = System.currentTimeMillis();
        if (sound != null) {
            sound.play(soundBomb, 1, 1, 2, 0, 1);
        }

        for (j = enemyTotalGridHeight - 2; j > lastInvisibleRow - 1; j --) {
            for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                if (enemyHiddenGrid[i][j] >= bombDamage) {
                    enemyHiddenGrid[i][j] -= bombDamage;
                } else if (enemyHiddenGrid[i][j] > GRID_EMPTY) {
                    enemyHiddenGrid[i][j] = GRID_EMPTY;
                }
            }
        }
    }

    public boolean isShotAlive(int sh) {
        double relativeX, relativeY, targetShotX, targetShotY, targetShotOnGridX, targetShotOnGridY, lastShotOnGridX, lastShotOnGridY;
        boolean result, topWall = false, leftWall = false, rightWall = false, bottomWall = false;

        if (!shotActive[sh]) {
            return false;
        }

        // target
        targetShotX = shotArrayX[sh] + distanceAfterShot * Math.cos(Math.toRadians(angle[sh]));
        targetShotY = shotArrayY[sh] + distanceAfterShot * Math.sin(Math.toRadians(angle[sh]));

        // translates position to array (enemyHiddenGrid)
        relativeX = targetShotX + halfShotSize;
        relativeY = targetShotY - gameAreaTopY + halfShotSize;
        targetShotOnGridX = (double)horizontalNumberOfBlocks / canvasWidth * relativeX;
        targetShotOnGridY = ((double)gameVisibleRows / gameAreaHeight * relativeY) + lastInvisibleRow;

        // tries to avoid error
        if (targetShotOnGridX < 0) targetShotOnGridX = 0;
        if (targetShotOnGridX > horizontalNumberOfBlocks - 1) targetShotOnGridX = horizontalNumberOfBlocks - 1;
        if (targetShotOnGridY < lastInvisibleRow) targetShotOnGridY = lastInvisibleRow;
        if (targetShotOnGridY > enemyTotalGridHeight - 1) targetShotOnGridY = enemyTotalGridHeight - 1;

        // calculates area of verification (distance)
        double x = targetShotX - shotArrayX[sh];
        double y = targetShotY - shotArrayY[sh];
        int dist = (int) Math.sqrt((x * x) + (y * y));

        for (i = 0; i < dist; i += tempDistance) {
            lastShotX = shotArrayX[sh];
            lastShotY = shotArrayY[sh];

            shotArrayX[sh] = shotArrayX[sh] + tempDistance * Math.cos(Math.toRadians(angle[sh]));
            shotArrayY[sh] = shotArrayY[sh] + tempDistance * Math.sin(Math.toRadians(angle[sh]));
            relativeX = shotArrayX[sh] + halfShotSize;
            relativeY = shotArrayY[sh] - gameAreaTopY + halfShotSize;

            if (relativeX < 0) {
                leftWall = true;
            } else if (relativeX > canvasWidth) {
                rightWall = true;
            }
            if (relativeY < 0) {
                topWall = true;
            } else if (relativeY > gameAreaHeight) {
                bottomWall = true;
            }

            shotOnGridX = (double)horizontalNumberOfBlocks / canvasWidth * relativeX;
            shotOnGridY = ((double)gameVisibleRows / gameAreaHeight * relativeY) + lastInvisibleRow;

            // tries to avoid error
            if (shotOnGridX < 0) shotOnGridX = 0;
            if (shotOnGridX > horizontalNumberOfBlocks -1) shotOnGridX = horizontalNumberOfBlocks - 1;
            if (shotOnGridY < lastInvisibleRow) shotOnGridY = lastInvisibleRow;
            if (shotOnGridY > enemyTotalGridHeight - 1) shotOnGridY = enemyTotalGridHeight - 1;

            if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] > 0 ||
                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK ||
                    leftWall ||
                    rightWall ||
                    topWall ||
                    bottomWall) {
                shotArrayX[sh] = lastShotX;
                shotArrayY[sh] = lastShotY;
                i = dist;
            }
        }

        relativeX = lastShotX + halfShotSize;
        relativeY = lastShotY - gameAreaTopY + halfShotSize;
        lastShotOnGridX = (double)horizontalNumberOfBlocks / canvasWidth * relativeX;
        lastShotOnGridY = ((double)gameVisibleRows / gameAreaHeight * relativeY) + lastInvisibleRow;

        if (bottomWall) {
            aliveShots --;
            shotActive[sh] = false;
            if (firstTouchedGround) {
                shotArrayX[sh] = shotX;
            } else {
                shotX = shotArrayX[sh];
                firstShotOnGroundX = shotArrayX[sh];
                firstTouchedGround = true;
            }
            result = false;
        } else if (leftWall) {
            if (angle[sh] >= 180 && angle[sh] < 270) { // going up
                angle[sh] = 360 - angle[sh] + 180;
            } else if (angle[sh] > 90 && angle[sh] < 180) { // going down
                angle[sh] = 180 - angle[sh];
            }
            if (angle[sh] < 0) {
                angle[sh] += 360;
            } else if (angle[sh] >= 360) {
                angle[sh] -= 360;
            }
            if (shotArrayX[sh] < 0) {
                shotArrayX[sh] = 0;
            } else if (shotArrayX[sh] > canvasWidth - shotSize) {
                shotArrayX[sh] = canvasWidth - shotSize;
            }
            if (shotArrayY[sh] < gameAreaTopY) {
                shotArrayY[sh] = gameAreaTopY;
            }
            result = true;
        } else if (rightWall) {
            if (angle[sh] < 90) { // going down
                angle[sh] = 180 - angle[sh];
            } else if (angle[sh] > 270) { // going up
                angle[sh] = 360 - angle[sh] + 180;
            }
            if (angle[sh] < 0) {
                angle[sh] += 360;
            } else if (angle[sh] >= 360) {
                angle[sh] -= 360;
            }
            if (shotArrayX[sh] < 0) {
                shotArrayX[sh] = 0;
            } else if (shotArrayX[sh] > canvasWidth - shotSize) {
                shotArrayX[sh] = canvasWidth - shotSize;
            }
            if (shotArrayY[sh] < gameAreaTopY) {
                shotArrayY[sh] = gameAreaTopY;
            }
            result = true;
        } else if (topWall) {
            angle[sh] = 360 - angle[sh];
            if (angle[sh] < 0) {
                angle[sh] += 360;
            } else if (angle[sh] >= 360) {
                angle[sh] -= 360;
            }
            if (shotArrayX[sh] < 0) {
                shotArrayX[sh] = 0;
            } else if (shotArrayX[sh] > canvasWidth - shotSize) {
                shotArrayX[sh] = canvasWidth - shotSize;
            }
            if (shotArrayY[sh] < gameAreaTopY) {
                shotArrayY[sh] = gameAreaTopY;
            }
            result = true;
        } else {
            switch (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY]) {
                case GRID_EMPTY:
                case GRID_FADE1:
                case GRID_FADE2:
                case GRID_FADE3:
                case GRID_FADE4:
                case GRID_FADE5:
                case GRID_FADE6:
                case GRID_FADE7:
                case GRID_FADE8:
                    result =  true;
                    break;
                case GRID_HORIZ_LASER:
                    for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                        if (enemyHiddenGrid[i][(int) shotOnGridY] > GRID_EMPTY) {
                            enemyHiddenGrid[i][(int) shotOnGridY] --;
                            if (enemyHiddenGrid[i][(int) shotOnGridY] == GRID_EMPTY) {
                                enemyHiddenGrid[i][(int) shotOnGridY] = GRID_FADE1;
                            }
                        }
                    }
                    if (!horizHit) {
                        horizHit = true;
                        horizHitXX = (int)shotOnGridX;
                        horizHitY = ((int)shotOnGridY - lastInvisibleRow) * blockSize + gameAreaTopY;
                    }
                    if (sound != null && System.currentTimeMillis() - lastTime > soundTimeGap) {
                        sound.play(soundShock, 1, 1, 2, 0, 1);
                        lastTime = System.currentTimeMillis();
                    }
                    result = true;
                    break;
                case GRID_VERT_LASER:
                    for (j = 0; j < gameVisibleRows; j ++) {
                        if (enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] > GRID_EMPTY) {
                            enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] --;
                            if (enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] == GRID_EMPTY) {
                                enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] = GRID_FADE1;
                            }
                        }
                    }
                    if (!vertHit) {
                        vertHit = true;
                        vertHitX = (int)(shotOnGridX) * blockSize;
                        vertHitYY = (int)shotOnGridY;
                    }
                    if (sound != null && System.currentTimeMillis() - lastTime > soundTimeGap) {
                        sound.play(soundShock, 1, 1, 2, 0, 1);
                        lastTime = System.currentTimeMillis();
                    }
                    result = true;
                    break;
                case GRID_MULTI_LASER:
                    for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                        if (enemyHiddenGrid[i][(int) shotOnGridY] > GRID_EMPTY) {
                            enemyHiddenGrid[i][(int) shotOnGridY] --;
                            if (enemyHiddenGrid[i][(int) shotOnGridY] == GRID_EMPTY) {
                                enemyHiddenGrid[i][(int) shotOnGridY] = GRID_FADE1;
                            }
                        }
                    }
                    for (j = 0; j < gameVisibleRows; j ++) {
                        if (enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] > GRID_EMPTY) {
                            enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] --;
                            if (enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] == GRID_EMPTY) {
                                enemyHiddenGrid[(int) shotOnGridX][j + lastInvisibleRow] = GRID_FADE1;
                            }
                        }
                    }
                    if (!multiHit) {
                        multiHit = true;
                        multiHitXX = (int)shotOnGridX;
                        multiHitYY = (int)shotOnGridY;
                        multiHitX = (int)(shotOnGridX) * blockSize;
                        multiHitY = ((int)(shotOnGridY) - lastInvisibleRow) * blockSize + gameAreaTopY;
                    }
                    if (sound != null && System.currentTimeMillis() - lastTime > soundTimeGap) {
                        sound.play(soundShock, 1, 1, 2, 0, 1);
                        lastTime = System.currentTimeMillis();
                    }
                    result = true;
                    break;
                case GRID_RANDOM_ANGLE:
                    angle[sh] = Math.random() * 360;
                    result = true;
                    break;
                case GRID_IMMOVABLE_BLOCK:
                case GRID_MOVABLE_BLOCK:
                default:
                    if ((int) lastShotOnGridY == (int) shotOnGridY &&
                            (int) lastShotOnGridX != (int) shotOnGridX) { // sides
                        angle[sh] = 360 + (180 - angle[sh]);
                    } else if ((int) lastShotOnGridX == (int) shotOnGridX &&
                            (int) lastShotOnGridY != (int) shotOnGridY) { // top or bottom
                        angle[sh] = 360 - angle[sh];
                    } else { // diagonals
                        if (angle[sh] > 0 && angle[sh] < 90 && shotOnGridX > 0 && shotOnGridY > lastInvisibleRow + 1) {
                            // hit top left corner of object
                            if ((enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) &&
                                    (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] > 0 ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_MOVABLE_BLOCK ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_IMMOVABLE_BLOCK)) {
                                angle[sh] = angle[sh] - 180; // corner
                            } else if (enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 - angle[sh]; // top or bottom
                            } else if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] > 0 ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_MOVABLE_BLOCK ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 + (180 - angle[sh]); // sides
                            } else {
                                angle[sh] = angle[sh] - 180; // corner
                            }
                        } else if (angle[sh] > 90 && angle[sh] < 180 && shotOnGridX < horizontalNumberOfBlocks - 1 && shotOnGridY > lastInvisibleRow + 1) {
                            // hit top right corner of object
                            if ((enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) &&
                                    (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] > 0 ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_MOVABLE_BLOCK ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_IMMOVABLE_BLOCK)) {
                                angle[sh] = angle[sh] - 180; // corner
                            } else if (enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 - angle[sh]; // top or bottom
                            } else if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY - 1] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 + (180 - angle[sh]); // sides
                            } else {
                                angle[sh] = angle[sh] - 180; // corner
                            }
                        } else if (angle[sh] > 180 && angle[sh] < 270 && shotOnGridX < horizontalNumberOfBlocks - 1 && shotOnGridY < enemyTotalGridHeight - 1) {
                            // hit bottom right corner of object
                            if ((enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) &&
                                    (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] > 0 ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_MOVABLE_BLOCK ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_IMMOVABLE_BLOCK)) {
                                angle[sh] = angle[sh] - 180; // corner
                            } else if (enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX + 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 - angle[sh]; // top or bottom
                            } else if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 + (180 - angle[sh]); // sides
                            } else {
                                angle[sh] = angle[sh] - 180; // corner
                            }
                        } else if (angle[sh] > 270 && angle[sh] < 360 && shotOnGridX > 0 && shotOnGridY < enemyTotalGridHeight - 1) {
                            // hit bottom left corner of object
                            if ((enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) &&
                                    (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] > 0 ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_MOVABLE_BLOCK ||
                                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_IMMOVABLE_BLOCK)) {
                                angle[sh] = angle[sh] - 180; // corner
                            } else if (enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX - 1][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 - angle[sh]; // top or bottom
                            } else if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] > 0 ||
                                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_MOVABLE_BLOCK ||
                                    enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY + 1] == GRID_IMMOVABLE_BLOCK) {
                                angle[sh] = 360 + (180 - angle[sh]); // sides
                            } else {
                                angle[sh] = angle[sh] - 180; // corner
                            }
                        }

                        if (angle[sh] < 0) {
                            angle[sh] += 360;
                        }
                    }

                    if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] == GRID_IMMOVABLE_BLOCK ||
                            enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] == GRID_MOVABLE_BLOCK) {
                        if (sound != null && System.currentTimeMillis() - lastTime > soundTimeGap) {
                            sound.play(soundToc, 1, 1, 0, 0, 1);
                            lastTime = System.currentTimeMillis();
                        }
                    } else if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] > 1) {
                        enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] --;
                        if (sound != null && System.currentTimeMillis() - lastTime > soundTimeGap) {
                            sound.play(soundToc, 1, 1, 0, 0, 1);
                            lastTime = System.currentTimeMillis();
                        }
                    } else if (enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] == 1) {
                        enemyHiddenGrid[(int) shotOnGridX][(int) shotOnGridY] = GRID_FADE1;
                        checkStarGain();
                        if (sound != null) {
                            levelPoints += pointsSingle;
                            sound.play(soundPop, 1, 1, 1, 0, 1);
                            lastTime = System.currentTimeMillis();
                        }
                    }

                    if (angle[sh] < 0) {
                        angle[sh] += 360;
                    } else if (angle[sh] >= 360) {
                        angle[sh] -= 360;
                    }
                    if (shotArrayX[sh] < 0) {
                        shotArrayX[sh] = 0;
                    } else if (shotArrayX[sh] > canvasWidth - shotSize) {
                        shotArrayX[sh] = canvasWidth - shotSize;
                    }
                    if (shotArrayY[sh] < gameAreaTopY) {
                        shotArrayY[sh] = gameAreaTopY;
                    }

                    result = true;
                    break;
            }
        }
        return result;
    }

    public void checkStarGain() {
        if (System.currentTimeMillis() - lastStarGain < starGainTimeGap &&
                starLevel + starIncr < starBGTotalWidth) {
            starLevel += starIncr;
            levelPoints += pointsStarStepIncr;
        }
        lastStarGain = System.currentTimeMillis();
    }

    public void allShotsHitGround() {
        speedUpIcon = false;
        if (firstTouchedGround) {
            lastShotX = firstShotOnGroundX;
            shotArrayX[0] = lastShotX;
        } else {
            lastShotX = shotArrayX[0];
        }

        shotY = gameAreaBottomY - shotSize;
        sliderX = (canvasWidth / 2) - (sliderSize / 2);
        for (i = 0; i < numberOfShots; i ++) {
            shotArrayX[i] = shotArrayX[0];
            shotArrayY[i] = shotY;
            shotActive[i] = true;
        }
        aliveShots = numberOfShots;
    }

    public boolean hasWon() {
        boolean result = true;
        for (j = enemyTotalGridHeight - 1; j > lastInvisibleRow - 1; j --) {
            for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                if (enemyHiddenGrid[i][j] > 0) {
                    result = false;
                }
            }
        }
        return result;
    }

    public boolean hasEnoughFreeSpace() {
        boolean result = true;
        for (j = enemyTotalGridHeight - 1; j > lastInvisibleRow; j --) {
            for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                if (enemyHiddenGrid[i][j] > GRID_EMPTY) {
                    result = false;
                }
            }
        }
        return result;
    }

    public void movesGridDown() {
        // moves hidden grid 1 down
        if (sound != null) {
            sound.play(soundDown, 1, 1, 0, 0, 1);
        }
        for (j = enemyTotalGridHeight - 1; j > 0; j --) {
            for (i = horizontalNumberOfBlocks - 1; i >= 0; i --) {
                if (enemyHiddenGrid[i][j - 1] != GRID_IMMOVABLE_BLOCK &&
                        enemyHiddenGrid[i][j] != GRID_IMMOVABLE_BLOCK) {
                    if (j == 1) {
                        enemyHiddenGrid[i][j] = GRID_EMPTY;
                    } else if (j == enemyTotalGridHeight - 1 && enemyHiddenGrid[i][j - 1] == GRID_MOVABLE_BLOCK) {
                        enemyHiddenGrid[i][j] = GRID_EMPTY;
                    } else {
                        enemyHiddenGrid[i][j] = enemyHiddenGrid[i][j - 1];
                    }
                }
            }
        }
    }

    public boolean isPlayerAlive() {
        boolean result = true;
        for (i = 0; i < horizontalNumberOfBlocks; i ++) {
            if (enemyHiddenGrid[i][enemyTotalGridHeight - 1] > GRID_EMPTY) {
                result = false;
            }
        }
        return result;
    }

    public void wins() {
        if (alive) {
            event = "won";
            eventChar = (int)Math.floor(Math.random() * 11);
            alive = false;
            speedUpIcon = false;
            winTimes ++;
            if (sound != null) {
                sound.play(soundWon, 1, 1, 1, 0, 1);
            }
            if (currentLevel == unlockedLevel) {
                totalPoints += levelPoints;
                unlockedLevel ++;
                levelBombReceived = false;
                centersUnlockedLevel();
                editor = sharedPref.edit();
                editor.putInt("unlockedLevel", unlockedLevel);
                editor.putBoolean("levelBombReceived", levelBombReceived);
                editor.putInt("totalPoints", totalPoints);
                editor.apply();
            }
            Handler handlerWin = new Handler();
            handlerWin.postDelayed(new Runnable() {
                @Override
                public void run() {
                    won = true;
                    currentLevel ++;
                }
            }, 500);
        }
    }

    public void dies() {
        alive = false;
        event = "died";
        eventChar = (int)Math.floor(Math.random() * 10);
        speedUpIcon = false;
        if (sound != null) {
            sound.play(soundLost, 1, 1, 1, 0, 1);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deadDialog = true;
            }
        }, 1000);
    }

    // ui

    public void menuScreen() {
        if (c != null) {
            // background color
            hsv[0] = 360f / jjDiv * jj;
            hsv[1] = 0.2f;
            hsv[2] = 0.95f;
            jj ++;
            if (jj >= jjDiv) {
                jj = 0;
            }
            menuBGColor = Color.HSVToColor(hsv);
            c.drawColor(menuBGColor);

            menuPlayIconX = (menuScreenBlockSize * 9) - (menuScreenBlockStrokeWidth / 2);
            menuPlayIconY = (menuScreenBlockSize * 22) - (menuScreenBlockStrokeWidth / 2);
            // square pattern
            for (j = 0; j <= canvasHeight / menuScreenBlockSize; j ++) {
                for (i = 0; i < 24; i ++) {
                    if (!(i > 19 && i < 23 && j > 0 && j < 4) && // close button area
                            !(i > 8 && i < 15 && j > 25 && j < 32)) { // play button area
                        c.drawRect(i * menuScreenBlockSize, j * menuScreenBlockSize, (i * menuScreenBlockSize) + menuScreenBlockSize, (j * menuScreenBlockSize) + menuScreenBlockSize, menuScreenBlockPaint);
                    }
                }
            }
            // button stroke color
            levelsButtonStrokePaint.setColor(0xffffffff);
            // title
            drawsSprite("menuTitle", menuTitleX, menuTitleY);
            // buttons
            drawsSprite("backIcon", rightIconX, menuScreenBlockSize);
            c.drawRect(rightIconX, menuScreenBlockSize, rightIconX + backIconSize, menuScreenBlockSize + backIconSize, levelsButtonStrokePaint);
            drawsSprite("menuPlayIcon", menuPlayIconX, menuPlayIconY + (4 * menuScreenBlockSize));
            c.drawRect(menuPlayIconX, menuPlayIconY + (4 * menuScreenBlockSize), menuPlayIconX + menuPlayIconSize, menuPlayIconY + menuPlayIconSize + (4 * menuScreenBlockSize), levelsButtonStrokePaint);
        }
    }

    public void levelsScreen() {
        int levelsButtonY;
        hsv[0] = (360f / ((levelsButtonSize * 2) * numberOfLevels) * levelsScreenPositionY) + 40;
        if (hsv[0] > 360) {
            hsv[0] -= 360;
        }
        hsv[1] = 0.3f;
        hsv[2] = 1.0f;
        if (c != null) {
            c.drawColor(Color.HSVToColor(hsv));
            for (j = 0; j <= canvasHeight / menuScreenBlockSize; j ++) { // grid
                for (i = 0; i < 24; i ++) {
                    if (!(i > 19 && i < 23 && j > 0 && j < 4)) {
                        c.drawRect(i * menuScreenBlockSize, j * menuScreenBlockSize, (i * menuScreenBlockSize) + menuScreenBlockSize, (j * menuScreenBlockSize) + menuScreenBlockSize, menuScreenBlockPaint);
                    }
                }
            }
            levelsButtonX = (canvasWidth / 2) - (levelsButtonSize / 2);
            for (i = 1; i < numberOfLevels; i ++) {
                levelsButtonY = canvasHeight - (levelsButtonSize * (i * 2)) + levelsScreenPositionY;
                if (levelsButtonY > 0 - levelsButtonSize && levelsButtonY < canvasHeight + levelsButtonSize) {
                    levelsButtonFillPaint.setColor(Color.HSVToColor(hsv));
                    if (i <= unlockedLevel) {
                        levelsButtonStrokePaint.setColor(0xffffffff);
                        hsv[1] = 1.0f;
                        hsv[2] = 0.5f;
                        levelsButtonTextPaint.setColor(Color.HSVToColor(hsv));
                        hsv[1] = 0.3f;
                        hsv[2] = 1.0f;
                    } else {
                        levelsButtonStrokePaint.setColor(0x80ffffff);
                        hsv[1] = 0.4f;
                        hsv[2] = 0.9f;
                        levelsButtonTextPaint.setColor(Color.HSVToColor(hsv));
                        hsv[1] = 0.3f;
                        hsv[2] = 1.0f;
                    }
                    c.drawRect(levelsButtonX, levelsButtonY, levelsButtonX + levelsButtonSize, levelsButtonY + levelsButtonSize, levelsButtonFillPaint);
                    c.drawRect(levelsButtonX, levelsButtonY, levelsButtonX + levelsButtonSize, levelsButtonY + levelsButtonSize, levelsButtonStrokePaint);
                    if (i < 10) {
                        c.drawText(i + "", levelsButtonX + (levelsButtonSize / 3), levelsButtonY + (levelsButtonSize / 14 * 11), levelsButtonTextPaint);
                    } else {
                        c.drawText(i + "", levelsButtonX + (levelsButtonSize / 11 * 2), levelsButtonY + (levelsButtonSize / 14 * 11), levelsButtonTextPaint);
                    }
                }
            }
            drawsSprite("backIcon", rightIconX, menuScreenBlockSize);
            levelsButtonStrokePaint.setColor(0xffffffff);
            c.drawRect(rightIconX, menuScreenBlockSize, rightIconX + backIconSize, menuScreenBlockSize + backIconSize, levelsButtonStrokePaint);
        }
    }

    public void centersUnlockedLevel() {
        if (unlockedLevel > 2) {
            levelsScreenPositionY = ((unlockedLevel - 3) * 2 * levelsButtonSize) + levelsButtonSize + (levelsButtonSize / 3);
        }
    }

    private void drawsGameBackground() {
        if (c != null) {
            c.drawColor(gameBGColor);
            if (!won && !usingBomb) {
                c.drawText(currentLevel + "", 0, levelTextY, levelTextPaint);
            }
        }
    }

    public void drawsGameTopAndBottomPanel() {
        if (c != null) {
            c.drawRect(0, 0, canvasWidth, gameAreaTopY, panelPaint);
            c.drawRect(0, gameAreaBottomY, canvasWidth, canvasHeight, panelPaint);
        }
    }

    public void drawsStarsBar() {
        j = starBGTotalWidth / starBGBlockWidth;
        
        // this next for should be incorporated inside another for
        for (i = 0; i < j; i ++) {
            drawsSprite("starBGEmpty", starX + (i * starBGBlockWidth), starY - (starBGBlockWidth / 2));
        }

        rectOrigin.set(0, 650, 49, 699);
        rectDestin.set(starX, starY - (starBGBlockWidth / 2), starX + starLevel, starY + (starBGBlockWidth / 2));
        c.drawBitmap(texture, rectOrigin, rectDestin, null);

        if (starLevel == 0) {
            drawsSprite("starEmpty", starX - (starSize / 2), starY - (starSize / 2));
            drawsSprite("starEmpty", (canvasWidth / 2) - (starSize / 2), starY - (starSize / 2));
            drawsSprite("starEmpty", (canvasWidth - starX) - (starSize / 2), starY - (starSize / 2));
        } else if (starLevel < starBGTotalWidth / 2) {
            if (starAnimCounter == 0) {
                drawsSprite("starFull", starX - (starSize / 2), starY - (starSize / 2));
            } else {
                drawsSprite("starFullAnim", starX - (starSize / 2), starY - (starSize / 2));
                if (starAnimCounter > starAnimSize && starAnimIncr > 0) {
                    starAnimIncr = starAnimStep * -1;
                }
                if (starAnimCounter <= 0 && starAnimIncr < 0) {
                    starAnimCounter = 0;
                }
                starAnimCounter += starAnimIncr;
            }
            drawsSprite("starEmpty", (canvasWidth / 2) - (starSize / 2), starY - (starSize / 2));
            drawsSprite("starEmpty", (canvasWidth - starX) - (starSize / 2), starY - (starSize / 2));
            if (!firstStarAchieved) {
                firstStarAchieved = true;
                levelPoints += pointsWholeStarIncr;
                if (sound != null) {
                    sound.play(soundStar, 1, 1, 2, 0, 1);
                }
                starAnimIncr = starAnimStep;
                starAnimCounter += starAnimIncr;
            }
        } else if (starLevel + starIncr < starBGTotalWidth) {
            drawsSprite("starFull", starX - (starSize / 2), starY - (starSize / 2));
            if (starAnimCounter == 0) {
                drawsSprite("starFull", (canvasWidth / 2) - (starSize / 2), starY - (starSize / 2));
            } else {
                drawsSprite("starFullAnim", (canvasWidth / 2) - (starSize / 2), starY - (starSize / 2));
                if (starAnimCounter > starAnimSize && starAnimIncr > 0) {
                    starAnimIncr = starAnimStep * -1;
                }
                if (starAnimCounter <= 0 && starAnimIncr < 0) {
                    starAnimCounter = 0;
                }
                starAnimCounter += starAnimIncr;
            }
            drawsSprite("starEmpty", (canvasWidth - starX) - (starSize / 2), starY - (starSize / 2));
            if (!secondStarAchieved) {
                secondStarAchieved = true;
                levelPoints += pointsWholeStarIncr;
                if (sound != null) {
                    sound.play(soundStar, 1, 1, 2, 0, 1);
                }
                starAnimIncr = starAnimStep;
                starAnimCounter += starAnimIncr;
            }
        } else {
            drawsSprite("starFull", starX - (starSize / 2), starY - (starSize / 2));
            drawsSprite("starFull", (canvasWidth / 2) - (starSize / 2), starY - (starSize / 2));
            if (starAnimCounter == 0) {
                drawsSprite("starFull", (canvasWidth - starX) - (starSize / 2), starY - (starSize / 2));
            } else {
                drawsSprite("starFullAnim", (canvasWidth - starX) - (starSize / 2), starY - (starSize / 2));
                if (starAnimCounter > starAnimSize && starAnimIncr > 0) {
                    starAnimIncr = starAnimStep * -1;
                }
                if (starAnimCounter <= 0 && starAnimIncr < 0) {
                    starAnimCounter = 0;
                }
                starAnimCounter += starAnimIncr;
            }
            if (!thirdStarAchieved) {
                event = "thirdStar";
                eventChar = (int)Math.floor(Math.random() * 11);
                thirdStarAchieved = true;
                levelPoints += pointsWholeStarIncr;
                if (sound != null) {
                    sound.play(soundStar, 1, 1, 2, 0, 1);
                }
                starAnimIncr = starAnimStep;
                starAnimCounter += starAnimIncr;
                if (!levelBombReceived && currentLevel == unlockedLevel) {
                    levelBombReceived = true;
                    levelPoints += pointsThirdStarIncr;
                    numberOfBombs ++;
                    editor = sharedPref.edit();
                    editor.putInt("numberOfBombs", numberOfBombs);
                    editor.putBoolean("levelBombReceived", levelBombReceived);
                    editor.apply();
                }
            }
        }
    }

    public void drawsGrid() {
        if (c != null) {
            danger = false;
            hsv[1] = 0.65f;
            hsv[2] = 1.0f;
            int xx, yy;
            for (j = enemyTotalGridHeight - 1; j > lastInvisibleRow - 1; j --) {
                for (i = 0; i < horizontalNumberOfBlocks; i ++) {
                    xx = i * blockSize;
                    yy = ((j - (lastInvisibleRow)) * blockSize) + gameAreaTopY;
                    if (enemyHiddenGrid[i][j] > GRID_EMPTY) {
                        // draws enemy
                        hsv[0] = 360f / highestPossibleNumber * enemyHiddenGrid[i][j];
                        enemyFillPaint.setColor(Color.HSVToColor(hsv));
                        c.drawRect(xx + enemyBetweenSpaces, yy + enemyBetweenSpaces, xx + blockSize - enemyBetweenSpaces, yy + blockSize - enemyBetweenSpaces, enemyFillPaint);
                        if (enemyHiddenGrid[i][j] < 10) { // 0..9
                            writesText(enemyHiddenGrid[i][j] + "", xx + charWidthA, yy + charHeightA, blockNumberPaint);
                        } else if (enemyHiddenGrid[i][j] < 100) { // 10..99
                            writesText(enemyHiddenGrid[i][j] + "", xx + charWidthB, yy + charHeightA, blockNumberPaint);
                        } else { // 100..999
                            writesText(enemyHiddenGrid[i][j] + "", xx + charWidthC, yy + charHeightA, blockNumberPaint);
                        }
                        if (j == enemyTotalGridHeight - 2) {
                            danger = true;
                        }
                    } else if (enemyHiddenGrid[i][j] == GRID_MOVABLE_BLOCK) {
                        drawsSprite("movable", xx, yy);
                    } else if (enemyHiddenGrid[i][j] == GRID_IMMOVABLE_BLOCK) {
                        drawsSprite("immovable", xx, yy);
                    } else if (enemyHiddenGrid[i][j] == GRID_HORIZ_LASER) {
                        drawsSprite("horizLightningOrigin", xx, yy);
                    } else if (enemyHiddenGrid[i][j] == GRID_VERT_LASER) {
                        drawsSprite("vertLightningOrigin", xx, yy);
                    } else if (enemyHiddenGrid[i][j] == GRID_MULTI_LASER) {
                        drawsSprite("multiLightningOrigin", xx, yy);
                    } else if (enemyHiddenGrid[i][j] == GRID_RANDOM_ANGLE) {
                        drawsSprite("random", xx, yy);
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE1) {
                        drawsSprite("fade1", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE2) {
                        drawsSprite("fade2", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE3) {
                        drawsSprite("fade3", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE4) {
                        drawsSprite("fade4", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE5) {
                        drawsSprite("fade5", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE6) {
                        drawsSprite("fade6", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE7) {
                        drawsSprite("fade7", xx, yy);
                        enemyHiddenGrid[i][j] --;
                    } else if (enemyHiddenGrid[i][j] == GRID_FADE8) {
                        drawsSprite("fade8", xx, yy);
                        enemyHiddenGrid[i][j] = GRID_EMPTY;
                    }
                }
            }
        }
    }

    public void drawsLeftIcon() {
        if (numberOfBombs > 0 && touchFree) {
            drawsSprite("leftIconEnabled", 0, gameAreaBottomY);
            if (numberOfBombs < 10) {
                c.drawText(numberOfBombs + "", (int)(gameAreaBottomSize * 0.433f), (int)(gameAreaBottomY + (gameAreaBottomSize * 0.62f)), bombTextPaint);
            } else if (numberOfBombs < 20) {
                c.drawText(numberOfBombs + "", (int)(gameAreaBottomSize * 0.415f), (int)(gameAreaBottomY + (gameAreaBottomSize * 0.62f)), bombTextPaint);
            } else {
                c.drawText(numberOfBombs + "", (int)(gameAreaBottomSize * 0.398f), (int)(gameAreaBottomY + (gameAreaBottomSize * 0.62f)), bombTextPaint);
            }
        } else {
                drawsSprite("leftIconDisabled", 0, gameAreaBottomY);
        }
    }

    public void drawsRightIcon() {
        if (danger) {
            if(!dangerAlreadyTriggered && sound != null) {
                sound.play(soundDanger, 1, 1, 1, 0, 1);
                dangerAlreadyTriggered = true;
            }
            drawsSprite("rightIconEnabled", canvasWidth - gameAreaBottomSize, gameAreaBottomY);
        } else if (speedUpIcon) {
            drawsSprite("speedIcon", canvasWidth - gameAreaBottomSize, gameAreaBottomY);
        } else {
            if(dangerAlreadyTriggered) {
                dangerAlreadyTriggered = false;
            }
            drawsSprite("rightIconDisabled", canvasWidth - gameAreaBottomSize, gameAreaBottomY);
        }
    }

    public void drawsPauseIcon() {
        if ((comboRowsAnim || touchFree || alive) && !pauseDialog) {
            drawsSprite("pauseIconEnabled", canvasWidth - gameAreaTopY, 0);
        } else {
            drawsSprite("pauseIconDisabled", canvasWidth - gameAreaTopY, 0);
        }
    }

    public void drawsHandOverSlider() {
        handX += handIncr;
        if (handX >= sliderXMaxLimit - blockSize) {
            handIncr = handFactor * -1;
        } else if (handX <= sliderXMinLimit - blockSize) {
            handIncr = handFactor;
        }
        drawsSprite("hand", handX, gameAreaBottomY);
    }

    public void drawsBombHand() {
        handFactor = blockSize / 20;
        handY += handIncr;
        if (handY >= gameAreaBottomY) {
            handIncr = handFactor * -1;
        } else if (handY <= gameAreaBottomY - blockSize) {
            handIncr = handFactor;
        }
        drawsSprite("hand", blockSize, handY);
    }

    public void handlesBomb() {
        if (System.currentTimeMillis() - timeOfBomb >= 2000) {
            gameBGColor = 0xff242329;
            gameBGPaint.setColor(gameBGColor);
            if (hasWon()) {
                wins();
            } else {
                usingBomb = false;
            }
        } else if (System.currentTimeMillis() - timeOfBomb >= 1900) {
            gameBGColor = 0xff3a393f;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1800) {
            gameBGColor = 0xff504f54;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1700) {
            gameBGColor = 0xff66656a;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1600) {
            gameBGColor = 0xff7c7b7f;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1500) {
            gameBGColor = 0xff929194;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1400) {
            gameBGColor = 0xffa7a7a9;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1300) {
            gameBGColor = 0xffbebdbf;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1200) {
            gameBGColor = 0xffd3d3d4;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1100) {
            gameBGColor = 0xffeae9ea;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 1000) {
            gameBGColor = 0xffffffff;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 900) {
            gameBGColor = 0xffff0000;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 800) {
            gameBGColor = 0xffffffff;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 700) {
            gameBGColor = 0xffff0000;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 600) {
            gameBGColor = 0xffffffff;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 500) {
            gameBGColor = 0xffff0000;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 400) {
            gameBGColor = 0xffffffff;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 300) {
            gameBGColor = 0xffff0000;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 200) {
            gameBGColor = 0xffffffff;
            gameBGPaint.setColor(gameBGColor);
        } else if (System.currentTimeMillis() - timeOfBomb >= 100) {
            gameBGColor = 0xffff0000;
            gameBGPaint.setColor(gameBGColor);
        }
    }

    public void drawsPoints() {
        c.drawText(levelPoints + "", (int)(canvasWidth * 0.035f), (int)(canvasHeight * 0.042f), levelPointsPaint);
        c.drawText(totalPoints + "", (int)(canvasWidth * 0.035f), (int)(canvasHeight * 0.077f), totalPointsPaint);
    }

    public void drawsSlider() {
        j = sliderBGWidth / sliderBGHeight;
        for (i = 0; i < j; i ++) {
            if (i == 0) {
                drawsSprite("sliderBackgroundLeft", sliderXMinLimit, sliderBGY);
            } else if (i == j - 1) {
                drawsSprite("sliderBackgroundRight", sliderXMinLimit + (i * sliderBGHeight), sliderBGY);
            } else {
                drawsSprite("sliderBackgroundMiddle", sliderXMinLimit + (i * sliderBGHeight), sliderBGY);
            }
        }
        drawsSprite(slider, (int) sliderX, (int) sliderY);
    }

    public void drawsShotTrail() {
        double trailAngle, tempShotTrailX, tempShotTrailY;
        double shotTrailX = lastShotX;
        double shotTrailY = shotY;
        int trailSize = 15;

        angle[0] = (int) (169f / sliderBGWidth * (sliderX - sliderXMinLimit)) + 220;
        if (angle[0] < 0) {
            angle[0] += 360;
        }

        trailAngle = angle[0];

        for (i = 0; i < trailSize; i ++) {
            if (i == 0) {
                distanceTrail = 0;
            } else {
                distanceTrail = distanceTrailStatic;
            }

            tempShotTrailX = shotTrailX;
            tempShotTrailY = shotTrailY;

            shotTrailX = shotTrailX + distanceTrail * Math.cos(Math.toRadians(trailAngle));
            shotTrailY = shotTrailY + distanceTrail * Math.sin(Math.toRadians(trailAngle));

            // checks if trail hits wall

            if (shotTrailX <= 0 || shotTrailX >= canvasWidth - shotSize) {
                shotTrailX = tempShotTrailX;
                shotTrailY = tempShotTrailY;
                while (shotTrailX > 0 && shotTrailX < canvasWidth - shotSize) {
                    shotTrailX = shotTrailX + tempDistance * Math.cos(Math.toRadians(trailAngle));
                    shotTrailY = shotTrailY + tempDistance * Math.sin(Math.toRadians(trailAngle));
                }

                trailAngle = 360 + (180 - trailAngle);

                if (trailAngle >= 360) {
                    trailAngle -= 360;
                }
                if (trailAngle < 0) {
                    trailAngle += 360;
                }
                shotTrailX = shotTrailX + distanceTrail * Math.cos(Math.toRadians(trailAngle));
                shotTrailY = shotTrailY + distanceTrail * Math.sin(Math.toRadians(trailAngle));
            }

            if (i == 0) {
                drawsSprite("shot", (int) shotTrailX, (int) shotTrailY);
            } else {
                drawsSprite("previewShot", (int) shotTrailX, (int) shotTrailY);
            }
        }
    }

    public void drawsHorizHit() {
        horizHit = false;
        for (int h = 0; h < horizontalNumberOfBlocks; h ++) {
            if (horizHitXX != h) {
                drawsSprite("horizLightning", h * blockSize, horizHitY);
            }
        }

    }

    public void drawsVertHit() {
        vertHit = false;
        for (int v = 0; v < gameVisibleRows; v ++) {
            if (vertHitYY != v + lastInvisibleRow) {
                drawsSprite("vertLightning", vertHitX, (v * blockSize) + gameAreaTopY);
            }
        }
    }

    public void drawsMultiHit() {
        multiHit = false;
        for (int h = 0; h < horizontalNumberOfBlocks; h ++) {
            if (multiHitXX != h) {
                drawsSprite("horizLightning", h * blockSize, multiHitY);
            }
        }
        for (int v = 0; v < gameVisibleRows; v ++) {
            if (multiHitYY != v + lastInvisibleRow) {
                drawsSprite("vertLightning", multiHitX, (v * blockSize) + gameAreaTopY);
            }
        }
    }

    public void drawsWonDialog() {
        c.drawRect(threeButtonDialogX + shadowGap, threeButtonDialogY + shadowGap, threeButtonDialogX + threeButtonDialogWidth + shadowGap, threeButtonDialogY + threeButtonDialogHeight + shadowGap, shadowPaint);
        c.drawRect(threeButtonDialogX, threeButtonDialogY, threeButtonDialogX + threeButtonDialogWidth, threeButtonDialogY + threeButtonDialogHeight, panelPaint);
        c.drawText("Level Points", (int)(canvasWidth * 0.355f), (int)(canvasHeight * 0.345f), winPointsTextPaint);
        if (levelPoints > 999) {
            c.drawText("" + levelPoints, (int)(canvasWidth * 0.449f), (int)(canvasHeight * 0.395f), winPointsTextPaint);
        } else {
            c.drawText("" + levelPoints, (int)(canvasWidth * 0.465f), (int)(canvasHeight * 0.395f), winPointsTextPaint);
        }
        for (int b = 1; b < 3; b ++) {
            c.drawRect(dialogButtonX + shadowGap, dialogButtonY + (dialogButtonGap * b) + shadowGap, dialogButtonX + dialogButtonWidth + shadowGap, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b) + shadowGap, shadowPaint);
            c.drawRect(dialogButtonX, dialogButtonY + (dialogButtonGap * b), dialogButtonX + dialogButtonWidth, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b), dialogButtonFillPaint);
            c.drawRect(dialogButtonX, dialogButtonY + (dialogButtonGap * b), dialogButtonX + dialogButtonWidth, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b), dialogButtonStrokePaint);
            switch (b) {
                case 1:
                    c.drawText("Next level", dialogButtonX + (int)(canvasWidth * 0.12f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
                case 2:
                    c.drawText("Leave game", dialogButtonX + (int)(canvasWidth * 0.12f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
            }
        }
    }

    public void drawsPauseDialog() {
        c.drawRect(threeButtonDialogX + shadowGap, threeButtonDialogY + shadowGap, threeButtonDialogX + threeButtonDialogWidth + shadowGap, threeButtonDialogY + threeButtonDialogHeight + shadowGap, shadowPaint);
        c.drawRect(threeButtonDialogX, threeButtonDialogY, threeButtonDialogX + threeButtonDialogWidth, threeButtonDialogY + threeButtonDialogHeight, panelPaint);
        for (int b = 0; b < 3; b ++) {
            c.drawRect(dialogButtonX + shadowGap, dialogButtonY + (dialogButtonGap * b) + shadowGap, dialogButtonX + dialogButtonWidth + shadowGap, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b) + shadowGap, shadowPaint);
            c.drawRect(dialogButtonX, dialogButtonY + (dialogButtonGap * b), dialogButtonX + dialogButtonWidth, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b), dialogButtonFillPaint);
            c.drawRect(dialogButtonX, dialogButtonY + (dialogButtonGap * b), dialogButtonX + dialogButtonWidth, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b), dialogButtonStrokePaint);
            switch (b) {
                case 0:
                    c.drawText("Restart level", dialogButtonX + (int)(canvasWidth * 0.09f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
                case 1:
                    c.drawText("Continue", dialogButtonX + (int)(canvasWidth * 0.145f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
                case 2:
                    c.drawText("Leave game", dialogButtonX + (int)(canvasWidth * 0.12f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
            }
        }
    }

    public void drawsDeadDialog() {
        c.drawRect(threeButtonDialogX + shadowGap, threeButtonDialogY + shadowGap, threeButtonDialogX + threeButtonDialogWidth + shadowGap, threeButtonDialogY + threeButtonDialogHeight + shadowGap, shadowPaint);
        c.drawRect(threeButtonDialogX, threeButtonDialogY, threeButtonDialogX + threeButtonDialogWidth, threeButtonDialogY + threeButtonDialogHeight, panelPaint);
        for (int b = 0; b < 3; b ++) {
            c.drawRect(dialogButtonX + shadowGap, dialogButtonY + (dialogButtonGap * b) + shadowGap, dialogButtonX + dialogButtonWidth + shadowGap, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b) + shadowGap, shadowPaint);
            c.drawRect(dialogButtonX, dialogButtonY + (dialogButtonGap * b), dialogButtonX + dialogButtonWidth, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b), dialogButtonFillPaint);
            c.drawRect(dialogButtonX, dialogButtonY + (dialogButtonGap * b), dialogButtonX + dialogButtonWidth, (dialogButtonY + dialogButtonHeight) + (dialogButtonGap * b), dialogButtonStrokePaint);
            switch (b) {
                case 0:
                    c.drawText("Try again", dialogButtonX + (int)(canvasWidth * 0.14f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
                case 1:
                    c.drawText("Ad & continue", dialogButtonX + (int)(canvasWidth * 0.1f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
                case 2:
                    c.drawText("Leave game", dialogButtonX + (int)(canvasWidth * 0.125f), dialogButtonY + (int)(canvasHeight * 0.047f) + (dialogButtonGap * b), dialogButtonTextPaint);
                    break;
            }
        }
    }

    public void writesText(String txt, int xx, int yy, Paint pp) {
        if (c != null) {
            int l, m = 0, k = 0;
            for (l = 0; l < txt.length(); l ++) {
                if (l + 1 < txt.length()) {
                    if (txt.substring(l, l + 1).equals("\n")) {
                        m ++;
                        k = 0;
                        l ++;
                    }
                }
                c.drawText(String.valueOf(txt.charAt(l)), xx + (k * fontCharSpace), yy + (m * fontLineSpace), pp);
                k++;
            }
        }
    }

    public void drawsSprite(String sprite, int sx, int sy) {
        if (c != null) {
            switch (sprite) {
                case "shot":
                    rectOrigin.set(100, 650, 149, 699);
                    rectDestin.set(sx, sy, sx + shotSize, sy + shotSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "previewShot":
                    rectOrigin.set(150, 650, 199, 699);
                    rectDestin.set(sx, sy, sx + shotSize, sy + shotSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "sliderPressed":
                    rectOrigin.set(400, 200, 599, 399);
                    rectDestin.set(sx, sy, sx + sliderSize, sy + sliderSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "sliderUnpressed":
                    rectOrigin.set(600, 200, 799, 399);
                    rectDestin.set(sx, sy, sx + sliderSize, sy + sliderSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "sliderBackgroundLeft":
                    rectOrigin.set(0, 600, 49, 649);
                    rectDestin.set(sx, sy, sx + sliderBGHeight, sy + sliderBGHeight);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "sliderBackgroundMiddle":
                    rectOrigin.set(50, 600, 99, 649);
                    rectDestin.set(sx, sy, sx + sliderBGHeight, sy + sliderBGHeight);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "sliderBackgroundRight":
                    rectOrigin.set(100, 600, 149, 649);
                    rectDestin.set(sx, sy, sx + sliderBGHeight, sy + sliderBGHeight);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "leftIconDisabled":
                    rectOrigin.set(0, 200, 199, 399);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "leftIconEnabled":
                    rectOrigin.set(200, 200, 399, 399);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "rightIconDisabled":
                    rectOrigin.set(0, 400, 199, 599);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "rightIconEnabled":
                    rectOrigin.set(200, 400, 399, 599);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "stopIcon":
                    rectOrigin.set(800, 0, 999, 199);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "starFull":
                    rectOrigin.set(600, 600, 699, 699);
                    rectDestin.set(sx, sy, sx + starSize, sy + starSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "starFullAnim":
                    rectOrigin.set(600, 600, 699, 699);
                    rectDestin.set(sx - starAnimCounter, sy - starAnimCounter, sx + starSize + starAnimCounter, sy + starSize + starAnimCounter);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "starEmpty":
                    rectOrigin.set(700, 600, 799, 699);
                    rectDestin.set(sx, sy, sx + starSize, sy + starSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "starBGEmpty":
                    rectOrigin.set(50, 650, 99, 699);
                    rectDestin.set(sx, sy, sx + starBGBlockWidth, sy + starBGBlockWidth);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "menuPlayIcon":
                    rectOrigin.set(600, 0, 799, 199);
                    rectDestin.set(sx, sy, sx + menuPlayIconSize, sy + menuPlayIconSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "menuRankingIcon":
                    rectOrigin.set(800, 600, 999, 799);
                    rectDestin.set(sx, sy, sx + menuPlayIconSize, sy + menuPlayIconSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "menuTitle":
                    rectOrigin.set(0, 0, 597, 199);
                    rectDestin.set(sx, sy, sx + menuTitleWidth, sy + menuTitleHeight);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "pauseIconEnabled":
                    rectOrigin.set(400, 400, 599, 599);
                    rectDestin.set(sx, sy, sx + gameAreaTopY, sy + gameAreaTopY);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "pauseIconDisabled":
                    rectOrigin.set(600, 400, 799, 599);
                    rectDestin.set(sx, sy, sx + gameAreaTopY, sy + gameAreaTopY);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "adsIcon":
                    rectOrigin.set(900, 400, 999, 499);
                    rectDestin.set(sx, sy, sx + backIconSize, sy + backIconSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "backIcon":
                    rectOrigin.set(800, 400, 899, 499);
                    rectDestin.set(sx, sy, sx + backIconSize, sy + backIconSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "speedIcon":
                    rectOrigin.set(800, 200, 999, 399);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "horizLightning":
                    rectOrigin.set(200, 600, 299, 699);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "vertLightning":
                    rectOrigin.set(300, 600, 399, 699);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "horizLightningOrigin":
                    rectOrigin.set(0, 700, 99, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "vertLightningOrigin":
                    rectOrigin.set(100, 700, 199, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "multiLightningOrigin":
                    rectOrigin.set(700, 700, 799, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "movable":
                    rectOrigin.set(400, 600, 499, 699);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "immovable":
                    rectOrigin.set(500, 600, 599, 699);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "random":
                    rectOrigin.set(900, 900, 999, 999);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade1":
                    rectOrigin.set(200, 700, 249, 749);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade2":
                    rectOrigin.set(250, 700, 299, 749);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade3":
                    rectOrigin.set(300, 700, 349, 749);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade4":
                    rectOrigin.set(350, 700, 399, 749);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade5":
                    rectOrigin.set(200, 750, 249, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade6":
                    rectOrigin.set(250, 750, 299, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade7":
                    rectOrigin.set(300, 750, 349, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "fade8":
                    rectOrigin.set(350, 750, 399, 799);
                    rectDestin.set(sx, sy, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "hand":
                    rectOrigin.set(150, 600, 199, 649);
                    rectDestin.set(sx - blockSize, sy - blockSize, sx + blockSize, sy + blockSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                // events
                case "eventComboRow":
                    rectOrigin.set(eventChar * 100, 1000, 99 + (eventChar * 100), 1099);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "eventBomb":
                    rectOrigin.set(eventChar * 100, 1100, 99 + (eventChar * 100), 1199);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "eventDied":
                    rectOrigin.set(eventChar * 100, 800, 99 + (eventChar * 100), 899);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "eventWon":
                    rectOrigin.set(eventChar * 100, 1200, 99 + (eventChar * 100), 1299);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "eventThirdStar":
                    rectOrigin.set(eventChar * 100, 1400, 99 + (eventChar * 100), 1499);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "eventRestartLevel":
                    rectOrigin.set(eventChar * 100, 900, 99 + (eventChar * 100), 999);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
                case "eventRewarded":
                    rectOrigin.set(eventChar * 100, 1300, 99 + (eventChar * 100), 1399);
                    rectDestin.set(sx, sy, sx + gameAreaBottomSize, sy + gameAreaBottomSize);
                    c.drawBitmap(texture, rectOrigin, rectDestin, null);
                    break;
            }
        }
    }

    public void animEvent() {
        if (eventX < 0) {
            eventX = (int)(Math.random() * (canvasWidth - gameAreaBottomSize));
        }
        eventAnimCurrentDist += eventAnimIncr;
        if (eventAnimCurrentDist >= eventAnimTotalDist * 2 && eventAnimIncr > 0) {
            eventAnimIncr = eventOriginalIncr * (-1);
        } else if (eventAnimCurrentDist <= 0) {
            eventAnimIncr = eventOriginalIncr;
            eventAnimCurrentDist = 0;
            eventX = -1;
            event = "";
        }

        switch (event) {
            case "bomb":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventBomb", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventBomb", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;
            case "won":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventWon", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventWon", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;
            case "died":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventDied", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventDied", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;
            case "comboRow":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventComboRow", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventComboRow", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;
            case "thirdStar":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventThirdStar", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventThirdStar", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;
            case "restartLevel":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventRestartLevel", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventRestartLevel", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;
            case "rewarded":
                if (eventAnimCurrentDist <= eventAnimTotalDist) {
                    drawsSprite("eventRewarded", eventX, gameAreaBottomY - eventAnimCurrentDist);
                } else {
                    drawsSprite("eventRewarded", eventX, gameAreaBottomY - eventAnimTotalDist);
                }
                break;



        }
    }

    // handles input

    public void handlesMenuTouch() {
        switch (action) {
            case "down":
                touchGrabTime = System.currentTimeMillis();
                if (touchX > menuPlayIconX &&
                        touchX < menuPlayIconX + menuPlayIconSize &&
                        touchY > menuPlayIconY + (4 * menuScreenBlockSize) &&
                        touchY < menuPlayIconY + menuPlayIconSize + (4 * menuScreenBlockSize)) {
                    touchGrab = true;
                } else if (touchX > canvasWidth - menuScreenBlockSize - backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize) {
                    touchGrab = true;
                } else if (touchX < leftIconX + backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize) {
                    touchGrab = true;
                }
                break;
            case "move":
                if (touchX > menuPlayIconX &&
                        touchX < menuPlayIconX + menuPlayIconSize &&
                        touchY > menuPlayIconY + (4 * menuScreenBlockSize) &&
                        touchY < menuPlayIconY + menuPlayIconSize + (4 * menuScreenBlockSize)) {
                    if (System.currentTimeMillis() - touchGrabTime >= touchGrabGap) {
                        touchGrab = true;
                    }
                } else if (touchX > canvasWidth - menuScreenBlockSize - backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize) {
                    if (System.currentTimeMillis() - touchGrabTime >= touchGrabGap) {
                        touchGrab = true;
                    }
                } else if (touchX < leftIconX + backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize) {
                    if (System.currentTimeMillis() - touchGrabTime >= touchGrabGap) {
                        touchGrab = true;
                    }
                } else {
                    touchGrab = false;
                    touchGrabTime = System.currentTimeMillis();
                }
                break;
            case "up":
                if (touchX > menuPlayIconX &&
                        touchX < menuPlayIconX + menuPlayIconSize &&
                        touchY > menuPlayIconY + (4 * menuScreenBlockSize) &&
                        touchY < menuPlayIconY + menuPlayIconSize + (4 * menuScreenBlockSize) &&
                        touchGrab) {
                    if (sound != null) {
                        sound.play(soundPlim, 1, 1, 0, 0, 1);
                    }
                    action = "";
                    centersUnlockedLevel();
                    touchGrab = false;
                    gameState = "levels";
                } else if (touchX > canvasWidth - menuScreenBlockSize - backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize &&
                        touchGrab) {
                    touchGrab = false;
                    sendBackPressed();
                }
                break;
        }
    }

    public void handlesLevelsTouch() {
        currentLevel = ((levelsScreenPositionY + (canvasHeight - touchY)) / levelsButtonSize / 2) + 1;
        int bbottom = (currentLevel * (levelsButtonSize * 2)) - levelsButtonSize;
        int btop = bbottom + levelsButtonSize;
        int relativeTouchY = canvasHeight - touchY + levelsScreenPositionY;
        switch (action) {
            case "down":
                touchInitY = touchY;
                if (touchX > levelsButtonX &&
                        touchX < levelsButtonX + levelsButtonSize &&
                        relativeTouchY > bbottom &&
                        relativeTouchY < btop &&
                        unlockedLevel >= currentLevel) {
                    touchGrab = true;
                } else if (touchX > canvasWidth - menuScreenBlockSize - backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize) {
                    touchGrab = true;
                }

                break;
            case "move":
                if (touchInitY < 0) {
                    touchInitY = touchY;
                }

                if (touchX > levelsButtonX &&
                        touchX < levelsButtonX + levelsButtonSize &&
                        relativeTouchY > bbottom &&
                        relativeTouchY < btop &&
                        unlockedLevel >= currentLevel) {
                    if (System.currentTimeMillis() - touchGrabTime >= touchGrabGap) {
                        touchGrab = true;
                    }
                } else if (touchX > canvasWidth - menuScreenBlockSize - backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize) {
                    if (System.currentTimeMillis() - touchGrabTime >= touchGrabGap) {
                        touchGrab = true;
                    }
                } else {
                    touchGrab = false;
                    touchGrabTime = System.currentTimeMillis();
                }

                if (touchY < touchInitY && levelsScreenPositionY > 0) {
                    levelsScreenPositionY -= (touchInitY - touchY);
                    if (levelsScreenPositionY < 0) {
                        levelsScreenPositionY = 0;
                    }
                    touchInitY = touchY;
                } else if (touchY > touchInitY && levelsScreenPositionY < levelsButtonSize * 2 * (numberOfLevels - 4)) {
                    levelsScreenPositionY += (touchY - touchInitY);
                    touchInitY = touchY;
                }
                break;
            case "up":
                touchInitY = -1;
                if (touchX > levelsButtonX &&
                        touchX < levelsButtonX + levelsButtonSize &&
                        relativeTouchY > bbottom &&
                        relativeTouchY < btop &&
                        unlockedLevel >= currentLevel &&
                        touchGrab) {
                    touchGrab = false;
                    action = "";
                    initLevel(currentLevel);
                } else if (touchX > canvasWidth - menuScreenBlockSize - backIconSize &&
                        touchY < menuScreenBlockSize + backIconSize &&
                        touchGrab) {
                    touchGrab = false;
                    sendBackPressed();
                }
                break;
        }
    }

    public void handlesGameTouch() {
        switch (action) {
            case "down":
                if (touchFree && !usingBomb &&
                        touchX > sliderX &&
                        touchX < sliderX + sliderSize &&
                        touchY > sliderY &&
                        touchY < sliderY + sliderSize) {
                    grabSlider();
                }
                break;
            case "move":
                if (touchFree && alive && !usingBomb) {
                    if (touchX > sliderX &&
                            touchX < sliderX + sliderSize &&
                            touchY > sliderY &&
                            touchY < sliderY + sliderSize) {
                        grabSlider();
                    }
                    if (sliderGrabbed) {
                        moveSlider();
                    }
                }
                break;
            case "up":
                if (touchFree && alive && !usingBomb) {
                    if (sliderGrabbed) {
                        shoot();
                    } else  if (pauseDialog) {
                        handlesPauseMenu();
                    } else if (touchX > canvasWidth - gameAreaTopY &&
                            touchY < gameAreaTopY) {
                        pause();
                    } else if (touchX < gameAreaBottomSize &&
                            touchY > canvasHeight - gameAreaBottomSize &&
                            numberOfBombs > 0) {
                        usesBomb();
                    }
                } else if (pauseDialog) {
                    handlesPauseMenu();
                } else if (deadDialog) {
                    handlesDeadMenu();
                } else if (won) {
                    handlesWonMenu();
                } else if (touchX > (canvasWidth / 2) - (gameAreaBottomSize / 2) &&
                        touchX < (canvasWidth / 2) + (gameAreaBottomSize / 2) &&
                        touchY > gameAreaBottomY && alive && !usingBomb) {
                    stopButton();
                } else if (touchX > canvasWidth - gameAreaTopY &&
                        touchY < gameAreaTopY) {
                    pause();
                } else {
                    touchX = -1;
                    touchY = -1;
                }
                break;
        }
    }

    public void grabSlider() {
        sliderGrabbed = true;
        if (drawsHand) {
            drawsHand = false;
        }
        slider = "sliderPressed";
        action = "down";
    }

    public void moveSlider() {
        sliderX = touchX - (sliderSize / 2);
        if (sliderX < sliderXMinLimit - (sliderSize / 2)) {
            sliderX = sliderXMinLimit - (sliderSize / 2);
        } else if (sliderX > sliderXMaxLimit - (sliderSize / 2)) {
            sliderX = sliderXMaxLimit - (sliderSize / 2);
        }
    }

    public void pause() {
        touchX = -1;
        touchY = -1;
        if (sound != null) {
            sound.play(soundPlim, 1, 1, 0, 0, 1);
        }
        pauseDialog = true;
    }

    public void handlesPauseMenu() {
        if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY &&
                touchY < dialogButtonY + dialogButtonHeight) {
            retryTimes ++;
            if (retryTimes == timesUntilInterstitial) {
                retryTimes = 0;
                interstitial = true;
            } else {
                event = "restartLevel";
                eventChar = (int)Math.floor(Math.random() * 4);
            }
            initLevel(currentLevel);
        } else if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY + dialogButtonGap &&
                touchY < dialogButtonY + dialogButtonGap + dialogButtonHeight) {
            if (sound != null) {
                sound.play(soundPlim, 1, 1, 0, 0, 1);
            }
            pauseDialog = false;
        } else if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY + (dialogButtonGap * 2) &&
                touchY < dialogButtonY + (dialogButtonGap * 2) + dialogButtonHeight) {
            alive = false;
            sendBackPressed();
        }
    }

    public void handlesDeadMenu() {
        if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY &&
                touchY < dialogButtonY + dialogButtonHeight) { // retry level
            touchX = -1;
            touchY = -1;
            retryTimes ++;
            if (retryTimes == timesUntilInterstitial) {
                retryTimes = 0;
                interstitial = true;
            }
            initLevel(currentLevel);
        } else if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY + dialogButtonGap &&
                touchY < dialogButtonY + dialogButtonGap + dialogButtonHeight) { // ad & continue
            touchX = -1;
            touchY = -1;
            if (sound != null) {
                sound.play(soundPlim, 1, 1, 0, 0, 1);
            }
            waitingForReward = true;
        } else if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY + (dialogButtonGap * 2) &&
                touchY < dialogButtonY + (dialogButtonGap * 2) + dialogButtonHeight) { // leave game
            touchX = -1;
            touchY = -1;
            if (sound != null) {
                sound.play(soundPlim, 1, 1, 0, 0, 1);
            }
            sendBackPressed();
        }
    }

    public void handlesWonMenu() {
        if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY + dialogButtonGap &&
                touchY < dialogButtonY + dialogButtonGap + dialogButtonHeight) { // ad & continue
            touchX = -1;
            touchY = -1;
            if (currentLevel < numberOfLevels) {
                initLevel(currentLevel);
            } else {
                sendBackPressed();
            }

        } else if (touchX > dialogButtonX &&
                touchX < dialogButtonX + dialogButtonWidth &&
                touchY > dialogButtonY + (dialogButtonGap * 2) &&
                touchY < dialogButtonY + (dialogButtonGap * 2) + dialogButtonHeight) { // leave game
            touchX = -1;
            touchY = -1;
            if (sound != null) {
                sound.play(soundPlim, 1, 1, 0, 0, 1);
            }
            sendBackPressed();
        }
    }

    public void sendTouchData(String tact, int tx, int ty) {
        action = tact;
        touchX = tx;
        touchY = ty;
    }

    public void sendBackPressed() {
        switch (gameState) {
            case "menu":
                // TODO should exit through the main thread
                if (sound != null) {
                    sound.play(soundPlim, 1, 1, 0, 0, 1);
                }
                System.exit(0);
                break;
            case "levels":
                if (sound != null) {
                    sound.play(soundPlim, 1, 1, 0, 0, 1);
                }
                action = "";
                gameState = "menu";
                break;
            case "game":
                if (!pauseDialog && alive) {
                    pause();
                } else if (pauseDialog && alive) {
                    if (sound != null) {
                        sound.play(soundPlim, 1, 1, 0, 0, 1);
                    }
                    pauseDialog = false;
                } else {
                    if (sound != null) {
                        sound.play(soundPlim, 1, 1, 0, 0, 1);
                    }
                    action = "";
                    allShotsHitGround();
                    gameState = "levels";
                }
                break;
        }
    }

    // environment

    public void sendTexture(Bitmap bmp) {
        texture = bmp;
    }

    public boolean getWaitingForReward() {
        return waitingForReward;
    }

    public void setWaitingForReward(boolean waitingForReward) {
        this.waitingForReward = waitingForReward;
    }

    public void reward() {
        rewarded = true;
        retryTimes = 0;
    }

    public boolean getInterstitial() {
        return interstitial;
    }

    public void setInterstitial(boolean interstitial) {
        this.interstitial = interstitial;
    }

    public boolean getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public void setAdPrefDialog(boolean adPrefDialog) {
        this.adPrefDialog = adPrefDialog;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        if (gameState.equals("game")) {
            gameState = "levels";
        }
        canvasWidth = width;
        canvasHeight = height;
        initVariables();
        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }
}