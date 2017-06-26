package com.zhry.like1.flychess.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.zhry.like1.flychess.R;
import com.zhry.like1.flychess.data.PathNode;
import com.zhry.like1.flychess.listener.PathNodeClickListener;

/**
 * Created by like1 on 2017/4/13.
 */

@SuppressLint("AppCompatCustomView")
public class PathNodeView extends ImageView {
    private PathNode pathNode;
    public PathNodeView(Context context,int id) {
        super(context);
    }

    public PathNodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PathNodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setPathNode(PathNode pathNode)
    {
        this.pathNode = pathNode;
    }

    public PathNode getPathNode() {
        return pathNode;
    }
}
