package com.dealermanagmentsystem.ui.home;

import android.app.Activity;

public interface IHomePresenter {

    public void getLeadsOverview(Activity activity);

    public void sendFcmToken(Activity activity, String token);

}