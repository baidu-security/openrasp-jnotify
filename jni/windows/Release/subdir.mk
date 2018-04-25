################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../Lock.cpp \
../Logger.cpp \
../WatchData.cpp \
../Win32FSHook.cpp \
../com_fuxi_javaagent_contentobjects_jnotify_win32_JNotify_win32.cpp 

OBJS += \
./Lock.o \
./Logger.o \
./WatchData.o \
./Win32FSHook.o \
./com_fuxi_javaagent_contentobjects_jnotify_win32_JNotify_win32.o 

CPP_DEPS += \
./Lock.d \
./Logger.d \
./WatchData.d \
./Win32FSHook.d \
./com_fuxi_javaagent_contentobjects_jnotify_win32_JNotify_win32.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I"c:\Program Files\Java\jdk1.6.0_45\include\win32" -I"c:\Program Files\Java\jdk1.6.0_45\include" -O0 -Os -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


