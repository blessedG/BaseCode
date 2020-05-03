package ke.co.toshngure.views.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import ke.co.toshngure.basecode.app.GlideRequests;
import ke.co.toshngure.views.R;

public class MediaView extends ViewGroup implements View.OnClickListener {

    static final int MAX_IMAGE_VIEW_COUNT = 4;
    private static final CharSequence CONTENT_DESC = "content_description";

    private static final int DEFAULT_DIVIDER_SIZE = 2;
    private static final int DEFAULT_CORNER_RADII = 5;

    private final OverlayImageView[] imageViews = new OverlayImageView[MAX_IMAGE_VIEW_COUNT];
    private List<Object> mediaEntities = Collections.emptyList();
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private int mediaDividerSize;
    private int imageCount;
    final float[] radii = new float[8];
    int mediaBgColor;
    int photoErrorResId;

    boolean internalRoundedCornersEnabled;
    private OnMediaClickListener onMediaClickListener;
    private int roundedCornersRadii;
    private GlideRequests glideRequests;

    public MediaView(Context context) {
        this(context, null);
    }

    public MediaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode())
            initAttributes(attrs, defStyle);
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MediaView, defStyle, 0);
        try {
            roundedCornersRadii = a.getDimensionPixelSize(R.styleable.MediaView_mvCornerRadii, DEFAULT_CORNER_RADII);
            mediaDividerSize = a.getDimensionPixelSize(R.styleable.MediaView_mvDividerSize, DEFAULT_DIVIDER_SIZE);
            photoErrorResId = a.getResourceId(R.styleable.MediaView_mvPlaceholder, R.drawable.ic_place_holder);
            mediaBgColor = a.getColor(R.styleable.MediaView_mvBackgroundColor, Color.WHITE);
        } finally {
            a.recycle();
        }
    }

    public void setRoundedCornersRadii(int radii) {
        this.radii[0] = radii;
        this.radii[1] = radii;
        this.radii[2] = radii;
        this.radii[3] = radii;
        this.radii[4] = radii;
        this.radii[5] = radii;
        this.radii[6] = radii;
        this.radii[7] = radii;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (imageCount > 0) {
            layoutImages();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final Size size;
        if (imageCount > 0) {
            size = measureImages(widthMeasureSpec, heightMeasureSpec);
        } else {
            size = Size.EMPTY;
        }
        setMeasuredDimension(size.width, size.height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        path.reset();
        rect.set(0, 0, w, h);
        path.addRoundRect(rect, radii, Path.Direction.CW);
        path.close();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (internalRoundedCornersEnabled &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final int saveState = canvas.save();
            canvas.clipPath(path);
            super.dispatchDraw(canvas);
            canvas.restoreToCount(saveState);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    public void setOnMediaClickListener(OnMediaClickListener onMediaClickListener) {
        this.onMediaClickListener = onMediaClickListener;
    }

    public interface OnMediaClickListener {
        void onMediaClick(View view, int index);
    }

    @Override
    public void onClick(View view) {
        //Integer mediaEntityIndex = (Integer) view.getTag();
        Integer mediaEntityIndex = generateIndexBaseOnId(view.getId());
        if (onMediaClickListener != null && mediaEntities != null && !mediaEntities.isEmpty()) {
            onMediaClickListener.onMediaClick(view, mediaEntityIndex);
        }
    }

    public void setMedias(List<Object> mediaEntities, GlideRequests glideRequests) {
        this.glideRequests = glideRequests;
        this.mediaEntities = mediaEntities;
        setRoundedCornersRadii(roundedCornersRadii);
        clearImageViews();
        initializeImageViews(mediaEntities);
        internalRoundedCornersEnabled = true;
        requestLayout();
    }


    Size measureImages(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int halfWidth = (width - mediaDividerSize) / 2;
        final int halfHeight = (height - mediaDividerSize) / 2;
        switch (imageCount) {
            case 1:
                measureImageView(0, width, height);
                break;
            case 2:
                measureImageView(0, halfWidth, height);
                measureImageView(1, halfWidth, height);
                break;
            case 3:
                measureImageView(0, halfWidth, height);
                measureImageView(1, halfWidth, halfHeight);
                measureImageView(2, halfWidth, halfHeight);
                break;
            case 4:
                measureImageView(0, halfWidth, halfHeight);
                measureImageView(1, halfWidth, halfHeight);
                measureImageView(2, halfWidth, halfHeight);
                measureImageView(3, halfWidth, halfHeight);
                break;
            default:
                break;
        }
        return Size.fromSize(width, height);
    }

    void measureImageView(int i, int width, int height) {
        imageViews[i].measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    void layoutImages() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int halfWidth = (width - mediaDividerSize) / 2;
        int halfHeight = (height - mediaDividerSize) / 2;
        int middle = halfWidth + mediaDividerSize;
        switch (imageCount) {
            case 1:
                layoutImage(0, 0, 0, width, height);
                break;
            case 2:
                layoutImage(0, 0, 0, halfWidth, height);
                layoutImage(1, halfWidth + mediaDividerSize, 0, width, height);
                break;
            case 3:
                layoutImage(0, 0, 0, halfWidth, height);
                layoutImage(1, middle, 0, width, halfHeight);
                layoutImage(2, middle, halfHeight + mediaDividerSize, width, height);
                break;
            case 4:
                layoutImage(0, 0, 0, halfWidth, halfHeight);
                layoutImage(2, 0, halfHeight + mediaDividerSize, halfWidth, height);
                layoutImage(1, middle, 0, width, halfHeight);
                layoutImage(3, middle, halfHeight + mediaDividerSize, width, height);
                break;
            default:
                break;
        }
    }

    void layoutImage(int i, int left, int top, int right, int bottom) {
        final ImageView view = imageViews[i];
        if (view.getLeft() == left && view.getTop() == top && view.getRight() == right
                && view.getBottom() == bottom) {
            return;
        }

        view.layout(left, top, right, bottom);
    }

    void clearImageViews() {
        for (int index = 0; index < imageCount; index++) {
            final ImageView imageView = imageViews[index];
            if (imageView != null) {
                imageView.setVisibility(GONE);
            }
        }
        imageCount = 0;
    }

    void initializeImageViews(List<Object> mediaEntities) {
        imageCount = Math.min(MAX_IMAGE_VIEW_COUNT, mediaEntities.size());

        for (int index = 0; index < imageCount; index++) {
            final OverlayImageView imageView = getOrCreateImageView(index);

            final Object mediaEntity = mediaEntities.get(index);
            setAltText(imageView, "media");
            setMediaImage(imageView, mediaEntity);
            setOverlayImage(imageView);
        }
    }

    OverlayImageView getOrCreateImageView(int index) {
        OverlayImageView imageView = imageViews[index];
        if (imageView == null) {
            imageView = new OverlayImageView(getContext());
            imageView.setLayoutParams(generateDefaultLayoutParams());
            imageView.setOnClickListener(this);
            imageViews[index] = imageView;
            addView(imageView, index);
        } else {
            measureImageView(index, 0, 0);
            layoutImage(index, 0, 0, 0, 0);
        }

        imageView.setVisibility(VISIBLE);
        imageView.setBackgroundColor(mediaBgColor);
        imageView.setId(generateIdBaseOnIndex(index));
        //imageView.setTag(index);

        return imageView;
    }

    private int generateIdBaseOnIndex(int index) {
        switch (index) {
            case 0:
                return R.id.image_index_one;
            case 1:
                return R.id.image_index_two;
            case 2:
                return R.id.image_index_three;
            case 3:
                return R.id.image_index_four;
        }
        return 0;
    }


    private int generateIndexBaseOnId(int id) {
        if (id == R.id.image_index_one) {
            return 0;
        } else if (id == R.id.image_index_two) {
            return 1;
        } else if (id == R.id.image_index_three) {
            return 2;
        } else if (id == R.id.image_index_four) {
            return 3;
        } else {
            return 0;
        }
    }

    @SuppressLint("PrivateResource")
    void setAltText(ImageView imageView, String description) {
        if (!TextUtils.isEmpty(description)) {
            imageView.setContentDescription(description);
        } else {
            imageView.setContentDescription(CONTENT_DESC);
        }
    }

    @SuppressLint("PrivateResource")
    void setOverlayImage(OverlayImageView imageView) {
        imageView.setOverlayDrawable(null);
    }

    void setMediaImage(ImageView imageView, Object imagePath) {
        if (glideRequests == null) return;
        glideRequests.load(imagePath)
                .centerCrop()
                .placeholder(new ColorDrawable(Color.LTGRAY))
                .error(R.drawable.ic_place_holder)
                .listener(new Listener(imageView))
                .into(imageView);
        /*imageLoader.init(imagePath)
                .fit()
                .centerCrop()
                .error(photoErrorResId)
                .into(imageView, new PicassoCallback(imageView));*/
    }


    private static class Listener implements RequestListener<Drawable> {

        final WeakReference<ImageView> imageViewWeakReference;

        Listener(ImageView imageView) {
            imageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            /* intentionally blank */
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            final ImageView imageView = imageViewWeakReference.get();
            if (imageView != null) {
                imageView.setBackgroundResource(android.R.color.transparent);
            }
            return false;
        }
    }

    private static class Size {
        static final Size EMPTY = new Size();
        final int width;
        final int height;

        private Size() {
            this(0, 0);
        }

        private Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        static Size fromSize(int w, int h) {
            final int boundedWidth = Math.max(w, 0);
            final int boundedHeight = Math.max(h, 0);
            return boundedWidth != 0 || boundedHeight != 0 ?
                    new Size(boundedWidth, boundedHeight) : EMPTY;
        }
    }

}
