package org.homunculus.android.example.module.toolbar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.compat.ToolbarBuilder;
import org.homunculus.android.compat.ToolbarBuilder.ContentViewHolder;
import org.homunculus.android.compat.ToolbarBuilder.ToolbarHolder;

public class ToolbarActivity extends EventAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentViewHolder<ToolbarHolder<MyContentView>, MyLeftDrawer, MyRightDrawer> contentView = applyState(this);
        setContentView(contentView);
    }

    @SuppressWarnings("unchecked")
    private static ContentViewHolder<ToolbarHolder<MyContentView>, MyLeftDrawer, MyRightDrawer> applyState(EventAppCompatActivity context) {
        ContentViewHolder<?, ?, ?> contentView = ToolbarBuilder.
                define().
                setContentView(new MyContentView(context)).
                setLeftDrawer(new MyLeftDrawer(context)).
                setRightDrawer(new MyRightDrawer(context)).
                create(null, context);
        return (ContentViewHolder<ToolbarHolder<MyContentView>, MyLeftDrawer, MyRightDrawer>) contentView;
    }

    private static class MyContentView extends View {

        public MyContentView(Context context) {
            super(context);
            setBackgroundColor(Color.BLUE);
        }
    }

    private static class MyRightDrawer extends View {
        public MyRightDrawer(Context context) {
            super(context);
            setBackgroundColor(Color.RED);
        }
    }

    private static class MyLeftDrawer extends View {
        public MyLeftDrawer(Context context) {
            super(context);
            setBackgroundColor(Color.YELLOW);
        }
    }
}
