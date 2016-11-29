
#include <stdio.h>
#include "rpc.h"

#ifndef ARDUINO
#include "mock_serial.h"
#define PRINT printf
#else
#include <Arduino.h>
#define PRINT Serial.print
#endif

namespace tests {

using namespace rpc;
using rpc::float_t;
using rpc::int_t;

#define RUN_TEST(NAME) \
  PRINT("starting " #NAME "\n"); \
  result = NAME(); \
  PRINT("finished " #NAME ": "); \
  PRINT(result ? "PASSED\n" : "FAILED\n"); \
  success &= result;

#define ASSERT(EXPR) if(!(EXPR)) {\
  PRINT("Not true that \"" #EXPR "\"\n"); \
  return false; \
  }

// Convert args to and from byte form.
bool test_callbackArgs() {
  CallbackArgs args;
  
  args.from_int(12345);
  ASSERT(args.to_int() == 12345);
  
  args.from_float(10.1f);
  ASSERT(args.to_float() == 10.1f);
  
  return true;
}

// This is a dirty test, and is totally not atomic, but testing on microcontrollers is awkward.
// What would be super nice would be to have a mocking framework, but OH WELL.
DECL_RPC_CALLBACK2(hello, longlong, float);
longlong long_arg;
float float_arg;
bool test_tryProcess() {
  
  hello_Callback callback;
  callback.tryProcess("hello 000000000000000F3DCCCCCD");
  ASSERT(long_arg == 15);
  ASSERT(float_arg == 0.1f);
  
  return true;
}

void hello(longlong a, float b) {
  long_arg = a;
  float_arg = b;
}

bool run_suite() {
  bool success = true;
  bool result = false;
  PRINT("Starting tests...\n");
  
  RUN_TEST(test_callbackArgs);
  RUN_TEST(test_tryProcess);
  
  PRINT("Finished\n");
  return success;
}

} // namespace tests

#ifndef ARDUINO
int main(int argc, char** argv) {
  return tests::run_suite();
}

#endif
