APP_STL := gnustl_static

# could fix gles missing err
# report from ndk
APP_PLATFORM:=android-14

APP_CPPFLAGS := -frtti -fexceptions
APP_OPTIM=release
#APP_ABI := all
APP_ABI :=  armeabi-v7a x86 
