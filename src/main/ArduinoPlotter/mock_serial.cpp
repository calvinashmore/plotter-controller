
#include <stdio.h>
#include "mock_serial.h"

MockSerial Serial;

bool MockSerial::available() {
  return false;
}

char MockSerial::read() {
  return 0;
}

void MockSerial::print(char* s) {
  printf(s);
}

void MockSerial::println() {
  printf("\n");
}
