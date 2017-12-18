package org.jenkinsci.plugins.artifactpromotion.jobdsl;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import javaposse.jobdsl.dsl.Context;

import org.jenkinsci.plugins.artifactpromotion.jobdsl.ArtifactPromotionJobDslExtension.RepositorySystem;

/**
 * Provides the DSL context to execute the artifactionPromotion closure in.
 * The public methods of this class can be called from the closure and thus define the DSL vocabulary
 * inside the artifactPromotion element.
 *  
 * @author Patrick Schlebusch
 */
public class ArtifactPromotionDslContext implements Context {
	private String groupId;
	private String artifactId;
	private String classifier;
	private String version;
	private String extension = "jar";
	
	private String stagingRepository;
	private StandardUsernamePasswordCredentials stagingCredentials;

	private String releaseRepository;
	private StandardUsernamePasswordCredentials releaseCredentials;

	private String promoterClass = RepositorySystem.NexusOSS.getClassName();
	private boolean debug = false;
	private boolean skipDeletion = true;

	public void groupId(String groupId) {
		this.groupId = groupId;
	}
	String getGroupId() {
		return groupId;
	}

	public void artifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	String getArtifactId() {
		return artifactId;
	}

	public void classifier(String classifier) {
		this.classifier = classifier;
	}
	String getClassifier() {
		return classifier;
	}

	public void version(String version) {
		this.version = version;
	}
	String getVersion() {
		return version;
	}

	public void extension(String extension) {
		this.extension = extension;
	}
	String getExtension() {
		return extension;
	}

	public void stagingRepository(String repository, StandardUsernamePasswordCredentials credentials) {
		this.stagingRepository(repository, credentials, true);
	}
	public void stagingRepository(String repository, StandardUsernamePasswordCredentials credentials, boolean skipDeletion) {
		this.stagingRepository = repository;
		this.stagingCredentials = credentials;
		this.skipDeletion = skipDeletion;
	}
	String getStagingRepository() {
		return stagingRepository;
	}

	StandardUsernamePasswordCredentials getStagingCredentials() {
		return stagingCredentials;
	}

	public void releaseRepository(String repository, StandardUsernamePasswordCredentials credentials) {
		this.releaseRepository = repository;
		this.releaseCredentials = credentials;
	}
	String getReleaseRepository() {
		return releaseRepository;
	}

	StandardUsernamePasswordCredentials getReleaseCredentials() {
		return releaseCredentials;
	}

	String getPromoterClass() {
		return promoterClass;
	}

	public void debug(boolean debug) {
		this.debug = debug;
	}
	boolean isDebugEnabled() {
		return debug;
	}

	boolean isSkipDeletionEnabled() {
		return skipDeletion;
	}
	
}
