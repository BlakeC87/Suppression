package firestormsoftwarestudio.suppression;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SuppressionToDo extends AppCompatActivity {
    private static Handler handler;
    public static Space[][] spaces;
    private static Move currentMove;
    private static ArrayList<Space> cpuSpaces;
    private static int userScore;
    private static int cpuScore;
    private static final int TOTAL_SPACES = 64;
    private static boolean isPlayerTurn;
    private static String[] hints;
    private final int NUM_HINTS = 13;
    private static int currHint;
    private static boolean hintsDone;
    private static boolean gameStart;
    public static int aiSetting;
    public static int startingPieces;
    private boolean firstAITurn;
    private boolean secondAITurn;
    private boolean thirdAITurn;
    private boolean fourthAITurn;
    public static int firstAICol;
    InterstitialAd mInterstitialAd;
    private boolean wantsNewGame;
    private boolean gameInProgress;
    private String save;

    public static int screenWidth;
    public static int screenHeight;

    private int numWins;
    private int numLosses;
    private int numTies;
    private int numSuppressions;
    public static final String PREFS_NAME = "SuppressionSettings";

    public Button endGame;
    public Button replay;
    public LinearLayout buttonBar;
    public ImageButton audioButton;

    public Drawable speaker;
    public Drawable mute;

    public SoundPool soundPool;
    public SparseIntArray soundMap;
    private boolean muted;
    public TableLayout overlayGrid;

    public GameView gameView;
    //public PieceView pieceView;

    final int NUM_ROWS = 8;
    int cellSize;
    int[] cellCoordsX;
    int[] cellCoordsY;

    private Random rand;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    public TextView userText;
    public TextView cpuText;
    public TextView infoText;
    public FrameLayout fullGameLayout;

    public Drawable copyOverlay;
    public Drawable moveOverlay;

    public Drawable userPiece;
    public Drawable cpuPiece;
    public Drawable empty;

    public ImageView[][] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Context context = this.getApplicationContext();
        setContentView(R.layout.activity_suppression);

        wantsNewGame = false;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        PreferenceManager.getDefaultSharedPreferences(context);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.adUnitID));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                if (wantsNewGame) {
                    resetBoard();
                } else {
                    returnToMainMenu();
                }
            }
        });

        requestNewInterstitial();

        mute = getResources().getDrawable(R.drawable.mute);
        speaker = getResources().getDrawable(R.drawable.speaker);
        copyOverlay = getResources().getDrawable(R.drawable.orangeoverlay);
        moveOverlay = getResources().getDrawable(R.drawable.blueoverlay);
        cpuPiece = getResources().getDrawable(R.drawable.redpiece);
        userPiece = getResources().getDrawable(R.drawable.greenpiece);
        empty = getResources().getDrawable(R.drawable.empty);

        fullGameLayout = (FrameLayout) findViewById(R.id.fullGameLayout);

        //pieceView = (PieceView) findViewById(R.id.pieceView);
        gameView = (GameView) findViewById(R.id.gameView);

        /*
        pieceView.setZOrderOnTop(true);
        gameView.setZOrderOnTop(true);

        SurfaceHolder pieceHolder = pieceView.getHolder();
        pieceHolder.setFormat(PixelFormat.TRANSPARENT);

        SurfaceHolder gameHolder = gameView.getHolder();
        gameHolder.setFormat(PixelFormat.TRANSPARENT);
*/

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        userText = (TextView) findViewById(R.id.userScoreText);
        cpuText = (TextView) findViewById(R.id.cpuScoreText);
        infoText = (TextView) findViewById(R.id.hintText);
        endGame = (Button) findViewById(R.id.endGameBtn);
        replay = (Button) findViewById(R.id.replayBtn);
        buttonBar = (LinearLayout) findViewById(R.id.endButtonBar);
        audioButton = (ImageButton) findViewById(R.id.muteButton);
        overlayGrid = (TableLayout) findViewById(R.id.overlayGrid);

        if (soundPool == null) {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            soundMap = new SparseIntArray(3);
            soundMap.put(0, soundPool.load(context, R.raw.click1, 1));
            soundMap.put(1, soundPool.load(context, R.raw.click2, 1));
            soundMap.put(2, soundPool.load(context, R.raw.startgame, 1));
        }

        endGame.setClickable(false);
        replay.setClickable(false);
        buttonBar.setVisibility(View.INVISIBLE);

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (muted) {
                    audioButton.setImageDrawable(speaker);
                    muted = false;
                    getPreferences(MODE_PRIVATE).edit()
                            .putBoolean("music", true).commit();
                    Music.play(getApplicationContext(), R.raw.timefluxgame);
                } else {
                    audioButton.setImageDrawable(mute);
                    muted = true;
                    getPreferences(MODE_PRIVATE).edit()
                            .putBoolean("music", false).commit();
                    Music.stop(getApplicationContext());
                }
                audioButton.setScaleType(ImageView.ScaleType.FIT_XY);
                audioButton.setAdjustViewBounds(true);
            }
        });

        if (images == null) {
            images = new ImageView[8][8];

            images[0][7] = (ImageView) findViewById(R.id.imageView);
            images[1][7] = (ImageView) findViewById(R.id.imageView2);
            images[2][7] = (ImageView) findViewById(R.id.imageView3);
            images[3][7] = (ImageView) findViewById(R.id.imageView4);
            images[4][7] = (ImageView) findViewById(R.id.imageView5);
            images[5][7] = (ImageView) findViewById(R.id.imageView6);
            images[6][7] = (ImageView) findViewById(R.id.imageView7);
            images[7][7] = (ImageView) findViewById(R.id.imageView8);


            images[0][6] = (ImageView) findViewById(R.id.imageView9);
            images[1][6] = (ImageView) findViewById(R.id.imageView10);
            images[2][6] = (ImageView) findViewById(R.id.imageView11);
            images[3][6] = (ImageView) findViewById(R.id.imageView12);
            images[4][6] = (ImageView) findViewById(R.id.imageView13);
            images[5][6] = (ImageView) findViewById(R.id.imageView14);
            images[6][6] = (ImageView) findViewById(R.id.imageView15);
            images[7][6] = (ImageView) findViewById(R.id.imageView16);


            images[0][5] = (ImageView) findViewById(R.id.imageView17);
            images[1][5] = (ImageView) findViewById(R.id.imageView18);
            images[2][5] = (ImageView) findViewById(R.id.imageView19);
            images[3][5] = (ImageView) findViewById(R.id.imageView20);
            images[4][5] = (ImageView) findViewById(R.id.imageView21);
            images[5][5] = (ImageView) findViewById(R.id.imageView22);
            images[6][5] = (ImageView) findViewById(R.id.imageView23);
            images[7][5] = (ImageView) findViewById(R.id.imageView24);


            images[0][4] = (ImageView) findViewById(R.id.imageView25);
            images[1][4] = (ImageView) findViewById(R.id.imageView26);
            images[2][4] = (ImageView) findViewById(R.id.imageView27);
            images[3][4] = (ImageView) findViewById(R.id.imageView28);
            images[4][4] = (ImageView) findViewById(R.id.imageView29);
            images[5][4] = (ImageView) findViewById(R.id.imageView30);
            images[6][4] = (ImageView) findViewById(R.id.imageView31);
            images[7][4] = (ImageView) findViewById(R.id.imageView32);


            images[0][3] = (ImageView) findViewById(R.id.imageView33);
            images[1][3] = (ImageView) findViewById(R.id.imageView34);
            images[2][3] = (ImageView) findViewById(R.id.imageView35);
            images[3][3] = (ImageView) findViewById(R.id.imageView36);
            images[4][3] = (ImageView) findViewById(R.id.imageView37);
            images[5][3] = (ImageView) findViewById(R.id.imageView38);
            images[6][3] = (ImageView) findViewById(R.id.imageView39);
            images[7][3] = (ImageView) findViewById(R.id.imageView40);


            images[0][2] = (ImageView) findViewById(R.id.imageView41);
            images[1][2] = (ImageView) findViewById(R.id.imageView42);
            images[2][2] = (ImageView) findViewById(R.id.imageView43);
            images[3][2] = (ImageView) findViewById(R.id.imageView44);
            images[4][2] = (ImageView) findViewById(R.id.imageView45);
            images[5][2] = (ImageView) findViewById(R.id.imageView46);
            images[6][2] = (ImageView) findViewById(R.id.imageView47);
            images[7][2] = (ImageView) findViewById(R.id.imageView48);


            images[0][1] = (ImageView) findViewById(R.id.imageView49);
            images[1][1] = (ImageView) findViewById(R.id.imageView50);
            images[2][1] = (ImageView) findViewById(R.id.imageView51);
            images[3][1] = (ImageView) findViewById(R.id.imageView52);
            images[4][1] = (ImageView) findViewById(R.id.imageView53);
            images[5][1] = (ImageView) findViewById(R.id.imageView54);
            images[6][1] = (ImageView) findViewById(R.id.imageView55);
            images[7][1] = (ImageView) findViewById(R.id.imageView56);


            images[0][0] = (ImageView) findViewById(R.id.imageView57);
            images[1][0] = (ImageView) findViewById(R.id.imageView58);
            images[2][0] = (ImageView) findViewById(R.id.imageView59);
            images[3][0] = (ImageView) findViewById(R.id.imageView60);
            images[4][0] = (ImageView) findViewById(R.id.imageView61);
            images[5][0] = (ImageView) findViewById(R.id.imageView62);
            images[6][0] = (ImageView) findViewById(R.id.imageView63);
            images[7][0] = (ImageView) findViewById(R.id.imageView64);

        }


        if (handler == null) {
            handler = new Handler();
        }

        calculateCoords();

        gameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int row;
                int col;

                int x = (int) event.getX();
                int y = (int) event.getY();


                // if touch event is ignored
                if (x < cellCoordsX[0] || x > cellCoordsX[7] + cellSize
                        || y < cellCoordsY[0] || y > cellCoordsY[7] + cellSize) {
                    return true;
                } else {
                    row = (x / cellSize);
                    col = 10 - (y / cellSize);
                    Log.d("clicked", "Game View clicked. Row: " + row + ", Col: " + col);
                    Log.d("locX", "X: " + x);
                    Log.d("locY", "Y: " + y);

                    if (row > 7) row = 7;
                    if (col > 7) col = 7;
                    if (row < 0) row = 0;
                    if (col < 0) col = 0;

                    selectSpace(row, col);
                }

                disableButtons();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableButtons();
                    }
                }, 250);

                return false;
            }
        });

        gameView.requestFocus();

        spaces = new Space[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                spaces[i][j] = new Space(i, j);
            }
        }

        aiSetting = prefs.getInt("AISetting", 1);
        startingPieces = prefs.getInt("StartingPieces", 1);
        save = prefs.getString("save", "");
        if (save.length() == 65) {
            loadSave(save);
        }
        else {
            resetBoard();
        }

        rand = new Random();


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }


    public void clearOverlay() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!spaces[i][j].isClaimed()) {
                    images[i][j].setImageDrawable(empty);

                    images[i][j].setScaleType(ImageView.ScaleType.FIT_XY);
                    images[i][j].setAdjustViewBounds(true);
                }
            }
        }
    }
    public void clearPieces() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                images[i][j].setImageDrawable(empty);
                images[i][j].setScaleType(ImageView.ScaleType.FIT_XY);
                images[i][j].setAdjustViewBounds(true);
            }
        }
    }

    public void drawOverlay(int row, int col, boolean isCopy) {
        if (isCopy) {
            images[row][col].setImageDrawable(copyOverlay);
        } else {
            images[row][col].setImageDrawable(moveOverlay);
        }
        images[row][col].setScaleType(ImageView.ScaleType.FIT_XY);
        images[row][col].setAdjustViewBounds(true);
    }

    public void addPiece(int row, int col, boolean isPlayer) {
        if (isPlayer) {
            images[row][col].setImageDrawable(userPiece);
        } else {
            images[row][col].setImageDrawable(cpuPiece);
        }
        images[row][col].setScaleType(ImageView.ScaleType.FIT_XY);
        images[row][col].setAdjustViewBounds(true);
    }
    public void removePiece(int row, int col) {
        images[row][col].setImageDrawable(empty);
        images[row][col].setScaleType(ImageView.ScaleType.FIT_XY);
        images[row][col].setAdjustViewBounds(true);
    }

    private void loadSave(String info) {
        int r = 0;
        int c = 0;
        userScore = 0;
        cpuScore = 0;
        cpuSpaces = new ArrayList<Space>();

        for (int index = 0; index < info.length() - 1; index++) {
            // player space
            if (info.charAt(index) == '1') {
                spaces[r][c].setIsClaimed(true);
                spaces[r][c].setIsClaimedByPlayer(true);
                addPiece(r, c, true);
                userScore++;
            }
            // opponent space
            else if (info.charAt(index) == '2') {
                spaces[r][c].setIsClaimed(true);
                spaces[r][c].setIsClaimedByPlayer(false);
                addPiece(r, c, false);
                cpuScore++;
            }
            // empty
            else {
                spaces[r][c].setIsClaimed(false);
                spaces[r][c].setIsClaimedByPlayer(false);
            }
            // update board spaces from index
            r++;
            if (r > 7) {
                r = 0;
                c++;
            }

            if (info.charAt(info.length() - 1) == '1') {
                isPlayerTurn = true;
            }
            else {
                isPlayerTurn = false;
            }
        }

        generateHints();

        firstAITurn = false;
        secondAITurn = false;
        thirdAITurn = false;
        fourthAITurn = false;

        if (cpuScore < 5) {
            if (spaces[7][7].isClaimed() && !spaces[7][7].isClaimedByPlayer()
                    && spaces[7][0].isClaimed() && !spaces[7][0].isClaimedByPlayer()) {
                if (cpuScore == 1) {
                    firstAITurn = true;
                }
                else if (cpuScore == 2) {
                    secondAITurn = true;
                }
                else if (cpuScore == 3) {
                    thirdAITurn = true;
                }
                else {
                    fourthAITurn = true;
                }
            }
        }

        gameStart = true;
        gameInProgress = true;
        hintsDone = false;
        currHint = userScore;
        if (currHint >= NUM_HINTS - 1) {
            currHint = NUM_HINTS - 2;
            hintsDone = true;
        }
        currentMove = new Move();
        userText.setText(Integer.toString(userScore));
        cpuText.setText(Integer.toString(cpuScore));
        infoText.setText(hints[currHint]);
        currHint++;
        if (isPlayerTurn) {
            enableButtons();
        }
        else {
            disableButtons();
            cpuTurn();
        }
    }

    private void calculateCoords() {
        int width;
        int height;
        int gapHeight;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        width = size.x;
        height = size.y;

        screenWidth = width;
        screenHeight = height;

        cellCoordsX = new int[NUM_ROWS];
        cellCoordsY = new int[NUM_ROWS];

        gapHeight = Math.abs(height - width) / 2;
        cellSize = width / NUM_ROWS;

        for (int i = 0; i < NUM_ROWS; i++) {
            cellCoordsX[i] = cellSize * i;
            cellCoordsY[i] = cellSize * i + gapHeight;

            //cellCoordsX[i] = images[i][7].getLeft();
            //cellCoordsY[i] = images[0][7-i].getTop();
        }

        gameView.measure(width, height);
        //overlayGrid.setLayoutParams(new FrameLayout.LayoutParams(screenWidth, screenWidth));
        //overlayGrid.setTop(70);
        //pieceView.measure(width, height);


        gameView.setVisibility(View.VISIBLE);
        //pieceView.setVisibility(View.VISIBLE);
    }



    private void returnToMainMenu() {
        Intent i = new Intent(this, Menu.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Music.stop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Music.play(this, R.raw.timefluxgame);
    }

    private void playSoundEffect() {
        double rand = Math.random();
        int num;
        if (rand <= 0.4) {
            num = 0;
        }
        else {
            num = 1;
        }

        if (num == 0) {
            soundPool.play(soundMap.get(0), 1.0f, 1.0f, 1, 0, 1.0f);
        } else {
            soundPool.play(soundMap.get(1), 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    private void resetBoard() {
        endGame.setClickable(false);
        replay.setClickable(false);
        buttonBar.setVisibility(View.INVISIBLE);
        gameInProgress = true;
        calculateCoords();
        clearPieces();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                spaces[i][j].setCanCopy(false);
                spaces[i][j].setCanMove(false);
                spaces[i][j].setIsClaimedByPlayer(false);
                spaces[i][j].setIsClaimed(false);
                spaces[i][j].setIsSelected(false);
                spaces[i][j].setIsUnavailable(false);
            }
        }
        spaces[0][0].setIsClaimed(true);
        spaces[0][0].setIsClaimedByPlayer(true);
        spaces[7][7].setIsClaimed(true);
        addPiece(0, 0, true);
        addPiece(7, 7, false);

        if (startingPieces != 1) {
            spaces[0][7].setIsClaimed(true);
            spaces[0][7].setIsClaimedByPlayer(true);
            spaces[7][0].setIsClaimed(true);
            addPiece(0, 7, true);
            addPiece(7, 0, false);
        }

        userScore = startingPieces;
        cpuScore = startingPieces;

        generateHints();

        if (startingPieces == 2) {
            firstAITurn = true;
        }
        else {
            firstAITurn = false;
        }
        secondAITurn = false;
        thirdAITurn = false;
        fourthAITurn = false;
        gameStart = true;
        hintsDone = false;
        currHint = 0;
        currentMove = new Move();
        cpuSpaces = new ArrayList<Space>();
        isPlayerTurn = true;
        enableButtons();
        userText.setText(Integer.toString(userScore));
        cpuText.setText(Integer.toString(cpuScore));
        infoText.setText(hints[currHint]);
        currHint++;
    }

    private void generateHints() {
        hints = new String[NUM_HINTS];
        if (startingPieces == 1) {
            hints[0] = "You start at the bottom left corner.  To begin, click your green piece.";
        }
        else {
            hints[0] = "You start on the left side.  To begin, click one of your green pieces.";
        }
        hints[1] = "The highlighted spaces are all the locations you can move to.  You can copy your piece to an orange space, or jump to a blue one.";
        hints[2] = "After your turn, your opponent gets to make a move.  Then it\'s your turn again.";
        hints[3] = "If you choose a piece you don\'t want to move, simply pick a different piece to see the available move options.";
        hints[4] = "At first, it might be a good idea to build up a base by coping your pieces.";
        hints[5] = "As the game goes on and the board fills, your strategy should change.";
        hints[6] = "Once you have opponent pieces close to yours, you can gain an advantage faster by capturing their pieces.";
        hints[7] = "If you move next to an enemy piece, all of the pieces that are adjacent to your landing spot will become yours.";
        hints[8] = "But be careful - the same rule applies for your opponent.";
        hints[9] = "If you can stop your opponent from making any moves, you automatically claim the rest of the board at twice the normal amount of points.";
        hints[10] = "When the board is filled, or either player has no pieces or moves remaining, the game is over.";
        hints[11] = "Whoever has the most pieces at the end of the game wins!";
        hints[12] = "";
    }

    private void showNextHint() {
        infoText.setText(hints[currHint]);
        currHint++;
        if (currHint >= NUM_HINTS) {
            hintsDone = true;
        }
    }

    private void selectSpace(int row, int col) {
        clearOverlay();

        if (isPlayerTurn) {
            if (spaces[row][col].isClaimedByPlayer()) {
                deselect();
                if (gameStart) {
                    showNextHint();
                    gameStart = false;
                }
                currentMove.addStart(spaces[row][col]);
                markSpacesForHighlighting(spaces[row][col]);
            }
            else if (((spaces[row][col].canMove())
                    || (spaces[row][col].canCopy()))
                    && (!spaces[row][col].isClaimed())) {
                disableButtons();
                currentMove.addEnd(spaces[row][col]);
                currentMove.calculateScore(spaces[row][col], true, aiSetting);
                move(currentMove);
            }
            else {
                deselect();
                currentMove.resetMove();
            }
        }
        else {
            if (spaces[row][col].isClaimed() && !spaces[row][col].isClaimedByPlayer()) {
                deselect();
                currentMove.addStart(spaces[row][col]);
                markSpacesForHighlighting(spaces[row][col]);
            }
            else if (((spaces[row][col].canMove())
                    || (spaces[row][col].canCopy()))
                    && (!spaces[row][col].isClaimed())) {
                disableButtons();
                currentMove.addEnd(spaces[row][col]);
                currentMove.calculateScore(spaces[row][col], false, aiSetting);
                move(currentMove);
            }
            else {
                deselect();
                currentMove.resetMove();
            }
        }

    }

    private void markSpacesForHighlighting(Space space) {
        spaces[space.getRow()][space.getCol()].setIsSelected(true);
        for (Location loc : space.getMovable()) {
            int row = loc.getRow();
            int col = loc.getCol();
            if (!spaces[row][col].isClaimed()) {
                if (loc.canCopy()) {
                    spaces[row][col].setCanCopy(true);
                    spaces[row][col].setCanMove(true);

                    // have the gameView put a copy overlay at this location
                    drawOverlay(row, col, true);
                }
                else {
                    spaces[row][col].setCanMove(true);

                    // have the gameView put a move overlay at this location
                    drawOverlay(row, col, false);
                }
            }
        }
    }

    private void markSpacesForMoving(Space space) {
        spaces[space.getRow()][space.getCol()].setIsSelected(true);
        for (Location loc : space.getMovable()) {
            int row = loc.getRow();
            int col = loc.getCol();
            if (!spaces[row][col].isClaimed()) {
                if (loc.canCopy()) {
                    spaces[row][col].setCanCopy(true);
                    spaces[row][col].setCanMove(true);
                }
                else {
                    spaces[row][col].setCanMove(true);
                }
            }
        }
    }

    private void updateScores() {
        userText.setText(Integer.toString(userScore));
        cpuText.setText(Integer.toString(cpuScore));
    }


    private void move(Move move) {

        final int startRow = move.getStartingSpace().getRow();
        final int startCol = move.getStartingSpace().getCol();
        final boolean endCanCopy = move.getEndingSpace().canCopy();
        final int endRow = move.getEndingSpace().getRow();
        final int endCol = move.getEndingSpace().getCol();

        if (isPlayerTurn && !hintsDone) {
            showNextHint();
        }

        if (hintsDone && !infoText.getText().equals(hints[12])) {
            infoText.setText(hints[12]);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!endCanCopy) {
                    spaces[startRow][startCol].setIsClaimedByPlayer(false);
                    spaces[startRow][startCol].setIsClaimed(false);

                    if (isPlayerTurn) {
                        userScore -= 1;
                        updateDisplay(startRow, startCol, true, true);
                    }
                    else {
                        cpuScore -= 1;
                        updateDisplay(startRow, startCol, false, true);
                    }
                    playSoundEffect();
                }

                deselect();
            }
        }, 50);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                spaces[endRow][endCol].setIsClaimed(true);
                if (isPlayerTurn) {
                    spaces[endRow][endCol].setIsClaimedByPlayer(true);
                    userScore += 1;
                    updateDisplay(endRow, endCol, true, false);

                } else {
                    spaces[endRow][endCol].setIsClaimedByPlayer(false);
                    cpuScore += 1;
                    updateDisplay(endRow, endCol, false, false);

                }
                playSoundEffect();

                updateScores();
            }
        }, 100);

        int multiplier = 0;
        for (Location loc : spaces[endRow][endCol].getAdjacent()) {
            final int y = loc.getRow();
            final int x = loc.getCol();
            if (isPlayerTurn) {
                if (spaces[y][x].isClaimed() && !spaces[y][x].isClaimedByPlayer()) {
                    multiplier++;
                }
            }
            else {
                if (spaces[y][x].isClaimed() && spaces[y][x].isClaimedByPlayer()) {
                    multiplier++;
                }
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isPlayerTurn) {
                        if (spaces[y][x].isClaimed() && !spaces[y][x].isClaimedByPlayer()) {
                            spaces[y][x].setIsClaimedByPlayer(true);
                            userScore += 1;
                            cpuScore -= 1;
                            playSoundEffect();
                            updateDisplay(y, x, true, false);
                        }
                    }
                    else {
                        if (spaces[y][x].isClaimed() && spaces[y][x].isClaimedByPlayer()) {
                            spaces[y][x].setIsClaimedByPlayer(false);
                            userScore -= 1;
                            cpuScore += 1;
                            playSoundEffect();
                            updateDisplay(y, x, false, false);
                        }
                    }
                    updateScores();
                }
            }, 200 + (multiplier * 50));
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                endTurn();
            }
        }, 300 + (multiplier * 50));
    }

    private void endTurn() {
        currentMove = new Move();
        cpuSpaces.clear();

        boolean playerHasMoves = analyzePlayerMoves();
        boolean cpuHasMoves = analyzeCPUMoves();

        if (!playerHasMoves && !cpuHasMoves) {
            gameOver();
        }
        else if ((userScore + cpuScore >= TOTAL_SPACES)
                || (userScore <= 0) || (cpuScore <= 0)) {
            gameOver();
        }
        else if (!playerHasMoves && !isPlayerTurn) {
            infoText.setText("You have been suppressed.\nAll unclaimed spaces are worth double.");
            finishGame(false);
        }
        else if (!cpuHasMoves && isPlayerTurn) {
            infoText.setText("The computer has been suppressed.\nAll unclaimed spaces are worth double.");
            finishGame(true);
        }
        else if (isPlayerTurn) {
            isPlayerTurn = false;
            cpuTurn();
        }
        else {
            isPlayerTurn = true;
            enableButtons();
        }
    }

    private void finishGame(boolean isPlayer) {
        if (isPlayer) {
            numSuppressions = 1;
        }
        int delay = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                final int row = i;
                final int col = j;
                if (!spaces[i][j].isClaimed()) {
                    delay ++;
                    if (isPlayer) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                spaces[row][col].setIsClaimed(true);
                                spaces[row][col].setIsClaimedByPlayer(true);
                                userScore += 2;
                                updateScores();
                                updateDisplay(row, col, true, false);
                                playSoundEffect();
                            }
                        }, 25 + (delay * 35));
                    }
                    else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                spaces[row][col].setIsClaimed(true);
                                cpuScore += 2;
                                updateScores();
                                updateDisplay(row, col, false, false);
                                playSoundEffect();
                            }
                        }, 25 + (delay * 35));
                    }
                }
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gameOver();
            }
        }, 125 + (delay * 35));
    }

    private boolean analyzePlayerMoves() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (spaces[i][j].isClaimedByPlayer()) {
                    for (Location loc : spaces[i][j].getMovable()) {
                        if (!spaces[loc.getRow()][loc.getCol()].isClaimed()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean analyzeCPUMoves() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((!spaces[i][j].isClaimedByPlayer())
                        && (spaces[i][j].isClaimed())) {
                    for (Location loc : spaces[i][j].getMovable()) {
                        if (!spaces[loc.getRow()][loc.getCol()].isClaimed()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void updateDisplay(int row, int col, boolean isPlayer, boolean remove) {

        if (remove) {
            removePiece(row, col);
        }
        else {
            addPiece(row, col, isPlayer);
        }
    }

    private void cpuTurn() {
        deselect();
        int highScore = -63;
        int currScore = -63;
        Location bestStart = new Location(0, 0, false);
        Location bestEnd = new Location(0, 0, false);
        cpuSpaces = new ArrayList<Space>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (spaces[i][j].isClaimed() && !spaces[i][j].isClaimedByPlayer()) {
                    cpuSpaces.add(spaces[i][j]);
                }
            }
        }

        for (Space space : cpuSpaces) {
            markSpacesForMoving(space);
            for (Location loc : space.getMovable()) {
                int row = loc.getRow();
                int col = loc.getCol();

                if (spaces[row][col].canMove()) {
                    currentMove = new Move(space, spaces[row][col]);
                    currentMove.calculateScore(spaces[row][col], false, aiSetting);
                    currScore = currentMove.getScore();

                    if (fourthAITurn) {
                        if (firstAICol == 0) {
                            if (space.getCol() != 0) {
                                currScore = -63;
                            }
                        }
                        else {
                            if (space.getCol() != 7) {
                                currScore = -63;
                            }
                        }
                    }

                    if (secondAITurn || thirdAITurn) {
                        if (firstAICol == 0) {
                            if (space.getCol() != 7) {
                                currScore = -63;
                            }
                        }
                        else {
                            if (space.getCol() != 0) {
                                currScore = -63;
                            }
                        }
                    }

                    if (currScore > highScore) {
                        highScore = currScore;
                        bestStart = new Location(space.getRow(), space.getCol(), false);
                        bestEnd = new Location(loc.getRow(), loc.getCol(), loc.canCopy());
                    }
                }
            }
        }

        final int startRow = bestStart.getRow();
        final int startCol = bestStart.getCol();
        final int endRow = bestEnd.getRow();
        final int endCol = bestEnd.getCol();

        if (fourthAITurn) {
            fourthAITurn = false;
        }
        else if (thirdAITurn) {
            thirdAITurn = false;
            fourthAITurn = true;
        }
        else if (secondAITurn) {
            secondAITurn = false;
            thirdAITurn = true;
        }
        else if (firstAITurn) {
            firstAICol = startCol;
            firstAITurn = false;
            secondAITurn = true;
        }

        //deselect();

        int delay;
        if (aiSetting == 1) {
            // strategic
            delay = 625;
        }
        else if (aiSetting == 2 || aiSetting == 3) {
            // aggressive, reckless
            delay = 500;
        }
        else if (aiSetting == 0 || aiSetting == 4){
            // cautious, pacifist
            delay = 750;
        }
        else {
            //confused
            delay = rand.nextInt(600) + 400;

        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectSpace(startRow, startCol);
            }
        }, delay);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectSpace(endRow, endCol);
            }
        }, (delay * 2));
    }

    private void gameOver() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        disableButtons();

        gameInProgress = false;
        editor.putString("save", "");

        String endInfo;
        if (userScore > cpuScore) {
            endInfo = "You win!";
            numWins = settings.getInt("wins", 0) + 1;
            editor.putInt("wins", numWins);

        }
        else if (userScore < cpuScore) {
            endInfo = "You lose.";
            numLosses = settings.getInt("losses", 0) + 1;
            editor.putInt("losses", numLosses);
        }
        else {
            endInfo = "The game was a draw.";
            numTies = settings.getInt("ties", 0) + 1;
            editor.putInt("ties", numTies);
        }

        editor.putInt("suppressions", numSuppressions);

        editor.commit();
        endInfo += "\nDo you want a rematch?";

        infoText.setText(endInfo);

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wantsNewGame = true;
                if (mInterstitialAd.isLoaded()) {
                    //if (!muted) {
                        //audioThread.mediaPlayer.pause();
                        //audioThread.menuPlayer.pause();
                    //}
                    mInterstitialAd.show();
                } else {
                    buttonBar.setVisibility(View.INVISIBLE);
                    endGame.setClickable(false);
                    replay.setClickable(false);
                    resetBoard();
                }
            }
        });

        endGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wantsNewGame = false;
                if (mInterstitialAd.isLoaded()) {
                    //if (!muted) {
                        //audioThread.mediaPlayer.pause();
                        //audioThread.menuPlayer.pause();
                    //}
                    mInterstitialAd.show();
                }
                else {
                    buttonBar.setVisibility(View.INVISIBLE);
                    endGame.setClickable(false);
                    replay.setClickable(false);
                    returnToMainMenu();
                }
            }
        });

        buttonBar.setVisibility(View.VISIBLE);
        endGame.setClickable(true);
        replay.setClickable(true);
        buttonBar.bringToFront();
    }

    private void disableButtons() {
        gameView.setClickable(false);
        //pieceView.setClickable(false);
    }

    private void enableButtons() {
        gameView.setClickable(true);
        //pieceView.setClickable(true);
    }

    private void deselect() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                spaces[i][j].deselect();
            }
        }
        clearOverlay();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void saveGame() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        if (gameInProgress) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (spaces[i][j].isClaimedByPlayer()) {
                        sb.append('1');
                    }
                    else if (spaces[i][j].isClaimed()) {
                        sb.append('2');
                    }
                    else {
                        sb.append('0');
                    }
                }
            }

            if (isPlayerTurn) {
                sb.append('1');
            }
            else {
                sb.append('0');
            }

            editor.putString("save", sb.toString());

        }

        editor.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isMuted", muted);

        // save game if one is in progress
        if (gameInProgress) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (spaces[i][j].isClaimedByPlayer()) {
                        sb.append('1');
                    }
                    else if (spaces[i][j].isClaimed()) {
                        sb.append('2');
                    }
                    else {
                        sb.append('0');
                    }
                }
            }

            if (isPlayerTurn) {
                sb.append('1');
            }
            else {
                sb.append('0');
            }

            editor.putString("save", sb.toString());

        }

        editor.commit();

        finish();
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        saveGame();
        Intent i = new Intent(this, Menu.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}


