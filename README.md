# APEER ImageJ Macro Extension Plugin

Kota Miura

## Introduction

This plugin addes new macro commands to ImageJ (Fiji distribution). These commands are supposed to be helpful for coding ImageJ macro to be used as a part of APEER modules. Although it is possible to write everything to create an APEER module from scratch using only Built-In ImageJ Macro Functions, additional macro commands added by this plugin allow you to shortcut many efforts to let you become focused on the coding of desired image processing and analysis. 

To understand how these new commands work, it is essential to know how file and parameters are loaded and saved in APEER system. Although more details are written in the documentaiton pages in APEER website, here, we focus on how files and parameters are managed for inputs and outputs when ImageJ macro is used. If you know "Module_Specification.json" file well and it's role, please skip the next section and directly move on to "Usage" section. 

##APEER Module I/O

**Input file is unknown**: Opening and saving of files in APEER module are achieved not by directly providing the file path for opening and saving: the reason is that for a modul placed in a APEER workflow, the exact  name of the input file and its location (file path) is unknown until the runtime and can be various. 

**Declare that there will be output file**: For the output of files, we need to let the APEER system to know that there are output files even though they are not yet present, so that those files that eventually becomes available can be used in the successive module as input files. 

### Input of files and parameters 

In normal ImageJ macro, when we want to open a file, we provide the file path to the command *open* e.g. `open("/Users/me/image.tif")` .  In APEER module, such a fixed file path does not work because input files are passed from the upstream module and the file name and its location are determined only during the runtime.  

Alternatively, we can provide an empty path like `open()` , which triggers ImageJ to show a file chooser window to let the user choose a file interactively. However, APEER does not create a file chooser window during its execution and uses a different strategy to specify the input file. 

In APEER, the input file is specified in (1) the Web GUI and/or (2) by a line that connects from an output of the upstream module to the module that we are now working on (Fig). 

(Fig)

For this module-line-module style to work properly, we need to notify the APEER system that the module needs an input file to run the macro contained in the module. If we need two files, we need to notify the system that we need two files. Similary, if there is a parameter to set in the macro, that needs to be adjusted depending on the nature of the images, we also need to notify APEER sytem that there is a parameter that needs to have some user input (though we can also let the parameter value to take the default value). 

How do we notify such requirements to the system? Here comes the concept of "**Workflow Environmental Variables (WFE variables)**", which is used by any module published in APEER. A WFE variable is a pair of a tag and it's actual value. In the programming world, we call such a pair a "key-value" pair. This is often coded by separating a key and its value by a colon. It looks like this:

`imagefile_path : "/Users/me/image.tif"`

In this example, `imagefile_path` is the key (or tag), and `"/Users/me/image.tif"` is the value for that key. Here, the value is fixed to a specific path. If we let the value to be empty and let the system to know that THERE IS A KEY THAT IS WAITING FOR A SPECIFIC VALUE, we can let the web interface of APEER to create a symbol in a module that the module has a empty field for someone to specify the input and determine the value for that key (Fig ). Then APEER system can place a inlet and outlet for each module for connecting it with other modules for passing those files.  

(Fig. )

For this reason, we list such keys in a file named "**Module_Specification.json**", that is saved in the root directory of each APEER module. An example of this file is shown in below, copied from the [Documentation page in APEER](https://docs.apeer.com/tutorials/python-ome-tiff-example). 


```json
{
    "spec": {
        "inputs": {
            "input_image": {
                "type:file": {}
            },
            "angle": {
                "type:string": {},
                "default":"0.0"
            },
            "shift_x": {
                "type:string": {},
                "default":"0"
            },
            "shift_y": {
                "type:string": {},
                "default":"0"
            }
        },
        "outputs": {
            "output_image": {
                "type:file": {}
            }
        }
    },
    "ui": {
        "inputs": {
            "input_image": {
                "index": 0,
                "widget:none": null,
                "label": "Input image"
            },
            "angle": {
                "index": 1,
                "label": "Rotation Angle in degrees",
                "widget:textbox": {
                }
            },
            "shift_x": {
                "index": 2,
                "label": "Shift x in px",
                "description": "Shift x in px (must be integer number)",
                "widget:textbox": {
                }
            },
            "shift_y": {
                "index": 3,
                "label": "Shift y in px",
                "description": "Shift y in px (must be integer number)",
                "widget:textbox": {
                }
            }
        },
        "outputs": {}
    }
}
```

Modulel_Specification.json file has two objects named `spec` and `ui`. Each of these objects contains same number of objects with same names. Here, there are four objects with name `input_image`, `angle`, `shift_x` and `shift_y`. These are the names of the variables that needs to be associated with some value. Within the `spec` object, and each parameter object is specified with their type, like `file` or `string`. It's not appearing here, but the type can also be `choice_binary` (what others may call it Boolean), `choice_single`, `integer`, and `number`

[How do we fill the value?]

[... in case of WEB GUI]

[... ... values provided on runtime] 

input files

parametes

[... in case of local test]

[... ...input json file, just for the local execution]

both for input files and parameters, we specify them in a special input file. 



[void] input paths are provided from environmental variable (variable that is kept in the operating systems) or via paramter file written in JSON formats, and output file path should not only save the target file but should also be written to a file that lists outputs. Here are more details. 



### Output of Files

[like input files, we declare the output keys - in case of outputs, we only output files in most cases - but can be any? --- need some confirmation]

[file path of output files needs to be told to the systems - JSONout. We create it in the output folder, by default]. 

## Installing the Macro Extension

For APEER module project, insert the following line in Dockerfile:

`ADD https://github.com/miura/APEER_MacroExtension/releases/download/v0.2.0/Apeer_MacroExt-0.2.0-SNAPSHOT.jar /Fiji.app/plugins/`

This will download the plugin and then copy it to the Fiji plugin directory. When you want to test it, download the plugin JAR file to the plugin folder of ImageJ or Fiji. 

##Step-By-Step Usage

### Local Development

For using Macro extension commands added by this plugin, load new commands in your ImageJ macro by inserting the follwoing one line in the macro\. 

`run("APEER MacroExtensions");`



## Command Reference

| command                                                      | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| Ext.currentTime()                                            | **[Optional]** Returns current time as a string.             |
| Ext.captureWFE_JSON( *wfe_json* )                            | **[Optional]** Used only for checking that WFE is loaded in the current environment, or from a local file. Returns the Work Flow Environment (WFE) variables in JSON format. Use *Ext.shout( string )* to print the JSON text in the console. |
| Ext.exit()                                                   | **[Required]** System exit. Terminates currently running ImageJ/Fiji instance - and this is needed for running it in headless mode to finalize the processing. Use this with caution in the local environment, because this commands terminates (quits) currently running Fiji instance. |
| Ext.getValue( *keystring* )                                  | **[Required]** This command returns a value for the given *keystring from WFE varibles from Environmental variables. During development, WFE is acquired from local file *WFE_input_params.json*. |
| Ext.initializeJSON_out()                                     | Clear and resets the JSON out key-value pairs.               |
| Ext.saveAsAPEER(*keystring, formatstring, pathstring* )      | This command is an extension of standard macro command saveAs(format, path), added with registering of the *pathstring* as value for the key *keystring*. *Ext.saveResultsAPEER, Ext.saveTableAPEER, and Ext.saveTiffAPEER* are output-format specified version of this command. |
| saveJSON_OUT( *pathstring* )                                 | **[Required]** After all files are saved in the macro, key-value pairs for each saved file are only internally stored. To output those key-value pairs as a JSON file, this command can be used to output JSON-out.json file specified by *pathstring*. |
| Ext.saveResultsAPEER( *keystring, pathstring*)               | **Saves a CSV file specified by pathstring** (absolute path: follow the convention and use /output directory for sving files). When a CSV file specified by *pathstring* is saved using this command, *keystring* is registered as well. This registered key-value pair is then used for inserting this pair to the JSON out file (do this after all processing and saving are done using *saveJSON_OUT* command). It is necessary that that the *keystring* should be identical to one of the keys in outputs stated in the *module_specification.json*. |
| Ext.saveTableAPEER(*keystring, tableNameString, pathstring*) | Similar to Ext.saveResultsAPEER, but non-Results table with a specific title *tableNameString* can be saved. |
| Ext.saveTiffAPEER ( *keystring,  pathstring* );              | **Saves a tiff file specified by pathstring** (absolute path: follow the convention and use /output directory). When a TIFF file specified by *pathstring* is saved using this command, *keystring* is registered as well. This registered key-value pair is then used for inserting this pair to the JSON out file (do this after all processing and saving are done using *saveJSON_OUT* command). It is necessary that that the *keystring* should be identical to one of the keys in outputs stated in the *module_specification.json*. |
| Ext.setWFE_Input_FilePath( *pathstring* )                    | **[required for local testing]** This commands sets the file path to the **WFE_input_params.json** file. Details: Input data information that are required for running the macro, such as the file path to the image data, parameter settings for the execution of certain algorithm (e.g. Gaussian blur sigma value) are passed to the module via an environmental variable *WFE_INPUT_JSON*, that is stored in the operation system, when the module is executed in the APEER server. This is because modules are supposed to be a part of a workflow, and some outputs data from one module are passed to the next module and location of these data files and  raw values should be notified to the next module, and this is done using this environmental variable. However, in case of running the module locally for development, we can also provide those information as a text file called **WFE_input_params.json**. This commands allows you to set the path to this file. |
| Ext.shout( *String* )                                        | **[Optional]**This is like the build-in IJ macro `print( string )`, but outputs the *String* in the console. As the Log window is not present in the docker-headless mode, it helps debugging and also to monitor the progress especially during development. |
|                                                              |                                                              |
|                                                              |                                                              |
|                                                              |                                                              |
|                                                              |                                                              |



## Example

Below is for testing the Macro Extension commands. 

```javascript
// test macro extensions. 
run("APEER MacroExtensions");
Ext.shout("here is the output");

curt = Ext.currentTime();
Ext.shout( "logging " + curt );
print( curt );
print("----");
//Ext.test2strings("thefirst", "the second");

inputpath = "/<path>/<to>/WFE_input_params.json";
//inputpath = "/params/WFE_input_params.json";
Ext.setWFE_Input_FilePath( inputpath );
wfejson = Ext.captureWFE_JSON();
print( wfejson );

print("------");
filename = Ext.getValue("3d_image_stack");
print("3d_image_stack:", filename);

print("------");
Ext.initializeJSON_out();

print("------");
run("Blobs (25K)");
run("Auto Threshold", "method=Otsu");
setThreshold(0, 254);
run("Analyze Particles...", "display exclude clear");
Ext.saveTiffAPEER("image", "/Users/miura/Downloads/testap.tif");
Ext.saveResultsAPEER("data", "/Users/miura/Downloads/testap.csv");
Ext.saveJSON_OUT("/Users/miura/Downloads/out.json");
```



