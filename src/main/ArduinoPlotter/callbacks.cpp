
#include <stdio.h>
#include <string.h>
#include "callbacks.h"

typedef unsigned char byte;

// Utility method
void parseHex(char* hexString, byte* out, size_t length) {
  char* pos = hexString;
  for (int count = length-1; count >= 0; count--) {
    sscanf(pos, "%2hhx", &out[count]);
    pos += 2;
  }
}

int typeSizeof(Type t) {
  switch (t) {
    case int_t: return 4;
    case float_t: return 4;
    case longlong_t: return 8;
  }
  return 0;
}

void Callback::tryProcess(char* command) {
  if(strncmp(command, _name, strlen(_name)) == 0 && 
          command[strlen(_name)] == ' ' ) {
    
    char* args_string = command + strlen(_name) + 1;
    
    int arg_count = sizeof(_types);
    CallbackArgs args[arg_count];
    
    for(int i=0; i<arg_count; i++) {
      Type t = _types[i];
      parseHex(args_string, (byte*) &args[i], typeSizeof(t));
      args_string += 2*typeSizeof(t);
    }
    
    doStuff(args);
  }
}

int totalCallbacks = 0;
Callback *allCallbacks[TOTAL_CALLBACKS];

void addCallback(Callback* callback) {
  allCallbacks[totalCallbacks++] = callback;
}

void processCallbacks(char* command) {
  for (int i=0; i<totalCallbacks; i++) {
    allCallbacks[i]->tryProcess(command);
  }
}

