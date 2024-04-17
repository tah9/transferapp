package com.genymobile.transferclient.home;

public class MessageType {
    public static char STR = 's';//字符串命令，字符串包含options信息，创建虚拟屏幕，运行
    public static char PORT = 'p';//端口命令，母端向连接的设备发送分配的端口号
    public static char MIRROR = 'm';//镜像屏幕
    public static char APP = 'a';//流转应用
}
