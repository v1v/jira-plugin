package hudson.plugins.jira;


import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
/**
 * Created by victor.martinez on 16/02/15.
 */
public class Example2 {

    private static URI jiraServerUri = URI.create("http://192.168.59.103:8080");

    public static void main(String[] args) throws IOException {
        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "robin", "robin");

		try {
			final List<Promise<BasicIssue>> promises = Lists.newArrayList();
			final IssueRestClient issueClient = restClient.getIssueClient();

			System.out.println("Sending issue creation requests...");
			for (int i = 0; i < 1; i++) {
				final String summary = "NewIssue#" + i;
				final IssueInput newIssue = new IssueInputBuilder("TEST", 1L, summary).build();
				System.out.println("\tCreating: " + summary);
				promises.add(issueClient.createIssue(newIssue));
			}
            final Issue issue = restClient.getIssueClient().getIssue("TEST-1").claim();
            System.out.println(issue);

		} finally {
			restClient.close();
		}
	}
}
