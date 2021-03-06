/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */

package com.microsoft.embeddedsocial.ui.activity;

import com.microsoft.embeddedsocial.sdk.R;
import com.microsoft.embeddedsocial.ui.fragment.ViewImageFragment;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Activity for detailed view single image.
 */
public class ViewImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.es_activity_view_image);

        setSupportActionBar((Toolbar) findViewById(R.id.es_toolbar));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        final ViewImageFragment fragment = new ViewImageFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.es_content, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
