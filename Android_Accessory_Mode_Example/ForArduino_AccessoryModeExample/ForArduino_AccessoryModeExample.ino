//要先导入UsbHost库
#include <adk.h>
USB Usb;
ADK adk(&Usb, "xh", // Manufacturer Name****设置正确手机才能自动识别*****
        "Arduino Uno", // Model Name ****设置正确手机才能自动识别*****
        "Example sketch for the USB Host Shield", // Description (user-visible string)
        "1.0", // Version    ****设置正确手机才能自动识别*****
        "", // *****自动下载手机软件地址*******URL (web page to visit if no installed apps support the accessory)
        "123456789"); // Serial Number (optional)

uint32_t timer;

void setup() {
  Serial.begin(115200);
  while (!Serial); // Wait for serial port to connect - used on Leonardo, Teensy and other boards with built-in USB CDC serial connection
  if (Usb.Init() == -1) {
    Serial.print("\r\nOSCOKIRQ failed to assert");
    while (1); // halt
  }
  Serial.println("Arduino Started");
}

String sendBuffer = "";
String receiveBuffer = "";

void loop() {
  Usb.Task();
  if (!adk.isReady()) {
    return;
  }
  uint8_t rcode;
  //接收
  uint8_t msg[64] = { 0x00 };
  uint16_t len = sizeof(msg);
  adk.RcvData(&len, msg);
  if (len > 0) {
    Serial.print("receive msg:");
    for ( int i = 0; i < len; i++ ) {
      receiveBuffer += (char)msg[i];
    }
    Serial.println(receiveBuffer);
    receiveBuffer = "";
  }

  ///发送,缓冲区有内容就发送
  int slen = sendBuffer.length();
  if (slen > 0) {
    if (slen > 64) { //最多64位
      slen = 64;
    }
    char sendChar[64];
    sendBuffer.toCharArray(sendChar, slen + 1);//不知道为什么要加1
    adk.SndData(slen, (uint8_t *)sendChar);

    Serial.print(rcode);
    Serial.print(":");
    /////////////////////////////
    ///////////////////////////////////////
    ////未知原因，出现发送命令滞后一个现象,
    ////所以追加一个空格，把前个命令“推出去”
    Usb.Task();
    char enter[] = " ";
    rcode = adk.SndData(1, (uint8_t *)enter);
    ////////////////////////////////////////
    /////////////////////////////////////////
    Serial.print(sendBuffer);
    sendBuffer = "";
  }


  ///发送，数据加入缓冲，循环到了就会发送
  if (millis() < timer) { //millis()溢出了（大概要50天）
    timer = millis();
  }
  if (millis() - timer >= 1000) { // Send data every 1s
    timer = millis();
    sendBuffer += timer;
    sendBuffer += "\n";
  }
}

