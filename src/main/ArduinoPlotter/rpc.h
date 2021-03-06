
#ifndef RPC_H
#define RPC_H

typedef unsigned char byte;

namespace rpc {

// Use this to avoid issues with macro concatenation
typedef long long longlong;

// Type of a rpc parameter
enum Type {
  int_t,
  longlong_t,
  float_t,
};

// Holder of an rpc parameter
#define CALLBACK_ARGS_DATA_SIZE 8
struct CallbackArgs {
  byte data[CALLBACK_ARGS_DATA_SIZE];

  int to_int() {
    return *((int*)&data[0]);
  }

  float to_float() {
    return *((float*)&data[0]);
  }

  longlong to_longlong() {
    return *((longlong*)&data[0]);
  }

  void from_int(int x) {
    ((int*)&data[0])[0] = x;
  }
  
  void from_float(float x) {
    ((float*)&data[0])[0] = x;
  }

  void from_longlong(longlong x) {
    ((longlong*)&data[0])[0] = x;
  }
};

// Would like to make this generic. Difficult to do with the preprocessor, though, since it can't
// recurse or operate conditionally. Could potentially use something awful with templates.
// OR, could have a python preprocessor that generates DECL_CALLBACK1 through N.
// May be nice to have a DECL_CALLBACK0 too.

#define IMPL_RPC_CALL1(NAME, T1) \
void NAME (T1 v1) { \
  Type types[] = {T1 ## _t}; \
  CallbackArgs args[1]; \
  args[0]. from_ ## T1 (v1); \
  callClientRpc(#NAME, types, args); \
}

#define IMPL_RPC_CALL2(NAME, T1, T2) \
void NAME (T1 v1, T2 v2) { \
  Type types[] = { \
      T1 ## _t, \
      T2 ## _t}; \
  CallbackArgs args[2]; \
  args[0]. from_ ## T1 (v1); \
  args[1]. from_ ## T2 (v2); \
  callClientRpc(#NAME, types, args); \
}

#define IMPL_RPC_CALL3(NAME, T1, T2, T3) \
void NAME (T1 v1, T2 v2, T3 v3) { \
  Type types[] = { \
      T1 ## _t, \
      T2 ## _t, \
      T3 ## _t}; \
  CallbackArgs args[3]; \
  args[0]. from_ ## T1 (v1); \
  args[1]. from_ ## T2 (v2); \
  args[2]. from_ ## T3 (v3); \
  callClientRpc(#NAME, types, args); \
}

#define DECL_RPC_CALLBACK1(NAME, T1) \
void NAME (T1); \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME, 1) {\
    _types = new Type[1]; \
    _types[0] = T1 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME ( \
      args[0].to_ ## T1 ()); \
  } \
}

#define DECL_RPC_CALLBACK2(NAME, T1, T2) \
void NAME (T1, T2); \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME, 2) {\
    _types = new Type[2]; \
    _types[0] = T1 ## _t; \
    _types[1] = T2 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME ( \
      args[0].to_ ## T1 (), \
      args[1].to_ ## T2 ()); \
  } \
}

#define DECL_RPC_CALLBACK3(NAME, T1, T2, T3) \
void NAME (T1, T2, T3); \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME, 3) {\
    _types = new Type[3]; \
    _types[0] = T1 ## _t; \
    _types[1] = T2 ## _t; \
    _types[2] = T3 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME (\
      args[0].to_ ## T1 (), \
      args[1].to_ ## T2 (), \
      args[2].to_ ## T3 ()); \
  } \
}

// ...

#define DECL_RPC_CALLBACK7(NAME, T1, T2, T3, T4, T5, T6, T7) \
void NAME (T1, T2, T3, T4, T5, T6, T7); \
class NAME ## _Callback : public Callback { \
public: \
  NAME ## _Callback() : Callback(#NAME, 7) {\
    _types = new Type[7]; \
    _types[0] = T1 ## _t; \
    _types[1] = T2 ## _t; \
    _types[2] = T3 ## _t; \
    _types[3] = T4 ## _t; \
    _types[4] = T5 ## _t; \
    _types[5] = T6 ## _t; \
    _types[6] = T7 ## _t; \
  } \
protected: \
  void doStuff(CallbackArgs args[]) { \
    NAME (\
      args[0].to_ ## T1 (), \
      args[1].to_ ## T2 (), \
      args[2].to_ ## T3 (), \
      args[3].to_ ## T4 (), \
      args[4].to_ ## T5 (), \
      args[5].to_ ## T6 (), \
      args[6].to_ ## T7 ()); \
  } \
}

#define REGISTER_RPC_CALLBACK(NAME) addCallback(new NAME ## _Callback())

class Callback {
public:
  Callback(char* name, int arg_count) : _name(name), _arg_count(arg_count) {}
  void tryProcess(char* command);
protected:
  virtual void doStuff(CallbackArgs args[]) = 0;
  Type *_types;
private:
  char* _name;
  int _arg_count;
};

// Registers a callback
void addCallback(Callback* callback);

// Handle all rpc input
void handleRpcInput();

// uses RPC
void callClientRpc(char* command, Type types[], CallbackArgs args[]);

} // namespace rpc

#endif /* RPC_H */

