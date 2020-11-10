# APEER ImageJ Macro Extension Plugin

20201110

Kota Miura

## Introduction

This plugin adds new macro commands in ImageJ (Fiji distribution) for creating APEER modules. Although it is possible without this plugin to write an APEER module from scratch, based only on Built-In ImageJ Macro Functions, the extended macro commands added on by this plugin allow you to shortcut many steps to let you become focused on the coding of image processing and analysis. 

For example, with APEER module, a routine is required to write out a JSON file listing all output items, but this can be done much easier using the extended macro command `Ext.saveJSON_OUT( pathstring ).` 

**Note**: To understand how these new commands work, it is essential that you know how file and parameters are loaded and saved in APEER system. More details are written in the documentaiton pages in APEER website (Link). If you already have expereince and know details, please just go on. If you do not, please read the official documentation page, and the section "APEER Module I/O" later in this document, to help you understand what is going on.  

## Installing the Macro Extension

For APEER module project, insert the following line in the Dockerfile:

`ADD https://github.com/miura/APEER_MacroExtension/releases/download/v0.2.1/Apeer_MacroExt-0.2.1-SNAPSHOT.jar /Fiji.app/plugins/`

This will let Docker to download the plugin and then copy it to the Fiji plugin directory within the container. 

If you want to test it in your local OS, download the plugin JAR file from the [release pages of the GitHub repository](https://github.com/miura/APEER_MacroExtension/releases), copy it into the plugin folder of ImageJ or Fiji in your local machine.

### Testing the example project in your local environment

Download the complete package from [here](https://www.dropbox.com/s/rwuzmlqyi5vtb15/APEER_IJ_macro_example.zip?dl=0). Unzip, and then in command line, build the docker container and then run. Example commads for this can be:
```bash
docker build --rm -t miura/ijmrunner 
docker run -it --rm -v $(pwd)/input:/input -v $(pwd)/output:/output -v $(pwd)/params:/params miura/ijmrunner:latest
```

## Usage

Essential commands are briefly explained, and then the actual example of using extension commands in ImageJ macro will be shown. The file structure required to run the code in the APEER environment, and during the local development, are individually explained. Lastly, a table listing extension commands will be shown. 

### Essential Commands

For using Macro extension commands added by this plugin, it should be initialized in your macro by inserting the follwoing one line at the beginning of the macro code. 

`run("APEER MacroExtensions");`

Then for the local development, path to the Workflow Ennvironment (WFE) Input Parameter file (e.g. WFE_input_params.json) should be specified by the follwoing command:

`Ext.setWFE_Input_FilePath( path );`

We can retreve actual parameter values based on the key for each parameter.

`value = Ext.getValue( key );`

In the APEER system (in the web), the value is acquired directly from the environmental variable, and during local development, the value is read out from the local WFE input parameter file specified by`Ext.setWFE_Input_FilePath` command. 

After processing and analyzing images, we need to save results as file/s. There are built-in commands for saving images, tables, and text, but avoid use these build-in commands (such as `save(format, path ` and `saveAs(format, path)`) and use extended commands. 

`Ext.saveTiffAPEER(Key, path);`

`Ext.saveResultsAPEER( key, path);`

Using these extended commands is essential for saving the output parameter JSON file, which should be done after all files are saved. 

`Ext.saveJSON_OUT(  fullpath );`

Extended commands for file saving caches file keys and their values (file paths), and those cached key-value pairs are saved as a JSON file by `saveJSON_OUT`command, ready to be used by the next module. 

Finally, macro should terminate currently running ImageJ/Fiji instance by itself using the following command. 

`Ext.exit();`

An optional but frequently used command is 

`Ext.shout( string );`

This command prints the string in the console (stdout). 

### Actual ImageJ macro code

Here is an actual example of ImageJ macro code, which simply apply Particle Analysis to the input image and outputs binary image and the results table (CSV),  using extended commands. The whole project can be downloaded from [the GitHub repository](https://github.com/miura/APEER-IJMacro-Example). This macro has six steps:

1. Preamble
2. Set parameters
3. Open image
4. Core
5. Saving Files
6. Saving WFE JSON out 

Steps No. 3 and 4 are what people usually code in normal Fiji GUI or command line environment. Steps 1, 2, 5, 6 are procedures that has some specific issues in the APEER environment, and where extended commands are used.   

#### Example Macro

```Javascript
//========= preamble ======= 
//install Macro Extensions for APEER environment
run("APEER MacroExtensions");

//set path to local WFE parameter file
wfeparam_path = "/params/WFE_input_params.json";
Ext.setWFE_Input_FilePath( wfeparam_path );

//check if parameters can be captured
wfejson = Ext.captureWFE_JSON();
Ext.shout( wfejson );

//=== set parameters ===
inputdir = "/input/";
outputdir = "/output/";
INPUT_IMAGE = Ext.getValue("input_image");
excludeEdgeParticles = Ext.getValue("exclude_Edge_Particles");
JSONOUT_NAME = Ext.getValue("WFE_output_params_file");

//========= open image ======= 
Ext.shout( "Open: " + INPUT_IMAGE );
open( INPUT_IMAGE );
orgID = getImageID();

//========= Core ======= 
if (excludeEdgeParticles)
	opt = "display exclude clear";
else 
	opt = "display clear";
	
run("Auto Threshold", "method=Otsu white");
run("Analyze Particles...", opt);

//========= Saving Files ======= 
//save binary image data
selectImage( orgID );
outputFileKey = "binarized";
outfullpath = outputdir + utputFileKey + ".tif";
Ext.saveTiffAPEER(outputFileKey, outfullpath);
Ext.shout("...saved: " + outfullpath );

//save analysis results data
outputResultFileKey = "particleAnalysis_results";
outResultsfullpath = outputdir + outputResultFileKey + ".csv";
Ext.saveResultsAPEER( outputResultFileKey, outResultsfullpath);
Ext.shout("...saved: " + outResultsfullpath );

//=== saving WFE JSON out ===
Ext.saveJSON_OUT( outputdir + JSONOUT_NAME);
Ext.shout("...saved: " + JSONOUT_NAME);
Ext.shout("JOB DONE.");
Ext.exit();
```

### File Structure

The essential files are only five (which you see in the Github repository):

```
├── Dockerfile
├── ImageJ_macroExtensionExample.ijm
├── font.conf
├── module_specification.json
└── start.sh
```

- Dockerfile: The configuration file for building a docker container. The configuration is almost same like it is explained in the [official documentation for ImageJ Macro based APEER Module](https://docs.apeer.com/tutorials/imagej-example), but with some difference: 
  - Download and copy the MacroExtension plugin to the plugin folder of Fiji. 
  -  Some files are not required so lines copying these files are absent (Calllog.class, JSON_Read.js), since the function of these codes are replaced by the MacroExtension plugin
- ImageJ_macroExtensionExample.ijm [you need to modify]: The ImageJ macro that you want to execute as APEER module
- font.conf: adds special characters available for outputs. 
- module_specification.json [you need to modify]:  Sets the key strings for input and output parameters, and also their properties (e.g. numbers, strings, files) and their UI in APEER environment. **It's quite important that the keys defined here are exactly same as keys in IJ macro when you try to get input values (`Ext.getValue(key)`) and save files as output (e.g. `Ext.saveTiffAPEER(Key, path)`)** .
  - In APEER workspace, you can create module_specification file using a Wizard - but you need to read out keys in this file, and use them in the macro.  
- start.sh [you need to modify]: A shell script file containing a single line shell command for the headless macro execution of Fiji. This command should be revised that the file name of ImageJ macro code matches with the one that you upload. This shell script is called by Dockerfile as the entry point. 

#### File Structure in Local Machine

Effectively, just like other APEER modules, below is the file structure of the project for the example IJ macro code (explained above) in your local machine.

```
├── Dockerfile
├── ImageJ_macroExtensionExample.ijm
├── font.conf
├── input
│   ├── blobs.tif
├── module_specification.json
├── output
├── params
│   └── WFE_input_params.json
└── start.sh
```

Following directories and files become necessary for local execution. 

- input/ : the directory where input images are stored. 
- output/: the directory where files will be saved by the macro. 
- params/WFE_input_params.json: A JSON file with input keys and actual values. Keys that are appearing in this file should exactly match with keys that are listed in module_specification.json file. 

When pushing this project to APEER, only some of these files are pushed, configured by .gitignore which looks like:

```
*.DS_Store
input/*
output/*
params/*
```

Effectively, only five files listed at the begninning of this subsection becomes pushed to the repository. 

## Command Reference

| command                                                      | description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| Ext.currentTime()                                            | **[Optional]** Returns current time as a string.             |
| Ext.captureWFE_JSON( *wfe_json* )                            | **[Optional]** Used only for checking that WFE is loaded in the current environment, or from a local file. Returns the Work Flow Environment (WFE) variables in JSON format. Use *Ext.shout( string )* to print the JSON text in the console. |
| Ext.exit()                                                   | **[Required]** System exit. Terminates currently running ImageJ/Fiji instance - and this is needed for running it in headless mode to finalize the processing. Use this with caution in the local environment, because this commands terminates (quits) currently running Fiji instance. |
| Ext.getValue( *keystring* )                                  | **[Required]** This command returns a value for the given *keystring from WFE varibles from Environmental variables. During development, WFE is acquired from local file *WFE_input_params.json*. |
| Ext.initializeJSON_out()                                     | Clear and resets the JSON out key-value pairs.               |
| Ext.saveAsAPEER(*keystring, formatstring, pathstring* )      | This command is an extension of standard macro command saveAs(format, path), added with registering of the *pathstring* as value for the key *keystring*. *Ext.saveResultsAPEER, Ext.saveTableAPEER, and Ext.saveTiffAPEER* are output-format specified version of this command. |
| Ext.saveJSON_OUT( *pathstring* )                             | **[Required]** After all files are saved in the macro, key-value pairs for each saved file are only internally stored. To output those key-value pairs as a JSON file, this command can be used to output JSON-out.json file specified by *pathstring*. |
| Ext.saveResultsAPEER( *keystring, pathstring*)               | **Saves a CSV file specified by pathstring** (absolute path: follow the convention and use /output directory for sving files). When a CSV file specified by *pathstring* is saved using this command, *keystring* is registered as well. This registered key-value pair is then used for inserting this pair to the JSON out file (do this after all processing and saving are done using *saveJSON_OUT* command). It is necessary that that the *keystring* should be identical to one of the keys in outputs stated in the *module_specification.json*. |
| Ext.saveTableAPEER(*keystring, tableNameString, pathstring*) | Similar to Ext.saveResultsAPEER, but non-Results table with a specific title *tableNameString* can be saved. |
| Ext.saveTiffAPEER( *keystring,  pathstring* );               | **Saves a tiff file specified by *pathstring*** (absolute path: follow the convention and use /output directory). When a TIFF file specified by *pathstring* is saved using this command, *keystring* is registered as well. This registered key-value pair is then used for inserting this pair to the JSON out file (do this after all processing and saving are done using *saveJSON_OUT* command). It is necessary that that the *keystring* should be identical to one of the keys in outputs stated in the *module_specification.json*. |
| Ext.saveStringAPEER(*keystring, textString, pathstring*)     | **Saves *textString* to a text file specified by *pathstring*** (absolute path: follow the convention and use /output directory). When a TEXT file specified by *pathstring* is saved using this command, *keystring* is registered as well. This registered key-value pair is then used for inserting this pair to the JSON out file (do this after all processing and saving are done using *saveJSON_OUT* command). It is necessary that that the *keystring* should be identical to one of the keys in outputs stated in the *module_specification.json*. |
| Ext.setWFE_Input_FilePath( *pathstring* )                    | **[required for local testing]** This commands sets the file path to the **WFE_input_params.json** file. Details: Input data information that are required for running the macro, such as the file path to the image data, parameter settings for the execution of certain algorithm (e.g. Gaussian blur sigma value) are passed to the module via an environmental variable *WFE_INPUT_JSON*, that is stored in the operation system, when the module is executed in the APEER server. This is because modules are supposed to be a part of a workflow, and some outputs data from one module are passed to the next module and location of these data files and  raw values should be notified to the next module, and this is done using this environmental variable. However, in case of running the module locally for development, we can also provide those information as a text file called **WFE_input_params.json**. This commands allows you to set the path to this file. |
| Ext.shout( *String* )                                        | **[Optional]**This is like the build-in IJ macro `print( string )`, but outputs the *String* in the console. As the Log window is not present in the docker-headless mode, it helps debugging and also to monitor the progress especially during development. |

 

## APEER Module I/O

Here, we focus on how files and parameters are managed for inputs and outputs when ImageJ macro is used as an APEER module (Overview of the APEER system can be find in [the ofiicial documentation](https://docs.apeer.com/create-modules/the-apeer-architecture)). 

**Declare that the modle needs input file/s**: Opening and saving of files in APEER module are achieved not by directly providing the file path for opening and saving: the reason is that for a modul placed in a APEER workflow, the exact  name of the input file and its location (file path) is unknown until the runtime and can be various, but the APEER system should know that the module requires certain input file, so that a workflow can be constructed. 

**Declare that there will be output file**: For the output of files, we need to notify the APEER system before the execution that there are output files even though they are still not present. This notification allows us to create a workflow that those output files to be used in the successive module as input files. 

### Input of files and parameters 

In normal ImageJ macro, when we want to open a file, we provide the file path to the command *open* e.g. `open("/Users/me/image.tif")` .  In APEER module, such a fixed file path does not work because files for input are passed from the upstream module and the file name and its location are determined only during the runtime.  

Alternatively, we can provide an empty path like `open()` , which triggers ImageJ to show a file chooser window to let the user choose a file interactively. However, APEER does not create a file chooser window during its execution and uses a different strategy to specify the input file. 

In APEER, the input file is specified through (1) the Web GUI and/or (2) by a line that connects from an output of the upstream module to the module that we are now working on (Fig). 

(Fig)

For this module-line-module style to work properly, we need to notify the APEER system that the module needs an input file to run the macro contained in the module. If we need two files, we need to notify the system that we need two files. Similary, if there is a parameter in the macro, that needs to be adjusted depending on the nature of the images, we also need to notify APEER sytem that there is a parameter that needs to have some user input (though we can also let the parameter value to take the default value). 

How do we notify such requirements to the system? Here comes the concept of "**Workflow Environmental Variables (WFE variables)**", which is used in the APEER system to define the presence of inputs and outputs. A WFE variable is a pair of a tag and it's actual value. In the programming world, we call such a pair a "key-value" pair. This is often coded by separating a key and its value by a colon. It looks like this:

`imagefile_path : "/Users/me/image.tif"`

In this example, `imagefile_path` is the key (or tag), and `"/Users/me/image.tif"` is the value for that key. Here, the value is fixed to a specific path. If we let the value to be empty and let the system to know that THERE IS A KEY THAT IS WAITING FOR A SPECIFIC VALUE, we can let the web interface of APEER to create a symbol in a module that the module has a empty field for someone to specify the input and determine the value for that key (Fig ). Then APEER system can place a inlet and outlet for each module for connecting it with other modules for passing those files.  

(Fig. )

### Module_Specification.json

For this reason, we list such keys of WFE variables in a file named "**Module_Specification.json**", that is saved in the root directory of each APEER module. This file specifies the name of required input and output variables. This file can be composed in the APEER Workspace using a wizard and be downloaded to local machine ([see more details in the official documentation](https://docs.apeer.com/documentation/module-specification)). It needs to be checked locally in any case to do the local development. An example of this file is shown in below, copied from the [APEER example ImageJ macro](https://github.com/miura/APEER-IJMacro-Example). 


```json
{
    "spec": {
        "inputs": {
            "input_image": {
                "type:file": {
                    "format": [
                        "tiff"
                    ]
                }
            },
            "exclude_Edge_Particles": {
                "type:choice_binary": null,
                "default" : true
            }
        },
        "outputs": {
            "binarized": {
                "type:file": {
                    "format": [
                        "tiff"
                    ]
                }
            },            
            "particleAnalysis_results": {
                "type:file": {
                    "format": [
                        "csv"
                    ]
                }
            }         
        }
    },
    "ui": {
        "inputs": {
            "input_image": {
                "index": 1,
                "label": "",
                "widget:none": null,
                "description": "the input image to be measured"
            },
            "exclude_Edge_Particles": {
                "index": 2,
                "label": "Exclude edge particles",
                "widget:checkbox": {},
                "description": "Exclude objects touching edge from measurements"
            }
        },
        "outputs": {
            "binarized": {
                "index": 1,
                "label": "Binarized Image",
                
                "description": "Intensity Thresholded Image (Otsu)"
            },
            "particleAnalysis_results": {
                "index": 2,
                "label": "Particle Analysis Results",
                "description": "CSV file with measurement results"
            }          
        }
    }
}
```

#### The structure of Modulel_Specification.json

**Modulel_Specification.json** file has two objects named `spec` and `ui`. Each of these objects contains `inputs` and `outputs` objects. This structure is always same in all APEER modules. What we need to define is objects below these hierarchy. 

Number of objects and their names under `inputs` and `outputs` within `spec` and `ui` are the identical.  In this example, there are two objects `input_image`, and `exclude_Edge_Particles` inside `inputs` (let's call them "parameter objects"), and two object within `outputs` object, `binarized` and `particleAnalysis_results`.  These are the names of the variables that needs to be associated with some value - waiting to be determined in runtime. In face, when you write macro, these names of parameter objects are used as argument for `Ext.getValue( key )`, to acquire the actual values (see lines 16-18 of [the example macro code above](#example-macro)).  

Within the `spec` object, and each parameter object is specified with their type, like `file` or `choice_binary`. It's not appearing here, but the type can also be `string` (what others may call it Boolean), `choice_single`, `integer`, and `number.`

As module_specification.json only list the object names, keys, for inputs and outputs, the actual value for these keys are specified differently in the APEER system and duing the local execution  

### Input files and parameters

#### Getting input values, in the APEER system (the web GUI):

As modules only run as a component of workflow, the actual values for inputs are determined either by a line that connects between the upstream module or by the parameters that are set for that module, both being set via Web UI manually. In practice, these ke-value pairs are provided to the module using and environmental variable "**WFE_INPUT_JSON**", the value of which is a JSON objects filled with key-value pairs.   

#### Getting input values, during the local development:

During local development, as Web GUI is not existing, a specific JSON file is looked up for key-value pairs. This file is by convention named "**WFE_input_params.json**" and saved under a folder named "params" (see [File Structure in Local Machine](#file-structure-in-local-machine) section). The keys appearing in this file should match with those that appear in the module specification (except for one key-value pair, see below), and also the keys used by `Ext.getValue( key )` command in the macro  (see lines 16-18 of [the example macro code above](#example-macro)). Here is the example WFE_input_params.json, [required for the example project](https://github.com/miura/APEER-IJMacro-Example). As this repository do not contain files that are for only local use, you can download the local project files from [here](https://www.dropbox.com/s/rwuzmlqyi5vtb15/APEER_IJ_macro_example.zip?dl=0). 

Here is how the **WFE_input_params.json** looks like for the example project. 

```jSON
{
    "WFE_output_params_file": "wfe_module_params_1.json",
    "input_image" : "blobs.tif",
    "exclude_Edge_Particles": true           
}
```

Important Note: the key values are exactly same as the name of parameter objects under `inputs` in Module_Specification.json, and also those used in the example macro code.

Note 2: the key WFE_output_params_file is used only within the ImageJ macro code for getting the name of the output JSON file, and used for notifying the system the name of the output JSON file (therefore, it is not listed in the module_specification.json). 

### Output of Files

Just like the APEER system needs to be notified about input files, list of resulting output files should be notified to the systme. This is done by writing a file with list of key-value pairs of output files. Conventionally, this is called "JSONout", but the actual name is specific. This file name is automatically generated when the module is executed within APEER workflow, and is specified in **WFE_input_params.json** in the case of the execution in the local environment. 

A extension command `Ext.saveJSON_OUT( full-filepath)`is used in ImageJ macro for writing this file. This command looks-up in the memory what was saved as files during the execution of the macro, and compile JSON code and write that code in a file specfied as argument `full-filepath`(usually under the directory output/). For this command to work properly, saving of any file should be done using the extension commands:

- `Ext.saveTiffAPEER`
- `Ext.saveAsAPEER`
- `Ext.saveResultsAPEER`
- `Ext.saveTableAPEER`
- `Ext.saveStringAPEER`  

This requirement is because only the extension commands will keep the paths of saved files in the memory that can be used by `Ext.saveJSON_OUT`. Files saved by build-in commands will be ignored by this command, and will cause trouble in finding the file in APEER environment. 

Again, it is important that keys used as argument for saving files should exactly match with those listed under `outputs` object in module_specification.json file. As this often causes bug in workflow, the command `Ext.saveJSON_OUT` also checks the consistency between the output object keys and the keys written in the JSONout file, and prints that out in the console. 

Example of successful match:

```bash
[plugin] === Checking consistency between Module_Specification.json and JSON out file ===
[plugin] binarized ---> binarized
[plugin] particleAnalysis_results ---> particleAnalysis_results
[plugin] JSON out written...
```

Example of un-matched keys:

```bash
[plugin] === Checking consistency between Module_Specification.json and JSON out file ===
[plugin] binarized ---> binarized
[plugin] particleAnalysis_results ---> [WARNING] missing key in JSON out
[plugin] JSON out written...
```

Note that this will only be a warning and does not stop the execution of the code. If you have problem in running the module in workflow, check the console carefully if such a warning is there. 

