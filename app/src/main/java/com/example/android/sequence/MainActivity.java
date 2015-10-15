package com.example.android.sequence;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private int level = 1;
    private int points = 0;
    private int mysteriousNumber;
    String solution;

    private Random random = new Random();
    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        generateSequence(level);
        startTimer();
        disableSoftKeyboard((EditText) findViewById(R.id.mysteriousNumber));

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    /* GENERATE */
    private void generateSequence(int level) {
        int[] numbers = new int[5];
        if (level < 3) {
            String sign = generateSign(level);
            int startNumber = generateStartNumber(level, sign);
            int x = generateX(level, sign);


            for (int i = 0; i < 5; i++) {
                numbers[i] = startNumber;
                startNumber = calculate(startNumber, sign, x);
            }

            setNumbers(numbers);
            solution = sign + x;
        } else {
            String sign = "*";
            int startNumber = generateStartNumber(level, sign);
            int x = generateX(level, sign);

            String sign2 = generateSign(1);
            int x2 = generateX(level, sign);

            for (int i = 0; i < 5; i++) {
                numbers[i] = startNumber;
                startNumber = calculate(calculate(startNumber, sign, x), sign2, x2);
            }

            setNumbers(numbers);
            solution = sign + x + sign2 + x2;


        }


    }

    private void setNumbers(int[] numbers) {
        ((TextView) findViewById(R.id.number1)).setText(String.valueOf(numbers[0]));
        ((TextView) findViewById(R.id.number2)).setText(String.valueOf(numbers[1]));
        ((TextView) findViewById(R.id.number4)).setText(String.valueOf(numbers[3]));
        ((TextView) findViewById(R.id.number5)).setText(String.valueOf(numbers[4]));

        mysteriousNumber = numbers[2];
    }

    private int generateStartNumber(int level, String sign) {
        int min;
        int max;
        switch (sign) {
            case "+":
            case "-":
                min = 0;
                max = 10;
                break;
            case "*":
            default:
                min = 1;
                max = 3;
        }
        return random.nextInt(max - min + 1) + min;

    }

    private int generateX(int level, String sign) {
        int min;
        int max;
        switch (sign) {
            case "+":
            case "-":
                min = 2;
                max = 10;
                break;
            case "*":
            default:
                min = -2;
                max = 4;
        }

        return random.nextInt(max - min + 1) + min;
    }

    private String generateSign(int level) {
        String[] signs = {"+", "-", "*", /*"^", "%"*/};
        switch(level) {
            case 1:
                return signs[random.nextInt(2)];
            case 2:
            default:
                return signs[random.nextInt(3)];
        }
    }

    private int calculate(int startNumber, String sign, int number) {
        int result;
        switch(sign) {
            case "+" :
                result = startNumber + number;
                break;
            case "-" :
                result = startNumber - number;
                break;
            case "*" :
                result = startNumber * number;
                break;
            case "^" :
                result = startNumber ^ number;
                break;
            default:
                result = 0;
        }
        return result;
    }

    /* CHECK */
    public void checkSequence(View view) {

        try {
            int guessedNumber = Integer.valueOf(((TextView) findViewById(R.id.mysteriousNumber)).getText().toString());
            boolean isCorrect = checkCorrectness(guessedNumber);

            updateStatus(isCorrect);
            if(isCorrect) {
                addPoints(level);
                showSolution();         //TODO: remove it!
                generateSequence(level);
                resetStatus();          //TODO: fade the status
                resetMysteriousNumber();
            }

        } catch (NumberFormatException e) {
            String text = getResources().getString(R.string.guessInfo);
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkCorrectness(int givenNumber) {
        return mysteriousNumber == givenNumber;
    }

    /*  RESET */
    public void resetGame(View view) {
        long time = (SystemClock.elapsedRealtime() - stopTimer()) / 1000;
        saveRecord(points, time);
        resetLevel();
        resetStatus();
        resetPoints();
        resetMysteriousNumber();
        generateSequence(level);
        startTimer();
    }

    /* STATUS */
    private void updateStatus(boolean isCorrect) {
        ColorStateList statusColor;
        String statusText;


        if (isCorrect) {
            statusColor = getResources().getColorStateList(R.color.correct);
            TypedArray correctStatuses = getResources().obtainTypedArray(R.array.correctStatuses);
            statusText = correctStatuses.getString(random.nextInt(correctStatuses.length()));
        }
        else {
            statusColor = getResources().getColorStateList(R.color.incorrect);
            TypedArray incorrectStatuses = getResources().obtainTypedArray(R.array.incorrectStatuses);
            statusText = incorrectStatuses.getString(random.nextInt(incorrectStatuses.length()));
        }

        TextView status = (TextView) findViewById(R.id.status);
        status.setTextColor(statusColor);
        status.setText(statusText + mysteriousNumber);
        status.setVisibility(View.VISIBLE);
    }

    private void resetStatus() {
        TextView status = (TextView) findViewById(R.id.status);
        status.setText("");
        status.setVisibility(View.INVISIBLE);
    }

    /* POINTS */
    private void addPoints(int points) {
        this.points = this.points + points;
        updatePoints(this.points);
        updateLevel();
    }

    private void updatePoints(int points) {
        ((TextView) findViewById(R.id.points)).setText(String.valueOf(points));
    }

    private void resetPoints() {
        points = 0;
        updatePoints(points);
    }

    /* LEVEL */
    private void updateLevel() {
        if(points >= level * level * 10) {
            level++;
        }
        ((TextView) findViewById(R.id.level)).setText(String.valueOf(level));
    }

    private void resetLevel() {
        level = 1;
        ((TextView) findViewById(R.id.level)).setText(String.valueOf(level));
    }

    /* TIMER */
    private void startTimer() {
        Chronometer timer = ((Chronometer) findViewById(R.id.timer));
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    private long stopTimer() {
        ((Chronometer) findViewById(R.id.timer)).stop();
        return ((Chronometer) findViewById(R.id.timer)).getBase();
    }

    /* RECORD */
    private void saveRecord(int points, long time) {
        if (isRecord(points, time)) {
            String text = "Your new record is: " + points + " " + time / 60 + ":" + time % 60;
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isRecord(int currentPoints, long currentTime) {
        //TODO: check if it really is a record - compare with last record(points and time)
        return true;
    }

    /* CUSTOM KEYBOARD */
    public void writeNumber(View view) {
        String number = ((Button) view).getText().toString();
        EditText mysteriousNumberView = ((EditText) findViewById(R.id.mysteriousNumber));
        String currentText = mysteriousNumberView.getText().toString();

        if (currentText.length() < getResources().getInteger(R.integer.maxLength)) {
            if (number.equals("-")) {
                if (!currentText.isEmpty())
                    return;
            }
            if (number.equals("0")) {
                if (currentText.isEmpty() || currentText.equals("-"))
                    return;
            }
            currentText = currentText + number;
            mysteriousNumberView.setText(currentText);
            mysteriousNumberView.setSelection(currentText.length());
            v.vibrate(50);
        }
    }

    public void deleteNumber(View view) {
        EditText mysteriousNumberView = ((EditText) findViewById(R.id.mysteriousNumber));
        String currentText = mysteriousNumberView.getText().toString();

        if (!currentText.isEmpty()) {
            currentText = currentText.substring(0, currentText.length() - 1);
            mysteriousNumberView.setText(currentText);
            mysteriousNumberView.setSelection(currentText.length());
            v.vibrate(50);
        }

    }

    /* */
    private void resetMysteriousNumber() {
        ((TextView) findViewById(R.id.mysteriousNumber)).setText("");
    }


    private void addMysteriousSolutionListener() {
        TextView pointsView = (TextView) findViewById(R.id.points);

    }

    public void showSolution() {
        Toast.makeText(this, solution, Toast.LENGTH_SHORT).show();
        //addPoints(-level);  //TODO: add it if it is possible to see solution
    }

    public void disableSoftKeyboard(EditText et) {
        //EditText et = (EditText) findViewById(R.id.mysteriousNumber);
        et.setInputType(InputType.TYPE_NULL);
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
            et.setTextIsSelectable(true);
        }
    }

    /* MENU */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            Log.d(this.getLocalClassName(), getResources().getString(R.string.instruction));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.instruction).setTitle("Sequence");
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
