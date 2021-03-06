package capital.fbg.wallet.ui.wallet.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import capital.fbg.wallet.AppApplication;
import capital.fbg.wallet.R;
import capital.fbg.wallet.base.BaseActivity;
import capital.fbg.wallet.bean.WalletBean;
import capital.fbg.wallet.common.Constant;
import capital.fbg.wallet.common.util.AppManager;
import capital.fbg.wallet.common.util.ToastUtil;
import capital.fbg.wallet.ui.ScanActivity;
import capital.fbg.wallet.event.BaseEventBusBean;
import capital.fbg.wallet.event.KeyEvent;
import me.drakeet.materialdialog.MaterialDialog;
import unichain.ETHWallet;
import unichain.Unichain;

/**
 * Created by Administrator on 2017/7/27.
 * 功能描述：
 * 版本：@version
 */

public class WatchImportWalletActivity extends BaseActivity {
    @BindView(R.id.txt_left_title)
    TextView txtLeftTitle;
    @BindView(R.id.txt_main_title)
    TextView txtMainTitle;
    @BindView(R.id.txt_right_title)
    TextView txtRightTitle;
    @BindView(R.id.hit)
    TextView hit;
    @BindView(R.id.et_info)
    EditText etInfo;
    @BindView(R.id.tv_import)
    TextView tvImport;

    private int type;

    private MaterialDialog mMaterialDialog;

    private WalletBean watchWallet;

    @Override
    protected void getBundleExtras(Bundle extras) {
        type=extras.getInt("type");
        watchWallet= (WalletBean) extras.getSerializable("wallet");
        isOpenEventBus=true;
    }

    @Override
    protected int setLayoutID() {
        return R.layout.wallet_acivity_import_wallet;
    }

    @Override
    protected void initView() {
        txtLeftTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Drawable drawableInfo= getResources().getDrawable(R.mipmap.import_scan);
        /// 这一步必须要做,否则不会显示.
        drawableInfo.setBounds(0, 0, drawableInfo.getMinimumWidth(), drawableInfo.getMinimumHeight());
        txtRightTitle.setCompoundDrawables(drawableInfo,null,null,null);
        txtRightTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mActivity, ScanActivity.class);
                intent.putExtra("type",1);
                keepTogo(intent);
            }
        });

        tvImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etInfo.getText().toString().length() == 0) {
                    ToastUtil.show("填写内容不能为空");
                    return;
                }
                switch (type){
                    case 1:
                        impotKeystore(etInfo.getText().toString());
                        break;
                    case 2:
                        impotAnquanma(etInfo.getText().toString());
                        break;
                    case 3:
                        impotKey(etInfo.getText().toString());
                        break;
//                    case 4:
//                        impotWatch(etInfo.getText().toString());
//                        break;
//                    case 5:
//                        impotSeed(etInfo.getText().toString());
//                        break;
                }
            }
        });
        switch (type){
            case 1:
                txtMainTitle.setText("添加结果");
                hit.setText(R.string.wallet_hit21);
                break;
            case 2:
                txtMainTitle.setText("添加助记词");
                hit.setText(R.string.wallet_hit22);
                break;
            case 3:
                txtMainTitle.setText("添加明文私钥");
                hit.setText(R.string.wallet_hit23);
                break;
//            case 4:
//                txtMainTitle.setText("添加观察钱包");
//                hit.setText(R.string.wallet_hit24);
//                break;
//            case 5:
//                txtMainTitle.setText("添加种子");
//                hit.setText(R.string.wallet_hit25);
//                break;
        }
    }

    private void impotKeystore(final String key) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.view_dialog_pass, null, false);
        final EditText pass= (EditText) view.findViewById(R.id.et_pass);
        TextView cancle= (TextView) view.findViewById(R.id.cancle);
        TextView ok= (TextView) view.findViewById(R.id.ok);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaterialDialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pass.getText().toString().length()==0){
                    ToastUtil.show(getString(R.string.qingshurumima));
                    return;
                }
                importKey(pass.getText().toString());
            }
        });

        mMaterialDialog = new MaterialDialog(mActivity).setView(view);
        mMaterialDialog.setBackgroundResource(R.drawable.trans_bg);
        mMaterialDialog.setCanceledOnTouchOutside(true);
        mMaterialDialog.show();
    }

    private void impotAnquanma(String key) {
        Intent intent=new Intent(mActivity,WatchImportWalletSettingActivity.class);
        intent.putExtra("wallet",watchWallet);
        intent.putExtra("pass","");
        intent.putExtra("key",key);
        intent.putExtra("type",type);
        keepTogo(intent);
    }

    private void impotKey(String key) {
        Intent intent=new Intent(mActivity,WatchImportWalletSettingActivity.class);
        intent.putExtra("wallet",watchWallet);
        intent.putExtra("pass","");
        intent.putExtra("key",key);
        intent.putExtra("type",type);
        keepTogo(intent);
    }

    private void importKey(final String pass){
        showLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    ETHWallet wallet= Unichain.openETHWallet(etInfo.getText().toString().getBytes("utf-8"),pass);

                    //将钱包保存到ACCOUNTMANAGER
                    saveWallet(etInfo.getText().toString().getBytes("utf-8")
                            , wallet.address().toLowerCase()
                            , pass
                            , watchWallet.getName()
                            , Constant.ZHENGCHANG);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            AppManager.getAppManager().finishActivity(WatchImportWalletTypeActivity.class);
                            EventBus.getDefault().postSticky(new BaseEventBusBean(Constant.EVENT_WATCH_TRANSFER));
                            EventBus.getDefault().postSticky(new BaseEventBusBean(Constant.EVENT_WALLET));
                            finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show("请检查钱包密码是否输入正确");
                            hideLoading();
                        }
                    });
                }
            }
        }).start();
    }

    private void saveWallet(byte[] json, String address, String pass,String name,String type) {
        AccountManager accountManager = AccountManager.get(this);
        Account account = new Account(address, "capital.fbg.wallet");
        accountManager.addAccountExplicitly(account, pass, null);
        accountManager.setUserData(account, "wallet", new String(json));
        accountManager.setUserData(account, "name", name);
        accountManager.setUserData(account, "type", type);
        accountManager.setUserData(account, "wallet_type","hot");

        account=null;
        String wallets= AppApplication.get().getSp().getString(Constant.WALLETS,"");
        if (!wallets.contains(address)){
            wallets=wallets+address+",";
            AppApplication.get().getSp().putString(Constant.WALLETS,wallets);
        }
    }

    private void impotSeed(String key) {
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void EventBean(BaseEventBusBean event) {
        if (event.getEventCode()== Constant.EVENT_KEY){
            KeyEvent keyEvent= (KeyEvent) event.getData();
            etInfo.setText(keyEvent.getKey());
        }
    }
}
