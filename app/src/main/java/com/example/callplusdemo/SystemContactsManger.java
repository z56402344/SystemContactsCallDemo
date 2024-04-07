package com.example.callplusdemo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import java.util.ArrayList;

public final class SystemContactsManger {

    private static class Holder {
        static SystemContactsManger utils = new SystemContactsManger();
    }

    private SystemContactsManger() {}
    public static SystemContactsManger getInstance() {
        return Holder.utils;
    }

    private final String ACCOUNT_NAME = "SystemContactsCallDemo";
    //必须和 app/src/main/res/xml/sync_contacts.xml 文件中内容一致
    private final String ACCOUNT_TYPE = "com.example.callplusdemo";

    //必须和 app/src/main/res/xml/contacts.xml 文件中内容一致
    //需要在 AndroidManifest.xml 中使用 intent-filter 注册
    public final String AUDIO_CALL = "vnd.android.cursor.item/vnd.com.example.callplusdemo.audiocall";

    //必须和 app/src/main/res/xml/contacts.xml 文件中内容一致
    //需要在 AndroidManifest.xml 中使用 intent-filter 注册
    public final String VIDEO_CALL = "vnd.android.cursor.item/vnd.com.example.callplusdemo.videocall";

    private final String[] SELECTION_ARGS = new String[]{ACCOUNT_TYPE};
    private final String[] PROJECTION = {
        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.TYPE,
        ContactsContract.CommonDataKinds.Phone.LABEL,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        RawContacts.ACCOUNT_TYPE
    };

    public void addAccount(Context context) {
        try {
            Account account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
            AccountManager.get(context).addAccountExplicitly(account, "", null);
        } catch (Exception e) {
            Log.e("bugtags", "SystemContactsManger-->addAccount()-->exception: " +e.getMessage());
        }
    }

    public void addContact(Context context, String remoteUseName, String remoteUserPhone, String remoteUserId) {
        if (!hasContactsPermission(context)) {
            return;
        }

        deleteOld(context, remoteUserId);

        ArrayList<ContentProviderOperation> providerOperationList = new ArrayList<>();

        ContentProviderOperation operationId = ContentProviderOperation.
            newInsert(appendQueryParameter(RawContacts.CONTENT_URI))
            .withYieldAllowed(true)
            .withValue(RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
            .withValue(RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .withValue(RawContacts.SYNC2, remoteUserId)
            .build();
        providerOperationList.add(operationId);

        ContentProviderOperation operationUngroupedVisible =   ContentProviderOperation.newInsert(
                appendQueryParameter(ContactsContract.Settings.CONTENT_URI))
            .withYieldAllowed(true)
            .withValue(RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
            .withValue(RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 1)
            .build();
        providerOperationList.add(operationUngroupedVisible);

        ContentProviderOperation operationName = ContentProviderOperation.newInsert(appendQueryParameter(ContactsContract.Data.CONTENT_URI))
            .withYieldAllowed(true)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, remoteUseName)
            .build();
        providerOperationList.add(operationName);

        ContentProviderOperation operationPhoneNumber=ContentProviderOperation.newInsert(appendQueryParameter(ContactsContract.Data.CONTENT_URI))
            .withYieldAllowed(true)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, remoteUserPhone)
            .build();
        providerOperationList.add(operationPhoneNumber);

        // custom mimeType
        ContentProviderOperation operationVoiceRawContactId = ContentProviderOperation.newInsert(appendQueryParameter(ContactsContract.Data.CONTENT_URI))
            .withYieldAllowed(true)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, AUDIO_CALL)
            .withValue(ContactsContract.Data.DATA1, remoteUserPhone)
            .withValue(ContactsContract.Data.DATA2, ACCOUNT_NAME +" Voice Call " + remoteUserPhone)
            .withValue(ContactsContract.Data.DATA3, remoteUserId)
            .build();
        providerOperationList.add(operationVoiceRawContactId);

        ContentProviderOperation operationVideoRawContactId = ContentProviderOperation.newInsert(appendQueryParameter(ContactsContract.Data.CONTENT_URI))
            .withYieldAllowed(true)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, VIDEO_CALL)
            .withValue(ContactsContract.Data.DATA1, remoteUserPhone)
            .withValue(ContactsContract.Data.DATA2, ACCOUNT_NAME +" Video Call " + remoteUserPhone)
            .withValue(ContactsContract.Data.DATA3, remoteUserId)
            .build();
        providerOperationList.add(operationVideoRawContactId);


        ContentResolver contentResolver = context.getContentResolver();

        try {
            ContentProviderResult[] contentProviderResults = contentResolver.applyBatch(ContactsContract.AUTHORITY, providerOperationList);
            for (ContentProviderResult contentProviderResult : contentProviderResults) {
                Log.e("bugtags", "contentProviderResult: " +contentProviderResult.toString());
            }
        } catch (OperationApplicationException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private Uri appendQueryParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    private boolean hasContactsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        }

        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION,null,null,null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    private void deleteOld(Context context, String id) {
        Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(RawContacts.ACCOUNT_NAME, ACCOUNT_NAME)
            .appendQueryParameter(RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE)
            .build();

        ContentResolver contentResolver = context.getContentResolver();

        String[] selectionArgs = new String[]{id};
        contentResolver.delete(rawContactUri, RawContacts.SYNC2 + " = ?", selectionArgs);
        Log.d("bugtags", "SystemContactsManger-->deleteOld()-->id: " + id);
    }

    public void clearAll(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(RawContacts.CONTENT_URI, RawContacts.ACCOUNT_TYPE + " = ?", SELECTION_ARGS);
        Log.d("bugtags", "SystemContactsManger-->clearAll()");
    }
}
