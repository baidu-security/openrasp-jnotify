################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux.c 

OBJS += \
./com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux.o 

C_DEPS += \
./com_fuxi_javaagent_contentobjects_jnotify_linux_JNotify_linux.c.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -I/opt/jdk1.6.0_45/include  -I/opt/jdk1.6.0_45/include/linux -O3 -Wall -Werror -c -fmessage-length=0 -fPIC -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


