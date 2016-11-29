CC=gcc
CXX=g++
RM=rm -f
CPPFLAGS=-g -c -D_REENTRANT -pthread -I/sw/include/root
LDFLAGS=-g
LDLIBS=

SRCS=mock_serial.cpp rpc.cpp tests.cpp
OBJS=$(subst .cpp,.o,$(SRCS))

all: tests

tests: $(OBJS)
	$(CXX) $(LDFLAGS) -o tests $(OBJS) $(LDLIBS) 

depend: .depend

.depend: $(SRCS)
	rm -f ./.depend
	$(CXX) $(CPPFLAGS) -MM $^>>./.depend;

clean:
	$(RM) $(OBJS)

dist-clean: clean
	$(RM) *~ .depend

include .depend
