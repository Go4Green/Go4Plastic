package com.realtwin.goforplastic.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.view_thumbnail_image_button.view.*
import com.realtwin.goforplastic.R


internal class ThumbnailImageButton: FrameLayout {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : this(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    fun init(attrs: AttributeSet?){
        View.inflate(context, R.layout.view_thumbnail_image_button, this)

        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ThumbnailImageButton,
            0,
            0
        )


        val res = a.getResourceId(R.styleable.ThumbnailImageButton_thumbnailSrc, -1);
        if (res == -1)  btn_thumbnail.setImageDrawable(null)
        else btn_thumbnail.setImageResource(res)
        val res_b = a.getResourceId(R.styleable.ThumbnailImageButton_backgroundSrc, -1)
        if (res_b == -1) btn_background.setImageDrawable(null)
        else btn_background.setImageResource(res_b)
        btn_text.text = (a.getText(R.styleable.ThumbnailImageButton_label))
    }

    fun getTextView(): TextView {return btn_text}
    fun getBackgroundImageView(): ImageView {return btn_background}
    fun getThumbnailImageView(): ImageView {return btn_thumbnail}
}