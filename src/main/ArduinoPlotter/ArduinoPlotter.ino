#include <AccelStepper.h>
#include <MultiStepper.h>
#include <LiquidCrystal.h>
#include <Servo.h>

AccelStepper stepper1(AccelStepper::DRIVER, 9, 8);
AccelStepper stepper2(AccelStepper::DRIVER, 11, 10);

MultiStepper multiStepper;
Servo servo1;

LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

#define ENABLE_PIN 12

void setup() {
  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  // Print a message to the LCD.
  Serial.begin(9600);

  multiStepper.addStepper(stepper1);
  multiStepper.addStepper(stepper2);

  pinMode(ENABLE_PIN, OUTPUT);
  servo1.attach(13);
}

#define ITEMS 6
#define START_BLOCK 0xDEADBEEF
#define VERIFIER    0x777777A7
#define HANDSHAKE_CONFIRM 0x7887655653351FF1L
#define DELTA 10

struct Data {
  long long index;
  float values[ITEMS];
  int verifier;
};

Data currentData;
bool hasHandshake = false;

void loop() {

  if(hasHandshake) {
    if(currentData.verifier == VERIFIER) {
      enableSteppers(true);
      lcd.setCursor(0, 0);
      lcd.print((unsigned long) currentData.index);
      lcd.print("        ");
      lcd.setCursor(0, 1);

      long stepperCoodinates[2];
      stepperCoodinates[0] = (long)(100*currentData.values[0]);
      stepperCoodinates[1] = (long)(100*currentData.values[1]);
      
      // speed in steps/second
      // > 1000 are unreliable.
      float stepperSpeed = max(100*currentData.values[2],10);
      int servoCoordinates = (int)(currentData.values[3]);

      int totalToGo = abs(stepper1.distanceToGo()) + abs(stepper2.distanceToGo());
  
      lcd.print(stepperSpeed);
      lcd.print(" ");
      lcd.print(totalToGo);
      lcd.print(" ");
      
      stepper1.setMaxSpeed(stepperSpeed);
      stepper2.setMaxSpeed(stepperSpeed);
      multiStepper.moveTo(stepperCoodinates);
      multiStepper.run();
  
      servo1.write(servoCoordinates);
  
      if(totalToGo < DELTA) {
        readyForNextData();
      }
    } else {
      // handshake but no verifier
      lcd.setCursor(0, 0);
      lcd.print("Handshake");
      lcd.setCursor(0, 1);
      lcd.print(currentData.verifier);

      // Let's get the java server onboard
      writeLong(HANDSHAKE_CONFIRM);
    }
    
  } else {
    lcd.setCursor(0, 0);
    lcd.print("Ready");
    lcd.setCursor(0, 1);
    enableSteppers(false);
  }
  
  if(Serial.available()) {
    if(hasHandshake) {
      // Read data normally
      readUntilStartBlock();
      Serial.readBytes((byte*) &currentData.index, sizeof(long long));
      for(int i=0;i<ITEMS;i++) {
        Serial.readBytes((byte*) &currentData.values[i], sizeof(float));
      }
      Serial.readBytes((byte*) &currentData.verifier, sizeof(int));
    } else {
      // Read until we can get the handshake.
      readUntilHandshake();
      hasHandshake = true;
      delay(1000);
    }
  }
}

void writeLong(long long data) {
  Serial.write((byte*) &data, sizeof(long long));
}

void readyForNextData() {
  writeLong(currentData.index);
}

void enableSteppers(boolean enabled) {
  if (enabled) {
    digitalWrite(ENABLE_PIN, LOW);
  } else {
    digitalWrite(ENABLE_PIN, HIGH);
  }
}

void readUntilHandshake() {
  while(true) {
    int b;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 0)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 1)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 2)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 3)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 4)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 5)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 6)) continue;
    b = blockingReadByte();
    if (b != longByte(HANDSHAKE_CONFIRM, 7)) continue;
    return;
  }
}

void readUntilStartBlock() {
  // Read until we get the START_BLOCK
  // Remember this is little endian,
  // DEADBEEF should appear in this order:
  // EF BE AD DE
  while(true) {
    int b;
    b = blockingReadByte();
    if (b != intByte(START_BLOCK, 0)) continue;
    b = blockingReadByte();
    if (b != intByte(START_BLOCK, 1)) continue;
    b = blockingReadByte();
    if (b != intByte(START_BLOCK, 2)) continue;
    b = blockingReadByte();
    if (b != intByte(START_BLOCK, 3)) continue;
    return;
  }
}

int intByte(int data, int pos) {
  return (data >> (8*pos)) & 0xFF;
}

int longByte(long long data, int pos) {
  return (data >> (8*pos)) & 0xFF;
}

byte blockingReadByte() {
  byte b[1];
  Serial.readBytes(b,1);
  return b[0];
}

void hexByte(int b, char* s) {
  sprintf(s, "%02X", b);
}
