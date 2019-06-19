package de.tum.in.tumcampusapp.component.ui.barrierfree;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TumCabeClient;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.ui.barrierfree.model.BarrierfreeMoreInfo;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BarrierFreeMoreInfoActivity
        extends ActivityForLoadingInBackground<Void, List<BarrierfreeMoreInfo>>
        implements AdapterView.OnItemClickListener {

    public StickyListHeadersListView listview;
    public List<BarrierfreeMoreInfo> infos;
    public BarrierfreeMoreInfoAdapter adapter;

    public BarrierFreeMoreInfoActivity() {
        super(R.layout.activity_barrier_free_list_info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listview = findViewById(R.id.activity_barrier_info_list_view);

        startLoading();
    }

    @Override
    protected void onLoadFinished(List<BarrierfreeMoreInfo> result) {
        showLoadingEnded();
        if (result == null || result.isEmpty()) {
            showErrorLayout();
            return;
        }

        infos = result;
        adapter = new BarrierfreeMoreInfoAdapter(this, infos);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
    }

    @Override
    protected List<BarrierfreeMoreInfo> onLoadInBackground(Void... arg) {
        showLoadingStart();
        return TumCabeClient.getInstance(this).getMoreInfoList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = infos.get(position)
                          .getUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);

    }
}
