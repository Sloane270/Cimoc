package com.hiroshi.cimoc.ui.view;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public interface SourceDetailView extends BaseView, DialogView {

    void onSourceLoadSuccess(int type, String title, long count, String server);

}