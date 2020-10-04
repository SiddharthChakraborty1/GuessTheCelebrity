package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UTFDataFormatException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    String[] answers = new String[4];
    int locationOfCorrectAnswer = 0;
    ImageView imageView;
    int chosenCeleb = 0;
    Button b1,b2,b3,b4;

    public class ImageDownloader extends AsyncTask<String,Void, Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap myBitmap = null;

            try
            {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(in);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return myBitmap;






        }
    }



    public class DownloadTask extends AsyncTask<String,Void,String>
    {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection = null;
            String result = "";
            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data !=-1)
                {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }



            }
            catch (Exception e)
            {
                e.printStackTrace();
                result = "failed";
            }

            return result;

        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        imageView = (ImageView) findViewById(R.id.imageView);
        b1 = (Button) findViewById(R.id.btn1);
        b2 = (Button) findViewById(R.id.btn2);
        b3 = (Button) findViewById(R.id.btn3);
        b4 = (Button) findViewById(R.id.btn4);

        String result = null;
        try {

            result = task.execute("https://web.archive.org/web/20190119082828/www.posh24.se/kandisar").get();
            String[] splitResult = result.split("<div class=\"listedArticle\">");

            Pattern pattern = Pattern.compile("img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);

            while (matcher.find())
            {
                celebURLs.add(matcher.group(1));
            }





            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);

            while (matcher.find())
            {
                celebNames.add(matcher.group(1));
            }


            newQuestion();



        }
        catch (Exception e)
        {
            e.printStackTrace();


        }

    }

    public void celebChosen(View view)
    {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer)))
        {
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
            newQuestion();
        }
        else {
            Toast.makeText(getApplicationContext(), "Wrong, it was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

    }

    public void newQuestion()
    {

        try {

            Random rand = new Random();
            chosenCeleb = rand.nextInt(celebURLs.size());
            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = null;

            celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);

            int incorrectAnswerLocation;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = rand.nextInt(celebNames.size());

                    while (incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt(celebNames.size());

                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            b1.setText(answers[0]);
            b2.setText(answers[1]);
            b3.setText(answers[2]);
            b4.setText(answers[3]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}