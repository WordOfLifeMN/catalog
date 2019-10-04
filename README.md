# Word of Life Catalog Factory

Utility to construct and update the web pages that comprise the message catalog Word of Life Ministries of Minnesota.
Builds and uploads all the pages that http://www.wordoflifemn.org/messages-online.html points to.

## Install

1. Clone this repository
2. Run `mvn package`
3. Install the `aws` command line interface as described here: https://aws.amazon.com/cli/

## Configure

1. `mkdir ~/.wolm`

2. Get your service ID from Google and store it in the `google.properties` file.
```
$ cat ~/.wolm/google.properties
serviceAccountId=BLAHBLAH-BLAHBLAH@developer.gserviceaccount.com
```

3. Get your AWS credentials and store them in the `~/.wolm/aws.s3.properties` file.
```
$ cat ~/.wolm/aws.s3.properties 
username=Media.Department
accessKey=****************S76Q
secretKey=****************GufJ
```

4. Install the same credentials as above for the aws command line under the `wolm` profile.
```
$ aws --profile=wolm configure
AWS Access Key ID: ****************S76Q 
AWS Secret Access Key:****************GufJ 
Default region name: us-west-2
Default output format: json 

```

## Run

1. `cd` to project directory (the one containing this README)
2. `./generate-public-catalog.command`
