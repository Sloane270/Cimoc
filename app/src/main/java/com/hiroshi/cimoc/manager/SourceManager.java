package com.hiroshi.cimoc.manager;

import android.util.SparseArray;

import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.parser.Parser;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;
import com.hiroshi.cimoc.model.SourceDao.Properties;
import com.hiroshi.cimoc.source.CCTuku;
import com.hiroshi.cimoc.source.Chuiyao;
import com.hiroshi.cimoc.source.DM5;
import com.hiroshi.cimoc.source.Dmzj;
import com.hiroshi.cimoc.source.HHAAZZ;
import com.hiroshi.cimoc.source.HHSSEE;
import com.hiroshi.cimoc.source.IKanman;
import com.hiroshi.cimoc.source.MH57;
import com.hiroshi.cimoc.source.U17;
import com.hiroshi.cimoc.source.Webtoon;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceManager {

    public static final int SOURCE_IKANMAN = 0;
    public static final int SOURCE_DMZJ = 1;
    public static final int SOURCE_HHAAZZ = 2;
    public static final int SOURCE_CCTUKU = 3;
    public static final int SOURCE_U17 = 4;
    public static final int SOURCE_DM5 = 5;
    public static final int SOURCE_WEBTOON = 6;
    public static final int SOURCE_HHSSEE = 7;
    public static final int SOURCE_57MH = 8;
    public static final int SOURCE_CHUIYAO = 9;

    private static SourceManager mInstance;

    private SourceDao mSourceDao;
    private SparseArray<Parser> mParserArray = new SparseArray<>();

    private SourceManager(AppGetter getter) {
        mSourceDao = getter.getAppInstance().getDaoSession().getSourceDao();
    }

    public Observable<List<Source>> list() {
        return mSourceDao.queryBuilder()
                .orderAsc(Properties.Type)
                .rx()
                .list();
    }

    public Observable<List<Source>> listEnableInRx() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Type)
                .rx()
                .list();
    }

    public List<Source> listEnable() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Type)
                .list();
    }

    public Source load(int type) {
        return mSourceDao.queryBuilder()
                .where(Properties.Type.eq(type))
                .unique();
    }

    public long insert(Source source) {
        return mSourceDao.insert(source);
    }

    public void update(Source source) {
        mSourceDao.update(source);
    }

    public static String getTitle(int id) {
        switch (id) {
            case SOURCE_IKANMAN:
                return "看漫画";
            case SOURCE_DMZJ:
                return "动漫之家";
            case SOURCE_HHAAZZ:
                return "手机汗汗";
            case SOURCE_CCTUKU:
                return "CC图库";
            case SOURCE_U17:
                return "有妖气";
            case SOURCE_DM5:
                return "动漫屋";
            case SOURCE_WEBTOON:
                return "Webtoon";
            case SOURCE_HHSSEE:
                return "汗汗漫画";
            case SOURCE_57MH:
                return "57漫画";
            case SOURCE_CHUIYAO:
                return "吹妖漫画";
        }
        return "null";
    }

    public Parser getParser(int type) {
        Parser parser = mParserArray.get(type);
        if (parser == null) {
            Source source;
            switch (type) {
                case SOURCE_IKANMAN:
                    source = load(type);
                    parser = new IKanman(source.getServer());
                    break;
                case SOURCE_DMZJ:
                    parser = new Dmzj();
                    break;
                case SOURCE_HHAAZZ:
                    source = load(type);
                    parser = new HHAAZZ(source.getServer());
                    break;
                case SOURCE_CCTUKU:
                    parser = new CCTuku();
                    break;
                case SOURCE_U17:
                    parser = new U17();
                    break;
                case SOURCE_DM5:
                    parser = new DM5();
                    break;
                case SOURCE_WEBTOON:
                    parser = new Webtoon();
                    break;
                case SOURCE_HHSSEE:
                    parser = new HHSSEE();
                    break;
                case SOURCE_57MH:
                    source = load(type);
                    parser = new MH57(source.getServer());
                    break;
                case SOURCE_CHUIYAO:
                    parser = new Chuiyao();
                    break;
            }
            mParserArray.put(type, parser);
        }
        return parser;
    }

    public void resetParse(Source source) {
        Parser parser = mParserArray.get(source.getType());
        switch (source.getType()) {
            case SOURCE_IKANMAN:
                parser = new IKanman(source.getServer());
                break;
            case SOURCE_HHAAZZ:
                parser = new HHAAZZ(source.getServer());
                break;
            case SOURCE_57MH:
                parser = new MH57(source.getServer());
                break;
        }
        mParserArray.put(source.getType(), parser);
    }

    public static SourceManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            mInstance = new SourceManager(getter);
        }
        return mInstance;
    }

}