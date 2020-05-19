package ydk.core.activityresult;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public final class RxActivityResult {
    static ActivitiesLifecycleCallbacks activitiesLifecycle;
    static final String RX_ACTIVITY_RESULT_NOT_REGISTER = "使用startIntent前必须调用RxActivityResult.register(application)";

    private RxActivityResult() {
    }

    public static void register(final Application application) {
        activitiesLifecycle = new ActivitiesLifecycleCallbacks(application);
    }

    public static <T extends Activity> Builder<T> on(T activity) {
        return new Builder<T>(activity);
    }

    public static <T extends Fragment> Builder<T> on(T fragment) {
        return new Builder<T>(fragment);
    }
	
	 public static Activity getLiveActivity() {
        if (activitiesLifecycle == null) {
            return null;
        }
        return activitiesLifecycle.getLiveActivity();
    }

    public static class Builder<T> {
        final Class clazz;
        final PublishSubject<Result> subject = PublishSubject.create();
        private final boolean uiTargetActivity;

        public Builder(T t) {
            if (activitiesLifecycle == null) {
                throw new IllegalStateException(RX_ACTIVITY_RESULT_NOT_REGISTER);
            }

            this.clazz = t.getClass();
            this.uiTargetActivity = t instanceof Activity;
        }

        public Observable<Result> startIntentSender(IntentSender intentSender, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) {
            return startIntentSender(intentSender, fillInIntent, flagsMask, flagsValues, extraFlags, null);
        }

        public Observable<Result> startIntentSender(IntentSender intentSender, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) {
            RequestIntentSender requestIntentSender = new RequestIntentSender(intentSender, fillInIntent, flagsMask, flagsValues, extraFlags, options);
            return startHolderActivity(requestIntentSender, null);
        }

        public Observable<Result> startIntent(final Intent intent) {
            return startIntent(intent, null);
        }

        public Observable<Result> startIntent(final Intent intent, @Nullable OnPreResult onPreResult) {
            return startHolderActivity(new Request(intent), onPreResult);
        }

        private Observable<Result> startHolderActivity(Request request, @Nullable OnPreResult onPreResult) {

            OnResult onResult = uiTargetActivity ? onResultActivity() : onResultFragment();
            request.setOnResult(onResult);
            request.setOnPreResult(onPreResult);

            HolderActivity.setRequest(request);

            activitiesLifecycle.getOLiveActivity().subscribe(new Consumer<Activity>() {
                @Override
                public void accept(Activity activity) throws Exception {
                    activity.startActivity(new Intent(activity, HolderActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                }
            });

            return subject;
        }

        private OnResult onResultActivity() {
            return new OnResult() {
                @Override
                public void response(int requestCode, int resultCode, Intent data) {
                    subject.onNext(new Result(null, requestCode, resultCode, data));
                    subject.onComplete();
                }

                @Override
                public void error(Throwable throwable) {
                    subject.onError(throwable);
                }
            };
        }

        private OnResult onResultFragment() {
            return new OnResult() {
                @Override
                public void response(int requestCode, int resultCode, Intent data) {
                    if (activitiesLifecycle.getLiveActivity() == null) return;

                    Activity activity = activitiesLifecycle.getLiveActivity();

                    FragmentActivity fragmentActivity = (FragmentActivity) activity;
                    FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

                    Fragment targetFragment = getTargetFragment(fragmentManager.getFragments());

                    if (targetFragment != null) {
                        subject.onNext(new Result(targetFragment.getContext(), requestCode, resultCode, data));
                        subject.onComplete();
                    }

                    //If code reaches this point it means some other activity has been stacked as a secondary process.
                    //Do nothing until the current activity be the target activity to get the associated fragment
                }

                @Override
                public void error(Throwable throwable) {
                    subject.onError(throwable);
                }
            };
        }

        @Nullable
        Fragment getTargetFragment(List<Fragment> fragments) {
            if (fragments == null) return null;

            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible() && fragment.getClass() == clazz) {
                    return fragment;
                } else if (fragment != null && fragment.isAdded() && fragment.getChildFragmentManager() != null) {
                    List<Fragment> childFragments = fragment.getChildFragmentManager().getFragments();
                    Fragment candidate = getTargetFragment(childFragments);
                    if (candidate != null) return candidate;
                }
            }

            return null;
        }
    }
}
