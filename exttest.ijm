// test macro extensions. 

run("APEER MacroExtensions");
Ext.shout("here is the output");

curt = Ext.currentTime();
Ext.shout( "logging " + curt );
print( curt );
print("----");
//Ext.test2strings("thefirst", "the second");

inputpath = "/Users/miura/Dropbox/Freelance/projects/ZEISS_APEER/APEER_IJM_EpidermalCell_Tracking/params/WFE_input_params.json";
//inputpath = "/params/WFE_input_params.json";
Ext.setWFE_Input_FilePath( inputpath );
wfejson = Ext.captureWFE_JSON();
print( wfejson );

print("------");
filename = Ext.getValue("trackstack_name");
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
