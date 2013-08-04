package com.example.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.multiknobprogressbar.R;

/**
 * 
 * @author mohit
 *	Slider widget may have infinite knobs 
 */
public class InfiniteKnobSeekBar extends McProgressBar{
	
//	private static final String TAG = "ThreeKnobSeekBar";
	
	private int mThumbsCount			=	1;
	 private static final int NO_ALPHA = 0xFF;
	private	Drawable mDrawableArray[]	=	null;
	private	long  mprogressArray[]	=	null;
	private	Drawable mSeekDrawable			=	null;
	private int thumbOffset;
	int mMinWidth;
	int mMaxWidth;
	int mMinHeight;
	int mMaxHeight;
	private ThumbsProgressListener mThumbsProgressListener;
	private	TextView	mMovableText	=	null;
	private int			mMovingTextLeftMargin	=	0;
	
	public interface ThumbsProgressListener{
		public void onProgressStart();
		public boolean onThumbProgressChanged(int index, long progress);
		public void onProgressStop();
	}
	
	public InfiniteKnobSeekBar(Context context) {
		super(context);
	}
	
	public InfiniteKnobSeekBar(Context context, AttributeSet attrs) throws Exception {
		this(context, attrs,0);
	}

	public InfiniteKnobSeekBar(Context context, AttributeSet attrs, int defStyle) throws Exception {
		super(context, attrs, defStyle);
		 
		 TypedArray a = context.obtainStyledAttributes(attrs,
	                R.styleable.InfiniteKonbSeekBar, defStyle, 0);
	        mThumbsCount = a.getInt(R.styleable.InfiniteKonbSeekBar_thumbsCount,1);
	        Drawable	drawable	=	a.getDrawable(R.styleable.InfiniteKonbSeekBar_thumbDrawable);
	     
	        mMaxHeight 		=  	a.getDimensionPixelSize(R.styleable.InfiniteKonbSeekBar_maxHeight,14);
	        mMinHeight		=	a.getDimensionPixelSize(R.styleable.InfiniteKonbSeekBar_minHeight, 8);
	        
	        mMaxWidth 		=  	a.getDimensionPixelSize(R.styleable.InfiniteKonbSeekBar_maxWidth,308);
	        mMinWidth		=	a.getDimensionPixelSize(R.styleable.InfiniteKonbSeekBar_minWidth, 24);
	        
	     // ...but allow layout to override this
	        thumbOffset = a.getDimensionPixelOffset(
	                R.styleable.InfiniteKonbSeekBar_thumbOffset, getThumbOffset());
	        
	        if(drawable != null){
	        	mSeekDrawable	=	drawable;
	        	mSeekDrawable.setState(getDrawableState());
	        	 initKnobseekBar();
	        }else{
	        	throw new Exception("Invalid drawable");
	        }
	        
	        setThumbOffset(thumbOffset);
	        
	        
	        calcThumbOffset(); // will guess mThumbOffset if thumb != null...
	        a.recycle();
	        
	}
	/**
     * <p>
     * Initialize the Twoknob seek bar 's default values:
     * </p>
     * <ul>
     * <li>thumb1Progress = 0</li>
     * <li>thumb2Progress = max progress</li>
     * <li>max = 100</li>
     * </ul>
     */
    private void initKnobseekBar() {
    	mMovingTextLeftMargin = getContext().getResources().getDimensionPixelSize(R.dimen.moving_text_left_margin);
    	mDrawableArray	=	new Drawable[mThumbsCount];
    	mprogressArray	=	new long[mThumbsCount];
    	int maxProgress	=	getMax();
    	int progressGap = maxProgress/mThumbsCount;
    	int progress = 0;
    	for (int i = 0; i < mThumbsCount; i++) {
			mDrawableArray[i]=mSeekDrawable.getConstantState().newDrawable();
			mprogressArray[i]= progress;
			progress = progress+progressGap;
		}
    }
    
   @Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null) {
            progressDrawable.setAlpha(isEnabled() ? NO_ALPHA : (int) (NO_ALPHA * 0.5f));
        }
        
        if (mDrawableArray != null && mDrawableArray.length>0) {
        	for (int i = 0; i < mDrawableArray.length; i++) {
        		Drawable drawable = mDrawableArray[i];
				if (drawable.isStateful()) {
					 int[] state = getDrawableState();
					 drawable.setState(state);
				}
			}
           
        }
	}
   
   
    public void setThumbsCount(int thumbCount){
    	if (thumbCount != mThumbsCount) {
    		mThumbsCount = thumbCount;
        	initKnobseekBar();
        	if (getWidth() != 0 && getHeight() != 0) {
        		updateThumbPos(getWidth(), getHeight());
        	}
		}
    }
    
    public void setMovingText(TextView txtMoving){
    	mMovableText = txtMoving;
    }
    
    public int getThumbCount(){
    	return mThumbsCount;
    }
    
    public void setThumbsProgressListener(ThumbsProgressListener listener){
    	this.mThumbsProgressListener = listener;
    }
    
	private void calcThumbOffset() {
		int thumbW = mSeekDrawable.getIntrinsicWidth();
		thumbOffset = thumbW / 2;
	}
	
	public int getThumbOffset(){
		return thumbOffset;
	}
	
	public long[] getProgressArray(){
		return mprogressArray;
	}
	
	public void updateThumbsAndProgress(long[] progressArray,int thumbCount){
		mThumbsCount = thumbCount;
		mprogressArray = progressArray;
		mDrawableArray = new Drawable[mThumbsCount];
		for (int i = 0; i < mThumbsCount; i++) {
			mDrawableArray[i]=mSeekDrawable.getConstantState().newDrawable();
		}
		if (getWidth() != 0 && getHeight() != 0) {
    		updateThumbPos(getWidth(), getHeight());
    	}
	}
	
	
	public void setThumbOffset(int offset) {
		this.thumbOffset = offset;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateThumbPos(w, h);
	}
	
	private void updateThumbPos(int w, int h) {

		Drawable drawable = getProgressDrawable();
		int thumbHeight = mSeekDrawable == null ? 0 : mSeekDrawable.getIntrinsicHeight();
		// The max height does not incorporate padding, whereas the height
		// parameter does
		int trackHeight = Math.min(mMaxHeight, h - getPaddingTop()
				- getPaddingBottom());
		float maxProgress = getMax();
		
		if (maxProgress > 0) {
			for (int i = 0; i < mThumbsCount; i++) {
				float thumbScale = (float)mprogressArray[i] / (float)maxProgress;
				if (thumbHeight > trackHeight) {
				 	setThumbPosition(i,w,thumbScale, 0);
		            int gapForCenteringTrack = (thumbHeight - trackHeight) / 2;
		            if (drawable != null) {
		                // Canvas will be translated by the padding, so 0,0 is where we start drawing
		            	drawable.setBounds(0, gapForCenteringTrack, 
		                        w - getPaddingRight() - getPaddingLeft(), h - getPaddingBottom() - gapForCenteringTrack
		                        - getPaddingTop());
		            }
		        } else {
		            if (drawable != null) {
		                // Canvas will be translated by the padding, so 0,0 is where we start drawing
		            	drawable.setBounds(0, 0, w - getPaddingRight() - getPaddingLeft(), h - getPaddingBottom()
		                        - getPaddingTop());
		            }
		            int gap = (trackHeight - thumbHeight) / 2;
		             setThumbPosition(i,w,thumbScale, gap);
		        }
			}
		} 
		
		
	}
	
	
	public void setThumbProgress(int index, long progress, boolean fromUser) {
		mprogressArray[index]=(int)progress;
		boolean changeScale = true;
		if(mThumbsProgressListener != null && fromUser){
			changeScale = mThumbsProgressListener.onThumbProgressChanged(index, progress);
		}
		
		if (!changeScale) {
			return;
		}
		
		if(progress < 0){
			progress = 0;
		}
		
		if(progress > getMax()){
			progress = getMax();
		}
		
		float scale = getMax() > 0 ? (float) progress / (float) getMax() : 0;
		setThumbPosition(index, getWidth(), scale, Integer.MIN_VALUE);
		invalidate();
	}
	
	
	private void setThumbPosition(int index, int w, float thumbScale, int gap) {

		int available = w-getPaddingLeft()-getPaddingRight();
		for (int i = 0; i < mDrawableArray.length; i++) {
			Drawable drawable = mDrawableArray[i];
			available -= drawable.getIntrinsicWidth();
		}
		available += mThumbsCount*2*thumbOffset;
		
		int thumbPos = (int)((float)(thumbScale)*(float)available);
		Drawable thumb = mDrawableArray[index];
		int topBound, bottomBound;
		if (gap == Integer.MIN_VALUE) {
			Rect oldBounds = thumb.getBounds();
			topBound = oldBounds.top;
			bottomBound = oldBounds.bottom;
			
		} else {
			topBound = gap;
			bottomBound = gap + mDrawableArray[index].getIntrinsicHeight();
//			// move top and bottom bounds slighly up
//			float movement = getHeight() / 2.5f;
//			topBound = topBound - (int) movement;
		}
		
		// Canvas will be translated, so 0,0 is where we start drawing
		mDrawableArray[index].setBounds(thumbPos, topBound, thumbPos + mDrawableArray[index].getIntrinsicWidth(), bottomBound);
	}

	
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		for (int i = 0; i < mThumbsCount; i++) {
			Drawable  mThumb = mDrawableArray[i];
			if (mThumb != null) {
	            canvas.save();
	            // Translate the padding. For the x, we need to allow the thumb to
	            // draw in its extra space
	            canvas.translate(getPaddingLeft() - thumbOffset, getPaddingTop());
	            mThumb.draw(canvas);
	            
	            canvas.restore();
	        }
			
		}
	}
	
	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Drawable d = getProgressDrawable();
        int  thumbHeight = mSeekDrawable.getIntrinsicHeight();
        
        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
            dh = Math.max(thumbHeight, dh);
        }
        
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(dw, widthMeasureSpec),
        		resolveSize(dh, heightMeasureSpec));
	}
	
	 /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
    	ViewParent mParent = getParent();
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }
    
	int			lastDragged	=	0;
	boolean 	isDragging	=	false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}
		
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:
			int x= (int)event.getX();
			int y= (int)event.getY();
			lastDragged = withinThumb(x, y);
			if (lastDragged != -1) {
				isDragging = true;
				moveProgressText(calcScale(event));
				if (mThumbsProgressListener != null) {
					mThumbsProgressListener.onProgressStart();
				}
			
			}
			
			return true;
			 
		case MotionEvent.ACTION_MOVE:
			setPressed(true);
			if (isDragging) {
				startTrackingThumb(event, lastDragged);
			}
			attemptClaimDrag();
			
			return true;
			
		case MotionEvent.ACTION_UP:
			setPressed(false);
			invalidate();
			if (isDragging && mThumbsProgressListener != null) {
				mThumbsProgressListener.onProgressStop();
				
			}
			isDragging = false;
			break;
			
		case MotionEvent.ACTION_CANCEL:
			setPressed(false);
			invalidate();
			if (isDragging && mThumbsProgressListener != null) {
				mThumbsProgressListener.onProgressStop();
			}
			isDragging = false;
			break;
		default:
			break;
		}
		return false;
		
	}
	
	private void moveProgressText(float scale){
		int textPos = (int)((float)(scale)*(float)getAvailableSpace());
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mMovableText.getLayoutParams();
		int margin = 0;
		if (scale>.86) {
			mMovableText.setBackgroundResource(R.drawable.watch_small_right_icon);
			margin = -mMovingTextLeftMargin;
		}else{
			mMovableText.setBackgroundResource(R.drawable.watch_small_left_icon);
			margin = mMovingTextLeftMargin;
		}
		if (scale < 0.96) {
			lp.leftMargin = textPos+margin;
			 mMovableText.setLayoutParams(lp);
		}
       
	}
	
	private float calcScale(MotionEvent event){
		int x = (int) event.getX();
		final int width = getWidth();
		final int available = width - getPaddingLeft() - getPaddingRight();
		float scale;
		if (x < getPaddingLeft()) {
			scale = 0.0f;
		} else if (x > width - getPaddingRight()) {
			scale = 1.0f;
		} else {
			scale = (float) (x - getPaddingLeft()) / (float) available;
		}
		return scale;
	}
	
	private void startTrackingThumb(MotionEvent event, int lastDragged) {
		int progress = 0;
		float scale = calcScale(event);
		final long max = getMax();
		progress += scale * max;
		setThumbProgress(lastDragged, progress, true);
		moveProgressText(scale);
	}

	
	

	private int getAvailableSpace(){
		int available = getWidth()-getPaddingLeft()-getPaddingRight();
		for (int i = 0; i < mThumbsCount; i++) {
			int thumbWidth = mDrawableArray[i].getIntrinsicWidth();
			available -= thumbWidth;
		}
		available += mThumbsCount*2*thumbOffset;
		return available;
	}
	
	private int withinThumb(int x, int y){
		int touchCount	=	-1;
		int touchThumbIndex	= 0;
		int available = getAvailableSpace();
		for (int i = 0; i < mThumbsCount; i++) {
			float thumbScale = getMax() > 0 ? (float)(mprogressArray[i]) / (float)getMax(): 0;
			int position = (int)(thumbScale*available);
			if(position>x- thumbOffset && position<x+ thumbOffset){
				touchThumbIndex = i;
				touchCount++;
				
			}
		}
		
		if (touchCount >= 1) {
			return touchThumbIndex;
		}else if (touchCount == 0){
			return touchThumbIndex;
		}else{
			return -1;
		}
	}
	
	
	static class TwoKnobSavedState extends BaseSavedState{
		private	long  progressArray[]	=	null;
		
		public TwoKnobSavedState(Parcel parcel) {
			super(parcel);
			parcel.readLongArray(progressArray);
		}

		public TwoKnobSavedState(Parcelable parcelable) {
			super(parcelable);
		}

		@Override
		public int describeContents() {
			return super.describeContents();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeLongArray(progressArray);
		}
		
		public static final Parcelable.Creator<TwoKnobSavedState> CREATOR = new Creator<InfiniteKnobSeekBar.TwoKnobSavedState>() {
			
			@Override
			public TwoKnobSavedState[] newArray(int size) {
				return new TwoKnobSavedState[size];
			}
			
			@Override
			public TwoKnobSavedState createFromParcel(Parcel source) {
				return new TwoKnobSavedState(source);
			}
		};
		
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable	parcelable = super.onSaveInstanceState();
		TwoKnobSavedState savedState = new TwoKnobSavedState(parcelable);
		savedState.progressArray = mprogressArray;
		return savedState;
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		TwoKnobSavedState savedState = (TwoKnobSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mprogressArray = savedState.progressArray;
		for (int i = 0; i < mThumbsCount; i++) {
			setThumbProgress(i, mprogressArray[i], true);
		}
	}
}
