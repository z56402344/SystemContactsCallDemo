package com.example.callplusdemo.service;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

public final class ContactsSyncAdapterService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        SyncAdapterImpl syncAdapter = new SyncAdapterImpl(this.getApplicationContext());
        Log.d("bugtags", "ContactsSyncAdapterService-->onBind");
        return syncAdapter.getSyncAdapterBinder();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

        private Context mContext;

        public SyncAdapterImpl(Context context){
            super(context, true);
            this.mContext = context;
            Log.d("bugtags", "SyncAdapterImpl-->");
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            Log.d("bugtags", "SyncAdapterImpl-->onPerformSync()");
        }
    }
}
