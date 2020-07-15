// test macro extensions. 

run("APEER MacroExtensions");
Ext.callLog("here is the output");

Ext.currentTime( curt );
Ext.callLog( "logging " + curt );
print( curt );
print("----");
//Ext.test2strings("thefirst", "the second");

inputpath = "/Users/miura/Dropbox/Freelance/projects/ZEISS_APEER/APEER_IJM_EpidermalCell_Tracking/params/WFE_input_params.json";
Ext.setWFE_Input_FilePath( inputpath );
Ext.captureWFE_JSON( wfejson );
print( wfejson );

print("------");
Ext.getWFEvalue("trackstack_name", filename);
print("trackstack_name:", filename);

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
