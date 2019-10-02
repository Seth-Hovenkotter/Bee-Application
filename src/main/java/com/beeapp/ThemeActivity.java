package com.beeapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by Seth on 4/24/2015.
 */

public class ThemeActivity extends ActionBarActivity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_view);

        //Fill all dropdown boxes with the color options
        fillDropDowns();

        setColors(1);

        spinnerListeners();
    }

    //What happens when back is pressed
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    //Fill all of the drop downs with their colors
    private void fillDropDowns(){
        //Get all of the dropdown boxes
        Spinner spinBg = (Spinner) findViewById(R.id.ddBackground);
        Spinner spinBtnBg = (Spinner) findViewById(R.id.ddBtnBackground);
        Spinner spinBtnText = (Spinner) findViewById(R.id.ddBtnText);
        Spinner spinPanelBg = (Spinner) findViewById(R.id.ddPanelBackground);
        Spinner spinTitleBg = (Spinner) findViewById(R.id.ddTitleBg);
        Spinner spinTitleText = (Spinner) findViewById(R.id.ddTitleText);

        //Fill String array with the colors
        String[] items = new String[]{"Black", "Blue", "Brown", "Dark Grey", "Light Blue", "Light Grey", "Orange", "Pink", "Red", "White", "Yellow"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        //Add string array to dropdown
        spinBg.setAdapter(adapter);
        spinBtnBg.setAdapter(adapter);
        spinBtnText.setAdapter(adapter);
        spinPanelBg.setAdapter(adapter);
        spinTitleBg.setAdapter(adapter);
        spinTitleText.setAdapter(adapter);
    }

    //Listener for the save button, confirms and saves all color changes
    public void saving(View view){
        final View thisView = view;
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Save Colors");
        dialog.setMessage("Are you sure you want to save these colors?");

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String colors = getColors();
                createSaveFile(colors);
                onBackPressed();
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        });

        dialog.show();
    }

    //Listener for the default button, confirms and returns all colors to original
    public void defaultSettings (View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Default Colors");
        dialog.setMessage("Are you sure you want to return all colors to the default?");

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String defaultColors = "5,5,0,9,4,0";
                createSaveFile(defaultColors);
                setColors(1);
                onBackPressed();
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        });

        dialog.show();
    }

    //Returns all of the selected colors from the dropdowns
    String getColors(){
        //Get all of the dropdown boxes
        Spinner spinBg = (Spinner) findViewById(R.id.ddBackground);
        Spinner spinBtnBg = (Spinner) findViewById(R.id.ddBtnBackground);
        Spinner spinBtnText = (Spinner) findViewById(R.id.ddBtnText);
        Spinner spinPanelBg = (Spinner) findViewById(R.id.ddPanelBackground);
        Spinner spinTitleBg = (Spinner) findViewById(R.id.ddTitleBg);
        Spinner spinTitleText = (Spinner) findViewById(R.id.ddTitleText);

        int first = spinBg.getSelectedItemPosition();
        int second = spinBtnBg.getSelectedItemPosition();
        int third = spinBtnText.getSelectedItemPosition();
        int fourth = spinPanelBg.getSelectedItemPosition();
        int fifth = spinTitleBg.getSelectedItemPosition();
        int sixth = spinTitleText.getSelectedItemPosition();

        String answer = "" + first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth;

        return answer;
    }

    //Gives the hexadecimal colors
    String getColor(int i){
        if(i == 0){
            //Black
            return "#000000";
        }
        else if(i == 1){
            //Blue
            return "#0000FF";
        }
        else if(i == 2){
            //Brown
            return "#694016";
        }
        else if(i == 3){
            //Dark Grey
            return "#E0E0E0";
        }
        else if(i == 4){
            //Light Blue
            return "#6e8ffcff";
        }
        else if(i == 5){
            //Light Grey
            return "#f1f1f1";
        }
        else if(i == 6){
            //Orange
            return "#FFCC66";
        }
        else if(i == 7){
            //Pink
            return "#FF99FF";
        }
        else if(i == 8){
            //Red
            return "#FF0000";
        }
        else if(i == 9){
            //White
            return "#FFFFFF";
        }
        else{
            //Yellow
            return "#FFFF19";
        }
    }

    //Creates the save file on the phone, added this here for the onclick listener
    void createSaveFile(String colors) {
        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/BeeApp/Settings.txt");

            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);

            OutputStreamWriter out= new OutputStreamWriter(fOut);
            out.append(colors);
            out.close();
            fOut.close();
        }

        catch (Throwable t) {
            Toast.makeText(this, "Saving Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    void spinnerListeners(){
        //Get all of the dropdown boxes
        final Spinner spinBg = (Spinner) findViewById(R.id.ddBackground);
        final Spinner spinBtnBg = (Spinner) findViewById(R.id.ddBtnBackground);
        final Spinner spinBtnText = (Spinner) findViewById(R.id.ddBtnText);
        final Spinner spinPanelBg = (Spinner) findViewById(R.id.ddPanelBackground);
        final Spinner spinTitleBg = (Spinner) findViewById(R.id.ddTitleBg);
        final Spinner spinTitleText = (Spinner) findViewById(R.id.ddTitleText);

        spinBg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setColors(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //Do Nothing
            }
        });

        spinBtnBg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setColors(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //Do Nothing
            }
        });

        spinBtnText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setColors(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //Do Nothing
            }
        });

        spinPanelBg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setColors(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //Do Nothing
            }
        });

        spinTitleBg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setColors(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //Do Nothing
            }
        });

        spinTitleText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setColors(0);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                //Do Nothing
            }
        });
    }

    void setColors(int i){
        //Get all of the dropdown boxes
        final Spinner spinBg = (Spinner) findViewById(R.id.ddBackground);
        final Spinner spinBtnBg = (Spinner) findViewById(R.id.ddBtnBackground);
        final Spinner spinBtnText = (Spinner) findViewById(R.id.ddBtnText);
        final Spinner spinPanelBg = (Spinner) findViewById(R.id.ddPanelBackground);
        final Spinner spinTitleBg = (Spinner) findViewById(R.id.ddTitleBg);
        final Spinner spinTitleText = (Spinner) findViewById(R.id.ddTitleText);

        TextView txt = (TextView) findViewById(R.id.textBg);
        TextView txt2 = (TextView) findViewById(R.id.btnBg);
        TextView txt3 = (TextView) findViewById(R.id.btnText);
        TextView txt4 = (TextView) findViewById(R.id.textPanelBg);
        TextView txt5 = (TextView) findViewById(R.id.textTitleBg);
        TextView txt6 = (TextView) findViewById(R.id.textTitleText);

        if(i == 1){
            //Create new instance of save
            SaveData save = new SaveData(this);

            //Get any saved colors, otherwise get default colors
            List<String> colors = save.getSavedColors();

            //Set their default selected values
            spinBg.setSelection(Integer.parseInt(colors.get(0)));
            spinBtnBg.setSelection(Integer.parseInt(colors.get(1)));
            spinBtnText.setSelection(Integer.parseInt(colors.get(2)));
            spinPanelBg.setSelection(Integer.parseInt(colors.get(3)));
            spinTitleBg.setSelection(Integer.parseInt(colors.get(4)));
            spinTitleText.setSelection(Integer.parseInt(colors.get(5)));
        }

        int color = spinBg.getSelectedItemPosition();
        String hexColor = getColor(color);
        txt.setTextColor(Color.parseColor(hexColor));

        int color2 = spinBtnBg.getSelectedItemPosition();
        String hexColor2 = getColor(color2);
        txt2.setTextColor(Color.parseColor(hexColor2));

        int color3 = spinBtnText.getSelectedItemPosition();
        String hexColor3 = getColor(color3);
        txt3.setTextColor(Color.parseColor(hexColor3));

        int color4 = spinPanelBg.getSelectedItemPosition();
        String hexColor4 = getColor(color4);
        txt4.setTextColor(Color.parseColor(hexColor4));

        int color5 = spinTitleBg.getSelectedItemPosition();
        String hexColor5 = getColor(color5);
        txt5.setTextColor(Color.parseColor(hexColor5));

        int color6 = spinTitleText.getSelectedItemPosition();
        String hexColor6 = getColor(color6);
        txt6.setTextColor(Color.parseColor(hexColor6));
    }
}
