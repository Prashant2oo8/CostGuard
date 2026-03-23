
# CostGuard – Intelligent AWS Cloud Cost Optimization System

CostGuard is a backend-based cloud cost optimization system that analyzes AWS resources and provides intelligent insights to reduce unnecessary cloud expenditure.

The system identifies expensive resources, detects inefficiencies, evaluates utilization patterns, and generates actionable recommendations to improve overall cloud efficiency.

Built using **Java, Spring Boot, and AWS SDK v2**, CostGuard simulates a real-world cloud cost management platform similar to tools like AWS Trusted Advisor.

---

# Project Overview

Modern cloud environments often contain idle, underutilized, or misconfigured resources that lead to unnecessary costs.

CostGuard addresses this problem by:

* Automatically discovering AWS resources across multiple services
* Estimating monthly cost of infrastructure
* Analyzing usage patterns using CloudWatch metrics
* Detecting inefficiencies and edge cases (e.g., stopped instances)
* Calculating potential savings and efficiency score
* Providing intelligent optimization recommendations

The system exposes a REST API that returns a **complete cloud cost optimization dashboard**, which can be consumed by a frontend UI.

---

# Technologies Used

## Backend

* Java 21
* Spring Boot
* Maven

## AWS Integration

* AWS SDK v2
* Amazon EC2 (compute resources)
* Amazon CloudWatch (monitoring & CPU utilization)
* Amazon S3 (storage analysis)
* Amazon EBS (block storage)
* Amazon RDS (database services)
* Elastic Load Balancer (ELB)
* Auto Scaling Groups (ASG)

---

# Core Features

## 1. Cloud Resource Discovery

CostGuard automatically fetches and analyzes AWS resources across services:

* EC2 Instances (virtual machines)
* EBS Volumes (block storage)
* S3 Buckets (object storage)
* RDS Instances (managed databases)
* Load Balancers (traffic distribution)
* Auto Scaling Groups (dynamic scaling infrastructure)

---

## 2. Cloud Cost Estimation

The system estimates monthly cost using standard AWS pricing models.

Example:

EC2 Monthly Cost = Hourly Price × 730 hours

Cost estimation is supported for:

* EC2 (based on instance type)
* EBS (based on storage size and type)
* S3 (based on storage usage)

---

## 3. Service-Wise Cost Breakdown

CostGuard provides a clear distribution of cloud cost across services:

* EC2 → Compute cost (usually high contributor in cost)
* EBS → Persistent storage cost
* S3 → Object storage cost
* RDS → Database cost (if available)
* ELB → Traffic handling cost
* Auto Scaling → Indirect scaling cost

This helps identify which service is contributing most to total cost.

---

## 4. EC2 Analysis (Compute Optimization)

CostGuard performs detailed EC2 analysis:

* Detects instance state (running / stopped)
* Fetches CPU utilization using CloudWatch
* Identifies underutilized instances
* Provides recommendations such as:

    * Stop idle instances
    * Rightsize instance types

Special Case Handling:

* If instance is **stopped**, CPU metrics are unavailable → system avoids incorrect recommendations

---

## 5. EBS Analysis (Storage Optimization)

For EBS volumes:

* Calculates cost based on storage size
* Detects whether volume is:

    * Attached (in-use)
    * Detached (unused)

Recommendations include:

* Delete unused volumes
* Take snapshot before deletion
* Optimize storage type if required

---

## 6. S3 Analysis (Object Storage Optimization)

CostGuard analyzes S3 buckets:

* Calculates storage size (GB)
* Estimates monthly cost
* Detects low or high usage patterns

Recommendations:

* Apply lifecycle policies
* Move data to cheaper storage classes (e.g., Glacier)
* Delete unnecessary objects

---

## 7. RDS Analysis (Database Services)

For RDS:

* Detects presence of database instances
* Provides status if no databases are found
* (Extensible for future cost estimation)

Current Behavior:

* If no RDS instances exist → returns:

    * Status: Not Initialized
    * Reason: No databases found

---

## 8. Load Balancer (ELB) Analysis

CostGuard checks for:

* Application / Network Load Balancers
* Usage presence in infrastructure

If not configured:

* Returns "Not Initialized" state

Future Scope:

* Cost estimation based on traffic
* Idle load balancer detection

---

## 9. Auto Scaling Group (ASG) Analysis

Analyzes scaling infrastructure:

* Detects configured Auto Scaling groups
* Identifies whether dynamic scaling is used

If no ASG exists:

* Marks service as "Not Initialized"

Future enhancements:

* Detect over-provisioning
* Optimize scaling policies

---

## 10. Expensive Resource Detection

Identifies top cost-contributing resources:

Example:

* High-cost EC2 instances
* Large EBS volumes

Helps prioritize optimization efforts.

---

## 11. Intelligent Waste Analysis

CostGuard uses context-aware logic to detect inefficiencies:

* Avoids false positives
* Handles edge cases like stopped instances
* Differentiates between active and idle resources

---

## 12. Optimization Insights

Provides actionable recommendations such as:

* Rightsizing EC2 instances
* Removing unused EBS volumes
* Applying S3 lifecycle policies
* Monitoring high-cost resources

---

# Cloud Dashboard API

Endpoint:

```
GET /api/cloud/report
```

This endpoint returns a complete cloud cost optimization report.

---

# Example API Response

```json
{
  "status": "success",
  "message": "Cloud report generated successfully",
  "data": {
    "summary": {
      "currentMonthlyCost": 8.25,
      "potentialSavings": 0,
      "optimizedMonthlyCost": 8.25,
      "savingsPercentage": 0,
      "efficiencyScore": 100
    },
    "serviceCostBreakdown": {
      "ec2": 7.592,
      "ebs": 0.64,
      "s3": 0.023,
      "rds": 0,
      "elb": 0,
      "autoscaling": 0
    },
    "topExpensiveResources": [
      {
        "name": "i-089b03f5af5f06fcc",
        "type": "EC2",
        "monthlyCost": 7.592
      }
    ],
    "topWastefulResources": [],
    "optimizationInsights": [
      "Apply lifecycle policies to optimize S3 storage cost",
      "EC2 contributes highest cost - consider rightsizing instances"
    ]
  }
}
```

---

# Project Structure

```
controller → REST API endpoints  
service → business logic and optimization engine  
model → data structures and response objects  
```

---

# Future Improvements

* Interactive Web Dashboard (Frontend UI)
* AWS Cost Explorer integration
* Multi-account monitoring support
* Real-time cost tracking
* Automated optimization actions (self-healing system)
* Machine Learning-based cost prediction

---

# Author

Prashant Bhardwaj 

MCA Major Project – Intelligent AWS Cloud Cost Optimization System

Project Title - CostGuard
