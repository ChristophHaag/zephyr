QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = zephyr
TEMPLATE = app

SOURCES += main.cpp\
        overlaywidget.cpp \
    openvroverlaycontroller.cpp \
    sio_client.cpp \
    sio_socket.cpp \
    internal/sio_client_impl.cpp \
    internal/sio_packet.cpp

HEADERS  += overlaywidget.h \
    openvroverlaycontroller.h

FORMS    += overlaywidget.ui

INCLUDEPATH += ../../websocketpp
INCLUDEPATH += ../../rapidjson/include

LIBS += -L../lib/win32 -lopenvr_api -lboost_system

DESTDIR = ../bin/win32

CONFIG+=c++11

RESOURCES += \
    resources.qrc
