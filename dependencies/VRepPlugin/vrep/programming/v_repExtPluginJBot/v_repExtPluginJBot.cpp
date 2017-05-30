#include "v_repExtPluginJBot.h"
#include "luaFunctionData.h"
#include "v_repLib.h"
#include <iostream>
#include <string.h>
#include <jni.h>
#include <stdlib.h>

#ifdef _WIN32
	#ifdef QT_COMPIL
		#include <direct.h>
	#else
		#include <shlwapi.h>
		#pragma comment(lib, "Shlwapi.lib")
	#endif
#endif /* _WIN32 */
#if defined (__linux) || defined (__APPLE__)
	#include <unistd.h>
#endif /* __linux || __APPLE__ */

#define CONCAT(x,y,z) x y z
#define strConCat(x,y,z)	CONCAT(x,y,z)

#define PLUGIN_VERSION 2 // 2 since version 3.2.1

LIBRARY vrepLib; // the V-REP library that we will dynamically load and bind

JavaVM *jvm;
JNIEnv *env;
jclass factoryCls;
jmethodID loadContrMtd;
jmethodID contrStepMtd;


/*
LoadController command
*/

#define LUA_LOADCONTROLLER_COMMAND "simExtLoadController"

const int inArgs_LOADCONTROLLER[]={ // Decide what kind of arguments we need
	3, // we want 2 input arguments
	sim_lua_arg_int,0, // int handle
	sim_lua_arg_int,0, // int type
    sim_lua_arg_float|sim_lua_arg_table,0 // vector of floats
};

void LUA_LOADCONTROLLER_CALLBACK(SLuaCallBack* p) // the callback function of the new Lua command
{ 
	CLuaFunctionData D;
	D.readDataFromLua(p,inArgs_LOADCONTROLLER,inArgs_LOADCONTROLLER[0],LUA_LOADCONTROLLER_COMMAND);
	std::vector<CLuaFunctionDataItem>* inData=D.getInDataPtr();

	jvm->AttachCurrentThread((void **)&env, NULL);

	// handle
	int handle = inData->at(0).intData[0];
	
	// controller type
	int type = inData->at(1).intData[0];
	
	// float vector -- controller parameters
	std::vector<float>& floatVec=inData->at(2).floatData;
    jfloatArray floatArr = env->NewFloatArray(floatVec.size());
	float* f = &floatVec[0];
	env->SetFloatArrayRegion(floatArr, 0, floatVec.size(), f);

	env->CallStaticVoidMethod(factoryCls, loadContrMtd, handle, type, floatArr);

	env->DeleteLocalRef(floatArr);
	
	if (env->ExceptionCheck()) {
		std::cout << "Exception in load controller\n";
		env->ExceptionDescribe();
  		return;
	}
}

/*
ControlStep command
*/
#define LUA_CONTROLSTEP_COMMAND "simExtControlStep"

const int inArgs_CONTROLSTEP[]={ // Decide what kind of arguments we need
	2, // we want 2 input arguments
    sim_lua_arg_int,0, // int
	sim_lua_arg_float|sim_lua_arg_table,0 // vector of floats
};

void LUA_CONTROLSTEP_CALLBACK(SLuaCallBack* p) // the callback function of the new Lua command
{ 
	CLuaFunctionData D;
	D.readDataFromLua(p,inArgs_CONTROLSTEP,inArgs_CONTROLSTEP[0],LUA_CONTROLSTEP_COMMAND);
	std::vector<CLuaFunctionDataItem>* inData=D.getInDataPtr();
	
	//jvm->AttachCurrentThread((void **)&env, NULL);

	// int handle
	int handle=inData->at(0).intData[0];
	
	// float vector
	std::vector<float>& floatVec=inData->at(1).floatData;
	float* f = &floatVec[0];
    jfloatArray floatArr = env->NewFloatArray(floatVec.size());
	env->SetFloatArrayRegion(floatArr, 0, floatVec.size(), f);

	// call
	jfloatArray res = (jfloatArray) env->CallStaticObjectMethod(factoryCls, contrStepMtd, handle, floatArr);
	if (env->ExceptionCheck()) {
		std::cout << "Exception in control step\n";
		env->ExceptionDescribe();
  		return;
	}

	env->DeleteLocalRef(floatArr);

	if (res == NULL) {
		D.pushOutData(CLuaFunctionDataItem());
	} else {
		jsize size = env->GetArrayLength(res);
		jfloat *buf = env->GetFloatArrayElements(res, 0);

		// vector from array of jfloats
		std::vector<float> returnData;
		for(int i = 0 ; i < size ; i++) {
			returnData.push_back(buf[i]);	
		}
		env->DeleteLocalRef(res);
		
		D.pushOutData(CLuaFunctionDataItem(returnData));
	}
	D.writeDataToLua(p);
}



// --------------------------------------------------------------------------------------


// This is the plugin start routine (called just once, just after the plugin was loaded):
VREP_DLLEXPORT unsigned char v_repStart(void* reservedPointer,int reservedInt)
{
	// Dynamically load and bind V-REP functions:
	// ******************************************
	// 1. Figure out this plugin's directory:
	char curDirAndFile[1024];
#ifdef _WIN32
	#ifdef QT_COMPIL
		_getcwd(curDirAndFile, sizeof(curDirAndFile));
	#else
		GetModuleFileName(NULL,curDirAndFile,1023);
		PathRemoveFileSpec(curDirAndFile);
	#endif
#elif defined (__linux) || defined (__APPLE__)
	getcwd(curDirAndFile, sizeof(curDirAndFile));
#endif

	std::string currentDirAndPath(curDirAndFile);
	// 2. Append the V-REP library's name:
	std::string temp(currentDirAndPath);
#ifdef _WIN32
	temp+="\\v_rep.dll";
#elif defined (__linux)
	temp+="/libv_rep.so";
#elif defined (__APPLE__)
	temp+="/libv_rep.dylib";
#endif /* __linux || __APPLE__ */
	// 3. Load the V-REP library:
	vrepLib=loadVrepLibrary(temp.c_str());
	if (vrepLib==NULL)
	{
		std::cout << "Error, could not find or correctly load the V-REP library. Cannot start 'PluginJBot' plugin.\n";
		return(0); // Means error, V-REP will unload this plugin
	}
	if (getVrepProcAddresses(vrepLib)==0)
	{
		std::cout << "Error, could not find all required functions in the V-REP library. Cannot start 'PluginJBot' plugin.\n";
		unloadVrepLibrary(vrepLib);
		return(0); // Means error, V-REP will unload this plugin
	}


	// Check the version of V-REP:
	// ******************************************
	int vrepVer;
	simGetIntegerParameter(sim_intparam_program_version,&vrepVer);
	if (vrepVer<30200) // if V-REP version is smaller than 3.02.00
	{
		std::cout << "Sorry, your V-REP copy is somewhat old. Cannot start 'PluginJBot' plugin.\n";
		unloadVrepLibrary(vrepLib);
		return(0); // Means error, V-REP will unload this plugin
	}


    // INIT JVM HERE:
    // ******************************************
    JavaVMInitArgs vm_args;
    JavaVMOption* options = new JavaVMOption[1];
    options[0].optionString = "-Djava.class.path=programming/v_repExtPluginJBot/java/jbot.jar";
	vm_args.version = JNI_VERSION_1_6;
    vm_args.nOptions = 1;
    vm_args.options = options;
    vm_args.ignoreUnrecognized = 1;
    jint rc = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	delete options;
    if (rc != JNI_OK) {
	   std::cout << "JVM NOT INITIALIZED CORRECTLY\n";
       std::cin.get();
       exit(EXIT_FAILURE);
    } else {
       std::cout << "JVM load succeeded: Version ";
       jint ver = env->GetVersion();
       std::cout << ((ver>>16)&0x0f) << "."<<(ver&0x0f) << "\n";		
	}


	// GET CLASS AND METHODS HANDLES HERE:
	// ******************************************

	factoryCls = env->FindClass("mase/vrep/ControllerFactory");
	if(factoryCls == NULL) {
		std::cout << "CANNOT FIND ControllerFactory class\n";
		exit(EXIT_FAILURE);
	}	

	loadContrMtd = env->GetStaticMethodID(factoryCls, "loadController", "(II[F)V");
	if(loadContrMtd == NULL) {
		std::cout << "CANNOT FIND loadController method\n";
		exit(EXIT_FAILURE);
	}	

	contrStepMtd = env->GetStaticMethodID(factoryCls, "controlStep", "(I[F)[F");
	if(contrStepMtd == NULL) {
		std::cout << "CANNOT FIND controlStep method\n";
		exit(EXIT_FAILURE);
	}	
	std::cout << "ALL CLASSES AND METHODS FOUND\n";

    
    // REGISTER THE NEW COMMANDS HERE:
    // ******************************************
	std::vector<int> inArgs;

	// Register the LoadController command
	CLuaFunctionData::getInputDataForFunctionRegistration(inArgs_LOADCONTROLLER,inArgs);
	simRegisterCustomLuaFunction(LUA_LOADCONTROLLER_COMMAND,NULL,&inArgs[0],LUA_LOADCONTROLLER_CALLBACK);

	// Register the ControlStep command
	CLuaFunctionData::getInputDataForFunctionRegistration(inArgs_CONTROLSTEP,inArgs);
	simRegisterCustomLuaFunction(LUA_CONTROLSTEP_COMMAND,NULL,&inArgs[0],LUA_CONTROLSTEP_CALLBACK);

	return(PLUGIN_VERSION); // initialization went fine, we return the version number of this plugin (can be queried with simGetModuleName)
}


// This is the plugin end routine (called just once, when V-REP is ending, i.e. releasing this plugin):
VREP_DLLEXPORT void v_repEnd()
{
	// Here you could handle various clean-up tasks
	unloadVrepLibrary(vrepLib); // release the library
}


// This is the plugin messaging routine (i.e. V-REP calls this function very often, with various messages):
VREP_DLLEXPORT void* v_repMessage(int message,int* auxiliaryData,void* customData,int* replyData)
{ // This is called quite often. Just watch out for messages/events you want to handle
	// Keep following 5 lines at the beginning and unchanged:
	static bool refreshDlgFlag=true;
	int errorModeSaved;
	simGetIntegerParameter(sim_intparam_error_report_mode,&errorModeSaved);
	simSetIntegerParameter(sim_intparam_error_report_mode,sim_api_errormessage_ignore);
	void* retVal=NULL;

	// Here we can intercept many messages from V-REP (actually callbacks). Only the most important messages are listed here.
	// For a complete list of messages that you can intercept/react with, search for "sim_message_eventcallback"-type constants
	// in the V-REP user manual.
    // SEE THE PLUGIN SKELETON IF THIS NEEDS TO BE USED

	// Keep following unchanged:
	simSetIntegerParameter(sim_intparam_error_report_mode,errorModeSaved); // restore previous settings
	return(retVal);
}

