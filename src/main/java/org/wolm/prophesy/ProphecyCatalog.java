package org.wolm.prophesy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.wolm.aws.AwsS3Helper;
import org.wolm.catalog.App;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.beust.jcommander.internal.Nullable;

public class ProphecyCatalog {
	private List<Prophecy> prophecies = new ArrayList<>();

	private final String bucketName;
	private final String prophecyDirectoryName;

	public ProphecyCatalog(@Nonnull String bucketName, @Nullable String prophecyDirectoryName) {
		super();

		this.bucketName = bucketName;
		this.prophecyDirectoryName = prophecyDirectoryName;
	}

	public void populateFromAwsDocuments(String bucketName, String outputDir) {
		List<String> prophecyKeys = listPropheciesInBucket();
		for (String prophecyKey : prophecyKeys) {
			addProphecy(readProphecy(prophecyKey));
		}
	}

	public List<Prophecy> getProphecies() {
		return prophecies;
	}

	public void addProphecy(Prophecy prophecy) {
		if (prophecy == null) return;
		if (prophecy.getDateString() == null) {
			App.logError("Prophecy '" + prophecy.getTitle() + "' has no date. Ignoring this prophecy.");
			return;
		}
		prophecies.add(prophecy);
	}

	private List<String> listPropheciesInBucket() {
		AwsS3Helper helper = AwsS3Helper.instance();

		List<String> prophecyKeys = new ArrayList<>();
		Bucket bucket = helper.getBucket(bucketName);

		if (bucket == null) throw new RuntimeException("Bucket name is not defined");
		for (S3ObjectSummary summary : helper.getObjectList(bucket, prophecyDirectoryName, null)) {
			App.logInfo("Reading " + summary.getKey());
			prophecyKeys.add(summary.getKey());
		}

		return prophecyKeys;
	}

	private Prophecy readProphecy(@Nonnull String key) {
		AwsS3Helper helper = AwsS3Helper.instance();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(helper.getContent(bucketName, key), "UTF-8"))) {
			Prophecy prophecy = new Prophecy();
			String line;
			StringBuilder body = null;
			while ((line = reader.readLine()) != null) {
				// System.out.println(key + " | " + line);
				if (body != null) {
					body.append(StringUtils.isBlank(line) ? "</p><p>" : line);
					body.append(' ');
				}
				else if (StringUtils.isBlank(line)) {
					body = new StringBuilder();
					body.append("<p>");
				}
				else if (line.startsWith("Title:")) {
					prophecy.setTitle(StringUtils.trimToNull(line.substring("Title:".length())));
				}
				else if (line.startsWith("Date:")) {
					prophecy.setDateString(StringUtils.trimToNull(line.substring("Date:".length())));
				}
				else if (line.startsWith("Location:")) {
					prophecy.setLocation(StringUtils.trimToNull(line.substring("Location:".length())));
				}
				else if (line.startsWith("By:")) {
					prophecy.setBy(StringUtils.trimToNull(line.substring("By:".length())));
				}
				else {
					System.err.println("WARN: Unknown line: " + line);
				}
			}
			if (body != null) body.append("</p>");
			prophecy.setHtmlBody(body == null ? "" : body.toString().replaceAll("  *", " "));
			return prophecy;
		}
		catch (IOException e) {
			System.err.println("ERROR: Cannot read prophecy '" + key + "': " + e.getMessage());
			return null;
		}
	}
}
