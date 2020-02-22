#!/bin/bash

# aws s3 sync s3://wordoflife.mn.catalog/prophecy ~/tmp/catalog/prophecy
aws s3 cp --recursive s3://wordoflife.mn.prophecy ~/tmp/catalog/prophecy
