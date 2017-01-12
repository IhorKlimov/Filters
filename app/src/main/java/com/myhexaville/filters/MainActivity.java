package com.myhexaville.filters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.RelativeLayout;

import com.myhexaville.filters.databinding.ActivityMainBinding;

import static android.graphics.Color.TRANSPARENT;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";

    private DisplayMetrics mDisplayMetrics;
    private ActivityMainBinding mBinding;
    private float mStartX;
    private float mStartY;
    private int mBottomY;
    private int mBottomX;

    private boolean mIsCancel;
    private float mBottomListStartY;
    private boolean resetBottomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbar);

        setDisplayMetrics();

        mBinding.fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sheet)));

        Drawable d = mBinding.bottomListBackground.getBackground();
        final GradientDrawable gd = (GradientDrawable) d;

        gd.setCornerRadius(0f);
    }

    public void animate(View view) {
        if (!mIsCancel) {
            if (mStartX == 0.0f) {
                mStartX = view.getX();
                mStartY = view.getY();

                mBottomX = getBottomFilterXPosition();
                mBottomY = getBottomFilterYPosition();

                mBottomListStartY = mBinding.bottomListBackground.getY();
            }

            final int x = getFinalXPosition();
            final int y = getFinalYPosition();


            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();

                    mBinding.fab.setX(
                            x + (mStartX - x - ((mStartX - x) * v))
                    );

                    mBinding.fab.setY(
                            y + (mStartY - y - ((mStartY - y) * (v * v)))
                    );
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    removeFabBackground();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBinding.fab.animate()
                                    .y(mBottomY)
                                    .setDuration(200)
                                    .start();

                        }
                    },50);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBinding.cancel.setVisibility(VISIBLE);
                            mBinding.cancel.setTranslationX(-(mBottomX - x));

                            mBinding.cancel.animate()
                                    .translationXBy(mBottomX - x)
                                    .setDuration(200)
                                    .start();

                            mBinding.fab.animate()
                                    .x(mBottomX)
                                    .setDuration(200)
                                    .start();

                            mBinding.fab.animate()
                                    .x(mBottomX)
                                    .setDuration(200)
                                    .start();

                            mBinding.sheetTop.setScaleY(0f);
                            mBinding.sheetTop.setVisibility(VISIBLE);

                            mBinding.sheetTop.animate()
                                    .scaleY(1f)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            mBinding.scroll.setVisibility(VISIBLE);
                                        }
                                    })
                                    .setDuration(200)
                                    .start();
                        }
                    }, 200);

                    if (resetBottomList) {
                        Log.d(LOG_TAG, "onAnimationEnd: reset");
                        resetBottomListBackground();
                    }


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBinding.bottomListBackground.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            mBinding.fab.setImageResource(R.drawable.cancel);
                                            mBinding.fab.setVisibility(INVISIBLE);
                                            mBinding.fab.setX(mBinding.cancel.getX() - mDisplayMetrics.density * 4);
                                            mBinding.fab.setY(getBottomFilterYPosition());
                                            mBinding.applyFilters.setVisibility(VISIBLE);
                                        }
                                    })
                                    .start();
                        }
                    }, 400);

                    revealFilterSheet(y);
                }
            });

            animator.start();
        } else {
            mBinding.fab.setImageResource(R.drawable.filter);
            mBinding.fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sheet)));
            mIsCancel = false;
        }
    }

    private void resetBottomListBackground() {
        resetBottomList = false;
        mBinding.bottomListBackground.setVisibility(VISIBLE);
        Drawable d = mBinding.bottomListBackground.getBackground();
        final GradientDrawable gd = (GradientDrawable) d;
        mBinding.bottomListBackground.setAlpha(0f);
        gd.setCornerRadius(0f);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBinding.bottomListBackground.getLayoutParams();
        params.width = -1;
        params.height = (int) (mDisplayMetrics.density * 64);
        mBinding.bottomListBackground.setY(mBottomListStartY + mDisplayMetrics.density * 8);
        mBinding.bottomListBackground.requestLayout();
    }

    private int getBottomFilterYPosition() {
        return (int) (
                mBinding.applyFilters.getY()
                        + (mDisplayMetrics.heightPixels - getStatusBarHeight() - mDisplayMetrics.density * 64)
                        - mDisplayMetrics.density * 4);
    }

    private int getBottomFilterXPosition() {
        return (int) (
                mBinding.applyFilters.getX()
                        + mDisplayMetrics.widthPixels / 2
                        - mDisplayMetrics.density * 4);
    }

    private void removeFabBackground() {
        mBinding.fab.setBackgroundTintList(ColorStateList.valueOf(TRANSPARENT));

        mBinding.fab.setElevation(0f);
    }

    private void revealFilterSheet(int y) {
        mBinding.reveal.setVisibility(VISIBLE);

        Animator a = ViewAnimationUtils.createCircularReveal(
                mBinding.reveal,
                mDisplayMetrics.widthPixels / 2,
                (int) (y - mBinding.reveal.getY()) + getFabSize() / 2,
                getFabSize() / 2,
                mBinding.reveal.getHeight() * .7f);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBinding.list.setVisibility(VISIBLE);
            }
        });
        a.start();
    }

    public int getFinalXPosition() {
        return mDisplayMetrics.widthPixels / 2 - getFabSize() / 2;
    }

    public int getFinalYPosition() {
        int marginFromBottom = getFinalYPositionFromBottom();
        return mDisplayMetrics.heightPixels - marginFromBottom + getFabSize() / 2;
    }

    public void setDisplayMetrics() {
        mDisplayMetrics = getResources().getDisplayMetrics();
    }

    public int getFinalYPositionFromBottom() {
        return (int) (mDisplayMetrics.density * 250);
    }

    public int getFabSize() {
        return (int) (mDisplayMetrics.density * 56);
    }

    public void acceptFilters(View view) {
        mBinding.fab.setVisibility(VISIBLE);
        mBinding.list.setVisibility(INVISIBLE);
        mBinding.scroll.setVisibility(INVISIBLE); 

        mIsCancel = true;
        final int x = getFinalXPosition();
        final int y = getFinalYPosition();


        mBinding.applyFilters.setVisibility(INVISIBLE);
        mBinding.cancel.setVisibility(INVISIBLE);

        final int startX = (int) mBinding.fab.getX();
        final int startY = (int) mBinding.fab.getY();

        mBinding.sheetTop.setVisibility(INVISIBLE);
        Animator reveal = ViewAnimationUtils.createCircularReveal(
                mBinding.reveal,
                mDisplayMetrics.widthPixels / 2,
                (int) (y - mBinding.reveal.getY()) + getFabSize() / 2,
                mBinding.reveal.getHeight() * .5f,
                getFabSize() / 2);

        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBinding.reveal.setVisibility(INVISIBLE);
                mBinding.fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(MainActivity
                        .this, R.color.colorAccent)));
                mBinding.fab.setElevation(mDisplayMetrics.density * 4);

            }
        });
        reveal.start();

        animateBottomSheet();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();

                mBinding.fab.setX(
                        x - (x - startX - ((x - startX) * v))
                );

                mBinding.fab.setY(
                        y + (startY - y - ((startY - y) * (v * v)))
                );


            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mBinding.fab.animate()
                        .rotationBy(360)
                        .setDuration(1000)
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnFabToInitialPosition();
                        mBinding.bottomListBackground.setVisibility(INVISIBLE);
                    }
                }, 1000);
            }
        });
        animator.start();
    }

    private void animateBottomSheet() {
        Drawable d = mBinding.bottomListBackground.getBackground();
        final GradientDrawable gd = (GradientDrawable) d;


        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                mBinding.bottomListBackground.getLayoutParams();

        final int startWidth = mBinding.bottomListBackground.getWidth();
        final int startHeight = mBinding.bottomListBackground.getHeight();
        final int startY = (int) mBinding.bottomListBackground.getY();


        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                gd.setCornerRadius(mDisplayMetrics.density * 50 * v);

                int i = (int) (startWidth - (startWidth - getFabSize()) * v);
                params.width = i;
                params.height = (int) (startHeight - (startHeight - getFabSize()) * v);
                mBinding.bottomListBackground.setY(getFinalYPosition() + (startY
                        - getFinalYPosition()) - ((startY - getFinalYPosition()) * v));

                mBinding.bottomListBackground.requestLayout();
            }
        });
        animator.start();
    }

    private void returnFabToInitialPosition() {
        final int x = getFinalXPosition();
        final int y = getFinalYPosition();
        resetBottomList = true;




        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();

                mBinding.fab.setX(
                        x + ((mStartX - x) * v)
                );

                mBinding.fab.setY(
                        (float) (y + (mStartY - y) * (Math.pow(v, .5f)))
                );
            }
        });
        animator.start();
    }

    public int getStatusBarHeight() {
        return (int) (mDisplayMetrics.density * 24);
    }
}