/*
 * Copyright (c) 2018.
 *
 * Anthony Ngure
 *
 * Email : anthonyngure25@gmail.com
 */
package ke.co.toshngure.views.media

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ke.co.toshngure.basecode.app.GlideRequests
import ke.co.toshngure.views.CircleImageView
import ke.co.toshngure.views.R
import java.io.File
import java.lang.ref.WeakReference

/**
 * Created by Anthony Ngure on 20/02/2017.
 * Email : anthonyngure25@gmail.com.
 */
class NetworkImage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var imageView: ImageView
        protected set
    protected var mBackgroundImageView: ImageView
    protected var mErrorButton: ImageView
    protected var mProgressBar: ProgressBar
    private var loadingCallBack: LoadingCallBack? = null
    private var noRetry = false
    private var placeholder: Drawable? = ColorDrawable(Color.LTGRAY)
    private var error: Drawable? = ColorDrawable(Color.LTGRAY)
    fun loadImageFromNetwork(networkPath: String?, glideRequests: GlideRequests) {
        mProgressBar.visibility = VISIBLE
        mErrorButton.setOnClickListener { v: View? ->
            Log.d(TAG, "Retrying to loadFromNetwork image")
            mProgressBar.visibility = VISIBLE
            mErrorButton.visibility = GONE
            loadImageFromNetwork(networkPath, glideRequests)
        }

        glideRequests.load(networkPath)
            .centerCrop()
            .placeholder(placeholder)
            .error(error)
            .listener(Listener(imageView, mProgressBar, mErrorButton, loadingCallBack, noRetry))
            .into(imageView)
    }

    fun loadImageFromMediaStore(path: String?, glideRequests: GlideRequests) {
        mProgressBar.visibility = VISIBLE

        path?.let {
            val file = File(path)
            glideRequests.load(Uri.fromFile(file))
                .centerCrop()
                .placeholder(ColorDrawable(Color.LTGRAY))
                .error(R.drawable.ic_place_holder)
                .listener(Listener(imageView, mProgressBar, mErrorButton, loadingCallBack, noRetry))
                .into(imageView)
        }
    }

    fun loadImageFromMediaStore(uri: Uri?, glideRequests: GlideRequests) {
        mProgressBar.visibility = VISIBLE
        glideRequests.load(uri)
            .centerCrop()
            .placeholder(ColorDrawable(Color.LTGRAY))
            .error(R.drawable.ic_place_holder)
            .listener(Listener(imageView, mProgressBar, mErrorButton, loadingCallBack, noRetry))
            .into(imageView)
    }

    fun setLoadingCallBack(loadingCallBack: LoadingCallBack?): NetworkImage {
        this.loadingCallBack = loadingCallBack
        return this
    }

    fun setImageDrawable(drawable: Drawable?): NetworkImage {
        imageView.setImageDrawable(drawable)
        imageView.visibility = VISIBLE
        mProgressBar.visibility = GONE
        mErrorButton.visibility = GONE
        return this
    }

    fun setImageUri(uri: Uri?): NetworkImage {
        imageView.setImageURI(uri)
        imageView.visibility = VISIBLE
        mProgressBar.visibility = GONE
        mErrorButton.visibility = GONE
        return this
    }

    fun setImageBackgroundColor(@ColorInt color: Int): NetworkImage {
        mBackgroundImageView.setImageDrawable(ColorDrawable(color))
        return this
    }

    fun setImageResource(@DrawableRes resId: Int): NetworkImage {
        val drawable = AppCompatResources.getDrawable(context, resId)
        imageView.setImageDrawable(drawable)
        return this
    }

    fun setPlaceholderDrawable(drawable: Drawable?): NetworkImage {
        placeholder = drawable
        // 0702 238 023
        return this
    }

    fun setErrorDrawable(drawable: Drawable?): NetworkImage {
        error = drawable
        return this
    }

    fun setNoRetry(): NetworkImage {
        noRetry = true
        return this
    }

    fun setScaleType(scaleType: ScaleType?): NetworkImage {
        imageView.scaleType = scaleType
        return this
    }

    interface LoadingCallBack {
        fun onSuccess(drawable: Drawable?)
    }

    /**
     * Glide Callback which clears the ImageView's background onSuccess. This is done to reduce
     * overdraw. A weak reference is used to avoid leaking the Activity context because the Callback
     * will be strongly referenced by Glide.
     */
    internal class Listener(
        imageView: ImageView?, progressBar: ProgressBar, errorImageView: ImageView,
        loadingCallBack: LoadingCallBack?, noRetry: Boolean
    ) : RequestListener<Drawable?> {
        val imageViewWeakReference: WeakReference<ImageView?>
        val progressBarWeakReference: WeakReference<ProgressBar>
        val errorImageViewWeakReference: WeakReference<ImageView>
        val noRetryWeakReference: WeakReference<Boolean>
        val loadingCallBackWeakReference: WeakReference<LoadingCallBack?>
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Drawable?>,
            isFirstResource: Boolean
        ): Boolean {
            Log.e(TAG, "error")
            Log.e(TAG, e.toString())
            val progressBar = progressBarWeakReference.get()
            if (progressBar != null) {
                progressBar.visibility = GONE
            }
            val errorImageView = errorImageViewWeakReference.get()
            val noRetry = noRetryWeakReference.get()!!
            if (errorImageView != null) {
                errorImageView.visibility = if (noRetry) GONE else VISIBLE
            }
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any,
            target: Target<Drawable?>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            val imageView = imageViewWeakReference.get()
            val loadingCallBack = loadingCallBackWeakReference.get()
            loadingCallBack?.onSuccess(resource)
            val progressBar = progressBarWeakReference.get()
            if (progressBar != null) {
                progressBar.visibility = GONE
            }
            val errorImageView = errorImageViewWeakReference.get()
            if (errorImageView != null) {
                errorImageView.visibility = GONE
            }
            return false
        }

        init {
            imageViewWeakReference = WeakReference(imageView)
            progressBarWeakReference = WeakReference(progressBar)
            errorImageViewWeakReference = WeakReference(errorImageView)
            loadingCallBackWeakReference = WeakReference(loadingCallBack)
            noRetryWeakReference = WeakReference(noRetry)
        }
    }

    companion object {
        private val TAG = NetworkImage::class.java.simpleName
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_network_image, this, true)
        mErrorButton = findViewById(R.id.errorButton)
        mProgressBar = findViewById(R.id.progressBar)
        val imageFL = findViewById<FrameLayout>(R.id.imageFL)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NetworkImage)
        val circled = typedArray.getBoolean(R.styleable.NetworkImage_niCircled, false)
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (circled) {
            val imageView = CircleImageView(context)
            val borderColor =
                typedArray.getColor(R.styleable.NetworkImage_niBorderColor, Color.WHITE)
            val borderWidth =
                typedArray.getDimensionPixelOffset(R.styleable.NetworkImage_niBorderWidth, 0)
            imageView.borderColor = borderColor
            imageView.borderWidth = borderWidth
            imageView.isBorderOverlay = true
            this.imageView = imageView
            // mImageView = new CircleImageView(context);
            mBackgroundImageView = CircleImageView(context)
        } else {
            imageView = AppCompatImageView(context)
            mBackgroundImageView = AppCompatImageView(context)
            imageView.setScaleType(ScaleType.CENTER_CROP)
            mBackgroundImageView.setScaleType(ScaleType.CENTER_CROP)
        }
        imageFL.addView(mBackgroundImageView, layoutParams)
        imageFL.addView(imageView, layoutParams)

        /*Image*/
        val drawableResId = typedArray.getResourceId(R.styleable.NetworkImage_niSrc, -1)
        if (drawableResId != -1) {
            val drawable = AppCompatResources.getDrawable(getContext(), drawableResId)
            setImageDrawable(drawable)
        }

        /*Background*/
        val backgroundResId = typedArray.getResourceId(R.styleable.NetworkImage_niBackground, -1)
        if (backgroundResId != -1) {
            val backgroundDrawable = AppCompatResources.getDrawable(getContext(), backgroundResId)
            mBackgroundImageView.setImageDrawable(backgroundDrawable)
        }
        typedArray.recycle()
    }
}