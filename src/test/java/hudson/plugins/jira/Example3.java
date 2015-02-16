package hudson.plugins.jira;

/**
 * Created by victorm on 16/02/15.
 */
public class Example3 {

    public static void main(String[] args) throws URISyntaxException {
        final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        final URI jiraServerUri = new URI("http://localhost:8090/jira");
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "yourusername", "yourpassword");
        final NullProgressMonitor pm = new NullProgressMonitor();
        final Issue issue = restClient.getIssueClient().getIssue("TST-1", pm);

        System.out.println(issue);

        // now let's vote for it
        restClient.getIssueClient().vote(issue.getVotesUri(), pm);

        // now let's watch it
        restClient.getIssueClient().watch(issue.getWatchers().getSelf(), pm);

        // now let's start progress on this issue
        final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri(), pm);
        final Transition startProgressTransition = getTransitionByName(transitions, "Start Progress");
        restClient.getIssueClient().transition(issue.getTransitionsUri(), new TransitionInput(startProgressTransition.getId()), pm);

        // and now let's resolve it as Incomplete
        final Transition resolveIssueTransition = getTransitionByName(transitions, "Resolve Issue");
        Collection<FieldInput> fieldInputs = Arrays.asList(new FieldInput("resolution", "Incomplete"));
        final TransitionInput transitionInput = new TransitionInput(resolveIssueTransition.getId(), fieldInputs, Comment.valueOf("My comment"));
        restClient.getIssueClient().transition(issue.getTransitionsUri(), transitionInput, pm);

    }

    private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }
}


