/* 
* A ImageJ macro extensions to be called from ImageJ macro
* Some utility type of functions for writing APEER codes
* 
* MIT License
* Copyright (c) 2020 Kota Miura
*/

package com.apeer.imagej.util;

import ij.IJ;
import ij.ImageJ;
import ij.macro.ExtensionDescriptor;
import ij.macro.Functions;
import ij.macro.MacroExtension;
import ij.plugin.PlugIn;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.HashMap;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/*
 * APEER ImageJ Macro Extension
 * Macro extension commands for implementing APEER modules with ImageJ Macro.
 *  
 * @author Kota Miura
 */
public class Apeer_MacroExt implements PlugIn, MacroExtension {

    private static HashMap<String, String> jsonmap;
    private static String WFE_input_file = "";
//    private static String WFE_output_file = "";

	@Override
	public void run(final String arg) {
		if (!IJ.macroRunning()) {
			IJ.error("Cannot install extensions from outside a macro!");
			return;
		}

		Functions.registerExtensions(this);
	}

	private final ExtensionDescriptor[] extensions = {
		ExtensionDescriptor.newDescriptor("setWFE_Input_FilePath", this, ARG_STRING),
//		ExtensionDescriptor.newDescriptor("setWFE_Output_FilePath", this, ARG_STRING),
		ExtensionDescriptor.newDescriptor("initializeJSON_out", this),
		ExtensionDescriptor.newDescriptor("currentTime", this, ARG_OUTPUT + ARG_STRING),
		ExtensionDescriptor.newDescriptor("captureWFE_JSON", this, ARG_OUTPUT + ARG_STRING),
	    ExtensionDescriptor.newDescriptor("getWFEvalue", this, ARG_STRING, ARG_OUTPUT + ARG_STRING),
	    ExtensionDescriptor.newDescriptor("saveTiffAPEER", this, ARG_STRING, ARG_STRING),
	    ExtensionDescriptor.newDescriptor("saveResultsAPEER", this, ARG_STRING, ARG_STRING),
		ExtensionDescriptor.newDescriptor("saveJSON_OUT", this, ARG_STRING),
		ExtensionDescriptor.newDescriptor("callLog", this, ARG_STRING), 
		ExtensionDescriptor.newDescriptor("test2strings", this, ARG_STRING, ARG_STRING),};
	
    @Override
	public ExtensionDescriptor[] getExtensionFunctions() {
		return extensions;
	}

	@Override
	public String handleExtension(final String name, final Object[] args) {

		if (name.equals("callLog")) {
			String logtext = (String) args[0];
			System.out.println("[LOG]: " + logtext);
		}
		else if (name.equals("currentTime")) {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//System.out.println(timestamp);
			((String[]) args[0])[0] = (String) timestamp.toString();
		}
        else if (name.equals("setWFE_Input_FilePath")) {
            String filepath = (String) args[0];
            WFE_input_file = filepath;
            System.out.println( "WFE input file: set to " +  WFE_input_file );
        }
//        else if (name.equals("setWFE_Output_FilePath")) {
//            String filepath = (String) args[0];
//            WFE_output_file = filepath;
//            System.out.println( "WFE output file: set to " +  WFE_output_file );
//        }
        else if (name.equals("initializeJSON_out")) {
            initializeJSONmap();
            System.out.println( "JSON_out initialized...");
        }		
		else if (name.equals("captureWFE_JSON")) {
			String WFE_JSON = "";
			File wfef = new File(WFE_input_file);
			if ( !wfef.exists() ){
				WFE_JSON = java.lang.System.getenv("WFE_INPUT_JSON");
			} else {
				try {
                    WFE_JSON = new String(Files.readAllBytes(Paths.get( WFE_input_file )));
                } catch (IOException e) {
                    e.printStackTrace();
                }
			}		
			//System.out.println( WFE_JSON );
			((String[]) args[0])[0] = (String) WFE_JSON;
		}
        else if (name.equals("getWFEvalue")) {
            String WFE_JSON = "";
            File wfef = new File(WFE_input_file);
            if ( !wfef.exists() ){
                System.out.println( "Loading input parameters from environment..." );
                WFE_JSON = java.lang.System.getenv("WFE_INPUT_JSON");
            } else {
                System.out.println( "Loading input parameters from local file..." );
                try {
                    WFE_JSON = new String(Files.readAllBytes(Paths.get( WFE_input_file )));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }       
            //System.out.println( WFE_JSON );
            if (WFE_JSON != ""){
                String key = (String) args[0];
                JSONObject jo = new JSONObject( WFE_JSON );
                String val = jo.getString( key );
                ((String[]) args[1])[0] = (String) val;
            } else {
                ((String[]) args[1])[0] = "None";
            }
        }
        else if (name.equals("saveTiffAPEER")) {
            String labelstring = (String) args[0];
            String pathstring = (String) args[1];
            saveTiffjson( labelstring, pathstring);
            System.out.println("Saved Tiff...");
        }
        else if (name.equals("saveResultsAPEER")) {
            String labelstring = (String) args[0];
            String pathstring = (String) args[1];
            saveResultsJson( labelstring, pathstring);
            System.out.println("Saved Results...");
        }   		
        else if (name.equals("saveJSON_OUT")) {
            String pathstring = (String) args[0];           
            Path path = Paths.get( pathstring );
            JSONObject jo = new JSONObject( jsonmap );
            try ( BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8")) ){               
                writer.write( jo.toString(2) );
            }catch(IOException ex){
                ex.printStackTrace();
            }            
            System.out.println("JSON out written...");
        }
        else if (name.equals("test2strings")) {
            String text = (String) args[0];
            String text1 = (String) args[1];
            System.out.println("[LOG0]: " + text);
            System.out.println("[LOG1]: " + text1);
        }		
		return null;
	}
	void initializeJSONmap(){
	    jsonmap = new HashMap<String, String>();
	}
	
	void saveTiffjson(String labelName, String path){
	    IJ.saveAs("Tiff", path);
	    if ( jsonmap == null )
	        initializeJSONmap();
	    jsonmap.put(labelName, path);
	}
	
	//save results as CSV file if the path ends with .csv
    void saveResultsJson(String labelName, String path){
        IJ.saveAs("Results", path);
        if ( jsonmap == null )
            initializeJSONmap();
        jsonmap.put(labelName, path);
    }	


// 	public static void main(String[] args) throws Exception {
// 		// set the plugins.dir property to make the plugin appear in the Plugins menu
// 		// see: https://stackoverflow.com/a/7060464/1207769
// 		Class<?> clazz = Apeer_MacroExt.class;
// 		java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
// 		java.io.File file = new java.io.File(url.toURI());
// 		System.setProperty("plugins.dir", file.getAbsolutePath());
// 
// 		// start ImageJ
// 		new ImageJ();
// 
// 		// open the Clown sample
// 		ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
// 		image.show();
// 
// 		// run the plugin
// 		IJ.runPlugIn(clazz.getName(), "");
// 	}
}
