package org.jenkinsci.plugins.plugin;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;


public class KeyValue
        extends AbstractDescribableImpl<KeyValue> {

    private String key;
    private String value;

    @DataBoundConstructor
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    @Extension
    public static final class DescriptorImpl
            extends Descriptor<KeyValue> {

        @Override
        public String getDisplayName() {
            return "Key Value Data";
        }
    }

}