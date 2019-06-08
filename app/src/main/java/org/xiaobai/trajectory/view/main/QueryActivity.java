package org.xiaobai.trajectory.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.litepal.LitePal;
import org.xiaobai.trajectory.R;
import org.xiaobai.trajectory.adapter.LatLngLiseAdapter;
import org.xiaobai.trajectory.db.LocationInfo;
import org.xiaobai.trajectory.view.base.BaseActivity;
import org.xiaobai.utils.EditTextChangedListenerUtlis;
import org.xiaobai.utils.PickerViewUtils;
import org.xiaobai.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 轨迹查询
 */
public class QueryActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_time_more)
    TextView tvTimeMore;
    @BindView(R.id.time_more_clear)
    ImageView timeMoreClear;
    @BindView(R.id.tv_time_less)
    TextView tvTimeLess;
    @BindView(R.id.time_less_clear)
    ImageView timeLessClear;
    @BindView(R.id.bt_query)
    Button btQuery;
    @BindView(R.id.tv_latlng_number)
    TextView tvLatlngNumber;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.bt_line)
    Button btLine;

    //列表
    private LatLngLiseAdapter mLatLngLiseAdapter;
    private List<LocationInfo> mSumList = new ArrayList<>();//总数据

    @Override
    protected int setLayoutResourceID() {
        return R.layout.activity_query;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        initToolbarNav();
        setTitle("轨迹查询");
        EditTextChangedListenerUtlis.EditTextChangedListener(tvTimeMore, timeMoreClear);
        EditTextChangedListenerUtlis.EditTextChangedListener(tvTimeLess, timeLessClear);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLatLngLiseAdapter = new LatLngLiseAdapter(mSumList);
        recyclerView.setAdapter(mLatLngLiseAdapter);

    }

    @OnClick({R.id.tv_time_more, R.id.tv_time_less, R.id.bt_query, R.id.bt_line})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_time_more://开始时间
                PickerViewUtils.showDatePicker(this, tvTimeMore);
                break;
            case R.id.tv_time_less://结束时间
                PickerViewUtils.showDatePicker(this, tvTimeLess);
                break;
            case R.id.bt_query://查询
                QuerLatLngs();
                break;
            case R.id.bt_line://生产轨迹
                if (mSumList.size() == 0) {
                    ToastUtil.showToast("没有轨迹!");
                    return;
                }
                StringBuffer stringBuffer = new StringBuffer();
                for (LocationInfo loca : mSumList) {
                    stringBuffer.append(loca.getLng());
                    stringBuffer.append(",");
                    stringBuffer.append(loca.getLat());
                    stringBuffer.append(";");
                }
                Intent intent = new Intent(this, MapTrackActivity.class);
                intent.putExtra("name", "我");
                intent.putExtra("latlngs", stringBuffer.toString());
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 根据时间查询保存的位置信息
     */
    private void QuerLatLngs() {
        mSumList.clear();
        String str1 = tvTimeMore.getText().toString();
        String str2 = tvTimeLess.getText().toString();
        if (!TextUtils.isEmpty(str1) && !TextUtils.isEmpty(str2)) {
            mSumList.addAll(LitePal
                    .where("time >=? and time <=?", str1, str2)
                    .order("id desc")
                    .find(LocationInfo.class));
        } else if (!TextUtils.isEmpty(str1) && TextUtils.isEmpty(str2)) {
            mSumList.addAll(LitePal
                    .where("time >=?", str1)
                    .order("id desc")
                    .find(LocationInfo.class));
        } else if (TextUtils.isEmpty(str1) && !TextUtils.isEmpty(str2)) {
            mSumList.addAll(LitePal
                    .where("time <=?", str2)
                    .order("id desc")
                    .find(LocationInfo.class));
        } else {
            mSumList.addAll(LitePal
                    .order("id desc")
                    .find(LocationInfo.class));
        }
        tvLatlngNumber.setText("查询结果(" + mSumList.size() + ")条");
        mLatLngLiseAdapter.notifyDataSetChanged();
    }
}
