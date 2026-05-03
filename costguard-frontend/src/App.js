import { useState, useEffect, useCallback, useMemo } from "react";
import {
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from "recharts";

const REPORT_API = "http://localhost:8080/cloud/report";
const EXECUTE_API = "http://localhost:8080/cloud/execute";

const MOCK = {
  status: "success",
  message: "Cloud report generated successfully",
  data: {
    summary: {
      currentMonthlyCost: 8.255,
      potentialSavings: 0.82,
      optimizedMonthlyCost: 7.435,
      savingsPercentage: 9.93,
      efficiencyScore: 90,
    },
    serviceCostBreakdown: { ebs: 0.64, ec2: 7.592, s3: 0.023, rds: 0, elb: 0, autoscaling: 0, vpc: 0 },
    topExpensiveResources: [
      { name: "i-089b03f5af5f06fcc", type: "EC2", monthlyCost: 7.592 },
      { name: "vol-0d3fab40db3f12fb9", type: "EBS", monthlyCost: 0.64 },
      { name: "costguard-demo", type: "S3", monthlyCost: 0.023 },
    ],
    ec2Instances: [
      {
        instanceId: "i-089b03f5af5f06fcc",
        instanceType: "t3.micro",
        state: "stopped",
        monthlyCost: 7.592,
        optimizedCost: 0,
        savings: 7.592,
        cpuUtilization: 0,
        recommendation: "Instance is stopped - no CPU data available",
      },
    ],
    ebsVolumes: [
      {
        volumeId: "vol-0d3fab40db3f12fb9",
        size: 8,
        state: "in-use",
        volumeType: "gp3",
        monthlyCost: 0.64,
        optimizedCost: 0.4,
        savings: 0.24,
        recommendation: "Attached to stopped instance - if used regularly no action needed, otherwise consider snapshot and delete",
      },
    ],
    s3Buckets: [
      {
        bucketName: "costguard-demo",
        storageGB: 1,
        monthlyCost: 0.023,
        optimizedCost: 0.008,
        savings: 0.015,
        recommendation: "Very low storage - minimal cost",
      },
    ],
    rds: { status: "Not Initialized", reason: "No RDS databases found" },
    elb: { status: "Not Initialized", reason: "No Load Balancers found" },
    autoscaling: { status: "Not Initialized", reason: "No Auto Scaling Groups configured" },
    optimizationInsights: [
      "Apply lifecycle policies to optimize S3 storage cost",
      "EC2 contributes highest cost - consider rightsizing instances",
    ],
  },
};

const THEMES = {
  dark: {
    bg: "#0f1117",
    surface: "#161b27",
    border: "#1f2937",
    text: "#f1f5f9",
    muted: "#94a3b8",
    accent: "#6366f1",
    green: "#10b981",
    red: "#ef4444",
    yellow: "#f59e0b",
    cyan: "#06b6d4",
    cardBg: "#121825",
    insightBg: "#6366f110",
    insightBorder: "#6366f130",
    insightText: "#c7d2fe",
  },
  light: {
    bg: "#f1f5f9",
    surface: "#ffffff",
    border: "#e2e8f0",
    text: "#0f172a",
    muted: "#64748b",
    accent: "#4f46e5",
    green: "#059669",
    red: "#dc2626",
    yellow: "#d97706",
    cyan: "#0891b2",
    cardBg: "#f8fafc",
    insightBg: "#eef2ff",
    insightBorder: "#c7d2fe",
    insightText: "#3730a3",
  },
};

const COLORS = ["#6366f1", "#06b6d4", "#f59e0b", "#10b981", "#ef4444", "#a78bfa", "#22c55e"];

const nav = [
  { id: "dashboard", icon: "⬛", label: "Dashboard" },
  { id: "ec2", icon: "🖥️", label: "EC2" },
  { id: "ebs", icon: "💾", label: "EBS" },
  { id: "s3", icon: "🗄️", label: "S3" },
  { id: "rds", icon: "🛢️", label: "RDS" },
  { id: "elb", icon: "⚖️", label: "ELB" },
  { id: "asg", icon: "🔄", label: "ASG" },
  { id: "vpc", icon: "🌐", label: "VPC" },
];

const fmtMoney = (n) => `$${Number(n || 0).toFixed(2)}`;
const safeArray = (v) => (Array.isArray(v) ? v : []);
const stateKey = (s = "") => s.toLowerCase().replaceAll("_", "-");

const titleCase = (value = "") =>
  value
    .toString()
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/\b\w/g, (s) => s.toUpperCase());

const stateColor = (state, C) => {
  const key = stateKey(state);
  return ({
    running: C.green,
    active: C.green,
    "in-use": C.accent,
    available: C.cyan,
    stopped: C.red,
    idle: C.yellow,
    optimized: C.green,
    actionable: C.yellow,
    executed: C.green,
    failed: C.red,
    deleted: C.red,
    retained: C.cyan,
    scheduled: C.cyan,
    snapshotted: C.cyan,
    glacier: C.cyan,
    "standard-ia": C.cyan,
  }[key] || C.muted);
};

const Badge = ({ s, C }) => (
  <span
    style={{
      background: `${stateColor(s, C)}22`,
      color: stateColor(s, C),
      border: `1px solid ${stateColor(s, C)}44`,
      borderRadius: 6,
      padding: "2px 8px",
      fontSize: 11,
      fontWeight: 700,
      whiteSpace: "nowrap",
    }}
  >
    {titleCase(s)}
  </span>
);

const Stat = ({ label, value, color, icon, C }) => (
  <div
    style={{
      background: C.surface,
      border: `1px solid ${C.border}`,
      borderRadius: 14,
      padding: "20px 24px",
      flex: 1,
      minWidth: 160,
    }}
  >
    <div style={{ color: C.muted, fontSize: 11, fontWeight: 700, textTransform: "uppercase", letterSpacing: 1, marginBottom: 10 }}>
      {icon} {label}
    </div>
    <div style={{ fontSize: 28, fontWeight: 800, color: color || C.accent }}>{value}</div>
  </div>
);

const Section = ({ title, children, C }) => (
  <div style={{ marginBottom: 28 }}>
    <div style={{ fontSize: 14, fontWeight: 700, color: C.text, marginBottom: 14, paddingBottom: 10, borderBottom: `1px solid ${C.border}` }}>{title}</div>
    {children}
  </div>
);

const Empty = ({ icon, msg, C }) => (
  <div style={{ textAlign: "center", padding: "48px 20px", color: C.muted }}>
    <div style={{ fontSize: 36, marginBottom: 10 }}>{icon}</div>
    <div style={{ fontSize: 14 }}>{msg}</div>
  </div>
);

const TH = ({ children, C }) => (
  <th style={{ textAlign: "left", padding: "10px 14px", color: C.muted, fontSize: 11, fontWeight: 600, textTransform: "uppercase", letterSpacing: 0.6, borderBottom: `1px solid ${C.border}` }}>
    {children}
  </th>
);

const TD = ({ children, mono, color, C }) => (
  <td style={{ padding: "13px 14px", fontSize: 13, color: color || C.text, fontFamily: mono ? "monospace" : "inherit", borderBottom: `1px solid ${C.border}40`, verticalAlign: "top" }}>{children}</td>
);

function DashboardPage({ d, C }) {
  const breakdown = Object.entries(d.serviceCostBreakdown || {})
    .filter(([, v]) => Number(v) > 0)
    .map(([k, v]) => ({ name: k.toUpperCase(), value: Number(v.toFixed(2)) }));

  const trend = useMemo(() => {
    const current = Number(d?.summary?.currentMonthlyCost || 0);
    const optimized = Number(d?.summary?.optimizedMonthlyCost || current);
    const spread = current - optimized;
    return Array.from({ length: 12 }, (_, i) => {
      const step = (i + 1) / 12;
      return {
        month: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"][i],
        cost: Number((optimized + spread * (0.4 + step * 0.6)).toFixed(2)),
      };
    });
  }, [d]);

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <div style={{ fontSize: 22, fontWeight: 800, color: C.text }}>Dashboard Overview</div>
        <div style={{ color: C.muted, fontSize: 13, marginTop: 4 }}>Real-time cloud cost & efficiency summary</div>
      </div>
      <div style={{ display: "flex", gap: 14, flexWrap: "wrap", marginBottom: 28 }}>
        <Stat C={C} icon="💰" label="Monthly Cost" value={fmtMoney(d.summary.currentMonthlyCost)} color={C.accent} />
        <Stat C={C} icon="✨" label="Potential Savings" value={fmtMoney(d.summary.potentialSavings)} color={C.green} />
        <Stat C={C} icon="📉" label="Optimized Cost" value={fmtMoney(d.summary.optimizedMonthlyCost)} color={C.cyan} />
        <Stat C={C} icon="🎯" label="Efficiency Score" value={`${Number(d.summary.efficiencyScore || 0).toFixed(0)}%`} color={C.yellow} />
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1.4fr 1fr", gap: 20, marginBottom: 28 }}>
        <div style={{ background: C.surface, border: `1px solid ${C.border}`, borderRadius: 14, padding: 24 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 16 }}>Cost Trend</div>
          <ResponsiveContainer width="100%" height={180}>
            <AreaChart data={trend}>
              <defs>
                <linearGradient id="cg" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={C.accent} stopOpacity={0.3} />
                  <stop offset="95%" stopColor={C.accent} stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke={C.border} />
              <XAxis dataKey="month" tick={{ fill: C.muted, fontSize: 10 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: C.muted, fontSize: 10 }} axisLine={false} tickLine={false} tickFormatter={(v) => `$${v}`} />
              <Tooltip formatter={(v) => [fmtMoney(v), "Cost"]} contentStyle={{ background: C.surface, border: `1px solid ${C.border}`, borderRadius: 8, fontSize: 12, color: C.text }} />
              <Area type="monotone" dataKey="cost" stroke={C.accent} strokeWidth={2} fill="url(#cg)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div style={{ background: C.surface, border: `1px solid ${C.border}`, borderRadius: 14, padding: 24 }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: C.text, marginBottom: 16 }}>Cost by Service</div>
          <ResponsiveContainer width="100%" height={130}>
            <PieChart>
              <Pie data={breakdown} cx="50%" cy="50%" innerRadius={40} outerRadius={62} dataKey="value" paddingAngle={4}>
                {breakdown.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip formatter={(v) => fmtMoney(v)} contentStyle={{ background: C.surface, border: `1px solid ${C.border}`, borderRadius: 8, fontSize: 12, color: C.text }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      <Section title="Optimization Insights" C={C}>
        {safeArray(d.optimizationInsights).map((text, i) => (
          <div key={i} style={{ display: "flex", gap: 12, alignItems: "flex-start", background: C.insightBg, border: `1px solid ${C.insightBorder}`, borderRadius: 10, padding: "12px 16px", marginBottom: 8 }}>
            <span>💡</span>
            <span style={{ color: C.insightText, fontSize: 13, lineHeight: 1.6 }}>{text}</span>
          </div>
        ))}
      </Section>
    </div>
  );
}

function getRowsForResource(page, data) {
  if (page === "ec2") {
    return safeArray(data.ec2Instances).map((row) => {
      const state = stateKey(row.state || "unknown");
      const isRunning = state === "running";
      const isStopped = state === "stopped";
      const rec = row.recommendation || (isRunning ? "Idle workload detected — stop instance" : "No further action needed");

      return {
        id: row.instanceId,
        currentCost: row.monthlyCost,
        optimizedCost: row.optimizedCost ?? row.monthlyCost,
        savings: row.savings ?? Math.max(0, (row.monthlyCost || 0) - (row.optimizedCost ?? row.monthlyCost ?? 0)),
        state,
        recommendation: rec,
        action: isRunning ? "STOP" : null,
        availableActions: isRunning ? ["STOP", "SCHEDULE_STOP", "SCHEDULE_START_STOP", "RIGHTSIZE_RECOMMEND"] : [],
        executionState: isStopped ? "optimized" : (isRunning ? "actionable" : "idle"),
        meta: row.instanceType,
      };
    });
  }

  if (page === "ebs") {
    return safeArray(data.ebsVolumes).map((row) => {
      const state = stateKey(row.state || "unknown");
      const isAvailable = state === "available";
      return {
        id: row.volumeId,
        currentCost: row.monthlyCost,
        optimizedCost: row.optimizedCost ?? row.monthlyCost,
        savings: row.savings ?? 0,
        state,
        recommendation: row.recommendation || (isAvailable ? "Unused volume detected — snapshot or delete" : "Volume attached/in-use"),
        action: isAvailable ? "SNAPSHOT" : null,
        availableActions: isAvailable ? ["SNAPSHOT", "SNAPSHOT_DELETE", "DELETE"] : [],
        executionState: isAvailable ? "actionable" : "in-use",
        meta: row.volumeType,
      };
    });
  }

  if (page === "s3") {
    return safeArray(data.s3Buckets).map((row) => {
      const state = stateKey(row.state || "standard");
      const hasCost = Number(row.monthlyCost || 0) > 0;
      return {
        id: row.bucketName,
        currentCost: row.monthlyCost,
        optimizedCost: row.optimizedCost ?? row.monthlyCost,
        savings: row.savings ?? 0,
        state,
        recommendation: row.recommendation || (hasCost ? "Rarely accessed objects — move to colder storage" : "No further action needed"),
        action: hasCost ? "MOVE_TO_GLACIER" : null,
        availableActions: hasCost ? ["MOVE_TO_STANDARD_IA", "MOVE_TO_GLACIER", "APPLY_LIFECYCLE_POLICY"] : [],
        executionState: hasCost ? "actionable" : "optimized",
        meta: `${row.storageGB} GB`,
      };
    });
  }

  if (page === "rds") {
    const dbs = safeArray(data?.rds?.data?.databases);
    return dbs.map((row) => {
      const state = stateKey(row.state || "available");
      const idle = /idle|stop/i.test(row.recommendation || "");
      return {
        id: row.dbIdentifier,
        currentCost: row.monthlyCost,
        optimizedCost: row.optimizedCost ?? row.monthlyCost,
        savings: row.savings ?? 0,
        state,
        recommendation: row.recommendation || (idle ? "Idle DB detected — stop database" : "No immediate optimization needed"),
        action: idle ? "STOP" : null,
        availableActions: idle ? ["STOP", "SCHEDULE_STOP", "RIGHTSIZE_RECOMMEND", "RESERVED_RECOMMEND"] : [],
        executionState: idle ? "actionable" : "optimized",
        meta: `${row.engine} • ${row.instanceClass}`,
      };
    });
  }

  if (page === "elb") {
    const lbs = safeArray(data?.elb?.data?.loadBalancers);
    return lbs.map((row) => {
      const state = stateKey(row.state || "active");
      const deletable = row.name?.startsWith("arn:");
      return {
        id: row.name,
        currentCost: row.monthlyCost,
        optimizedCost: row.optimizedCost ?? row.monthlyCost,
        savings: row.savings ?? 0,
        state,
        recommendation: row.recommendation || (deletable ? "Idle LB detected — delete or keep" : "No further action needed"),
        action: deletable ? "DELETE" : null,
        availableActions: deletable ? ["DELETE", "KEEP"] : [],
        executionState: deletable ? "actionable" : "optimized",
        meta: `${row.type} • ${row.state}`,
      };
    });
  }

  if (page === "asg") {
    const groups = safeArray(data?.autoscaling?.data?.groups);
    return groups.map((row) => {
      const state = stateKey(row.state || "active");
      const actionable = /reduce|idle|scale/i.test(row.recommendation || "");
      return {
        id: row.name,
        currentCost: row.monthlyCost,
        optimizedCost: row.optimizedCost ?? row.monthlyCost,
        savings: row.savings ?? 0,
        state,
        recommendation: row.recommendation || (actionable ? "Underutilized ASG — scale down or reduce min/max" : "No immediate optimization needed"),
        action: actionable ? "SCALE_DOWN" : null,
        availableActions: actionable ? ["SCALE_DOWN", "REDUCE_MIN_MAX", "SCHEDULE_SCALE_DOWN"] : [],
        executionState: actionable ? "actionable" : "optimized",
        meta: `Desired ${row.desiredCapacity} (min ${row.minSize}, max ${row.maxSize})`,
      };
    });
  }

  if (page === "vpc") {
    const vpcData = safeArray(data.vpcOptimizations);
    if (vpcData.length > 0) {
      return vpcData.map((row) => ({
        id: row.resourceId,
        currentCost: row.currentCost,
        optimizedCost: row.optimizedCost,
        savings: row.savings,
        state: stateKey(row.state || "idle"),
        recommendation: row.recommendation || "Unused VPC resource detected",
        action: row.action && row.action !== "RECOMMEND_ONLY" ? row.action : null,
        availableActions: row.action && row.action !== "RECOMMEND_ONLY" ? [row.action] : [],
        executionState: row.action && row.action !== "RECOMMEND_ONLY" ? "actionable" : "optimized",
        vpcResourceType: row.resourceType,
        meta: titleCase(row.resourceType),
      }));
    }

    return [
      {
        id: "eip-unassociated-001",
        currentCost: 3.6,
        optimizedCost: 0,
        savings: 3.6,
        state: "idle",
        recommendation: "Unused Elastic IP detected",
        action: "RELEASE_EIP",
        availableActions: ["RELEASE_EIP"],
        executionState: "actionable",
        vpcResourceType: "EIP",
        meta: "Elastic IP",
      },
      {
        id: "nat-001",
        currentCost: 32,
        optimizedCost: 0,
        savings: 32,
        state: "idle",
        recommendation: "Idle NAT Gateway detected",
        action: "DELETE_NAT",
        availableActions: ["DELETE_NAT"],
        executionState: "actionable",
        vpcResourceType: "NAT_GATEWAY",
        meta: "NAT Gateway",
      },
      {
        id: "vpce-001",
        currentCost: 7.2,
        optimizedCost: 0,
        savings: 7.2,
        state: "idle",
        recommendation: "Unused VPC Endpoint detected",
        action: "DELETE_ENDPOINT",
        availableActions: ["DELETE_ENDPOINT"],
        executionState: "actionable",
        vpcResourceType: "VPC_ENDPOINT",
        meta: "VPC Endpoint",
      },
    ];
  }

  return [];
}

function actionToPostState(action) {
  const key = (action || "").toUpperCase();
  if (key === "STOP") return "stopped";
  if (key === "SCHEDULE_STOP" || key === "SCHEDULE_START_STOP" || key === "SCHEDULE_SCALE_DOWN") return "scheduled";
  if (key === "SNAPSHOT") return "snapshotted";
  if (key === "SNAPSHOT_DELETE" || key === "DELETE") return "deleted";
  if (key === "MOVE_TO_GLACIER") return "glacier";
  if (key === "MOVE_TO_STANDARD_IA") return "standard-ia";
  if (key === "APPLY_LIFECYCLE_POLICY") return "lifecycle-applied";
  if (key === "SCALE_DOWN") return "scaled-down";
  if (key === "REDUCE_MIN_MAX") return "reduced";
  if (key === "KEEP") return "retained";
  if (key === "RELEASE_EIP") return "released";
  if (key === "DELETE_NAT" || key === "DELETE_ENDPOINT") return "deleted";
  return "optimized";
}

function postActionMessage(action) {
  const key = (action || "").toUpperCase();
  const map = {
    STOP: "Instance stopped successfully",
    SCHEDULE_STOP: "Stop schedule saved successfully",
    SCHEDULE_START_STOP: "Start/Stop schedule saved successfully",
    RIGHTSIZE_RECOMMEND: "Rightsize recommendation recorded",
    SNAPSHOT: "Snapshot created successfully",
    SNAPSHOT_DELETE: "Snapshot created and delete executed successfully",
    DELETE: "Delete action executed successfully",
    MOVE_TO_GLACIER: "Bucket transitioned to Glacier successfully",
    MOVE_TO_STANDARD_IA: "Bucket transitioned to Standard-IA successfully",
    APPLY_LIFECYCLE_POLICY: "Lifecycle policy applied successfully",
    RESERVED_RECOMMEND: "Reserved recommendation recorded",
    KEEP: "Resource marked as keep",
    SCALE_DOWN: "Scaled down successfully",
    REDUCE_MIN_MAX: "Min/Max reduced successfully",
    SCHEDULE_SCALE_DOWN: "Scale-down schedule saved successfully",
    RELEASE_EIP: "Elastic IP released successfully",
    DELETE_NAT: "NAT Gateway delete initiated successfully",
    DELETE_ENDPOINT: "VPC Endpoint delete initiated successfully",
  };
  return map[key] || `${action} applied successfully`;
}

function ResourcePage({ page, title, icon, rows, C, onExecute, executingId, toast, actionSelections, onActionSelect, executionResults }) {
  const serviceStates = {
    ec2: ["running", "stopped", "idle", "optimized"],
    ebs: ["in-use", "available", "snapshot-pending", "optimized"],
    s3: ["standard", "standard-ia", "glacier", "optimized"],
    rds: ["available", "stopped", "idle", "optimized"],
    elb: ["active", "inactive", "deleted", "optimized"],
    asg: ["active", "scaled-down", "idle", "optimized"],
    vpc: ["active", "idle", "optimized"],
  };

  const stateCounts = rows.reduce((acc, row) => {
    const lifecycle = executionResults[row.id]?.state;
    const effective = stateKey(lifecycle || row.state || row.executionState || (row.action ? "idle" : "optimized"));
    acc[effective] = (acc[effective] || 0) + 1;
    return acc;
  }, {});

  const totals = rows.reduce(
    (acc, row) => {
      acc.current += Number(row.currentCost || 0);
      acc.optimized += Number(row.optimizedCost ?? row.currentCost ?? 0);
      acc.savings += Number(row.savings ?? Math.max(0, (row.currentCost || 0) - (row.optimizedCost ?? row.currentCost ?? 0)));
      return acc;
    },
    { current: 0, optimized: 0, savings: 0 }
  );

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <div style={{ fontSize: 22, fontWeight: 800, color: C.text }}>{icon} {title}</div>
        <div style={{ color: C.muted, fontSize: 13, marginTop: 4 }}>Resource cost analysis, recommendation, and DSS execution</div>
      </div>

      <div style={{ display: "flex", gap: 14, flexWrap: "wrap", marginBottom: 28 }}>
        <Stat C={C} icon="💰" label="Total Cost" value={fmtMoney(totals.current)} color={C.accent} />
        <Stat C={C} icon="📉" label="Optimized Cost" value={fmtMoney(totals.optimized)} color={C.cyan} />
        <Stat C={C} icon="✨" label="Savings" value={fmtMoney(totals.savings)} color={C.green} />
      </div>

      <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginBottom: 18 }}>
        {(serviceStates[page] || Object.keys(stateCounts)).map((k) => (
          <div key={k} style={{ background: C.cardBg, border: `1px solid ${C.border}`, borderRadius: 8, padding: "8px 12px", fontSize: 12, color: C.muted }}>
            <span style={{ color: C.text, fontWeight: 700 }}>{titleCase(k)}</span>: {stateCounts[k] || 0}
          </div>
        ))}
      </div>

      {toast && (
        <div style={{ background: `${toast.type === "success" ? C.green : C.red}18`, border: `1px solid ${toast.type === "success" ? C.green : C.red}55`, borderRadius: 10, padding: 12, marginBottom: 16, fontSize: 13, color: toast.type === "success" ? C.green : C.red }}>
          {toast.message}
        </div>
      )}

      <div style={{ background: C.surface, border: `1px solid ${C.border}`, borderRadius: 14, overflow: "hidden" }}>
        {rows.length === 0 ? (
          <Empty icon={icon} msg={`No ${title} resources found`} C={C} />
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                {["ID", "Current Cost", "Optimized Cost", "Savings", "State", "Recommendation", "Action"].map((h) => (
                  <TH key={h} C={C}>{h}</TH>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => {
                const selected = actionSelections[row.id] || row.action;
                const result = executionResults[row.id];
                const hasApplied = result?.status === "executed";
                const isRecOnly = selected?.includes("RECOMMEND");

                return (
                  <tr key={row.id}>
                    <TD C={C} mono color={C.accent}>
                      <div>{row.id}</div>
                      {row.meta ? <div style={{ fontSize: 11, color: C.muted, marginTop: 2 }}>{row.meta}</div> : null}
                    </TD>
                    <TD C={C} color={C.yellow}>{fmtMoney(row.currentCost)}</TD>
                    <TD C={C} color={C.cyan}>{fmtMoney(row.optimizedCost)}</TD>
                    <TD C={C} color={C.green}>{fmtMoney(row.savings)}</TD>
                    <TD C={C}><Badge s={result?.state || row.state || row.executionState || "unknown"} C={C} /></TD>
                    <TD C={C} color={C.muted}>{result?.recommendation || row.recommendation || "No recommendation"}</TD>
                    <TD C={C}>
                      {hasApplied ? (
                        <span style={{ color: C.green, fontSize: 12, fontWeight: 700 }}>Action Applied</span>
                      ) : row.action ? (
                        <>
                          {row.availableActions?.length > 1 && (
                            <select
                              value={selected}
                              onChange={(e) => onActionSelect(row.id, e.target.value)}
                              style={{ marginRight: 8, borderRadius: 6, background: C.cardBg, color: C.text, border: `1px solid ${C.border}`, padding: "6px 8px", fontSize: 12 }}
                            >
                              {row.availableActions.map((a) => <option key={a} value={a}>{a}</option>)}
                            </select>
                          )}

                          {isRecOnly ? (
                            <span style={{ color: C.muted, fontSize: 12, fontWeight: 600 }}>Recommendation Only</span>
                          ) : (
                            <button
                              onClick={() => onExecute(page, row, selected)}
                              disabled={executingId === row.id}
                              style={{
                                background: C.accent,
                                color: "#fff",
                                border: "none",
                                borderRadius: 8,
                                padding: "8px 12px",
                                fontSize: 12,
                                fontWeight: 700,
                                cursor: "pointer",
                                opacity: executingId === row.id ? 0.6 : 1,
                              }}
                            >
                              {executingId === row.id ? "Executing..." : "Execute"}
                            </button>
                          )}
                        </>
                      ) : (
                        <span style={{ color: C.muted, fontSize: 12, fontWeight: 600 }}>No Action Needed</span>
                      )}
                    </TD>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default function App() {
  const [page, setPage] = useState("dashboard");
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [dark, setDark] = useState(true);
  const [executingId, setExecutingId] = useState(null);
  const [toast, setToast] = useState(null);
  const [actionSelections, setActionSelections] = useState({});
  const [executionResults, setExecutionResults] = useState({});

  const C = THEMES[dark ? "dark" : "light"];

  const load = useCallback(async () => {
    setLoading(true);
    setErr(null);

    try {
      const res = await fetch(REPORT_API);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json();
      setData(json.data || MOCK.data);
    } catch (e) {
      setErr(e.message);
      setData(MOCK.data);
    } finally {
      setLastUpdated(new Date());
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const executeAction = useCallback(async (resourceType, row, selectedAction) => {
    setToast(null);
    setExecutingId(row.id);

    try {
      const action = selectedAction || row.action;
      const payload = {
        resourceType: resourceType.toUpperCase(),
        resourceId: row.id,
        action,
      };

      if (resourceType === "vpc") {
        payload.vpcResourceType = row.vpcResourceType;
      }

      const res = await fetch(EXECUTE_API, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify(payload),
      });

      const raw = await res.text();
      let json = null;
      try {
        json = raw ? JSON.parse(raw) : null;
      } catch {
        json = null;
      }

      if (!res.ok || json?.success === false || json?.status === "error") {
        const backendMessage = json?.message || json?.error || raw || `Execution failed with HTTP ${res.status}`;
        throw new Error(backendMessage);
      }

      setExecutionResults((prev) => ({
        ...prev,
        [row.id]: {
          status: "executed",
          state: actionToPostState(action),
          recommendation: `${postActionMessage(action)}. No further action needed.`,
        },
      }));

      setToast({ type: "success", message: json?.message || `Action executed for ${row.id}.` });
      await load();
    } catch (e) {
      setExecutionResults((prev) => ({
        ...prev,
        [row.id]: {
          status: "failed",
          state: "failed",
          recommendation: `Execution failed: ${e.message || "Unknown error"}. Retry available.`,
        },
      }));
      setToast({ type: "error", message: `Execution failed for ${row.id}: ${e.message || "Unknown error"}` });
    } finally {
      setExecutingId(null);
    }
  }, [load]);

  const ec2Rows = useMemo(() => (data ? getRowsForResource("ec2", data) : []), [data]);
  const ebsRows = useMemo(() => (data ? getRowsForResource("ebs", data) : []), [data]);
  const s3Rows = useMemo(() => (data ? getRowsForResource("s3", data) : []), [data]);
  const rdsRows = useMemo(() => (data ? getRowsForResource("rds", data) : []), [data]);
  const elbRows = useMemo(() => (data ? getRowsForResource("elb", data) : []), [data]);
  const asgRows = useMemo(() => (data ? getRowsForResource("asg", data) : []), [data]);
  const vpcRows = useMemo(() => (data ? getRowsForResource("vpc", data) : []), [data]);

  const renderPage = () => {
    if (!data) return null;

    const commonProps = {
      C,
      onExecute: executeAction,
      executingId,
      toast,
      actionSelections,
      onActionSelect: (id, action) => setActionSelections((p) => ({ ...p, [id]: action })),
      executionResults,
    };

    if (page === "dashboard") return <DashboardPage d={data} C={C} />;
    if (page === "ec2") return <ResourcePage page="ec2" title="EC2" icon="🖥️" rows={ec2Rows} {...commonProps} />;
    if (page === "ebs") return <ResourcePage page="ebs" title="EBS" icon="💾" rows={ebsRows} {...commonProps} />;
    if (page === "s3") return <ResourcePage page="s3" title="S3" icon="🗄️" rows={s3Rows} {...commonProps} />;
    if (page === "rds") return <ResourcePage page="rds" title="RDS" icon="🛢️" rows={rdsRows} {...commonProps} />;
    if (page === "elb") return <ResourcePage page="elb" title="ELB" icon="⚖️" rows={elbRows} {...commonProps} />;
    if (page === "asg") return <ResourcePage page="asg" title="ASG" icon="🔄" rows={asgRows} {...commonProps} />;
    if (page === "vpc") return <ResourcePage page="vpc" title="VPC" icon="🌐" rows={vpcRows} {...commonProps} />;

    return null;
  };

  return (
    <div style={{ display: "flex", height: "100vh", background: C.bg, color: C.text, fontFamily: "'Inter',system-ui,sans-serif", overflow: "hidden", transition: "background .2s,color .2s" }}>
      <div style={{ width: 220, background: C.surface, borderRight: `1px solid ${C.border}`, display: "flex", flexDirection: "column", flexShrink: 0 }}>
        <div style={{ padding: "24px 20px 20px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 32, height: 32, background: "linear-gradient(135deg,#6366f1,#06b6d4)", borderRadius: 8, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16 }}>☁️</div>
            <div>
              <div style={{ fontWeight: 800, fontSize: 15, letterSpacing: -0.3, color: C.text }}>CostGuard</div>
              <div style={{ color: C.muted, fontSize: 10 }}>AWS Cost and Resource Optimization System</div>
            </div>
          </div>
        </div>

        <div style={{ padding: "0 10px", flex: 1 }}>
          {nav.map((n) => (
            <button
              key={n.id}
              onClick={() => {
                setToast(null);
                setPage(n.id);
              }}
              style={{
                width: "100%",
                display: "flex",
                alignItems: "center",
                gap: 10,
                padding: "10px 12px",
                borderRadius: 9,
                border: "none",
                cursor: "pointer",
                background: page === n.id ? `${C.accent}22` : "transparent",
                color: page === n.id ? C.accent : C.muted,
                fontWeight: page === n.id ? 700 : 500,
                fontSize: 13,
                textAlign: "left",
                marginBottom: 2,
              }}
            >
              <span style={{ fontSize: 15 }}>{n.icon}</span>
              {n.label}
              {page === n.id && <div style={{ marginLeft: "auto", width: 4, height: 16, background: C.accent, borderRadius: 4 }} />}
            </button>
          ))}
        </div>

        <div style={{ padding: "16px 20px", borderTop: `1px solid ${C.border}`, display: "flex", flexDirection: "column", gap: 8 }}>
          {lastUpdated && <div style={{ fontSize: 10, color: C.muted }}>Updated {lastUpdated.toLocaleTimeString()}</div>}
          <button onClick={() => setDark((p) => !p)} style={{ width: "100%", background: C.cardBg, border: `1px solid ${C.border}`, color: C.text, padding: "8px 0", borderRadius: 8, fontWeight: 600, fontSize: 12, cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center", gap: 6 }}>
            {dark ? "☀️  Light Mode" : "🌙  Dark Mode"}
          </button>
          <button onClick={load} disabled={loading} style={{ width: "100%", background: "linear-gradient(135deg,#6366f1,#4f46e5)", border: "none", color: "#fff", padding: "8px 0", borderRadius: 8, fontWeight: 700, fontSize: 12, cursor: "pointer", opacity: loading ? 0.6 : 1 }}>
            {loading ? "Fetching..." : "↻  Refresh"}
          </button>
        </div>
      </div>

      <div style={{ flex: 1, overflow: "auto", padding: 32, background: C.bg }}>
        {err && (
          <div style={{ background: C.surface, border: `1px solid ${C.red}44`, borderRadius: 12, padding: 16, marginBottom: 24 }}>
            <div style={{ color: C.red, fontWeight: 700, marginBottom: 4 }}>⚠ Could not reach {REPORT_API}</div>
            <div style={{ color: C.muted, fontSize: 13 }}>{err} — Showing sample data. Make sure backend is running on port 8080.</div>
          </div>
        )}

        {loading && !data && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", height: "60vh", color: C.muted }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>⟳</div>
            <div>Connecting to backend...</div>
          </div>
        )}

        {data && renderPage()}
      </div>
    </div>
  );
}