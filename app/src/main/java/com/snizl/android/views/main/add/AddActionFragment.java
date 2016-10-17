package com.snizl.android.views.main.add;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.views.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddActionFragment extends BaseFragment {
    private final int INSTORE_ACTION_TYPE = 0;
    private final int WEBSITE_ACTION_TYPE = 1;
    private final int PHONE_ACTION_TYPE   = 2;
    private final int CUSTOM_ACTION_TYPE  = 3;

    @BindView(R.id.tv_page_title)       TextView tvTitle;
    @BindView(R.id.tv_page_description) TextView tvDescription;
    @BindView(R.id.btn_instore)         Button btnInstore;
    @BindView(R.id.btn_website)         Button btnWebsite;
    @BindView(R.id.btn_phone)           Button btnPhone;
    @BindView(R.id.btn_custom)          Button btnCustom;

    @OnClick(R.id.btn_instore)
    public void onInstore() {
        action_type = INSTORE_ACTION_TYPE;
        showActionDialog();
    }

    @OnClick(R.id.btn_website)
    public void onWebsite() {
        action_type = WEBSITE_ACTION_TYPE;
        showActionDialog();
    }

    @OnClick(R.id.btn_phone)
    public void onPhone() {
        action_type = PHONE_ACTION_TYPE;
        showActionDialog();
    }

    @OnClick(R.id.btn_custom)
    public void onCustom() {
        action_type = CUSTOM_ACTION_TYPE;
        showActionDialog();
    }


    private Context mContext;
    private int action_type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_action, container, false);
        ButterKnife.bind(this, view);

        initData();
        initUI();

        return view;
    }

    private void initData() {
        mContext = getContext();
    }

    private void initUI() {
        String pageTitle =  getString(AppController.add_type == 0 ? R.string.add_action_title_deal : R.string.add_action_title_promotion);
        tvTitle.setText(pageTitle);

        String pageDescription =  getString(AppController.add_type == 0 ? R.string.add_action_description_deal : R.string.add_action_description_promotion);
        tvDescription.setText(pageDescription);

        changeButtonColor();
    }

    private void showActionDialog() {
        final Dialog dialog = new Dialog(mContext, R.style.AppTheme_Dialog);
        dialog.setContentView(R.layout.dialog_enter_action);
        dialog.setCancelable(false);

        EditText etInstore = (EditText)dialog.findViewById(R.id.et_instore);
        EditText etWebsite = (EditText)dialog.findViewById(R.id.et_website);
        EditText etPhone   = (EditText)dialog.findViewById(R.id.et_phone);
        EditText etCustom  = (EditText)dialog.findViewById(R.id.et_custom);
        int shownViewId = 0;
        switch (action_type) {
            case INSTORE_ACTION_TYPE:
                etInstore.setVisibility(View.VISIBLE);
                etWebsite.setVisibility(View.GONE);
                etPhone.setVisibility(View.GONE);
                etCustom.setVisibility(View.GONE);
                etInstore.setText("");
                etInstore.append(AppController.strInstore);
                shownViewId = etInstore.getId();
                break;
            case WEBSITE_ACTION_TYPE:
                etInstore.setVisibility(View.GONE);
                etWebsite.setVisibility(View.VISIBLE);
                etPhone.setVisibility(View.GONE);
                etCustom.setVisibility(View.GONE);
                etWebsite.setText("");
                etWebsite.append(AppController.strWebsite);
                shownViewId = etWebsite.getId();
                break;
            case PHONE_ACTION_TYPE:
                etInstore.setVisibility(View.GONE);
                etWebsite.setVisibility(View.GONE);
                etPhone.setVisibility(View.VISIBLE);
                etCustom.setVisibility(View.GONE);
                etPhone.setText("");
                etPhone.append(AppController.strPhone);
                shownViewId = etPhone.getId();
                break;
            case CUSTOM_ACTION_TYPE:
                etInstore.setVisibility(View.GONE);
                etWebsite.setVisibility(View.GONE);
                etPhone.setVisibility(View.GONE);
                etCustom.setVisibility(View.VISIBLE);
                etCustom.setText("");
                etCustom.append(AppController.strCustom);
                shownViewId = etCustom.getId();
                break;
        }

        Button btnDismiss  = (Button) dialog.findViewById(R.id.btn_dismiss);
        Button btnConfirm  = (Button) dialog.findViewById(R.id.btn_confirm);

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (action_type) {
                    case INSTORE_ACTION_TYPE:
                        AppController.strInstore = "";
                        break;
                    case WEBSITE_ACTION_TYPE:
                        AppController.strWebsite = "";
                        break;
                    case PHONE_ACTION_TYPE:
                        AppController.strPhone = "";
                        break;
                    case CUSTOM_ACTION_TYPE:
                        AppController.strCustom = "";
                        break;
                }
                changeButtonColor();
                dialog.dismiss();
            }
        });

        final int finalId = shownViewId;
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText shownView = (EditText)dialog.findViewById(finalId);
                String strAction = shownView.getText().toString().trim();
                switch (action_type) {
                    case INSTORE_ACTION_TYPE:
                        AppController.strInstore = strAction;
                        break;
                    case WEBSITE_ACTION_TYPE:
                        AppController.strWebsite = strAction;
                        break;
                    case PHONE_ACTION_TYPE:
                        AppController.strPhone = strAction;
                        break;
                    case CUSTOM_ACTION_TYPE:
                        AppController.strCustom = strAction;
                        break;
                }
                changeButtonColor();
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

    private void changeButtonColor() {
        commonUtils.changeActionButtonStateColor(btnInstore, !AppController.strInstore.isEmpty(), mContext);
        commonUtils.changeActionButtonStateColor(btnWebsite, !AppController.strWebsite.isEmpty(), mContext);
        commonUtils.changeActionButtonStateColor(btnPhone,   !AppController.strPhone  .isEmpty(), mContext);
        commonUtils.changeActionButtonStateColor(btnCustom,  !AppController.strCustom .isEmpty(), mContext);
    }
}