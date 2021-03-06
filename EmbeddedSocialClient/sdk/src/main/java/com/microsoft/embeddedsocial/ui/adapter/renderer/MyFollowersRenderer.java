/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.embeddedsocial.ui.adapter.renderer;

import com.microsoft.embeddedsocial.account.UserAccount;
import com.microsoft.embeddedsocial.autorest.models.FollowerStatus;
import com.microsoft.embeddedsocial.data.model.AccountData;
import com.microsoft.embeddedsocial.sdk.R;
import com.microsoft.embeddedsocial.server.model.view.UserCompactView;
import com.microsoft.embeddedsocial.service.worker.RemoveFollowerWorker;
import com.microsoft.embeddedsocial.service.worker.WorkerHelper;
import com.microsoft.embeddedsocial.ui.adapter.viewholder.UserListItemHolder;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * Renders my followers with context menu.
 */
public class MyFollowersRenderer extends UserRenderer {

    public MyFollowersRenderer(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected void onItemRendered(UserCompactView user, UserListItemHolder holder) {
        super.onItemRendered(user, holder);
        UserAccount userAccount = UserAccount.getInstance();
        if (userAccount.isCurrentUser(user.getHandle()) || user.getFollowerStatus() == FollowerStatus.BLOCKED) {
            holder.actionButton.setVisibility(View.GONE);
        } else {
            renderRemoveFollowerButton(userAccount.getAccountDetails(), user, holder);
            renderActionButton(user, holder);
        }
    }

    private void renderRemoveFollowerButton(AccountData currUser, UserCompactView otherUser,
                                            UserListItemHolder holder) {
        TextView button = holder.removeFollowerButton;
        button.setVisibility(View.VISIBLE);
        button.setEnabled(true);
        button.setOnClickListener(v -> {
            Data inputData = new Data.Builder()
                    .putString(RemoveFollowerWorker.USER_HANDLE, otherUser.getHandle()).build();
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(RemoveFollowerWorker.class)
                    .setInputData(inputData).build();
            WorkManager.getInstance().enqueue(workRequest);

            WorkerHelper.handleResult(ProcessLifecycleOwner.get(), workRequest.getId(), new WorkerHelper.ResultHandler() {
                @Override
                public void onSuccess() {
                    // redraw the button
                    button.setText(R.string.es_removed_follower);
                    getStyleHelper().applyRedCompletedStyle(button);
                    // decrement local followers count
                    currUser.setFollowersCount(Math.max(0, currUser.getFollowersCount() - 1));
                }

                @Override
                public void onFailure() { }
            });
        });
        button.setText(R.string.es_remove_follower);
        getStyleHelper().applyRedStyle(button);
    }
}
