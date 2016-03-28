//要先导入UsbHost库
#include <adk.h>
USB Usb;
ADK adk(&Usb, "xh", // Manufacturer Name
        "Arduino Uno", // Model Name *****必须包含 Arduino *********
        "Example sketch for the USB Host Shield", // Description (user-visible string)
        "1.0", // Version
        "", // URL (web page to visit if no installed apps support the accessory)
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
  sendMessage();
  Usb.Task();
  receiveMessage();
}

void sendMessage() {
  if (adk.isReady()) {
    if (millis() < timer) { //millis()溢出了（大概要50天）
      timer = millis();
    }
    if (millis() - timer >= 1000) { // Send data every 1s
      timer = millis();

      sendBuffer += timer;
      sendBuffer +="\r\n";
      
      int slen = sendBuffer.length();
      if (slen > 64) { //最多64位
        slen = 64;
      }
      char sendChar[64];
      sendBuffer.toCharArray(sendChar, slen + 1);
      //发送
      adk.SndData(slen, (uint8_t *)sendChar);
      Serial.print(sendBuffer);
      sendBuffer = "";
    }
  }
}

void receiveMessage() {
  if (adk.isReady()) {
    //接收
    uint8_t msg[64] = { 0x00 };
    uint16_t len = 64;
    adk.RcvData(&len, msg);
    if (len > 0) {
      Serial.print("receive msg:");
      for ( int i = 0; i < len; i++ ) {
        receiveBuffer += (char)msg[i];
      }
      Serial.println(receiveBuffer);
      receiveBuffer = "";
    }
  }
}

