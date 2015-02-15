package hudson.plugins.jira;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import hudson.plugins.jira.api.JiraRestApi;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Parses build changelog for JIRA issue IDs and then
 * updates JIRA issues accordingly based on different attributes
 *
 * @author Victor Martinez
 */
public class JiraIssueKingUpdater extends Recorder implements MatrixAggregatable {

    private String assignee;
    private final String comment;

    @DataBoundConstructor
    public JiraIssueKingUpdater(String assignee, String comment) {
        this.assignee = assignee;
        this.comment = Util.fixEmptyAndTrim(comment);

    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        // Don't do anything for individual matrix runs.
        if (build instanceof MatrixRun) {
            return true;
        }
        if (this.assignee != null && ! this.assignee.equals("")) {
          // TODO:
          // https://developer.atlassian.com/jiradev/api-reference/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-edit-issues#JIRARESTAPIExample-Editissues-Exampleofassigninganissuetouser"harry"
          String realComment = Util.fixEmptyAndTrim(build.getEnvironment(listener).expand(this.comment));
          return JiraRestApi.perform(build, listener, this.assignee, realComment);
        } else {
          return Updater.perform(build, listener);
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener) {
            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                PrintStream logger = listener.getLogger();
                logger.println("End of Matrix Build. Updating JIRA.");
                // TODO:
                // https://developer.atlassian.com/jiradev/api-reference/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-example-edit-issues#JIRARESTAPIExample-Editissues-Exampleofassigninganissuetouser"harry"
                return Updater.perform(this.build, this.listener);
            }
        };
    }

    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private DescriptorImpl() {
            super(JiraIssueKingUpdater.class);
        }

        public FormValidation doCheckAssignee(@QueryParameter String value)
                throws IOException {
            if (value.length() == 0) {
                return FormValidation.error("Please set the assigned user");
            }
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            // Displayed in the publisher section
            return Messages.JiraIssueKingUpdater_DisplayName();

        }

        @Override
        public String getHelpFile() {
            return "/plugin/jira/help-jira-issue-king-updater.html";
        }

        @Override
        public Publisher newInstance(StaplerRequest req,
                                                   JSONObject formData) throws FormException {
            return req.bindJSON(JiraIssueKingUpdater.class, formData);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
