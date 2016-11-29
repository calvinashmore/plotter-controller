
#ifndef MOCK_SERIAL_H
#define MOCK_SERIAL_H

class MockSerial {
public:
  bool available();
  char read();
  void print(char*);
  void println();
};

extern MockSerial Serial;

#endif // MOCK_SERIAL_H
