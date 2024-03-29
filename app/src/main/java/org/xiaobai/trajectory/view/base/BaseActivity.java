package org.xiaobai.trajectory.view.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;


import com.kaopiz.kprogresshud.KProgressHUD;


import org.xiaobai.trajectory.BuildConfig;
import org.xiaobai.trajectory.R;

import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public abstract class BaseActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private KProgressHUD kProgressHUD;
    private CompositeDisposable mCompositeDisposable;//订阅


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutResourceID());
        ButterKnife.bind(this);
        initVariables();
        initViews(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);

        //解除所有未完成的订阅 防止内存泄漏
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    public CompositeDisposable getmCompositeDisposable() {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        return mCompositeDisposable;
    }

    /**
     * 添加订阅
     *
     * @param disposable
     */
    public void addDisposable(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    public void initToolbarNav() {
        if (mToolbar == null) {
            try {
                initToolbar();
            } catch (Exception e) {
                return;
            }
        }
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(v -> {
            try {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                onBackPressed();
            } finally {
                onBackPressed();
            }
        });
    }

    /**
     * 加载一个资源图标到Toolbar
     *
     * @param resID
     */
    public void initToolbarIcon(int resID) {
        if (mToolbar == null) {
            try {
                initToolbar();
            } catch (Exception e) {
                return;
            }
        }
        mToolbar.setNavigationIcon(resID);
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
    }

    public void setTitle(String title) {
        if (mToolbar == null) {
            initToolbar();
        }
        mToolbar.setTitle(title);
    }

    protected abstract int setLayoutResourceID();

    protected abstract void initVariables(); //上一个界面传过来的数据

    protected abstract void initViews(Bundle savedInstanceState);

    //加载对话框相关
    public KProgressHUD getProgressDialog() {
        if (kProgressHUD != null) {
            kProgressHUD.dismiss();
        } else {
            kProgressHUD = new KProgressHUD(this);
        }
        return kProgressHUD;
    }

    public void showProgressDialog(String message) {
        if (kProgressHUD == null) {
            kProgressHUD = new KProgressHUD(this);
        }
        kProgressHUD.setLabel(message);
        if (!isFinishing() && !kProgressHUD.isShowing()) {
            kProgressHUD.show();
        }
    }

    public void showProgressDialog(String message, boolean cancelable) {
        if (kProgressHUD == null) {
            kProgressHUD = new KProgressHUD(this);
        }
        kProgressHUD.setCancellable(cancelable);
        kProgressHUD.setLabel(message);
        if (!isFinishing() && !kProgressHUD.isShowing()) {
            kProgressHUD.show();
        }
    }

    public void closeProgressDialog() {
        if (kProgressHUD != null && kProgressHUD.isShowing()) {
            kProgressHUD.dismiss();
        }
    }

    /**
     * 显示日志
     * DEBUG 模式下输出  release关闭
     *
     * @param logs
     */
    public void LOG(String tag, String logs) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, logs);
        }
    }
}
