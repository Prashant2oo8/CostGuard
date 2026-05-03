# CostGuard: Intelligent AWS Cost and Resource Optimization System

CostGuard is a full-stack AWS cloud cost and resource optimization system that analyzes cloud infrastructure, detects inefficiencies, and executes optimization actions to reduce unnecessary AWS expenditure.

The system identifies expensive and underutilized resources, evaluates usage patterns using CloudWatch metrics, generates intelligent optimization recommendations, and allows direct execution of cost-saving actions through an interactive dashboard.

Built using **Java, Spring Boot, React, and AWS SDK v2**, CostGuard functions as a practical cloud optimization platform similar to modern FinOps and cloud governance tools.

---

# Project Overview

Modern AWS environments often contain idle, underutilized, or misconfigured resources that silently increase cloud costs.

CostGuard addresses this problem by:

* Automatically discovering AWS resources across multiple services
* Estimating monthly infrastructure cost
* Analyzing utilization patterns using CloudWatch metrics
* Detecting inefficiencies, idle resources, and edge cases
* Calculating potential savings and efficiency score
* Providing intelligent optimization recommendations
* Executing optimization actions directly from the dashboard
* Reflecting post-action state and recommendation updates in real time

CostGuard is not just a reporting dashboard. It is a practical **AWS optimization execution console**.

---

# Technologies Used

## Frontend

* React
* JavaScript
* Recharts
* Custom Dashboard UI

## Backend

* Java 21
* Spring Boot
* Maven
* REST APIs

## AWS Integration

* AWS SDK v2
* Amazon EC2 (compute resources)
* Amazon CloudWatch (monitoring & utilization metrics)
* Amazon S3 (object storage analysis)
* Amazon EBS (block storage optimization)
* Amazon RDS (database optimization)
* Elastic Load Balancer (ELB)
* Auto Scaling Groups (ASG)
* Amazon VPC (network resource optimization)

---

# Core Features

## 1. Cloud Resource Discovery

CostGuard automatically discovers and analyzes AWS resources across services:

* EC2 Instances (virtual machines)
* EBS Volumes (block storage)
* S3 Buckets (object storage)
* RDS Instances (managed databases)
* Load Balancers (traffic distribution)
* Auto Scaling Groups (dynamic scaling infrastructure)
* VPC Resources (Elastic IPs, NAT Gateways, VPC Endpoints)

This provides a complete view of infrastructure usage and cloud cost exposure.

---

## 2. Cloud Cost Estimation

The system estimates monthly infrastructure cost using AWS usage and pricing logic.

Example:

EC2 Monthly Cost = Hourly Price × 730 hours

Cost estimation is supported for:

* EC2 (based on instance type and runtime)
* EBS (based on storage size and type)
* S3 (based on storage usage)
* RDS (based on database size and runtime)
* ELB / ASG (infrastructure cost awareness)

This enables service-level and resource-level cost visibility.

---

## 3. Service-Wise Cost Breakdown

CostGuard provides a clear distribution of cloud cost across services:

* EC2 → Compute cost
* EBS → Persistent storage cost
* S3 → Object storage cost
* RDS → Database cost
* ELB → Traffic handling cost
* Auto Scaling → Scaling overhead

This helps identify which AWS service contributes most to total cost.

---

## 4. Detailed Resource State Visibility

CostGuard shows exact resource distribution by real operational state instead of only generic totals.

Examples:

### EC2
* Running Instances
* Stopped Instances
* Idle Instances
* Optimized Instances

### EBS
* In-Use Volumes
* Available Volumes
* Snapshot Pending
* Optimized Volumes

### S3
* Standard Buckets
* IA Buckets
* Glacier Buckets
* Optimized Buckets

### RDS
* Running Databases
* Stopped Databases
* Idle Databases
* Optimized Databases

### ELB
* Active Load Balancers
* Inactive Load Balancers
* Deleted Load Balancers
* Optimized Load Balancers

### ASG
* Active Groups
* Scaled Down Groups
* Idle Groups
* Optimized Groups

### VPC
* Active Resources
* Idle Resources
* Optimized Resources

This gives users immediate operational visibility into infrastructure state.

---

## 5. EC2 Analysis (Compute Optimization)

CostGuard performs detailed EC2 analysis:

* Detects instance state (running / stopped)
* Fetches CPU utilization using CloudWatch
* Identifies underutilized and idle instances
* Provides recommendations such as:

    * Stop idle instances
    * Schedule instance stop
    * Schedule start/stop
    * Rightsize instance types

Special Case Handling:

* If instance is **stopped**, CPU metrics are unavailable → system avoids incorrect recommendations

---

## 6. EBS Analysis (Storage Optimization)

For EBS volumes:

* Calculates cost based on storage size
* Detects whether volume is:

    * Attached (in-use)
    * Detached (unused)

Recommendations include:

* Snapshot only
* Snapshot + delete
* Delete unused volume
* Optimize storage type if required

---

## 7. S3 Analysis (Object Storage Optimization)

CostGuard analyzes S3 buckets:

* Calculates storage size (GB)
* Estimates monthly cost
* Detects storage access patterns
* Identifies cost optimization opportunities

Recommendations:

* Move to Standard-IA
* Move to Glacier
* Apply lifecycle policies
* Delete unnecessary objects

---

## 8. RDS Analysis (Database Optimization)

For RDS:

* Detects presence and state of database instances
* Identifies idle database workloads
* Evaluates optimization opportunities

Recommendations:

* Stop idle database
* Schedule stop
* Reserved / rightsize recommendation

---

## 9. Load Balancer (ELB) Analysis

CostGuard checks for:

* Application / Network Load Balancers
* Usage activity and operational state
* Idle or inactive load balancers

Recommendations:

* Delete idle load balancer
* Keep active load balancer

---

## 10. Auto Scaling Group (ASG) Analysis

Analyzes scaling infrastructure:

* Detects configured Auto Scaling groups
* Identifies underutilized scaling capacity
* Evaluates scaling inefficiencies

Recommendations:

* Scale down desired capacity
* Reduce min/max capacity
* Schedule scale down

---

## 11. VPC Resource Analysis

CostGuard analyzes VPC-related resources for network waste:

* Elastic IPs
* NAT Gateways
* VPC Endpoints

Recommendations:

* Release unused Elastic IP
* Delete idle NAT Gateway
* Delete unused VPC Endpoint

---

## 12. Intelligent Optimization Recommendations

CostGuard uses context-aware logic to generate realistic optimization recommendations:

* Avoids false positives
* Handles edge cases like stopped resources
* Differentiates between active, idle, and optimized resources
* Provides actionable and service-aware recommendations

---

## 13. Multi-Action Optimization Support

CostGuard supports multiple valid optimization actions per resource.

Examples:

* EC2 → Stop / Schedule Stop
* EBS → Snapshot Only / Snapshot + Delete
* S3 → Standard-IA / Glacier
* ASG → Scale Down / Reduce Min-Max

Users can choose which optimization action they want to apply instead of being limited to one hardcoded action.

---

## 14. Optimization Execution Engine

CostGuard allows direct execution of optimization actions from the dashboard.

Execution flow:

* User selects optimization action
* Frontend sends execution request
* Backend validates request
* AWS SDK executes optimization
* UI refreshes with updated state and recommendation

This transforms CostGuard from a recommendation system into a true optimization execution platform.

---

## 15. Post-Action State Handling

After successful execution:

* Resource state updates correctly
* Recommendation updates
* Execute is hidden when no further action is needed
* Repeated invalid execution is prevented

This ensures accurate post-action UX and state consistency.

---

## 16. Post-Action Recommendation Updates

Recommendations are action-aware and state-aware.

Before execution:

* Idle workload detected — stop instance
* Rarely accessed objects — move to Glacier

After execution:

* Instance stopped successfully
* Lifecycle policy applied successfully
* No further action needed

---


## 17. Cloud Dashboard APIs

### Generate Cloud Report
`GET /cloud/report`

Returns the complete cloud optimization report including:
*   Summary metrics
*   Service-wise cost breakdown
*   Resource state distribution
*   Optimization recommendations
*   Optimization insights

### Execute Optimization Action
`POST /cloud/execute`

Executes optimization actions on AWS resources.

**Accepts:**
*   Resource type
*   Resource ID
*   Selected action

---

## 18. AWS SDK v2 Integration

CostGuard uses **AWS SDK for Java v2** to interact directly with AWS services for both analysis and optimization.

**Used for:**
*   Resource discovery
*   Cost analysis
*   CloudWatch metrics
*   Optimization execution

---

## 19. IAM-Aware Optimization Execution

CostGuard requires IAM permissions for both:
1.  **Resource analysis** (read)
2.  **Optimization execution** (write)

This ensures CostGuard can safely analyze and optimize AWS resources across supported services.

---

## 20. Structured Error Handling

CostGuard returns:
*   Clear backend responses
*   AWS authorization failures
*   Readable execution errors

This improves debugging and removes generic failures such as "Failed to fetch".

### Example API Response
```json
{
  "status": "success",
  "message": "Cloud report generated successfully",
  "data": {
    "summary": {
      "currentMonthlyCost": 8.25,
      "potentialSavings": 2.10,
      "optimizedMonthlyCost": 6.15,
      "savingsPercentage": 25.45,
      "efficiencyScore": 82
    },
    "serviceCostBreakdown": {
      "ec2": 7.592,
      "ebs": 0.64,
      "s3": 0.023,
      "rds": 0,
      "elb": 0,
      "autoscaling": 0
    },
    "optimizationInsights": [
      "Apply lifecycle policies to optimize S3 storage cost",
      "EC2 contributes highest cost - consider rightsizing instances"
    ]
  }
}
```
---

## Project Structure
*   **controller** → REST API endpoints
*   **service** → business logic and optimization engine
*   **model** → request / response structures
*   **frontend** → React optimization dashboard

---

## Major Improvements Implemented
*   Fixed broken POST execution flow
*   Fixed frontend fetch handling
*   Fixed CORS and browser execution issues
*   Fixed backend execute mapping
*   Fixed IAM write-permission failures
*   Added detailed resource state counts
*   Added richer optimization logic
*   Added multi-action support
*   Improved post-action state handling
*   Improved recommendation updates
*   Prevented repeated invalid executions
*   **Upgraded CostGuard from a static recommendation dashboard into a complete optimization execution console**

---

## Future Improvements
*   AWS Cost Explorer integration
*   Multi-account AWS monitoring
*   Scheduled optimization automation
*   Role-based access control
*   Approval workflow for destructive actions
*   Optimization history / audit logs
*   Predictive cost forecasting
*   ML-based optimization recommendations

---

## Author
**Prashant Bhardwaj**  
*MCA Major Project*
# CostGuard: Intelligent AWS Cost and Resource Optimization System



