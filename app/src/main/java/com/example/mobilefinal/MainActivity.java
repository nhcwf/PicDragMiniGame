/**
 *
 * @author  NHC
 * @version 1.2
 * @since   2023-10-26
 */
package com.example.mobilefinal;

import static com.example.mobilefinal.DatabaseHelper.DATABASE_NAME;
import static com.example.mobilefinal.DatabaseHelper.DATABASE_VERSION;
import static com.example.mobilefinal.LoginActivity.USERNAME_BUNDLE;
import static com.example.mobilefinal.LoginActivity.USERNAME_STRING;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final int DROP_BOXES_MAX_SIZE = 9;
    public static final String CURRENT_SCORE = "score";
    public static final String GAME_STARTED_TIME = "startTime";
    public static final String BOX_COUNT = "boxCount";
    public static final String RANDOM_IDS = "randomIds";
    public static final String CORRECT_BOX_ID = "correctBoxId";
    // The score will start to get punished when the total seconds, which is how long the game has been played, is larger than the time threshold.
    public int gameTimeThresholdLimitValue = 10;
    public int scoreMultiplier = 10 * gameTimeThresholdLimitValue; // The lower the multiplier, the harder it gets.
    public int gameSessionCount;
    public int currentScore = 0;
    boolean image_default_position_is_not_saved = true;
    boolean is_add_image_button_first_click = true;
    boolean weak_player_confirmed = false;
    long startTimeMillisecond, endTimeMillisecond;
    float imageX, imageY;
    ImageView importedImage, removeImage, surender;
    ArrayList<ImageView> dropBoxes = new ArrayList<>(DROP_BOXES_MAX_SIZE);
    int[] randomIds = new int[DROP_BOXES_MAX_SIZE];
    int correctBoxId = -1, boxCount = 0, importedImageCount = 0;
    Button addImage, stop;
    ConstraintLayout mainLayout;
    Bitmap bitmap;
    TextView username, score;
    String usernameString;

    @SuppressLint({"DefaultLocale", "MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper database = new DatabaseHelper(MainActivity.this, DATABASE_NAME, null, DATABASE_VERSION);
        gameSessionCount = database.getGameSessionCount();
        database.close();

        assignViews();

        score.setText("Score: 0");

        usernameString = getUsernameString();
        username.setText(usernameString);

        addImage.setOnClickListener(addImageOnClickListener);

        stop.setOnClickListener(stopOnClickListener);
        stop.setVisibility(View.INVISIBLE);

        importedImage.setOnLongClickListener(importedImageOnLongClickListener);
        removeImage.setOnDragListener(removeImageOnDragListener);
        mainLayout.setOnDragListener(mainLayoutOnDragListener);

        for (ImageView dropbox : dropBoxes) {
            dropbox.setOnDragListener(dropboxOnDragListener);
            dropbox.setOnLongClickListener(dropBoxImageOnLongClickListener);
        }

        generateRandomUniqueArray(randomIds, DROP_BOXES_MAX_SIZE, DROP_BOXES_MAX_SIZE);
        correctBoxId = getCorrectBoxId(randomIds[boxCount++]);

        surender.setOnClickListener(surenderOnClickListener);
    }

    //  Assigns views to local view variables using findViewById().
    private void assignViews() {
        score = (TextView) findViewById(R.id.tv_score);
        username = (TextView) findViewById(R.id.tv_username);
        addImage = (Button) findViewById(R.id.btn_add_image);
        stop = (Button) findViewById(R.id.btn_finish);
        importedImage = (ImageView) findViewById(R.id.iv_imported_image);
        removeImage = (ImageView) findViewById(R.id.iv_remove_image);
        mainLayout = (ConstraintLayout) findViewById(R.id.cl_main_layout);
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_1));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_2));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_3));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_4));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_5));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_6));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_7));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_8));
        dropBoxes.add((ImageView) findViewById(R.id.iv_dropbox_9));
        surender = (ImageView) findViewById(R.id.iv_surender);
    }

    // Assigns to array a random int[] that has unique elements with value ranging from 0 to max_bound.
    private void generateRandomUniqueArray(int[] array, int size, int max_bound) {
        HashSet<Integer> ids = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            int value = random.nextInt(max_bound);

            while (ids.contains(value)) {
                value = random.nextInt(max_bound);
            }

            ids.add(value);
            array[i] = value;
        }
    }

    // Returns the View Id of the "correct" drop box.
    private int getCorrectBoxId(int index) {
        if (index == DROP_BOXES_MAX_SIZE)
            return -1;
        return dropBoxes.get(index).getId();
    }

    // Save the time when the game starts (in millisecond).
    public void runTimer() {
        if (is_add_image_button_first_click) {
            startTimeMillisecond = Calendar.getInstance().getTimeInMillis();
            stop.setVisibility(View.VISIBLE);
            is_add_image_button_first_click = false;
            Toast.makeText(MainActivity.this, "Game started!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_SCORE, currentScore);
        outState.putInt(BOX_COUNT, boxCount);
        outState.putInt(CORRECT_BOX_ID, correctBoxId);
        outState.putLong(GAME_STARTED_TIME, startTimeMillisecond);
        outState.putString(USERNAME_STRING, usernameString);
        outState.putIntArray(RANDOM_IDS, randomIds);
        outState.putBoolean("weak", weak_player_confirmed);
    }

    @SuppressLint({"DefaultLocale"})
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentScore = savedInstanceState.getInt(CURRENT_SCORE);
        boxCount = savedInstanceState.getInt(BOX_COUNT);
        correctBoxId = savedInstanceState.getInt(CORRECT_BOX_ID);
        startTimeMillisecond = savedInstanceState.getLong(GAME_STARTED_TIME);
        usernameString = savedInstanceState.getString(USERNAME_STRING);
        randomIds = savedInstanceState.getIntArray(RANDOM_IDS);
        weak_player_confirmed = savedInstanceState.getBoolean("weak");
    }

    // Returns usernameString. Data is achieved from Intent.BundleExtra().
    public String getUsernameString() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(USERNAME_BUNDLE);

        String username = "";
        if (bundle != null) { username = Objects.requireNonNull(bundle).getString(USERNAME_STRING); }
        return username;
    }

    // Allows the user to pick only a single image after clicking the View.
    View.OnClickListener addImageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String mimeType = "image/*";
            imagePicker.launch(new Intent().setType(mimeType).setAction(Intent.ACTION_GET_CONTENT));
        }
    };

    // Handle process that should run when user has picked an image.
    private void handleAfterPickingImage() {
        importedImageCount++;
        runTimer();
    }

    ActivityResultLauncher<Intent> imagePicker = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();

            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                importedImage.setImageBitmap(bitmap);
                importedImage.setVisibility(View.VISIBLE);

                if (image_default_position_is_not_saved) {
                    // then save imported image default position
                    imageX = importedImage.getX();
                    imageY = importedImage.getY();
                    image_default_position_is_not_saved = false;
                }

                importedImage.setX(imageX);
                importedImage.setY(imageY);

                handleAfterPickingImage();
            }
        }
    });

    // Show the hint when the user click the surender icon.
    View.OnClickListener surenderOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            weak_player_confirmed = true;
            currentScore -= randomIds[0] * scoreMultiplier;
            score.setText(String.format("Score: %s", currentScore));
            TextView hint = (TextView) findViewById(R.id.tv_hint);
            StringBuilder hintString = new StringBuilder();

            for (int i = 0; i < DROP_BOXES_MAX_SIZE; i++) { hintString.append(randomIds[i]).append(" "); }

            hint.setText(hintString.toString());
        }
    };

    // Performs actions when the game stop: save game session data to the database, then proceed to the Results Screen.
    View.OnClickListener stopOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save the time when the game stops.
            endTimeMillisecond = Calendar.getInstance().getTimeInMillis();

            gameSessionCount++;
            DatabaseHelper database = new DatabaseHelper(MainActivity.this, DATABASE_NAME, null, DATABASE_VERSION);
            database.insertGameSession(gameSessionCount, currentScore, endTimeMillisecond - startTimeMillisecond);
            database.close();

            Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString(USERNAME_STRING, usernameString);
            intent.putExtra(USERNAME_BUNDLE, bundle);

            startActivity(intent);
        }
    };

    View.OnLongClickListener importedImageOnLongClickListener = v -> {
        ClipData dragData = ClipData.newPlainText("", "");
        View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);

        v.startDragAndDrop(dragData, dragShadowBuilder,  v, 0);

        v.setVisibility(View.INVISIBLE);

        return true;
    };

    View.OnLongClickListener dropBoxImageOnLongClickListener = v -> {
        ClipData dragData = ClipData.newPlainText("", "");
        View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v);

        v.startDragAndDrop(dragData, dragShadowBuilder,  v, 0);

        ((ImageView) v).setImageBitmap(null);

        return true;
    };

    // Remove the image when the user drop it on the View. The View may change background color based on the user drag and drop actions.
    View.OnDragListener removeImageOnDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundResource(R.drawable.rounded_corner_light_green);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.TRANSPARENT);
                    break;
                case DragEvent.ACTION_DROP:
                    importedImage.setImageBitmap(null);

                    // Funny stuff that is so called easter eggs. I guess.
                    if (weak_player_confirmed) {
                        if (boxCount == DROP_BOXES_MAX_SIZE) {
                            currentScore = Integer.MIN_VALUE;
                            usernameString = "Disappointment";
                            username.setText(usernameString);
                            Toast.makeText(MainActivity.this, "I am SO proud of such a loser like you.", Toast.LENGTH_SHORT).show();
                        } else if (boxCount == 1) {
                            currentScore = Integer.MIN_VALUE;
                            usernameString = "Disappointment Two";
                            username.setText(usernameString);
                            Toast.makeText(MainActivity.this, "Good job, you dumb dumb.", Toast.LENGTH_SHORT).show();
                        }
                        score.setText(String.format("Score: %s", currentScore));
                        break;
                    }

                    // This brace is for honorable beings only.
                    if (boxCount == DROP_BOXES_MAX_SIZE) {
                        currentScore = Integer.MAX_VALUE;
                        usernameString = "The honest";
                        username.setText(usernameString);
                        Toast.makeText(MainActivity.this, "You may have spent the rest of your life luck just seconds ago.", Toast.LENGTH_SHORT).show();
                    }
                    score.setText(String.format("Score: %s", currentScore));
                    break;
                default:
                    break;
            }

            return true;
        }
    };

    View.OnDragListener mainLayoutOnDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                importedImage.setImageBitmap(bitmap);
                importedImage.setVisibility(View.VISIBLE);
                // Locate the drop location and assign new coordinates to the image
                importedImage.setX(event.getX() - (float) importedImage.getHeight() / 2);
                importedImage.setY(event.getY() - (float) importedImage.getHeight() / 2);
            }
            return true;
        }
    };

    // Attaches image on the box when the user drop it on the View. The View may change background color based on the user drag and drop actions.
    View.OnDragListener dropboxOnDragListener = new View.OnDragListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onDrag(View v, DragEvent event) {
            long currentTimeMillisecond = Calendar.getInstance().getTimeInMillis();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundResource(R.color.light_yellow);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundResource(R.color.light_green);
                    break;
                case DragEvent.ACTION_DROP:
                    // If this is the correct box,
                    if (correctBoxId == v.getId()) {
                        v.setVisibility(View.INVISIBLE);
                        importedImage.setVisibility(View.VISIBLE);
                        importedImage.setImageBitmap(bitmap);
                        importedImage.setX(imageX);
                        importedImage.setY(imageY);

                        if (boxCount < DROP_BOXES_MAX_SIZE)
                            correctBoxId = getCorrectBoxId(randomIds[boxCount++]);
                        currentScore += (boxCount + importedImageCount) * scoreMultiplier;
                        score.setText("Score: " + currentScore);
                        break;
                    }
                    // x<10 -> f(x)>0; x>=10 -> f(x) <0
                    // (10 - x) * Multiplier
                    // 100 - x/100 & -(x*100)
                    currentScore += scoreMultiplier - (currentTimeMillisecond - startTimeMillisecond) / scoreMultiplier;
                    score.setText("Score: " + currentScore);
                    ((ImageView) v).setImageBitmap(bitmap);
                    importedImage.setImageBitmap(null);
                    break;
                default:
                    break;
            }
            return true;
        }
    };
}