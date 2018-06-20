package com.pranjaldesai.getfit;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PushUpWidgetService extends IntentService {

    private static final String ACTION_PUSHUP = "com.pranjaldesai.getfit.action.PUSHUP";


    public PushUpWidgetService() {
        super("PushUpWidgetService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPushUp(Context context, String param1) {
        Intent intent = new Intent(context, PushUpWidgetService.class);
        intent.setAction(ACTION_PUSHUP);
        intent.putExtra(context.getResources().getString(R.string.totalPushUps), param1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PUSHUP.equals(action)) {
                String pushUps= (String) intent.getExtras().get(getResources().getString(R.string.totalPushUps));
                handleActionPushUp(pushUps);
            }
        }
    }

    private void handleActionPushUp(String param1) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TotalPushupsWidget.class));
        TotalPushupsWidget.updatePushUpsWidgets(this, appWidgetManager, param1, appWidgetIds);
    }

}
