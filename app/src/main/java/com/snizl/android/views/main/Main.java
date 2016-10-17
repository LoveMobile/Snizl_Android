package com.snizl.android.views.main;

import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.snizl.android.AppController;
import com.snizl.android.R;
import com.snizl.android.api.GetDataTask;
import com.snizl.android.api.OnTaskCompleted;
import com.snizl.android.views.base.BaseActivity;
import com.snizl.android.views.main.add.Add;
import com.snizl.android.views.user.Login;
import com.snizl.android.views.user.MyWallet;
import com.snizl.android.views.user.Setting;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class Main extends BaseActivity implements OnTaskCompleted {
    @BindView(R.id.drawer_layout)   DrawerLayout mDrawerLayout;
    @BindView(R.id.main_content)    LinearLayout mMainContent;
    @BindView(R.id.toolbar)         Toolbar      mToolbar;
    @BindView(R.id.content_frame)   FrameLayout  mContentFrame;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;
    @BindView(R.id.floatActionMenu) FloatingActionsMenu mFloatingActionsMenu;

    @OnClick(R.id.btn_scan_qr)
    public void onScanQR() {
        Log.d(TAG, "ScanQR");
        mFloatingActionsMenu.collapse();
    }

    @OnClick(R.id.btn_deal)
    public void onDeal() {
        AppController.add_type = 0;
        navigationWithoutFinish(this, Add.class);
        mFloatingActionsMenu.collapse();
    }

    @OnClick(R.id.btn_promo)
    public void onPromo() {
        AppController.add_type = 1;
        navigationWithoutFinish(this, Add.class);
        mFloatingActionsMenu.collapse();
    }

//    ***********************************************************************

    private int selectedFragmentIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {
        setToolbar();
        setLeftMenu();
        setFAB();

        performMenuAction(0);
    }

    private void performMenuAction(int position) {
        if (position == selectedFragmentIndex) {
            mDrawerLayout.closeDrawer(mNavigationView);
            return;
        }
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new OfferFragment();
                break;
            case 1:
                fragment = new EventFragment();
                break;
            case 2:
                showAdjustRangeDialog();
                break;
            case 3:
                navigationWithoutFinish(this, MyWallet.class);
                mDrawerLayout.closeDrawer(mNavigationView);
                break;
            case 4:
                commonUtils.logOut(this);
                navigationWithFinish(this, Login.class);
                break;
        }

        if (fragment != null) {
            mDrawerLayout.closeDrawer(mNavigationView);
            selectedFragmentIndex = position;
            getSupportFragmentManager().beginTransaction().replace(mContentFrame.getId(), fragment).commit();
        }
    }

    public void showAdjustRangeDialog() {
        final Dialog dialog = new Dialog(this, R.style.AppTheme_Dialog);
        dialog.setContentView(R.layout.dialog_adjust_range);
        dialog.setCancelable(false);
        Button btnDismiss = (Button) dialog.findViewById(R.id.btn_dismiss);
        Button btnSave    = (Button) dialog.findViewById(R.id.btn_save);
        final DiscreteSeekBar rangeSeekBar = (DiscreteSeekBar)dialog.findViewById(R.id.seekBar_range);
        rangeSeekBar.setProgress(getUserRange());

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRange(rangeSeekBar.getProgress());
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

    private void setToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerOpen(mNavigationView))
                    mDrawerLayout.closeDrawer(mNavigationView);
                else
                    mDrawerLayout.openDrawer(mNavigationView);
            }
        });

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.str_navigation_drawer_open, R.string.str_navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void setLeftMenu() {
        View headerLayout = mNavigationView.getHeaderView(0);
        CircleImageView ivAvatar = (CircleImageView)headerLayout.findViewById(R.id.iv_avatar);
        TextView tvName = (TextView)headerLayout.findViewById(R.id.tv_name);
        Button btnMySettings = (Button)headerLayout.findViewById(R.id.btn_settings);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);

                switch (menuItem.getItemId()){
                    case R.id.drawer_deals:
                        performMenuAction(0);
                        break;
                    case R.id.drawer_events:
                        performMenuAction(1);
                        break;
                    case R.id.drawer_range:
                        performMenuAction(2);
                        break;
                    case R.id.drawer_my_wallet:
                        performMenuAction(3);
                        break;
                    case R.id.drawer_log_out:
                        performMenuAction(4);
                        break;
                }

                return true;
            }
        });

        JSONObject userPhoto = commonUtils.getJSONObjectFromJSONObject(AppController.currentUser, PICTURE);

        if (userPhoto == null) {
            ivAvatar.setImageResource(R.drawable.img_no_avatar);
        } else {
            String user_photo_url = commonUtils.getStringFromJSONObject(userPhoto, KEY_URL);
            Log.d(TAG, "User Photo Url : " + user_photo_url);

            Glide.with(this)
                    .load(user_photo_url)
                    .placeholder(R.drawable.img_no_avatar)
                    .error(R.drawable.img_no_avatar)
                    .centerCrop()
                    .into(ivAvatar);
        }

        String user_full_name = commonUtils.getStringFromJSONObject(AppController.currentUser, FIRST_NAME) + " " + commonUtils.getStringFromJSONObject(AppController.currentUser, LAST_NAME);
        tvName.setText(user_full_name);

        btnMySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(mNavigationView);
                navigationWithoutFinish(Main.this, Setting.class);
            }
        });
    }

    private void setFAB() {
        if (AppController.businesses.length() == 0) {
            mFloatingActionsMenu.setVisibility(View.INVISIBLE);
        }
    }

    private void postRange(int range) {
        String access_token, token_type;
        access_token = commonUtils.getStringFromJSONObject(AppController.oauth_token, ACCESS_TOKEN);
        token_type   = commonUtils.getStringFromJSONObject(AppController.oauth_token, TOKEN_TYPE);

        ContentValues headers = new ContentValues();
        headers.put(HEADER_AUTHORIZATION, token_type + " " + access_token);

        ContentValues body = new ContentValues();
        body.put(BODY_RANGE, range);

        isLoadingBase = true;

        String url = USERS_URL + "/" + getUserId();

        new GetDataTask(url, headers, body, POST_METHOD, this).execute();
    }

    @Override
    public void onDataReceived(JSONObject response) {
        isLoadingBase = false;

        if (response == null) {
            commonUtils.showAlertDialog(this, "Please check your internet connection status");
            return;
        }

        JSONObject currentUser = commonUtils.getJSONObjectFromJSONObject(response, USER);
        AppController.currentUser = currentUser;
        commonUtils.setJSONObjectToSharedPreference(this, USER, currentUser);
    }
}
