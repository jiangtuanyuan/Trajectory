package org.xiaobai.trajectory.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.xiaobai.trajectory.R;
import org.xiaobai.trajectory.db.LocationInfo;
import org.xiaobai.utils.ToastUtil;

import java.util.List;

public class RecipeLiseAdapter extends BaseQuickAdapter<LocationInfo, BaseViewHolder> {
    public RecipeLiseAdapter(List<LocationInfo> data) {
        super(R.layout.latlng_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LocationInfo item) {

        helper.setText(R.id.tv_time, "时间:" + item.getTime())
                .setText(R.id.tv_latlng, "经纬度:" + item.getLat() + "," + item.getLng())
                .setText(R.id.tv_address, "详情:" + item.getAddress());
        helper.itemView.setOnClickListener(v -> ToastUtil.showToast("点" + helper.getAdapterPosition()));
    }
}
