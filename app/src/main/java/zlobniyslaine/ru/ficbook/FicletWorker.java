package zlobniyslaine.ru.ficbook;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

public class FicletWorker extends Worker {

    public FicletWorker(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
    }

    @NotNull
    @Override
    public Result doWork() {
        Log.i("WORK", "Run");
        Application.checkNotifications();

        return ListenableWorker.Result.success();
    }

    @Override
    public void onStopped() {
        // Cleanup because you are being stopped.
    }
}