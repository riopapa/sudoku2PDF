package com.urrecliner.sudoku2pdf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WheelView<T> extends View implements Runnable {

    private static final float DEFAULT_LINE_SPACING = dp2px(2f);
    private static final float DEFAULT_TEXT_SIZE = sp2px(15f);
    private static final float DEFAULT_TEXT_BOUNDARY_MARGIN = dp2px(2);
    private static final float DEFAULT_DIVIDER_HEIGHT = dp2px(1);
    private static final int DEFAULT_NORMAL_TEXT_COLOR = Color.DKGRAY;
    private static final int DEFAULT_SELECTED_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_VISIBLE_ITEM = 5;
    private static final int DEFAULT_SCROLL_DURATION = 250;
    private static final long DEFAULT_CLICK_CONFIRM = 120;
    private static final String DEFAULT_INTEGER_FORMAT = "%02d";
    //Default refraction ratio, through the font size to achieve refraction visual difference
    private static final float DEFAULT_REFRACT_RATIO = 1f;

    //Text alignment
    public static final int TEXT_ALIGN_LEFT = 0;
    public static final int TEXT_ALIGN_CENTER = 1;
    public static final int TEXT_ALIGN_RIGHT = 2;

    //Scrolling state
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SCROLLING = 2;

    //벤드 효과 정렬
    public static final int CURVED_ARC_DIRECTION_LEFT = 0;
    public static final int CURVED_ARC_DIRECTION_CENTER = 1;
    public static final int CURVED_ARC_DIRECTION_RIGHT = 2;

    public static final float DEFAULT_CURVED_FACTOR = 0.75f;

    //Bending effect alignment
    public static final int DIVIDER_TYPE_FILL = 0;
    public static final int DIVIDER_TYPE_WRAP = 1;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //font size
    private float mTextSize;
    //Whether to automatically adjust the font size to display completely
    private boolean isAutoFitTextSize;
    private Paint.FontMetrics mFontMetrics;
    //Height of each item
    private int mItemHeight;
   //텍스트의 최대 너비
    private int mMaxTextWidth;
    //텍스트 중심에서 기준선까지의 거리
    private int mCenterToBaselineY;
    //표시되는 항목 수
    private int mVisibleItems;
    //각 항목 사이의 간격, 줄 간격
    private float mLineSpacing;
    //루프에서 스크롤할지 여부
    private boolean isCyclic;
    //텍스트 정렬
    @TextAlign
    private int mTextAlign;
    //글자 색
    private int mTextColor;
    //항목 텍스트 색상 선택
    private int mSelectedItemTextColor;

    //구분선을 표시할지 여부
    private boolean isShowDivider;
    //구분선의 색
    private int mDividerColor;
    //디바이더 높이
    private float mDividerSize;
    //구분선 채우기 유형
    @DividerType
    private int mDividerType;
    //When the split line type is DIVIDER_TYPE_WRAP, the distance between the left and right ends of the split line is from the text.
    private float mDividerPaddingForWrap;
    //기본 둥근 머리가 있는 구분선의 두 끝 모양
    private Paint.Cap mDividerCap = Paint.Cap.ROUND;
    //선과 선택 오프셋을 분할하여 선택 영역 확대
    private float mDividerOffset;

    //선택한 영역을 그릴지 여부
    private boolean isDrawSelectedRect;
    //선택한 영역 색상
    private int mSelectedRectColor;

    //텍스트 시작 X
    private int mStartX;
    //X축 중심점
    private int mCenterX;
    //Y축 중심점
    private int mCenterY;
    //경계의 상한과 하한 확인
    private int mSelectedItemTopLimit;
    private int mSelectedItemBottomLimit;
    //경계 자르기
    private int mClipLeft;
    private int mClipTop;
    private int mClipRight;
    private int mClipBottom;
    //영역 그리기
    private Rect mDrawRect;
    //여백을 사용하기 위한 글꼴 여백
    private float mTextBoundaryMargin;
    //Whether format conversion is required when the data is of type Integer
    private boolean isIntegerNeedFormat;
    //데이터가 정수 형식이면 형식은 기본적으로 두 자리 숫자로 변환됩니다.
    private String mIntegerFormat;

    //3D 효과
    private Camera mCamera;
    private Matrix mMatrix;
    //굽힘(3D) 효과인지 여부
    private boolean isCurved;
    //벤드(3D) 효과는 좌우, 호 오프셋 효과, 효과 중심의 방향은 오프셋되지 않습니다.
    @CurvedArcDirection
    private int mCurvedArcDirection;
    //굽힘(3D) 효과 좌우 호 오프셋 효과 계수 0-1 사이 클수록 두드러집니다.
    private float mCurvedArcDirectionFactor;
    //선택하면 굴절 오프셋은 글꼴 크기의 비율이고 1은 오프셋 없음입니다. 오프셋이 작을수록 더 뚜렷합니다.
    //(일반 및 3D 효과 모두에 적합)
    private float mRefractRatio;

    //데이터 목록
    @NonNull
    private List<T> mDataList = new ArrayList<>(1);
    //데이터가 변경될 때 선택한 첨자를 첫 번째 위치로 재설정할지 여부
    private boolean isResetSelectedPosition = false;

    private VelocityTracker mVelocityTracker;
    private int mMaxFlingVelocity;
    private int mMinFlingVelocity;
    private Scroller mScroller;

    //최소 스크롤 거리, 상한
    private int mMinScrollY;
    //최대 스크롤 거리, 하한
    private int mMaxScrollY;

    //Y축 롤링 오프셋
    private int mScrollOffsetY;
    //Y축은 스크롤 오프셋되어 다시 그리기 횟수를 제어합니다.
    private int mScrolledY = 0;
    //손가락의 마지막 터치 위치
    private float mLastTouchY;
    //손가락 누름 시간, 클릭 스크롤은 프레스 리프트 시간의 차이에 따라 처리됩니다.
    private long mDownStartTime;
    //스크롤을 강제로 중지할지 여부
    private boolean isForceFinishScroll = false;
    //빠른 스크롤인지 여부, 빠른 스크롤이 끝난 후 점프 위치
    private boolean isFlingScroll;
    //현재 선택된 아래 첨자
    private int mSelectedItemPosition;
    //현재 스크롤 중인 아래 첨자
    private int mCurrentScrollPosition;

    //글꼴
    private boolean mIsBoldForSelectedItem = false;
    //mIsBoldForSelectedItem==true인 경우 이 글꼴은 선택되지 않은 항목의 글꼴입니다.
    private Typeface mNormalTypeface = null;
    //mIsBoldForSelectedItem==true인 경우 이 글꼴은 선택한 항목의 글꼴입니다.
    private Typeface mBoldTypeface = null;

    //경청자
    private OnItemSelectedListener<T> mOnItemSelectedListener;
    private OnWheelChangedListener mOnWheelChangedListener;

    //오디오
    private SoundHelper mSoundHelper;
    //오디오 효과가 켜져 있는지 여부
    private boolean isSoundEffect = false;

    public WheelView(Context context) {
        this(context, null);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrsAndDefault(context, attrs);
        initValue(context);
    }

    /**
     * Initialize custom properties and default values
     *
     * @param context 上下文
     * @param attrs   attrs
     */
    private void initAttrsAndDefault(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WheelView);
        mTextSize = typedArray.getDimension(R.styleable.WheelView_wv_textSize, DEFAULT_TEXT_SIZE);
        isAutoFitTextSize = typedArray.getBoolean(R.styleable.WheelView_wv_autoFitTextSize, false);
        mTextAlign = typedArray.getInt(R.styleable.WheelView_wv_textAlign, TEXT_ALIGN_CENTER);
        mTextBoundaryMargin = typedArray.getDimension(R.styleable.WheelView_wv_textBoundaryMargin,
                DEFAULT_TEXT_BOUNDARY_MARGIN);
        mTextColor = typedArray.getColor(R.styleable.WheelView_wv_normalItemTextColor, DEFAULT_NORMAL_TEXT_COLOR);
        mSelectedItemTextColor = typedArray.getColor(R.styleable.WheelView_wv_selectedItemTextColor, DEFAULT_SELECTED_TEXT_COLOR);
        mLineSpacing = typedArray.getDimension(R.styleable.WheelView_wv_lineSpacing, DEFAULT_LINE_SPACING);
        isIntegerNeedFormat = typedArray.getBoolean(R.styleable.WheelView_wv_integerNeedFormat, false);
        mIntegerFormat = typedArray.getString(R.styleable.WheelView_wv_integerFormat);
        if (TextUtils.isEmpty(mIntegerFormat)) {
            mIntegerFormat = DEFAULT_INTEGER_FORMAT;
        }

        mVisibleItems = typedArray.getInt(R.styleable.WheelView_wv_visibleItems, DEFAULT_VISIBLE_ITEM);
        //항목이 이상한지 확인하기 위해 이동
        mVisibleItems = adjustVisibleItems(mVisibleItems);
        mSelectedItemPosition = typedArray.getInt(R.styleable.WheelView_wv_selectedItemPosition, 0);
        //스크롤 첨자 초기화
        mCurrentScrollPosition = mSelectedItemPosition;
        isCyclic = typedArray.getBoolean(R.styleable.WheelView_wv_cyclic, false);

        isShowDivider = typedArray.getBoolean(R.styleable.WheelView_wv_showDivider, false);
        mDividerType = typedArray.getInt(R.styleable.WheelView_wv_dividerType, DIVIDER_TYPE_FILL);
        mDividerSize = typedArray.getDimension(R.styleable.WheelView_wv_dividerHeight, DEFAULT_DIVIDER_HEIGHT);
        mDividerColor = typedArray.getColor(R.styleable.WheelView_wv_dividerColor, DEFAULT_SELECTED_TEXT_COLOR);
        mDividerPaddingForWrap = typedArray.getDimension(R.styleable.WheelView_wv_dividerPaddingForWrap, DEFAULT_TEXT_BOUNDARY_MARGIN);

        mDividerOffset = typedArray.getDimensionPixelOffset(R.styleable.WheelView_wv_dividerOffset, 0);

        isDrawSelectedRect = typedArray.getBoolean(R.styleable.WheelView_wv_drawSelectedRect, false);
        mSelectedRectColor = typedArray.getColor(R.styleable.WheelView_wv_selectedRectColor, Color.TRANSPARENT);

        isCurved = typedArray.getBoolean(R.styleable.WheelView_wv_curved, true);
        mCurvedArcDirection = typedArray.getInt(R.styleable.WheelView_wv_curvedArcDirection, CURVED_ARC_DIRECTION_CENTER);
        mCurvedArcDirectionFactor = typedArray.getFloat(R.styleable.WheelView_wv_curvedArcDirectionFactor, DEFAULT_CURVED_FACTOR);
        //굴절 오프셋 기본값
        //Deprecated 새 버전에서 제거됩니다.
        float curvedRefractRatio = typedArray.getFloat(R.styleable.WheelView_wv_curvedRefractRatio, 0.9f);
        mRefractRatio = typedArray.getFloat(R.styleable.WheelView_wv_refractRatio, DEFAULT_REFRACT_RATIO);
        mRefractRatio = isCurved ? Math.min(curvedRefractRatio, mRefractRatio) : mRefractRatio;
        if (mRefractRatio > 1f) {
            mRefractRatio = 1.0f;
        } else if (mRefractRatio < 0f) {
            mRefractRatio = DEFAULT_REFRACT_RATIO;
        }
        typedArray.recycle();
    }

    /**
     * 기본값 초기화 및 설정
     *
     * @param context 上下文
     */
    private void initValue(Context context) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mScroller = new Scroller(context);
        mDrawRect = new Rect();
        mCamera = new Camera();
        mMatrix = new Matrix();
        mSoundHelper = SoundHelper.obtain();    // @ha
//
//        if (!isInEditMode()) {
//            mSoundHelper = SoundHelper.obtain();
//            initDefaultVolume(context);
//        }
        calculateTextSize();
        updateTextAlign();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSoundHelper != null) {
            mSoundHelper.release();
        }
    }

    /**
     * 기본 볼륨 초기화
     *
     * @param context 上下文
     */
    private void initDefaultVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            //시스템 미디어의 현재 볼륨을 가져옵니다.
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //시스템 미디어의 최대 볼륨을 가져옵니다.
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            //재생 볼륨 설정
            mSoundHelper.setPlayVolume(currentVolume * 1.0f / maxVolume);
        } else {
            mSoundHelper.setPlayVolume(0.3f);
        }
    }

    /**
     * Measure the maximum space occupied by text
     */
    private void calculateTextSize() {
        mPaint.setTextSize(mTextSize);
        for (int i = 0; i < mDataList.size(); i++) {
            int textWidth = (int) mPaint.measureText(getDataText(mDataList.get(i)));
            mMaxTextWidth = Math.max(textWidth, mMaxTextWidth);
        }

        mFontMetrics = mPaint.getFontMetrics();
        //항목 높이는 실제로 글꼴 높이 + 한 줄 간격과 같습니다.
        mItemHeight = (int) (mFontMetrics.bottom - mFontMetrics.top + mLineSpacing);
    }

    /**
     * update textAlign
     */
    private void updateTextAlign() {
        switch (mTextAlign) {
            case TEXT_ALIGN_LEFT:
                mPaint.setTextAlign(Paint.Align.LEFT);
                break;
            case TEXT_ALIGN_RIGHT:
                mPaint.setTextAlign(Paint.Align.RIGHT);
                break;
            case TEXT_ALIGN_CENTER:
            default:
                mPaint.setTextAlign(Paint.Align.CENTER);
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //줄 간격은 mItemHeight 단위로 계산됩니다.
        int height;
        if (isCurved) {
            height = (int) ((mItemHeight * mVisibleItems * 2 / Math.PI) + getPaddingTop() + getPaddingBottom());
        } else {
            height = mItemHeight * mVisibleItems + getPaddingTop() + getPaddingBottom();
        }
        int width = (int) (mMaxTextWidth + getPaddingLeft() + getPaddingRight() + mTextBoundaryMargin * 2);
        if (isCurved) {
            int towardRange = (int) (Math.sin(Math.PI / 48) * height);
            width += towardRange;
        }
        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, 0),
                resolveSizeAndState(height, heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //콘텐츠 그리기 가능 영역을 설정합니다.
        mDrawRect.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        mCenterX = mDrawRect.centerX();
        mCenterY = mDrawRect.centerY();
        mSelectedItemTopLimit = (int) (mCenterY - mItemHeight / 2 - mDividerOffset);
        mSelectedItemBottomLimit = (int) (mCenterY + mItemHeight / 2 + mDividerOffset);
        mClipLeft = getPaddingLeft();
        mClipTop = getPaddingTop();
        mClipRight = getWidth() - getPaddingRight();
        mClipBottom = getHeight() - getPaddingBottom();

        calculateDrawStart();
        //롤링 한계 계산
        calculateLimitY();

        //초기화 시 선택한 첨자가 있는 경우 선택한 위치까지의 거리가 계산됩니다.
        int itemDistance = calculateItemDistance(mSelectedItemPosition);
        if (itemDistance > 0) {
            doScroll(itemDistance);
        }
    }

    /**
     * Starting position
     */
    private void calculateDrawStart() {
        switch (mTextAlign) {
            case TEXT_ALIGN_LEFT:
                mStartX = (int) (getPaddingLeft() + mTextBoundaryMargin);
                break;
            case TEXT_ALIGN_RIGHT:
                mStartX = (int) (getWidth() - getPaddingRight() - mTextBoundaryMargin);
                break;
            case TEXT_ALIGN_CENTER:
            default:
                mStartX = getWidth() / 2;
                break;
        }

        //The distance from the text center to the baseline
        mCenterToBaselineY = (int) (mFontMetrics.ascent + (mFontMetrics.descent - mFontMetrics.ascent) / 2);
    }

    /**
     * 롤링 한계 계산
     */
    private void calculateLimitY() {
        mMinScrollY = isCyclic ? Integer.MIN_VALUE : 0;
        //하한 (dataSize - 1 - mInitPosition) * mItemHeight
        mMaxScrollY = isCyclic ? Integer.MAX_VALUE : (mDataList.size() - 1) * mItemHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //선택한 영역 그리기
        drawSelectedRect(canvas);
        //구분선 그리기
        drawDivider(canvas);

        //스크롤된 항목 수와 스크롤된 Y 값의 높이를 항목의 각 줄 높이에서 뺀 값
        int scrolledItem = mScrollOffsetY / dividedItemHeight();
        //항목을 스크롤 할 때 오프셋 값이없고 부드럽게 미끄러집니다.
        int scrolledOffset = mScrollOffsetY % dividedItemHeight();
        //반올림
        int halfItem = (mVisibleItems + 1) / 2;
        //계산된 최소 지수
        int minIndex;
        //계산된 최대 인덱스
        int maxIndex;
        if (scrolledOffset < 0) {
            //0보다 작음
            minIndex = scrolledItem - halfItem - 1;
            maxIndex = scrolledItem + halfItem;
        } else if (scrolledOffset > 0) {
            minIndex = scrolledItem - halfItem;
            maxIndex = scrolledItem + halfItem + 1;
        } else {
            minIndex = scrolledItem - halfItem;
            maxIndex = scrolledItem + halfItem;
        }

        //항목 그리기
        for (int i = minIndex; i < maxIndex; i++) {
            if (isCurved) {
                draw3DItem(canvas, i, scrolledOffset);
            } else {
                drawItem(canvas, i, scrolledOffset);
            }
        }

    }

    /**
     * 선택한 영역 그리기
     *
     * @param canvas 画布
     */
    private void drawSelectedRect(Canvas canvas) {
        if (isDrawSelectedRect) {
            mPaint.setColor(mSelectedRectColor);
            canvas.drawRect(mClipLeft, mSelectedItemTopLimit, mClipRight, mSelectedItemBottomLimit, mPaint);
        }
    }

    /**
     * 구분선 그리기
     *
     * @param canvas 画布
     */
    private void drawDivider(Canvas canvas) {
        if (isShowDivider) {
            mPaint.setColor(mDividerColor);
            float originStrokeWidth = mPaint.getStrokeWidth();
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(mDividerSize);
            if (mDividerType == DIVIDER_TYPE_FILL) {
                canvas.drawLine(mClipLeft, mSelectedItemTopLimit, mClipRight, mSelectedItemTopLimit, mPaint);
                canvas.drawLine(mClipLeft, mSelectedItemBottomLimit, mClipRight, mSelectedItemBottomLimit, mPaint);
            } else {
                //경계 처리 경계 초과는 DIVIDER_TYPE_FILL 유형에 따라 직접 처리됩니다.
                int startX = (int) (mCenterX - mMaxTextWidth / 2 - mDividerPaddingForWrap);
                int stopX = (int) (mCenterX + mMaxTextWidth / 2 + mDividerPaddingForWrap);

                int wrapStartX = startX < mClipLeft ? mClipLeft : startX;
                int wrapStopX = stopX > mClipRight ? mClipRight : stopX;
                canvas.drawLine(wrapStartX, mSelectedItemTopLimit, wrapStopX, mSelectedItemTopLimit, mPaint);
                canvas.drawLine(wrapStartX, mSelectedItemBottomLimit, wrapStopX, mSelectedItemBottomLimit, mPaint);
            }
            mPaint.setStrokeWidth(originStrokeWidth);
        }
    }

    /**
     * 2D 효과 그리기
     *
     * @param canvas         画布
     * @param index          下标
     * @param scrolledOffset 滚动偏移
     */
    private void drawItem(Canvas canvas, int index, int scrolledOffset) {
        String text = getDataByIndex(index);
        if (text == null) {
            return;
        }

        //중간 항목에서 인덱스 항목의 오프셋
        int item2CenterOffsetY = (index - mScrollOffsetY / dividedItemHeight()) * mItemHeight - scrolledOffset;
        //초기 측정 글꼴 시작 X 기록
        int startX = mStartX;
        //글꼴 너비 및 기준선 오프셋 다시 측정
        int centerToBaselineY = isAutoFitTextSize ? remeasureTextSize(text) : mCenterToBaselineY;

        if (Math.abs(item2CenterOffsetY) <= 0) {
            //선택한 항목을 그립니다.
            mPaint.setColor(mSelectedItemTextColor);
            clipAndDraw2DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit, item2CenterOffsetY, centerToBaselineY);
        } else if (item2CenterOffsetY > 0 && item2CenterOffsetY < mItemHeight) {
            //아래쪽 경계와 교차하는 항목 그리기
            mPaint.setColor(mSelectedItemTextColor);
            clipAndDraw2DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit, item2CenterOffsetY, centerToBaselineY);

            mPaint.setColor(mTextColor);
            //굴절 효과를 위해 글꼴 줄이기
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            //mIsBoldForSelectedItem==true 글꼴 변경
            changeTypefaceIfBoldForSelectedItem();
            clipAndDraw2DText(canvas, text, mSelectedItemBottomLimit, mClipBottom, item2CenterOffsetY, centerToBaselineY);
            mPaint.setTextSize(textSize);
            //mIsBoldForSelectedItem==true 글꼴 복원
            resetTypefaceIfBoldForSelectedItem();
        } else if (item2CenterOffsetY < 0 && item2CenterOffsetY > -mItemHeight) {
            //위쪽 경계와 교차하는 항목 그리기
            mPaint.setColor(mSelectedItemTextColor);
            clipAndDraw2DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit, item2CenterOffsetY, centerToBaselineY);

            mPaint.setColor(mTextColor);
            //굴절 효과를 위해 글꼴 줄이기
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            //mIsBoldForSelectedItem==true 글꼴 변경
            changeTypefaceIfBoldForSelectedItem();
            clipAndDraw2DText(canvas, text, mClipTop, mSelectedItemTopLimit, item2CenterOffsetY, centerToBaselineY);
            mPaint.setTextSize(textSize);
            //mIsBoldForSelectedItem==true 글꼴 복원
            resetTypefaceIfBoldForSelectedItem();
        } else {
            //추가 항목 그리기
            mPaint.setColor(mTextColor);
            //굴절 효과를 위해 글꼴 줄이기
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            //mIsBoldForSelectedItem==true 글꼴 변경
            changeTypefaceIfBoldForSelectedItem();
            clipAndDraw2DText(canvas, text, mClipTop, mClipBottom, item2CenterOffsetY, centerToBaselineY);
            mPaint.setTextSize(textSize);
            //mIsBoldForSelectedItem==true 글꼴 복원
            resetTypefaceIfBoldForSelectedItem();
        }

        if (isAutoFitTextSize) {
            //다시 측정하기 전의 스타일로 되돌리기
            mPaint.setTextSize(mTextSize);
            mStartX = startX;
        }
    }

    /**
     * 자르기 및 그리기 2d text
     *
     * @param canvas             画布
     * @param text               그려진 텍스트
     * @param clipTop            작물의 상단 경계
     * @param clipBottom         작물의 아래쪽 경계
     * @param item2CenterOffsetY 중간으로부터의 오프셋
     * @param centerToBaselineY  텍스트 중심에서 기준선까지의 거리
     */
    private void clipAndDraw2DText(Canvas canvas, String text, int clipTop, int clipBottom,
                                   int item2CenterOffsetY, int centerToBaselineY) {
        canvas.save();
        canvas.clipRect(mClipLeft, clipTop, mClipRight, clipBottom);
        canvas.drawText(text, 0, text.length(), mStartX, mCenterY + item2CenterOffsetY - centerToBaselineY, mPaint);
        canvas.restore();
    }

    /**
     * 글꼴 크기 다시 측정
     *
     * @param contentText 被测量文字内容
     * @return 텍스트 중심에서 기준선까지의 거리
     */
    private int remeasureTextSize(String contentText) {
        float textWidth = mPaint.measureText(contentText);
        float drawWidth = getWidth();
        float textMargin = mTextBoundaryMargin * 2;
        //텍스트 여백이 너비까지 약간 증가했습니다.1/10
        if (textMargin > (drawWidth / 10f)) {
            drawWidth = drawWidth * 9f / 10f;
            textMargin = drawWidth / 10f;
        } else {
            drawWidth = drawWidth - textMargin;
        }
        if (drawWidth <= 0) {
            return mCenterToBaselineY;
        }
        float textSize = mTextSize;
        while (textWidth > drawWidth) {
            textSize--;
            if (textSize <= 0) {
                break;
            }
            mPaint.setTextSize(textSize);
            textWidth = mPaint.measureText(contentText);
        }
        //텍스트 시작 X 다시 계산
        recalculateStartX(textMargin / 2.0f);
        //고도 시작점도 변경되었습니다.
        return recalculateCenterToBaselineY();
    }

    /**
     * 글꼴 시작 X 다시 계산
     *
     * @param textMargin 텍스트 여백
     */
    private void recalculateStartX(float textMargin) {
        switch (mTextAlign) {
            case TEXT_ALIGN_LEFT:
                mStartX = (int) textMargin;
                break;
            case TEXT_ALIGN_RIGHT:
                mStartX = (int) (getWidth() - textMargin);
                break;
            case TEXT_ALIGN_CENTER:
            default:
                mStartX = getWidth() / 2;
                break;
        }
    }

    /**
     * 글꼴 크기가 변경된 후 기준선으로부터의 거리 다시 계산
     *
     * @return 텍스트 중심에서 기준선까지의 거리
     */
    private int recalculateCenterToBaselineY() {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        //고도 시작점도 변경되었습니다.
        return (int) (fontMetrics.ascent + (fontMetrics.descent - fontMetrics.ascent) / 2);
    }

    /**
     * 곡률(3D) 효과에 대한 항목 그리기
     *
     * @param canvas         画布
     * @param index          下标
     * @param scrolledOffset 滚动偏移
     */
    private void draw3DItem(Canvas canvas, int index, int scrolledOffset) {
        String text = getDataByIndex(index);
        if (text == null) {
            return;
        }
        // 스크롤 휠의 반경
        final int radius = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2;
        //index 的 item 중간으로부터의 오프셋
        int item2CenterOffsetY = (index - mScrollOffsetY / dividedItemHeight()) * mItemHeight - scrolledOffset;

        // 슬라이드의 각도가 y축에 수직인 경우(텍스트가 이미 선으로 표시된 지점) 텍스트가 그려지지 않습니다.
        if (Math.abs(item2CenterOffsetY) > radius * Math.PI / 2) return;

        final double angle = (double) item2CenterOffsetY / radius;
        // x축을 중심으로 회전하는 각도
        float rotateX = (float) Math.toDegrees(-angle);
        // 스크롤의 거리는 y축의 길이에 매핑됩니다.
        float translateY = (float) (Math.sin(angle) * radius);
        // 스크롤의 거리는 z축의 길이에 매핑됩니다.
        float translateZ = (float) ((1 - Math.cos(angle)) * radius);
        // 透明度
        int alpha = (int) (Math.cos(angle) * 255);

        //초기 측정 글꼴 시작 X 기록
        int startX = mStartX;
        //글꼴 너비 및 기준선 오프셋 다시 측정
        int centerToBaselineY = isAutoFitTextSize ? remeasureTextSize(text) : mCenterToBaselineY;
        if (Math.abs(item2CenterOffsetY) <= 0) {
            //선택한 항목을 그립니다.
            mPaint.setColor(mSelectedItemTextColor);
            mPaint.setAlpha(255);
            clipAndDraw3DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit,
                    rotateX, translateY, translateZ, centerToBaselineY);
        } else if (item2CenterOffsetY > 0 && item2CenterOffsetY < mItemHeight) {
            //아래쪽 경계와 교차하는 항목 그리기
            mPaint.setColor(mSelectedItemTextColor);
            mPaint.setAlpha(255);
            clipAndDraw3DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit,
                    rotateX, translateY, translateZ, centerToBaselineY);

            mPaint.setColor(mTextColor);
            mPaint.setAlpha(alpha);
            //굴절 효과를 위해 글꼴 줄이기
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            //mIsBoldForSelectedItem==true 글꼴 변경
            changeTypefaceIfBoldForSelectedItem();
            //글꼴 변경, 기준선에서 오프셋 다시 계산
            int reCenterToBaselineY = recalculateCenterToBaselineY();
            clipAndDraw3DText(canvas, text, mSelectedItemBottomLimit, mClipBottom,
                    rotateX, translateY, translateZ, reCenterToBaselineY);
            mPaint.setTextSize(textSize);
            //mIsBoldForSelectedItem==true 글꼴 복원
            resetTypefaceIfBoldForSelectedItem();
        } else if (item2CenterOffsetY < 0 && item2CenterOffsetY > -mItemHeight) {
            //위쪽 경계와 교차하는 항목 그리기
            mPaint.setColor(mSelectedItemTextColor);
            mPaint.setAlpha(255);
            clipAndDraw3DText(canvas, text, mSelectedItemTopLimit, mSelectedItemBottomLimit,
                    rotateX, translateY, translateZ, centerToBaselineY);

            mPaint.setColor(mTextColor);
            mPaint.setAlpha(alpha);

            //굴절 효과를 위해 글꼴 줄이기
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            //mIsBoldForSelectedItem==true 改变字体
            changeTypefaceIfBoldForSelectedItem();
            //글꼴 변경, 기준선에서 오프셋 다시 계산
            int reCenterToBaselineY = recalculateCenterToBaselineY();
            clipAndDraw3DText(canvas, text, mClipTop, mSelectedItemTopLimit,
                    rotateX, translateY, translateZ, reCenterToBaselineY);
            mPaint.setTextSize(textSize);
            //mIsBoldForSelectedItem==true 글꼴 복원
            resetTypefaceIfBoldForSelectedItem();
        } else {
            //추가 항목 그리기
            mPaint.setColor(mTextColor);
            mPaint.setAlpha(alpha);

            //굴절 효과를 위해 글꼴 줄이기
            float textSize = mPaint.getTextSize();
            mPaint.setTextSize(textSize * mRefractRatio);
            //mIsBoldForSelectedItem==true 改变字体
            changeTypefaceIfBoldForSelectedItem();
            //글꼴 변경, 기준선에서 오프셋 다시 계산
            int reCenterToBaselineY = recalculateCenterToBaselineY();
            clipAndDraw3DText(canvas, text, mClipTop, mClipBottom,
                    rotateX, translateY, translateZ, reCenterToBaselineY);
            mPaint.setTextSize(textSize);
            //mIsBoldForSelectedItem==true 恢复字体
            resetTypefaceIfBoldForSelectedItem();
        }

        if (isAutoFitTextSize) {
            //다시 측정하기 전의 스타일로 되돌리기
            mPaint.setTextSize(mTextSize);
            mStartX = startX;
        }
    }

    /**
     * 곡률(3D) 효과 자르기 및 그리기
     *
     * @param canvas            画布
     * @param text              绘制的文字
     * @param clipTop           裁剪的上边界
     * @param clipBottom        裁剪的下边界
     * @param rotateX           绕X轴旋转角度
     * @param offsetY           Y轴偏移
     * @param offsetZ           Z轴偏移
     * @param centerToBaselineY 텍스트 중심에서 기준선까지의 거리
     */
    private void clipAndDraw3DText(Canvas canvas, String text, int clipTop, int clipBottom,
                                   float rotateX, float offsetY, float offsetZ, int centerToBaselineY) {

        canvas.save();
        canvas.clipRect(mClipLeft, clipTop, mClipRight, clipBottom);
        draw3DText(canvas, text, rotateX, offsetY, offsetZ, centerToBaselineY);
        canvas.restore();
    }

    /**
     * 绘制弯曲（3D）的文字
     *
     * @param canvas            画布
     * @param text              绘制的文字
     * @param rotateX           绕X轴旋转角度
     * @param offsetY           Y轴偏移
     * @param offsetZ           Z轴偏移
     * @param centerToBaselineY 텍스트 중심에서 기준선까지의 거리
     */
    private void draw3DText(Canvas canvas, String text, float rotateX, float offsetY,
                            float offsetZ, int centerToBaselineY) {
        mCamera.save();
        mCamera.translate(0, 0, offsetZ);
        mCamera.rotateX(rotateX);
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        // 调节中心点
        float centerX = mCenterX;
        //굽힘(3 d) 정렬에 따른 계수 설정
        if (mCurvedArcDirection == CURVED_ARC_DIRECTION_LEFT) {
            centerX = mCenterX * (1 + mCurvedArcDirectionFactor);
        } else if (mCurvedArcDirection == CURVED_ARC_DIRECTION_RIGHT) {
            centerX = mCenterX * (1 - mCurvedArcDirectionFactor);
        }

        float centerY = mCenterY + offsetY;
        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);

        canvas.concat(mMatrix);
        canvas.drawText(text, 0, text.length(), mStartX, centerY - centerToBaselineY, mPaint);

    }

    private void changeTypefaceIfBoldForSelectedItem() {
        if (mIsBoldForSelectedItem) {
            mPaint.setTypeface(mNormalTypeface);
        }
    }

    private void resetTypefaceIfBoldForSelectedItem() {
        if (mIsBoldForSelectedItem) {
            mPaint.setTypeface(mBoldTypeface);
        }
    }

    /**
     * 아래 첨자를 기반으로 콘텐츠 가져오기
     *
     * @param index 下标
     * @return 그려지는 텍스트의 내용
     */
    private String getDataByIndex(int index) {
        int dataSize = mDataList.size();
        if (dataSize == 0) {
            return null;
        }

        String itemText = null;
        if (isCyclic) {
            int i = index % dataSize;
            if (i < 0) {
                i += dataSize;
            }
            itemText = getDataText(mDataList.get(i));
        } else {
            if (index >= 0 && index < dataSize) {
                itemText = getDataText(mDataList.get(index));
            }
        }
        return itemText;
    }

    /**
     * obtain item text
     *
     * @param item item数据
     * @return 文本内容
     */
    protected String getDataText(T item) {
        if (item == null) {
            return "";
        } else if (item instanceof IWheelEntity) {
            return ((IWheelEntity) item).getWheelText();
        } else if (item instanceof Integer) {
            //정수인 경우 최소 두 자리 수가 예약됩니다.
            return isIntegerNeedFormat ? String.format(Locale.getDefault(), mIntegerFormat, item)
                    : String.valueOf(item);
        } else if (item instanceof String) {
            return (String) item;
        }
        return item.toString();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Masking crashes when data is not set when touch causes incorrect operation data Issue 20
        if (!isEnabled() || mDataList.isEmpty()) {
            return super.onTouchEvent(event);
        }
        initVelocityTracker();
        mVelocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //手指按下
                //Handling slide event nesting intercept event sequences
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                //스크롤이 완료되지 않은 경우 강제로 스크롤을 완료하십시오.
                if (!mScroller.isFinished()) {
                    //强制滚动完成
                    mScroller.forceFinished(true);
                    isForceFinishScroll = true;
                }
                mLastTouchY = event.getY();
                //按下时间
                mDownStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                //手指移动
                float moveY = event.getY();
                float deltaY = moveY - mLastTouchY;

                if (mOnWheelChangedListener != null) {
                    mOnWheelChangedListener.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);
                }
                onWheelScrollStateChanged(SCROLL_STATE_DRAGGING);
                if (Math.abs(deltaY) < 1) {
                    break;
                }
                //deltaY 上滑为正，下滑为负
                doScroll((int) -deltaY);
                mLastTouchY = moveY;
                invalidateIfYChanged();

                break;
            case MotionEvent.ACTION_UP:
                //手指抬起
                isForceFinishScroll = false;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                float velocityY = mVelocityTracker.getYVelocity();
                if (Math.abs(velocityY) > mMinFlingVelocity) {
                    //快速滑动
                    mScroller.forceFinished(true);
                    isFlingScroll = true;
                    mScroller.fling(0, mScrollOffsetY, 0, (int) -velocityY, 0, 0,
                            mMinScrollY, mMaxScrollY);
                } else {
                    int clickToCenterDistance = 0;
                    if (System.currentTimeMillis() - mDownStartTime <= DEFAULT_CLICK_CONFIRM) {
                        //处理点击滚动
                        //手指抬起的位置到中心的距离为滚动差值
                        clickToCenterDistance = (int) (event.getY() - mCenterY);
                    }
                    int scrollRange = clickToCenterDistance +
                            calculateDistanceToEndPoint((mScrollOffsetY + clickToCenterDistance) % dividedItemHeight());
                    //大于最小值滚动值
                    boolean isInMinRange = scrollRange < 0 && mScrollOffsetY + scrollRange >= mMinScrollY;
                    //小于最大滚动值
                    boolean isInMaxRange = scrollRange > 0 && mScrollOffsetY + scrollRange <= mMaxScrollY;
                    if (isInMinRange || isInMaxRange) {
                        //스크롤 범위 내의 위치 수정
                        //平稳滑动
                        mScroller.startScroll(0, mScrollOffsetY, 0, scrollRange);
                    }
                }

                invalidateIfYChanged();
                ViewCompat.postOnAnimation(this, this);
                //回收 VelocityTracker
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                //事件被终止
                //回收
                recycleVelocityTracker();
                break;
        }
        return true;
    }

    /**
     * 初始化 VelocityTracker
     */
    private void initVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    /**
     * 回收 VelocityTracker
     */
    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 计算滚动偏移
     *
     * @param distance 滚动距离
     */
    private void doScroll(int distance) {
        mScrollOffsetY += distance;
        if (!isCyclic) {
            //修正边界
            if (mScrollOffsetY < mMinScrollY) {
                mScrollOffsetY = mMinScrollY;
            } else if (mScrollOffsetY > mMaxScrollY) {
                mScrollOffsetY = mMaxScrollY;
            }
        }
    }

    /**
     * Redraw when the offset value of the Y axis changes, reduce the number of times of return
     */
    private void invalidateIfYChanged() {
        if (mScrollOffsetY != mScrolledY) {
            mScrolledY = mScrollOffsetY;
            //滚动偏移发生变化
            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelScroll(mScrollOffsetY);
            }
            onWheelScroll(mScrollOffsetY);
            //观察item变化
            observeItemChanged();
            invalidate();
        }
    }

    /**
     * 观察item改变
     */
    private void observeItemChanged() {
        //item改变回调
        int oldPosition = mCurrentScrollPosition;
        int newPosition = getCurrentPosition();
        if (oldPosition != newPosition) {
            //改变了
            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelItemChanged(oldPosition, newPosition);
            }
            onWheelItemChanged(oldPosition, newPosition);
            //播放音频
            playSoundEffect();
            //更新下标
            mCurrentScrollPosition = newPosition;
        }
    }

    /**
     * 播放滚动音效
     */
    public void playSoundEffect() {
        if (mSoundHelper != null && isSoundEffect) {
            mSoundHelper.playSoundEffect();
        }
    }

    /**
     * Forced scrolling completed, stop directly
     */
    public void forceFinishScroll() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
    }

    /**
     * Force scrolling to complete and scroll directly to the final position
     */
    public void abortFinishScroll() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    /**
     * Calculate the offset of the distance end point and correct the selected item
     *
     * @param remainder 余数
     * @return 偏移量
     */
    private int calculateDistanceToEndPoint(int remainder) {
        if (Math.abs(remainder) > mItemHeight / 2) {
            if (mScrollOffsetY < 0) {
                return -mItemHeight - remainder;
            } else {
                return mItemHeight - remainder;
            }
        } else {
            return -remainder;
        }
    }

    /**
     * Use the run method instead of computeScroll because invalidate also executes computeScroll causing callback execution to be inaccurate
     */
    @Override
    public void run() {
        //停止滚动更新当前下标
        if (mScroller.isFinished() && !isForceFinishScroll && !isFlingScroll) {
            if (mItemHeight == 0) return;
            //滚动状态停止
            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            }
            onWheelScrollStateChanged(SCROLL_STATE_IDLE);
            int currentItemPosition = getCurrentPosition();
            //현재 선택된 포지션은 변경되지 않을 때 콜백하지 않습니다. onItemSelected()
            if (currentItemPosition == mSelectedItemPosition) {
                return;
            }
            mSelectedItemPosition = currentItemPosition;
            //중지 및 재할당
            mCurrentScrollPosition = mSelectedItemPosition;

            //스크롤을 중지하고 항목 콜백을 선택합니다.
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(this, mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            }
            onItemSelected(mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            //스크롤 상태 콜백
            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelSelected(mSelectedItemPosition);
            }
            onWheelSelected(mSelectedItemPosition);
        }

        if (mScroller.computeScrollOffset()) {
            int oldY = mScrollOffsetY;
            mScrollOffsetY = mScroller.getCurrY();

            if (oldY != mScrollOffsetY) {
                if (mOnWheelChangedListener != null) {
                    mOnWheelChangedListener.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
                }
                onWheelScrollStateChanged(SCROLL_STATE_SCROLLING);
            }
            invalidateIfYChanged();
            ViewCompat.postOnAnimation(this, this);
        } else if (isFlingScroll) {
            //스크롤이 완료되면 빠른 스크롤 프로세스인지 여부에 따라 최종 위치를 조정해야합니다.
            isFlingScroll = false;
            //빠른 스크롤 후 스크롤 후 최종 위치를 조정하고 스크롤을 다시 시작하여 중앙 위치로 슬라이드해야합니다.
            mScroller.startScroll(0, mScrollOffsetY, 0, calculateDistanceToEndPoint(mScrollOffsetY % dividedItemHeight()));
            invalidateIfYChanged();
            ViewCompat.postOnAnimation(this, this);
        }
    }

    /**
     * 오프셋을 기준으로 현재 위치 첨자를 계산합니다.
     *
     * @return 오프셋에 해당하는 현재 아래 첨자 if dataList is empty return -1
     */
    private int getCurrentPosition() {
        if (mDataList.isEmpty()) {
            return -1;
        }
        int itemPosition;
        if (mScrollOffsetY < 0) {
            itemPosition = (mScrollOffsetY - mItemHeight / 2) / dividedItemHeight();
        } else {
            itemPosition = (mScrollOffsetY + mItemHeight / 2) / dividedItemHeight();
        }
        int currentPosition = itemPosition % mDataList.size();
        if (currentPosition < 0) {
            currentPosition += mDataList.size();
        }

        return currentPosition;
    }

    /**
     * mItemHeight 제수일 때 0을 피하십시오.
     *
     * @return 被除数不为0
     */
    private int dividedItemHeight() {
        return mItemHeight > 0 ? mItemHeight : 1;
    }

    /**
     * 获取音效开关状态
     *
     * @return 是否开启滚动音效
     */
    public boolean isSoundEffect() {
        return isSoundEffect;
    }

    /**
     * 设置音效开关
     *
     * @param isSoundEffect 是否开启滚动音效
     */
    public void setSoundEffect(boolean isSoundEffect) {
        this.isSoundEffect = isSoundEffect;
    }

    /**
     * 设置声音效果资源
     *
     * @param rawResId 声音效果资源 越小效果越好 {@link RawRes}
     */
    public void setSoundEffectResource(@RawRes int rawResId) {
        if (mSoundHelper != null) {
            mSoundHelper.load(getContext(), rawResId);
        }
    }

    /**
     * 获取播放音量
     *
     * @return 播放音量 range 0.0-1.0
     */
    public float getPlayVolume() {
        return mSoundHelper == null ? 0 : mSoundHelper.getPlayVolume();
    }

    /**
     * 设置播放音量
     *
     * @param playVolume 播放音量 range 0.0-1.0
     */
    public void setPlayVolume(@FloatRange(from = 0.0, to = 1.0) float playVolume) {
        if (mSoundHelper != null) {
            mSoundHelper.setPlayVolume(playVolume);
        }
    }

    /**
     * 获取指定 position 的数据
     *
     * @param position 下标
     * @return position 对应的数据 {@link Nullable}
     */
    @Nullable
    public T getItemData(int position) {
        if (isPositionInRange(position)) {
            return mDataList.get(position);
        } else if (mDataList.size() > 0 && position >= mDataList.size()) {
            return mDataList.get(mDataList.size() - 1);
        } else if (mDataList.size() > 0 && position < 0) {
            return mDataList.get(0);
        }
        return null;
    }

    /**
     * 获取当前选中的item数据
     *
     * @return 当前选中的item数据
     */
    public T getSelectedItemData() {
        return getItemData(mSelectedItemPosition);
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    public List<T> getData() {
        return mDataList;
    }

    /**
     * 设置数据
     *
     * @param dataList 数据列表
     */
    public void setData(List<T> dataList) {
        if (dataList == null) {
            return;
        }
        mDataList = dataList;
        if (!isResetSelectedPosition && mDataList.size() > 0) {
            //선택한 아래 첨자를 재설정하지 마십시오.
            if (mSelectedItemPosition >= mDataList.size()) {
                mSelectedItemPosition = mDataList.size() - 1;
                //重置滚动下标
                mCurrentScrollPosition = mSelectedItemPosition;
            }
        } else {
            //선택한 첨자 및 스크롤 첨자 재설정
            mCurrentScrollPosition = mSelectedItemPosition = 0;
        }
        //强制滚动完成
        forceFinishScroll();
        calculateTextSize();
        calculateLimitY();
        //重置滚动偏移
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        requestLayout();
        invalidate();
    }

    /**
     * Whether to reset the selected subscript to the first one when the data changes
     *
     * @return 是否重置选中下标到第一个
     */
    public boolean isResetSelectedPosition() {
        return isResetSelectedPosition;
    }

    /**
     * Set whether to reset the selected subscript to the first one when the data changes
     *
     * @param isResetSelectedPosition 当数据变化时,是否重置选中下标到第一个
     */
    public void setResetSelectedPosition(boolean isResetSelectedPosition) {
        this.isResetSelectedPosition = isResetSelectedPosition;
    }

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * 设置字体大小
     *
     * @param textSize 字体大小
     */
    public void setTextSize(float textSize) {
        setTextSize(textSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param textSize 字体大小
     * @param isSp     单位是否是 sp
     */
    public void setTextSize(float textSize, boolean isSp) {
        float tempTextSize = mTextSize;
        mTextSize = isSp ? sp2px(textSize) : textSize;
        if (tempTextSize == mTextSize) {
            return;
        }
        //强制滚动完成
        forceFinishScroll();
        calculateTextSize();
        calculateDrawStart();
        calculateLimitY();
        //字体大小变化，偏移距离也变化了
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        requestLayout();
        invalidate();
    }

    /**
     * 글꼴 크기가 자동으로 조정되어 전체를 표시할지 여부를 가져옵니다.
     *
     * @return 글꼴 크기를 자동으로 조정할지 여부
     */
    public boolean isAutoFitTextSize() {
        return isAutoFitTextSize;
    }

    /**
     * 글꼴 크기를 자동으로 조정하여 전체를 표시할지 여부를 설정합니다.
     *
     * @param isAutoFitTextSize 是否自动调整字体大小
     */
    public void setAutoFitTextSize(boolean isAutoFitTextSize) {
        this.isAutoFitTextSize = isAutoFitTextSize;
        invalidate();
    }

    /**
     * 获取当前字体
     *
     * @return 字体
     */
    public Typeface getTypeface() {
        return mPaint.getTypeface();
    }

    /**
     * 设置当前字体
     *
     * @param typeface 字体
     */
    public void setTypeface(Typeface typeface) {
        setTypeface(typeface, false);
    }

    /**
     * 设置当前字体
     *
     * @param typeface              字体
     * @param isBoldForSelectedItem 선택한 항목의 글꼴이 굵게 설정되면 다른 항목은 굵게 표시되지 않습니다.
     */
    public void setTypeface(Typeface typeface, boolean isBoldForSelectedItem) {
        if (typeface == null || mPaint.getTypeface() == typeface) {
            return;
        }
        //强制滚动完成
        forceFinishScroll();
        mIsBoldForSelectedItem = isBoldForSelectedItem;
        if (mIsBoldForSelectedItem) {
            //如果设置了选中条目字体加粗，其他条目不会加粗，则拆分两份字体
            if (typeface.isBold()) {
                mNormalTypeface = Typeface.create(typeface, Typeface.NORMAL);
                mBoldTypeface = typeface;
            } else {
                mNormalTypeface = typeface;
                mBoldTypeface = Typeface.create(typeface, Typeface.BOLD);
            }
            //굵은 글꼴은 일반 글꼴보다 넓기 때문에 측정 할 때 굵은 글꼴을 사용하여 측정합니다.
            mPaint.setTypeface(mBoldTypeface);
        } else {
            mPaint.setTypeface(typeface);
        }
        calculateTextSize();
        calculateDrawStart();
        //글꼴 크기가 변경되었으며 오프셋 거리도 변경되었습니다.
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        calculateLimitY();
        requestLayout();
        invalidate();
    }

    /**
     * 텍스트 맞춤을 가져옵니다.
     *
     * @return 文字对齐
     * {@link #TEXT_ALIGN_LEFT}
     * {@link #TEXT_ALIGN_CENTER}
     * {@link #TEXT_ALIGN_RIGHT}
     */
    public int getTextAlign() {
        return mTextAlign;
    }

    /**
     * 텍스트 맞춤을 설정합니다.
     *
     * @param textAlign 文字对齐方式
     *                  {@link #TEXT_ALIGN_LEFT}
     *                  {@link #TEXT_ALIGN_CENTER}
     *                  {@link #TEXT_ALIGN_RIGHT}
     */
    public void setTextAlign(@TextAlign int textAlign) {
        if (mTextAlign == textAlign) {
            return;
        }
        mTextAlign = textAlign;
        updateTextAlign();
        calculateDrawStart();
        invalidate();
    }

    /**
     * 선택되지 않은 항목의 색을 가져옵니다.
     *
     * @return 未选中条目颜色 ColorInt
     */
    public int getNormalItemTextColor() {
        return mTextColor;
    }

    /**
     * 선택되지 않은 항목의 색상을 설정합니다.
     *
     * @param textColorRes 입력 색상이 선택되지 않았습니다. {@link ColorRes}
     */
    public void setNormalItemTextColorRes(@ColorRes int textColorRes) {
        setNormalItemTextColor(ContextCompat.getColor(getContext(), textColorRes));
    }

    /**
     * 선택되지 않은 항목의 색상을 설정합니다.
     *
     * @param textColor 입력 색상이 선택되지 않았습니다. {@link ColorInt}
     */
    public void setNormalItemTextColor(@ColorInt int textColor) {
        if (mTextColor == textColor) {
            return;
        }
        mTextColor = textColor;
        invalidate();
    }

    /**
     * 선택한 항목의 색을 가져옵니다.
     *
     * @return 选中条目颜色 ColorInt
     */
    public int getSelectedItemTextColor() {
        return mSelectedItemTextColor;
    }

    /**
     * 선택한 항목의 색상을 설정합니다.
     *
     * @param selectedItemColorRes 选中条目颜色 {@link ColorRes}
     */
    public void setSelectedItemTextColorRes(@ColorRes int selectedItemColorRes) {
        setSelectedItemTextColor(ContextCompat.getColor(getContext(), selectedItemColorRes));
    }

    /**
     * 선택한 항목의 색상을 설정합니다.
     *
     * @param selectedItemTextColor 选中条目颜色 {@link ColorInt}
     */
    public void setSelectedItemTextColor(@ColorInt int selectedItemTextColor) {
        if (mSelectedItemTextColor == selectedItemTextColor) {
            return;
        }
        mSelectedItemTextColor = selectedItemTextColor;
        invalidate();
    }

    /**
     * 텍스트 거리 경계의 여백을 가져옵니다.
     *
     * @return 外边距值
     */
    public float getTextBoundaryMargin() {
        return mTextBoundaryMargin;
    }

    /**
     * 设置文字距离边界的外边距
     *
     * @param textBoundaryMargin 外边距值
     */
    public void setTextBoundaryMargin(float textBoundaryMargin) {
        setTextBoundaryMargin(textBoundaryMargin, false);
    }

    /**
     * 문자 거리 경계의 여백을 설정합니다.
     *
     * @param textBoundaryMargin 外边距值
     * @param isDp               单位是否为 dp
     */
    public void setTextBoundaryMargin(float textBoundaryMargin, boolean isDp) {
        float tempTextBoundaryMargin = mTextBoundaryMargin;
        mTextBoundaryMargin = isDp ? dp2px(textBoundaryMargin) : textBoundaryMargin;
        if (tempTextBoundaryMargin == mTextBoundaryMargin) {
            return;
        }
        requestLayout();
        invalidate();
    }

    /**
     * 获取item间距
     *
     * @return 行间距值
     */
    public float getLineSpacing() {
        return mLineSpacing;
    }

    /**
     * 设置item间距
     *
     * @param lineSpacing 行间距值
     */
    public void setLineSpacing(float lineSpacing) {
        setLineSpacing(lineSpacing, false);
    }

    /**
     * 设置item间距
     *
     * @param lineSpacing 行间距值
     * @param isDp        lineSpacing 单位是否为 dp
     */
    public void setLineSpacing(float lineSpacing, boolean isDp) {
        float tempLineSpace = mLineSpacing;
        mLineSpacing = isDp ? dp2px(lineSpacing) : lineSpacing;
        if (tempLineSpace == mLineSpacing) {
            return;
        }
        mScrollOffsetY = 0;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 데이터가 정수 형식일 때 변환이 필요한지 여부
     *
     * @return isIntegerNeedFormat
     */
    public boolean isIntegerNeedFormat() {
        return isIntegerNeedFormat;
    }

    /**
     * 데이터를 Integer 형식으로 설정할 때 변환이 필요한지 여부
     *
     * @param isIntegerNeedFormat 데이터가 정수 형식일 때 변환이 필요한지 여부
     */
    public void setIntegerNeedFormat(boolean isIntegerNeedFormat) {
        if (this.isIntegerNeedFormat == isIntegerNeedFormat) {
            return;
        }
        this.isIntegerNeedFormat = isIntegerNeedFormat;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 同时设置 isIntegerNeedFormat=true 和 mIntegerFormat=integerFormat
     *
     * @param integerFormat 注意：integerFormat 형식 지정자를 하나만 포함해야 하며 포함할 수 있습니다.（format specifier）
     *                      格式说明符请参照 http://java2s.com/Tutorials/Java/Data_Format/Java_Format_Specifier.htm
     *                      <p>
     *                      형식 지정자가 두 개 이상 있으면 throw됩니다. java.util.MissingFormatArgumentException: Format specifier '%s'(多出来的说明符)
     */
    public void setIntegerNeedFormat(String integerFormat) {
        isIntegerNeedFormat = true;
        mIntegerFormat = integerFormat;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取Integer类型转换格式
     *
     * @return integerFormat
     */
    public String getIntegerFormat() {
        return mIntegerFormat;
    }

    /**
     * 设置Integer类型转换格式
     *
     * @param integerFormat 참고: integerFormat은 하나의 형식 지정자만 포함해야 하며 포함할 수 있습니다.（format specifier）
     *                      格式说明符请参照 http://java2s.com/Tutorials/Java/Data_Format/Java_Format_Specifier.htm
     *                      <p>
     *                      둘 이상의 형식 지정자가있는 경우 java.util.MissingFormatArgumentException이 발생합니다.: Format specifier '%s'(多出来的说明符)
     */
    public void setIntegerFormat(String integerFormat) {
        if (TextUtils.isEmpty(integerFormat) || integerFormat.equals(mIntegerFormat)) {
            return;
        }
        mIntegerFormat = integerFormat;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取可见条目数
     *
     * @return 可见条目数
     */
    public int getVisibleItems() {
        return mVisibleItems;
    }

    /**
     * 设置可见的条目数
     *
     * @param visibleItems 可见条目数
     */
    public void setVisibleItems(int visibleItems) {
        if (mVisibleItems == visibleItems) {
            return;
        }
        mVisibleItems = adjustVisibleItems(visibleItems);
        mScrollOffsetY = 0;
        requestLayout();
        invalidate();
    }

    /**
     * 跳转可见条目数为奇数
     *
     * @param visibleItems 可见条目数
     * @return 调整后的可见条目数
     */
    private int adjustVisibleItems(int visibleItems) {
        return Math.abs(visibleItems / 2 * 2 + 1); // 当传入的值为偶数时,换算成奇数;
    }

    /**
     * 是否是循环滚动
     *
     * @return 是否是循环滚动
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * 设置是否循环滚动
     *
     * @param isCyclic 是否是循环滚动
     */
    public void setCyclic(boolean isCyclic) {
        if (this.isCyclic == isCyclic) {
            return;
        }
        this.isCyclic = isCyclic;

        forceFinishScroll();
        calculateLimitY();
        //设置当前选中的偏移值
        mScrollOffsetY = mSelectedItemPosition * mItemHeight;
        invalidate();
    }

    /**
     * 获取当前选中下标
     *
     * @return 当前选中的下标
     */
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    /**
     * 设置当前选中下标
     *
     * @param position 下标
     */
    public void setSelectedItemPosition(int position) {
        setSelectedItemPosition(position, false);
    }

    /**
     * 设置当前选中下标
     *
     * @param position       下标
     * @param isSmoothScroll 是否平滑滚动
     */
    public void setSelectedItemPosition(int position, boolean isSmoothScroll) {
        setSelectedItemPosition(position, isSmoothScroll, 0);
    }

    /**
     * 设置当前选中下标
     * <p>
     * bug 레코드 수정: 대부분의 경우 이 메서드를 호출할 때 초기화할 때 onSizeChanged() 메서드가 실행되지 않으면 이 메서드를 호출하면 무효화됩니다.
     * 因为 onSizeChanged() 경계와 같은 정보는 메서드 실행이 끝난 후에 만 결정됩니다.
     * 所以在 onSizeChanged() 롤오버 메서드는 롤오버에 대해 mSelectedItemPosition >0이 다시 계산되는 경우 호환성을 추가합니다.。
     *
     * @param position       下标
     * @param isSmoothScroll 是否平滑滚动
     * @param smoothDuration 平滑滚动时间
     */
    public void setSelectedItemPosition(int position, boolean isSmoothScroll, int smoothDuration) {
        if (!isPositionInRange(position)) {
            return;
        }

        //item之间差值
        int itemDistance = calculateItemDistance(position);
        if (itemDistance == 0) {
            return;
        }
        //如果Scroller滑动未停止，强制结束动画
        abortFinishScroll();

        if (isSmoothScroll) {
            //부드러운 스크롤이고 이전 스크롤 스크롤이 완료된 경우
            mScroller.startScroll(0, mScrollOffsetY, 0, itemDistance,
                    smoothDuration > 0 ? smoothDuration : DEFAULT_SCROLL_DURATION);
            invalidateIfYChanged();
            ViewCompat.postOnAnimation(this, this);

        } else {
            doScroll(itemDistance);
            mSelectedItemPosition = position;
            //选中条目回调
            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(this, mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            }
            onItemSelected(mDataList.get(mSelectedItemPosition), mSelectedItemPosition);
            if (mOnWheelChangedListener != null) {
                mOnWheelChangedListener.onWheelSelected(mSelectedItemPosition);
            }
            onWheelSelected(mSelectedItemPosition);
            invalidateIfYChanged();
        }

    }

    private int calculateItemDistance(int position) {
        return position * mItemHeight - mScrollOffsetY;
    }

    /**
     * 判断下标是否在数据列表范围内
     *
     * @param position 下标
     * @return 是否在数据列表范围内
     */
    public boolean isPositionInRange(int position) {
        return position >= 0 && position < mDataList.size();
    }

    /**
     * 获取是否显示分割线
     *
     * @return 是否显示分割线
     */
    public boolean isShowDivider() {
        return isShowDivider;
    }

    /**
     * 设置是否显示分割线
     *
     * @param isShowDivider 是否显示分割线
     */
    public void setShowDivider(boolean isShowDivider) {
        if (this.isShowDivider == isShowDivider) {
            return;
        }
        this.isShowDivider = isShowDivider;
        invalidate();
    }

    /**
     * 获取分割线颜色
     *
     * @return 分割线颜色 ColorInt
     */
    public int getDividerColor() {
        return mDividerColor;
    }

    /**
     * 设置分割线颜色
     *
     * @param dividerColorRes 分割线颜色 {@link ColorRes}
     */
    public void setDividerColorRes(@ColorRes int dividerColorRes) {
        setDividerColor(ContextCompat.getColor(getContext(), dividerColorRes));
    }

    /**
     * 设置分割线颜色
     *
     * @param dividerColor 分割线颜色 {@link ColorInt}
     */
    public void setDividerColor(@ColorInt int dividerColor) {
        if (mDividerColor == dividerColor) {
            return;
        }
        mDividerColor = dividerColor;
        invalidate();
    }

    /**
     * 获取分割线高度
     *
     * @return 分割线高度
     */
    public float getDividerHeight() {
        return mDividerSize;
    }

    /**
     * 设置分割线高度
     *
     * @param dividerHeight 分割线高度
     */
    public void setDividerHeight(float dividerHeight) {
        setDividerHeight(dividerHeight, false);
    }

    /**
     * 设置分割线高度
     *
     * @param dividerHeight 分割线高度
     * @param isDp          单位是否是 dp
     */
    public void setDividerHeight(float dividerHeight, boolean isDp) {
        float tempDividerHeight = mDividerSize;
        mDividerSize = isDp ? dp2px(dividerHeight) : dividerHeight;
        if (tempDividerHeight == mDividerSize) {
            return;
        }
        invalidate();
    }

    /**
     * 获取分割线填充类型
     *
     * @return 分割线填充类型
     * {@link #DIVIDER_TYPE_FILL}
     * {@link #DIVIDER_TYPE_WRAP}
     */
    public int getDividerType() {
        return mDividerType;
    }

    /**
     * 设置分割线填充类型
     *
     * @param dividerType 分割线填充类型
     *                    {@link #DIVIDER_TYPE_FILL}
     *                    {@link #DIVIDER_TYPE_WRAP}
     */
    public void setDividerType(@DividerType int dividerType) {
        if (mDividerType == dividerType) {
            return;
        }
        mDividerType = dividerType;
        invalidate();
    }

    /**
     * Split line padding when getting adaptive split line type
     *
     * @return 分割线内边距
     */
    public float getDividerPaddingForWrap() {
        return mDividerPaddingForWrap;
    }

    /**
     * 设置自适应分割线类型时的分割线内边距
     *
     * @param dividerPaddingForWrap 分割线内边距
     */
    public void setDividerPaddingForWrap(float dividerPaddingForWrap) {
        setDividerPaddingForWrap(dividerPaddingForWrap, false);
    }

    /**
     * Split line padding when setting the adaptive split line type
     *
     * @param dividerPaddingForWrap 分割线内边距
     * @param isDp                  单位是否是 dp
     */
    public void setDividerPaddingForWrap(float dividerPaddingForWrap, boolean isDp) {
        float tempDividerPadding = mDividerPaddingForWrap;
        mDividerPaddingForWrap = isDp ? dp2px(dividerPaddingForWrap) : dividerPaddingForWrap;
        if (tempDividerPadding == mDividerPaddingForWrap) {
            return;
        }
        invalidate();
    }

    /**
     * 获取分割线两端形状
     *
     * @return 分割线两端形状
     * {@link Paint.Cap#BUTT}
     * {@link Paint.Cap#ROUND}
     * {@link Paint.Cap#SQUARE}
     */
    public Paint.Cap getDividerCap() {
        return mDividerCap;
    }

    /**
     * 设置分割线两端形状
     *
     * @param dividerCap 分割线两端形状
     *                   {@link Paint.Cap#BUTT}
     *                   {@link Paint.Cap#ROUND}
     *                   {@link Paint.Cap#SQUARE}
     */
    public void setDividerCap(Paint.Cap dividerCap) {
        if (mDividerCap == dividerCap) {
            return;
        }
        mDividerCap = dividerCap;
        invalidate();
    }

    /**
     * 获取是否绘制选中区域
     *
     * @return 是否绘制选中区域
     */
    public boolean isDrawSelectedRect() {
        return isDrawSelectedRect;
    }

    /**
     * 设置是否绘制选中区域
     *
     * @param isDrawSelectedRect 是否绘制选中区域
     */
    public void setDrawSelectedRect(boolean isDrawSelectedRect) {
        this.isDrawSelectedRect = isDrawSelectedRect;
        invalidate();
    }

    /**
     * 获取选中区域颜色
     *
     * @return 选中区域颜色 ColorInt
     */
    public int getSelectedRectColor() {
        return mSelectedRectColor;
    }

    /**
     * 设置选中区域颜色
     *
     * @param selectedRectColorRes 选中区域颜色 {@link ColorRes}
     */
    public void setSelectedRectColorRes(@ColorRes int selectedRectColorRes) {
        setSelectedRectColor(ContextCompat.getColor(getContext(), selectedRectColorRes));
    }

    /**
     * 设置选中区域颜色
     *
     * @param selectedRectColor 选中区域颜色 {@link ColorInt}
     */
    public void setSelectedRectColor(@ColorInt int selectedRectColor) {
        mSelectedRectColor = selectedRectColor;
        invalidate();
    }

    /**
     * 获取是否是弯曲（3D）效果
     *
     * @return 是否是弯曲（3D）效果
     */
    public boolean isCurved() {
        return isCurved;
    }

    /**
     * 设置是否是弯曲（3D）效果
     *
     * @param isCurved 是否是弯曲（3D）效果
     */
    public void setCurved(boolean isCurved) {
        if (this.isCurved == isCurved) {
            return;
        }
        this.isCurved = isCurved;
        calculateTextSize();
        requestLayout();
        invalidate();
    }

    /**
     * 获取弯曲（3D）效果左右圆弧效果方向
     *
     * @return 左右圆弧效果方向
     * {@link #CURVED_ARC_DIRECTION_LEFT}
     * {@link #CURVED_ARC_DIRECTION_CENTER}
     * {@link #CURVED_ARC_DIRECTION_RIGHT}
     */
    public int getCurvedArcDirection() {
        return mCurvedArcDirection;
    }

    /**
     * 设置弯曲（3D）效果左右圆弧效果方向
     *
     * @param curvedArcDirection 左右圆弧效果方向
     *                           {@link #CURVED_ARC_DIRECTION_LEFT}
     *                           {@link #CURVED_ARC_DIRECTION_CENTER}
     *                           {@link #CURVED_ARC_DIRECTION_RIGHT}
     */
    public void setCurvedArcDirection(@CurvedArcDirection int curvedArcDirection) {
        if (mCurvedArcDirection == curvedArcDirection) {
            return;
        }
        mCurvedArcDirection = curvedArcDirection;
        invalidate();
    }

    /**
     * 获取弯曲（3D）效果左右圆弧偏移效果方向系数
     *
     * @return 左右圆弧偏移效果方向系数
     */
    public float getCurvedArcDirectionFactor() {
        return mCurvedArcDirectionFactor;
    }

    /**
     * 设置弯曲（3D）效果左右圆弧偏移效果方向系数
     *
     * @param curvedArcDirectionFactor 左右圆弧偏移效果方向系数
     *                                 range 0.0-1.0 越大越明显
     */
    public void setCurvedArcDirectionFactor(@FloatRange(from = 0.0f, to = 1.0f) float curvedArcDirectionFactor) {
        if (mCurvedArcDirectionFactor == curvedArcDirectionFactor) {
            return;
        }
        if (curvedArcDirectionFactor < 0) {
            curvedArcDirectionFactor = 0f;
        } else if (curvedArcDirectionFactor > 1) {
            curvedArcDirectionFactor = 1f;
        }
        mCurvedArcDirectionFactor = curvedArcDirectionFactor;
        invalidate();
    }

    /**
     * 获取折射偏移比例
     *
     * @return 折射偏移比例
     */
    public float getRefractRatio() {
        return mRefractRatio;
    }

    /**
     * Set the selected item refraction offset ratio
     *
     * @param refractRatio 折射偏移比例 range 0.0-1.0
     */
    public void setRefractRatio(@FloatRange(from = 0.0f, to = 1.0f) float refractRatio) {
        float tempRefractRatio = mRefractRatio;
        mRefractRatio = refractRatio;
        if (mRefractRatio > 1f) {
            mRefractRatio = 1.0f;
        } else if (mRefractRatio < 0f) {
            mRefractRatio = DEFAULT_REFRACT_RATIO;
        }
        if (tempRefractRatio == mRefractRatio) {
            return;
        }
        invalidate();
    }

    @Deprecated
    public float getCurvedRefractRatio() {
        return mRefractRatio;
    }

    @Deprecated
    public void setCurvedRefractRatio(@FloatRange(from = 0.0f, to = 1.0f) float refractRatio) {
        setRefractRatio(refractRatio);
    }

    /**
     * 获取选中监听
     *
     * @return 选中监听器
     */
    public OnItemSelectedListener<T> getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    /**
     * 设置选中监听
     *
     * @param onItemSelectedListener 选中监听器
     */
    public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    /**
     * 获取滚动变化监听
     *
     * @return 滚动变化监听器
     */
    public OnWheelChangedListener getOnWheelChangedListener() {
        return mOnWheelChangedListener;
    }

    /**
     * 设置滚动变化监听
     *
     * @param onWheelChangedListener 滚动变化监听器
     */
    public void setOnWheelChangedListener(OnWheelChangedListener onWheelChangedListener) {
        mOnWheelChangedListener = onWheelChangedListener;
    }

    /*
      --------- 滚动变化方法同监听器方法（适用于子类） ------
     */

    /**
     * WheelView 滚动
     *
     * @param scrollOffsetY 滚动偏移
     */
    protected void onWheelScroll(int scrollOffsetY) {

    }

    /**
     * WheelView 条目变化
     *
     * @param oldPosition 旧的下标
     * @param newPosition 新下标
     */
    protected void onWheelItemChanged(int oldPosition, int newPosition) {

    }

    /**
     * WheelView 选中
     *
     * @param position 选中的下标
     */
    protected void onWheelSelected(int position) {

    }

    /**
     * WheelView 滚动状态
     *
     * @param state 滚动状态
     *              {@link WheelView#SCROLL_STATE_IDLE}
     *              {@link WheelView#SCROLL_STATE_DRAGGING}
     *              {@link WheelView#SCROLL_STATE_SCROLLING}
     */
    protected void onWheelScrollStateChanged(int state) {

    }

    /**
     * 条目选中回调
     *
     * @param data     选中的数据
     * @param position 选中的下标
     */
    protected void onItemSelected(T data, int position) {

    }

    /*
      --------- 滚动变化方法同监听器方法（适用于子类） ------
     */

    /**
     * dp转换px
     *
     * @param dp dp值
     * @return 转换后的px值
     */
    protected static float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * sp转换px
     *
     * @param sp sp值
     * @return 转换后的px值
     */
    protected static float sp2px(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 自定义文字对齐方式注解
     * <p>
     * {@link #mTextAlign}
     * {@link #setTextAlign(int)}
     */
    @IntDef({TEXT_ALIGN_LEFT, TEXT_ALIGN_CENTER, TEXT_ALIGN_RIGHT})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface TextAlign {
    }

    /**
     * 自定义左右圆弧效果方向注解
     * <p>
     * {@link #mCurvedArcDirection}
     * {@link #setCurvedArcDirection(int)}
     */
    @IntDef({CURVED_ARC_DIRECTION_LEFT, CURVED_ARC_DIRECTION_CENTER, CURVED_ARC_DIRECTION_RIGHT})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface CurvedArcDirection {
    }

    /**
     * 自定义分割线类型注解
     * <p>
     * {@link #mDividerType}
     * {@link #setDividerType(int)}
     */
    @IntDef({DIVIDER_TYPE_FILL, DIVIDER_TYPE_WRAP})
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface DividerType {
    }

    /**
     * 条目选中监听器
     *
     * @param <T>
     */
    public interface OnItemSelectedListener<T> {

        /**
         * 条目选中回调
         *
         * @param wheelView wheelView
         * @param data      选中的数据
         * @param position  选中的下标
         */
        void onItemSelected(WheelView<T> wheelView, T data, int position);
    }

    /**
     * WheelView滚动状态改变监听器
     */
    public interface OnWheelChangedListener {

        /**
         * WheelView 滚动
         *
         * @param scrollOffsetY 滚动偏移
         */
        void onWheelScroll(int scrollOffsetY);

        /**
         * WheelView 条目变化
         *
         * @param oldPosition 旧的下标
         * @param newPosition 新下标
         */
        void onWheelItemChanged(int oldPosition, int newPosition);

        /**
         * WheelView 选中
         *
         * @param position 选中的下标
         */
        void onWheelSelected(int position);

        /**
         * WheelView 滚动状态
         *
         * @param state 滚动状态
         *              {@link WheelView#SCROLL_STATE_IDLE}
         *              {@link WheelView#SCROLL_STATE_DRAGGING}
         *              {@link WheelView#SCROLL_STATE_SCROLLING}
         */
        void onWheelScrollStateChanged(int state);
    }

    /**
     * SoundPool 辅助类
     */
    private static class SoundHelper {

        private SoundPool mSoundPool;
        private int mSoundId;
        private float mPlayVolume;

        @SuppressWarnings("deprecation")
        private SoundHelper() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSoundPool = new SoundPool.Builder().build();
            } else {
                mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 1);
            }
        }

        /**
         * 初始化 SoundHelper
         *
         * @return SoundHelper 对象
         */
        static SoundHelper obtain() {
            return new SoundHelper();
        }

        /**
         * 加载音频资源
         *
         * @param context 上下文
         * @param resId   音频资源 {@link RawRes}
         */
        void load(Context context, @RawRes int resId) {
            if (mSoundPool != null) {
                mSoundId = mSoundPool.load(context, resId, 1);
            }
        }

        /**
         * 设置音量
         *
         * @param playVolume 音频播放音量 range 0.0-1.0
         */
        void setPlayVolume(@FloatRange(from = 0.0, to = 1.0) float playVolume) {
            this.mPlayVolume = playVolume;
        }

        /**
         * 获取音量
         *
         * @return 音频播放音量 range 0.0-1.0
         */
        float getPlayVolume() {
            return mPlayVolume;
        }

        /**
         * 播放声音效果
         */
        void playSoundEffect() {
            if (mSoundPool != null && mSoundId != 0) {
                mSoundPool.play(mSoundId, mPlayVolume, mPlayVolume, 1, 0, 1);
            }
        }

        /**
         * 释放SoundPool
         */
        void release() {
            if (mSoundPool != null) {
                mSoundPool.release();
                mSoundPool = null;
            }
        }
    }
}