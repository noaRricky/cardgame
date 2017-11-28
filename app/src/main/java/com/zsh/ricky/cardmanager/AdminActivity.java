package com.zsh.ricky.cardmanager;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.fragment.CardFragment;
import com.zsh.ricky.cardmanager.fragment.HistoryFragment;
import com.zsh.ricky.cardmanager.util.ModelUri;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.UrlResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AdminActivity extends FragmentActivity {

    private String userID;

    private Fragment cardFragment;
    private Fragment historyFragment;

    private ImageButton addImgBt, backImgBt;
    private TabLayout adminTab;

    private ViewPager adminPager;
    private LayoutInflater mInflater;
    private List<Fragment> fragments = new ArrayList<>();
    private List<String> tabs = new ArrayList<>();

    private static final String TAG = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
        initFragment();
    }

    private void initFragment(){
        tabs.add("卡牌管理");
        tabs.add("历史管理");
        fragments.add(new CardFragment(this));
        fragments.add(new HistoryFragment(this));
        adminPager.setAdapter(new TabAdapter(getSupportFragmentManager()));
        adminTab.setupWithViewPager(adminPager);
    }

    private void initView() {
        Intent intent = getIntent();
        userID = intent.getStringExtra(ModelUri.USER_ID);

        addImgBt = (ImageButton) this.findViewById(R.id.admin_addBt);
        backImgBt = (ImageButton) this.findViewById(R.id.admin_backImageButton);
        adminTab = (TabLayout) this.findViewById(R.id.admin_tabs);

        adminPager=(ViewPager)findViewById(R.id.admin_viewPager);

        addImgBt.setOnClickListener(new AddMenuClick());

        backImgBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> map = new HashMap<>();
                map.put(ModelUri.ACTION, ModelUri.EXIT);
                map.put(ModelUri.USER_ID, userID);

                OkHttpHelper helper = new OkHttpHelper();
                Call call = helper.postRequest(UrlResources.LOGIN, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplication(), "无法连接服务器",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplication(), "退出",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private class AddMenuClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(AdminActivity.this, addImgBt);
            popup.getMenuInflater().inflate(R.menu.toolbar_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_addCard:
                            Log.i(TAG, "onMenuItemClick: addCard");
                            Intent intent = new Intent(AdminActivity.this,
                                    AddCardActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case R.id.menu_addHistory:
                            Log.i(TAG, "onMenuItemClick: addHistory");
                            Intent intent2 = new Intent(AdminActivity.this,
                                    AddHistoryActivity.class);
                            startActivity(intent2);
                            finish();
                            break;
                    }

                    return true;
                }
            });

            popup.show();
        }
    }

    private class TabAdapter extends FragmentPagerAdapter {

        private TabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
        //显示标签上的文字
        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position);
        }
    }
}

