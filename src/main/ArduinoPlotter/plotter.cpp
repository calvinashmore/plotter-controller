#include <AccelStepper.h>
#include <MultiStepper.h>
#include <LiquidCrystal.h>
#include <Servo.h>
#include "rpc.h"
#include "plotter.h"

namespace plotter {
  
using namespace rpc;
using rpc::float_t;
using rpc::int_t;

struct Data {
  long long index;
  float position_x;
  float position_y;
  float speed;
  float pressure_z;
  float pitch;
  float yaw;
};

// Controls
AccelStepper stepper1(AccelStepper::DRIVER, 9, 8);
AccelStepper stepper2(AccelStepper::DRIVER, 11, 10);
MultiStepper multiStepper;
Servo servo1;
LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

#define ENABLE_PIN 12
#define DELTA 5

// State
Data currentData;
Data nextData;
bool hasData = false;
bool isStarted = false;

// "private" methods
void enableSteppers(boolean);
void doStuff();
void readyForNextData();

// RPCs
DECL_RPC_CALLBACK1(enable, int);
DECL_RPC_CALLBACK7(handleData, longlong, float, float, float, float, float, float);
IMPL_RPC_CALL1(reportProgress, longlong);

void setup() {
  
  REGISTER_RPC_CALLBACK(enable);
  REGISTER_RPC_CALLBACK(handleData);

  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  // Print a message to the LCD.
  Serial.begin(9600);

  multiStepper.addStepper(stepper1);
  multiStepper.addStepper(stepper2);

  pinMode(ENABLE_PIN, OUTPUT);
  servo1.attach(13);
}

void loop() {
  if (isStarted) {
    enableSteppers(true);
    if (hasData) {
      doStuff();
    }

  } else {
    enableSteppers(false);
    lcd.setCursor(0, 0);
    lcd.print("Ready");
    lcd.setCursor(0, 1);
  }

  handleRpcInput();
}

void doStuff() {
  lcd.setCursor(0, 0);
  lcd.print((unsigned long) currentData.index);
  lcd.print("        ");
  lcd.setCursor(0, 1);

  long stepperCoodinates[2];
  stepperCoodinates[0] = (long)(100*currentData.position_x);
  stepperCoodinates[1] = (long)(100*currentData.position_y);
  
  // speed in steps/second
  // > 1000 are unreliable.
  float stepperSpeed = max(100*currentData.speed,10);
  int servoCoordinates = (int)(currentData.pitch);

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
}

void readyForNextData() {
  reportProgress(currentData.index);
  currentData = nextData;
}

void enableSteppers(boolean enabled) {
  if (enabled) {
    digitalWrite(ENABLE_PIN, LOW);
  } else {
    digitalWrite(ENABLE_PIN, HIGH);
  }
}

// RPC impls

void enable(int enabled) {
  if (enabled && !isStarted) {
    isStarted = true;
  }

  if (!enabled && isStarted) {
    isStarted = false;
    hasData = false;  
  }
}

void handleData(long long index, float position_x, float position_y, float speed, float pressure_z, float pitch, float yaw) {
  nextData.index = index;
  nextData.position_x = position_x;
  nextData.position_y = position_y;
  nextData.speed = speed;
  nextData.pressure_z = pressure_z;
  nextData.pitch = pitch;
  nextData.yaw = yaw;

  if(!hasData) {
    currentData = nextData;
    hasData = true;
  }
}

} // namespace plotter
