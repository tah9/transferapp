package com.genymobile.transferclient.home;

public class MessageType {
    public static char STR = 's';//字符串命令，字符串包含options信息，创建虚拟屏幕，运行
    public static char PORT = 'p';//端口命令，母端向连接的设备发送分配的端口号
    public static int MIRROR = 111111;//镜像屏幕
    public static int MIRROR_PORT = 222222;//镜像屏幕,主控设备收到对方的端口
    public static int APP = 90909090;//流转应用
    public static int FILE = 80808080;//流转应用
}
