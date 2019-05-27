
# Linux


## requirements

* java-1.8


## Configure

Please configure local.properties

sdk.dir=/opt/android-sdk-linux
ndk.dir=/opt/android-ndk-r10e


## Build
	$ ./gradlew assembleDebug

## Install to device 

Please use correct version

	$ adb install -r app/build/outputs/apk/app-debug.apk



# Windows


## requirements

* java-1.8


## Configure

Please configure local.properties

sdk.dir=C\:\\android-sdk
ndk.dir=C\:\\android-ndk-r10e


## Build
	$ gradlew.bat assembleDebug

## Install to device 

Please use correct version

	$ adb install -r app/build/outputs/apk/app-debug.apk


