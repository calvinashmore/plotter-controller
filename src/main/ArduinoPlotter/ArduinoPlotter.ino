
// Enable/disable tests
//#define TESTING

#ifndef TESTING

#include "plotter.h"
void setup() {
  plotter::setup();
}

void loop() {
  plotter::loop();
}

#else // TESTING

#include "tests.h"
void setup() {
  // so we can get the result of the tests streamed to serial
  Serial.begin(9600);
  tests::run_suite();
}

void loop() {}

#endif // TESTING
