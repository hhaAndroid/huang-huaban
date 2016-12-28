package huang.demo.com.huaban.Widget.MyDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.ButterKnife;
import huang.demo.com.huaban.API.HttpsAPI.OperateAPI;
import huang.demo.com.huaban.Util.Logger;
import huang.demo.com.huaban.Base.BaseDialogFragment;
import huang.demo.com.huaban.Module.ImageDetail.GatherInfoBean;
import huang.demo.com.huaban.Util.Constant;
import huang.demo.com.huaban.API.Dialog.OnGatherDialogInteractionListener;
import huang.demo.com.huaban.HttpUtils.RetrofitClient;
import huang.demo.com.huabandemo.R;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Title: GatherDialogFragment.java
 *
 * @Description: 采集对话框--点击imageDetailFragment里面FAB弹出
 *
 * @Company:南京航空航天大学
 *
 * @author：黄海安
 *
 * @date： 16-7-30 下午9:14.
 */
public class GatherDialogFragment extends BaseDialogFragment {

    private static final String KEYAUTHORIZATION = "keyAuthorization";
    private static final String KEYVIAID = "keyViaId";
    private static final String KEYDESCRIBE = "keyDescribe";
    private static final String KEYBOARDTITLEARRAY = "keyBoardTitleArray";


    EditText mEditTextDescribe;
    TextView mTVGatherWarning;
    Spinner mSpinnerBoardTitle;


    private Context mContext;
    private String mViaId;
    private String mDescribeText;
    private String[] mBoardTitleArray;

    private int mSelectPosition=0;//默认的选中项

    OnGatherDialogInteractionListener mListener;

    @Override
    protected String getTAG() {
        return this.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof OnGatherDialogInteractionListener){
            mListener= (OnGatherDialogInteractionListener) context;
        }else {
            throwRuntimeException(context);
        }

    }

    public static GatherDialogFragment create(String Authorization,String viaId, String describe, String[] boardTitleArray) {
        Bundle bundle = new Bundle();
        bundle.putString(KEYAUTHORIZATION,Authorization);
        bundle.putString(KEYVIAID, viaId);
        bundle.putString(KEYDESCRIBE, describe);
        bundle.putStringArray(KEYBOARDTITLEARRAY, boardTitleArray);
        GatherDialogFragment fragment = new GatherDialogFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mAuthorization=args.getString(KEYAUTHORIZATION);
            mViaId = args.getString(KEYVIAID);
            mDescribeText = args.getString(KEYDESCRIBE);
            mBoardTitleArray = args.getStringArray(KEYBOARDTITLEARRAY);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Logger.d(TAG);
        //AlertDialog是V7里面的包，MD风格
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.dialog_title_gather));
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View dialogView = factory.inflate(R.layout.dialog_gather, null);

        initView(dialogView);

        builder.setView(dialogView);

        builder.setNegativeButton(R.string.dialog_negative,null);
        builder.setPositiveButton(R.string.dialog_gather_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取出输入字符串 没有输入用hint文本作为默认值
                String input=mEditTextDescribe.getText().toString();
                if (TextUtils.isEmpty(input)){
                    input=mEditTextDescribe.getHint().toString();
                }
                //自定义的点击事件
                mListener.onDialogPositiveClick(input,mSelectPosition);
            }
        });

        //访问网络，如果没有登录，该Url会自动跳转至http://api.huaban.com/login/，从而失败
        addSubscription(getGatherInfo());

        return builder.create();
    }


    //可能需要保存数据的回调 一般是按下Home键
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Logger.d();
    }

    //取消的回调
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
//        Logger.d(dialog.toString());
    }

    private void initView(View dialogView) {
        //这种写法也可以，还有更简洁写法
        mEditTextDescribe = ButterKnife.findById(dialogView, R.id.edit_describe);
        mTVGatherWarning = ButterKnife.findById(dialogView, R.id.tv_gather_warning);
        mSpinnerBoardTitle = ButterKnife.findById(dialogView, R.id.spinner_title);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.support_simple_spinner_dropdown_item, mBoardTitleArray);
        if (!TextUtils.isEmpty(mDescribeText)) {
            mEditTextDescribe.setHint(mDescribeText);
        }else {
            mEditTextDescribe.setHint(R.string.text_image_describe_null);
        }
        //Spinner下拉列表控件
        mSpinnerBoardTitle.setAdapter(adapter);
        mSpinnerBoardTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.d("position="+position);
                mSelectPosition=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }



    //以下的方法几乎没有用
    public Subscription getGatherInfo() {
        //用于判断该图片是否已经采集了，如果采集了服务器返回的数据不一样
        //没有登录的话，会跳转至登录api，服务器设定了
        return RetrofitClient.createService(OperateAPI.class)
                .httpsGatherInfo(mAuthorization, mViaId, Constant.OPERATECHECK)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GatherInfoBean>() {
                    @Override
                    public void onCompleted() {
//                        Logger.d();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d(e.toString());
                    }

                    @Override
                    public void onNext(GatherInfoBean gatherInfoBean) {
                        //在没有登录情况下，gatherInfoBean.getExist_pin()=null
                        Logger.d("this pin exist =" + (gatherInfoBean.getExist_pin() != null));
                        if (gatherInfoBean.getExist_pin()!=null){
                            String formatWarning = getResources().getString(R.string.text_gather_warning);
                            mTVGatherWarning.setVisibility(View.VISIBLE);
                            mTVGatherWarning.setText(String.format(formatWarning, gatherInfoBean.getExist_pin().getBoard().getTitle()));
                        }
                    }
                });
    }
}
