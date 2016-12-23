# WhatIsWhat： 一个全语音交互的知识探索APP

## 背景

近两年都是在做人工智能的技术产品，也一直在思考 AI 能给人们带来哪些便利。某天闲聊的时候，有个妈妈同事说，她家宝宝问她很多东西不懂，只好去搜索，发现百度百科的不少词条有个“秒懂百科”，用视频讲解百科词条，宝宝很爱看。只是可惜宝宝不认字，不会自己搜索。然后我就想，要是有个工具，能用语音问问题，语音或者视频回答问题，那挺不错啊，就有了这个 APP。

把它开源出来，是有一个想法：其实每个爸爸妈妈，都希望自己给孩子的爱是特殊的呀，那 TA 就可以在这个 APP 基础上进行修改，比如在每条语音提示前都加上自己宝宝的名字，甚至直接替换成自己的录音。那这就变成了送给宝宝的一份用心的独一无二的礼物了！

## 怎么编译

### 下载代码，配置编译环境

通过 git clone，或者下载 zip 包的方式，将源代码下载到本地。
```
git clone https://github.com/solrex/WhatIsWhat.git
OR
git clone git@github.com:solrex/WhatIsWhat.git
```
安装 Android Studio，用 Import Project 将源代码目录导入到 Android Studio 中。如本地 SDK Manager 没有下载所有版本的 SDK，也许需要调整一下 app 的 build.gradle，主要是将 `compileSdkVersion, buildToolsVersion, minSdkVersion, targetSdkVersion` 这几个依赖版本，调整到本地 SDK Manager 已经有的版本。

### 申请百度语音识别调用接口（免费）

到 http://yuyin.baidu.com/ ，使用百度账号登陆，注册成为开发者。开发者审核通过后，新建一个应用。新建应用时，勾选使用“语音识别”、“语音合成”两大功能，应用包名填入“`com.ooolab.whatiswhat`”。

点击应用右侧的“查看key”链接，将显示的信息填入以下 xml 文件：

``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_id">YOUR_APP_ID</string>
    <string name="api_key">YOUR_API_KEY</string>
    <string name="secret_key">YOUR_SECRET_KEY</string>
</resources>
```
将文件保存到 `WhatIsWhat/app/src/main/res/values/apikey.xml`，这是为您的应用添加语音识别和语音合成的调用授权。

然后就可以按照一般的 Android 项目，进行编译了。

## 遇到 BUG 了怎么办？

因为这还是一个非常不成熟的 APP，甚至可以说它还只是个 demo 阶段，所以遇到 BUG 的概率应该还是很大的。

当你遇到问题时，欢迎在 Issues:  https://github.com/solrex/WhatIsWhat/issues 报告问题，将你的运行时环境（例如：手机型号，操作系统版本，联网情况）、复现问题的操作顺序（例如：先点击了什么，后点击了什么）、logcat 捕获的异常（如果有的话），填写到 Issue 中，以便定位和修复问题。
