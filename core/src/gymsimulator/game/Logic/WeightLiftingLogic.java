package gymsimulator.game.Logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.util.Random;


/**
 * Created by Tiago on 31/05/2016.
 */
public class WeightLiftingLogic implements Input.TextInputListener {

    Preferences prefs;

    public static final long[] loseVibratePattern = new long[] {0, 100, 30, 100};
    public int trace_x;
    public int trace_y;
    public boolean endGame=false;
    public int changeBarDirection;
    public int statusBarMaxX;
    public int statusBarMinX;
    public int statusGreenBarMinX;
    public int statusGreenBarMaxX;
    public int score;
    public boolean incScore;
    public int timer;
    public int liftTimer;
    public boolean startTimer;
    public int highscoreLifting = 0;
    public boolean scoresSaved = false;
    private float traceSpeed;
    public boolean lifted = false;
    public int leftArmSize=200;
    public int rightArmSize=200;
    public float weightRotation=0;
    public boolean gameStart=false;
    public boolean saveScores=true;
    public String userName="";
    private FileHandle file;



    public WeightLiftingLogic()
    {
        prefs = Gdx.app.getPreferences("GymHighScores");
        highscoreLifting = prefs.getInteger("highscoreWeight");


        changeBarDirection = 1;
        statusBarMaxX=((Gdx.graphics.getWidth()/4) * 3)-25;
        statusBarMinX=(Gdx.graphics.getWidth()/4)+ 5;
        statusGreenBarMinX =  (Gdx.graphics.getWidth()/2)-(Gdx.graphics.getWidth()/16);
        statusGreenBarMaxX = (Gdx.graphics.getWidth()/2)+(Gdx.graphics.getWidth()/16);

        Random rand = new Random();

        trace_x = statusGreenBarMaxX - (statusGreenBarMaxX - statusGreenBarMinX)/2 ;
        trace_y = Gdx.graphics.getHeight()-Gdx.graphics.getHeight()/6;

        score = 0;
        incScore = false;
        timer=8*100;
        liftTimer = 150;
        startTimer= false;
    }

    @Override
    public void input (String text) {
        this.userName=text;
    }

    @Override
    public void canceled () {

    }
    public int update(float delta) {

        if(!endGame && gameStart) {
            if(trace_x-(Gdx.graphics.getWidth()/4)+ 5<statusBarMaxX/2)
                weightRotation=trace_x/100;
            else
                weightRotation=-trace_x/100;
            if (!lifted) {
                endGame = lift(delta);
                if(endGame)
                    saveScore();
            }
            else
                lifted = wait(delta);
        }

        return 0;
    }

    public boolean wait(float delta){

        liftTimer -= delta;
        if(liftTimer <= 0) {

            liftTimer = 150 - 4 * score;
            if(liftTimer < 80)
                liftTimer = 80;
            Random rand = new Random();
            trace_x = rand.nextInt(statusBarMaxX - statusBarMinX + 1) + statusBarMinX;
            return false;
        }
        return true;
    }

    public boolean lift(float delta){
        if (traceSpeed > 0)
            traceSpeed -= 0.5;
        else if (traceSpeed < 0)
            traceSpeed += 0.5;

        Random rand = new Random();

        int randAcc = rand.nextInt(9);
        switch (randAcc) {
            case 1:
                traceSpeed += 0.2;
                break;
            case 2:
                traceSpeed += 0.4;
                break;
            case 3:
                traceSpeed += -0.2;
                break;
            case 4:
                traceSpeed += -0.4;
                break;
            default:
                traceSpeed += 0;
                break;
        }

        traceSpeed += calcAcceleration();

        trace_x += traceSpeed;

        if (trace_x > statusBarMaxX) {
            trace_x = statusBarMaxX;
            traceSpeed = 1;
        }
        if (trace_x < statusBarMinX) {
            trace_x = statusBarMinX;
            traceSpeed = -1;
        }

        liftTimer -= delta;

        if (liftTimer <= 0) {
            if (trace_x > statusGreenBarMinX && trace_x < statusGreenBarMaxX) {
                Gdx.input.vibrate(75);
                lifted = true;
                liftTimer = 40;
                score++;

            } else {
                Gdx.input.vibrate(loseVibratePattern, -1);
                return true;
            }
        }
        return false;
    }

    public double calcAcceleration(){

        double  mult = (double)score / 9 + 0.2;

        double acc = ((Gdx.input.getAccelerometerY()) * mult);

        return acc;
    }

    public void saveScore()
    {
            if(score > highscoreLifting && saveScores) {
                //Gdx.input.getTextInput(this, "Name", " ", "InsertYourName");
                prefs.putInteger("highscoreWeight", score);
                prefs.flush();
                saveScores=false;
                highscoreLifting=score;
                saveToFile(score);
            }
    }

    public void saveToFile(int score){
        String filename;
        filename = "weightHighScores.txt";
        file = Gdx.files.local(filename);

        if(file.exists()){
            file.writeString(((Integer)(score)).toString(), false);
            }
        else {
            try {
                file.file().createNewFile();
                file.writeString(((Integer)(score)).toString(), false);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
