package com.example.callplusdemo;

public class App extends android.app.Application {

    private static App INSTANCE;

    @Override
    public void onCreate() {
        INSTANCE = this;
        super.onCreate();
        SessionManager.initContext(this);
    }

    public static App getApplication() {
        return INSTANCE;
    }
}
