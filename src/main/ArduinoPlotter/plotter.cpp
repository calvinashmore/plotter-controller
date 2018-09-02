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
AccelStepper stepper1(AccelStepper::DRIVER, 22, 24); // X
AccelStepper stepper2(AccelStepper::DRIVER, 26, 28); // Y
AccelStepper stepper3(AccelStepper::DRIVER, 32, 30); // yaw
MultiStepper multiStepper;
Servo servo1; // pitch
Servo servo2; // pressure
LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

#define ENABLE_PIN 38
#define LIMIT_X_MIN_PIN 42
#define LIMIT_X_MAX_PIN 46
#define LIMIT_Y_MIN_PIN 40
#define LIMIT_Y_MAX_PIN 44
#define DELTA 5

#define MIN_STEPPER_SPEED 100
#define MAX_STEPPER_SPEED 900

// State
Data currentData;
Data nextData;
bool hasData = false;
bool isStarted = false;
bool limitXMinReached = false;
bool limitXMaxReached = false;
bool limitYMinReached = false;
bool limitYMaxReached = false;
longlong limitXMinValue;
longlong limitXMaxValue;
longlong limitYMinValue;
longlong limitYMaxValue;

// "private" methods
void enableSteppers(boolean);
void doStuff();
void readyForNextData();

// RPCs
DECL_RPC_CALLBACK1(enable, int);
DECL_RPC_CALLBACK7(handleData, longlong, float, float, float, float, float, float);
DECL_RPC_CALLBACK2(moveXY, float, float);
DECL_RPC_CALLBACK2(moveAxis, int, float);
IMPL_RPC_CALL1(reportProgress, longlong);
IMPL_RPC_CALL3(reportLimit, int, int, longlong); // axis, max vs min, value

void setup() {
  
  REGISTER_RPC_CALLBACK(enable);
  REGISTER_RPC_CALLBACK(handleData);
  REGISTER_RPC_CALLBACK(moveXY);
  REGISTER_RPC_CALLBACK(moveAxis);

  // set up the LCD's number of columns and rows:
  lcd.begin(16, 2);
  // Print a message to the LCD.
  Serial.begin(9600);

  multiStepper.addStepper(stepper1);
  multiStepper.addStepper(stepper2);
  multiStepper.addStepper(stepper3);

  pinMode(ENABLE_PIN, OUTPUT);
  servo1.attach(34);
  servo2.attach(36);
  
  pinMode(LIMIT_X_MIN_PIN, INPUT);
  pinMode(LIMIT_X_MAX_PIN, INPUT);
  pinMode(LIMIT_Y_MIN_PIN, INPUT);
  pinMode(LIMIT_Y_MAX_PIN, INPUT);
}

void loop() {
  if (isStarted) {
    checkLimits();
    enableSteppers(true);
    if (hasData) {
      doStuff();
    } else {
      lcd.setCursor(0, 0);
      lcd.print("Waiting");
      lcd.setCursor(0, 1);
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
//  lcd.setCursor(0, 0);
//  lcd.print((unsigned long) currentData.index);
//  lcd.print("        ");
//  lcd.setCursor(0, 1);

  long stepperCoordinates[3];
  stepperCoordinates[0] = (long)(100*currentData.position_x);
  stepperCoordinates[1] = (long)(100*currentData.position_y);
  stepperCoordinates[2] = (long)(100*currentData.yaw);

  // Apply limits
  if (limitXMinReached) {
    stepperCoordinates[0] = max(stepperCoordinates[0], limitXMinValue);
  }
  if (limitXMaxReached) {
    stepperCoordinates[0] = min(stepperCoordinates[0], limitXMaxValue);
  }
  if (limitYMinReached) {
    stepperCoordinates[1] = max(stepperCoordinates[1], limitYMinValue);
  }
  if (limitYMaxReached) {
    stepperCoordinates[1] = min(stepperCoordinates[1], limitYMaxValue);
  }
  
  // speed in steps/second
  // > 1000 are unreliable.
  float stepperSpeed = min(MAX_STEPPER_SPEED, max(100*currentData.speed, MIN_STEPPER_SPEED));
  int servo1Coordinates = (int)(currentData.pitch);
  int servo2Coordinates = (int)(currentData.pressure_z);
  
  stepper1.setMaxSpeed(stepperSpeed);
  stepper2.setMaxSpeed(stepperSpeed);
  stepper3.setMaxSpeed(stepperSpeed);
  multiStepper.moveTo(stepperCoordinates);
  multiStepper.run();

  servo1.write(servo1Coordinates);
  servo2.write(servo2Coordinates);

  int totalToGo = abs(stepper1.distanceToGo()) + abs(stepper2.distanceToGo());

//  lcd.print(stepperSpeed);
//  lcd.print(" ");
//  lcd.print(totalToGo);
//  lcd.print(" ");
//  lcd.print(currentData.position_x);

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
  if (enabled) {
    isStarted = true;
  }

  if (!enabled) {
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

void moveXY(float position_x, float position_y) {
  currentData.position_x = position_x;
  currentData.position_y = position_y;
  hasData = true;
}

void moveAxis(int axis, float value) {
  switch(axis) {
  case 0:
    currentData.position_x = value;
    break;
  case 1:
    currentData.position_y = value;
    break;
  case 2:
    currentData.speed = value;
    break;
  case 3:
    currentData.pressure_z = value;
    break;
  case 4:
    currentData.pitch = value;
    break;
  case 5:
    currentData.yaw = value;
    break;
  }
  nextData = currentData;
  hasData = true;
}

void checkLimits() {
  if (digitalRead(LIMIT_X_MIN_PIN) && !limitXMinReached) {
    limitXMinReached = true;
    limitXMinValue = stepper1.currentPosition();
    reportLimit(0, -1, limitXMinValue);
  }
  if (digitalRead(LIMIT_X_MAX_PIN) && !limitXMaxReached) {
    limitXMaxReached = true;
    limitXMaxValue = stepper1.currentPosition();
    reportLimit(0, 1, limitXMaxValue);
  }
  
  if (digitalRead(LIMIT_Y_MIN_PIN) && !limitYMinReached) {
    limitYMinReached = true;
    limitYMinValue = stepper2.currentPosition();
    reportLimit(1, -1, limitYMinValue);
  }
  if (digitalRead(LIMIT_Y_MAX_PIN) && !limitYMaxReached) {
    limitYMaxReached = true;
    limitYMaxValue = stepper2.currentPosition();
    reportLimit(1, 1, limitYMaxValue);
  }
}

} // namespace plotter

