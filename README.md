# MockGPS

Author：Nero

修改Android系统的GPS定位

__目前仅适用于百度地图和高德地图的SDK定位，腾讯系列无法使用__

__安装包路径: app\release\MockGPS_v1.9.6.191114_beta.apk__

需要开启 __开发者选项中的位置模拟__

其次 __悬浮窗最好开启一下，否则可能定位不稳定__

还有 __GPS定位一定要开启__  我测试的时候GPS、WLAN和移动网络定位可以同时开启

代码的原理在 https://blog.csdn.net/Hilavergil/article/details/81907548

有问题或者bug的话，就发邮件吧 [565563269@qq.com]


__Version 1.9.6 beta版__【branch：dev】
  
  签名apk的目录 ./app/release/MockGPS_v1.9.6.191114_beta.apk
  
  下载链接：https://github.com/Hilaver/MockGPS/raw/dev/app/release/MockGPS_v1.9.6.191114_beta.apk

  1.增加了Android10的适配，新代码放到了dev分支，其他没什么大的改动了

  2.哦对了，有朋友反应说开启/关闭按钮遮挡比例尺了，我把按钮往中间移动了一点
  

Version 1.9.5测试版
  
  下载地址：https://github.com/Hilaver/MockGPS/blob/master/app/release/MockGPS_v1.9.5.190604_alpha.apk

  1.开发环境换成了Android 9，增加一些Android 9的适配
  
  2.修改Android 9中获取位置详情失败的bug
  
  3.删除了获取IMEI的权限
  
  4.修改EMUI9.1的存储权限获取bug(然后我想到如果不给存储权限的话，这个app就直接崩了...)
  
  5.增强了一下稳定性???我也不是很确定，不太好测试
  
  6.增加运行日志记录，目录为 你的手机/MockGPS/Log/xxx.log，这个是为了方便bug反馈和调试。有好几个人反应说定位来回跳，但我死活重现不了。所以加上这个运行日志记录，看看能不能找到问题在哪。但是我还没服务器，这个log文件怎么传给我还没想好，先改这些吧有空再说


Version 1.9.4

  1.修改定位历史的删除逻辑
  
  2.修改未开启GPS时地图SDK初始化失败的bug
  
  3.优化关键字搜索
  
  4.添加手动输入经纬度定位(这里使用的是BD09坐标系，可以使用百度地图坐标拾取器获取。国内的经纬度定位基本上没啥事，
    但是输入国外的经纬度定位好像会有些问题，还不知道为啥)

Version 1.9.3

  1.添加搜索历史

Version 1.9.2

  1.优化主页面关键字搜索，从所在城市内的关键字搜索修改为全国范围内的模糊搜索

Version 1.9.1
  
  1.修改 无网络时定位出错的Bug
  
  2.修改 无网络时离线地图下载导致进程崩溃的Bug
  

