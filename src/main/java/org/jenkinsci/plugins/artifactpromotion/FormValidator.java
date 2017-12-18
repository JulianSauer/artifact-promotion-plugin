package org.jenkinsci.plugins.artifactpromotion;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import java.net.URI;
import java.net.URISyntaxException;

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

    default ListBoxModel doFillStagingCredentialsItems(@AncestorInPath Item item, @QueryParameter String stagingCredentials) {
        return doFillCredentialsItems(item, stagingCredentials);
    }

    default ListBoxModel doFillReleaseCredentialsItems(@AncestorInPath Item item, @QueryParameter String releaseCredentials) {
        return doFillCredentialsItems(item, releaseCredentials);
    }

    default ListBoxModel doFillCredentialsItems(Item item, String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(credentialsId);
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return result.includeCurrentValue(credentialsId);
            }
        }
        return result
                .includeEmptyValue()
                .includeMatchingAs(ACL.SYSTEM, Jenkins.getInstance(), StandardUsernamePasswordCredentials.class, URIRequirementBuilder.fromUri("").build(), CredentialsMatchers.always())
                .includeCurrentValue(credentialsId);
    }

    default FormValidation doCheckStagingCredentials(
            @AncestorInPath Item item,
            @QueryParameter("stagingCredentials") String stagingCredentials,
            @QueryParameter("stagingRepository") String stagingRepository) {
        return doCheckCredentials(item, stagingCredentials, stagingRepository);
    }

    default FormValidation doCheckReleaseCredentials(
            @AncestorInPath Item item,
            @QueryParameter("releaseCredentials") String releaseCredentials,
            @QueryParameter("releaseRepository") String releaseRepository) {
        return doCheckCredentials(item, releaseCredentials, releaseRepository);
    }

    default FormValidation doCheckCredentials(Item item, String credentialsId, String repository) {
        if (item == null) {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                return FormValidation.ok();
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return FormValidation.ok();
            }
        }
        StandardUsernamePasswordCredentials credentials = CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance(), ACL.SYSTEM), CredentialsMatchers.withId(credentialsId));
        if (credentials == null) {
            return FormValidation.error("Cannot find currently selected credentials!");
        }

        // Test connection
        URI stagingURI = null;
        try {
            stagingURI = new URI(repository);
        } catch (URISyntaxException e) {
            return FormValidation.error("Repository URI is not valid");
        }
        if (!stagingURI.isAbsolute()) {
            return FormValidation.error("Repository URI is not valid");
        }

        Client client = Client.create();
        String auth = credentials.getUsername() + ":" + Secret.toString(credentials.getPassword());
        WebResource webResource = client.resource(repository);
        ClientResponse response;
        try {
            response = webResource.header("Authorization", "Digest " + auth).type("application/json")
                    .accept("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            return FormValidation.error("Could not connect to repository!");
        }

        int statusCode = response.getStatus();
        if (statusCode == 401) {
            return FormValidation.error("Invalid Username or Password for repository!");
        } else if (statusCode >= 400 && statusCode < 500) {
            return FormValidation.error(statusCode + ": Could not connect to repository!");
        }

        return FormValidation.ok();
    }

}
