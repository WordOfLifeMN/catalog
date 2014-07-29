package org.wolm.aws;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AwsS3Helper {

	@Nonnull
	private AmazonS3 getS3Client() {
		AmazonS3Client s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider(
				"org/wolm/aws/AwsCredentials.properties"));
		s3.setRegion(Region.getRegion(Regions.US_WEST_2));
		return s3;
	}

	/** @return All buckets in the S3 account */
	@Nonnull
	public List<Bucket> getBuckets() {
		return getS3Client().listBuckets();
	}

	/**
	 * @param bucketName
	 * @return Bucket with the name, or <code>null</code> if not found
	 */
	@Nullable
	public Bucket getBucket(@Nonnull String bucketName) {
		for (Bucket bucket : getBuckets())
			if (bucketName.equals(bucket.getName())) return bucket;
		return null;
	}

	/**
	 * Uploads a file to the root of the bucket, using the file name as the object key
	 * 
	 * @param bucketName Name of the bucket to upload to
	 * @param fileToUpload File to upload
	 * @return Result of the upload
	 */
	@Nonnull
	public PutObjectResult uploadPublicFileToRoot(@Nonnull Bucket bucket, @Nonnull File fileToUpload) {
		return uploadPublicFile(bucket.getName(), fileToUpload.getName(), fileToUpload);
	}

	/**
	 * 
	 * @param bucket Bucket to upload to
	 * @param objectKey Key for the file to upload
	 * @param fileToUpload File to upload
	 * @return Result of the upload
	 */
	@Nonnull
	public PutObjectResult uploadPublicFile(@Nonnull Bucket bucket, @Nonnull String objectKey,
			@Nonnull File fileToUpload) {
		return uploadPublicFile(bucket.getName(), objectKey, fileToUpload);
	}

	/**
	 * Uploads a file to the bucket.
	 * 
	 * @param bucketName Name of the bucket to upload to
	 * @param objectKey Key to use for the file contents
	 * @param fileToUpload File to upload
	 * @return Result of the upload
	 */
	@Nonnull
	private PutObjectResult uploadPublicFile(@Nonnull String bucketName, @Nonnull String objectKey,
			@Nonnull File fileToUpload) {
		return getS3Client().putObject(
				new PutObjectRequest(bucketName, objectKey, fileToUpload)
						.withCannedAcl(CannedAccessControlList.PublicRead));
	}

	/**
	 * @param bucket
	 * @param objectKey
	 * @return Information about the object
	 */
	@Nullable
	public S3ObjectSummary getObjectSummary(@Nonnull Bucket bucket, @Nonnull String objectKey) {
		ObjectListing listObjects = getS3Client().listObjects(
				new ListObjectsRequest().withBucketName(bucket.getName()).withPrefix(objectKey));
		for (S3ObjectSummary summary : listObjects.getObjectSummaries())
			if (summary.getKey().equals(objectKey)) return summary;
		return null;
	}
}
