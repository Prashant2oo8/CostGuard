package com.prashant.costguard.model;

import java.util.List;

public class CloudReport {

    private Summary summary;

    private List<CostResource> topExpensiveResources;

    private List<WasteResource> topWastefulResources;

    private List<EC2Instance> ec2Instances;

    private List<EbsVolume> ebsVolumes;

    private List<S3Bucket> s3Buckets;

    private Object rds;

    private Object elb;

    private Object autoscaling;

    public CloudReport(
            Summary summary,
            List<CostResource> topExpensiveResources,
            List<WasteResource> topWastefulResources,
            List<EC2Instance> ec2Instances,
            List<EbsVolume> ebsVolumes,
            List<S3Bucket> s3Buckets,
            Object rds,
            Object elb,
            Object autoscaling
    ) {
        this.summary = summary;
        this.topExpensiveResources = topExpensiveResources;
        this.topWastefulResources = topWastefulResources;
        this.ec2Instances = ec2Instances;
        this.ebsVolumes = ebsVolumes;
        this.s3Buckets = s3Buckets;
        this.rds = rds;
        this.elb = elb;
        this.autoscaling = autoscaling;
    }

    public Summary getSummary() {
        return summary;
    }

    public List<CostResource> getTopExpensiveResources() {
        return topExpensiveResources;
    }

    public List<WasteResource> getTopWastefulResources() {
        return topWastefulResources;
    }

    public List<EC2Instance> getEc2Instances() {
        return ec2Instances;
    }

    public List<EbsVolume> getEbsVolumes() {
        return ebsVolumes;
    }

    public List<S3Bucket> getS3Buckets() {
        return s3Buckets;
    }

    public Object getRds() {
        return rds;
    }

    public Object getElb() {
        return elb;
    }

    public Object getAutoscaling() {
        return autoscaling;
    }
}