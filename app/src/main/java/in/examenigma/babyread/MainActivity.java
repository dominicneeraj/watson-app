/**
 * Copyright IBM Corporation 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package in.examenigma.babyread;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.Language;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import in.examenigma.babyread.support.StreamPlayer;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private RadioGroup targetLanguage;
    private EditText input;
    private ImageButton mic;
    private Button translate;
    private ImageButton play;
    private TextView translatedText;
    private Button gallery;
    private Button camera;
    private ImageView loadedImage;

    private SpeechToText speechService;
    private TextToSpeech textService;
    private LanguageTranslation translationService;
    private Language selectedTargetLanguage = Language.ENGLISH;

    private StreamPlayer player = new StreamPlayer();


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        speechService = initSpeechToTextService();
        textService = initTextToSpeechService();
        translationService = initLanguageTranslationService();

        targetLanguage = (RadioGroup) findViewById(R.id.target_language);
        input = (EditText) findViewById(R.id.input);

        translate = (Button) findViewById(R.id.translate);
        play = (ImageButton) findViewById(R.id.play);
        translatedText = (TextView) findViewById(R.id.translated_text);

        targetLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.spanish:
                        selectedTargetLanguage = Language.SPANISH;
                        break;
                    case R.id.french:
                        selectedTargetLanguage = Language.FRENCH;
                        break;
                    case R.id.italian:
                        selectedTargetLanguage = Language.ITALIAN;
                        break;
                }
            }
        });

        input.addTextChangedListener(new EmptyTextWatcher() {
            @Override public void onEmpty(boolean empty) {
                if (empty) {
                    translate.setEnabled(false);
                } else {
                    translate.setEnabled(true);
                }
            }
        });



        translate.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick(View v) {
                new TranslationTask().execute(input.getText().toString());
            }
        });

        translatedText.addTextChangedListener(new EmptyTextWatcher() {
            @Override public void onEmpty(boolean empty) {
                if (empty) {
                    play.setEnabled(false);
                } else {
                    play.setEnabled(true);
                }
            }
        });

        play.setEnabled(false);

        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new SynthesisTask().execute(translatedText.getText().toString());
            }
        });




    }


    private void showTranslation(final String translation) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                translatedText.setText(translation);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }





    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username = getString(R.string.speech_text_username);
        String password = getString(R.string.speech_text_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint("https://stream.watsonplatform.net/speech-to-text/api");
        return service;
    }

    private TextToSpeech initTextToSpeechService() {
        TextToSpeech service = new TextToSpeech();
        String username = getString(R.string.text_speech_username);
        String password = getString(R.string.text_speech_password);
        service.setUsernameAndPassword(username, password);
        return service;
    }

    private LanguageTranslation initLanguageTranslationService() {
        LanguageTranslation service = new LanguageTranslation();
        String username = getString(R.string.language_translation_username);
        String password = getString(R.string.language_translation_password);
        service.setUsernameAndPassword(username, password);
        return service;
    }



    private abstract class EmptyTextWatcher implements TextWatcher {
        private boolean isEmpty = true; // assumes text is initially empty

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                isEmpty = true;
                onEmpty(true);
            } else if (isEmpty) {
                isEmpty = false;
                onEmpty(false);
            }
        }

        @Override public void afterTextChanged(Editable s) {}

        public abstract void onEmpty(boolean empty);
    }


    private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override protected String doInBackground(String... params) {
            showTranslation(translationService.translate(params[0], Language.ENGLISH, selectedTargetLanguage).getFirstTranslation());
            return "Did translate";
        }
    }

    private class SynthesisTask extends AsyncTask<String, Void, String> {

        @Override protected String doInBackground(String... params) {
            player.playStream(textService.synthesize(params[0], Voice.EN_LISA));
            return "Did syntesize";
        }
    }



}
