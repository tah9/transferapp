// ITransferInterface.aidl
package com.genymobile.transfer;


interface ITransferInterface {
    //创建虚拟显示器,将指定包名的程序运行其上
    String appRunOnTargetDisplay(String packageName,String optionsStr);


}