CostGuard: Intelligent AWS Cost and Resource Optimization System

CostGuard: Intelligent AWS Cost and Resource Optimization System is a full-stack cloud optimization platform designed to analyze AWS infrastructure, detect cost inefficiencies, and execute optimization actions directly from a centralized cloud operations dashboard.

CostGuard is built to solve a common cloud problem: organizations often pay for idle, underutilized, or misconfigured AWS resources without realizing how much cost is being wasted. CostGuard continuously analyzes cloud resources, estimates cost, detects optimization opportunities, and allows controlled execution of cost-saving actions from a single interface.

Unlike static reporting tools, CostGuard is designed as an intelligent AWS cost and resource optimization system that not only identifies waste but also enables direct optimization execution across multiple AWS services.

Built using Java, Spring Boot, React, and AWS SDK v2, CostGuard combines cloud analysis, cost intelligence, and execution workflows into a practical FinOps-style optimization console.

Project Overview

Modern AWS environments often contain:

idle EC2 instances,
unattached EBS volumes,
underutilized storage,
inactive load balancers,
oversized scaling groups,
and unused VPC networking resources.

These resources silently increase monthly cloud spend and reduce infrastructure efficiency.

CostGuard addresses this by providing a complete optimization workflow:

automatically discover AWS resources,
estimate infrastructure cost,
analyze utilization patterns using CloudWatch,
detect inefficiencies and waste,
generate optimization recommendations,
execute optimization actions,
and reflect post-action changes in real time.

CostGuard is not just a reporting dashboard.
It is a practical AWS optimization execution console.

Core Objective

The primary goal of CostGuard: Intelligent AWS Cost and Resource Optimization System is to reduce unnecessary AWS expenditure by turning cloud cost visibility into actionable optimization decisions.

The system is designed to:

discover cloud resources across AWS services,
estimate service-wise and resource-wise monthly cost,
identify underutilized or idle infrastructure,
generate optimization recommendations,
allow direct execution of optimization actions,
and improve overall cloud efficiency.
Technology Stack
Frontend
React
JavaScript
Recharts
Custom dashboard UI
Backend
Java 21
Spring Boot
Maven
REST APIs
AWS Integration
AWS SDK for Java v2
Amazon EC2
Amazon EBS
Amazon S3
Amazon RDS
Elastic Load Balancer (ELB)
Auto Scaling Groups (ASG)
Amazon VPC
Amazon CloudWatch
System Architecture
Frontend (React Optimization Dashboard)
        ↓
Spring Boot Backend (Optimization Engine)
        ↓
AWS SDK v2 Clients
        ↓
AWS Services (EC2, EBS, S3, RDS, ELB, ASG, VPC, CloudWatch)
Key Features
1. Intelligent Cloud Resource Discovery

CostGuard automatically discovers and analyzes AWS resources across supported services:

EC2 Instances
EBS Volumes
S3 Buckets
RDS Databases
Load Balancers
Auto Scaling Groups
VPC Resources

This provides a complete view of infrastructure usage and cost exposure.

2. Cloud Cost Estimation Engine

CostGuard estimates monthly infrastructure cost using AWS resource usage and pricing logic.

Examples:

EC2 cost based on instance type and runtime
EBS cost based on volume type and size
S3 cost based on storage usage
RDS, ELB, and ASG cost awareness

This enables accurate service-level and resource-level cost visibility.

3. Service-Wise Cost Breakdown

CostGuard breaks total monthly cost into service-level contributions:

EC2 → Compute cost
EBS → Storage cost
S3 → Object storage cost
RDS → Database cost
ELB → Load balancing cost
ASG → Scaling overhead

This helps identify which AWS service contributes most to monthly spend.

4. Detailed Resource State Visibility

A major improvement in CostGuard is real operational state visibility.

Instead of only generic totals, CostGuard shows exact resource distribution by real state for each service.

Examples:

EC2
Running Instances
Stopped Instances
Idle Instances
Optimized Instances
EBS
In-Use Volumes
Available Volumes
Snapshot Pending
Optimized Volumes
S3
Standard Buckets
IA Buckets
Glacier Buckets
Optimized Buckets
RDS
Running Databases
Stopped Databases
Idle Databases
Optimized Databases
ELB
Active Load Balancers
Inactive Load Balancers
Deleted Load Balancers
Optimized Load Balancers
ASG
Active Groups
Scaled Down Groups
Idle Groups
Optimized Groups
VPC
Active Resources
Idle Resources
Optimized Resources

This gives users immediate operational visibility into infrastructure state.

5. Intelligent Optimization Recommendations

CostGuard generates service-specific optimization recommendations based on resource usage, utilization patterns, and waste signals.

Examples:

stop idle EC2 instances,
snapshot and delete unused EBS volumes,
move infrequently accessed S3 data to Glacier,
stop idle RDS databases,
delete inactive load balancers,
scale down underutilized ASGs,
release unused VPC resources.

Recommendations are context-aware and avoid false positives where possible.

6. Multi-Action Optimization Support

A major improvement in CostGuard was expanding optimization logic beyond one hardcoded action.

CostGuard now supports richer optimization choices for each service.

Examples:

EC2
Stop Instance
Schedule Stop
Schedule Start/Stop
Rightsize Recommendation
EBS
Snapshot Only
Snapshot + Delete
Delete Unused Volume
S3
Move to Standard-IA
Move to Glacier
Apply Lifecycle Policy
RDS
Stop Database
Schedule Stop
Reserved / Rightsize Recommendation
ELB
Delete Idle Load Balancer
Keep Load Balancer
ASG
Scale Down Desired Capacity
Reduce Min/Max Capacity
Schedule Scale Down
VPC
Release Elastic IP
Delete NAT Gateway
Delete Unused VPC Endpoint

This makes CostGuard much more realistic and operationally useful.

7. Action Selection Support

Resources that support multiple optimization paths now allow action selection.

Instead of forcing one hardcoded optimization action, users can choose which optimization strategy they want to execute.

Examples:

Snapshot only vs Snapshot + Delete
Move to IA vs Move to Glacier
Scale Down vs Reduce Min/Max

This gives users more control over optimization behavior.

8. Optimization Execution Engine

CostGuard does not stop at recommendations.
It allows direct execution of optimization actions from the dashboard.

Execution flow:

user selects action,
frontend sends execution request,
backend validates request,
AWS SDK executes optimization,
UI refreshes with updated state.

This transforms CostGuard from a recommendation system into a true optimization execution platform.

9. Post-Action State Handling

A major improvement made during development was fixing post-action behavior.

Earlier:

Execute button reappeared,
recommendation remained stale,
UI still behaved as if optimization was pending.

Now after successful execution:

state updates correctly,
recommendation updates,
Execute is hidden when no further action is needed,
repeated invalid execution is prevented.

This significantly improves operational correctness.

10. Post-Action Recommendation Updates

Recommendations are now action-aware and state-aware.

Before execution:

“Idle workload detected — stop instance”
“Rarely accessed objects — move to Glacier”

After execution:

“Instance stopped successfully”
“Lifecycle policy applied successfully”
“No further action needed”

This keeps UI recommendations aligned with real infrastructure state.

11. Consistent Execution Lifecycle

CostGuard now follows a consistent execution lifecycle:

actionable
executing
executed
optimized
failed

This controls:

action visibility,
retry behavior,
status rendering,
post-action UX.
12. Cloud Report API
GET /cloud/report

Generates the complete cloud optimization report.

Returns:

summary metrics,
cost breakdown,
resource lists,
optimization recommendations,
optimization insights.

This powers the dashboard.

13. Optimization Execute API
POST /cloud/execute

Executes optimization actions on AWS resources.

Accepts:

resource type,
resource id,
selected action.

This powers direct optimization execution from the dashboard.

14. AWS SDK v2 Integration

CostGuard uses AWS SDK for Java v2 to interact directly with AWS services for both analysis and optimization.

Used for:

resource discovery,
cost analysis,
optimization execution.

This enables direct cloud control through official AWS APIs.

15. IAM-Aware Optimization Execution

One major backend improvement was fixing IAM write-permission issues.

Initially:

report generation worked,
execution failed.

Root cause:

IAM had read permissions,
but lacked write permissions.

This was resolved by enabling proper IAM permissions for optimization actions across supported AWS services.

16. Structured Error Handling

Error handling was improved to return:

clear backend responses,
AWS authorization failures,
readable execution errors.

This replaced generic frontend failures such as:

Failed to fetch

and made debugging significantly easier.

REST API Endpoints
Generate Cloud Report
GET /cloud/report
Execute Optimization Action
POST /cloud/execute
Project Structure
controller → REST APIs
service    → optimization engine and AWS execution logic
model      → request/response structures
frontend   → React optimization dashboard
Major Improvements Implemented

During development, CostGuard was significantly improved by:

fixing broken POST execution flow,
fixing frontend fetch handling,
fixing CORS and browser execution issues,
fixing backend execute mapping,
fixing IAM write-permission failures,
improving state-aware UI,
adding detailed resource state counts,
adding richer optimization logic,
adding multi-action support,
improving post-action state handling,
improving recommendation updates,
preventing repeated invalid executions,
transforming CostGuard from a static recommendation dashboard into a complete optimization execution console.
Future Scope
AWS Cost Explorer integration
Multi-account AWS monitoring
Scheduled optimization automation
Role-based access control
Approval workflow for destructive actions
Optimization history / audit logs
Predictive cost forecasting
ML-based optimization recommendations
Author

Prashant Bhardwaj
MCA Major Project

Project Title:
CostGuard: Intelligent AWS Cost and Resource Optimization System
