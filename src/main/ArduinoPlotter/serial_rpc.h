
/**
 * Serial RPC library.
 */

/**
 * Send the following RPC command.
 * Should be called by API delegate libraries.
 * e.g.
 * 
 * void sendUpdate(int status) {
 *   serialRpcSend("update", 1, status);
 * }
 */
void serialRpcSend(char* commandName, int numArgs, ...);

// Would like to have 
//#define SERIAL_RPC1(commandName, type ) void commandName(...) {callCommand(encodeArgs(...)) ;}

/**
 * Process incoming messages.
 */
void serialRpcProcess();

