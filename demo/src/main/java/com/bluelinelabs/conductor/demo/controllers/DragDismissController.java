package com.bluelinelabs.conductor.demo.controllers;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.ScaleFadeChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.widget.ElasticDragDismissFrameLayout;
import com.bluelinelabs.conductor.demo.widget.ElasticDragDismissFrameLayout.ElasticDragDismissCallback;

import butterknife.BindView;

@TargetApi(VERSION_CODES.LOLLIPOP)
public class DragDismissController extends BaseController {

    @BindView(R.id.tv_lorem_ipsum) TextView tvLoremIpsum;

    private final ElasticDragDismissCallback dragDismissListener = new ElasticDragDismissCallback() {
        @Override
        public void onDragDismissed() {
            overridePopHandler(new ScaleFadeChangeHandler());
            getRouter().popController(DragDismissController.this);
        }
    };

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_drag_dismiss, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        ((ElasticDragDismissFrameLayout)view).addListener(dragDismissListener);

        tvLoremIpsum.setText("Lorem ipsum dolor sit amet, volutpat lacus egestas integer vitae, tempus potenti posuere dolore, elit cras ut vulputate pede eros. Pharetra curabitur, cum ultrices nisi nulla, non a est diamlorem in pede. Feugiat vivamus id, leo massa, pede ligula libero wisi, posuere nec interdum risus. Mauris eros. Scelerisque etiam dignissim, sem odio magna posuere libero in. Eget non posuere, rutrum nunc ut, ipsum ornare, vestibulum nisl turpis, urna interdum. Arcu mi velit. Sem dolor amet sed hymenaeos tempor. Cras felis.\n" +
                "Tempus risus vehicula, mauris nulla interdum purus sodales suspendisse, morbi ultrices tempus vitae vestibulum porttitor, vel mus enim tellus non massa in, quis nec fermentum scelerisque sem congue dolores. Accumsan lacinia urna eu feugiat, habitasse wisi cras id nonummy sapien, a pede. Turpis ac donec, adipiscing ut faucibus, odio ut morbi, habitant volutpat lacinia. At vitae ipsum, porttitor wisi hendrerit pellentesque sapien, suspendisse pellentesque praesent sit tellus varius. At in ligula neque imperdiet eget, viverra lectus risus est sem feugiat. Diam amet non phasellus, sed enim ante etiam lorem id at, feugiat eu urna, urna posuere. Amet vel, vehicula ac in fermentum id, elit mauris dolor, eget orci in nec non in vestibulum. Tortor risus mattis, massa vel condimentum non ornare, pede nunc curabitur dui, eu dolorum luctus duis pellentesque. Nec in imperdiet ac tortor pulvinar bibendum. Parturient lacus luctus posuere, quis luctus sociis tellus iaculis.\n" +
                "Eu nec, nulla ut neque ac suspendisse, ac purus mattis pretium orci orci, penatibus sollicitudin pellentesque massa, felis et molestie natoque justo vitae. Laoreet nunc nulla augue semper, nulla ridiculus elit in quis bibendum ultrices, integer duis. Dolor ut donec ligula vehicula odio, in lacinia mattis ut quos, semper libero nulla, et euismod ut, nec curabitur turpis aliquet mauris sit a. Tellus interdum dignissim felis. Ultrices dignissim ut, enim urna adipiscing, nunc accumsan justo odio fringilla magna penatibus. Amet integer metus sollicitudin tristique libero dolor, augue nulla pellentesque, massa in suspendisse, adipiscing donec neque nunc. Malesuada non, luctus vestibulum, et at taciti orci felis. Aliquam dolor nibh erat in tincidunt in, risus suscipit integer, quis dolor quis quam. Ultrices dictum, dignissim interdum aenean pede ornare pretium, vehicula adipiscing enim nec magna mollis duis. Imperdiet dolor velit phasellus, laoreet sed ridiculus sollicitudin sit viverra metus, integer sit nunc fusce nonummy augue. Maecenas adipiscing porttitor eu risus nunc malesuada, quo vitae blandit amet feugiat nunc. Pede hac duis in.\n" +
                "Lorem posuere ridiculus donec, volutpat vehicula erat nunc ut, justo occaecati vivamus aliquam massa, felis etiam, sed feugiat molestie eu. Et leo non dignissim nam. Fringilla pretium suspendisse vitae nascetur massa hymenaeos, lectus ut senectus amet, a enim. Pellentesque integer erat, mauris morbi pellentesque, sodales phasellus turpis purus nulla porta, massa vulputate consectetuer habitasse malesuada pulvinar, vehicula in elit eros interdum ut. Et enim vulputate, aliquam donec ullamcorper et, vel consequat tincidunt. Ipsum quam sed ante quis at, ultrices eget. Rutrum qui et velit possimus in, odio amet ut adipiscing sed nec, a tellus ut, quam molestie. Nisl adipiscing euismod nec, eget facilisis ac sit. Suspendisse elit amet consequat dolor senectus vivamus, at scelerisque erat, odio doloribus velit et felis neque, turpis adipiscing, arcu varius placerat leo.");
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);

        ((ElasticDragDismissFrameLayout)view).removeListener(dragDismissListener);
    }

    @Override
    protected String getTitle() {
        return "Drag to Dismiss";
    }
}
