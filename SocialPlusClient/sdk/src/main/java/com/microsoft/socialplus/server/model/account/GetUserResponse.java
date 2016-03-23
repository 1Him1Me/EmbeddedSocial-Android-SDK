/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 *
 */

package com.microsoft.socialplus.server.model.account;

import com.microsoft.socialplus.autorest.models.UserProfileView;
import com.microsoft.socialplus.server.model.view.UserCompactView;

/**
 *
 */
public class GetUserResponse {

	private UserCompactView user;

	public GetUserResponse(UserCompactView user) {
		this.user = user;
	}

	public GetUserResponse(UserProfileView view) {
		user = new UserCompactView(view);
	}

	public UserCompactView getUser() {
		return user;
	}
}
