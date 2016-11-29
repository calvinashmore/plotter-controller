/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include <stdio.h>
#include <string.h>
#ifdef ARDUINO
#include <Arduino.h>
#else
#include "mock_serial.h"
#endif
#include "rpc.h"

namespace rpc {

//  LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

typedef unsigned char byte;

// Utility method
void parseHex(char* hexString, byte* out, size_t length) {
  char* pos = hexString;
  for (int count = length-1; count >= 0; count--) {
    sscanf(pos, "%2hhx", &out[count]);
    pos += 2;
  }
}

void formatHex(char* hexString, byte* in, size_t length) {
  char* pos = hexString;
  for (int count = 0; count < length; count++) {
    sprintf(pos, "%02hhx", in[length-count-1]);
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

    CallbackArgs args[_arg_count];

//    lcd.setCursor(0, 1);
//    lcd.print("* ");
//    lcd.print(_arg_count);
//    lcd.print(" *");
    
    for(int i=0; i<_arg_count; i++) {
      Type t = _types[i];
      size_t typeSize = typeSizeof(t);
      size_t typeOffset = 0;//CALLBACK_ARGS_DATA_SIZE - typeSize;
      parseHex(args_string, &args[i].data[typeOffset], typeSize);
      args_string += 2*typeSize;
    }

//    if(_arg_count > 1) {
//      lcd.setCursor(0, 1);
//      lcd.print("* ");
//      long k = (long)(args[1].to_longlong() >> 32);
//      lcd.print( k );
//      lcd.print(" ");
//      lcd.print(args[1].to_float());
//      lcd.print(" *");
//    }
    
    doStuff(args);
  }
}

#define TOTAL_CALLBACKS 10

// If I were a better person, this would be a proper list.
int totalCallbacks = 0;
Callback *allCallbacks[TOTAL_CALLBACKS];

void addCallback(Callback* callback) {
  if (totalCallbacks == TOTAL_CALLBACKS) {
    // Sure would be nice if we could log a warning.
    return;
  }
  allCallbacks[totalCallbacks++] = callback;
}

void processCallbacks(char* command) {
  for (int i=0; i<totalCallbacks; i++) {
    allCallbacks[i]->tryProcess(command);
  }
}

#define COMMAND_SIZE 255

char inputBuffer[COMMAND_SIZE];
int bufferIndex = 0;

void handleRpcInput() {
  while(Serial.available()) {
    char c = Serial.read();
    inputBuffer[bufferIndex++] = c;
    if (c == '\n') {
      inputBuffer[bufferIndex] = '\0';
      processCallbacks(inputBuffer);
      
      memset(inputBuffer, 0, COMMAND_SIZE);
      bufferIndex = 0;
    }
  }
}

void callClientRpc(char* command, Type types[], CallbackArgs args[]) {
  char toSend[COMMAND_SIZE];
  
  char* pos = toSend;
  
  int length = sprintf(toSend, "%s ", command);
  pos += length; //sizeof(command)+1;
  int totalLength = sizeof(command)+1;
  
  int numberArgs = sizeof(args);
  
  for (int i=0; i<numberArgs; i++) {
    Type type = types[i];
    int typeSize = typeSizeof(type);
    int argSize = 2*typeSize;
    totalLength += argSize;
    
    if(totalLength+1 > COMMAND_SIZE) {
      // error????
      return;
    }
    
    formatHex(pos, (byte*) &args[i], typeSize);
    pos += argSize;
  }

  // null terminate the string
  toSend[totalLength] = 0;
  
  // RPC send
  Serial.print("   ");
  Serial.print(toSend);
  Serial.print("   ");
  Serial.println();
}

} // namespace rpc
