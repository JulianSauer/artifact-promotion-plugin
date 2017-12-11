package org.jenkinsci.plugins.artifactpromotion;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

/**
 * Perform validations for StepDescriptor implementations.
 *
 * @author Halil-Cem Guersoy (hcguersoy@gmail.com)
 * @author Julian Sauer (julian_sauer@mx.net)
 */
public interface FormValidator {

    /**
     * Performs on-the-fly validation of the form field 'name'.
     *
     * @param value This parameter receives the value that the user has typed.
     * @return Indicates the outcome of the validation. This is sent to the
     * browser.
     */
    default FormValidation doCheckArtifactId(@QueryParameter String value) {
        if (value.length() == 0)
            return FormValidation.error("Please set an ArtifactId!");
        return FormValidation.ok();
    }

    default FormValidation doCheckGroupId(@QueryParameter String value) {
        if (value.length() == 0)
            return FormValidation.error("Please set a GroupId!");
        return FormValidation.ok();
    }

    default FormValidation doCheckVersion(@QueryParameter String value) {
        if (value.length() == 0)
            return FormValidation
                    .error("Please set a Version for your artifact!");
        return FormValidation.ok();
    }

    default FormValidation doCheckStagingRepository(
            @QueryParameter String value) {
        return checkURI(value);
    }

    default FormValidation doCheckReleaseRepository(
            @QueryParameter String value) {
        return checkURI(value);
    }

    /**
     * This method checks originally the URL if it is valid. On the way to
     * support tokens this behavior is build out. It will be reactivated
     * after a general refactoring for better token macro support.
     * <p>
     * TODO implment a URL validation which works with token macro plugin
     *
     * @param value
     * @return
     */
    default FormValidation checkURI(String value) {
        if (value.length() == 0) {
            return FormValidation
                    .error("Please set an URL for the staging repository!");
        }
        return FormValidation.ok();
    }

    /**
     * Generates LisBoxModel for available Repository systems
     *
     * @return available Promoters as ListBoxModel
     */
    default ListBoxModel doFillPromoterClassItems() {
        ListBoxModel promoterModel = new ListBoxModel();
        for (Promotor promotor : Jenkins.getInstance()
                .getExtensionList(Promotor.class)) {
            promoterModel.add(promotor.getDescriptor().getDisplayName(), promotor
                    .getClass().getCanonicalName());
        }

        return promoterModel;
    }

    default ListBoxModel doFillStagingPWItems(@AncestorInPath Item item, @QueryParameter String stagingPW) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(stagingPW);
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return result.includeCurrentValue(stagingPW);
            }
        }
        return result
                .includeEmptyValue()
                .includeMatchingAs(ACL.SYSTEM, Jenkins.getInstance(), StandardUsernamePasswordCredentials.class, URIRequirementBuilder.fromUri("").build(), CredentialsMatchers.always())
                .includeCurrentValue(stagingPW);
    }

}
