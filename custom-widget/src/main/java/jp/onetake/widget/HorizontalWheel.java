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

/**
 * 横向きに回転して値を取得するためのホイール
 */
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
	
	// ValueAnimatorの角度がアップデートされるたびに呼び出されるリスナ
	private ValueAnimator.AnimatorUpdateListener mAnimationUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			rotateTo((float)valueAnimator.getAnimatedValue());
		}
	};
	
	// アニメーションが終了した時のイベントを処理するリスナ
	// 主にValueAnimatorで扱う値と角度(ラジアン)のデータ型がそれぞれfloat、doubleと異なることに起因する不都合に対処するためのクラス
	private class AnimationEndListener extends AnimatorListenerAdapter {
		private double mToRadian;
		
		AnimationEndListener(double toRadian) {
			mToRadian = toRadian;
		}
		
		@Override
		public void onAnimationEnd(Animator animator) {
			// animatorはValueAnimatorで、扱う値はfloat
			// 対して角度はラジアンでdoubleなので、厳密にはアニメーションでの終了値が異なる場合がある
			// もし終了時の角度が目標値と異なる場合はそこに位置を合わせる
			if (mCurrentRadian != mToRadian) {
				rotateTo(mToRadian);
			}
			
			mCurrentState = State.Idle;
		}
	}
	
	private GestureDetector mGestureDetector;	// ジェスチャ検出オブジェクト
	private double mCurrentRadian;				// 現在の角度
	private double mIntervalRadian;				// 目盛り間の角度
	private boolean mIsPointerVisible;			// ポインタの可視・不可視
	private Paint mPointerPaint;				// ポインタの色
	private Paint mScalePaint;					// 目盛りの色
	private boolean mIsValueLimited;			// 取得される値は0-360の範囲かどうか
	private boolean mIsSnapScale;				// ホイール操作が終わった後に最も近い目盛りの値にスナップするか
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
		mIsValueLimited = true;
		mIsSnapScale = false;
		
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalWheel);
			
			scalesNumber = array.getInt(R.styleable.HorizontalWheel_scales_number, scalesNumber);
			pointerColor = array.getColor(R.styleable.HorizontalWheel_pointer_color, pointerColor);
			pointerWidth = array.getDimensionPixelSize(R.styleable.HorizontalWheel_pointer_width, (int)pointerWidth);
			scaleColor = array.getColor(R.styleable.HorizontalWheel_scale_color, scaleColor);
			scaleWidth = array.getDimensionPixelSize(R.styleable.HorizontalWheel_scale_width, (int)scaleWidth);
			mIsPointerVisible = array.getBoolean(R.styleable.HorizontalWheel_pointer_visible, mIsPointerVisible);
			mIsValueLimited = array.getBoolean(R.styleable.HorizontalWheel_value_limited, mIsValueLimited);
			mIsSnapScale = array.getBoolean(R.styleable.HorizontalWheel_snap_scale, mIsSnapScale);
			
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
			setState(State.Idle);
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
		// ドラッグ操作が終了するときのみ、必要なら最も近くの目盛りにスナップする
		// (フリック操作の場合は、startInertiaScrollで近くの目盛りにスナップするようにしてある)
		if (mIsSnapScale && mCurrentState == State.Dragging && newState == State.Idle) {
			double toRadian = getNearestRadian(mCurrentRadian);
			int duration = (int)(Math.abs(mCurrentRadian - toRadian) * 1000);
			
			ValueAnimator anim = ValueAnimator.ofFloat((float)mCurrentRadian, (float)toRadian);
			anim.setDuration(duration);
			anim.addUpdateListener(mAnimationUpdateListener);
			anim.addListener(new AnimationEndListener(toRadian));
			anim.start();
		} else {
			mCurrentState = newState;
		}
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
		if (mIsValueLimited) {
			if (newRadian > Math.PI * 2.0) {
				mCurrentRadian = newRadian % (Math.PI * 2.0);
			} else if (newRadian < 0.0) {
				mCurrentRadian = Math.PI * 2.0 + newRadian;
			} else {
				mCurrentRadian = newRadian;
			}
		} else {
			mCurrentRadian = newRadian;
		}

		invalidate();
		
		if (mListener != null) {
			mListener.onAngleChange(Math.toDegrees(mCurrentRadian));
		}
	}
	
	/**
	 * 慣性による回転(っぽい)アニメーションを開始する
	 * @param endRadian	回転終了角
	 */
	public void startInertiaScroll(double endRadian) {
		if (mCurrentState == State.Inertia) {
			return;
		}
		
		// 必要なら回転終了角の最も近い目盛りにスナップ
		// onAnimationEndでスナップすると、一旦止まったスクロールが再度動くという変な挙動になるので
		double toRadian = mIsSnapScale ? getNearestRadian(endRadian) : endRadian;
		int duration = (int) (Math.abs(mCurrentRadian - toRadian) * 1000);
		
		mInertiaAnimator = ValueAnimator.ofFloat((float)mCurrentRadian, (float)toRadian);
		mInertiaAnimator.setDuration(duration);
		mInertiaAnimator.addUpdateListener(mAnimationUpdateListener);
		mInertiaAnimator.addListener(new AnimationEndListener(toRadian));
		mInertiaAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
		mInertiaAnimator.start();
	}
	
	/**
	 * 慣性による回転(っぽい)アニメーションを中止する
	 */
	public void cancelInertiaScroll() {
		if (mCurrentState == State.Inertia && mInertiaAnimator != null) {
			mInertiaAnimator.cancel();
		}
	}
	
	/**
	 * 角度radianから最も近い目盛りの示す角度を返す
	 * @param radian	基準となる角度(rad)
	 * @return	radianから最も近い目盛りの示す角度
	 */
	private double getNearestRadian(double radian) {
		int q1 = (int)(radian / mIntervalRadian);
		double s = radian - q1 * mIntervalRadian;
		int q2 = (q1 >= 0) ? q1 + 1 : q1 - 1;
		
		return (Math.abs(s) <= mIntervalRadian / 2.0) ? mIntervalRadian * q1 : mIntervalRadian * q2;
	}
}
