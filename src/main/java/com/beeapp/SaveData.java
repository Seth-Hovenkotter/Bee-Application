package com.beeapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Seth Welch on 2/22/2015.
 */
public class SaveData {
    private final Context mContext;
    private String folderName = "";

    public SaveData(Context context){
        mContext = context;
    }

    String createFolder(String name){
        folderName = name;
        String filepath = folderName;

        File myExternalFile = new File(Environment.getExternalStorageDirectory() + "/BeeApp/" + filepath + "/");
        myExternalFile.mkdirs();

        try {
            FileOutputStream fos = new FileOutputStream(myExternalFile);
            fos.write("Testing".getBytes());
            Log.e("Writing", "Reached end of writing");
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepath;
    }

    void createFile(String data, String type, String filepath) {

        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/BeeApp/" + filepath + "/" + type + ".txt");

            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);

            OutputStreamWriter out= new OutputStreamWriter(fOut);
            out.append(data);
            out.close();
            fOut.close();
        }

        catch (Throwable t) {
            Toast.makeText(mContext, "Saving Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //Creates the save file on the phone
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
            Toast.makeText(mContext, "Saving Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    List<String> getSavedColors(){
        List<String> items = null;

        //Bg, btn bg, btn text, panel bg, title bg, title text
        String defaultColors = "5,5,0,9,4,0";

        try {
            File myFile = new File(Environment.getExternalStorageDirectory() + "/BeeApp/Settings.txt");
            if(myFile.exists()) {
                InputStream in = new BufferedInputStream(new FileInputStream(myFile));
                if (in != null) {

                    InputStreamReader tmp = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();

                    while ((str = reader.readLine()) != null) {
                        buf.append(str);
                    }

                    in.close();

                    items = Arrays.asList(buf.toString().split("\\s*,\\s*"));
                }
                else {
                    createSaveFile(defaultColors);
                    getSavedColors();
                }
            }
            else{
                createSaveFile(defaultColors);
                getSavedColors();
            }
        }

        catch (java.io.FileNotFoundException e) {
            createSaveFile(defaultColors);
            getSavedColors();
        }

        catch (Throwable t) {
            //Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

        return items;
    }

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

    void zipFile(String filepath){

        try{
            FileOutputStream ZipFile = new FileOutputStream(Environment.getExternalStorageDirectory() + "/BeeApp/" + filepath + ".zip");
            ZipOutputStream ZipOutput = new ZipOutputStream(ZipFile);
            ZipEntry FileToZip = new ZipEntry(Environment.getExternalStorageDirectory() + "/BeeApp/" + filepath + "/");
            ZipOutput.putNextEntry(FileToZip);

            File src = new File(Environment.getExternalStorageDirectory() + "/BeeApp/" + filepath + "/");
            File[] files = src.listFiles();

            for (int i = 0; i < files.length; i++) {
                Log.d("", "Adding file: " + files[i].getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                ZipOutput.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    ZipOutput.write(buffer, 0, length);
                }
                ZipOutput.closeEntry();
                fis.close();
            }

            ZipOutput.close();

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
