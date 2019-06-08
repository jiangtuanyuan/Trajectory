package org.xiaobai.trajectory.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.xiaobai.trajectory.R;
import org.xiaobai.trajectory.db.LocationInfo;

import java.util.List;

public class LatLngLiseAdapter extends BaseQuickAdapter<LocationInfo, BaseViewHolder> {
    public LatLngLiseAdapter(List<LocationInfo> data) {
        super(R.layout.latlng_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LocationInfo item) {
        helper.setText(R.id.tv_time, "时间:" + item.getTime())
                .setText(R.id.tv_latlng, "经纬度:" + item.getLat() + "," + item.getLng())
                .setText(R.id.tv_address, "详情:" + item.getAddress());
    }
}
