package com.bluelinelabs.conductor.rxlifecycle2;

import android.support.annotation.NonNull;
import android.view.View;
import com.bluelinelabs.conductor.Controller;
import io.reactivex.subjects.BehaviorSubject;

public class ControllerLifecycleSubjectHelper {
    private ControllerLifecycleSubjectHelper() {
    }

    public static BehaviorSubject<ControllerEvent> create(Controller controller){
        final BehaviorSubject<ControllerEvent> subject = BehaviorSubject.createDefault(ControllerEvent.CREATE);

        controller.addLifecycleListener(new Controller.LifecycleListener() {
            @Override
            public void preCreateView(@NonNull Controller controller) {
                subject.onNext(ControllerEvent.CREATE_VIEW);
            }

            @Override
            public void preAttach(@NonNull Controller controller, @NonNull View view) {
                subject.onNext(ControllerEvent.ATTACH);
            }

            @Override
            public void preDetach(@NonNull Controller controller, @NonNull View view) {
                subject.onNext(ControllerEvent.DETACH);
            }

            @Override
            public void preDestroyView(@NonNull Controller controller, @NonNull View view) {
                subject.onNext(ControllerEvent.DESTROY_VIEW);
            }

            @Override
            public void preDestroy(@NonNull Controller controller) {
                subject.onNext(ControllerEvent.DESTROY);
            }
        });

        return subject;
    }
}
