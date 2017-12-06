package org.homunculus.android.example.module.toolbar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.Str;
import org.homunculus.android.component.ToolbarBuilder;
import org.homunculus.android.component.ToolbarBuilder.ContentViewHolder;
import org.homunculus.android.component.ToolbarBuilder.ToolbarHolder;
import org.homunculus.android.example.R;

import static org.homunculus.android.component.Str.str;

public class ToolbarActivity extends EventAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentViewHolder<ToolbarHolder<MyContentView>, MyLeftDrawer, MyRightDrawer> contentView = applyState(this);
        setContentView(contentView);
    }

    private static ContentViewHolder<ToolbarHolder<MyContentView>, MyLeftDrawer, MyRightDrawer> applyState(EventAppCompatActivity context) {
        return ToolbarBuilder.
                define().
                setTitle(str(R.string.app_name)).
                setLogo(org.homunculus.android.component.R.drawable.ic_launcher).
                setMenuId(R.menu.testmenu).
                create(null, context, context, new MyContentView(context), new MyLeftDrawer(context), new MyRightDrawer(context));
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
