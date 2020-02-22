package org.wolm.aws;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Helper for accessing Amazon's S3 content.
 * <p>
 * To configure, you must have a properties file at ~/.wolm/aws.s3.properties that contains at least the following
 * values, one per line in 'name=value' format:
 * <ul>
 * <li>accessKey=
 * <li>secretKey=
 * </ul>
 * 
 * @author wolm
 */
public class AwsS3Helper {

	public enum Disposition {
		download, browser
	};

	/** singleton instance */
	private static AwsS3Helper instance = null;

	/** cached client */
	private transient AmazonS3 s3Client;

	/** cached map of buckets */
	private transient Map<String, Bucket> bucketCache = new HashMap<>();

	/** @return Singleton instance of the AWS S3 Helper */
	public static AwsS3Helper instance() {
		if (instance == null) instance = new AwsS3Helper();
		return instance;
	}

	/** This is a singleton, so the constructor is private */
	private AwsS3Helper() {
		super();
	}

	@Nonnull
	private AmazonS3 getS3Client() {
		if (s3Client == null) {
			s3Client = new AmazonS3Client(
					new PropertiesFileCredentialsProvider(System.getenv("HOME") + "/.wolm/aws.s3.properties"));
			s3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
		}
		return s3Client;
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
		if (bucketCache.containsKey(bucketName)) return bucketCache.get(bucketName);

		for (Bucket bucket : getBuckets())
			if (bucketName.equals(bucket.getName())) {
				bucketCache.put(bucketName, bucket);
				return bucket;
			}

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
		return getS3Client().putObject(new PutObjectRequest(bucketName, objectKey, fileToUpload)
				.withCannedAcl(CannedAccessControlList.PublicRead));
	}

	/**
	 * Given a bucket, will return a list of the objects in the bucket
	 * 
	 * @param bucket Bucket
	 * @param prefix Optional prefix
	 * @param regex Optional pattern to match. {@code null} to return everything
	 * @return List of object summaries
	 */
	public List<S3ObjectSummary> getObjectList(@Nonnull Bucket bucket, @Nullable String prefix,
			@Nullable String regex) {
		ListObjectsRequest request = new ListObjectsRequest().withBucketName(bucket.getName());
		if (prefix != null) request.setPrefix(prefix);

		Pattern pattern = null;
		if (regex != null) pattern = Pattern.compile(regex);

		List<S3ObjectSummary> summaries = new ArrayList<>();
		for (S3ObjectSummary summary : getS3Client().listObjects(request).getObjectSummaries()) {
			if (pattern == null || pattern.matcher(summary.getKey()).matches()) summaries.add(summary);
		}

		return summaries;
	}

	/**
	 * @param bucket
	 * @param objectKey
	 * @return Information about the object
	 */
	@Nullable
	public S3ObjectSummary getObjectSummary(@Nonnull Bucket bucket, @Nonnull String objectKey) {
		ObjectListing listObjects = getS3Client()
				.listObjects(new ListObjectsRequest().withBucketName(bucket.getName()).withPrefix(objectKey));
		for (S3ObjectSummary summary : listObjects.getObjectSummaries())
			if (summary.getKey().equals(objectKey)) return summary;
		return null;
	}

	/**
	 * @param bucket
	 * @param objectKey
	 * @return A pre-signed URL for this object
	 */
	public URL getSignedUrl(@Nonnull Bucket bucket, @Nonnull String objectKey, Disposition disposition) {
		Date expiration = new Date(System.currentTimeMillis() + 7 * DateUtils.MILLIS_PER_DAY);

		GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket.getName(), objectKey);
		request.setExpiration(expiration);

		switch (disposition) {
		case browser:
			// this is the default behavior
			break;
		case download:
			ResponseHeaderOverrides overrides = new ResponseHeaderOverrides().withContentDisposition("attachment");
			request.setResponseHeaders(overrides);
			break;
		}

		return getS3Client().generatePresignedUrl(request);
	}

	/**
	 * @param url A standard S3 URL, copied from the S3 web page like
	 * {@code https://s3-us-west-2.amazonaws.com/wordoflife.mn.audio/2005/2005-08-14+Poor+of+Soul.mp3}
	 * @return A pre-signed URL for this object
	 */
	public URL getSignedUrl(@Nullable URL url, Disposition disposition) {
		if (url == null) return null;
		List<String> parts = Arrays.asList(url.toString().split("/"));
		if (parts.size() < 5) return null;
		if (!parts.get(2).equals("s3-us-west-2.amazonaws.com")) return null;

		try {
			Bucket bucket = getBucket(parts.get(3));
			if (bucket == null) return null;

			String key = StringUtils.join(parts.subList(4, parts.size()).iterator(), '/');
			if (key == null) return null;
			key = URLDecoder.decode(key, "UTF-8");

			URL signedUrl = getSignedUrl(bucket, key, disposition);
			signedUrl = new URL(signedUrl.toString().replace("https:", "http:"));
			return signedUrl;
		}
		catch (MalformedURLException | UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * Gets an input stream for a specific object.
	 * 
	 * @param bucketName Bucket
	 * @param key Object key
	 * @return Input stream for the object. Caller must close()
	 */
	public InputStream getContent(@Nonnull String bucketName, @Nonnull String key) {
		S3Object object = getS3Client().getObject(new GetObjectRequest(bucketName, key));
		return object.getObjectContent();
	}
}
