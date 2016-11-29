
#include <stdio.h>
#include "rpc.h"
#include "mock_serial.h"

using namespace rpc;

#define RUN_TEST(NAME) \
  printf("starting " #NAME "\n"); \
  result = NAME(); \
  printf("finished " #NAME ": "); \
  printf(result ? "PASSED\n" : "FAILED\n"); \
  success &= result;

#define ASSERT(EXPR) if(!(EXPR)) {\
  printf("Not true that \"" #EXPR "\"\n"); \
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
//extern void processCallbacks(char* command);
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

int main(int argc, char** argv) {
  bool success = true;
  bool result = false;
  printf("Starting tests...\n");
  
  RUN_TEST(test_callbackArgs);
  RUN_TEST(test_tryProcess);
  
  printf("Finished\n");
  return success;
}
