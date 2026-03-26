import { useState, useEffect, useCallback } from "react";
import { AreaChart, Area, PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";

const API = "http://localhost:8080/cloud/report";
const MOCK = {"status":"success","message":"Cloud report generated successfully","data":{"summary":{"currentMonthlyCost":8.255,"potentialSavings":0,"optimizedMonthlyCost":8.255,"savingsPercentage":0,"efficiencyScore":100},"serviceCostBreakdown":{"ebs":0.64,"ec2":7.592,"s3":0.023,"rds":0,"elb":0,"autoscaling":0},"topExpensiveResources":[{"name":"i-089b03f5af5f06fcc","type":"EC2","monthlyCost":7.592},{"name":"vol-0d3fab40db3f12fb9","type":"EBS","monthlyCost":0.64},{"name":"costguard-demo","type":"S3","monthlyCost":0.023}],"topWastefulResources":[],"ec2Instances":[{"instanceId":"i-089b03f5af5f06fcc","instanceType":"t3.micro","state":"stopped","monthlyCost":7.592,"cpuUtilization":0,"recommendation":"Instance is stopped - no CPU data available"}],"ebsVolumes":[{"volumeId":"vol-0d3fab40db3f12fb9","size":8,"state":"in-use","volumeType":"gp3","monthlyCost":0.64,"recommendation":"Attached to stopped instance - if used regularly no action needed, otherwise consider snapshot and delete"}],"s3Buckets":[{"bucketName":"costguard-demo","storageGB":1,"monthlyCost":0.023,"recommendation":"Very low storage - minimal cost"}],"rds":{"reason":"No RDS databases found","status":"Not Initialized"},"elb":{"reason":"No Load Balancers found","status":"Not Initialized"},"autoscaling":{"reason":"No Auto Scaling Groups configured","status":"Not Initialized"},"optimizationInsights":["Apply lifecycle policies to optimize S3 storage cost","EC2 contributes highest cost - consider rightsizing instances"]}};

const THEMES = {
dark:  { bg:"#0f1117", surface:"#161b27", border:"#1f2937", text:"#f1f5f9", muted:"#64748b", accent:"#6366f1", green:"#10b981", red:"#ef4444", yellow:"#f59e0b", cyan:"#06b6d4", cardBg:"#12121c", insightBg:"#6366f110", insightBorder:"#6366f130", insightText:"#c7d2fe" },
light: { bg:"#f1f5f9", surface:"#ffffff", border:"#e2e8f0", text:"#0f172a", muted:"#64748b", accent:"#4f46e5", green:"#059669", red:"#dc2626", yellow:"#d97706", cyan:"#0891b2", cardBg:"#f8fafc", insightBg:"#eef2ff", insightBorder:"#c7d2fe", insightText:"#3730a3" },
};

const COLORS = ["#6366f1","#06b6d4","#f59e0b","#10b981","#ef4444","#a78bfa"];
const $ = n => `$${Number(n).toFixed(3)}`;
const stateColor = (s, C) => ({ running:C.green, stopped:C.red, "in-use":C.accent, available:C.cyan }[s] || C.muted);

const Badge = ({ s, C }) => <span style={{ background:stateColor(s,C)+"22", color:stateColor(s,C), border:`1px solid ${stateColor(s,C)}44`, borderRadius:6, padding:"2px 8px", fontSize:11, fontWeight:700 }}>{s}</span>;
const Stat = ({ label, value, color, icon, C }) => (
<div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, padding:"20px 24px", flex:1, minWidth:140 }}>
<div style={{ color:C.muted, fontSize:11, fontWeight:600, textTransform:"uppercase", letterSpacing:1, marginBottom:10 }}>{icon} {label}</div>
<div style={{ fontSize:28, fontWeight:800, color: color || C.accent }}>{value}</div>
</div>
);
const Section = ({ title, children, C }) => (
<div style={{ marginBottom:28 }}>
    <div style={{ fontSize:14, fontWeight:700, color:C.text, marginBottom:14, paddingBottom:10, borderBottom:`1px solid ${C.border}` }}>{title}</div>
    {children}
</div>
);
const Empty = ({ icon, msg, C }) => (
<div style={{ textAlign:"center", padding:"48px 20px", color:C.muted }}>
<div style={{ fontSize:36, marginBottom:10 }}>{icon}</div>
<div style={{ fontSize:14 }}>{msg}</div>
</div>
);
const Insight = ({ text, C }) => (
<div style={{ display:"flex", gap:12, alignItems:"flex-start", background:C.insightBg, border:`1px solid ${C.insightBorder}`, borderRadius:10, padding:"12px 16px", marginBottom:8 }}>
<span>💡</span><span style={{ color:C.insightText, fontSize:13, lineHeight:1.6 }}>{text}</span>
</div>
);
const TH = ({ children, C }) => <th style={{ textAlign:"left", padding:"10px 14px", color:C.muted, fontSize:11, fontWeight:600, textTransform:"uppercase", letterSpacing:.6, borderBottom:`1px solid ${C.border}` }}>{children}</th>;
const TD = ({ children, mono, color, C }) => <td style={{ padding:"13px 14px", fontSize:13, color: color || C.text, fontFamily: mono?"monospace":"inherit", borderBottom:`1px solid ${C.border}40` }}>{children}</td>;
const nav = [
{ id:"dashboard", icon:"⬛", label:"Dashboard" },
{ id:"ec2",       icon:"🖥️",  label:"EC2" },
{ id:"ebs",       icon:"💾",  label:"EBS" },
{ id:"s3",        icon:"🗄️",  label:"S3" },
{ id:"rds",       icon:"🛢️",  label:"RDS" },
{ id:"elb",       icon:"⚖️",  label:"ELB" },
{ id:"asg",       icon:"🔄",  label:"ASG" },
];

function DashboardPage({ d, C }) {
const breakdown = Object.entries(d.serviceCostBreakdown).filter(([,v])=>v>0).map(([k,v])=>({ name:k.toUpperCase(), value:+v.toFixed(3) }));
const trend = [1.2,2.4,3.1,2.8,4.5,5.2,6.0,6.8,7.4,7.9,8.1,8.255].map((v,i)=>({ month:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"][i], cost:v }));
return (
<div>
    <div style={{ marginBottom:24 }}>
        <div style={{ fontSize:22, fontWeight:800, color:C.text }}>Dashboard Overview</div>
        <div style={{ color:C.muted, fontSize:13, marginTop:4 }}>Real-time cloud cost & efficiency summary</div>
    </div>
    <div style={{ display:"flex", gap:14, flexWrap:"wrap", marginBottom:28 }}>
    <Stat C={C} icon="💰" label="Monthly Cost"     value={$(d.summary.currentMonthlyCost)}   color={C.accent}/>
        <Stat C={C} icon="✨" label="Potential Savings" value={$(d.summary.potentialSavings)}      color={C.green}/>
            <Stat C={C} icon="📉" label="Optimized Cost"   value={$(d.summary.optimizedMonthlyCost)} color={C.cyan}/>
                <Stat C={C} icon="🎯" label="Efficiency Score" value={`${d.summary.efficiencyScore}%`}    color={C.yellow}/>
</div>
<div style={{ display:"grid", gridTemplateColumns:"1.4fr 1fr", gap:20, marginBottom:28 }}>
<div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, padding:24 }}>
    <div style={{ fontSize:13, fontWeight:700, color:C.text, marginBottom:16 }}>Cost Trend (12 months)</div>
    <ResponsiveContainer width="100%" height={180}>
        <AreaChart data={trend}>
            <defs><linearGradient id="cg" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={C.accent} stopOpacity={.3}/><stop offset="95%" stopColor={C.accent} stopOpacity={0}/></linearGradient></defs>
            <CartesianGrid strokeDasharray="3 3" stroke={C.border}/>
                <XAxis dataKey="month" tick={{ fill:C.muted, fontSize:10 }} axisLine={false} tickLine={false}/>
                    <YAxis tick={{ fill:C.muted, fontSize:10 }} axisLine={false} tickLine={false} tickFormatter={v=>`$${v}`}/>
                        <Tooltip formatter={v=>[`$${v}`,"Cost"]} contentStyle={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:8, fontSize:12, color:C.text }}/>
                            <Area type="monotone" dataKey="cost" stroke={C.accent} strokeWidth={2} fill="url(#cg)"/>
        </AreaChart>
    </ResponsiveContainer>
</div>
<div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, padding:24 }}>
    <div style={{ fontSize:13, fontWeight:700, color:C.text, marginBottom:16 }}>Cost by Service</div>
    <ResponsiveContainer width="100%" height={130}>
        <PieChart>
            <Pie data={breakdown} cx="50%" cy="50%" innerRadius={40} outerRadius={62} dataKey="value" paddingAngle={4}>
                {breakdown.map((_,i)=><Cell key={i} fill={COLORS[i%COLORS.length]}/>)}
            </Pie>
            <Tooltip formatter={v=>`$${v}`} contentStyle={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:8, fontSize:12, color:C.text }}/>
        </PieChart>
    </ResponsiveContainer>
    <div style={{ display:"flex", flexDirection:"column", gap:6, marginTop:8 }}>
    {breakdown.map((b,i)=>(
    <div key={i} style={{ display:"flex", justifyContent:"space-between", alignItems:"center" }}>
    <div style={{ display:"flex", alignItems:"center", gap:6 }}>
    <div style={{ width:8, height:8, borderRadius:2, background:COLORS[i%COLORS.length] }}/>
    <span style={{ fontSize:12, color:C.muted }}>{b.name}</span>
</div>
<span style={{ fontSize:12, fontWeight:700, color:COLORS[i%COLORS.length] }}>${b.value}</span>
</div>
))}
</div>
</div>
</div>
<Section title="Top Expensive Resources" C={C}>
    <div style={{ display:"flex", gap:14, flexWrap:"wrap" }}>
    {d.topExpensiveResources.map((r,i)=>(
    <div key={i} style={{ flex:1, minWidth:160, background:C.cardBg, border:`1px solid ${C.border}`, borderRadius:12, padding:16 }}>
        <div style={{ fontSize:10, color:C.muted, textTransform:"uppercase", letterSpacing:.8, marginBottom:6 }}>#{i+1} · {r.type}</div>
    <div style={{ fontSize:12, color:C.accent, fontFamily:"monospace", marginBottom:8, wordBreak:"break-all" }}>{r.name}</div>
    <div style={{ fontSize:22, fontWeight:800, color:C.yellow }}>{$(r.monthlyCost)}<span style={{ fontSize:10, color:C.muted, fontWeight:400 }}>/mo</span></div>
    </div>
    ))}
    </div>
</Section>
<Section title="Optimization Insights" C={C}>
    {d.optimizationInsights.map((t,i)=><Insight key={i} text={t} C={C}/>)}
</Section>
</div>
);
}

function EC2Page({ instances, C }) {
const barData = instances.map(i=>({ id:i.instanceId.slice(-8), cpu:i.cpuUtilization, cost:i.monthlyCost }));
return (
<div>
    <div style={{ marginBottom:24 }}>
        <div style={{ fontSize:22, fontWeight:800, color:C.text }}>EC2 Instances</div>
        <div style={{ color:C.muted, fontSize:13, marginTop:4 }}>Instance usage, idle detection & cost analysis</div>
    </div>
    <div style={{ display:"flex", gap:14, flexWrap:"wrap", marginBottom:28 }}>
    <Stat C={C} icon="🖥️" label="Total Instances" value={instances.length} color={C.accent}/>
        <Stat C={C} icon="🔴" label="Stopped" value={instances.filter(i=>i.state==="stopped").length} color={C.red}/>
            <Stat C={C} icon="🟢" label="Running" value={instances.filter(i=>i.state==="running").length} color={C.green}/>
                <Stat C={C} icon="💰" label="Total Cost" value={$(instances.reduce((a,i)=>a+i.monthlyCost,0))} color={C.yellow}/>
</div>
{instances.length > 0 && (
<div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, padding:24, marginBottom:24 }}>
    <div style={{ fontSize:13, fontWeight:700, color:C.text, marginBottom:16 }}>CPU Utilization & Cost per Instance</div>
    <ResponsiveContainer width="100%" height={160}>
        <BarChart data={barData}>
            <CartesianGrid strokeDasharray="3 3" stroke={C.border}/>
                <XAxis dataKey="id" tick={{ fill:C.muted, fontSize:10 }} axisLine={false} tickLine={false}/>
                    <YAxis tick={{ fill:C.muted, fontSize:10 }} axisLine={false} tickLine={false}/>
                        <Tooltip contentStyle={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:8, fontSize:12, color:C.text }}/>
                        <Bar dataKey="cpu" name="CPU %" fill={C.cyan} radius={[4,4,0,0]}/>
                            <Bar dataKey="cost" name="Cost $" fill={C.accent} radius={[4,4,0,0]}/>
        </BarChart>
    </ResponsiveContainer>
</div>
)}
<div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, overflow:"hidden" }}>
{instances.length === 0 ? <Empty icon="🖥️" msg="No EC2 instances found" C={C}/> : (
    <table style={{ width:"100%", borderCollapse:"collapse" }}>
    <thead><tr>{["Instance ID","Type","State","CPU %","Monthly Cost","Recommendation"].map(h=><TH key={h} C={C}>{h}</TH>)}</tr></thead>
    <tbody>{instances.map((r,i)=>(
    <tr key={i}>
        <TD C={C} mono color={C.accent}>{r.instanceId}</TD>
        <TD C={C}><span style={{ background:C.accent+"22", color:C.accent, borderRadius:6, padding:"2px 8px", fontSize:11, fontWeight:700 }}>{r.instanceType}</span></TD>
        <TD C={C}><Badge s={r.state} C={C}/></TD>
        <TD C={C} color={r.cpuUtilization===0 ? C.red : C.green}>{r.cpuUtilization}%</TD>
        <TD C={C} color={C.yellow}>{$(r.monthlyCost)}</TD>
        <TD C={C} color={C.muted}>{r.recommendation}</TD>
    </tr>
    ))}</tbody>
    </table>
    )}
    </div>
    </div>
    );
    }

    function EBSPage({ volumes, C }) {
    return (
    <div>
        <div style={{ marginBottom:24 }}>
            <div style={{ fontSize:22, fontWeight:800, color:C.text }}>EBS Volumes</div>
            <div style={{ color:C.muted, fontSize:13, marginTop:4 }}>Unused & unattached volume detection</div>
        </div>
        <div style={{ display:"flex", gap:14, flexWrap:"wrap", marginBottom:28 }}>
        <Stat C={C} icon="💾" label="Total Volumes" value={volumes.length} color={C.accent}/>
            <Stat C={C} icon="📦" label="Total Storage" value={`${volumes.reduce((a,v)=>a+v.size,0)} GB`} color={C.cyan}/>
                <Stat C={C} icon="💰" label="Total Cost"    value={$(volumes.reduce((a,v)=>a+v.monthlyCost,0))} color={C.yellow}/>
    </div>
    <div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, overflow:"hidden" }}>
    {volumes.length === 0 ? <Empty icon="💾" msg="No EBS volumes found" C={C}/> : (
        <table style={{ width:"100%", borderCollapse:"collapse" }}>
        <thead><tr>{["Volume ID","Size","Type","State","Monthly Cost","Recommendation"].map(h=><TH key={h} C={C}>{h}</TH>)}</tr></thead>
        <tbody>{volumes.map((v,i)=>(
        <tr key={i}>
            <TD C={C} mono color={C.accent}>{v.volumeId}</TD>
            <TD C={C}>{v.size} GB</TD>
            <TD C={C}><span style={{ background:C.cyan+"22", color:C.cyan, borderRadius:6, padding:"2px 8px", fontSize:11, fontWeight:700 }}>{v.volumeType}</span></TD>
            <TD C={C}><Badge s={v.state} C={C}/></TD>
            <TD C={C} color={C.yellow}>{$(v.monthlyCost)}</TD>
            <TD C={C} color={C.muted}>{v.recommendation}</TD>
        </tr>
        ))}</tbody>
        </table>
        )}
        </div>
        </div>
        );
        }

        function S3Page({ buckets, C }) {
        return (
        <div>
            <div style={{ marginBottom:24 }}>
                <div style={{ fontSize:22, fontWeight:800, color:C.text }}>S3 Buckets</div>
                <div style={{ color:C.muted, fontSize:13, marginTop:4 }}>Storage usage & lifecycle suggestions</div>
            </div>
            <div style={{ display:"flex", gap:14, flexWrap:"wrap", marginBottom:28 }}>
            <Stat C={C} icon="🗄️" label="Total Buckets" value={buckets.length} color={C.accent}/>
                <Stat C={C} icon="📦" label="Total Storage" value={`${buckets.reduce((a,b)=>a+b.storageGB,0)} GB`} color={C.cyan}/>
                    <Stat C={C} icon="💰" label="Total Cost"    value={$(buckets.reduce((a,b)=>a+b.monthlyCost,0))} color={C.yellow}/>
        </div>
        <div style={{ display:"grid", gap:14 }}>
        {buckets.length === 0
        ? <div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14 }}><Empty icon="🗄️" msg="No S3 buckets found" C={C}/></div>
        : buckets.map((b,i)=>(
        <div key={i} style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, padding:20 }}>
            <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start" }}>
            <div>
                <div style={{ fontSize:15, fontWeight:700, color:C.accent, marginBottom:4 }}>{b.bucketName}</div>
                <div style={{ display:"flex", gap:16 }}>
                <span style={{ fontSize:12, color:C.muted }}>Storage: <strong style={{ color:C.text }}>{b.storageGB} GB</strong></span>
                <span style={{ fontSize:12, color:C.muted }}>Cost: <strong style={{ color:C.yellow }}>{$(b.monthlyCost)}/mo</strong></span>
            </div>
        </div>
        </div>
        {b.recommendation && <div style={{ marginTop:12, background:C.insightBg, border:`1px solid ${C.insightBorder}`, borderRadius:8, padding:"10px 14px", fontSize:12, color:C.insightText }}>💡 {b.recommendation}</div>}
        </div>
        ))}
        </div>
        </div>
        );
        }

        function NotInitPage({ icon, label, svc, C }) {
        return (
        <div>
            <div style={{ marginBottom:24 }}>
                <div style={{ fontSize:22, fontWeight:800, color:C.text }}>{icon} {label}</div>
                <div style={{ color:C.muted, fontSize:13, marginTop:4 }}>Monitoring & optimization</div>
            </div>
            <div style={{ background:C.surface, border:`1px solid ${C.border}`, borderRadius:14, padding:48, textAlign:"center" }}>
            <div style={{ fontSize:48, marginBottom:16 }}>{icon}</div>
            <div style={{ fontSize:16, fontWeight:700, color:C.text, marginBottom:8 }}>{svc.status}</div>
            <div style={{ fontSize:13, color:C.muted }}>{svc.reason}</div>
            <div style={{ marginTop:20, background:C.yellow+"11", border:`1px solid ${C.yellow}33`, borderRadius:8, padding:"10px 20px", display:"inline-block", fontSize:12, color:C.yellow }}>No cost incurred for this service</div>
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

        const C = THEMES[dark ? "dark" : "light"];

        const load = useCallback(async () => {
        setLoading(true); setErr(null);
        try {
        const res = await fetch(API);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const j = await res.json();
        setData(j.data); setLastUpdated(new Date());
        } catch(e) {
        setErr(e.message);
        setData(MOCK.data);
        setLastUpdated(new Date());
        }
        setLoading(false);
        }, []);

        useEffect(() => { load(); }, [load]);

        const renderPage = () => {
        if (!data) return null;
        if (page==="dashboard") return <DashboardPage d={data} C={C}/>;
            if (page==="ec2")       return <EC2Page instances={data.ec2Instances} C={C}/>;
                if (page==="ebs")       return <EBSPage volumes={data.ebsVolumes} C={C}/>;
                    if (page==="s3")        return <S3Page buckets={data.s3Buckets} C={C}/>;
                        if (page==="rds")       return <NotInitPage icon="🛢️" label="RDS" svc={data.rds} C={C}/>;
                            if (page==="elb")       return <NotInitPage icon="⚖️" label="ELB" svc={data.elb} C={C}/>;
                                if (page==="asg")       return <NotInitPage icon="🔄" label="Auto Scaling Groups" svc={data.autoscaling} C={C}/>;
                                    };

                                    return (
                                    <div style={{ display:"flex", height:"100vh", background:C.bg, color:C.text, fontFamily:"'Inter',system-ui,sans-serif", overflow:"hidden", transition:"background .2s,color .2s" }}>

                                    {/* Sidebar */}
                                    <div style={{ width:220, background:C.surface, borderRight:`1px solid ${C.border}`, display:"flex", flexDirection:"column", flexShrink:0 }}>
                                    <div style={{ padding:"24px 20px 20px" }}>
                                    <div style={{ display:"flex", alignItems:"center", gap:10 }}>
                                    <div style={{ width:32, height:32, background:"linear-gradient(135deg,#6366f1,#06b6d4)", borderRadius:8, display:"flex", alignItems:"center", justifyContent:"center", fontSize:16 }}>☁️</div>
                                    <div>
                                        <div style={{ fontWeight:800, fontSize:15, letterSpacing:-.3, color:C.text }}>CostGuard</div>
                                        <div style={{ color:C.muted, fontSize:10 }}>AWS Cost Optimizer</div>
                                    </div>
                                    </div>
                                    </div>

                                    <div style={{ padding:"0 10px", flex:1 }}>
                                    {nav.map(n=>(
                                    <button key={n.id} onClick={()=>setPage(n.id)} style={{ width:"100%", display:"flex", alignItems:"center", gap:10, padding:"10px 12px", borderRadius:9, border:"none", cursor:"pointer", background: page===n.id ? C.accent+"22" : "transparent", color: page===n.id ? C.accent : C.muted, fontWeight: page===n.id ? 700 : 500, fontSize:13, textAlign:"left", marginBottom:2 }}>
                                        <span style={{ fontSize:15 }}>{n.icon}</span>{n.label}
                                        {page===n.id && <div style={{ marginLeft:"auto", width:4, height:16, background:C.accent, borderRadius:4 }}/>}
                                    </button>
                                    ))}
                                    </div>

                                    <div style={{ padding:"16px 20px", borderTop:`1px solid ${C.border}`, display:"flex", flexDirection:"column", gap:8 }}>
                                    {lastUpdated && <div style={{ fontSize:10, color:C.muted }}>Updated {lastUpdated.toLocaleTimeString()}</div>}

                                    {/* Dark mode toggle */}
                                    <button onClick={()=>setDark(p=>!p)} style={{ width:"100%", background:C.cardBg, border:`1px solid ${C.border}`, color:C.text, padding:"8px 0", borderRadius:8, fontWeight:600, fontSize:12, cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", gap:6 }}>
                                        {dark ? "☀️  Light Mode" : "🌙  Dark Mode"}
                                    </button>

                                    <button onClick={load} disabled={loading} style={{ width:"100%", background:"linear-gradient(135deg,#6366f1,#4f46e5)", border:"none", color:"#fff", padding:"8px 0", borderRadius:8, fontWeight:700, fontSize:12, cursor:"pointer", opacity:loading?.6:1 }}>
                                    {loading ? "Fetching..." : "↻  Refresh"}
                                    </button>
                                    </div>
                                    </div>

                                    {/* Main */}
                                    <div style={{ flex:1, overflow:"auto", padding:32, background:C.bg }}>
                                    {err && (
                                    <div style={{ background:C.surface, border:`1px solid ${C.red}44`, borderRadius:12, padding:16, marginBottom:24 }}>
                                        <div style={{ color:C.red, fontWeight:700, marginBottom:4 }}>⚠ Could not reach {API}</div>
                                        <div style={{ color:C.muted, fontSize:13 }}>{err} — Showing sample data. Make sure your Spring Boot backend is running on port 8080.</div>
                                    </div>
                                    )}
                                    {loading && !data && (
                                    <div style={{ display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", height:"60vh", color:C.muted }}>
                                    <div style={{ fontSize:40, marginBottom:12 }}>⟳</div>
                                    <div>Connecting to backend...</div>
                                    </div>
                                    )}
                                    {data && renderPage()}
                                    </div>
                                    </div>
                                    );
                                    }