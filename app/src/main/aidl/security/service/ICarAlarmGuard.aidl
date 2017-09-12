// ICarAlarmGuardInterface.aidl
package security.service;

// Declare any non-default types here with import statements

interface ICarAlarmGuard {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    /** Request the process ID of this service, to do evil things with it. */
    String getPid();

    boolean setAsService();

    boolean startAlarm();

    boolean stopAlarm();

    boolean setAlarmOn();

    boolean setAlarmOff();

    boolean setAlarmTest();

    boolean isRunning();

    boolean isAlarmActivated();

    double getAltitude();

    double getLatidute();

    double getLongitude();

    void stopGPSFix();

    void shutDown();

    boolean isGPSFixPosition();

    int getStatus();

    void reloadConfig();
}