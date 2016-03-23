/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 *
 */

package com.microsoft.socialplus.server.model.content.topics;

import com.microsoft.socialplus.autorest.UserTopicsOperations;
import com.microsoft.socialplus.autorest.UserTopicsOperationsImpl;
import com.microsoft.socialplus.autorest.models.FeedResponseTopicView;
import com.microsoft.rest.ServiceException;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.socialplus.autorest.models.TimeRange;
import com.microsoft.socialplus.data.model.TopicFeedType;
import com.microsoft.socialplus.server.exception.NetworkRequestException;
import com.microsoft.socialplus.server.model.FeedUserRequest;

import java.io.IOException;

public final class GetTopicFeedRequest extends FeedUserRequest {

	private static final UserTopicsOperations USER_TOPICS =
			new UserTopicsOperationsImpl(RETROFIT, CLIENT);
	private final TopicFeedType topicFeedType;
	private final String query;

	public GetTopicFeedRequest(TopicFeedType topicFeedType) {
		this(null, topicFeedType);
	}

	public GetTopicFeedRequest(String query, TopicFeedType topicFeedType) {
		this.query = query;
		this.topicFeedType = topicFeedType;
	}

	public String getQuery() {
		return query;
	}

	public TopicFeedType getTopicFeedType() {
		return topicFeedType;
	}

	@Override
	public TopicsListResponse send() throws NetworkRequestException {
		ServiceResponse<FeedResponseTopicView> serviceResponse;
		String cursor = getCursor();
		int limit = getBatchSize();
		try {
			switch (topicFeedType) {
				case USER_RECENT:
					// {userHandle}/topics
					serviceResponse = USER_TOPICS.getTopics(query, cursor, limit, appKey, bearerToken);
					break;
				case FOLLOWING_RECENT:
					// users/me/following/topics
					serviceResponse = FOLLOWING.getTopics(bearerToken, cursor, limit);
					break;
				case EVERYONE_RECENT:
					serviceResponse = TOPICS.getTopics(cursor, limit, appKey, bearerToken);
					break;
				default: // Based on popularity
					serviceResponse = getPopularTopics(getIntCursor(), limit);
			}
		} catch (ServiceException|IOException e) {
			throw new NetworkRequestException(e.getMessage());
		}
		checkResponseCode(serviceResponse);
		return new TopicsListResponse(serviceResponse.getBody());
	}

	private ServiceResponse<FeedResponseTopicView> getPopularTopics(int cursor, int limit)
			throws ServiceException, IOException {
		if (topicFeedType == TopicFeedType.USER_POPULAR) {
			// {userHandle}/topics/popular
			return USER_TOPICS.getPopularTopics(query, cursor, limit, appKey, bearerToken);
		} else {
			TimeRange timeRange = getTimeRange();
			return TOPICS.getPopularTopics(timeRange, cursor, limit, appKey, bearerToken);
		}
	}

	private TimeRange getTimeRange() {
		switch (topicFeedType) {
			case EVERYONE_POPULAR_THIS_MONTH:
				return TimeRange.THISMONTH;
			case EVERYONE_POPULAR_THIS_WEEK:
				return TimeRange.THISWEEK;
			case EVERYONE_POPULAR_TODAY:
				return TimeRange.TODAY;
			default: // all time
				return TimeRange.ALLTIME;
		}
	}

	@Override
	public String toString() {
		return "GetTopicFeedRequest{" +
			"query='" + query + '\'' +
			", topicFeedType=" + topicFeedType +
			'}';
	}
}
