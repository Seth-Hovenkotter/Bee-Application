package com.beeapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/*
Created by Seth Welch 2/6/2015-4/24/2015
Created by Seth Hovenkotter 2/6/2015-4/24/2015?
 */
public class FileChooser extends ListActivity {
	
    private File currentDir;
    private FileArrayAdapter adapter;
    private File toBeDeleted;

    //onCreate to start the app call this to run the application
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //file path of where you want the navigator to start
		currentDir = new File(Environment.getExternalStorageDirectory() + "/BeeApp/");
        //fill the screen with a list view of your current directory
        fill(currentDir);
        //list view lv is the list view we will be using for our visual in the app
        ListView lv = getListView();
        //enable the long click function for our list view lv
        lv.setLongClickable(true);
        //long click listener method needs to be implemented within on create due to the way it is included in activity library
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            //actual long click method that will delete items in the list after a dialog
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //final may not work
                //variable may need to be changed in order for delete to function properly
                final Option o = adapter.getItem(position);
                //dialog builder for making sure users want to delete the item they have selected
                Builder deleteDialog = new AlertDialog.Builder(FileChooser.this);
                deleteDialog.setTitle("Delete " + o.getName() + "?");
                deleteDialog.setMessage("Are you sure you want to delete this file?");
                //Yes option on dialog
                deleteDialog.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //may need to add a new variable to represent the filepath for the file that needs to be deleted
                        //should be finished after this
                        if (o.getData().equalsIgnoreCase("folder") || o.getData().equalsIgnoreCase("parent directory")) {
                            //update the file path of toBeDeleted so we delete the right folder
                            toBeDeleted = new File(o.getPath());
                            //delete it
                            deleteDir(toBeDeleted);
                            //refresh our directory
                            fill(currentDir);
                        } else {
                            //delete a single file in here
                            //the file path may be a bit strange due to our activity running on the SD card
                            toBeDeleted = new File(o.getPath());
                            toBeDeleted.delete();
                            //refresh our directory
                            fill(currentDir);
                        }
                    }
                });
                //No option from dialog
                deleteDialog.setNegativeButton("No", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                //make sure the dialog is visable
                deleteDialog.show();

                //this may need to be changed to false in some cases
                return true;
            }

        });
    }

    //fill method for taking the files located at the file path given and then moving them into an array list for display
    private void fill(File f)
    {
    	File[]dirs = f.listFiles();

         //Set our title to the name of our currently directory
		 this.setTitle("Current Dir: "+f.getName());
		 List<Option>dir = new ArrayList<Option>();
		 List<Option>fls = new ArrayList<Option>();
		 try{
			 for(File ff: dirs)
			 {
				if(ff.isDirectory())
					dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
				else
				{
					fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
				}
			 }
		 }catch(Exception e)
		 {
			 
		 }
		 Collections.sort(dir);
		 Collections.sort(fls);
		 dir.addAll(fls);

         //don't allow users to move above our root directory
		 if(!f.getName().equalsIgnoreCase("BeeApp"))
			 dir.add(0,new Option("..","Parent Directory",f.getParent()));

		 adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
		 this.setListAdapter(adapter);
    }

    //method to set our current directory to the option the user selects
    //do something when a list item is clicked
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		}
		else
		{
			onFileClick(o);
		}
	}

    //do something when a file is clicked
    private void onFileClick(Option o)
    {
    	Toast.makeText(this, "Press and hold the item to delete it", Toast.LENGTH_SHORT).show();
    }

    //delete a file or directory method to be called in ItemLongClick
    //delete a directory or folder recursively in here
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}