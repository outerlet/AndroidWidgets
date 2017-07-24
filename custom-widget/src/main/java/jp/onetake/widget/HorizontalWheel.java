package jp.onetake.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class HorizontalWheel extends View {
	/**
	 * ホイールに発生したイベントを捕捉するためのリスナ
	 */
	public interface EventListener {
		/**
		 * ホイールの指す角度が変わったときにその角度を得る
		 * @param angle	角度。ラジアンではない
		 */
		void onAngleChange(double angle);
	}
	
	/**
	 * 現在のホイールの状態
	 */
	public enum State {
		Idle,		// アイドル
		Dragging,	// ドラッグ中
		Inertia,	// 慣性回転アニメーション中
	}
	
	private ValueAnimator.AnimatorUpdateListener mAnimationUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			rotateTo((float)valueAnimator.getAnimatedValue());
		}
	};
	
	private AnimatorListenerAdapter AnimationListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			mCurrentState = State.Idle;
		}
	};
	
	private GestureDetector mGestureDetector;	// ジェスチャ検出オブジェクト
	private double mCurrentRadian;				// 現在の角度
	private double mIntervalRadian;				// 目盛り間の角度
	private boolean mIsPointerVisible;			// ポインタの可視・不可視
	private Paint mPointerPaint;				// ポインタの色
	private Paint mScalePaint;					// 目盛りの色
	private EventListener mListener;			// ホイールを操作した結果を伝播するためのリスナ
	private State mCurrentState;				// 現在の状態
	private ValueAnimator mInertiaAnimator;		// 慣性による回転アニメーションを実現するアニメーターオブジェクト
	
	public HorizontalWheel(Context context) {
		this(context, null);
	}
	
	@SuppressWarnings("deprecation")
	public HorizontalWheel(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		mGestureDetector = new GestureDetector(getContext(), new WheelGestureListener(this));
		
		Resources res = context.getResources();
		
		mCurrentRadian = 0.0;
		
		int scalesNumber = res.getInteger(R.integer.default_scales_number);
		
		int pointerColor = res.getColor(R.color.default_pointer_color);
		int scaleColor = res.getColor(R.color.default_scale_color);
		float pointerWidth = res.getDimensionPixelSize(R.dimen.default_pointer_width);
		float scaleWidth = res.getDimensionPixelSize(R.dimen.default_scale_width);
		mIsPointerVisible = true;
		
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalWheel);
			
			scalesNumber = array.getInt(R.styleable.HorizontalWheel_scalesNumber, scalesNumber);
			pointerColor = array.getColor(R.styleable.HorizontalWheel_pointerColor, pointerColor);
			pointerWidth = array.getDimensionPixelSize(R.styleable.HorizontalWheel_pointerWidth, (int)pointerWidth);
			scaleColor = array.getColor(R.styleable.HorizontalWheel_scaleColor, scaleColor);
			scaleWidth = array.getDimensionPixelSize(R.styleable.HorizontalWheel_scaleWidth, (int)scaleWidth);
			mIsPointerVisible = array.getBoolean(R.styleable.HorizontalWheel_pointerVisible, mIsPointerVisible);
			
			array.recycle();
		}
		
		mIntervalRadian = 2 * Math.PI / scalesNumber;
		
		mPointerPaint = new Paint();
		mPointerPaint.setColor(pointerColor);
		mPointerPaint.setStrokeWidth(pointerWidth);
		
		mScalePaint = new Paint();
		mScalePaint.setColor(scaleColor);
		mScalePaint.setStrokeWidth(scaleWidth);
		
		mCurrentState = State.Idle;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		
		int action = event.getActionMasked();
		if (mCurrentState != State.Inertia && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
			mCurrentState = State.Idle;
		}
		
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		float width = getWidth();
		float height = getHeight();
		float verticalMargin = height * 0.1f;

		// 中央線
		if (mIsPointerVisible) {
			canvas.drawLine(width / 2.0f, 0.0f, width / 2.0f, height, mPointerPaint);
		}
		
		double radian = (Math.PI * -1 / 2.0) + (mCurrentRadian % mIntervalRadian);
		while (radian <= Math.PI / 2.0) {
			double sin = Math.sin(radian);
			float sx = (float)((width / 2.0) * sin) + (width / 2.0f);
			if (sx > width) {
				break;
			}
			
			float sh = (float)(height * (1.0 - Math.abs(sin) * 0.1)) - (verticalMargin + 2.0f);
			float sy = (height - sh) / 2.0f;

			// 目盛り線
			canvas.drawLine(sx, sy, sx, sy + sh, mScalePaint);
			
			radian += mIntervalRadian;
		}
	}
	
	/**
	 * ホイールに発生したイベントを捕捉するためのリスナをセットする
	 * @param listener	ホイールに発生したイベントを捕捉するためのリスナ
	 */
	@SuppressWarnings("unused")
	public void setListener(EventListener listener) {
		mListener = listener;
	}
	
	/**
	 * 現在のホイールの指す角度を得る
	 * @return	ホイールの指す角度(ラジアン)
	 */
	public double getRadian() {
		return mCurrentRadian;
	}
	
	/**
	 * 現在のホイールの状態をセットする
	 * @param newState	ホイールの状態
	 */
	public void setState(State newState) {
		mCurrentState = newState;
	}
	
	/**
	 * 現在のホイールの状態を取得する
	 */
	public State getState() {
		return mCurrentState;
	}
	
	/**
	 * ホイールを回転させる
	 * @param newRadian	回転後の角度(rad)
	 */
	public void rotateTo(double newRadian) {
		if (newRadian > Math.PI * 2.0) {
			mCurrentRadian = newRadian % (Math.PI * 2.0);
		} else if (newRadian < 0.0) {
			mCurrentRadian = Math.PI * 2.0 + newRadian;
		} else {
			mCurrentRadian = newRadian;
		}

		invalidate();
		
		if (mListener != null) {
			mListener.onAngleChange(Math.toDegrees(mCurrentRadian));
		}
	}
	
	/**
	 * 慣性による回転アニメーションを開始する
	 * @param endRadian	回転終了角
	 */
	public void startInertiaScroll(double endRadian) {
		if (mCurrentState == State.Inertia) {
			return;
		}
		
		int duration = (int) (Math.abs(mCurrentRadian - endRadian) * 1000);
		
		mInertiaAnimator = ValueAnimator.ofFloat((float)mCurrentRadian, (float)endRadian);
		mInertiaAnimator.setDuration(duration);
		mInertiaAnimator.addUpdateListener(mAnimationUpdateListener);
		mInertiaAnimator.addListener(AnimationListener);
		mInertiaAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
		mInertiaAnimator.start();
	}
	
	/**
	 * 慣性による回転アニメーションを中止する
	 */
	public void cancelInertiaScroll() {
		if (mCurrentState == State.Inertia && mInertiaAnimator != null) {
			mInertiaAnimator.cancel();
		}
	}
}
