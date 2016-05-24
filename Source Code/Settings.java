package firestormsoftwarestudio.suppression;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Blake on 4/19/2016.
 */
public class Settings extends Activity implements OnClickListener {

    public Button startCustomGameBtn;
    public Button leaveSettingsBtn;
    public SeekBar aiSettingBar;
    public TextView aiDescriptionText;
    public SeekBar startingPiecesBar;
    public TextView startingPiecesText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        startCustomGameBtn = (Button) findViewById(R.id.startCustomGameBtn);
        leaveSettingsBtn = (Button) findViewById(R.id.leaveSettingsBtn);

        startCustomGameBtn.setOnClickListener(this);
        leaveSettingsBtn.setOnClickListener(this);

        aiSettingBar = (SeekBar) findViewById(R.id.aiSettingBar);
        aiDescriptionText = (TextView) findViewById(R.id.aiDescriptionText);

        aiSettingBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    aiDescriptionText.setText(getResources().getText(R.string.cautious));
                } else if (progress == 1) {
                    aiDescriptionText.setText(getResources().getText(R.string.strategic));
                } else if (progress == 2) {
                    aiDescriptionText.setText(getResources().getText(R.string.aggressive));
                } else if (progress == 3) {
                    aiDescriptionText.setText(getResources().getText(R.string.reckless));
                } else if (progress == 4) {
                    aiDescriptionText.setText(getResources().getText(R.string.pacifist));
                } else {
                    aiDescriptionText.setText(getResources().getText(R.string.confused));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        startingPiecesBar = (SeekBar) findViewById(R.id.startingPiecesBar);
        startingPiecesText = (TextView) findViewById(R.id.startingPiecesText);

        startingPiecesBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    startingPiecesText.setText(getResources().getText(R.string.one));
                } else {
                    startingPiecesText.setText(getResources().getText(R.string.two));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.startCustomGameBtn:

                SharedPreferences settings = getSharedPreferences(SuppressionToDo.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                Intent i = new Intent(this, SuppressionToDo.class);

                // store settings in shared preferences
                editor.putInt("AISetting", aiSettingBar.getProgress());
                editor.putInt("StartingPieces", (startingPiecesBar.getProgress() + 1));
                editor.putString("save", "");

                editor.commit();
                // have the Game class play the sound effect when it begins.  Easier this way.

                startActivity(i);
                break;
            case R.id.leaveSettingsBtn:
                Intent j = new Intent(this, Menu.class);
                j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(j);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Music.stop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Music.play(this, R.raw.timefluxmenu);
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
        Intent i = new Intent(this, Menu.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
