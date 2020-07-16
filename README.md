# APEER Macro Extension

Kota Miura

## Usage

This plugin addes new macro commands to ImageJ (Fiji), that are helpful for coding ImageJ macro for APEER modules. The plugin JAR file should be copied to the plugin directory. 

For using commands listed below, please first load new commands in your ImageJ macro inserting the follwoing one line before those commands are used. 

`run("APEER MacroExtensions");`

## Commands

| command                                       | description                                                  |
| --------------------------------------------- | ------------------------------------------------------------ |
| Ext.shout( *String* )                         | This is like the build in command `print( string )`, but outputs the *String* in the console. As Log window is not present in the docker-headless mode, it helps debugging and also to monitor the progress especially during development. |
| Ext.currentTime( *String*)                    | This commands calls back current time as a string to the given argument *String* |
| Ext.setWFE_Input_FilePath( *pathstring* )     | **[required for local testing]** This commands sets the file path to the **WFE_input_params.json** file. Details: Some information that are required for running the macro, such as the file path to the image data, parameter settings for the execution of certain algorithm (e.g. Gaussian blur sigma value) are handed to the module via environmental variable *WFE_INPUT_JSON*, that is stored in the operation system, when the module is executed in the APEER server. This is because modules are supposed to be a part of a workflow, and some outputs data from one module are passed to the next module and location of thse data files and  raw values should be notified to the next module, and this is done using this environmental variable. However, in case of running the module locally for development, we can also provide those information as a text file called **WFE_input_params.json**. This commands allows you to set the path to this file. |
| Ext.captureWFE_JSON( wfejson )                |                                                              |
| Ext.initializeJSON_out()                      |                                                              |
| Ext.getWFEvalue("trackstack_name", filename)  |                                                              |
| Ext.saveTiffAPEER ( keystring,  pathstring ); |                                                              |
| Ext.saveResultsAPEER( keystring, pathstring); |                                                              |

