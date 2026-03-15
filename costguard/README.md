# CostGuard – AWS Cloud Cost Optimization System

CostGuard is a backend system that analyzes AWS cloud resources and provides cost optimization insights.  
It helps identify expensive resources, detect waste, and estimate potential savings in a cloud infrastructure.

The system is built using **Java, Spring Boot, and AWS SDK**.

---

# Project Overview

Cloud environments often contain underutilized resources that increase operational costs.

CostGuard automatically:

- Discovers AWS resources
- Estimates monthly cloud cost
- Detects underutilized resources
- Calculates potential savings
- Provides optimization recommendations

The system exposes a REST API that returns a **cloud cost optimization dashboard**.

---

# Technologies Used

Backend:
- Java 21
- Spring Boot
- Maven

AWS Integration:
- AWS SDK v2
- EC2 API
- CloudWatch Metrics
- S3 API
- EBS API
- RDS API
- ELB API
- Auto Scaling API

---

# Features

## Cloud Resource Discovery

CostGuard automatically discovers resources from AWS including:

- EC2 Instances
- EBS Volumes
- S3 Buckets
- RDS Databases
- Load Balancers
- Auto Scaling Groups

---

## Cloud Cost Estimation

The system estimates the monthly cost of cloud resources.

Example:

EC2 Monthly Cost = Hourly Price × 730 hours

Supported services:

- EC2
- EBS
- S3

---

## Service Cost Breakdown

CostGuard shows cost distribution across cloud services.

Example:


---

## Expensive Resource Detection

Identifies resources contributing the most to cloud cost.

Example:

- EC2 instances with the highest monthly cost.

---

## Waste Detection

Detects underutilized resources such as:

- EC2 instances with very low CPU utilization.

---

## Optimization Insights

Provides recommendations to reduce cloud cost.

Example insights:

- Stop EC2 instances with low CPU usage.
- Monitor S3 buckets for lifecycle optimization.
- Delete unused EBS volumes.

---

# Cloud Dashboard API

Endpoint:


This endpoint returns a complete cloud cost optimization report.

---

# Example API Response
{
"summary": {
"currentMonthlyCost": 8.25,
"potentialSavings": 7.59,
"optimizedMonthlyCost": 0.66,
"savingsPercentage": 91.97,
"efficiencyScore": 8
},

"serviceCostBreakdown": {
"autoscaling": 0,
"elb": 0,
"rds": 0,
"s3": 0.023,
"ec2": 7.592,
"ebs": 0.64
},

"topExpensiveResources": [
{
"name": "i-089b03f5af5f06fcc",
"type": "EC2 Instance",
"monthlyCost": 7.592
}
],

"topWastefulResources": [
{
"name": "i-089b03f5af5f06fcc",
"reason": "Low CPU Utilization",
"potentialSaving": 7.592
}
],

"ec2Instances": [
{
"instanceId": "i-089b03f5af5f06fcc",
"instanceType": "t3.micro",
"state": "stopped",
"monthlyCost": 7.592,
"cpuUtilization": 0,
"recommendation": "Consider Stopping (Underutilized)"
}
],

"ebsVolumes": [
{
"volumeId": "vol-0d3fab40db3f12fb9",
"size": 8,
"state": "in-use",
"volumeType": "gp3",
"monthlyCost": 0.64,
"recommendation": "Volume in use"
}
],

"s3Buckets": [
{
"bucketName": "costguard-demo",
"storageGB": 1,
"monthlyCost": 0.023,
"recommendation": "Bucket size normal"
}
],

"rds": {
"status": "Not Initialized",
"reason": "No RDS databases found in AWS account"
},
"elb": {
"status": "Not Initialized",
"reason": "No Load Balancers found"
},
"autoscaling": {
"status": "Not Initialized",
"reason": "No Auto Scaling Groups configured"
},

"optimizationInsights": [
"Stop EC2 instance i-089b03f5af5f06fcc (Low CPU utilization)",
"Monitor S3 bucket costguard-demo for lifecycle optimization"
]
}


---

# Project Structure

costguard
│
├── controller
│ ├── EC2Controller
│ ├── EbsController
│ ├── S3Controller
│ └── CloudController
│
├── service
│ ├── EC2Service
│ ├── EbsService
│ ├── S3Service
│ ├── RdsService
│ ├── ElbService
│ ├── AsgService
│ └── CloudService
│
├── model
│ ├── EC2Instance
│ ├── EbsVolume
│ ├── S3Bucket
│ ├── Summary
│ └── CloudReport
│
└── CostguardApplication


---

# Future Improvements

Possible future enhancements:

- Web dashboard UI with charts
- AWS Cost Explorer integration
- Multi-account monitoring
- Real-time cloud monitoring
- Automated optimization actions

---

# Author

Prashant Bhardwaj

MCA Final Project – Cloud Cost Optimization System