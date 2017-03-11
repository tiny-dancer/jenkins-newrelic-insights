package org.jenkinsci.plugins.plugin;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.plugins.plugin.InsightsCredentialsImpl;

public class InsightsCredentialsNameProvider extends CredentialsNameProvider<InsightsCredentialsImpl>{
    @NonNull
    @Override
    public String getName(@NonNull InsightsCredentialsImpl credentials) {
        return credentials.getName();
    }
}