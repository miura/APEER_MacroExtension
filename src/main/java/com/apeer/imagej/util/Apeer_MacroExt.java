/* 
* A ImageJ macro extensions to be called from ImageJ macro
* Some utility type of functions for writing APEER codes
* 
* MIT License
* Copyright (c) 2020 Kota Miura
*/

package com.apeer.imagej.util;

import ij.IJ;
import ij.WindowManager;
import ij.macro.ExtensionDescriptor;
import ij.macro.Functions;
import ij.macro.MacroExtension;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.text.TextWindow;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import java.awt.Frame;
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
//		ExtensionDescriptor.newDescriptor("currentTime", this, ARG_OUTPUT + ARG_STRING),
        ExtensionDescriptor.newDescriptor("currentTime", this),
//		ExtensionDescriptor.newDescriptor("captureWFE_JSON", this, ARG_OUTPUT + ARG_STRING),
        ExtensionDescriptor.newDescriptor("captureWFE_JSON", this),		
        ExtensionDescriptor.newDescriptor("getValue", this, ARG_STRING),		
	    ExtensionDescriptor.newDescriptor("getWFEvalue", this, ARG_STRING, ARG_OUTPUT + ARG_STRING),
        ExtensionDescriptor.newDescriptor("getWFEvalueBoolean", this, ARG_STRING),        	    
	    ExtensionDescriptor.newDescriptor("saveTiffAPEER", this, ARG_STRING, ARG_STRING),
	    ExtensionDescriptor.newDescriptor("saveResultsAPEER", this, ARG_STRING, ARG_STRING),
	    ExtensionDescriptor.newDescriptor("saveTableAPEER", this, ARG_STRING, ARG_STRING, ARG_STRING),
	    ExtensionDescriptor.newDescriptor("saveAsAPEER", this, ARG_STRING, ARG_STRING, ARG_STRING),
	    ExtensionDescriptor.newDescriptor("saveStringAPEER", this, ARG_STRING, ARG_STRING, ARG_STRING),
		ExtensionDescriptor.newDescriptor("saveJSON_OUT", this, ARG_STRING),
//		ExtensionDescriptor.newDescriptor("checkSaveJSON_OUT", this, ARG_STRING),
		ExtensionDescriptor.newDescriptor("shout", this, ARG_STRING), 
		ExtensionDescriptor.newDescriptor("exit", this), 
		ExtensionDescriptor.newDescriptor("test2strings", this, ARG_STRING, ARG_STRING),};
	
    @Override
	public ExtensionDescriptor[] getExtensionFunctions() {
		return extensions;
	}

	@Override
	public String handleExtension(final String name, final Object[] args) {

		if (name.equals("shout")) {
			String logtext = (String) args[0];
			System.out.println("[MACRO LOG]: " + logtext);
		}
		else if (name.equals("currentTime")) {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//System.out.println(timestamp);
			//((String[]) args[0])[0] = (String) timestamp.toString();
			return timestamp.toString();
		}
        else if (name.equals("setWFE_Input_FilePath")) {
            String filepath = (String) args[0];
            WFE_input_file = filepath;
            System.out.println( "[plugin] WFE input file: set to " +  WFE_input_file );
        }
//        else if (name.equals("setWFE_Output_FilePath")) {
//            String filepath = (String) args[0];
//            WFE_output_file = filepath;
//            System.out.println( "WFE output file: set to " +  WFE_output_file );
//        }
        else if (name.equals("initializeJSON_out")) {
            initializeJSONmap();
            System.out.println( "[plugin] JSON_out initialized...");
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
			//((String[]) args[0])[0] = (String) WFE_JSON;
			return (String) WFE_JSON;
		}
        else if (name.equals("getValue")) {
            String WFE_JSON = "";
            File wfef = new File(WFE_input_file);
            if ( !wfef.exists() ){
                System.out.println( "[plugin] Loading input parameters from environment" );
                WFE_JSON = java.lang.System.getenv("WFE_INPUT_JSON");
            } else {
                System.out.println( "[plugin] Loading input parameters from local file" );
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
                String val = "";
                if (jo.get(key) instanceof Boolean){
                    boolean bval = jo.getBoolean(key);
                    if ( bval ) 
                        val = "true";
                    else
                        val = "false";
                } else 
                    val = jo.getString( key );
                
                // dealing with boolean values. 
                // even if the values are in String, 1 and 0 are considered as true and false in Macro. 
                // wow. 
                // if (val.equals("true")) val = "1";
                // if (val.equals("false")) val = "0";                 
                return (String) val;
            } else {
                return "[plugin] No value found";
            }
        }		
        else if (name.equals("getWFEvalue")) {
            String WFE_JSON = "";
            File wfef = new File(WFE_input_file);
            if ( !wfef.exists() ){
                System.out.println( "[plugin] Loading input parameters from environment" );
                WFE_JSON = java.lang.System.getenv("WFE_INPUT_JSON");
            } else {
                System.out.println( "[plugin] Loading input parameters from local file" );
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
                // want to return the proper type but not possible...
                //Object val = jo.get( key );
                String val = jo.getString( key );               
                ((String[]) args[1])[0] = (String) val;
                //((Object[]) args[1])[0] = (Object) val;
            } else {
                ((String[]) args[1])[0] = "[plugin] None";
            }
        }
        else if (name.equals("getWFEvalueBoolean")) {
            //this part is still a test
            String WFE_JSON = "";
            File wfef = new File(WFE_input_file);
            if ( !wfef.exists() ){
                System.out.println( "[plugin] Loading input parameters from environment" );
                WFE_JSON = java.lang.System.getenv("WFE_INPUT_JSON");
            } else {
                System.out.println( "[plugin] Loading input parameters from local file" );
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
                // want to return the proper type but not possible...
                Object val = jo.get( key );
                //String val = jo.getString( key );
                //((String[]) args[1])[0] = (String) val;
                //((Object[]) args[1])[0] = (Object) val;
                return (String) val;
            } else {
                return  "0";
                //((String[]) args[1])[0] = "[plugin] None";
            }
        }		
        else if (name.equals("saveTiffAPEER")) {
            String labelstring = (String) args[0];
            String pathstring = (String) args[1];
            saveTiffjson( labelstring, pathstring);
            System.out.println("[plugin] Saved Tiff");
        }
        else if (name.equals("saveResultsAPEER")) {
            String labelstring = (String) args[0];
            String pathstring = (String) args[1];
            saveResultsJson( labelstring, pathstring);
            System.out.println("[plugin] Saved Results");
        }
        else if (name.equals("saveTableAPEER")) {
            String labelstring = (String) args[0];
            String tabletitlestring = (String) args[1];
            String pathstring = (String) args[2];
            saveTableJson( labelstring, tabletitlestring, pathstring);
            System.out.println("[plugin] Saved Table");
        }		
        else if (name.equals("saveAsAPEER")) {
            String labelstring = (String) args[0];
            String formatstring = (String) args[1];
            String pathstring = (String) args[2];
            saveAsAPEER(labelstring, formatstring, pathstring);
            System.out.println("[plugin] Saved Results");
        } 
        else if (name.equals("saveStringAPEER")) {
            String labelstring = (String) args[0];
            String text = (String) args[1];
            String pathstring = (String) args[2];
            saveStringJson(labelstring, text, pathstring);
            System.out.println("[plugin] Saved text");
        } 		
//        else if (name.equals("saveJSON_OUT")) {
//            String pathstring = (String) args[0];           
//            Path path = Paths.get( pathstring );
//            JSONObject jo = new JSONObject( jsonmap );
//            try ( BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8")) ){               
//                writer.write( jo.toString(2) );
//            }catch(IOException ex){
//                ex.printStackTrace();
//            }            
//            System.out.println("[plugin] JSON out written...");
//        }
        else if (name.equals("saveJSON_OUT")) {
            String pathstring = (String) args[0];           
            Path path = Paths.get( pathstring );
            JSONObject jo = new JSONObject( jsonmap );
            if ( checkJSONOUTconsistency( jo ) ){
                try ( BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8")) ){               
                    writer.write( jo.toString(2) );
                }catch(IOException ex){
                    ex.printStackTrace();
                }            
                System.out.println("[plugin] JSON out written...");
            } 
        }		
        else if (name.equals("test2strings")) {
            String text = (String) args[0];
            String text1 = (String) args[1];
            System.out.println("[LOG0]: " + text);
            System.out.println("[LOG1]: " + text1);
        }
        else if (name.equals("exit")) {
            System.exit(0);
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
    //save string as a text file (.txt)
    void saveStringJson(String labelname, String text, String path){
        IJ.saveString(text, path);
        if ( jsonmap == null )
            initializeJSONmap();
        jsonmap.put(labelname, path);
    }    
    //save results as CSV file if the path ends with .csv
    void saveTableJson(String labelName, String tableTitle, String path){
        //IJ.saveAs("Results", path);
        Frame frame = WindowManager.getFrame(tableTitle);
        if (frame != null){
            ResultsTable rt = ((TextWindow) frame).getResultsTable();
            try {
                rt.saveAs(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if ( jsonmap == null )
                initializeJSONmap();
            jsonmap.put(labelName, path);
        } else {
            System.out.println("[PLUGIN] could not find the table:" + tableTitle);
        }
    }       

	//built-in macro saveAs command added with addition to JSON out
    void saveAsAPEER(String labelName, String format, String path){
        IJ.saveAs(format, path);
        if ( jsonmap == null )
            initializeJSONmap();
        jsonmap.put(labelName, path);
    }
    
    boolean checkJSONOUTconsistency(JSONObject jo){
        Path modspecpath = Paths.get("/module_specification.json");
        if (Files.exists( modspecpath )){
            JSONObject modspecjo;
            try {
                String ModSpec = new String(Files.readAllBytes( modspecpath));
                modspecjo = new JSONObject( ModSpec );
                Iterator<String> modspecOutkeys = modspecjo.getJSONObject("spec").getJSONObject("outputs").keys();
//                Iterator<String> jokeys = jo.keys();
                System.out.println("[plugin] === Checking consistency between Module_Specification.json and JSON out file ===");
                while ( modspecOutkeys.hasNext() ){
                    String k = modspecOutkeys.next();
                    if (jo.has( k )){
                        System.out.println("[plugin] " + k + " ---> " + k);
                    } else {
                        System.out.println("[plugin] " + k + " ---> [WARNING] missing key in JSON out");
                    }
                }
                return true;
            } catch (IOException e) {
                System.out.println("[plugin] /module_specification.json loading failed.");
                e.printStackTrace();
                return false;
            }
            
            
        } else {
            System.out.println("[plugin] /module_specification.json could not be found in ROOT... No check is done.");
            return true;
        }
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
