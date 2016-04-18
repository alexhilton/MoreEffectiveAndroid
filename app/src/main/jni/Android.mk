LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := effectiveandroid

LOCAL_SRC_FILES := net_toughcoder_widget_BlurMethod.cpp

LOCAL_C_INCLUDES := $(JNI_H_INCLUDE) \
				    net_toughcoder_widget_BlurMethod.h
LOCAL_LDLIBS +=  -llog -ldl -lz -lm
LOCAL_LDFLAGS += -ljnigraphics
#优化大小
LOCAL_CPPFLAGS += -Os -Werror -Wall
LOCAL_CPPFLAGS +=-DWITH_ANDROID -ffunction-sections -fdata-sections -fvisibility=hidden

include $(BUILD_SHARED_LIBRARY)
