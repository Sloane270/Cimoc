package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.view.View;
import android.widget.TextView;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.activity.settings.ReaderConfigActivity;
import com.hiroshi.cimoc.ui.custom.preference.CheckBoxPreference;
import com.hiroshi.cimoc.ui.custom.preference.ChoicePreference;
import com.hiroshi.cimoc.ui.custom.preference.SliderPreference;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.StorageEditorDialogFragment;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.ServiceUtils;
import com.hiroshi.cimoc.utils.StringUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView {

    private static final int DIALOG_REQUEST_OTHER_LAUNCH = 0;
    private static final int DIALOG_REQUEST_READER_MODE = 1;
    private static final int DIALOG_REQUEST_OTHER_THEME = 2;
    private static final int DIALOG_REQUEST_DOWNLOAD_CONN = 3;
    private static final int DIALOG_REQUEST_OTHER_STORAGE = 4;
    private static final int DIALOG_REQUEST_DOWNLOAD_THREAD = 5;
    private static final int DIALOG_REQUEST_DOWNLOAD_DELETE = 6;
    private static final int DIALOG_REQUEST_DOWNLOAD_SCAN = 7;
    private static final int DIALOG_REQUEST_OTHER_NIGHT_ALPHA = 8;

    @BindViews({R.id.settings_reader_title, R.id.settings_download_title, R.id.settings_other_title, R.id.settings_search_title})
    List<TextView> mTitleList;
    @BindView(R.id.settings_layout) View mSettingsLayout;
    @BindView(R.id.settings_reader_keep_bright) CheckBoxPreference mReaderKeepBright;
    @BindView(R.id.settings_reader_hide_info) CheckBoxPreference mReaderHideInfo;
    @BindView(R.id.settings_reader_disable_popup) CheckBoxPreference mReaderDisablePopup;
    @BindView(R.id.settings_reader_hide_nav) CheckBoxPreference mReaderHideNav;
    @BindView(R.id.settings_search_auto_complete) CheckBoxPreference mSearchAutoComplete;
    @BindView(R.id.settings_reader_mode) ChoicePreference mReaderMode;
    @BindView(R.id.settings_other_launch) ChoicePreference mOtherLaunch;
    @BindView(R.id.settings_other_theme) ChoicePreference mOtherTheme;
    @BindView(R.id.settings_other_night_alpha) SliderPreference mOtherNightAlpha;
    @BindView(R.id.settings_download_connection) SliderPreference mDownloadConnection;
    @BindView(R.id.settings_download_thread) SliderPreference mDownloadThread;

    private SettingsPresenter mPresenter;

    private String mStoragePath;
    private String mTempStorage;

    private int[] mResultArray = new int[6];
    private Intent mResultIntent = new Intent();

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SettingsPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mStoragePath = getAppInstance().getDocumentFile().getUri().toString();
        mReaderKeepBright.bindPreference(PreferenceManager.PREF_READER_KEEP_BRIGHT, false);
        mReaderHideInfo.bindPreference(PreferenceManager.PREF_READER_HIDE_INFO, false);
        mReaderDisablePopup.bindPreference(PreferenceManager.PREF_READER_DISABLE_POPUP, false);
        mReaderHideNav.bindPreference(PreferenceManager.PREF_READER_HIDE_NAV, false);
        mSearchAutoComplete.bindPreference(PreferenceManager.PREF_SEARCH_AUTO_COMPLETE, false);
        mReaderMode.bindPreference(getFragmentManager(), PreferenceManager.PREF_READER_MODE,
                PreferenceManager.READER_MODE_PAGE, R.array.reader_mode_items, DIALOG_REQUEST_READER_MODE);
        mOtherLaunch.bindPreference(getFragmentManager(), PreferenceManager.PREF_OTHER_LAUNCH,
                PreferenceManager.HOME_COMIC, R.array.launch_items, DIALOG_REQUEST_OTHER_LAUNCH);
        mOtherTheme.bindPreference(getFragmentManager(), PreferenceManager.PREF_OTHER_THEME,
                ThemeUtils.THEME_BLUE, R.array.theme_items, DIALOG_REQUEST_OTHER_THEME);
        mOtherNightAlpha.bindPreference(getFragmentManager(), PreferenceManager.PREF_OTHER_NIGHT_ALPHA, 0xB0,
                R.string.settings_other_night_alpha, DIALOG_REQUEST_OTHER_NIGHT_ALPHA);
        mDownloadConnection.bindPreference(getFragmentManager(), PreferenceManager.PREF_DOWNLOAD_CONNECTION, 0,
                R.string.settings_download_connection, DIALOG_REQUEST_DOWNLOAD_CONN);
        mDownloadThread.bindPreference(getFragmentManager(), PreferenceManager.PREF_DOWNLOAD_THREAD, 1,
                R.string.settings_download_thread, DIALOG_REQUEST_DOWNLOAD_THREAD);
    }

    @OnClick(R.id.settings_reader_config) void onReaderConfigBtnClick() {
        Intent intent = new Intent(this, ReaderConfigActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case DIALOG_REQUEST_OTHER_STORAGE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        showProgressDialog();
                        Uri uri = data.getData();
                        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, flags);
                        mTempStorage = uri.toString();
                        mPresenter.moveFiles(DocumentFile.fromTreeUri(this, uri));
                    } else {
                        showProgressDialog();
                        String path = data.getStringExtra(Extra.EXTRA_PICKER_PATH);
                        if (!StringUtils.isEmpty(path)) {
                            DocumentFile file = DocumentFile.fromFile(new File(path));
                            mTempStorage = file.getUri().toString();
                            mPresenter.moveFiles(file);
                        } else {
                            onExecuteFail();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_READER_MODE:
                mReaderMode.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_OTHER_LAUNCH:
                mOtherLaunch.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_OTHER_THEME:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                if (mOtherTheme.getValue() != index) {
                    mOtherTheme.setValue(index);
                    int theme = ThemeUtils.getThemeById(index);
                    setTheme(theme);
                    int primary = ThemeUtils.getResourceId(this, R.attr.colorPrimary);
                    int accent = ThemeUtils.getResourceId(this, R.attr.colorAccent);
                    changeTheme(primary, accent);
                    mResultArray[0] = 1;
                    mResultArray[1] = theme;
                    mResultArray[2] = primary;
                    mResultArray[3] = accent;
                    mResultIntent.putExtra(Extra.EXTRA_RESULT, mResultArray);
                    setResult(Activity.RESULT_OK, mResultIntent);
                }
                break;
            case DIALOG_REQUEST_DOWNLOAD_CONN:
                mDownloadConnection.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_OTHER_STORAGE:
                showSnackbar(R.string.settings_other_storage_not_found);
                break;
            case DIALOG_REQUEST_DOWNLOAD_THREAD:
                mDownloadThread.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_VALUE));
                break;
            case DIALOG_REQUEST_DOWNLOAD_SCAN:
                showProgressDialog();
                mPresenter.scanTask();
                break;
            case DIALOG_REQUEST_DOWNLOAD_DELETE:
                showProgressDialog();
                mPresenter.deleteTask();
                break;
            case DIALOG_REQUEST_OTHER_NIGHT_ALPHA:
                int alpha = bundle.getInt(EXTRA_DIALOG_RESULT_VALUE);
                mOtherNightAlpha.setValue(alpha);
                if (mNightMask != null) {
                    mNightMask.setBackgroundColor(alpha << 24);
                }
                mResultArray[4] = 1;
                mResultArray[5] = alpha;
                mResultIntent.putExtra(Extra.EXTRA_RESULT, mResultArray);
                setResult(Activity.RESULT_OK, mResultIntent);
                break;
        }
    }

    private void changeTheme(int primary, int accent) {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, primary));
        }
        for (TextView textView : mTitleList) {
            textView.setTextColor(ContextCompat.getColor(this, primary));
        }
        ColorStateList stateList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked }, { android.R.attr.state_checked }},
                new int[]{0x8A000000, ContextCompat.getColor(this, accent)});
        mReaderKeepBright.setColorStateList(stateList);
        mReaderHideInfo.setColorStateList(stateList);
        mReaderDisablePopup.setColorStateList(stateList);
        mReaderHideNav.setColorStateList(stateList);
        mSearchAutoComplete.setColorStateList(stateList);
    }

    @OnClick(R.id.settings_other_storage) void onOtherStorageClick() {
        if (ServiceUtils.isServiceRunning(this, DownloadService.class)) {
            showSnackbar(R.string.download_ask_stop);
        } else {
            StorageEditorDialogFragment fragment = StorageEditorDialogFragment.newInstance(R.string.settings_other_storage,
                    mStoragePath, DIALOG_REQUEST_OTHER_STORAGE);
            fragment.show(getFragmentManager(), null);
        }
    }

    @OnClick(R.id.settings_download_scan) void onDownloadScanClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.settings_download_scan_confirm, true, DIALOG_REQUEST_DOWNLOAD_SCAN);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_download_delete) void onDownloadDeleteClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.settings_download_delete_confirm, true, DIALOG_REQUEST_DOWNLOAD_DELETE);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_other_clear_cache) void onOtherCacheClick() {
        showProgressDialog();
        mPresenter.clearCache();
        showSnackbar(R.string.common_execute_success);
        hideProgressDialog();
    }

    @Override
    public void onFileMoveSuccess() {
        hideProgressDialog();
        mPreference.putString(PreferenceManager.PREF_OTHER_STORAGE, mTempStorage);
        mStoragePath = mTempStorage;
        ((App) getApplication()).initRootDocumentFile();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onExecuteSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onExecuteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_settings);
    }

    @Override
    protected View getLayoutView() {
        return mSettingsLayout;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_settings;
    }
    
}
