/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 *
 */

package com.microsoft.socialplus.server.model.view;

import android.text.TextUtils;

import com.microsoft.socialplus.autorest.models.ContentType;
import com.microsoft.socialplus.base.function.Predicate;
import com.microsoft.socialplus.data.model.ActivityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks {@link ActivityView}'s received from the server.
 */
public final class ActivityViewAssertion implements Predicate<ActivityView> {

	private static final Predicate<ActivityView> HAS_ONE_ACTOR_USER =
		object -> assertActorsCount(object, 1);

	private static final Predicate<ActivityView> HAS_ONE_ACTOR_USER_AND_CONTENT =
		object -> HAS_ONE_ACTOR_USER.test(object) && contentIsNotEmpty(object);

	private static final Predicate<ActivityView> HAS_AT_LEAST_ONE_ACTOR_USER_AND_CONTENT =
		object -> assertActorsCountAtLeast(object, 1) && contentIsNotEmpty(object);

	private final Map<ActivityType, Predicate<ActivityView>> predicatesByType;

	private ActivityViewAssertion(Map<ActivityType, Predicate<ActivityView>> predicatesByType) {
		this.predicatesByType = predicatesByType;
	}

	@Override
	public boolean test(ActivityView object) {
		Predicate<ActivityView> predicate = predicatesByType.get(object.getActivityType());
		return predicate != null && predicate.test(object);
	}

	private static boolean assertActorsCount(ActivityView activity, int expectedCount) {
		List<UserCompactView> actors = activity.getActorUsers();
		return actors != null && actors.size() == expectedCount;
	}

	private static boolean assertActorsCountAtLeast(ActivityView activity, int expectedCount) {
		List<UserCompactView> actors = activity.getActorUsers();
		int count = activity.getCount();
		final int maxActorsCount = 2;
		return actors != null
				&& count >= expectedCount
				&& ((count > maxActorsCount && actors.size() == maxActorsCount) || actors.size() == activity.getCount());
	}

	private static boolean isNotEmpty(String s) {
		return !TextUtils.isEmpty(s);
	}

	private static boolean contentIsNotEmpty(ActivityView activity) {
		return isNotEmpty(activity.getActedOnContentHandle()) && isNotEmpty(activity.getActedOnContentText());
	}

	private static boolean actedOnTopicOrComment(ActivityView activity) {
		return activity.getActedOnContentType() != ContentType.REPLY;
	}

	public static Predicate<ActivityView> forUserFeed() {
		Map<ActivityType, Predicate<ActivityView>> predicates = getCommonPredicates();
		predicates.put(ActivityType.FOLLOW_ACCEPT, HAS_ONE_ACTOR_USER);
		predicates.put(ActivityType.FOLLOW_REQUEST, HAS_ONE_ACTOR_USER);
		predicates.put(ActivityType.FOLLOWING, HAS_ONE_ACTOR_USER);
		return new ActivityViewAssertion(predicates);
	}

	public static Predicate<ActivityView> forFollowingFeed() {
		Map<ActivityType, Predicate<ActivityView>> predicates = getCommonPredicates();
		predicates.put(ActivityType.FOLLOWING, object -> HAS_ONE_ACTOR_USER.test(object) && object.getActedOnUser() != null);
		return new ActivityViewAssertion(predicates);
	}

	private static Map<ActivityType, Predicate<ActivityView>> getCommonPredicates() {
		Map<ActivityType, Predicate<ActivityView>> predicatesByType = new HashMap<>();
		predicatesByType.put(ActivityType.MENTION, HAS_ONE_ACTOR_USER_AND_CONTENT);
		predicatesByType.put(ActivityType.CHILD, object -> HAS_AT_LEAST_ONE_ACTOR_USER_AND_CONTENT.test(object) && actedOnTopicOrComment(object));
		predicatesByType.put(ActivityType.CHILD_PEER, object -> HAS_ONE_ACTOR_USER_AND_CONTENT.test(object) && actedOnTopicOrComment(object));
		predicatesByType.put(ActivityType.FOLLOW_ACCEPT, HAS_ONE_ACTOR_USER);
		predicatesByType.put(ActivityType.FOLLOW_REQUEST, HAS_ONE_ACTOR_USER);
		predicatesByType.put(ActivityType.LIKE, HAS_AT_LEAST_ONE_ACTOR_USER_AND_CONTENT);
		return predicatesByType;
	}
}
