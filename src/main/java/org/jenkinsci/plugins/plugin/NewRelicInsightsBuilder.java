package org.jenkinsci.plugins.plugin;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plugin.newrelic.NewRelicInsights;
import org.jenkinsci.plugins.plugin.newrelic.NewRelicInsightsApacheClient;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link NewRelicInsightsBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #json})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Matthew Grose
 */
public class NewRelicInsightsBuilder extends Builder implements SimpleBuildStep {

    private final String credentialsId;
    private final String json;

    // Fields in credentials.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public NewRelicInsightsBuilder(String credentialsId, String json) {
        this.credentialsId = credentialsId;
        this.json = json;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public String getJson() {
        return this.json;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {

        NewRelicInsights insights = getClient();
        InsightsCredentials creds = getInsightsCredentials(this.credentialsId, build);

        try {
            if (insights.sendCustomEvent(creds.getApiKey().getPlainText(), creds.getAccountId(), json)) {
                listener.getLogger().println("New Relic Insights: Success, inserted custom event.");
            } else {
                listener.getLogger().println("New Relic Insights: Failure, did not insert custom event.");
            }
        } catch(IOException ex) {
            listener.getLogger().println("New Relic Insights: Failure, exception thrown.");
            listener.error(ex.getMessage(), ex);
        }


    }

    private InsightsCredentials getInsightsCredentials(String credentialsId, Run<?,?> run) {
        List<InsightsCredentials> insightsCredentialsList = CredentialsProvider.lookupCredentials(InsightsCredentials.class, run.getParent(), ACL.SYSTEM, new ArrayList<DomainRequirement>());
        return CredentialsMatchers.firstOrNull(insightsCredentialsList, CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialsId)));
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link NewRelicInsightsBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/NewRelicInsightsBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    @Symbol("newrelicInsights")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */
        private boolean useFrench;

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "New Relic Insights Custom Event";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public boolean getUseFrench() {
            return useFrench;
        }

        @SuppressWarnings("unused") // used by stapler
        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Job context, @QueryParameter String source, @QueryParameter String value) {
            if (context == null || !context.hasPermission(Item.CONFIGURE)) {
                // previously it was recommended to just return an empty ListBoxModel
                // now recommended to return a model with just the current value
                return new StandardUsernameListBoxModel().includeCurrentValue(value);
            }
            // previously it was recommended to use the withXXX methods providing the credentials instances directly
            // now recommended to populate the model using the includeXXX methods which call through to
            // CredentialsProvider.listCredentials and to ensure that the current value is always present using
            // includeCurrentValue
            return new StandardUsernameListBoxModel()
                    .includeEmptyValue()
                    .includeAs(Tasks.getAuthenticationOf(context), context, InsightsCredentials.class,
                            URIRequirementBuilder.fromUri(source).build())
                    .includeCurrentValue(value);
        }
    }

    public NewRelicInsights getClient() {
        return new NewRelicInsightsApacheClient();
    }
}

