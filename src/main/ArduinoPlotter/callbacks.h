
#ifndef CALLBACKS_H
#define CALLBACKS_H

typedef long long longlong;

enum Type {
  int_t,
  longlong_t,
  float_t,
};

union CallbackArgs {
  int int_v;
  float float_v;
  longlong longlong_v;
};

// Would like to make this generic. Difficult to do with the preprocessor, though, since it can't
// recurse or operate conditionally. Could potentially use something awful with templates.
// OR, could have a python preprocessor that generates DECL_CALLBACK1 through N.
// May be nice to have a DECL_CALLBACK0 too.

#define DECL_CALLBACK1(NAME, T1) \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME) {\
    _types = new Type[1]; \
    _types[0] = T1 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME (args[0].T1 ## _v); \
  } \
}

#define DECL_CALLBACK2(NAME, T1, T2) \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME) {\
    _types = new Type[2]; \
    _types[0] = T1 ## _t; \
    _types[1] = T2 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME (args[0].T1 ## _v, args[1].T2 ## _v); \
  } \
}

#define DECL_CALLBACK3(NAME, T1, T2, T3) \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME) {\
    _types = new Type[3]; \
    _types[0] = T1 ## _t; \
    _types[1] = T2 ## _t; \
    _types[2] = T3 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME (\
      args[0].T1 ## _v, \
      args[1].T2 ## _v, \
      args[2].T3 ## _v); \
  } \
}

#define REGISTER_CALLBACK(NAME) addCallback(new NAME ## _Callback())

class Callback {
public:
  Callback(char* name) : _name(name) {}
  void tryProcess(char* command);
protected:
  virtual void doStuff(CallbackArgs args[]) = 0;
  Type *_types;
private:
  char* _name;
};

#define TOTAL_CALLBACKS 10

void addCallback(Callback* callback);
void processCallbacks(char* command);

#endif /* CALLBACKS_H */

